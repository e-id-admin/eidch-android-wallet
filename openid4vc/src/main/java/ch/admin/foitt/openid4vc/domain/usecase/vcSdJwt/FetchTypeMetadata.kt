package ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt

import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.FetchTypeMetadataError
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.TypeMetadata
import com.github.michaelbull.result.Result
import java.net.URL

interface FetchTypeMetadata {
    suspend operator fun invoke(vctUrl: URL, vctIntegrity: String?): Result<TypeMetadata, FetchTypeMetadataError>
}
