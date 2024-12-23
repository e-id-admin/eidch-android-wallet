package ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.GetTrustUrlFromDidError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.toGetTrustUrlFromDidError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.GetTrustUrlFromDid
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import io.ktor.utils.io.charsets.name
import java.net.URL
import java.net.URLEncoder
import javax.inject.Inject

internal class GetTrustUrlFromDidImpl @Inject constructor(
    private val repo: EnvironmentSetupRepository
) : GetTrustUrlFromDid {
    override fun invoke(actorDid: String): Result<URL, GetTrustUrlFromDidError> = runSuspendCatching {
        val trustDomain = getTrustDomainForDid(didString = actorDid)
        val trustEndpoint = trustDomain?.let {
            buildTrustUrl(trustDomain = trustDomain, actorDid = actorDid)
        }
        URL(trustEndpoint)
    }.mapError(Throwable::toGetTrustUrlFromDidError)

    private fun getTrustDomainForDid(didString: String): String? {
        val baseDomain = repo.baseTrustDomainRegex.find(didString)?.groups?.get(1)?.value ?: return null
        return repo.trustRegistryMapping.getOrDefault(baseDomain, null)
    }

    private fun buildTrustUrl(
        trustDomain: String,
        actorDid: String,
    ): String {
        val didUrlEncoded = URLEncoder.encode(actorDid, Charsets.UTF_8.name)
        return "$TRUST_SCHEME$trustDomain$TRUST_PATH$didUrlEncoded"
    }
    private companion object {
        const val TRUST_SCHEME = "https://"
        const val TRUST_PATH = "/api/v1/truststatements/"
    }
}
