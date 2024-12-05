package ch.admin.foitt.wallet.platform.scaffold.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.scaffold.domain.model.ErrorDialogState
import ch.admin.foitt.wallet.platform.scaffold.domain.repository.ErrorDialogRepository
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetErrorDialogState
import javax.inject.Inject

internal class SetErrorDialogStateImpl @Inject constructor(
    private val errorDialogRepository: ErrorDialogRepository,
) : SetErrorDialogState {
    override fun invoke(state: ErrorDialogState) {
        errorDialogRepository.setState(state)
    }
}
