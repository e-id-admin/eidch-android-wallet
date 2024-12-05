package ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.GetTrustUrlFromDidError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.toGetTrustUrlFromDidError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.GetTrustUrlFromDid
import ch.admin.foitt.wallet.platform.utils.BuildConfigProvider
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import io.ktor.utils.io.charsets.name
import java.net.URL
import java.net.URLEncoder
import javax.inject.Inject

internal class GetTrustUrlFromDidImpl @Inject constructor(
    private val buildConfigProvider: BuildConfigProvider,
) : GetTrustUrlFromDid {
    override fun invoke(actorDid: String): Result<URL, GetTrustUrlFromDidError> = runSuspendCatching {
        val baseDomain: String? = BASE_DOMAIN_REGEX.find(actorDid)?.groups?.get(1)?.value
        val trustDomain = buildConfigProvider.trustRegistryMapping[baseDomain]

        val trustEndpoint = trustDomain?.let {
            buildTrustUrl(trustDomain = trustDomain, actorDid = actorDid)
        }
        URL(trustEndpoint)
    }.mapError(Throwable::toGetTrustUrlFromDidError)

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

        val BASE_DOMAIN_REGEX: Regex by lazy {
            Regex("^did:tdw:[^:]+:([^:]+\\.bit\\.admin\\.ch):[^:]+", setOf(RegexOption.MULTILINE))
        }
    }
}
