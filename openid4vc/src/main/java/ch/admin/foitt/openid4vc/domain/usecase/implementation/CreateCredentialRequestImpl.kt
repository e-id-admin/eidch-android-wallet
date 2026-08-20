package ch.admin.foitt.openid4vc.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.CredentialType
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CreateCredentialRequestError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOfferError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialRequestCredentialResponseEncryption
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.DeferredCredentialRequest
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.VerifiableCredentialRequest
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialRequestEncryption
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialResponseEncryption
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.toCreateCredentialRequestError
import ch.admin.foitt.openid4vc.domain.model.jwe.CreateJWEError
import ch.admin.foitt.openid4vc.domain.model.jwk.Jwk
import ch.admin.foitt.openid4vc.domain.model.payloadEncryption.PayloadEncryption
import ch.admin.foitt.openid4vc.domain.model.payloadEncryption.PayloadEncryptionKeyPair
import ch.admin.foitt.openid4vc.domain.usecase.CreateCredentialRequest
import ch.admin.foitt.openid4vc.domain.usecase.jwe.CreateJWE
import ch.admin.foitt.openid4vc.utils.JsonParsingError
import ch.admin.foitt.openid4vc.utils.SafeJson
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import com.nimbusds.jose.jwk.Curve.P_256
import com.nimbusds.jose.jwk.ECKey
import timber.log.Timber
import java.security.interfaces.ECPublicKey
import javax.inject.Inject

internal class CreateCredentialRequestImpl @Inject constructor(
    private val safeJson: SafeJson,
    private val createJWE: CreateJWE,
) : CreateCredentialRequest {
    override suspend fun invoke(
        payloadEncryption: PayloadEncryption,
        credentialType: CredentialType,
    ): Result<String, CreateCredentialRequestError> = coroutineBinding {
        val credentialRequestCredentialResponseEncryption = createCredentialRequestCredentialResponseEncryption(
            payloadEncryptionKeyPair = payloadEncryption.responseEncryptionKeyPair,
            responseEncryption = payloadEncryption.responseEncryption,
        ).bind()

        createJWECredentialRequest(
            credentialType = credentialType,
            requestEncryption = payloadEncryption.requestEncryption,
            credentialResponseEncryption = credentialRequestCredentialResponseEncryption,
        ).bind()
    }

    /**
     * Creates the JWE used for request encryption
     * Optionally contains our public key as JWK that the issuer uses for response encryption
     */
    private suspend fun createJWECredentialRequest(
        credentialType: CredentialType,
        requestEncryption: CredentialRequestEncryption,
        credentialResponseEncryption: CredentialRequestCredentialResponseEncryption,
    ): Result<String, CreateCredentialRequestError> = coroutineBinding {
        val credentialRequest = when (credentialType) {
            is CredentialType.Verifiable -> VerifiableCredentialRequest(
                credentialConfigurationId = credentialType.verifiableCredentialParams.credentialConfiguration.identifier,
                proofs = credentialType.proofs,
                credentialResponseEncryption = credentialResponseEncryption,
            )

            is CredentialType.Deferred -> DeferredCredentialRequest(
                transactionId = credentialType.transactionId,
                credentialResponseEncryption = credentialResponseEncryption
            )
        }

        val credentialRequestJsonString = safeJson.safeEncodeObjectToString(credentialRequest)
            .mapError(JsonParsingError::toCreateCredentialRequestError)
            .bind()

        val algorithm = requestEncryption.jwks.keys.first().alg
            ?: return@coroutineBinding Err(
                CredentialOfferError.Unexpected(IllegalStateException("Public key algorithm is null"))
            ).bind<String>()

        val jweString = createJWE(
            algorithm = algorithm,
            encryptionMethod = requestEncryption.encValuesSupported.first(),
            compressionAlgorithm = requestEncryption.zipValuesSupported?.firstOrNull(),
            payload = credentialRequestJsonString,
            encryptionKey = requestEncryption.jwks.keys.first(), // use issuer public key to encrypt
        ).mapError(CreateJWEError::toCreateCredentialRequestError)
            .bind()

        jweString
    }

    /**
     * Create an object that contains our public key (plus some further info) that will be sent to the issuer when doing response encryption
     * The credential response we get will be encrypted with this public key, so we can decrypt with our private key
     */
    private suspend fun createCredentialRequestCredentialResponseEncryption(
        payloadEncryptionKeyPair: PayloadEncryptionKeyPair,
        responseEncryption: CredentialResponseEncryption,
    ): Result<CredentialRequestCredentialResponseEncryption, CreateCredentialRequestError> = coroutineBinding {
        val publicKey = runSuspendCatching {
            val pub = payloadEncryptionKeyPair.keyPair.keyPair.public
            ECKey.Builder(P_256, pub as ECPublicKey).build()
        }.mapError { throwable ->
            Timber.e(t = throwable, message = "Error when creating jwk for payload encryption")
            CredentialOfferError.Unexpected(throwable)
        }.bind()

        // create JWK with wallet public key
        val jwk = Jwk(
            kid = payloadEncryptionKeyPair.keyPair.keyId,
            kty = publicKey.keyType.toString(),
            use = "enc",
            crv = publicKey.curve.name,
            alg = responseEncryption.algValuesSupported.firstOrNull(),
            x = publicKey.x.toString(),
            y = publicKey.y.toString(),
        )

        CredentialRequestCredentialResponseEncryption(
            jwk = jwk,
            alg = payloadEncryptionKeyPair.alg,
            enc = payloadEncryptionKeyPair.enc,
            zip = payloadEncryptionKeyPair.zip,
        )
    }
}
