package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.GenerateDPoPKeyPairError
import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyDeferredCredential
import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyVerifiedBatchCredential
import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyVerifiedCredential
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOffer
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchCredentialByConfigError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchIssuerCredentialInfoError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.GetSignedMetadataDidError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.GetVerifiableCredentialParamsError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.Grant
import ch.admin.foitt.openid4vc.domain.usecase.FetchCredentialByConfig
import ch.admin.foitt.openid4vc.domain.usecase.FetchRawAndParsedIssuerCredentialInfo
import ch.admin.foitt.openid4vc.domain.usecase.GetSignedMetadataDid
import ch.admin.foitt.openid4vc.domain.usecase.GetVerifiableCredentialParams
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.FetchCredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.FetchCredentialResult
import ch.admin.foitt.wallet.platform.credential.domain.model.RefreshCredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.toFetchCredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.toRefreshCredentialError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.EvaluateBatchSize
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GetCredentialConfig
import ch.admin.foitt.wallet.platform.credential.domain.usecase.HandleBatchCredentialResult
import ch.admin.foitt.wallet.platform.credential.domain.usecase.HandleCredentialResult
import ch.admin.foitt.wallet.platform.credential.domain.usecase.RefreshCredential
import ch.admin.foitt.wallet.platform.credentialRefresh.domain.model.CredentialRefreshDataError
import ch.admin.foitt.wallet.platform.credentialRefresh.domain.model.GetBindingKeyPairError
import ch.admin.foitt.wallet.platform.credentialRefresh.domain.repository.CredentialRefreshDataRepository
import ch.admin.foitt.wallet.platform.credentialRefresh.domain.usecase.GetBindingKeyPair
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialAuthenticationWithDpopBinding
import ch.admin.foitt.wallet.platform.holderBinding.domain.model.GenerateProofKeyPairError
import ch.admin.foitt.wallet.platform.holderBinding.domain.usecase.GenerateDPoPKeyPair
import ch.admin.foitt.wallet.platform.holderBinding.domain.usecase.GenerateProofKeyPairs
import ch.admin.foitt.wallet.platform.payloadEncryption.domain.model.GetPayloadEncryptionTypeError
import ch.admin.foitt.wallet.platform.payloadEncryption.domain.usecase.GetPayloadEncryption
import ch.admin.foitt.wallet.platform.ssi.domain.model.BundleItemRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.DeleteBundleItemError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.BundleItemRepository
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.DeleteBundleItems
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ProcessIdentityTrustStatementError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ProcessIdentityTrustStatement
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import timber.log.Timber
import javax.inject.Inject

