package ch.admin.foitt.wallet.feature.onboarding.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.onboarding.presentation.composables.OnboardingScreenContent
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.presentation.ScreenMainImage
import ch.admin.foitt.wallet.platform.composables.presentation.SwipeableScreen
import ch.admin.foitt.wallet.platform.composables.presentation.layout.ScrollableColumnWithPicture
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.platform.utils.TestTags
import ch.admin.foitt.wallet.platform.utils.contentDescription
import ch.admin.foitt.wallet.theme.WalletTheme
import com.ramcosta.composedestinations.annotation.Destination

@Destination
@Composable
fun OnboardingIntroScreen(
    viewModel: OnboardingIntroViewModel,
) {
    SwipeableScreen(
        onSwipeForward = viewModel::onNext,
        onSwipeBackWard = {},
    ) {
        OnboardingIntroScreenContent(
            onNext = viewModel::onNext,
        )
    }
}

@Composable
private fun OnboardingIntroScreenContent(
    onNext: () -> Unit,
) = WalletLayouts.ScrollableColumnWithPicture(
    modifier = Modifier.contentDescription(stringResource(id = R.string.tk_onboarding_start_alt)),
    stickyStartContent = {
        ScreenMainImage(
            modifier = Modifier.testTag(TestTags.INTRO_ICON.name),
            iconRes = R.drawable.wallet_ic_shield_cross,
            backgroundRes = R.drawable.wallet_background_gradient_04,
        )
    },
    stickyBottomContent = {
        Buttons.FilledPrimary(
            modifier = Modifier.testTag(TestTags.START_BUTTON.name),
            text = stringResource(id = R.string.tk_onboarding_start_primarybutton),
            onClick = onNext,
        )
    }
) {
    OnboardingScreenContent(
        title = stringResource(id = R.string.tk_onboarding_start_title),
        subtitle = stringResource(id = R.string.tk_onboarding_start_body)
    )
}

@WalletAllScreenPreview
@Composable
private fun OnboardingIntroScreenPreview() {
    WalletTheme {
        OnboardingIntroScreenContent(
            onNext = {},
        )
    }
}
