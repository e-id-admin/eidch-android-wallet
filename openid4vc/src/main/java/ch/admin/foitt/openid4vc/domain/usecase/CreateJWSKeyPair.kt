package ch.admin.foitt.openid4vc.domain.usecase

import androidx.annotation.CheckResult
import ch.admin.foitt.openid4vc.domain.model.CreateJWSKeyPairError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.JWSKeyPair
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.SigningAlgorithm
import com.github.michaelbull.result.Result

internal interface CreateJWSKeyPair {
    @CheckResult
    suspend operator fun invoke(
        signingAlgorithm: SigningAlgorithm,
        provider: String,
    ): Result<JWSKeyPair, CreateJWSKeyPairError>
}