internal class RefreshCredentialImpl @Inject constructor(
    private val bundleItemRepository: BundleItemRepository,
    private val credentialRefreshDataRepository: CredentialRefreshDataRepository,
    private val getCredentialConfig: GetCredentialConfig,
    private val getPayloadEncryption: GetPayloadEncryption,
    private val fetchRawAndParsedIssuerCredentialInfo: FetchRawAndParsedIssuerCredentialInfo,
    private val getVerifiableCredentialParams: GetVerifiableCredentialParams,
    private val evaluateBatchSize: EvaluateBatchSize,
    private val getSignedMetadataDid: GetSignedMetadataDid,
    private val generateProofKeyPairs: GenerateProofKeyPairs,
    private val processIdentityTrustStatement: ProcessIdentityTrustStatement,
    private val fetchCredentialByConfig: FetchCredentialByConfig,
    private val getBindingKeyPair: GetBindingKeyPair,
    private val handleCredentialResult: HandleCredentialResult,
    private val handleBatchCredentialResult: HandleBatchCredentialResult,
    private val deleteBundleItems: DeleteBundleItems,
    private val generateDPoPKeyPair: GenerateDPoPKeyPair,
) : RefreshCredential {
    override suspend operator fun invoke(credentialId: Long): Result<Unit, RefreshCredentialError> = coroutineBinding {
        val credentialToRefresh = credentialRefreshDataRepository.getCredentialRefreshDataById(credentialId)
            .mapError(CredentialRefreshDataError::toRefreshCredentialError).bind()
        val credentialEntity = credentialToRefresh.credential

        credentialToRefresh.authentication?.refreshToken?.let { refreshToken ->
            val credentialOffer = CredentialOffer(
                credentialIssuer = credentialEntity.issuerUrl,
                credentialConfigurationIds = credentialEntity.selectedConfigurationId?.let { listOf(it) } ?: listOf(),
                grants = Grant(
                    refreshToken = refreshToken
                )
            )

            refreshAndSaveCredential(
                credentialId = credentialId,
                authentication = credentialToRefresh.authentication,
                credentialOffer = credentialOffer,
            ).mapError(FetchCredentialError::toRefreshCredentialError).bind()
        } ?: let {
            val exception = IllegalStateException("RefreshCredential: refresh data is missing")
            Timber.e(t = exception)
            Err(CredentialError.Unexpected(exception)).bind()
        }
    }

    private suspend fun refreshAndSaveCredential(
        credentialId: Long,
        authentication: CredentialAuthenticationWithDpopBinding,
        credentialOffer: CredentialOffer,
    ): Result<FetchCredentialResult, FetchCredentialError> = coroutineBinding {
        val rawAndParsedCredentialInfo = fetchRawAndParsedIssuerCredentialInfo(
            issuerEndpoint = credentialOffer.credentialIssuer,
            forceRefresh = true,
        ).mapError(FetchIssuerCredentialInfoError::toFetchCredentialError).bind()

        val issuerInfo = rawAndParsedCredentialInfo.issuerCredentialInfo
        val config = getCredentialConfig(
            credentials = credentialOffer.credentialConfigurationIds,
            credentialConfigurations = issuerInfo.credentialConfigurations
        ).bind()

        val verifiableCredentialParams = getVerifiableCredentialParams(
            credentialConfiguration = config,
            credentialOffer = credentialOffer,
            issuerCredentialInfo = issuerInfo
        ).mapError(GetVerifiableCredentialParamsError::toFetchCredentialError).bind()

        val batchSize = if (verifiableCredentialParams.isBatch) {
            evaluateBatchSize(issuerInfo).bind()
        } else {
            1
        }

        val actorDid = getSignedMetadataDid(rawAndParsedCredentialInfo.rawIssuerCredentialInfo)
            .mapError(GetSignedMetadataDidError::toFetchCredentialError)
            .bind()

        val proofKeyPairs = verifiableCredentialParams.proofTypeConfig?.let { proofTypeConfig ->
            generateProofKeyPairs(
                amount = batchSize,
                proofTypeConfig = proofTypeConfig,
                actorDid = actorDid,
            ).mapError(GenerateProofKeyPairError::toFetchCredentialError).bind()
        }

        val payloadEncryption = getPayloadEncryption(
            requestEncryption = rawAndParsedCredentialInfo.issuerCredentialInfo.credentialRequestEncryption,
            responseEncryption = rawAndParsedCredentialInfo.issuerCredentialInfo.credentialResponseEncryption,
        ).mapError(GetPayloadEncryptionTypeError::toFetchCredentialError).bind()

        val dpopKeyPair = getBindingKeyPair(authentication)
            .mapError(GetBindingKeyPairError::toFetchCredentialError).bind()
            ?: generateDPoPKeyPair(verifiableCredentialParams, actorDid)
                .mapError(GenerateDPoPKeyPairError::toFetchCredentialError).bind()

        val identityTrustStatement = processIdentityTrustStatement(issuerInfo.identityTrustStatement, actorDid)
            .mapError(ProcessIdentityTrustStatementError::toFetchCredentialError)
            .bind()

        val anyCredentialResult = fetchCredentialByConfig(
            verifiableCredentialParams = verifiableCredentialParams,
            bindingKeyPairs = proofKeyPairs,
            payloadEncryption = payloadEncryption,
            dpopKeyPair = dpopKeyPair,
        ).mapError(FetchCredentialByConfigError::toFetchCredentialError).bind()

        val oldBundleItems = bundleItemRepository.getAllByCredentialId(credentialId)
            .mapError(BundleItemRepositoryError::toFetchCredentialError).bind()

        val result = when (anyCredentialResult) {
            is AnyVerifiedBatchCredential -> handleBatchCredentialResult(
                credentialId = credentialId,
                issuerUrl = credentialOffer.credentialIssuer,
                batchSize = batchSize,
                anyVerifiedBatchCredential = anyCredentialResult,
                identityTrustStatement = identityTrustStatement,
                rawAndParsedCredentialInfo = rawAndParsedCredentialInfo,
                credentialConfig = config,
            ).bind()

            is AnyDeferredCredential -> {
                val exception = IllegalStateException("RefreshCredential: unexpectedly received a deferred credential")
                Timber.e(t = exception)
                Err(CredentialError.Unexpected(exception)).bind()
            }

            is AnyVerifiedCredential -> handleCredentialResult(
                credentialId = credentialId,
                issuerUrl = credentialOffer.credentialIssuer,
                anyVerifiedCredential = anyCredentialResult,
                identityTrustStatement = identityTrustStatement,
                rawAndParsedCredentialInfo = rawAndParsedCredentialInfo,
                credentialConfig = config,
            ).bind()
        }

        deleteBundleItems(oldBundleItems.map { it.id })
            .mapError(DeleteBundleItemError::toFetchCredentialError).bind()

        Timber.d(message = "Credential refreshed ${credentialOffer.credentialIssuer}")
        return@coroutineBinding result
    }
}
