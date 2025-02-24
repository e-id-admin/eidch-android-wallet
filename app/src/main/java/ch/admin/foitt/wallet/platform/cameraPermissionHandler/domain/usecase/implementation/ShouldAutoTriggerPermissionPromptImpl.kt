package ch.admin.foitt.wallet.platform.cameraPermissionHandler.domain.usecase.implementation

import androidx.annotation.CheckResult
import ch.admin.foitt.wallet.platform.cameraPermissionHandler.domain.repository.CameraIntroRepository
import ch.admin.foitt.wallet.platform.cameraPermissionHandler.domain.usecase.ShouldAutoTriggerPermissionPrompt
import javax.inject.Inject

class ShouldAutoTriggerPermissionPromptImpl @Inject constructor(
    private val cameraIntroRepository: CameraIntroRepository,
) : ShouldAutoTriggerPermissionPrompt {
    @CheckResult
    override suspend fun invoke(): Boolean = cameraIntroRepository.getPermissionPromptWasTriggered()
}
