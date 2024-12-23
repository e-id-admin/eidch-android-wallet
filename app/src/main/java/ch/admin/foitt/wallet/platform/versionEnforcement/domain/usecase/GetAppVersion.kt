package ch.admin.foitt.wallet.platform.versionEnforcement.domain.usecase

import ch.admin.foitt.wallet.platform.utils.AppVersion

fun interface GetAppVersion {
    operator fun invoke(): AppVersion
}
