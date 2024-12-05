package ch.admin.foitt.wallet.app.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph

@RootNavGraph(start = true)
@Destination
@Composable
fun StartScreen(
    viewModel: StartViewModel,
) {
    LaunchedEffect(key1 = viewModel) {
        viewModel.navigateToFirstScreen()
    }
}
