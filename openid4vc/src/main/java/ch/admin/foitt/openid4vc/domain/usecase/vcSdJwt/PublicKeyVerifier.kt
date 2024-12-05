package ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt

import ch.admin.eid.didresolver.didtoolbox.Jwk
import com.nimbusds.jwt.SignedJWT

internal interface PublicKeyVerifier {
    fun matchSignature(publicKey: Jwk, signedJWT: SignedJWT): Boolean
}
