package ch.admin.foitt.wallet.platform.cameraPermissionHandler.domain.usecase

import androidx.annotation.CheckResult

fun interface ShouldAutoTriggerPermissionPrompt {
    @CheckResult
    suspend operator fun invoke(): Boolean
}
