package ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialDisplayData
import ch.admin.foitt.wallet.platform.credential.domain.model.MapToCredentialDisplayDataError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.MapToCredentialDisplayData
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialWithDisplays
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialWithDisplaysRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.GetCredentialsWithDisplaysFlowError
import ch.admin.foitt.wallet.platform.ssi.domain.model.toGetCredentialWithDisplaysFlowError
import ch.admin.foitt.wallet.platform.ssi.domain.model.toGetCredentialsWithDisplaysFlowError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.CredentialWithDisplaysRepository
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.GetCredentialsWithDisplaysFlow
import ch.admin.foitt.wallet.platform.utils.andThen
import ch.admin.foitt.wallet.platform.utils.mapError
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCredentialsWithDisplaysFlowImpl @Inject constructor(
    private val credentialWithDisplaysRepository: CredentialWithDisplaysRepository,
    private val mapToCredentialDisplayData: MapToCredentialDisplayData
) : GetCredentialsWithDisplaysFlow {
    override fun invoke(): Flow<Result<List<CredentialDisplayData>, GetCredentialsWithDisplaysFlowError>> =
        credentialWithDisplaysRepository.getCredentialsWithDisplays()
            .mapError(CredentialWithDisplaysRepositoryError::toGetCredentialWithDisplaysFlowError)
            .andThen { credentials ->
                coroutineBinding {
                    createCredentialDisplayData(
                        credentials = credentials,
                    ).bind()
                }
            }

    private suspend fun createCredentialDisplayData(
        credentials: List<CredentialWithDisplays>
    ): Result<List<CredentialDisplayData>, GetCredentialsWithDisplaysFlowError> = coroutineBinding {
        credentials.map { credentialWithDisplays ->
            val credentialDisplayData = mapToCredentialDisplayData(
                credential = credentialWithDisplays.credential,
                credentialDisplays = credentialWithDisplays.displays
            ).mapError(MapToCredentialDisplayDataError::toGetCredentialsWithDisplaysFlowError)
                .bind()

            credentialDisplayData
        }
    }
}
