package ch.admin.foitt.openid4vc.domain.usecase.implementation

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import ch.admin.foitt.openid4vc.di.DefaultDispatcher
import ch.admin.foitt.openid4vc.domain.model.KeyPairError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.JWSKeyPair
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.toAlgorithmParameterSpec
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.toDigest
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.toKeyAlgorithm
import ch.admin.foitt.openid4vc.domain.usecase.CreateJWSKeyPair
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.getOr
import com.github.michaelbull.result.getOrThrow
import com.github.michaelbull.result.mapError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.util.UUID
import javax.inject.Inject

internal class CreateJWSKeyPairImpl @Inject constructor(
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) : CreateJWSKeyPair {
    override suspend operator fun invoke(
        signingAlgorithm: SigningAlgorithm,
        provider: String,
    ) = withContext(defaultDispatcher) {
        runSuspendCatching {
            val keyStore = KeyStore.getInstance(provider)
            keyStore.load(null)
            val keyId = generateKeyId(keyStore).getOrThrow()
            val keyPair = createKeyPair(keyId, signingAlgorithm, provider)
            JWSKeyPair(
                algorithm = signingAlgorithm,
                keyPair = keyPair,
                keyId = keyId
            )
        }.mapError { throwable ->
            KeyPairError.Unexpected(throwable)
        }
    }

    private fun createKeyPair(keyId: String, signingAlgorithm: SigningAlgorithm, provider: String): KeyPair {
        val generator =
            KeyPairGenerator.getInstance(signingAlgorithm.toKeyAlgorithm(), provider)
        val spec = KeyGenParameterSpec.Builder(keyId, KeyProperties.PURPOSE_SIGN)
            .setAlgorithmParameterSpec(signingAlgorithm.toAlgorithmParameterSpec())
            .setDigests(signingAlgorithm.toDigest())
            .build()
        generator.initialize(spec)
        return generator.generateKeyPair()
    }

    private fun generateKeyId(keyStore: KeyStore): Result<String, Throwable> {
        // A collision is nearly impossible, but if it happens, the overridden keypair and linked credential is lost.
        // So we check if some key already exists here
        repeat(keyIdRetries) {
            val keyId = UUID.randomUUID().toString()
            val isNewEntry = runSuspendCatching<Boolean> {
                keyStore.getEntry(keyId, null) == null
            }.getOr(true)
            if (isNewEntry) return Ok(keyId)
            Timber.w(collisionMessage)
        }
        return Err(Exception("Fatal $collisionMessage"))
    }

    companion object {
        private const val keyIdRetries: Int = 5
        private val collisionMessage by lazy { "Collision while creating a key Id" }
    }
}
