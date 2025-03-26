package ch.admin.foitt.wallet.platform.credential.presentation.mock

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.credential.presentation.model.CredentialCardState
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.CredentialDisplayStatus
import ch.admin.foitt.wallet.platform.preview.ComposableWrapper
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialClaimText

object CredentialMocks {

    val cardState01
        @Composable get() = CredentialCardState(
            credentialId = 0L,
            title = "Lernfahrausweis B",
            subtitle = "Max Mustermann",
            status = CredentialDisplayStatus.Valid,
            logo = painterResource(id = R.drawable.ic_swiss_cross_small),
            backgroundColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            borderColor = MaterialTheme.colorScheme.primaryContainer,
            isCredentialFromBetaIssuer = false,
        )

    private val cardState02
        @Composable get() = CredentialCardState(
            credentialId = 0L,
            title = "Lernfahrausweis A",
            subtitle = "Lilly Mustermann",
            status = CredentialDisplayStatus.Unknown,
            logo = painterResource(id = R.drawable.ic_swiss_cross_small),
            backgroundColor = Color(0xFF335588),
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            borderColor = Color(0xFF335588),
            isCredentialFromBetaIssuer = false,
        )

    private val cardState03
        @Composable get() = CredentialCardState(
            credentialId = 0L,
            title = "Lernfahrausweis B",
            subtitle = "Max Mustermann with a very looong name that does not fit in the card",
            status = CredentialDisplayStatus.Suspended,
            logo = null,
            backgroundColor = Color(0xFF996644),
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            borderColor = Color(0xFFBB9977),
            isCredentialFromBetaIssuer = false,
        )

    private val cardState04 @Composable get() = cardState01.copy(isCredentialFromBetaIssuer = true)
    private val cardState05 @Composable get() = cardState02.copy(isCredentialFromBetaIssuer = true)
    private val cardState06 @Composable get() = cardState03.copy(isCredentialFromBetaIssuer = true)

    private val cardState07
        @Composable get() = CredentialCardState(
            credentialId = 0L,
            title = "Credential name",
            subtitle = null,
            status = CredentialDisplayStatus.Valid,
            logo = null,
            backgroundColor = CredentialCardState.defaultCardColor,
            contentColor = Color.Black,
            borderColor = CredentialCardState.defaultCardColor,
            isCredentialFromBetaIssuer = true,
        )

    val cardStates by lazy {
        sequenceOf(
            ComposableWrapper { cardState01 },
            ComposableWrapper { cardState02 },
            ComposableWrapper { cardState03 },
            ComposableWrapper { cardState04 },
            ComposableWrapper { cardState05 },
            ComposableWrapper { cardState06 },
            ComposableWrapper { cardState07 },
        )
    }

    val claimList by lazy {
        listOf(
            CredentialClaimText(localizedKey = "Vorname", value = "Max"),
            CredentialClaimText(localizedKey = "Nachname", value = "Mustermann"),
            CredentialClaimText(localizedKey = "Geburtsdatum", value = "01.01.1970"),
        )
    }
}
