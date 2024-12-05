package ch.admin.foitt.wallet.platform.trustRegistry.domain.model

import ch.admin.foitt.openid4vc.domain.model.sdjwt.SdJwt

data class TrustStatement(
    val sdJwt: SdJwt,
)
