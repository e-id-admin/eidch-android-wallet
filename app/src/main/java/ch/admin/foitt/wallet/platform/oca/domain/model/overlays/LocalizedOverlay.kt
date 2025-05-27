package ch.admin.foitt.wallet.platform.oca.domain.model.overlays

import kotlinx.serialization.Serializable

@Serializable
sealed interface LocalizedOverlay : Overlay {
    val language: String
}
