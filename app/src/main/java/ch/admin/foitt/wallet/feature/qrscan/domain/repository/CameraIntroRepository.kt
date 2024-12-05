package ch.admin.foitt.wallet.feature.qrscan.domain.repository

interface CameraIntroRepository {
    suspend fun getPermissionPromptWasTriggered(): Boolean
    suspend fun setPermissionPromptWasTriggered(introWasPassed: Boolean)
}
