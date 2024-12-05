package ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase

import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.GetTrustUrlFromDidError
import com.github.michaelbull.result.Result
import java.net.URL

fun interface GetTrustUrlFromDid {
    operator fun invoke(actorDid: String): Result<URL, GetTrustUrlFromDidError>
}
