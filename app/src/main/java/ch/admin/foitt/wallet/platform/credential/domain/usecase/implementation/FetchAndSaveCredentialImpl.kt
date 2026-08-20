package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.GenerateDPoPKeyPairError
import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyCredentialResult
import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyDeferredCredential
import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyVerifiedBatchCredential
import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyVerifiedCredential
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOffer
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchCredentialByConfigError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchIssuerCredentialInfoError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.GetSignedMetadataDidError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.GetVerifiableCredentialParamsError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.AnyCredentialConfiguration
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.RawAndParsedIssuerCredentialInfo
import ch.admin.foitt.openid4vc.domain.usecase.FetchCredentialByConfig
import ch.admin.foitt.openid4vc.domain.usecase.FetchRawAndParsedIssuerCredentialInfo
import ch.admin.foitt.openid4vc.domain.usecase.GetSignedMetadataDid
import ch.admin.foitt.openid4vc.domain.usecase.GetVerifiableCredentialParams
import ch.admin.foitt.wallet.platform.actorEnvironment.domain.model.ActorEnvironment
import ch.admin.foitt.wallet.platform.actorEnvironment.domain.usecase.GetActorEnvironment
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.FetchCredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.FetchCredentialResult
import ch.admin.foitt.wallet.platform.credential.domain.model.toFetchCredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.toUnverifiedIssuerError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.EvaluateBatchSize
import ch.admin.foitt.wallet.platform.credential.domain.usecase.FetchAndSaveCredential
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GetCredentialConfig
import ch.admin.foitt.wallet.platform.credential.domain.usecase.HandleBatchCredentialResult
import ch.admin.foitt.wallet.platform.credential.domain.usecase.HandleCredentialResult
import ch.admin.foitt.wallet.platform.credential.domain.usecase.HandleDeferredCredentialResult
import ch.admin.foitt.wallet.platform.credential.domain.usecase.ValidateIssuerCredentialInfo
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.holderBinding.domain.model.GenerateProofKeyPairError
import ch.admin.foitt.wallet.platform.holderBinding.domain.usecase.GenerateDPoPKeyPair
import ch.admin.foitt.wallet.platform.holderBinding.domain.usecase.GenerateProofKeyPairs
import ch.admin.foitt.wallet.platform.payloadEncryption.domain.model.GetPayloadEncryptionTypeError
import ch.admin.foitt.wallet.platform.payloadEncryption.domain.usecase.GetPayloadEncryption
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.IdentityV2TrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ProcessIdentityTrustStatementError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ProcessIdentityTrustStatement
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.runCatching
import javax.inject.Inject

