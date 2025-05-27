package ch.admin.foitt.wallet.platform.oca.domain.usecase

import ch.admin.foitt.wallet.platform.oca.domain.model.OcaBundle
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaBundlerError
import com.github.michaelbull.result.Result

interface OcaBundler {
    suspend operator fun invoke(jsonString: String): Result<OcaBundle, OcaBundlerError>
}
