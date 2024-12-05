package ch.admin.foitt.wallet.feature.sessionTimeout.domain.implementation

import ch.admin.foitt.wallet.feature.sessionTimeout.domain.SessionTimeoutNavigation
import ch.admin.foitt.wallet.platform.login.domain.usecase.NavigateToLogin
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.utils.blackListedDestinationsSessionTimeout
import com.ramcosta.composedestinations.spec.Direction
import timber.log.Timber
import javax.inject.Inject

class SessionTimeoutNavigationImpl @Inject constructor(
    private val navManager: NavigationManager,
    private val navigateToLogin: NavigateToLogin,
) : SessionTimeoutNavigation {
    override suspend fun invoke(): Direction? {
        return if (blackListedDestinationsSessionTimeout.contains(navManager.currentDestination).not()) {
            Timber.d("Session timeout -> nav to login screen")
            navigateToLogin()
        } else {
            null
        }
    }
}
