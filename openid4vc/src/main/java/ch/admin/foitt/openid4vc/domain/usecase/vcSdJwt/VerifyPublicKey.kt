package ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt

import ch.admin.eid.didresolver.didtoolbox.Jwk
import com.github.michaelbull.result.Result
import com.nimbusds.jwt.SignedJWT

internal interface VerifyPublicKey {
    operator fun invoke(publicKey: Jwk, signedJWT: SignedJWT): Result<Unit, Unit>
}
