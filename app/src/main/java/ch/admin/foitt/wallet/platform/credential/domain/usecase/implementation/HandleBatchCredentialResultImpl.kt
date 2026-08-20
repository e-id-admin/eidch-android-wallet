package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.BatchSize
import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyVerifiedBatchCredential
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.AnyCredentialConfiguration
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.RawAndParsedIssuerCredentialInfo
import ch.admin.foitt.wallet.platform.credential.domain.model.FetchCredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.FetchCredentialResult
import ch.admin.foitt.wallet.platform.credential.domain.usecase.HandleBatchCredentialResult
import ch.admin.foitt.wallet.platform.credential.domain.usecase.SaveVcSdJwtCredentials
import ch.admin.foitt.wallet.platform.credentialRefresh.domain.model.CredentialRefreshDataError
import ch.admin.foitt.wallet.platform.credentialRefresh.domain.repository.CredentialRefreshDataRepository
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.IdentityV2TrustStatement
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import java.net.URL
import javax.inject.Inject

class HandleBatchCredentialResultImpl @Inject constructor(
    private val saveVcSdJwtCredentials: SaveVcSdJwtCredentials,
    private val credentialRefreshDataRepository: CredentialRefreshDataRepository,
) : HandleBatchCredentialResult {
    override suspend fun invoke(
        credentialId: Long,
        issuerUrl: URL,
        batchSize: BatchSize,
        anyVerifiedBatchCredential: AnyVerifiedBatchCredential,
        identityTrustStatement: IdentityV2TrustStatement?,
        rawAndParsedCredentialInfo: RawAndParsedIssuerCredentialInfo,
        credentialConfig: AnyCredentialConfiguration,
    ): Result<FetchCredentialResult, FetchCredentialError> = coroutineBinding {
        val savedCredentialId = saveVcSdJwtCredentials(
            credentialId = credentialId,
            issuerUrl = issuerUrl,
            vcSdJwtCredentials = anyVerifiedBatchCredential.vcSdJwtCredentials,
            identityTrustStatement = identityTrustStatement,
            rawAndParsedCredentialInfo = rawAndParsedCredentialInfo,
            credentialConfig = credentialConfig,
        ).bind()

        anyVerifiedBatchCredential.refreshToken?.let { refreshToken ->
            credentialRefreshDataRepository.saveRefreshData(
                credentialId = savedCredentialId,
                batchSize = batchSize,
                accessToken = anyVerifiedBatchCredential.accessToken,
                refreshToken = refreshToken,
                dpopKeyBinding = anyVerifiedBatchCredential.dpopKeyBinding,
            ).mapError(CredentialRefreshDataError::toFetchCredentialError).bind()
        }
        FetchCredentialResult.Credential(savedCredentialId)
    }
}
