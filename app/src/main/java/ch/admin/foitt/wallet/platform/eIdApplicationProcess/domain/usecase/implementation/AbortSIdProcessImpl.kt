package ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.appAttestation.domain.model.GenerateProofOfPossessionError
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.RequestClientAttestationError
import ch.admin.foitt.wallet.platform.appAttestation.domain.usecase.GenerateProofOfPossession
import ch.admin.foitt.wallet.platform.appAttestation.domain.usecase.RequestClientAttestation
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.AbortSIdProcessError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.SIdRepositoryError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.toAbortSIdProcessError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.SIdRepository
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.AbortSIdProcess
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import kotlinx.serialization.json.buildJsonObject
import javax.inject.Inject

class AbortSIdProcessImpl @Inject constructor(
    private val sIdRepository: SIdRepository,
    private val requestClientAttestation: RequestClientAttestation,
    private val generateProofOfPossession: GenerateProofOfPossession,
    private val environmentSetupRepository: EnvironmentSetupRepository,
) : AbortSIdProcess {
    override suspend fun invoke(caseId: String): Result<Unit, AbortSIdProcessError> = coroutineBinding {
        val clientAttestation = requestClientAttestation()
            .mapError(RequestClientAttestationError::toAbortSIdProcessError).bind()

        val challenge = sIdRepository.fetchChallenge()
            .mapError(SIdRepositoryError::toAbortSIdProcessError).bind().challenge

        val pop = generateProofOfPossession(
            clientAttestation = clientAttestation,
            challenge = challenge,
            audience = environmentSetupRepository.sidBackendUrl,
            requestBody = buildJsonObject {},
        ).mapError(GenerateProofOfPossessionError::toAbortSIdProcessError).bind()

        sIdRepository.abortSIdProcess(
            caseId = caseId,
            clientAttestation = clientAttestation,
            clientAttestationPoP = pop,
        ).mapError(SIdRepositoryError::toAbortSIdProcessError).bind()
    }
}
