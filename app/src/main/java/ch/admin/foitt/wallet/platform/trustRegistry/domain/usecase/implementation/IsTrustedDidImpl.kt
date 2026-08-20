package ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.implementation

import ch.admin.foitt.didResolver.domain.DidResolverHelper
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.GetTrustDomainFromDidError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.IsTrustedDidError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustRegistryError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.toIsTrustedDidError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.toUnexpected
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.GetTrustDomainFromDid
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.IsTrustedDid
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import javax.inject.Inject

internal class IsTrustedDidImpl @Inject constructor(
    private val didResolverHelper: DidResolverHelper,
    private val getTrustDomainFromDid: GetTrustDomainFromDid,
    private val environmentSetupRepository: EnvironmentSetupRepository,
) : IsTrustedDid {
    override suspend fun invoke(
        keyId: String,
        trustStatementType: String,
    ): Result<Unit, IsTrustedDidError> = coroutineBinding {
        val did = didResolverHelper.getDidStringFromAbsoluteKeyId(keyId)
            .mapError(Throwable::toUnexpected).bind()
        val trustDomain = getTrustDomainFromDid(did)
            .mapError(GetTrustDomainFromDidError::toIsTrustedDidError).bind()
        val trustedDids = environmentSetupRepository.trustRegistryTrustedDids[trustDomain]?.get(trustStatementType)
        if (trustedDids?.contains(did) != true) {
            Err(TrustRegistryError.UnknownRegistry).bind()
        }
    }
}
