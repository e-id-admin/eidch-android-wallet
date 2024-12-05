package ch.admin.foitt.wallet.feature.onboarding.presentation.composables

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.presentation.layout.ScrollableColumnWithPicture
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.platform.utils.TestTags
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun OnboardingLoadingScreenContent() =
    WalletLayouts.ScrollableColumnWithPicture(
        modifier = Modifier
            .background(WalletTheme.colorScheme.background),
        stickyStartContent = { LoadingIndicator() },
        stickyBottomContent = null,
    ) {
        Spacer(modifier = Modifier.height(Sizes.s06))
        WalletTexts.TitleScreen(
            text = stringResource(id = R.string.onboarding_apply_settings_primary),
        )
        Spacer(modifier = Modifier.height(Sizes.s06))
        WalletTexts.BodyLarge(
            text = stringResource(id = R.string.onboarding_apply_settings_secondary),
            modifier = Modifier
                .fillMaxWidth()
        )
    }

@Composable
private fun LoadingIndicator() = BoxWithConstraints(
    modifier = Modifier
        .fillMaxSize()
        .clip(WalletTheme.shapes.extraLarge)
        .background(WalletTheme.colorScheme.surfaceContainerLow)
) {
    val loadingIndicatorSize = minOf(maxWidth - Sizes.s10, 250.dp)
    val animationData = createAnimationData(maxWidth = loadingIndicatorSize)
    Box(
        modifier = Modifier
            .width(animationData.width)
            .height(animationData.height)
            .align(Alignment.Center)
            .clip(RoundedCornerShape(animationData.height))
    ) {
        Image(
            modifier = Modifier
                .wrapContentWidth(unbounded = true)
                .offset(x = animationData.offset.dp)
                .testTag(TestTags.LOADING_ICON.name),
            painter = painterResource(id = R.drawable.wallet_background_gradient_04),
            contentDescription = null,
            alignment = Alignment.Center,
            contentScale = ContentScale.FillWidth,
        )
    }
}

private class AnimationData(
    width: State<Dp>,
    height: State<Dp>,
    offset: State<Float>,
) {
    val width by width
    val height by height
    val offset by offset
}

@Composable
private fun createAnimationData(maxWidth: Dp): AnimationData {
    val infiniteTransition = rememberInfiniteTransition(label = "infiniteTransition")
    val offset = infiniteTransition.animateFloat(
        initialValue = -50f,
        targetValue = 50f,
        infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offsetAnimation"
    )
    val width = remember { mutableStateOf(50.dp) }
    val height = remember { mutableStateOf(4.dp) }
    val sizes = createAnimationSequenceSizes(maxWidth.value)
    LaunchedEffect(Unit) {
        sizes.forEach { size ->
            delay(500)
            launch {
                width.value = size.width.dp
            }
            launch {
                height.value = size.height.dp
            }
        }
    }
    val springSpec = spring<Dp>(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)
    val widthAnimation =
        animateDpAsState(targetValue = width.value, label = "widthAnimation", animationSpec = springSpec)
    val heightAnimation =
        animateDpAsState(targetValue = height.value, label = "heightAnimation", animationSpec = springSpec)
    return remember {
        AnimationData(
            width = widthAnimation,
            height = heightAnimation,
            offset = offset,
        )
    }
}

private fun createAnimationSequenceSizes(maxWidth: Float) = listOf(
    Size(maxWidth, 4f),
    Size(maxWidth - Sizes.s04.value, 28f),
)

@WalletAllScreenPreview
@Composable
private fun OnboardingLoadingScreenContentPreview() {
    WalletTheme {
        OnboardingLoadingScreenContent()
    }
}
