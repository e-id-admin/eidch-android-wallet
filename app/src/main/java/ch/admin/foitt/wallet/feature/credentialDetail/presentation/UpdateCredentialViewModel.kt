package ch.admin.foitt.wallet.feature.credentialDetail.presentation

import androidx.lifecycle.viewModelScope
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.credentialDetail.domain.usecase.GetCredentialIssuerDisplaysFlow
import ch.admin.foitt.wallet.feature.credentialDetail.presentation.model.UpdateCredentialUiState
import ch.admin.foitt.wallet.platform.composables.presentation.adapter.GetDrawableFromUri
import ch.admin.foitt.wallet.platform.credential.domain.usecase.RefreshCredential
import ch.admin.foitt.wallet.platform.credentialRefresh.domain.usecase.IsCredentialRefreshable
import ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.UpdateCredentialStatus
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.utils.UiString
import ch.admin.foitt.wallet.platform.utils.toPainter
import ch.admin.foitt.wallet.platform.utils.trackCompletion
import com.github.michaelbull.result.get
import com.github.michaelbull.result.onErr
import com.github.michaelbull.result.onOk
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel(assistedFactory = UpdateCredentialViewModel.Factory::class)
class UpdateCredentialViewModel @AssistedInject constructor(
    private val refreshCredential: RefreshCredential,
    private val navManager: NavigationManager,
    private val getDrawableFromUri: GetDrawableFromUri,
    private val updateCredentialStatus: UpdateCredentialStatus,
    isCredentialRefreshable: IsCredentialRefreshable,
    getCredentialIssuerDisplaysFlow: GetCredentialIssuerDisplaysFlow,
    setTopBarState: SetTopBarState,
    @Assisted val credentialId: Long,
) : ScreenViewModel(setTopBarState) {
    override val topBarState = TopBarState.Custom(
        onUp = navManager::popBackStack,
        title = UiString.Resource(R.string.tk_displayrefresh_title)
    )

    @AssistedFactory
    interface Factory {
        fun create(credentialId: Long): UpdateCredentialViewModel
    }

    val isRefreshable: StateFlow<Boolean?> = flow {
        emit(isCredentialRefreshable(credentialId))
    }.toStateFlow(null)

    private val isLoading = MutableStateFlow<Boolean>(false)

    private val credentialIssuerUiState = getCredentialIssuerDisplaysFlow(credentialId)

    val uiState: StateFlow<UpdateCredentialUiState> = combine(
        credentialIssuerUiState,
        isRefreshable,
        isLoading,
    ) { issuerDisplayResult, isRefreshable, isLoading ->
        val issuerDisplay = issuerDisplayResult.get()
        when {
            issuerDisplay != null -> UpdateCredentialUiState(
                issuerName = issuerDisplay.name,
                issuerPainter = issuerDisplay.image.toPainter(),
                isRefreshable = isRefreshable,
                isLoading = isLoading,
            )
            else -> UpdateCredentialUiState(
                isRefreshable = isRefreshable,
                isLoading = isLoading,
            )
        }
    }.toStateFlow(UpdateCredentialUiState())

    fun onUpdate() {
        viewModelScope.launch {
            val refreshResult = refreshCredential(credentialId)
            updateCredentialStatus(credentialId)
            refreshResult
                .onOk {
                    Timber.d("Credential update: success")
                }
                .onErr {
                    Timber.d("Credential update: failed")
                }
            navManager.popBackStack()
        }.trackCompletion(isLoading)
    }

    private suspend fun String?.toPainter() = let { getDrawableFromUri(it)?.toPainter() }
}
