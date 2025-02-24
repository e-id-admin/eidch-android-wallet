package ch.admin.foitt.wallet.platform.cameraPermissionHandler.domain.usecase

import androidx.annotation.CheckResult
import ch.admin.foitt.wallet.platform.cameraPermissionHandler.domain.model.PermissionState

fun interface CheckCameraPermission {
    @CheckResult
    suspend operator fun invoke(
        permissionsAreGranted: Boolean,
        rationaleShouldBeShown: Boolean,
        promptWasTriggered: Boolean,
    ): PermissionState
}
