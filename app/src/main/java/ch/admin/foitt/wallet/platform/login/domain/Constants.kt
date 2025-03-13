package ch.admin.foitt.wallet.platform.login.domain

object Constants {
    const val MAX_LOGIN_ATTEMPTS = 5

    // we want to be just below 5 minutes, to avoid flickering of the timer on the lock screen
    const val BLOCKING_TIME_MS = 5 * 60 * 1000L - 1
}
