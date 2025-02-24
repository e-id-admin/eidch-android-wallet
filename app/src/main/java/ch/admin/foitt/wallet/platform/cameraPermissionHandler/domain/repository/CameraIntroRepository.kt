package ch.admin.foitt.wallet.platform.cameraPermissionHandler.domain.repository

interface CameraIntroRepository {
    suspend fun getPermissionPromptWasTriggered(): Boolean
    suspend fun setPermissionPromptWasTriggered(introWasPassed: Boolean)
}
