package ch.admin.foitt.wallet.platform.credential.presentation.mock

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.credential.presentation.model.CredentialCardState
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialStatus
import ch.admin.foitt.wallet.platform.preview.ComposableWrapper

object CredentialCardMocks {

    val state1 @Composable get() = CredentialCardState(
        credentialId = 1L,
        title = "Lernfahrausweis B",
        subtitle = "Max Mustermann",
        status = CredentialStatus.VALID,
        logo = painterResource(id = R.drawable.wallet_ic_shield_cross),
        backgroundColor = Color(0xFF00A3E0),
        textColor = MaterialTheme.colorScheme.onPrimaryContainer,
        borderColor = MaterialTheme.colorScheme.primaryContainer,
        isCredentialFromBetaIssuer = false,
    )

    val state2 @Composable get() = CredentialCardState(
        credentialId = 2L,
        title = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore " +
            "et dolore magna aliquyam erat, sed diam voluptua.",
        subtitle = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore " +
            "et dolore magna aliquyam erat, sed diam voluptua.",
        status = CredentialStatus.VALID,
        logo = painterResource(id = R.drawable.wallet_ic_shield_cross),
        backgroundColor = Color(0xFF444444),
        textColor = MaterialTheme.colorScheme.onPrimaryContainer,
        borderColor = MaterialTheme.colorScheme.primaryContainer,
        isCredentialFromBetaIssuer = false,
    )

    val state3 @Composable get() = CredentialCardState(
        credentialId = 3L,
        title = "Lernfahrausweis B",
        subtitle = null,
        status = CredentialStatus.VALID,
        logo = painterResource(id = R.drawable.wallet_ic_shield_cross),
        backgroundColor = Color(0xFFFFFFFF),
        textColor = MaterialTheme.colorScheme.onPrimaryContainer,
        borderColor = MaterialTheme.colorScheme.primaryContainer,
        isCredentialFromBetaIssuer = false,
    )

    val state4 @Composable get() = CredentialCardState(
        credentialId = 4L,
        title = null,
        subtitle = null,
        status = CredentialStatus.VALID,
        logo = painterResource(id = R.drawable.wallet_ic_shield_cross),
        backgroundColor = Color(0xFFFFFF55),
        textColor = MaterialTheme.colorScheme.onPrimaryContainer,
        borderColor = MaterialTheme.colorScheme.primaryContainer,
        isCredentialFromBetaIssuer = false,
    )

    val state5 @Composable get() = CredentialCardState(
        credentialId = 5L,
        title = null,
        subtitle = null,
        status = CredentialStatus.VALID,
        logo = null,
        backgroundColor = Color(0xFF772277),
        textColor = MaterialTheme.colorScheme.onPrimaryContainer,
        borderColor = MaterialTheme.colorScheme.primaryContainer,
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
