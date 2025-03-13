package ch.admin.foitt.wallet.platform.scaffold.presentation

import androidx.lifecycle.ViewModel
import ch.admin.foitt.wallet.platform.scaffold.domain.repository.TopBarStateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
open class WalletTopBarViewModel @Inject constructor(
    private val topBarStateRepository: TopBarStateRepository,
) : ViewModel() {

    val state get() = topBarStateRepository.state
}
