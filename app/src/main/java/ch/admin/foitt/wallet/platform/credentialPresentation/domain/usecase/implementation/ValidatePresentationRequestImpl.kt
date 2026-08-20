package ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.SignatureAlgorithm
import ch.admin.foitt.openid4vc.domain.model.anycredential.Validity
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationRequest
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.ClientIdentifier
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.RequestObject
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.RequestObjectVerificationOutcome
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.getVerificationTrustStatementJwt
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtError
import ch.admin.foitt.openid4vc.domain.usecase.VerifyRequestObjectSignature
import ch.admin.foitt.wallet.platform.actorEnvironment.domain.model.ActorEnvironment
import ch.admin.foitt.wallet.platform.actorEnvironment.domain.usecase.GetActorEnvironment
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.CredentialPresentationError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.PresentationRequestWithRaw
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.ValidatePresentationRequestError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.VerificationProcessType
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.toValidatePresentationRequestError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.ValidatePresentationRequest
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.ValidateVerificationQueryPublicStatement
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.IdentityV2TrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.VerificationQueryPublicStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ProcessIdentityTrustStatement
import ch.admin.foitt.wallet.platform.utils.JsonParsingError
import ch.admin.foitt.wallet.platform.utils.SafeJson
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import kotlinx.serialization.json.jsonPrimitive
import uniffi.heidi_dcql_rust.DcqlQuery
import javax.inject.Inject

