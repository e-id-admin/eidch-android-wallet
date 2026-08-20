package ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.IdentityV2TrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ProcessIdentityTrustStatementError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ProcessIdentityV1TrustStatementError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustRegistryError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ValidateIdentityTrustStatementError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.toProcessIdentityTrustStatementError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ProcessIdentityTrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ProcessIdentityV1TrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ValidateIdentityTrustStatement
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.onErr
import timber.log.Timber
import javax.inject.Inject

class ProcessIdentityTrustStatementImpl @Inject constructor(
    private val validateIdentityTrustStatement: ValidateIdentityTrustStatement,
    private val environmentSetupRepository: EnvironmentSetupRepository,
    private val processIdentityV1TrustStatement: ProcessIdentityV1TrustStatement,
) : ProcessIdentityTrustStatement {
    override suspend operator fun invoke(
        identityTrustStatementJwt: Jwt?,
        actorDid: String,
    ): Result<IdentityV2TrustStatement?, ProcessIdentityTrustStatementError> = coroutineBinding {
        if (identityTrustStatementJwt != null) {
            validateIdentityTrustStatement(
                trustStatement = identityTrustStatementJwt,
                actorDid = actorDid,
            ).mapError(ValidateIdentityTrustStatementError::toProcessIdentityTrustStatementError)
                .bind()
        } else {
            if (environmentSetupRepository.terminateOnInvalidIdTSEnabled) {
                // in case of no v2 provided call v1 processing to make sure we have a valid v1 idTS before interaction with actor
                processIdentityV1TrustStatement(did = actorDid)
                    .mapError(ProcessIdentityV1TrustStatementError::toProcessIdentityTrustStatementError)
                    .bind()
            }
            null
        }
    }.onErr {
        when (it) {
            is TrustRegistryError.UnknownRegistry -> {} // logged at the root cause
            is TrustRegistryError.Unexpected -> Timber.e(t = it.cause, message = "Process idTS: Unverified actor")
        }
    }
}
