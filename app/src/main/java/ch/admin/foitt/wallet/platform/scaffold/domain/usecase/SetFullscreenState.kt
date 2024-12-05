package ch.admin.foitt.wallet.platform.scaffold.domain.usecase

import ch.admin.foitt.wallet.platform.scaffold.domain.model.FullscreenState

fun interface SetFullscreenState {
    operator fun invoke(state: FullscreenState)
}
