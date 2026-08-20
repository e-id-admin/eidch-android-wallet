package ch.admin.foitt.openid4vc.domain.usecase

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.GetSignedMetadataDidError
import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import com.github.michaelbull.result.Result

interface GetSignedMetadataDid {
    suspend operator fun invoke(signedMetadataJwt: Jwt): Result<String, GetSignedMetadataDidError>
}
