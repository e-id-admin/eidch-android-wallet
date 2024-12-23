package ch.admin.foitt.wallet.platform.eventToast.domain.repository

import kotlinx.coroutines.flow.StateFlow

interface PassphraseChangeSuccessToastRepository {
    val passphraseChangeSuccess: StateFlow<Boolean>

    fun showPassphraseChangeSuccess()
    fun hidePassphraseChangeSuccess()
}
