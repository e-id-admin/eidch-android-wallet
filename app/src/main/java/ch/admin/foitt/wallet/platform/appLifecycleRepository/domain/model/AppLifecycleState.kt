package ch.admin.foitt.wallet.platform.appLifecycleRepository.domain.model

import androidx.lifecycle.Lifecycle

sealed interface AppLifecycleState {
    object Foreground : AppLifecycleState
    object Background : AppLifecycleState

    companion object {
        fun from(lifecycleState: Lifecycle.State): AppLifecycleState = when (lifecycleState) {
            Lifecycle.State.DESTROYED,
            Lifecycle.State.INITIALIZED,
            Lifecycle.State.CREATED -> Background
            Lifecycle.State.STARTED,
            Lifecycle.State.RESUMED -> Foreground
        }
    }
}
