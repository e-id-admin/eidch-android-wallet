package ch.admin.foitt.wallet.platform.appAttestation.domain.usecase

import androidx.annotation.CheckResult
import ch.admin.foitt.openid4vc.domain.model.KeyStorageSecurityLevel
import ch.admin.foitt.openid4vc.domain.model.SigningAlgorithm
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.KeyAttestation
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.RequestKeyAttestationError
import com.github.michaelbull.result.Result

interface RequestKeyAttestation {
    @CheckResult
    suspend operator fun invoke(
        actorDid: String? = null,
        keyAlias: String? = KeyAttestation.KEY_ALIAS,
        signingAlgorithm: SigningAlgorithm = KeyAttestation.signingAlgorithm,
        keyStorageSecurityLevels: List<KeyStorageSecurityLevel>? = null,
    ): Result<KeyAttestation, RequestKeyAttestationError>
}
