package ch.admin.foitt.wallet.feature.credentialDetail.presentation

import android.content.Context
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.utils.UiString
import ch.admin.foitt.wallet.platform.utils.openLink
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class IssuanceInfoViewModel @Inject constructor(
    @param:ApplicationContext private val appContext: Context,
    private val navManager: NavigationManager,
    setTopBarState: SetTopBarState,
) : ScreenViewModel(setTopBarState = setTopBarState) {
    override val topBarState: TopBarState = TopBarState.Custom(
        title = UiString.Resource(R.string.tk_credentialDetail_issuanceInfo_title),
        onUp = ::onBack
    )

    fun onBack() {
        navManager.popBackStack()
    }

    fun onOpenMoreInfo() {
        appContext.openLink(R.string.tk_credentialDetail_issuanceInfo_more_info_link)
    }
}
