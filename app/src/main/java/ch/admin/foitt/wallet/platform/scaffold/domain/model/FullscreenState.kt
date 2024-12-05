package ch.admin.foitt.wallet.platform.scaffold.domain.model

/**
 * Sets the screen to use either
 * the full width and height (= draw behind system bars) or
 * use the insets
 */
sealed interface FullscreenState {
    data object Fullscreen : FullscreenState
    data object Insets : FullscreenState
}
