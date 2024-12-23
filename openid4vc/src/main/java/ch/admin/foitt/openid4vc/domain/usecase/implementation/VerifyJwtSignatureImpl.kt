package ch.admin.foitt.openid4vc.domain.usecase.implementation

import ch.admin.eid.didresolver.didtoolbox.DidDoc
import ch.admin.eid.didresolver.didtoolbox.Jwk
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtError
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VerifyJwtError
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.toVerifyJwtError
import ch.admin.foitt.openid4vc.domain.usecase.ResolveDid
import ch.admin.foitt.openid4vc.domain.usecase.VerifyJwtSignature
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.PublicKeyVerifier
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import com.nimbusds.jwt.SignedJWT
import javax.inject.Inject

internal class VerifyJwtSignatureImpl @Inject constructor(
    private val publicKeyVerifier: PublicKeyVerifier,
    private val resolveDid: ResolveDid,
) : VerifyJwtSignature {
    override suspend fun invoke(did: String, kid: String, signedJwt: SignedJWT): Result<Unit, VerifyJwtError> = coroutineBinding {
        val didDoc = resolveDid(did)
            .mapError { error -> error.toVerifyJwtError() }
            .bind()
        val publicKey = didDoc.getPublicKey(keyIdentifier = kid).bind()
        verifySignature(publicKey, signedJwt).bind()
    }

    private fun DidDoc.getPublicKey(keyIdentifier: String): Result<Jwk, VcSdJwtError.InvalidJwt> {
        val publicKey = getVerificationMethod()
            .firstOrNull { it.id.contentEquals(keyIdentifier) }?.publicKeyJwk

        return publicKey?.let {
            Ok(it)
        } ?: Err(VcSdJwtError.InvalidJwt)
    }

    private fun verifySignature(publicKey: Jwk, signedJWT: SignedJWT): Result<Unit, VcSdJwtError.InvalidJwt> {
        return if (publicKeyVerifier.matchSignature(publicKey, signedJWT)) {
            Ok(Unit)
        } else {
            Err(VcSdJwtError.InvalidJwt)
        }
    }
}
