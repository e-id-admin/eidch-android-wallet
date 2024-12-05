package ch.admin.foitt.wallet.platform.scaffold.domain.repository

import ch.admin.foitt.wallet.platform.scaffold.domain.model.ErrorDialogState
import kotlinx.coroutines.flow.StateFlow

interface ErrorDialogRepository {
    val state: StateFlow<ErrorDialogState>
    fun setState(errorDialogState: ErrorDialogState)
}
