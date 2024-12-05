package ch.admin.foitt.wallet.platform.appSetupState.domain.implementation

import ch.admin.foitt.wallet.platform.appSetupState.domain.repository.FirstCredentialAddedRepository
import ch.admin.foitt.wallet.platform.appSetupState.domain.usecase.SaveFirstCredentialWasAdded
import javax.inject.Inject

class SaveFirstCredentialWasAddedImpl @Inject constructor(
    private val firstCredentialAddedRepository: FirstCredentialAddedRepository,
) : SaveFirstCredentialWasAdded {
    override suspend fun invoke() = firstCredentialAddedRepository.setIsAdded()
}
