package ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.implementation

import ch.admin.foitt.openid4vc.di.DefaultDispatcher
import ch.admin.foitt.openid4vc.domain.model.GetKeyPairError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.toJWSAlgorithm
import ch.admin.foitt.openid4vc.domain.model.keyBinding.Jwk
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.CreateVcSdJwtVerifiablePresentationError
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequest
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.toCreateVcSdJwtVerifiablePresentationError
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtCredential
import ch.admin.foitt.openid4vc.domain.usecase.GetKeyPair
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.CreateVcSdJwtVerifiablePresentation
import ch.admin.foitt.openid4vc.utils.Constants.ANDROID_KEY_STORE
import ch.admin.foitt.openid4vc.utils.JsonParsingError
import ch.admin.foitt.openid4vc.utils.SafeJson
import ch.admin.foitt.openid4vc.utils.createDigest
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.ECDSASigner
import com.nimbusds.jose.jwk.Curve
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.time.Instant
import javax.inject.Inject

internal class CreateVcSdJwtVerifiablePresentationImpl @Inject constructor(
    private val safeJson: SafeJson,
    private val getKeyPair: GetKeyPair,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : CreateVcSdJwtVerifiablePresentation {
    override suspend fun invoke(
        credential: VcSdJwtCredential,
        requestedFields: List<String>,
        presentationRequest: PresentationRequest,
    ): Result<String, CreateVcSdJwtVerifiablePresentationError> = withContext(defaultDispatcher) {
        coroutineBinding {
            val sdJwtWithDisclosures = runSuspendCatching {
                credential.createVerifiableCredential(requestedFields)
            }.mapError { throwable -> throwable.toCreateVcSdJwtVerifiablePresentationError("createVerifiableCredential error") }
                .bind()

            if (credential.keyBindingIdentifier != null) {
                val base64UrlEncodedSdHash = runSuspendCatching {
                    sdJwtWithDisclosures.createDigest(HASH_ALGORITHM)
                }.mapError { throwable -> throwable.toCreateVcSdJwtVerifiablePresentationError("sdJwtWithDisclosures.createDigest error") }
                    .bind()

                val keyPair = getKeyPair(credential.keyBindingIdentifier, ANDROID_KEY_STORE)
                    .mapError(GetKeyPairError::toCreateVcSdJwtVerifiablePresentationError)
                    .bind()

                val keyBindingJwt = createKeyBindingJwt(credential, base64UrlEncodedSdHash, presentationRequest)
                val jwk = getKeyBindingJwk(credential).bind()
                val signer = ECDSASigner(keyPair.private, Curve(jwk.crv))
                keyBindingJwt.sign(signer)
                val keyBindingJwtString = keyBindingJwt.serialize()

                sdJwtWithDisclosures + keyBindingJwtString
            } else {
                sdJwtWithDisclosures
            }
        }
    }

    private fun createKeyBindingJwt(
        credential: VcSdJwtCredential,
        base64UrlEncodedSdHash: String,
        presentationRequest: PresentationRequest,
    ): SignedJWT {
        val jwtHeader = JWSHeader.Builder(credential.keyBindingAlgorithm?.toJWSAlgorithm())
            .type(JOSEObjectType(HEADER_TYPE))
            .build()
        val jwtBody = JWTClaimsSet.Builder()
            .claim(CLAIM_KEY_SD_HASH, base64UrlEncodedSdHash)
            .claim(CLAIM_KEY_AUD, presentationRequest.responseUri)
            .claim(CLAIM_KEY_NONCE, presentationRequest.nonce)
            .claim(CLAIM_KEY_IAT, Instant.now().epochSecond)
            .build()

        return SignedJWT(jwtHeader, jwtBody)
    }

    private fun getKeyBindingJwk(credential: VcSdJwtCredential): Result<Jwk, CreateVcSdJwtVerifiablePresentationError> {
        val cnf = runSuspendCatching {
            credential.cnf
        }.mapError { throwable -> throwable.toCreateVcSdJwtVerifiablePresentationError("credential.cnf error") }

        return safeJson.safeDecodeStringTo<Jwk>(cnf.value.toString())
            .mapError(JsonParsingError::toCreateVcSdJwtVerifiablePresentationError)
    }

    companion object {
        const val HASH_ALGORITHM = "SHA-256"
        const val HEADER_TYPE = "kb+jwt"
        const val CLAIM_KEY_SD_HASH = "sd_hash"
        const val CLAIM_KEY_AUD = "aud"
        const val CLAIM_KEY_NONCE = "nonce"
        const val CLAIM_KEY_IAT = "iat"
    }
}
