package ch.admin.foitt.wallet.platform.oca.domain.usecase

import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyCredential
import ch.admin.foitt.wallet.platform.oca.domain.model.FetchVcMetadataByFormatError
import ch.admin.foitt.wallet.platform.oca.domain.model.VcMetadata
import com.github.michaelbull.result.Result

interface FetchVcMetadataByFormat {
    suspend operator fun invoke(
        anyCredential: AnyCredential,
    ): Result<VcMetadata, FetchVcMetadataByFormatError>
}
