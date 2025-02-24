package ch.admin.foitt.wallet.platform.ssi.data.repository

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.Display
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.SigningAlgorithm
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialClaimDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialClaimDisplayDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialDisplayDao
import ch.admin.foitt.wallet.platform.database.data.dao.CredentialIssuerDisplayDao
import ch.admin.foitt.wallet.platform.database.data.dao.DaoProvider
import ch.admin.foitt.wallet.platform.database.domain.model.Credential
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaim
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaimDisplay
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialDisplay
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialIssuerDisplay
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialWithDetails
import ch.admin.foitt.wallet.platform.database.domain.model.DisplayLanguage
import ch.admin.foitt.wallet.platform.di.IoDispatcher
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialOfferRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.LocalizedCredentialOffer
import ch.admin.foitt.wallet.platform.ssi.domain.model.toCredentialOfferRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.CredentialOfferRepository
import ch.admin.foitt.wallet.platform.utils.catchAndMap
import ch.admin.foitt.wallet.platform.utils.suspendUntilNonNull
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CredentialOfferRepositoryImpl @Inject constructor(
    daoProvider: DaoProvider,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : CredentialOfferRepository {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getCredentialOfferByIdFlow(id: Long): Flow<Result<CredentialWithDetails?, CredentialOfferRepositoryError>> =
        credentialWithDetailsDaoFlow.flatMapLatest { dao ->
            dao?.getCredentialWithDetailsFlowById(id)
                ?.catchAndMap(Throwable::toCredentialOfferRepositoryError) ?: emptyFlow()
        }

    override suspend fun saveCredentialOffer(
        localizedCredentialOffer: LocalizedCredentialOffer,
    ): Result<Long, CredentialOfferRepositoryError> = withContext(ioDispatcher) {
        val credential = createCredential(
            privateKeyIdentifier = localizedCredentialOffer.keyBindingIdentifier,
            signingAlgorithm = localizedCredentialOffer.keyBindingAlgorithm,
            payload = localizedCredentialOffer.payload,
            format = localizedCredentialOffer.format,
            issuer = localizedCredentialOffer.issuer
        )
        val credentialIssuerDisplays = createCredentialIssuerDisplays(localizedCredentialOffer.issuerDisplays)
        val credDisplays = createCredentialDisplays(localizedCredentialOffer.credentialDisplays)
        val credentialClaims = createCredentialClaims(localizedCredentialOffer.claims)

        saveCredentialOffer(
            credential = credential,
            issuerDisplays = credentialIssuerDisplays,
            credentialDisplays = credDisplays,
            claims = credentialClaims,
        )
    }

    private fun createCredential(
        privateKeyIdentifier: String?,
        signingAlgorithm: SigningAlgorithm?,
        payload: String,
        format: CredentialFormat,
        issuer: String?
    ) = Credential(
        keyBindingIdentifier = privateKeyIdentifier,
        keyBindingAlgorithm = signingAlgorithm?.stdName,
        payload = payload,
        format = format,
        issuer = issuer
    )

    private fun createCredentialIssuerDisplays(
        issuerDisplays: List<Display>,
    ) = issuerDisplays.map { display ->
        CredentialIssuerDisplay(
            credentialId = -1,
            name = display.name,
            image = display.logo?.uri,
            imageAltText = display.logo?.altText,
            locale = display.locale ?: DisplayLanguage.FALLBACK,
        )
    }

    private fun createCredentialDisplays(
        credentialDisplays: List<Display>,
    ) = credentialDisplays.map { display ->
        CredentialDisplay(
            // at this point we do not have the credential id yet, since it will only be known after the credential was inserted in the db
            credentialId = -1,
            locale = display.locale ?: DisplayLanguage.FALLBACK,
            name = display.name,
            description = display.description,
            logoUri = display.logo?.uri,
            logoAltText = display.logo?.altText,
            backgroundColor = display.backgroundColor,
        )
    }

    private fun createCredentialClaims(
        claims: Map<CredentialClaim, List<Display>>
    ): Map<CredentialClaim, List<CredentialClaimDisplay>> = claims.map { entry ->
        entry.key to entry.value.map { display ->
            CredentialClaimDisplay(
                // at this point we do not have the claim id yet, since it will only be known after the claim was inserted in the db
                claimId = -1,
                name = display.name,
                locale = display.locale ?: DisplayLanguage.FALLBACK,
            )
        }
    }.toMap()

    private suspend fun saveCredentialOffer(
        credential: Credential,
        issuerDisplays: List<CredentialIssuerDisplay>,
        credentialDisplays: List<CredentialDisplay>,
        claims: Map<CredentialClaim, List<CredentialClaimDisplay>>
    ): Result<Long, CredentialOfferRepositoryError> = runSuspendCatching {
        val credentialId = credentialDao().insert(credential)
        credentialIssuerDisplayDao().insertAll(issuerDisplays.map { it.copy(credentialId = credentialId) })
        credentialDisplayDao().insertAll(credentialDisplays.map { it.copy(credentialId = credentialId) })
        claims.forEach { claimsMap ->
            val claim = claimsMap.key.copy(credentialId = credentialId)
            val claimId = credentialClaimDao().insert(claim)
            val displays = claimsMap.value.map { it.copy(claimId = claimId) }
            credentialClaimDisplayDao().insertAll(displays)
        }
        credentialId
    }.mapError(Throwable::toCredentialOfferRepositoryError)

    private suspend fun credentialDao(): CredentialDao = suspendUntilNonNull { credentialDaoFlow.value }
    private val credentialDaoFlow = daoProvider.credentialDaoFlow
    private suspend fun credentialDisplayDao(): CredentialDisplayDao = suspendUntilNonNull {
        credentialDisplayDaoFlow.value
    }
    private val credentialDisplayDaoFlow = daoProvider.credentialDisplayDaoFlow
    private suspend fun credentialIssuerDisplayDao(): CredentialIssuerDisplayDao = suspendUntilNonNull {
        credentialIssuerDisplayDaoFlow.value
    }
    private val credentialIssuerDisplayDaoFlow = daoProvider.credentialIssuerDisplayDaoFlow
    private suspend fun credentialClaimDao(): CredentialClaimDao = suspendUntilNonNull { credentialClaimDaoFlow.value }
    private val credentialClaimDaoFlow = daoProvider.credentialClaimDaoFlow
    private suspend fun credentialClaimDisplayDao(): CredentialClaimDisplayDao = suspendUntilNonNull {
        credentialClaimDisplayDaoFlow.value
    }
    private val credentialClaimDisplayDaoFlow = daoProvider.credentialClaimDisplayDaoFlow

    private val credentialWithDetailsDaoFlow = daoProvider.credentialWithDetailsDaoFlow
}
