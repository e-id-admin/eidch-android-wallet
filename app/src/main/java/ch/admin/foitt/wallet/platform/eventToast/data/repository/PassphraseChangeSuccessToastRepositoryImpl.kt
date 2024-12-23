package ch.admin.foitt.wallet.platform.eventToast.data.repository

import ch.admin.foitt.wallet.platform.eventToast.domain.repository.PassphraseChangeSuccessToastRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class PassphraseChangeSuccessToastRepositoryImpl @Inject constructor() : PassphraseChangeSuccessToastRepository {
    private val _passwordChangeSuccess = MutableStateFlow(false)
    override val passphraseChangeSuccess = _passwordChangeSuccess.asStateFlow()

    override fun showPassphraseChangeSuccess() {
        _passwordChangeSuccess.value = true
    }

    override fun hidePassphraseChangeSuccess() {
        _passwordChangeSuccess.value = false
    }
}
