package ch.admin.foitt.wallet.platform.credential.presentation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialClaimData
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialClaimImage
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialClaimText
import ch.admin.foitt.wallet.theme.Sizes
import coil.compose.AsyncImage

@Composable
fun ColumnScope.CredentialClaimItemsColumn(
    @StringRes title: Int,
    claims: List<CredentialClaimData>,
) {
    ListItemHeader(title = title, number = claims.size)

    for (claim in claims) {
        ListItem(
            modifier = if (claim is CredentialClaimImage) Modifier.padding(top = Sizes.s01) else Modifier,
            overlineContent = { Text(text = claim.localizedKey) },
            headlineContent = {
                when (claim) {
                    is CredentialClaimText -> Text(text = claim.value)
                    is CredentialClaimImage -> ClaimImage(claimImage = claim)
                }
            }
        )
        ItemDivider()
    }
}

@Composable
private fun ClaimImage(claimImage: CredentialClaimImage) {
    AsyncImage(
        modifier = Modifier
            .padding(top = Sizes.s02, bottom = Sizes.s01)
            .heightIn(max = Sizes.claimImageMaxHeight)
            .fillMaxWidth(),
        model = claimImage.imageData,
        alignment = Alignment.TopStart,
        contentScale = ContentScale.Fit,
        contentDescription = null,
        filterQuality = FilterQuality.High,
    )
}

@Composable
private fun ItemDivider() {
    HorizontalDivider(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = Sizes.s04),
    )
}

@Composable
private fun ListItemHeader(modifier: Modifier = Modifier, title: Int, number: Int = 0) {
    Column(modifier = modifier) {
        ListItem(
            modifier = Modifier.semantics { heading() },
            headlineContent = {
                Text(text = String.format(stringResource(id = title), number))
            }
        )
        HorizontalDivider(Modifier.fillMaxWidth())
    }
}
