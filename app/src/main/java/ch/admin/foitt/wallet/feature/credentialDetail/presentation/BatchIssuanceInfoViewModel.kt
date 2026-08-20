package ch.admin.foitt.wallet.feature.credentialDetail.presentation

import android.content.Context
import androidx.lifecycle.viewModelScope
import ch.admin.foitt.openid4vc.domain.model.threshold
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.credentialDetail.presentation.model.BatchIssuanceInfoUiState
import ch.admin.foitt.wallet.feature.credentialDetail.presentation.model.BatchIssuanceToastEvent
import ch.admin.foitt.wallet.platform.batch.domain.usecase.GetBatchSize
import ch.admin.foitt.wallet.platform.credential.domain.usecase.RefreshCredential
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetCurrentAppLocale
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.ssi.domain.repository.BundleItemRepository
import ch.admin.foitt.wallet.platform.ssi.domain.repository.VerifiableCredentialRepository
import ch.admin.foitt.wallet.platform.utils.UiString
import ch.admin.foitt.wallet.platform.utils.asDayMonthYearHoursMinutesWith
import ch.admin.foitt.wallet.platform.utils.epochSecondsToZonedDateTime
import ch.admin.foitt.wallet.platform.utils.openLink
import ch.admin.foitt.wallet.platform.utils.trackCompletion
import com.github.michaelbull.result.get
import com.github.michaelbull.result.onErr
import com.github.michaelbull.result.onOk
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = BatchIssuanceInfoViewModel.Factory::class)
class BatchIssuanceInfoViewModel @AssistedInject constructor(
    @param:ApplicationContext private val appContext: Context,
    private val navManager: NavigationManager,
    private val bundleItemRepository: BundleItemRepository,
    private val verifiableCredentialRepository: VerifiableCredentialRepository,
    private val getBatchSize: GetBatchSize,
    private val getCurrentAppLocale: GetCurrentAppLocale,
    private val refreshCredential: RefreshCredential,
    setTopBarState: SetTopBarState,
    @Assisted private val credentialId: Long,
) : ScreenViewModel(setTopBarState = setTopBarState) {
    override val topBarState: TopBarState = TopBarState.Custom(
        title = UiString.Resource(R.string.tk_credentialDetail_issuanceInfo_title),
        onUp = ::onBack
    )

    @AssistedFactory
    interface Factory {
        fun create(credentialId: Long): BatchIssuanceInfoViewModel
    }

    private val _isRenewing = MutableStateFlow(false)
    val isRenewing = _isRenewing.asStateFlow()

    private val _toastEvents = MutableSharedFlow<BatchIssuanceToastEvent>(extraBufferCapacity = 1)
    val toastEvents = _toastEvents.asSharedFlow()

    private val _uiState = MutableStateFlow<BatchIssuanceInfoUiState>(BatchIssuanceInfoUiState.Initial)
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch { updateUiState() }
    }

    private suspend fun updateUiState() {
        val availableUsages = bundleItemRepository.getNeverPresentedCount(credentialId).get() ?: 0
        val credential = verifiableCredentialRepository.getById(credentialId).get()
        val lastRenewalSeconds = credential?.refreshedAt ?: credential?.createdAt
        val lastRenewal = lastRenewalSeconds
            ?.epochSecondsToZonedDateTime()
            ?.asDayMonthYearHoursMinutesWith(", ", getCurrentAppLocale())
            .orEmpty()

        _uiState.value = if (availableUsages == 0) {
            BatchIssuanceInfoUiState.Ready.Exhausted(
                availableUsages = "0",
                lastRenewal = lastRenewal,
            )
        } else {
            BatchIssuanceInfoUiState.Ready.Normal(
                availableUsages = availableUsages.toString(),
                lastRenewal = lastRenewal,
                refreshThreshold = getBatchSize(credentialId).threshold.coerceAtLeast(MIN_REFRESH_THRESHOLD),
            )
        }
    }

    fun onBack() {
        navManager.popBackStack()
    }

    fun onOpenMoreInfo() {
        appContext.openLink(R.string.tk_credentialDetail_issuanceInfo_more_info_link)
    }

    fun onRenew() {
        viewModelScope.launch {
            refreshCredential(credentialId)
                .onOk {
                    updateUiState()
                    _toastEvents.tryEmit(BatchIssuanceToastEvent.RENEWAL_SUCCESS)
                }
                .onErr {
                    _toastEvents.tryEmit(BatchIssuanceToastEvent.RENEWAL_FAILURE)
                }
        }.trackCompletion(_isRenewing)
    }

    private companion object {
        private const val MIN_REFRESH_THRESHOLD = 1
    }
}
