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
    override suspend fun invoke(issuerDid: String, signedJwt: SignedJWT): Result<Unit, VerifyJwtError> = coroutineBinding {
        val didDoc = resolveDid(issuerDid)
            .mapError { error -> error.toVerifyJwtError() }
            .bind()
        val issuerJwks = didDoc.getIssuerJwks()
        verifySignature(issuerJwks, signedJwt).bind()
    }

    private fun DidDoc.getIssuerJwks() = getVerificationMethod().mapNotNull { verificationMethod ->
        verificationMethod.publicKeyJwk
    }

    private fun verifySignature(publicKeys: List<Jwk>, signedJWT: SignedJWT): Result<Unit, VcSdJwtError.InvalidJwt> = publicKeys.any {
        publicKeyVerifier.matchSignature(it, signedJWT)
    }.let {
        if (it) Ok(Unit) else Err(VcSdJwtError.InvalidJwt)
    }
}
