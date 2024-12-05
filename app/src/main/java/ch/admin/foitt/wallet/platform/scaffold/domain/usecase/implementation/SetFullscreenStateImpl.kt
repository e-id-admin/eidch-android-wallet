package ch.admin.foitt.wallet.platform.scaffold.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.scaffold.domain.model.FullscreenState
import ch.admin.foitt.wallet.platform.scaffold.domain.repository.FullscreenStateRepository
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetFullscreenState
import javax.inject.Inject

class SetFullscreenStateImpl @Inject constructor(
    private val fullscreenStateRepository: FullscreenStateRepository,
) : SetFullscreenState {
    override fun invoke(state: FullscreenState) {
        fullscreenStateRepository.setState(state)
    }
}
