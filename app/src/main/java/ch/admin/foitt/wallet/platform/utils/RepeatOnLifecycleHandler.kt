package ch.admin.foitt.wallet.platform.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineScope

@Composable
fun RepeatOnLifeCycleHandler(
    lifecycleState: Lifecycle.State = Lifecycle.State.RESUMED,
    action: suspend CoroutineScope.() -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(lifecycleState) {
            action()
        }
    }
}
