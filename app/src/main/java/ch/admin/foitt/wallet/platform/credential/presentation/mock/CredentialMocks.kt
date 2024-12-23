package ch.admin.foitt.wallet.platform.credential.presentation.mock

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialPreview
import ch.admin.foitt.wallet.platform.credential.presentation.model.CredentialCardState
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialStatus
import ch.admin.foitt.wallet.platform.preview.ComposableWrapper
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialClaimText

object CredentialMocks {

    val cardState01 @Composable get() = CredentialCardState(
        credentialId = 0L,
        title = "Lernfahrausweis B",
        subtitle = "Max Mustermann",
        status = CredentialStatus.VALID,
        logo = painterResource(id = R.drawable.pilot_ic_strassenverkehrsamt),
        backgroundColor = MaterialTheme.colorScheme.primaryContainer,
        textColor = MaterialTheme.colorScheme.onPrimaryContainer,
        borderColor = MaterialTheme.colorScheme.primaryContainer,
        isCredentialFromBetaIssuer = false,
    )

    private val cardState02 @Composable get() = CredentialCardState(
        credentialId = 0L,
        title = "Lernfahrausweis A",
        subtitle = "Lilly Mustermann",
        status = CredentialStatus.UNKNOWN,
        logo = painterResource(id = R.drawable.pilot_ic_swisscross_small),
        backgroundColor = Color(0xFF335588),
        textColor = MaterialTheme.colorScheme.onPrimaryContainer,
        borderColor = Color(0xFF335588),
        isCredentialFromBetaIssuer = false,
    )

    private val cardState03 @Composable get() = CredentialCardState(
        credentialId = 0L,
        title = "Lernfahrausweis B",
        subtitle = "Max Mustermann with a very looong name that does not fit in the card",
        status = CredentialStatus.SUSPENDED,
        logo = null,
        backgroundColor = Color(0xFF996644),
        textColor = MaterialTheme.colorScheme.onPrimaryContainer,
        borderColor = Color(0xFFBB9977),
        isCredentialFromBetaIssuer = false,
    )

    private val cardState04 @Composable get() = cardState01.copy(isCredentialFromBetaIssuer = true)
    private val cardState05 @Composable get() = cardState02.copy(isCredentialFromBetaIssuer = true)
    private val cardState06 @Composable get() = cardState03.copy(isCredentialFromBetaIssuer = true)

    val cardStates by lazy {
        sequenceOf(
            ComposableWrapper { cardState01 },
            ComposableWrapper { cardState02 },
            ComposableWrapper { cardState03 },
            ComposableWrapper { cardState04 },
            ComposableWrapper { cardState05 },
            ComposableWrapper { cardState06 },
        )
    }

    private val preview01 by lazy {
        CredentialPreview(
            credentialId = 9438,
            title = "Title 1",
            subtitle = "A subtitle",
            status = CredentialStatus.VALID,
            logoUri = null,
            backgroundColor = "#338855",
            isCredentialFromBetaIssuer = false,
        )
    }

    private val preview02 by lazy {
        CredentialPreview(
            credentialId = 9440,
            title = "Title 2",
            subtitle = "Another subtitle",
            status = CredentialStatus.REVOKED,
            logoUri = null,
            backgroundColor = "#335588",
            isCredentialFromBetaIssuer = false,
        )
    }

    private val preview03 by lazy {
        CredentialPreview(
            credentialId = 9440,
            title = "Title 3 that is somehow a little bit too long for comfort",
            subtitle = "And another subtitle with way to much text to properly be displayed ",
            status = CredentialStatus.UNKNOWN,
            logoUri = null,
            backgroundColor = "#883355",
            isCredentialFromBetaIssuer = false,
        )
    }

    val previewList by lazy {
        listOf(
            preview01,
            preview02,
            preview03,
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