class ValidatePresentationRequestImpl @Inject constructor(
    private val safeJson: SafeJson,
    private val environmentSetupRepository: EnvironmentSetupRepository,
    private val processIdentityTrustStatement: ProcessIdentityTrustStatement,
    private val verifyRequestObjectSignature: VerifyRequestObjectSignature,
    private val getActorEnvironment: GetActorEnvironment,
    private val validateVqPs: ValidateVerificationQueryPublicStatement,
) : ValidatePresentationRequest {
    override suspend fun invoke(
        verificationProcessType: VerificationProcessType,
        requestObject: RequestObject
    ): Result<PresentationRequestWithRaw, ValidatePresentationRequestError> = coroutineBinding {
        val request = validateAuthorizationRequest(verificationProcessType, requestObject).bind()
        val verifierAttestationTrusted = verifierAttestationTrusted(
            requestObject,
            request.responseUri,
            request.state,
            verificationProcessType
        ).bind()
        val idTS = if (verifierAttestationTrusted != true) {
            validateIdentityTrust(request).bind()
        } else {
            null
        }
        PresentationRequestWithRaw(
            authorizationRequest = request,
            rawPresentationRequest = requestObject.jwt.rawJwt,
            verificationProcessType = verificationProcessType,
            verifierAttestationTrusted = verifierAttestationTrusted,
            dcqlQuery = request.getDcqlQuery().bind(),
            hasVerifiedQuery = verifierAttestationTrusted == true || idTS == null || // remove idTS check when contracting TP 1.0
                request.getVerificationTrustStatementJwt(VerificationQueryPublicStatement.TYPE) != null,
        )
    }

    private suspend fun validateAuthorizationRequest(
        verificationProcessType: VerificationProcessType,
        requestObject: RequestObject
    ): Result<AuthorizationRequest, ValidatePresentationRequestError> = coroutineBinding {
        val jwt = requestObject.jwt
        val authorizationRequest = safeJson.safeDecodeElementTo<AuthorizationRequest>(jwt.payloadJson)
            .mapError(JsonParsingError::toValidatePresentationRequestError)
            .bind()

        val responseUri = if (verificationProcessType == VerificationProcessType.NETWORK) {
            authorizationRequest.responseUri
                ?: Err(CredentialPresentationError.Unexpected(IllegalStateException("Response URI is missing"))).bind()
        } else {
            null
        }
        validateJwtClaims(jwt, verificationProcessType, responseUri).bind()

        val isClientIdentifierValid = authorizationRequest.clientIdentifier.isValid(requestObject, verificationProcessType)
        if (jwt.type != JWT_HEADER_TYP || !isClientIdentifierValid || !authorizationRequest.isValid()) {
            Err(CredentialPresentationError.InvalidRequest(responseUri, authorizationRequest.state)).bind<Unit>()
        }
        if (authorizationRequest.transactionData != null) {
            Err(CredentialPresentationError.InvalidTransactionData(responseUri, authorizationRequest.state)).bind<Unit>()
        }

        if (authorizationRequest.clientIdentifier.clientIdPrefix == ClientIdentifier.ClientIdPrefix.DecentralizedIdentifier) {
            val verifierEnvironment = getActorEnvironment(authorizationRequest.clientIdentifier.clientId)
            if (verifierEnvironment == ActorEnvironment.EXTERNAL) {
                Err(CredentialPresentationError.UnknownRegistry(responseUri, authorizationRequest.state)).bind()
            }
        }

        authorizationRequest
    }

    private fun validateJwtClaims(
        jwt: Jwt,
        verificationProcessType: VerificationProcessType,
        responseUri: String?,
    ): Result<Unit, ValidatePresentationRequestError> = runSuspendCatching {
        check(jwt.algorithm == SignatureAlgorithm.ES256.stdName)
        if (verificationProcessType == VerificationProcessType.NETWORK) {
            checkNotNull(jwt.keyId) { "keyId is missing" }
        }
        check(jwt.jwtValidity == Validity.Valid) { "jwt is not yet valid or expired" }
        // aud is currently optional due to expand phase
        val aud = jwt.payloadJson[CLAIM_AUDIENCE]?.jsonPrimitive?.content
        if (aud != null) {
            check(aud == "https://self-issued.me/v2" || aud == jwt.iss) { "neither static nor dynamic discovery is used" }
        }
    }.mapError { throwable ->
        throwable.toValidatePresentationRequestError(responseUri = responseUri, state = null, message = "validatePresentationRequest error")
    }

    private fun ClientIdentifier.isValid(requestObject: RequestObject, verificationProcessType: VerificationProcessType) =
        hasSameClientId(requestObject.clientId) && prefixMatchesType(verificationProcessType)

    private fun ClientIdentifier.prefixMatchesType(type: VerificationProcessType) =
        when (type) {
            VerificationProcessType.NETWORK -> clientIdPrefix == ClientIdentifier.ClientIdPrefix.DecentralizedIdentifier
            VerificationProcessType.PROXIMITY -> clientIdPrefix == ClientIdentifier.ClientIdPrefix.VerifierAttestationJwt
        }

    private fun AuthorizationRequest.isValid() =
        responseType == VP_TOKEN &&
            responseMode in SUPPORTED_RESPONSE_MODES &&
            (responseMode != DIRECT_POST_JWT || !clientMetaData?.jwks?.keys.isNullOrEmpty())

    private suspend fun verifierAttestationTrusted(
        requestObject: RequestObject,
        responseUri: String?,
        state: String?,
        verificationProcessType: VerificationProcessType
    ): Result<Boolean?, ValidatePresentationRequestError> = coroutineBinding {
        val verificationOutcome = verifyRequestObjectSignature(
            requestObject = requestObject,
            trustedAttestationDids = environmentSetupRepository.attestationsServiceTrustedDids,
        ).mapError { error ->
            error.toValidatePresentationRequestError(responseUri, state)
        }.bind()

        if (verificationOutcome == RequestObjectVerificationOutcome.ATTESTATION_UNTRUSTED &&
            verificationProcessType == VerificationProcessType.NETWORK
        ) {
            Err(VcSdJwtError.IssuerValidationFailed.toValidatePresentationRequestError(responseUri, state)).bind<Unit>()
        }
        when (verificationOutcome) {
            RequestObjectVerificationOutcome.ATTESTATION_TRUSTED -> true
            RequestObjectVerificationOutcome.ATTESTATION_UNTRUSTED -> false
            RequestObjectVerificationOutcome.DID_PATH -> null
        }
    }

    private suspend fun validateIdentityTrust(
        request: AuthorizationRequest
    ): Result<IdentityV2TrustStatement?, ValidatePresentationRequestError> = coroutineBinding {
        val identityV2Jwt = request.getVerificationTrustStatementJwt(IdentityV2TrustStatement.TYPE)
        processIdentityTrustStatement(
            identityTrustStatementJwt = identityV2Jwt,
            actorDid = request.clientIdentifier.clientId,
        ).mapError { error ->
            error.toValidatePresentationRequestError(request.responseUri, request.state)
        }.bind()
    }

    private suspend fun AuthorizationRequest.getDcqlQuery(): Result<DcqlQuery, ValidatePresentationRequestError> = coroutineBinding {
        val query = if (scope != null) {
            parseQueryFromVqPS(clientIdentifier.clientId).bind()
        } else {
            dcqlQuery ?: Err(CredentialPresentationError.InvalidRequest(responseUri, state)).bind<DcqlQuery>()
        }
        if (query.hasEmptyClaims() || (state == null && !query.requestsOnlyHolderBinding())) {
            Err(CredentialPresentationError.InvalidRequest(responseUri, state)).bind<DcqlQuery>()
        }
        query
    }

    private suspend fun AuthorizationRequest.parseQueryFromVqPS(
        actorDid: String,
    ): Result<DcqlQuery, ValidatePresentationRequestError> = coroutineBinding {
        val vqPSJwt = getVerificationTrustStatementJwt(VerificationQueryPublicStatement.TYPE)
            ?: Err(CredentialPresentationError.InvalidRequest(responseUri, state)).bind<Jwt>()
        val vqPS = validateVqPs(
            jwt = vqPSJwt,
            actorDid = actorDid,
        ).mapError { it.toValidatePresentationRequestError(responseUri, state) }.bind()
        if (vqPS.request.scope != scope) {
            Err(CredentialPresentationError.InvalidRequest(responseUri, state)).bind<DcqlQuery>()
        }
        vqPS.request.query
    }

    private fun DcqlQuery.hasEmptyClaims() = credentials?.any {
        (it.format == CredentialFormat.VC_SD_JWT.format || it.format == CredentialFormat.DC_SD_JWT.format) &&
            it.claims?.isEmpty() == true
    } ?: false

    // State field must be provided if no holder binding is requested
    // see https://openid.net/specs/openid-4-verifiable-presentations-1_0.html#section-5.3
    private fun DcqlQuery.requestsOnlyHolderBinding() = credentials?.all {
        it.requireCryptographicHolderBinding == true
    } ?: false

    private fun ClientIdentifier.hasSameClientId(
        clientId: String?
    ) = clientId == null || clientId.removePrefix(ClientIdentifier.ClientIdPrefix.DecentralizedIdentifier.value + ":") == this.clientId

    private companion object {
        const val JWT_HEADER_TYP = "oauth-authz-req+jwt"
        const val VP_TOKEN = "vp_token"
        const val DIRECT_POST_JWT = "direct_post.jwt"
        const val DC_API_JWT = "dc_api.jwt"
        val SUPPORTED_RESPONSE_MODES = listOf(DIRECT_POST_JWT, DC_API_JWT)
        const val CLAIM_AUDIENCE = "aud"
    }
}
