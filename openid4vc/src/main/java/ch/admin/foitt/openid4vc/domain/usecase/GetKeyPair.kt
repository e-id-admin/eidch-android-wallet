package ch.admin.foitt.openid4vc.domain.usecase

import ch.admin.foitt.openid4vc.domain.model.GetKeyPairError
import com.github.michaelbull.result.Result
import java.security.KeyPair

fun interface GetKeyPair {
    suspend operator fun invoke(
        keyId: String,
        provider: String,
    ): Result<KeyPair, GetKeyPairError>
}
