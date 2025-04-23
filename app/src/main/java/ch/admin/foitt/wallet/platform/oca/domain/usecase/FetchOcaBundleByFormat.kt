package ch.admin.foitt.wallet.platform.oca.domain.usecase

import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyCredential
import ch.admin.foitt.wallet.platform.oca.domain.model.FetchOcaBundleByFormatError
import com.github.michaelbull.result.Result

interface FetchOcaBundleByFormat {
    suspend operator fun invoke(
        anyCredential: AnyCredential
    ): Result<String?, FetchOcaBundleByFormatError>
}
