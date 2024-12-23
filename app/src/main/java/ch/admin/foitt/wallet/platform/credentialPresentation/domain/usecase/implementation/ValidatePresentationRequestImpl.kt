package ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.InputDescriptorFormat
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.JwtPresentationRequest
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequest
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequest.Companion.DID
import ch.admin.foitt.openid4vc.domain.usecase.VerifyJwtSignature
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.CredentialPresentationError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.ValidatePresentationRequestError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.toValidatePresentationRequestError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.ValidatePresentationRequest
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import javax.inject.Inject

class ValidatePresentationRequestImpl @Inject constructor(
    private val verifyJwtSignature: VerifyJwtSignature,
) : ValidatePresentationRequest {
    override suspend fun invoke(
        presentationRequest: PresentationRequest
    ): Result<Unit, ValidatePresentationRequestError> = coroutineBinding {
        validatePresentationRequest(presentationRequest).bind()
        when (presentationRequest) {
            is JwtPresentationRequest -> validateJwtPresentationRequest(presentationRequest).bind()
            else -> Unit
        }
    }

    private fun validatePresentationRequest(
        presentationRequest: PresentationRequest
    ): Result<Unit, ValidatePresentationRequestError> = when {
        presentationRequest.responseType != VP_TOKEN -> Err(
            CredentialPresentationError.InvalidPresentation(presentationRequest.responseUri)
        )

        presentationRequest.responseMode != DIRECT_POST -> Err(
            CredentialPresentationError.InvalidPresentation(presentationRequest.responseUri)
        )

        presentationRequest.clientIdScheme == null -> Err(
            CredentialPresentationError.InvalidPresentation(presentationRequest.responseUri)
        )

        presentationRequest.clientIdScheme != DID -> Err(
            CredentialPresentationError.InvalidPresentation(presentationRequest.responseUri)
        )

        !presentationRequest.clientId.matches(DID_REGEX) -> Err(
            CredentialPresentationError.InvalidPresentation(presentationRequest.responseUri)
        )

        presentationRequest.isFieldsEmpty() -> Err(
            CredentialPresentationError.InvalidPresentation(presentationRequest.responseUri)
        )

        else -> Ok(Unit)
    }

    private fun PresentationRequest.isFieldsEmpty() = presentationDefinition.inputDescriptors.any { inputDescriptor ->
        inputDescriptor.formats.any { format ->
            format is InputDescriptorFormat.VcSdJwt &&
                inputDescriptor.constraints.fields.isEmpty()
        }
    }

    private suspend fun validateJwtPresentationRequest(
        presentationRequest: JwtPresentationRequest
    ): Result<Unit, ValidatePresentationRequestError> = coroutineBinding {
        val jwtHeader = presentationRequest.signedJWT.header
        when {
            jwtHeader.algorithm.name != SigningAlgorithm.ES256.stdName -> Err(
                CredentialPresentationError.InvalidPresentation(presentationRequest.responseUri)
            ).bind<Unit>()

            jwtHeader.keyID == null -> Err(
                CredentialPresentationError.InvalidPresentation(presentationRequest.responseUri)
            ).bind<Unit>()
        }

        verifyJwtSignature(
            did = presentationRequest.clientId,
            kid = jwtHeader.keyID,
            signedJwt = presentationRequest.signedJWT
        )
            .mapError { error -> error.toValidatePresentationRequestError(presentationRequest.responseUri) }
            .bind()
    }

    private companion object {
        const val VP_TOKEN = "vp_token"
        val DID_REGEX = Regex("^did:[a-z0-9]+:[a-zA-Z0-9.\\-_:]+\$")
        const val DIRECT_POST = "direct_post"
    }
}
