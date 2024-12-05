package ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequest
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.CredentialPresentationError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.GetCompatibleCredentialsError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.ProcessPresentationRequestError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.ProcessPresentationRequestResult
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.ValidatePresentationRequestError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.toProcessPresentationRequestError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.GetCompatibleCredentials
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.ProcessPresentationRequest
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.ValidatePresentationRequest
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.CredentialRepo
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.andThen
import com.github.michaelbull.result.mapError
import javax.inject.Inject

class ProcessPresentationRequestImpl @Inject constructor(
    private val validatePresentationRequest: ValidatePresentationRequest,
    private val getCompatibleCredentials: GetCompatibleCredentials,
    private val credentialRepository: CredentialRepo,
) : ProcessPresentationRequest {
    override suspend fun invoke(
        presentationRequest: PresentationRequest
    ): Result<ProcessPresentationRequestResult, ProcessPresentationRequestError> =
        validatePresentationRequest(presentationRequest)
            .mapError(ValidatePresentationRequestError::toProcessPresentationRequestError)
            .andThen {
                getAllCredentials(presentationRequest)
            }

    private suspend fun getAllCredentials(
        presentationRequest: PresentationRequest,
    ): Result<ProcessPresentationRequestResult, ProcessPresentationRequestError> =
        credentialRepository.getAll()
            .mapError(CredentialRepositoryError::toProcessPresentationRequestError)
            .andThen { credentials ->
                if (credentials.isEmpty()) {
                    Err(CredentialPresentationError.EmptyWallet)
                } else {
                    findCompatibleCredentials(presentationRequest)
                }
            }

    private suspend fun findCompatibleCredentials(
        presentationRequest: PresentationRequest
    ): Result<ProcessPresentationRequestResult, ProcessPresentationRequestError> =
        getCompatibleCredentials(presentationRequest.presentationDefinition.inputDescriptors)
            .mapError(GetCompatibleCredentialsError::toProcessPresentationRequestError)
            .andThen { credentials ->
                when {
                    credentials.isEmpty() -> Err(CredentialPresentationError.NoCompatibleCredential)
                    credentials.size == 1 -> Ok(ProcessPresentationRequestResult.Credential(credentials.first()))
                    else -> Ok(ProcessPresentationRequestResult.CredentialList(credentials))
                }
            }
}
