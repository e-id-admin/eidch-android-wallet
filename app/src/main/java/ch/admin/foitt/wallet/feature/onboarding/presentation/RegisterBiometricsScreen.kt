package ch.admin.foitt.wallet.feature.onboarding.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.onboarding.presentation.composables.OnboardingLoadingScreenContent
import ch.admin.foitt.wallet.platform.biometrics.presentation.BiometricsAvailableImage
import ch.admin.foitt.wallet.platform.biometrics.presentation.BiometricsContent
import ch.admin.foitt.wallet.platform.biometrics.presentation.BiometricsUnavailableImage
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.presentation.layout.ScrollableColumnWithPicture
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.navArgs.domain.model.RegisterBiometricsNavArg
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.platform.utils.LocalActivity
import ch.admin.foitt.wallet.platform.utils.OnResumeEventHandler
import ch.admin.foitt.wallet.platform.utils.TestTags
import ch.admin.foitt.wallet.theme.WalletTheme
import com.ramcosta.composedestinations.annotation.Destination

@Destination(
    navArgsDelegate = RegisterBiometricsNavArg::class,
)
@Composable
fun RegisterBiometricsScreen(viewModel: RegisterBiometricsViewModel) {
    OnResumeEventHandler {
        viewModel.refreshScreenState()
    }

    val screenState = viewModel.screenState.collectAsStateWithLifecycle().value
    val currentActivity = LocalActivity.current

    AnimatedContent(
        targetState = viewModel.initializationInProgress.collectAsStateWithLifecycle().value,
        label = "loadingFadeIn"
    ) { initializing ->
        if (initializing) {
            OnboardingLoadingScreenContent()
        } else {
            RegisterBiometricsContent(
                onTriggerPrompt = { viewModel.enableBiometrics(currentActivity) },
                onOpenSettings = viewModel::openSettings,
                onSkip = viewModel::declineBiometrics,
                screenState = screenState,
            )
        }
    }
}

@Composable
private fun RegisterBiometricsContent(
    onTriggerPrompt: () -> Unit,
    onOpenSettings: () -> Unit,
    onSkip: () -> Unit,
    screenState: RegisterBiometricsScreenState
) = WalletLayouts.ScrollableColumnWithPicture(
    stickyStartContent = {
        BiometricsImage(screenState = screenState)
    },
    stickyBottomContent = {
        BiometricsButtons(
            screenState = screenState,
            onSkip = onSkip,
            onTriggerPrompt = onTriggerPrompt,
            onOpenSettings = onOpenSettings,
        )
    },
    content = {
        BiometricsBodyContent(
            screenState = screenState
        )
    }
)

@Composable
private fun BiometricsImage(screenState: RegisterBiometricsScreenState) = when (screenState) {
    RegisterBiometricsScreenState.Initial -> {}
    RegisterBiometricsScreenState.Available -> BiometricsAvailableImage()
    RegisterBiometricsScreenState.DisabledForApp,
    RegisterBiometricsScreenState.DisabledOnDevice,
    RegisterBiometricsScreenState.Error,
    RegisterBiometricsScreenState.Lockout -> BiometricsUnavailableImage()
}

@Composable
private fun BiometricsBodyContent(screenState: RegisterBiometricsScreenState) = when (screenState) {
    RegisterBiometricsScreenState.Initial -> {}
    RegisterBiometricsScreenState.Available -> BiometricsContent(
        title = R.string.tk_onboarding_biometricsPermission_primary,
        description = R.string.tk_onboarding_biometricsPermission_secondary,
        infoText = R.string.tk_onboarding_biometricsPermission_tertiary,
    )

    RegisterBiometricsScreenState.Lockout,
    RegisterBiometricsScreenState.Error -> BiometricsContent(
        title = R.string.tk_onboarding_biometricsPermission_primary,
        description = R.string.tk_onboarding_biometricsPermissionLater_secondary,
        infoText = null,
    )

    RegisterBiometricsScreenState.DisabledOnDevice -> BiometricsContent(
        title = R.string.tk_onboarding_biometricsPermission_primary,
        description = R.string.tk_onboarding_biometricsPermissionDisabledDevice_primary,
        infoText = R.string.tk_onboarding_biometricsPermissionDisabled_tertiary,
    )

    RegisterBiometricsScreenState.DisabledForApp -> BiometricsContent(
        title = R.string.tk_onboarding_biometricsPermissionDisabled_primary,
        description = R.string.tk_onboarding_biometricsPermissionDisabled_secondary,
        infoText = R.string.tk_onboarding_biometricsPermissionDisabled_tertiary,
    )
}

@Composable
private fun BiometricsButtons(
    screenState: RegisterBiometricsScreenState,
    onSkip: () -> Unit,
    onTriggerPrompt: () -> Unit,
    onOpenSettings: () -> Unit,
) = when (screenState) {
    RegisterBiometricsScreenState.Available -> {
        Buttons.TonalSecondary(
            text = stringResource(id = R.string.tk_global_no),
            onClick = onSkip,
        )
        Buttons.FilledPrimary(
            text = stringResource(id = R.string.tk_onboarding_biometricsPermission_button_primary),
            onClick = onTriggerPrompt,
        )
    }
    RegisterBiometricsScreenState.DisabledForApp -> {
        Buttons.TonalSecondary(
            text = stringResource(id = R.string.tk_global_no),
            onClick = onSkip,
            modifier = Modifier.testTag(TestTags.NO_BIOMETRICS_BUTTON.name)
        )
        Buttons.FilledPrimary(
            text = stringResource(id = R.string.tk_onboarding_biometricsPermissionDisabled_button_primary),
            onClick = onOpenSettings,
            modifier = Modifier.testTag(TestTags.TO_SETTING_BUTTON.name)
        )
    }
    RegisterBiometricsScreenState.DisabledOnDevice -> {
        Buttons.TonalSecondary(
            text = stringResource(id = R.string.tk_global_no),
            onClick = onSkip,
            modifier = Modifier.testTag(TestTags.NO_BIOMETRICS_BUTTON.name)
        )
        Buttons.FilledPrimary(
            text = stringResource(id = R.string.tk_onboarding_biometricsPermissionDisabled_button_primary),
            onClick = onOpenSettings,
            modifier = Modifier.testTag(TestTags.TO_SETTING_BUTTON.name)
        )
    }
    RegisterBiometricsScreenState.Error,
    RegisterBiometricsScreenState.Lockout -> {
        Buttons.FilledPrimary(
            text = stringResource(id = R.string.tk_global_continue),
            onClick = onSkip,
        )
    }
    RegisterBiometricsScreenState.Initial -> {}
}

private class RegisterBiometricsPreviewParams : PreviewParameterProvider<RegisterBiometricsScreenState> {
    override val values = sequenceOf(
        RegisterBiometricsScreenState.Available,
        RegisterBiometricsScreenState.DisabledOnDevice,
        RegisterBiometricsScreenState.Lockout,
        RegisterBiometricsScreenState.Error,
        RegisterBiometricsScreenState.Initial,
    )
}

@WalletAllScreenPreview
@Composable
private fun RegisterBiometricsPreview(
    @PreviewParameter(RegisterBiometricsPreviewParams::class) previewParams: RegisterBiometricsScreenState
) {
    WalletTheme {
        RegisterBiometricsContent(
            onTriggerPrompt = {},
            onSkip = {},
            onOpenSettings = {},
            screenState = previewParams,
        )
    }
}
