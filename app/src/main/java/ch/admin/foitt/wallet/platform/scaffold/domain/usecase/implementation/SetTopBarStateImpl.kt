package ch.admin.foitt.wallet.platform.scaffold.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.repository.TopBarStateRepository
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import timber.log.Timber
import javax.inject.Inject

class SetTopBarStateImpl @Inject constructor(
    private val topBarStateRepo: TopBarStateRepository,
) : SetTopBarState {
    override fun invoke(state: TopBarState) {
        Timber.d("SetTopBarState: $state")
        topBarStateRepo.setState(state)
    }
}
