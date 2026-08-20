package ch.admin.foitt.wallet.platform.appAttestation.domain.usecase

import ch.admin.foitt.wallet.platform.appAttestation.domain.model.GetAttestationUrlFromDidError
import com.github.michaelbull.result.Result

fun interface GetAttestationUrlFromDid {
    operator fun invoke(
        actorDid: String?,
    ): Result<String, GetAttestationUrlFromDidError>
}
