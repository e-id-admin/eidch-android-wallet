package ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class ApplyRequest(
    val mrz: List<String>,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val legalRepresentant: Boolean = false,
    val email: String? = null,
)
