package ch.admin.foitt.wallet.platform.batch.domain.usecase.implementation

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
import ch.admin.foitt.openid4vc.domain.model.keyBinding.BindingKeyPair
import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBindingType
import ch.admin.foitt.openid4vc.domain.model.threshold
import ch.admin.foitt.openid4vc.domain.usecase.FetchCredentialByConfig
import ch.admin.foitt.openid4vc.domain.usecase.FetchRawAndParsedIssuerCredentialInfo
import ch.admin.foitt.openid4vc.domain.usecase.GetSignedMetadataDid
import ch.admin.foitt.openid4vc.domain.usecase.GetVerifiableCredentialParams
import ch.admin.foitt.wallet.platform.batch.domain.error.DeleteBundleItemsByAmountError
import ch.admin.foitt.wallet.platform.batch.domain.error.RefreshBatchCredentialsError
import ch.admin.foitt.wallet.platform.batch.domain.error.toRefreshBatchCredentialsError
import ch.admin.foitt.wallet.platform.batch.domain.model.BatchRefreshParams
import ch.admin.foitt.wallet.platform.batch.domain.usecase.DeleteBundleItemsByAmount
import ch.admin.foitt.wallet.platform.batch.domain.usecase.RefreshBatchCredentials
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.FetchCredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.FetchCredentialResult
import ch.admin.foitt.wallet.platform.credential.domain.model.toFetchCredentialError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.EvaluateBatchSize
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GetCredentialConfig
import ch.admin.foitt.wallet.platform.credential.domain.usecase.HandleBatchCredentialResult
import ch.admin.foitt.wallet.platform.credentialRefresh.domain.model.CredentialRefreshDataError
import ch.admin.foitt.wallet.platform.credentialRefresh.domain.model.GetBindingKeyPairError
import ch.admin.foitt.wallet.platform.credentialRefresh.domain.repository.CredentialRefreshDataRepository
import ch.admin.foitt.wallet.platform.credentialRefresh.domain.usecase.GetBindingKeyPair
import ch.admin.foitt.wallet.platform.holderBinding.domain.model.GenerateProofKeyPairError
import ch.admin.foitt.wallet.platform.holderBinding.domain.usecase.GenerateDPoPKeyPair
import ch.admin.foitt.wallet.platform.holderBinding.domain.usecase.GenerateProofKeyPairs
import ch.admin.foitt.wallet.platform.payloadEncryption.domain.model.GetPayloadEncryptionTypeError
import ch.admin.foitt.wallet.platform.payloadEncryption.domain.usecase.GetPayloadEncryption
import ch.admin.foitt.wallet.platform.ssi.domain.model.BundleItemRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.DeleteBundleItemError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.BundleItemRepository
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.DeleteBundleItems
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.DeleteKeyStoreEntry
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ProcessIdentityTrustStatementError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ProcessIdentityTrustStatement
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.get
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.onErr
import kotlinx.coroutines.sync.Mutex
import timber.log.Timber
import javax.inject.Inject

