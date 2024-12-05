package ch.admin.foitt.wallet.platform.scaffold.domain.usecase

import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState

fun interface SetTopBarState {
    operator fun invoke(state: TopBarState)
}
