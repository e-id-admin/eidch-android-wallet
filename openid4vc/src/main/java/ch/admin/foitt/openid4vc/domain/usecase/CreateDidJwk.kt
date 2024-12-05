package ch.admin.foitt.openid4vc.domain.usecase

import androidx.annotation.CheckResult
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CreateDidJwkError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.SigningAlgorithm
import com.github.michaelbull.result.Result
import java.security.KeyPair

internal interface CreateDidJwk {
    @CheckResult
    suspend operator fun invoke(
        keyPair: KeyPair,
        algorithm: SigningAlgorithm,
        asDid: Boolean = true
    ): Result<String, CreateDidJwkError>
}
