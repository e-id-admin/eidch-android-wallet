package ch.admin.foitt.wallet.platform.versionEnforcement.domain.usecase.implementation

import androidx.annotation.CheckResult
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetLocalizedDisplay
import ch.admin.foitt.wallet.platform.utils.AppVersion
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.model.AppVersionInfo
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.model.VersionEnforcement
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.repository.VersionEnforcementRepository
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.usecase.FetchAppVersionInfo
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.usecase.GetAppVersion
import com.github.michaelbull.result.mapBoth
import javax.inject.Inject

internal class FetchAppVersionInfoImpl @Inject constructor(
    private val versionEnforcementRepository: VersionEnforcementRepository,
    private val getAppVersion: GetAppVersion,
    private val getLocalizedDisplay: GetLocalizedDisplay,
) : FetchAppVersionInfo {

    val version by lazy { getAppVersion() }

    @CheckResult
    override suspend fun invoke(): AppVersionInfo =
        versionEnforcementRepository.fetchLatestHighPriority()
            .mapBoth(
                success = ::checkEnforcement,
                failure = { AppVersionInfo.Unknown }
            )

    private fun checkEnforcement(enforcement: VersionEnforcement?): AppVersionInfo {
        return if (enforcement != null && enforcement.criteria.isMet(version)) {
            val display = getLocalizedDisplay(enforcement.displays)
            AppVersionInfo.Blocked(display?.title, display?.text)
        } else {
            AppVersionInfo.Valid
        }
    }

    private fun VersionEnforcement.Criteria.isMet(version: AppVersion): Boolean {
        val isHigherThanMin = minAppVersionIncluded?.let {
            minAppVersionIncluded <= version
        } ?: true
        val isSmallerThanMax = version < maxAppVersionExcluded
        return isSmallerThanMax && isHigherThanMin
    }
}
