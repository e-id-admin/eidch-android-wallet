package ch.admin.foitt.wallet.feature.qrscan.domain.usecase

import androidx.annotation.CheckResult
import ch.admin.foitt.wallet.feature.qrscan.domain.model.PermissionState

fun interface CheckQrScanPermission {
    @CheckResult
    suspend operator fun invoke(
        permissionsAreGranted: Boolean,
        rationaleShouldBeShown: Boolean,
        promptWasTriggered: Boolean,
    ): PermissionState
}
