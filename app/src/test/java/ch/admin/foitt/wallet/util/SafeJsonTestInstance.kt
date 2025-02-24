package ch.admin.foitt.wallet.util

import ch.admin.foitt.wallet.platform.utils.SafeJson
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

object SafeJsonTestInstance {
    @OptIn(ExperimentalSerializationApi::class)
    val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        coerceInputValues = true
    }

    val safeJson = SafeJson(json)
}
