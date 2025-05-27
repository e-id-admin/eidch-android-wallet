package ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt

import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.FetchVcSchemaError
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSchema
import com.github.michaelbull.result.Result
import java.net.URL

interface FetchVcSchema {
    suspend operator fun invoke(
        schemaUrl: URL,
        schemaUriIntegrity: String?
    ): Result<VcSchema, FetchVcSchemaError>
}
