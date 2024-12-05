package ch.admin.foitt.openid4vc.domain.usecase.implementation.mock

import ch.admin.foitt.openid4vc.domain.model.VerifiableCredential
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOffer
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialRequestProofJwt
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialResponse
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.Grant
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.JWSKeyPair
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.PreAuthorizedContent
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.TokenResponse
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.IssuerConfiguration
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.IssuerCredentialInformation
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockIssuerCredentialConfiguration.vcSdJwtCredentialConfiguration
import io.mockk.mockk
import java.security.KeyPair

internal object MockCredentialOffer {
    const val CREDENTIAL_ISSUER = "credentialIssuer"
    const val CREDENTIAL_IDENTIFIER = "credentialIdentifier"
    private val CREDENTIALS = listOf(CREDENTIAL_IDENTIFIER)
    private const val PRE_AUTHORIZED_CODE = "preAuthorizedCode"
    val offerWithPreAuthorizedCode = CredentialOffer(
        credentialIssuer = CREDENTIAL_ISSUER,
        credentialConfigurationIds = CREDENTIALS,
        grants = Grant(PreAuthorizedContent(PRE_AUTHORIZED_CODE))
    )
    val offerWithoutPreAuthorizedCode = offerWithPreAuthorizedCode.copy(grants = Grant())
    val offerWithoutMatchingCredentialIdentifier =
        offerWithPreAuthorizedCode.copy(credentialConfigurationIds = listOf("otherCredentialIdentifier"))

    private const val ACCESS_TOKEN = "accessToken"
    const val C_NONCE = "cNonce"
    private const val C_NONCE_EXPIRES_IN = 1
    private const val EXPIRES_IN = 2
    private const val TOKEN_TYPE = "tokenType"
    val validTokenResponse = TokenResponse(
        accessToken = ACCESS_TOKEN,
        cNonce = C_NONCE,
        cNonceExpiresIn = C_NONCE_EXPIRES_IN,
        expiresIn = EXPIRES_IN,
        tokenType = TOKEN_TYPE
    )

    private const val TOKEN_ENDPOINT = "tokenEndpoint"
    val validIssuerConfig = IssuerConfiguration(
        issuer = CREDENTIAL_ISSUER,
        tokenEndpoint = TOKEN_ENDPOINT
    )

    private const val CREDENTIAL_ENDPOINT = "credentialEndpoint"
    val validIssuerCredentialInformation = IssuerCredentialInformation(
        credentialEndpoint = CREDENTIAL_ENDPOINT,
        credentialIssuer = CREDENTIAL_ISSUER,
        credentialConfigurations = listOf(vcSdJwtCredentialConfiguration),
        credentialResponseEncryption = null,
        display = listOf()
    )

    const val KEY_ID = "keyId"
    private const val PROOF_JWT = "proofJwt"
    val jwtProof = CredentialRequestProofJwt(PROOF_JWT)

    private val ALGORITHM = SigningAlgorithm.ES512
    private val mockKeyPair = mockk<KeyPair>()

    val validKeyPair = JWSKeyPair(
        algorithm = ALGORITHM,
        keyPair = mockKeyPair,
        keyId = KEY_ID,
    )

    private const val CREDENTIAL = "credential"
    private const val TRANSACTION_ID = "transaction_id"
    private const val NOTIFICATION_ID = "notification_id"
    val validCredentialResponse = CredentialResponse(
        credential = CREDENTIAL,
        format = CredentialFormat.VC_SD_JWT.format,
        transactionId = TRANSACTION_ID,
        cNonce = C_NONCE,
        cNonceExpiresIn = C_NONCE_EXPIRES_IN,
        notificationId = NOTIFICATION_ID,
    )

    val validVerifiableCredential = VerifiableCredential(
        format = CredentialFormat.VC_SD_JWT,
        credential = CREDENTIAL,
        signingKeyId = KEY_ID,
        signingAlgorithm = ALGORITHM,
    )
}