class FetchAndSaveCredentialImpl @Inject constructor(
    private val fetchRawAndParsedIssuerCredentialInfo: FetchRawAndParsedIssuerCredentialInfo,
    private val getSignedMetadataDid: GetSignedMetadataDid,
    private val processIdentityTrustStatement: ProcessIdentityTrustStatement,
    private val validateIssuerCredentialInfo: ValidateIssuerCredentialInfo,
    private val getPayloadEncryption: GetPayloadEncryption,
    private val getVerifiableCredentialParams: GetVerifiableCredentialParams,
    private val getCredentialConfig: GetCredentialConfig,
    private val evaluateBatchSize: EvaluateBatchSize,
    private val generateProofKeyPairs: GenerateProofKeyPairs,
    private val fetchCredentialByConfig: FetchCredentialByConfig,
    private val handleCredentialResult: HandleCredentialResult,
    private val handleBatchCredentialResult: HandleBatchCredentialResult,
    private val handleDeferredCredentialResult: HandleDeferredCredentialResult,
    private val environmentSetupRepository: EnvironmentSetupRepository,
    private val generateDPoPKeyPair: GenerateDPoPKeyPair,
    private val getActorEnvironment: GetActorEnvironment,
) : FetchAndSaveCredential {
    override suspend fun invoke(
        credentialOffer: CredentialOffer,
    ): Result<FetchCredentialResult, FetchCredentialError> = coroutineBinding {
        val rawAndParsedCredentialInfo = fetchRawAndParsedIssuerCredentialInfo(
            issuerEndpoint = credentialOffer.credentialIssuer,
            forceRefresh = true,
        ).mapError(FetchIssuerCredentialInfoError::toFetchCredentialError)
            .bind()

        val issuerInfo = rawAndParsedCredentialInfo.issuerCredentialInfo
        val actorDid = getSignedMetadataDid(rawAndParsedCredentialInfo.rawIssuerCredentialInfo)
            .mapError(GetSignedMetadataDidError::toFetchCredentialError)
            .bind()

        val identityTrustStatement = processIdentityTrustStatement(
            identityTrustStatementJwt = issuerInfo.identityTrustStatement,
            actorDid = actorDid,
        ).mapError(ProcessIdentityTrustStatementError::toFetchCredentialError)
            .bind()

        val isConfigValid = validateIssuerCredentialInfo(issuerInfo)
        if (!isConfigValid) {
            return@coroutineBinding Err(CredentialError.InvalidIssuerCredentialInfo).bind<FetchCredentialResult>()
        }

        val payloadEncryption = getPayloadEncryption(
            requestEncryption = issuerInfo.credentialRequestEncryption,
            responseEncryption = issuerInfo.credentialResponseEncryption,
        ).mapError(GetPayloadEncryptionTypeError::toFetchCredentialError)
            .bind()

        val config = getCredentialConfig(
            credentials = credentialOffer.credentialConfigurationIds,
            credentialConfigurations = issuerInfo.credentialConfigurations
        ).bind()

        val verifiableCredentialParams = getVerifiableCredentialParams(
            issuerCredentialInfo = issuerInfo,
            credentialConfiguration = config,
            credentialOffer = credentialOffer,
        ).mapError(GetVerifiableCredentialParamsError::toFetchCredentialError).bind()

        val batchSize = if (environmentSetupRepository.batchIssuanceEnabled.not() && verifiableCredentialParams.isBatch) {
            // This is a workaround for the BETA-ID, which is already issued as a batch
            1
        } else if (verifiableCredentialParams.isBatch) {
            evaluateBatchSize(issuerInfo).bind()
        } else {
            1
        }
        val proofKeyPairs = verifiableCredentialParams.proofTypeConfig?.let { proofTypeConfig ->
            generateProofKeyPairs(batchSize, proofTypeConfig, actorDid)
                .mapError(GenerateProofKeyPairError::toFetchCredentialError)
                .bind()
        }

        val dpopKeyPair = generateDPoPKeyPair(verifiableCredentialParams, actorDid)
            .mapError(GenerateDPoPKeyPairError::toFetchCredentialError)
            .bind()

        val anyCredentialResult = fetchCredentialByConfig(
            verifiableCredentialParams = verifiableCredentialParams,
            bindingKeyPairs = proofKeyPairs,
            payloadEncryption = payloadEncryption,
            dpopKeyPair = dpopKeyPair,
        ).mapError(FetchCredentialByConfigError::toFetchCredentialError).bind()

        validateIssuerDid(anyCredentialResult).bind()

        handleResult(
            credentialOffer = credentialOffer,
            anyCredentialResult = anyCredentialResult,
            actorDid = actorDid,
            identityTrustStatement = identityTrustStatement,
            batchSize = batchSize,
            rawAndParsedCredentialInfo = rawAndParsedCredentialInfo,
            config = config,
        ).bind()
    }

    /**
     * Reject issuers whose DID is not on the BaseRegistry.
     * For deferred credentials, the DID is not yet available and will be checked in [SaveCredentialFromDeferredImpl].
     */
    private suspend fun validateIssuerDid(anyCredentialResult: AnyCredentialResult): Result<Unit, FetchCredentialError> {
        val issuerDid = when (anyCredentialResult) {
            is AnyVerifiedCredential -> anyCredentialResult.vcSdJwtCredential.issuer
            is AnyVerifiedBatchCredential -> anyCredentialResult.vcSdJwtCredentials.first().issuer
            is AnyDeferredCredential -> null
        }
        if (issuerDid != null && getActorEnvironment(issuerDid) == ActorEnvironment.EXTERNAL) {
            return Err(CredentialError.UnknownRegistry)
        }
        return Ok(Unit)
    }

    private suspend fun handleResult(
        credentialOffer: CredentialOffer,
        anyCredentialResult: AnyCredentialResult,
        actorDid: String,
        identityTrustStatement: IdentityV2TrustStatement?,
        batchSize: Int,
        rawAndParsedCredentialInfo: RawAndParsedIssuerCredentialInfo,
        config: AnyCredentialConfiguration,
    ): Result<FetchCredentialResult, FetchCredentialError> = coroutineBinding {
        when (anyCredentialResult) {
            is AnyVerifiedCredential -> {
                compareCredentialAndMetadataDids(
                    credentialDid = anyCredentialResult.vcSdJwtCredential.issuer,
                    metadataDid = actorDid,
                ).bind()

                handleCredentialResult(
                    issuerUrl = credentialOffer.credentialIssuer,
                    anyVerifiedCredential = anyCredentialResult,
                    identityTrustStatement = identityTrustStatement,
                    rawAndParsedCredentialInfo = rawAndParsedCredentialInfo,
                    credentialConfig = config,
                ).bind()
            }

            is AnyVerifiedBatchCredential -> {
                compareCredentialAndMetadataDids(
                    credentialDid = anyCredentialResult.vcSdJwtCredentials.first().issuer,
                    metadataDid = actorDid,
                ).bind()

                handleBatchCredentialResult(
                    issuerUrl = credentialOffer.credentialIssuer,
                    batchSize = batchSize,
                    anyVerifiedBatchCredential = anyCredentialResult,
                    identityTrustStatement = identityTrustStatement,
                    rawAndParsedCredentialInfo = rawAndParsedCredentialInfo,
                    credentialConfig = config,
                ).bind()
            }

            is AnyDeferredCredential -> handleDeferredCredentialResult(
                issuerUrl = credentialOffer.credentialIssuer,
                deferredCredential = anyCredentialResult,
                rawAndParsedCredentialInfo = rawAndParsedCredentialInfo,
                credentialConfig = config,
            ).bind()
        }
    }

    private fun compareCredentialAndMetadataDids(
        credentialDid: String,
        metadataDid: String
    ): Result<Unit, FetchCredentialError> = runCatching {
        check(credentialDid == metadataDid)
    }.mapError(Throwable::toUnverifiedIssuerError)
}