class RefreshBatchCredentialsImpl @Inject constructor(
    private val bundleItemRepository: BundleItemRepository,
    private val credentialRefreshDataRepository: CredentialRefreshDataRepository,
    private val getCredentialConfig: GetCredentialConfig,
    private val getPayloadEncryption: GetPayloadEncryption,
    private val fetchRawAndParsedIssuerCredentialInfo: FetchRawAndParsedIssuerCredentialInfo,
    private val getVerifiableCredentialParams: GetVerifiableCredentialParams,
    private val evaluateBatchSize: EvaluateBatchSize,
    private val deleteBundleItemsByAmount: DeleteBundleItemsByAmount,
    private val generateProofKeyPairs: GenerateProofKeyPairs,
    private val getSignedMetadataDid: GetSignedMetadataDid,
    private val processIdentityTrustStatement: ProcessIdentityTrustStatement,
    private val fetchCredentialByConfig: FetchCredentialByConfig,
    private val getBindingKeyPair: GetBindingKeyPair,
    private val handleBatchCredentialResult: HandleBatchCredentialResult,
    private val deleteBundleItems: DeleteBundleItems,
    private val generateDPoPKeyPair: GenerateDPoPKeyPair,
    private val deleteKeyStoreEntry: DeleteKeyStoreEntry,
) : RefreshBatchCredentials {
    private val refreshMutex = Mutex()

    /**
     * Skips silently when a refresh is already running, so overlapping triggers (after login and on home resume)
     * cannot issue a second batch for the same credentials.
     */
    override suspend fun invoke(): Result<Unit, RefreshBatchCredentialsError> {
        if (!refreshMutex.tryLock()) {
            Timber.d(message = "Batch credential refresh already in flight, skipping")
            return Ok(Unit)
        }
        return try {
            refreshBatchCredentials()
        } finally {
            refreshMutex.unlock()
        }
    }

    private suspend fun refreshBatchCredentials(): Result<Unit, RefreshBatchCredentialsError> = coroutineBinding {
        val batchRefreshDataList = credentialRefreshDataRepository.getAllBatchCredentialRefreshData()
            .mapError(CredentialRefreshDataError::toRefreshBatchCredentialsError)
            .bind()

        batchRefreshDataList.forEach { verifiableCredentialWithBatchDataAndAuthentication ->
            val batchData = verifiableCredentialWithBatchDataAndAuthentication.batchData
            val presentableBundleItemCount = bundleItemRepository.getNeverPresentedCount(batchData.credentialId)
                .onErr { error ->
                    Timber.w(
                        message = "Batch credential refresh: getting item count failed for ${batchData.credentialId}: $error"
                    )
                }
                .get()
            val refreshToken = verifiableCredentialWithBatchDataAndAuthentication.authentication.refreshToken

            Timber.d(message = "Batch credential refresh for ${batchData.credentialId}:\nitem count:$presentableBundleItemCount")
            if (refreshToken != null && presentableBundleItemCount != null &&
                presentableBundleItemCount <= batchData.batchSize.threshold
            ) {
                val credential = verifiableCredentialWithBatchDataAndAuthentication.credential
                Timber.d(message = "Batch credential refresh:\ntriggering refresh for credential ${credential.id}")
                refreshAndSaveCredential(
                    credentialOffer = CredentialOffer(
                        credentialIssuer = credential.issuerUrl,
                        credentialConfigurationIds = credential.selectedConfigurationId?.let { listOf(it) } ?: listOf(),
                        grants = Grant(
                            refreshToken = refreshToken
                        )
                    ),
                    batchRefreshParams = BatchRefreshParams(
                        credentialId = batchData.credentialId,
                        presentableCredentialCount = presentableBundleItemCount,
                        oldBatchSize = batchData.batchSize,
                        authentication = verifiableCredentialWithBatchDataAndAuthentication.authentication,
                    )
                ).mapError(FetchCredentialError::toRefreshBatchCredentialsError)
                    .bind()
            }
        }
    }

    private suspend fun refreshAndSaveCredential(
        credentialOffer: CredentialOffer,
        batchRefreshParams: BatchRefreshParams
    ): Result<FetchCredentialResult, FetchCredentialError> {
        val generatedKeyPairs = mutableListOf<BindingKeyPair>()
        return fetchAndSaveCredential(
            credentialOffer = credentialOffer,
            batchRefreshParams = batchRefreshParams,
            generatedKeyPairs = generatedKeyPairs,
        ).onErr {
            deleteGeneratedHardwareKeys(generatedKeyPairs)
        }
    }

    @Suppress("LongMethod")
    private suspend fun fetchAndSaveCredential(
        credentialOffer: CredentialOffer,
        batchRefreshParams: BatchRefreshParams,
        generatedKeyPairs: MutableList<BindingKeyPair>,
    ): Result<FetchCredentialResult, FetchCredentialError> = coroutineBinding {
        val rawAndParsedCredentialInfo = fetchRawAndParsedIssuerCredentialInfo(
            issuerEndpoint = credentialOffer.credentialIssuer,
            forceRefresh = true,
        ).mapError(FetchIssuerCredentialInfoError::toFetchCredentialError)
            .bind()

        val issuerInfo = rawAndParsedCredentialInfo.issuerCredentialInfo
        val config = getCredentialConfig(
            credentials = credentialOffer.credentialConfigurationIds,
            credentialConfigurations = issuerInfo.credentialConfigurations
        ).bind()

        val batchSize = evaluateBatchSize(issuerInfo).bind()
        val needToReducePresentableCredentialCount = batchSize < batchRefreshParams.presentableCredentialCount
        val onlyUpdateRefreshData =
            batchSize.threshold < batchRefreshParams.presentableCredentialCount || needToReducePresentableCredentialCount

        if (needToReducePresentableCredentialCount) {
            deleteBundleItemsByAmount(batchRefreshParams.credentialId, batchRefreshParams.oldBatchSize - batchSize)
                .mapError(DeleteBundleItemsByAmountError::toFetchCredentialError).bind()
        }

        if (onlyUpdateRefreshData) {
            credentialRefreshDataRepository.updateBatchSize(
                credentialId = batchRefreshParams.credentialId,
                batchSize = batchSize,
            ).mapError(CredentialRefreshDataError::toFetchCredentialError).bind()
            return@coroutineBinding FetchCredentialResult.Credential(batchRefreshParams.credentialId)
        }

        val verifiableCredentialParams = getVerifiableCredentialParams(
            credentialConfiguration = config,
            credentialOffer = credentialOffer,
            issuerCredentialInfo = issuerInfo
        ).mapError(GetVerifiableCredentialParamsError::toFetchCredentialError).bind()

        val actorDid = getSignedMetadataDid(rawAndParsedCredentialInfo.rawIssuerCredentialInfo)
            .mapError(GetSignedMetadataDidError::toFetchCredentialError)
            .bind()

        val proofKeyPairs = verifiableCredentialParams.proofTypeConfig?.let { proofTypeConfig ->
            generateProofKeyPairs(
                amount = batchSize,
                proofTypeConfig = proofTypeConfig,
                actorDid = actorDid,
            ).mapError(GenerateProofKeyPairError::toFetchCredentialError)
                .bind()
                .also { generatedKeyPairs += it }
        }

        val payloadEncryption = getPayloadEncryption(
            requestEncryption = issuerInfo.credentialRequestEncryption,
            responseEncryption = issuerInfo.credentialResponseEncryption,
        ).mapError(GetPayloadEncryptionTypeError::toFetchCredentialError).bind()

        val dpopKeyPair = getBindingKeyPair(batchRefreshParams.authentication)
            .mapError(GetBindingKeyPairError::toFetchCredentialError)
            .bind()
            ?: generateDPoPKeyPair(verifiableCredentialParams, actorDid)
                .mapError(GenerateDPoPKeyPairError::toFetchCredentialError)
                .bind()
                ?.also { generatedKeyPairs += it }

        val identityTrustStatement = processIdentityTrustStatement(issuerInfo.identityTrustStatement, actorDid)
            .mapError(ProcessIdentityTrustStatementError::toFetchCredentialError)
            .bind()

        val anyCredentialResult = fetchCredentialByConfig(
            verifiableCredentialParams = verifiableCredentialParams,
            bindingKeyPairs = proofKeyPairs,
            payloadEncryption = payloadEncryption,
            dpopKeyPair = dpopKeyPair,
        )
            .onErr {
                Timber.d(message = "Batch credential refresh failed: $it")
            }
            .mapError(FetchCredentialByConfigError::toFetchCredentialError).bind()

        val oldBundleItems = bundleItemRepository.getAllByCredentialId(batchRefreshParams.credentialId)
            .mapError(BundleItemRepositoryError::toFetchCredentialError)
            .bind()

        val result = when (anyCredentialResult) {
            is AnyVerifiedBatchCredential -> handleBatchCredentialResult(
                credentialId = batchRefreshParams.credentialId,
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

            is AnyVerifiedCredential -> {
                val exception = IllegalStateException("RefreshCredential: unexpectedly received a non-batch credential")
                Timber.e(t = exception)
                Err(CredentialError.Unexpected(exception)).bind()
            }
        }
        generatedKeyPairs.clear()

        deleteBundleItems(oldBundleItems.map { it.id })
            .mapError(DeleteBundleItemError::toFetchCredentialError).bind()

        Timber.d(message = "Batch credential refreshed ${credentialOffer.credentialIssuer}")
        return@coroutineBinding result
    }

    private suspend fun deleteGeneratedHardwareKeys(keyPairs: List<BindingKeyPair>) {
        keyPairs
            .filter { it.keyPair.bindingType == KeyBindingType.HARDWARE }
            .forEach { bindingKeyPair ->
                deleteKeyStoreEntry(bindingKeyPair.keyPair.keyId)
            }
    }
}
