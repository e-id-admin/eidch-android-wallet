package ch.admin.foitt.wallet.platform.appSetupState.domain.implementation

import ch.admin.foitt.wallet.platform.appSetupState.domain.repository.FirstCredentialAddedRepository
import ch.admin.foitt.wallet.platform.appSetupState.domain.usecase.GetFirstCredentialWasAdded
import javax.inject.Inject

class GetFirstCredentialWasAddedImpl @Inject constructor(
    private val firstCredentialAddedRepository: FirstCredentialAddedRepository,
) : GetFirstCredentialWasAdded {
    override suspend fun invoke(): Boolean = firstCredentialAddedRepository.getIsAdded()
}
