package ch.admin.foitt.openid4vc.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CreateDidJwkError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOfferError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.toCurve
import ch.admin.foitt.openid4vc.domain.usecase.CreateDidJwk
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.map
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.toErrorIfNull
import com.nimbusds.jose.jwk.ECKey
import com.nimbusds.jose.util.Base64
import java.security.KeyPair
import java.security.interfaces.ECPublicKey
import javax.inject.Inject

internal class CreateDidJwkImpl @Inject constructor() : CreateDidJwk {
    override suspend fun invoke(
        keyPair: KeyPair,
        algorithm: SigningAlgorithm,
        asDid: Boolean
    ): Result<String, CreateDidJwkError> =
        keyPair.toPublicECKey(algorithm).map { publicKey ->
            if (asDid) {
                "did:jwk:${Base64.encode(publicKey.toJSONString())}"
            } else {
                publicKey.toJSONString()
            }
        }

    private fun KeyPair.toPublicECKey(algorithm: SigningAlgorithm): Result<ECKey, CreateDidJwkError> = runSuspendCatching {
        when (val pub = public) {
            is ECPublicKey -> pub.toPublicECKey(algorithm)
            else -> null
        }
    }.mapError { throwable ->
        CredentialOfferError.Unexpected(throwable)
    }.toErrorIfNull {
        CredentialOfferError.UnsupportedCryptographicSuite
    }

    private fun ECPublicKey.toPublicECKey(
        signingAlgorithm: SigningAlgorithm
    ): ECKey = ECKey.Builder(signingAlgorithm.toCurve(), this).build()
}
