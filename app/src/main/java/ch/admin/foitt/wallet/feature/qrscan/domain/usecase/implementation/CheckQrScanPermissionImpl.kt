package ch.admin.foitt.wallet.feature.qrscan.domain.usecase.implementation

import androidx.annotation.CheckResult
import ch.admin.foitt.wallet.feature.qrscan.domain.model.PermissionState
import ch.admin.foitt.wallet.feature.qrscan.domain.repository.CameraIntroRepository
import ch.admin.foitt.wallet.feature.qrscan.domain.usecase.CheckQrScanPermission
import timber.log.Timber
import javax.inject.Inject

class CheckQrScanPermissionImpl @Inject constructor(
    private val cameraIntroRepository: CameraIntroRepository,
) : CheckQrScanPermission {
    @CheckResult
    override suspend fun invoke(
        permissionsAreGranted: Boolean,
        rationaleShouldBeShown: Boolean,
        promptWasTriggered: Boolean,
    ): PermissionState {
        Timber.d(
            "Permission result\n" +
                "granted: $permissionsAreGranted\n" +
                "rationale should be shown: $rationaleShouldBeShown\n" +
                "prompt was Triggered: $promptWasTriggered"
        )

        if (promptWasTriggered) {
            cameraIntroRepository.setPermissionPromptWasTriggered(true)
        }

        val cameraIntroPromptTriggered = cameraIntroRepository.getPermissionPromptWasTriggered()

        return when {
            permissionsAreGranted -> PermissionState.Granted
            rationaleShouldBeShown -> PermissionState.Rationale
            !cameraIntroPromptTriggered -> PermissionState.Intro
            // Permission were refused after an active prompt
            // but no rationale was asked
            // Can happen for various reasons, like
            // - permissions were toggled/changed outside the app
            // - the system reset the permissions after some time
            // - the prompt was denied after a rationale
            // The state is not always consistent, but we have to assume the permission could be permanently denied
            // As of vanilla android 11.
            else -> PermissionState.Blocked
        }
    }
}
