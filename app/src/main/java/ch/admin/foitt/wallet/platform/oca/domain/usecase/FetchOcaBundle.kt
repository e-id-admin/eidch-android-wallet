package ch.admin.foitt.wallet.platform.oca.domain.usecase

import ch.admin.foitt.wallet.platform.oca.domain.model.FetchOcaBundleError
import ch.admin.foitt.wallet.platform.oca.domain.model.RawOcaBundle
import com.github.michaelbull.result.Result

interface FetchOcaBundle {
    suspend operator fun invoke(
        uri: String,
        integrity: String?, // uri#integrity is mandatory for https url, but optional for data uri
    ): Result<RawOcaBundle, FetchOcaBundleError>
}
