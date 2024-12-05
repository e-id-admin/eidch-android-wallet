package ch.admin.foitt.wallet.platform.appLifecycleRepository.domain.repository

import ch.admin.foitt.wallet.platform.appLifecycleRepository.domain.model.AppLifecycleState
import kotlinx.coroutines.flow.StateFlow

interface AppLifecycleRepository {
    val state: StateFlow<AppLifecycleState>
}
