package ch.admin.foitt.wallet.platform.credential.presentation.adapter.implementation

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import ch.admin.foitt.wallet.platform.composables.presentation.adapter.GetColor
import ch.admin.foitt.wallet.platform.composables.presentation.adapter.GetDrawableFromUri
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialDisplayData
import ch.admin.foitt.wallet.platform.credential.presentation.adapter.GetCredentialCardState
import ch.admin.foitt.wallet.platform.credential.presentation.model.CredentialCardState
import ch.admin.foitt.wallet.platform.utils.toPainter
import javax.inject.Inject

internal class GetCredentialCardStateImpl @Inject constructor(
    private val getColor: GetColor,
    private val getDrawableFromUri: GetDrawableFromUri,
) : GetCredentialCardState {

    override suspend fun invoke(credentialDisplayData: CredentialDisplayData): CredentialCardState {
        val backgroundColor: Color = getColor(credentialDisplayData.backgroundColor) ?: CredentialCardState.defaultCardColor

        return CredentialCardState(
            credentialId = credentialDisplayData.credentialId,
            title = credentialDisplayData.title,
            subtitle = credentialDisplayData.subtitle,
            status = credentialDisplayData.status,
            borderColor = backgroundColor,
            backgroundColor = backgroundColor,
            contentColor = getBestContrastColor(backgroundColor),
            logo = getDrawableFromUri(credentialDisplayData.logoUri)?.toPainter(),
            isCredentialFromBetaIssuer = credentialDisplayData.isCredentialFromBetaIssuer
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
