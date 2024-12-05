package ch.admin.foitt.openid4vc.domain.model.sdjwt

import kotlinx.serialization.json.JsonElement

data class SdJwtClaim(
    val key: String,
    val value: JsonElement,
    val disclosure: String
)
