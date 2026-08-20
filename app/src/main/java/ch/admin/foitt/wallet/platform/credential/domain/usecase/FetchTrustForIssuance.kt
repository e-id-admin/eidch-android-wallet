package ch.admin.foitt.wallet.platform.credential.domain.usecase

import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.wallet.platform.credential.domain.model.FetchTrustForIssuanceError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.IdentityV2TrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustCheckResult
import com.github.michaelbull.result.Result

interface FetchTrustForIssuance {
    suspend operator fun invoke(
        identityTrustStatement: IdentityV2TrustStatement?,
        protectedIssuanceAuthorizationTrustStatement: Jwt?,
        vcSchemaId: String,
        issuerDid: String,
    ): Result<TrustCheckResult?, FetchTrustForIssuanceError>
}
