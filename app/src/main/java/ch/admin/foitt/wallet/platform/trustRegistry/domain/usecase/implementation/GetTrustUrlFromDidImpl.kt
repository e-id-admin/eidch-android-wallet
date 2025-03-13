package ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.GetTrustUrlFromDidError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.toGetTrustUrlFromDidError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.GetTrustUrlFromDid
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.binding
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import io.ktor.utils.io.charsets.name
import java.net.URL
import java.net.URLEncoder
import javax.inject.Inject

internal class GetTrustUrlFromDidImpl @Inject constructor(
    private val repo: EnvironmentSetupRepository
) : GetTrustUrlFromDid {
    override fun invoke(actorDid: String): Result<URL, GetTrustUrlFromDidError> = binding {
        val trustDomain = getTrustDomainForDid(didString = actorDid).bind()
        buildTrustUrl(actorDid = actorDid, trustDomain = trustDomain).bind()
    }

    private fun getTrustDomainForDid(didString: String): Result<String, GetTrustUrlFromDidError> = runSuspendCatching {
        val baseDomain = repo.baseTrustDomainRegex.find(didString)?.groups?.get(1)?.value
        val trustUrl = repo.trustRegistryMapping[baseDomain]
        if (trustUrl.isNullOrBlank()) {
            return Err(
                GetTrustUrlFromDidError.NoTrustRegistryMapping(message = "Could not get trust registry mapping for base domain")
            )
        }
        trustUrl
    }.mapError { throwable ->
        throwable.toGetTrustUrlFromDidError(message = "Failed to get trust domain for did")
    }

    private fun buildTrustUrl(
        actorDid: String,
        trustDomain: String,
    ): Result<URL, GetTrustUrlFromDidError> = runSuspendCatching {
        val didUrlEncoded = URLEncoder.encode(actorDid, Charsets.UTF_8.name)
        URL("$TRUST_SCHEME$trustDomain$TRUST_PATH$didUrlEncoded")
    }.mapError { throwable ->
        throwable.toGetTrustUrlFromDidError(message = "Failed to build trust URL")
    }

    private companion object {
        const val TRUST_SCHEME = "https://"
        const val TRUST_PATH = "/api/v1/truststatements/"
    }
}
