package ch.admin.foitt.wallet.platform.appAttestation.domain.usecase.implementation

import ch.admin.foitt.didResolver.domain.DidResolverHelper
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.GetAttestationUrlFromDidError
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.toUnexpected
import ch.admin.foitt.wallet.platform.appAttestation.domain.usecase.GetAttestationUrlFromDid
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.binding
import com.github.michaelbull.result.mapError
import javax.inject.Inject

internal class GetAttestationUrlFromDidImpl @Inject constructor(
    private val didResolverHelper: DidResolverHelper,
    private val environmentSetupRepository: EnvironmentSetupRepository
) : GetAttestationUrlFromDid {
    override fun invoke(
        actorDid: String?,
    ): Result<String, GetAttestationUrlFromDidError> = binding {
        if (actorDid == null) return@binding environmentSetupRepository.defaultAttestationServiceUrl

        val didUrl = didResolverHelper.getHttpsUrl(actorDid)
            .mapError(Throwable::toUnexpected).bind()

        environmentSetupRepository.attestationServiceMapping[didUrl.host] ?: environmentSetupRepository.defaultAttestationServiceUrl
    }
}
