package ch.admin.foitt.wallet.feature.settings.presentation.biometrics

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.biometrics.presentation.BiometricsContent
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.LoadingOverlay
import ch.admin.foitt.wallet.platform.composables.presentation.layout.ScrollableColumn
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.navArgs.domain.model.EnableBiometricsNavArg
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.platform.utils.LocalActivity
import ch.admin.foitt.wallet.platform.utils.OnResumeEventHandler
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTheme
import com.ramcosta.composedestinations.annotation.Destination

@Destination(
    navArgsDelegate = EnableBiometricsNavArg::class,
)
@Composable
fun EnableBiometricsScreen(viewModel: EnableBiometricsViewModel) {
    val currentActivity = LocalActivity.current
    val areBiometricsEnabled = viewModel.areBiometricsEnabled.collectAsStateWithLifecycle().value

    OnResumeEventHandler {
        viewModel.refreshBiometricStatus(currentActivity)
    }

    if (areBiometricsEnabled) {
        BiometricsAvailableContent(
            onTriggerPrompt = { viewModel.enableBiometricsLogin(currentActivity) }
        )
    } else {
        BiometricsDisabledContent(
            onOpenSettings = viewModel::openSettings,
        )
    }

    LoadingOverlay(
        showOverlay = viewModel.initializationInProgress.collectAsStateWithLifecycle().value
    )
}

@Composable
private fun BiometricsAvailableContent(
    onTriggerPrompt: () -> Unit,
) = WalletLayouts.ScrollableColumn(
    useStatusBarInsets = false,
    useNavigationBarInsets = false,
    contentPadding = contentPadding,
    scrollableContent = {
        BiometricsContent(
            header = R.string.change_biometrics_header_text,
            description = R.string.change_biometrics_content_text,
            infoText = R.string.change_biometrics_info_text,
        )
    },
    stickyBottomContent = {
        Buttons.FilledTertiary(
            text = stringResource(id = R.string.change_biometrics_activate_button),
            onClick = onTriggerPrompt,
        )
    }
)

@Composable
private fun BiometricsDisabledContent(
    onOpenSettings: () -> Unit,
) = WalletLayouts.ScrollableColumn(
    useStatusBarInsets = false,
    useNavigationBarInsets = false,
    contentPadding = contentPadding,
    scrollableContent = {
        BiometricsContent(
            header = R.string.change_biometrics_header_text,
            description = R.string.change_biometrics_content_text,
            infoText = R.string.change_biometrics_info_text,
        )
    },
    stickyBottomContent = {
        Buttons.FilledPrimary(
            text = stringResource(id = R.string.change_biometrics_goToSettings_button),
            onClick = onOpenSettings,
            endIcon = painterResource(id = R.drawable.pilot_ic_link),
        )
    }
)

private val contentPadding = PaddingValues(
    top = Sizes.s05,
    bottom = Sizes.s06,
    start = Sizes.s06,
    end = Sizes.s06
)

private class EnableBiometricsPreviewParams : PreviewParameterProvider<Boolean> {
    override val values = sequenceOf(true, false)
}

@WalletAllScreenPreview
@Composable
private fun EnableBiometricsPreview(
    @PreviewParameter(EnableBiometricsPreviewParams::class) previewParams: Boolean
) {
    WalletTheme {
        if (previewParams) {
            BiometricsAvailableContent(
                onTriggerPrompt = {},
            )
        } else {
            BiometricsDisabledContent(
                onOpenSettings = {},
            )
        }
    }
}
