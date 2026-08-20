package ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.ClaimsPathPointer
import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.ClaimsPathPointerComponent
import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationRequest
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.getVerificationTrustStatementJwt
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.PresentationRequestWithRaw
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.VerificationProcessType
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.IdentityV2TrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ProcessVerificationAuthorizationTrustStatementError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ProtectedVerificationClaims
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustRegistryError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ValidateVerificationAuthorizationTrustStatementError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.VerificationAuthorizationTrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.runOrUnexpected
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.toProcessVerificationAuthorizationTrustStatementError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ProcessVerificationAuthorizationTrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ValidateVerificationAuthorizationTrustStatement
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.onErr
import timber.log.Timber
import javax.inject.Inject

internal class ProcessVerificationAuthorizationTrustStatementImpl @Inject constructor(
    private val protectedVerificationClaims: ProtectedVerificationClaims,
    private val validateVerificationAuthorizationTrustStatement: ValidateVerificationAuthorizationTrustStatement,
) : ProcessVerificationAuthorizationTrustStatement {
    override suspend fun invoke(
        authorizationRequestWithRaw: PresentationRequestWithRaw,
        requestedClaims: List<ClaimsPathPointer>
    ): Result<Unit, ProcessVerificationAuthorizationTrustStatementError> = coroutineBinding {
        if (authorizationRequestWithRaw.isVerifiedProximity()) return@coroutineBinding
        if (!authorizationRequestWithRaw.authorizationRequest.hasIdTS()) return@coroutineBinding
        val authorizationRequest = authorizationRequestWithRaw.authorizationRequest
        val protectedClaims = getProtectedClaims(requestedClaims)
        if (protectedClaims.isNotEmpty()) {
            val jwt = getPvaTSJwt(authorizationRequest).bind()
            val trustStatement = validateVerificationAuthorizationTrustStatement(jwt, authorizationRequest.clientIdentifier.clientId)
                .mapError(ValidateVerificationAuthorizationTrustStatementError::toProcessVerificationAuthorizationTrustStatementError)
                .bind()
            runOrUnexpected {
                check(trustStatement.authorizedFields.containsAll(protectedClaims)) { "Protected claims without trust statement found" }
            }.bind()
        }
    }.onErr { error ->
        when (error) {
            is TrustRegistryError.UnknownRegistry -> {} // logged at root
            is TrustRegistryError.Unexpected -> Timber.e(t = error.cause, message = "process pvaTS: unauthorized verification")
        }
    }

    private fun PresentationRequestWithRaw.isVerifiedProximity() =
        verificationProcessType == VerificationProcessType.PROXIMITY && verifierAttestationTrusted == true

    private fun getProtectedClaims(requestedClaims: List<ClaimsPathPointer>) =
        requestedClaims.flatMap { path ->
            path.mapNotNull { component ->
                (component as? ClaimsPathPointerComponent.String)
                    ?.name
                    ?.takeIf(protectedVerificationClaims.claims::contains)
            }
        }.toSet()

    private fun AuthorizationRequest.hasIdTS(): Boolean =
        this.getVerificationTrustStatementJwt(IdentityV2TrustStatement.TYPE) != null

    private suspend fun getPvaTSJwt(
        authorizationRequest: AuthorizationRequest
    ): Result<Jwt, ProcessVerificationAuthorizationTrustStatementError> = coroutineBinding {
        val trustStatementJwt = authorizationRequest.getVerificationTrustStatementJwt(VerificationAuthorizationTrustStatement.TYPE)
        runOrUnexpected {
            checkNotNull(trustStatementJwt) { "Unauthorized protected claims requested" }
            trustStatementJwt
        }.bind()
    }
}
