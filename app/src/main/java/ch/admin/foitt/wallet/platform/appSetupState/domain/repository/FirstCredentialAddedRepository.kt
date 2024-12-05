package ch.admin.foitt.wallet.platform.appSetupState.domain.repository

interface FirstCredentialAddedRepository {
    suspend fun getIsAdded(): Boolean
    suspend fun setIsAdded()
}
