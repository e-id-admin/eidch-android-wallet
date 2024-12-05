package ch.admin.foitt.wallet.platform.scaffold.domain.usecase

import ch.admin.foitt.wallet.platform.scaffold.domain.model.ErrorDialogState

fun interface SetErrorDialogState {
    operator fun invoke(state: ErrorDialogState)
}
