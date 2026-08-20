package ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyCredential
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.AnyCredentialConfiguration
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.IssuerCredentialInfo
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorMetaDataError
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.FetchAndCacheIssuerDisplayDataError
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.toFetchAndCacheIssuerDisplayDataError
import ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.CacheIssuerDisplayData
import ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.FetchAndCacheIssuerDisplayData
import ch.admin.foitt.wallet.platform.credential.domain.model.AnyIssuerDisplay
import ch.admin.foitt.wallet.platform.credential.domain.model.FetchTrustForIssuanceError
import ch.admin.foitt.wallet.platform.credential.domain.model.GetAllAnyCredentialsByCredentialIdError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.FetchTrustForIssuance
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GetAllAnyCredentialsByCredentialId
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialIssuerDisplayRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.RawCredentialDataRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.CredentialIssuerDisplayRepo
import ch.admin.foitt.wallet.platform.ssi.domain.repository.CredentialRepo
import ch.admin.foitt.wallet.platform.ssi.domain.repository.RawCredentialDataRepository
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ActorComplianceState
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.NonComplianceData
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ProcessIdentityTrustStatementError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ProcessIdentityTrustStatement
import ch.admin.foitt.wallet.platform.utils.JsonParsingError
import ch.admin.foitt.wallet.platform.utils.SafeJson
import ch.admin.foitt.wallet.platform.utils.decompress
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import javax.inject.Inject

internal class FetchAndCacheIssuerDisplayDataImpl @Inject constructor(
    private val getAllAnyCredentialsByCredentialId: GetAllAnyCredentialsByCredentialId,
    private val rawCredentialDataRepository: RawCredentialDataRepository,
    private val safeJson: SafeJson,
    private val processIdentityTrustStatement: ProcessIdentityTrustStatement,
    private val credentialRepository: CredentialRepo,
    private val fetchTrustForIssuance: FetchTrustForIssuance,
    private val credentialIssuerDisplayRepo: CredentialIssuerDisplayRepo,
    private val cacheIssuerDisplayData: CacheIssuerDisplayData,
) : FetchAndCacheIssuerDisplayData {
    override suspend operator fun invoke(
        credentialId: Long,
    ): Result<Unit, FetchAndCacheIssuerDisplayDataError> = coroutineBinding {
        val anyCredential = getAnyCredential(credentialId).bind()
        val issuerCredentialInfo = getIssuerCredentialInfo(credentialId).bind()

        // check status list again to make sure the identity trust is still valid
        val identityV2TrustStatement = processIdentityTrustStatement(
            identityTrustStatementJwt = issuerCredentialInfo.identityTrustStatement,
            actorDid = anyCredential.issuer,
        ).mapError(ProcessIdentityTrustStatementError::toFetchAndCacheIssuerDisplayDataError)
            .bind()

        val credentialConfiguration = getCredentialConfiguration(credentialId, issuerCredentialInfo).bind()
        val trustCheckResult = fetchTrustForIssuance(
            identityTrustStatement = identityV2TrustStatement,
            protectedIssuanceAuthorizationTrustStatement = credentialConfiguration?.protectedIssuanceAuthorizationTrustStatement,
            issuerDid = anyCredential.issuer,
            vcSchemaId = anyCredential.vcSchemaId,
        ).mapError(FetchTrustForIssuanceError::toFetchAndCacheIssuerDisplayDataError)
            .bind()

        val savedIssuerDisplays = credentialIssuerDisplayRepo.getIssuerDisplays(credentialId)
            .mapError(CredentialIssuerDisplayRepositoryError::toFetchAndCacheIssuerDisplayDataError)
            .bind()

        val localizedIssuerDisplays: List<AnyIssuerDisplay> = savedIssuerDisplays.map { display ->
            AnyIssuerDisplay(
                locale = display.locale,
                name = display.name,
                logo = display.image,
                logoAltText = display.imageAltText,
            )
        }

        cacheIssuerDisplayData(
            trustCheckResult = trustCheckResult,
            issuerDisplays = localizedIssuerDisplays,
            nonComplianceData = trustCheckResult?.nonComplianceData ?: UNKNOWN_NON_COMPLIANCE_DATA,
        )
    }

    private suspend fun getAnyCredential(
        credentialId: Long
    ): Result<AnyCredential, FetchAndCacheIssuerDisplayDataError> = coroutineBinding {
        val anyCredentials = getAllAnyCredentialsByCredentialId(credentialId)
            .mapError(GetAllAnyCredentialsByCredentialIdError::toFetchAndCacheIssuerDisplayDataError)
            .bind()

        runSuspendCatching {
            anyCredentials.first()
        }.mapError { ActorMetaDataError.Unexpected(it) }.bind()
    }

    private suspend fun getIssuerCredentialInfo(
        credentialId: Long,
    ): Result<IssuerCredentialInfo, FetchAndCacheIssuerDisplayDataError> = coroutineBinding {
        val rawIssuerCredentialInfoByteArray = rawCredentialDataRepository.getByCredentialId(credentialId)
            .mapError(RawCredentialDataRepositoryError::toFetchAndCacheIssuerDisplayDataError)
            .bind()
            .rawOIDMetadata
        val rawIssuerCredentialInfo = rawIssuerCredentialInfoByteArray?.decompress()?.decodeToString()
            ?: return@coroutineBinding Err(ActorMetaDataError.Unexpected(null)).bind<IssuerCredentialInfo>()

        safeJson.safeDecodeStringTo<IssuerCredentialInfo>(rawIssuerCredentialInfo)
            .mapError(JsonParsingError::toFetchAndCacheIssuerDisplayDataError)
            .bind()
    }

    private suspend fun getCredentialConfiguration(
        credentialId: Long,
        issuerCredentialInfo: IssuerCredentialInfo,
    ): Result<AnyCredentialConfiguration?, FetchAndCacheIssuerDisplayDataError> = coroutineBinding {
        val selectedConfigurationId = credentialRepository.getById(credentialId)
            .mapError(CredentialRepositoryError::toFetchAndCacheIssuerDisplayDataError)
            .bind()
            .selectedConfigurationId
        issuerCredentialInfo.credentialConfigurations.find { it.identifier == selectedConfigurationId }
    }

    private companion object {
        val UNKNOWN_NON_COMPLIANCE_DATA = NonComplianceData(
            state = ActorComplianceState.UNKNOWN,
            reasonDisplays = null,
        )
    }
}
