package ch.admin.foitt.wallet.platform.utils

import ch.admin.foitt.wallet.BuildConfig
import com.github.michaelbull.result.getOr
import javax.inject.Inject

class BuildConfigProviderImpl @Inject constructor(
    private val safeJson: SafeJson,
) : BuildConfigProvider {
    override val appVersion: AppVersion
        get() = AppVersion(BuildConfig.VERSION_NAME)

    override val appVersionEnforcementUrl: String
        get() = BuildConfig.APP_VERSION_ENFORCEMENT_URL

    override val trustRegistryMapping: Map<String, String>
        get() = safeJson.safeDecodeStringTo<Map<String, String>>(BuildConfig.BASE_TO_TRUST_REGISTRY_MAPPING).getOr(mapOf())

    override val trustedDids: List<String>
        get() = safeJson.safeDecodeStringTo<List<String>>(BuildConfig.TRUSTED_DIDS).getOr(listOf())
}
