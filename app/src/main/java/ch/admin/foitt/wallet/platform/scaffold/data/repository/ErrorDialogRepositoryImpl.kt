package ch.admin.foitt.wallet.platform.scaffold.data.repository

import ch.admin.foitt.wallet.platform.scaffold.domain.model.ErrorDialogState
import ch.admin.foitt.wallet.platform.scaffold.domain.repository.ErrorDialogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

internal class ErrorDialogRepositoryImpl @Inject constructor() : ErrorDialogRepository {
    private val _state = MutableStateFlow<ErrorDialogState>(ErrorDialogState.Closed)
    override val state = _state.asStateFlow()

    override fun setState(errorDialogState: ErrorDialogState) {
        _state.value = errorDialogState
    }
}
