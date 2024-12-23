package ch.admin.foitt.wallet.platform.versionEnforcement.domain.usecase.implementation

import ch.admin.foitt.wallet.BuildConfig
import ch.admin.foitt.wallet.platform.utils.AppVersion
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.usecase.GetAppVersion
import javax.inject.Inject

class GetAppVersionImpl @Inject constructor() : GetAppVersion {
    override fun invoke(): AppVersion = AppVersion(BuildConfig.VERSION_NAME)
}
