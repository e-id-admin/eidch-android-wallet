package ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation

import androidx.lifecycle.SavedStateHandle
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.GuardianConsentResultState
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetCurrentAppLocale
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.utils.asDayFullMonthYear
import ch.admin.foitt.wallet.platform.utils.asDayFullMonthYearHoursMinutes
import ch.admin.foitt.walletcomposedestinations.destinations.EIdGuardianConsentResultScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.EIdIntroScreenDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class EIdGuardianConsentResultViewModel @Inject constructor(
    private val navManager: NavigationManager,
    private val getCurrentAppLocale: GetCurrentAppLocale,
    setTopBarState: SetTopBarState,
    savedStateHandle: SavedStateHandle,
) : ScreenViewModel(setTopBarState) {
    override val topBarState = TopBarState.EmptyWithCloseButton(
        onClose = { navManager.navigateBackToHome(EIdIntroScreenDestination) }
    )

    private val navArgs = EIdGuardianConsentResultScreenDestination.argsFrom(savedStateHandle)

    val screenState: GuardianConsentResultState = navArgs.screenState

    private val date = navArgs.rawDeadline?.let {
        ZonedDateTime.parse(it, DateTimeFormatter.ISO_ZONED_DATE_TIME)
    }

    val formattedDate: String? get() = date?.let {
        when (screenState) {
            GuardianConsentResultState.QUEUEING_LEGAL_CONSENT_OK -> date.asDayFullMonthYear(getCurrentAppLocale())
            GuardianConsentResultState.AV_READY_LEGAL_CONSENT_PENDING -> date.asDayFullMonthYearHoursMinutes(getCurrentAppLocale())
            GuardianConsentResultState.QUEUEING_LEGAL_CONSENT_PENDING,
            GuardianConsentResultState.AV_EXPIRED_LEGAL_CONSENT_PENDING -> null
        }
    }

    fun onNext() = navManager.navigateBackToHome(EIdIntroScreenDestination)
}
