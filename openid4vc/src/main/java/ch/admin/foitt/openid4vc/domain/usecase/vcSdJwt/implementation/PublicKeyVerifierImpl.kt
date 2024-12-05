package ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.implementation

import ch.admin.eid.didresolver.didtoolbox.Jwk
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.PublicKeyVerifier
import com.nimbusds.jose.crypto.ECDSAVerifier
import com.nimbusds.jose.jwk.Curve
import com.nimbusds.jose.jwk.ECKey
import com.nimbusds.jose.util.Base64URL
import com.nimbusds.jwt.SignedJWT
import javax.inject.Inject

internal class PublicKeyVerifierImpl @Inject constructor() : PublicKeyVerifier {
    override fun matchSignature(publicKey: Jwk, signedJWT: SignedJWT): Boolean {
        val key = ECKey.Builder(
            Curve(publicKey.crv),
            Base64URL(publicKey.x),
            Base64URL(publicKey.y)
        ).build()
        val verifier = ECDSAVerifier(key)
        return signedJWT.verify(verifier)
    }
}
