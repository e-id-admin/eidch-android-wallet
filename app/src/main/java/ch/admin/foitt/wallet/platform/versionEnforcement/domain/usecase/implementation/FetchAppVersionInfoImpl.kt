package ch.admin.foitt.wallet.platform.versionEnforcement.domain.usecase.implementation

import androidx.annotation.CheckResult
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetLocalizedDisplay
import ch.admin.foitt.wallet.platform.utils.AppVersion
import ch.admin.foitt.wallet.platform.utils.BuildConfigProvider
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.model.AppVersionInfo
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.model.VersionEnforcement
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.repository.VersionEnforcementRepository
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.usecase.FetchAppVersionInfo
import com.github.michaelbull.result.mapBoth
import javax.inject.Inject

internal class FetchAppVersionInfoImpl @Inject constructor(
    buildConfigProvider: BuildConfigProvider,
    private val versionEnforcementRepository: VersionEnforcementRepository,
    private val getLocalizedDisplay: GetLocalizedDisplay,
) : FetchAppVersionInfo {

    val version by lazy { buildConfigProvider.appVersion }

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
