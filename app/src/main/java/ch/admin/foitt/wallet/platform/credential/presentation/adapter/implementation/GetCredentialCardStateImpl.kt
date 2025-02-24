package ch.admin.foitt.wallet.platform.credential.presentation.adapter.implementation

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import ch.admin.foitt.wallet.platform.composables.presentation.adapter.GetColor
import ch.admin.foitt.wallet.platform.composables.presentation.adapter.GetDrawableFromUri
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialPreview
import ch.admin.foitt.wallet.platform.credential.presentation.adapter.GetCredentialCardState
import ch.admin.foitt.wallet.platform.credential.presentation.model.CredentialCardState
import ch.admin.foitt.wallet.platform.utils.toPainter
import javax.inject.Inject

internal class GetCredentialCardStateImpl @Inject constructor(
    private val getColor: GetColor,
    private val getDrawableFromUri: GetDrawableFromUri,
) : GetCredentialCardState {

    override suspend fun invoke(credentialPreview: CredentialPreview): CredentialCardState {
        val backgroundColor: Color = getColor(credentialPreview.backgroundColor) ?: CredentialCardState.defaultCardColor

        return CredentialCardState(
            credentialId = credentialPreview.credentialId,
            title = credentialPreview.title,
            subtitle = credentialPreview.subtitle,
            status = credentialPreview.status,
            borderColor = getColor(credentialPreview.backgroundColor) ?: CredentialCardState.defaultCardColor,
            backgroundColor = backgroundColor,
            contentColor = getBestContrastColor(backgroundColor),
            logo = getDrawableFromUri(credentialPreview.logoUri)?.toPainter(),
            isCredentialFromBetaIssuer = credentialPreview.isCredentialFromBetaIssuer
        )
    }

    private fun getBestContrastColor(backgroundColor: Color): Color {
        val blackColor: Color = Color.Black
        val whiteColor: Color = Color.White
        val contrastWithBlack = ColorUtils.calculateContrast(backgroundColor.toArgb(), blackColor.toArgb())
        val contrastWithWhite = ColorUtils.calculateContrast(backgroundColor.toArgb(), whiteColor.toArgb())

        return if (contrastWithWhite > contrastWithBlack) whiteColor else blackColor
    }
}
