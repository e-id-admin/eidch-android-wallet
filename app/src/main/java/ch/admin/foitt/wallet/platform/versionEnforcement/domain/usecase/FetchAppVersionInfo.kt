package ch.admin.foitt.wallet.platform.versionEnforcement.domain.usecase

import androidx.annotation.CheckResult
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.model.AppVersionInfo

fun interface FetchAppVersionInfo {
    @CheckResult
    suspend operator fun invoke(): AppVersionInfo
}
