package ch.admin.foitt.wallet.platform.credential.presentation.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialStatus

data class CredentialCardState(
    val credentialId: Long,
    val title: String?,
    val subtitle: String?,
    val status: CredentialStatus?,
    val logo: Painter?,
    val backgroundColor: Color,
    val textColor: Color,
    val borderColor: Color,
    val isCredentialFromBetaIssuer: Boolean,
) {
    companion object {
        val EMPTY by lazy {
            CredentialCardState(
                credentialId = 0,
                title = "",
                subtitle = null,
                status = null,
                logo = null,
                backgroundColor = defaultCardColor,
                textColor = defaultCardTextColor,
                borderColor = defaultCardColor,
                isCredentialFromBetaIssuer = false
            )
        }

        val defaultCardColor = Color(0xFF5E6D7E)
        val defaultCardTextColor = Color(0xFFF8FAFC)
        const val lightLevels = 4f
    }
}
