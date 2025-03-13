package ch.admin.foitt.wallet.app.presentation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenContainer
import ch.admin.foitt.wallet.platform.utils.LocalActivity
import ch.admin.foitt.wallet.theme.WalletTheme
import com.ramcosta.composedestinations.animations.defaults.RootNavGraphDefaultAnimations
import com.ramcosta.composedestinations.rememberNavHostEngine

@Composable
fun MainScreen(
    activity: FragmentActivity,
    viewModel: MainViewModel = hiltViewModel(),
) {
    WalletTheme {
        val engine = rememberNavHostEngine(
            navHostContentAlignment = Alignment.Center,
            rootDefaultAnimations = rootNavigationAnimations,
            defaultAnimationsForNestedNavGraph = mapOf()
        )

        val navController = engine.rememberNavController()
        viewModel.initNavHost(navController)

        CompositionLocalProvider(LocalActivity provides activity) {
            ScreenContainer {
                NavigationHost(
                    engine = engine,
                    navController = navController,
                )
            }
        }

        LaunchedEffect(activity.intent) {
            viewModel.parseIntent(activity.intent)
        }
    }
}

private val rootNavigationAnimations = RootNavGraphDefaultAnimations(
    enterTransition = {
        slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Start,
            animationSpec = tween(durationMillis = 300)
        ) + fadeIn(
            tween(
                durationMillis = 300
            )
        )
    },
    exitTransition = {
        slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Start,
            animationSpec = tween(durationMillis = 300)
        ) + fadeOut(
            tween(
                durationMillis = 300
            )
        )
    },
    popExitTransition = {
        slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.End,
            animationSpec = tween(durationMillis = 300)
        ) + fadeOut(
            tween(
                durationMillis = 300
            )
        )
    },
    popEnterTransition = {
        slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.End,
            animationSpec = tween(durationMillis = 300)
        ) + fadeIn(
            tween(
                durationMillis = 300
            )
        )
    },
)
