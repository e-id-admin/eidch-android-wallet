package ch.admin.foitt.openid4vc.domain.usecase

import ch.admin.foitt.openid4vc.domain.model.DeleteKeyPairError
import com.github.michaelbull.result.Result

internal fun interface DeleteKeyPair {
    suspend operator fun invoke(keyId: String): Result<Unit, DeleteKeyPairError>
}
