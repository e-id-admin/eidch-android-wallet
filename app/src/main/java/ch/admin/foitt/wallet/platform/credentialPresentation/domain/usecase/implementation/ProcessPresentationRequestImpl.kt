package ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequest
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequestContainer
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequestContainer.Jwt
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.CompatibleCredential
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
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import javax.inject.Inject

class ProcessPresentationRequestImpl @Inject constructor(
    private val validatePresentationRequest: ValidatePresentationRequest,
    private val getCompatibleCredentials: GetCompatibleCredentials,
    private val credentialRepository: CredentialRepo,
) : ProcessPresentationRequest {
    override suspend fun invoke(
        presentationRequestContainer: PresentationRequestContainer,
    ): Result<ProcessPresentationRequestResult, ProcessPresentationRequestError> = coroutineBinding {
        val presentationRequest = validatePresentationRequest(presentationRequestContainer)
            .mapError(ValidatePresentationRequestError::toProcessPresentationRequestError).bind()

        checkIfWalletIsEmpty().bind()

        val compatibleCredentials = findCompatibleCredentials(presentationRequest).bind()
        val shouldFetchTrustStatement = presentationRequestContainer.shouldFetchTrustStatements()

        when {
            compatibleCredentials.isEmpty() -> Err(CredentialPresentationError.NoCompatibleCredential).bind()
            compatibleCredentials.size == 1 -> ProcessPresentationRequestResult.Credential(
                credential = compatibleCredentials.first(),
                presentationRequest = presentationRequest,
                shouldFetchTrustStatements = shouldFetchTrustStatement,
            )
            else -> ProcessPresentationRequestResult.CredentialList(
                credentials = compatibleCredentials,
                presentationRequest = presentationRequest,
                shouldFetchTrustStatements = shouldFetchTrustStatement,
            )
        }
    }

    private fun PresentationRequestContainer.shouldFetchTrustStatements(): Boolean =
        this is Jwt

    private suspend fun checkIfWalletIsEmpty(): Result<Unit, ProcessPresentationRequestError> = coroutineBinding {
        val credentials = credentialRepository.getAll()
            .mapError(CredentialRepositoryError::toProcessPresentationRequestError)
            .bind()

        if (credentials.isEmpty()) {
            Err(CredentialPresentationError.EmptyWallet).bind()
        }
    }

    private suspend fun findCompatibleCredentials(
        presentationRequest: PresentationRequest
    ): Result<List<CompatibleCredential>, ProcessPresentationRequestError> =
        getCompatibleCredentials(presentationRequest.presentationDefinition.inputDescriptors)
            .mapError(GetCompatibleCredentialsError::toProcessPresentationRequestError)
}
