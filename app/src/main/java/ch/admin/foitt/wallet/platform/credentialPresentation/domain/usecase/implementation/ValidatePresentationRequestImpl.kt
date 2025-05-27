package ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.InputDescriptorFormat
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequest
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequestContainer
import ch.admin.foitt.openid4vc.domain.usecase.VerifyJwtSignature
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.CredentialPresentationError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.ValidatePresentationRequestError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.toValidatePresentationRequestError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.ValidatePresentationRequest
import ch.admin.foitt.wallet.platform.utils.JsonParsingError
import ch.admin.foitt.wallet.platform.utils.SafeJson
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import kotlinx.serialization.json.jsonPrimitive
import timber.log.Timber
import javax.inject.Inject

class ValidatePresentationRequestImpl @Inject constructor(
    private val safeJson: SafeJson,
    private val verifyJwtSignature: VerifyJwtSignature,
) : ValidatePresentationRequest {
    override suspend fun invoke(
        presentationRequestContainer: PresentationRequestContainer
    ): Result<PresentationRequest, ValidatePresentationRequestError> = coroutineBinding {
        val presentationRequest: PresentationRequest = when (presentationRequestContainer) {
            is PresentationRequestContainer.Jwt -> validateJwtPresentationRequest(presentationRequestContainer).bind()
            is PresentationRequestContainer.Json -> presentationRequestContainer.toPresentationRequest().bind()
        }
        validatePresentationRequest(presentationRequest).bind()
        presentationRequest
    }

    private fun validatePresentationRequest(
        presentationRequest: PresentationRequest
    ): Result<Unit, ValidatePresentationRequestError> {
        val validationError = Err(CredentialPresentationError.InvalidPresentation(presentationRequest.responseUri))
        return when {
            presentationRequest.responseType != VP_TOKEN -> validationError
            presentationRequest.responseMode != DIRECT_POST -> validationError
            presentationRequest.clientIdScheme == null -> validationError
            presentationRequest.clientIdScheme != ID_SCHEME_DID -> validationError
            !presentationRequest.clientId.matches(DID_REGEX) -> validationError
            presentationRequest.isFieldsEmpty() -> validationError
            presentationRequest.hasInvalidConstraintsPath() -> validationError
            else -> Ok(Unit)
        }
    }

    private fun PresentationRequest.hasInvalidConstraintsPath(): Boolean {
        // JsonPath filter expressions in path are not allowed.
        // The filter expression starts with "[?" and may contain whitespace between these characters
        val invalidConstrainPath = """.*\[\s*\?.*""".toRegex()
        return presentationDefinition.inputDescriptors.any { inputDescriptor ->
            inputDescriptor.constraints.fields.any { field ->
                field.path.any { path ->
                    invalidConstrainPath.matches(path)
                }
            }
        }
    }

    private fun PresentationRequest.isFieldsEmpty() = presentationDefinition.inputDescriptors.any { inputDescriptor ->
        inputDescriptor.formats.any { format ->
            format is InputDescriptorFormat.VcSdJwt &&
                inputDescriptor.constraints.fields.isEmpty()
        }
    }

    private suspend fun validateJwtPresentationRequest(
        container: PresentationRequestContainer.Jwt,
    ): Result<PresentationRequest, ValidatePresentationRequestError> = coroutineBinding {
        val jwt = container.jwt
        val responseUri = runSuspendCatching {
            checkNotNull(jwt.payloadJson[CLAIM_RESPONSE_URI]?.jsonPrimitive?.content)
        }.mapError { throwable ->
            CredentialPresentationError.Unexpected(throwable)
        }.bind()

        val validationError = Err(CredentialPresentationError.InvalidPresentation(responseUri))

        if (jwt.algorithm != SigningAlgorithm.ES256.stdName) {
            validationError.bind<ValidatePresentationRequestError>()
        }

        runSuspendCatching {
            val issuerDid = checkNotNull(jwt.iss) { "issuer is missing" }
            val keyId = checkNotNull(jwt.keyId) { "keyId is missing" }

            verifyJwtSignature(
                did = issuerDid,
                kid = keyId,
                jwt = jwt,
            )
                .mapError { error -> error.toValidatePresentationRequestError(responseUri) }
                .bind()
        }.mapError { throwable ->
            Timber.w(t = throwable)
            throwable.toValidatePresentationRequestError(responseUri = responseUri, message = "validateJwtPresentationRequest error")
        }.bind()

        safeJson.safeDecodeElementTo<PresentationRequest>(jwt.payloadJson).mapError { error ->
            CredentialPresentationError.Unexpected(null)
        }.bind()
    }

    private fun PresentationRequestContainer.Json.toPresentationRequest():
        Result<PresentationRequest, ValidatePresentationRequestError> = safeJson.safeDecodeElementTo<PresentationRequest>(json)
        .mapError(JsonParsingError::toValidatePresentationRequestError)

    private companion object {
        const val ID_SCHEME_DID = "did"
        const val VP_TOKEN = "vp_token"
        val DID_REGEX = Regex("^did:[a-z0-9]+:[a-zA-Z0-9.\\-_:]+$")
        const val DIRECT_POST = "direct_post"
        const val CLAIM_RESPONSE_URI = "response_uri"
    }
}
