package ch.admin.foitt.openid4vc.domain.usecase.implementation

import ch.admin.foitt.didResolver.domain.DidResolverHelper
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.GetSignedMetadataDidError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.toUnexpected
import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.openid4vc.domain.usecase.GetSignedMetadataDid
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import javax.inject.Inject

internal class GetSignedMetadataDidImpl @Inject constructor(
    private val didResolverHelper: DidResolverHelper,
) : GetSignedMetadataDid {
    override suspend fun invoke(signedMetadataJwt: Jwt): Result<String, GetSignedMetadataDidError> = coroutineBinding {
        val keyId = runSuspendCatching {
            requireNotNull(signedMetadataJwt.keyId)
        }.bind()
        didResolverHelper.getDidStringFromAbsoluteKeyId(keyId)
            .bind()
    }.mapError(Throwable::toUnexpected)
}
