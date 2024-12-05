package ch.admin.foitt.wallet.platform.utils

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.os.bundleOf
import androidx.hilt.navigation.HiltViewModelFactory
import androidx.lifecycle.DEFAULT_ARGS_KEY
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * Creates a [ViewModel] using the hilt mechanism with the ability to provide arguments via the extras [Bundle] parameter that are
 * passed along to the [SavedStateHandle] of the created [ViewModel].
 *
 * @param extras The passed argument that will be available in the [SavedStateHandle] of the created [ViewModel]
 */
@Composable
inline fun <reified VM : ViewModel> componentViewModel(
    viewModelStoreOwner: ViewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) {
        "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
    },
    key: String? = null,
    extras: Bundle = bundleOf(),
): VM {
    val factory = createHiltViewModelFactory(viewModelStoreOwner)
    val creationExtras = if (viewModelStoreOwner is HasDefaultViewModelProviderFactory) {
        viewModelStoreOwner.defaultViewModelCreationExtras
    } else {
        CreationExtras.Empty
    }
    val mutableExtras = MutableCreationExtras(creationExtras)
    mutableExtras[DEFAULT_ARGS_KEY] = extras
    return viewModel(viewModelStoreOwner, key, factory = factory, extras = mutableExtras)
}

@Composable
@PublishedApi
internal fun createHiltViewModelFactory(
    viewModelStoreOwner: ViewModelStoreOwner
): ViewModelProvider.Factory? = if (viewModelStoreOwner is HasDefaultViewModelProviderFactory) {
    HiltViewModelFactory(
        context = LocalContext.current,
        delegateFactory = viewModelStoreOwner.defaultViewModelProviderFactory
    )
} else {
    // Use the default factory provided by the ViewModelStoreOwner
    // and assume it is an @AndroidEntryPoint annotated fragment or activity
    null
}
