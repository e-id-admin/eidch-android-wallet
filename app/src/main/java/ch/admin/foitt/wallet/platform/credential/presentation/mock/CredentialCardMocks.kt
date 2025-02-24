package ch.admin.foitt.wallet.platform.credential.presentation.mock

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.credential.presentation.model.CredentialCardState
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.CredentialDisplayStatus
import ch.admin.foitt.wallet.platform.preview.ComposableWrapper
import ch.admin.foitt.wallet.theme.WalletTheme

object CredentialCardMocks {

    val state1 @Composable get() = CredentialCardState(
        credentialId = 1L,
        title = "Lernfahrausweis B",
        subtitle = "Max Mustermann",
        status = CredentialDisplayStatus.Valid,
        logo = painterResource(id = R.drawable.wallet_ic_shield_cross),
        backgroundColor = Color(0xFF00A3E0),
        contentColor = WalletTheme.colorScheme.onPrimaryContainer,
        borderColor = WalletTheme.colorScheme.primaryContainer,
        isCredentialFromBetaIssuer = true,
    )

    val state2 @Composable get() = CredentialCardState(
        credentialId = 2L,
        title = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore " +
            "et dolore magna aliquyam erat, sed diam voluptua.",
        subtitle = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore " +
            "et dolore magna aliquyam erat, sed diam voluptua.",
        status = CredentialDisplayStatus.Valid,
        logo = painterResource(id = R.drawable.wallet_ic_shield_cross),
        backgroundColor = Color(0xFF444444),
        contentColor = WalletTheme.colorScheme.onPrimaryContainer,
        borderColor = WalletTheme.colorScheme.primaryContainer,
        isCredentialFromBetaIssuer = false,
    )

    val state3 @Composable get() = CredentialCardState(
        credentialId = 3L,
        title = "Lernfahrausweis B",
        subtitle = null,
        status = CredentialDisplayStatus.Valid,
        logo = painterResource(id = R.drawable.wallet_ic_shield_cross),
        backgroundColor = Color(0xFFFFFFFF),
        contentColor = WalletTheme.colorScheme.onPrimaryContainer,
        borderColor = WalletTheme.colorScheme.primaryContainer,
        isCredentialFromBetaIssuer = false,
    )

    val state4 @Composable get() = CredentialCardState(
        credentialId = 4L,
        title = null,
        subtitle = null,
        status = CredentialDisplayStatus.Valid,
        logo = painterResource(id = R.drawable.wallet_ic_shield_cross),
        backgroundColor = Color(0xFFFFFF55),
        contentColor = WalletTheme.colorScheme.onPrimaryContainer,
        borderColor = WalletTheme.colorScheme.primaryContainer,
        isCredentialFromBetaIssuer = false,
    )

    val state5 @Composable get() = CredentialCardState(
        credentialId = 5L,
        title = null,
        subtitle = null,
        status = CredentialDisplayStatus.Valid,
        logo = null,
        backgroundColor = Color(0xFF772277),
        contentColor = WalletTheme.colorScheme.onPrimaryContainer,
        borderColor = WalletTheme.colorScheme.primaryContainer,
        isCredentialFromBetaIssuer = false,
    )

    val mocks = sequenceOf(
        ComposableWrapper { state1 },
        ComposableWrapper { state2 },
        ComposableWrapper { state3 },
        ComposableWrapper { state4 },
        ComposableWrapper { state5 },
    )
}
