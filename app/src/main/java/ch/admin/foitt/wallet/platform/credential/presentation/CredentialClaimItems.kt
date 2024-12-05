package ch.admin.foitt.wallet.platform.credential.presentation

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.Avatar
import ch.admin.foitt.wallet.platform.composables.AvatarSize
import ch.admin.foitt.wallet.platform.composables.presentation.spaceBarKeyClickable
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialClaimData
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialClaimImage
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialClaimText
import ch.admin.foitt.wallet.theme.Sizes
import coil.compose.AsyncImage

fun LazyListScope.credentialClaimItems(
    @StringRes title: Int,
    claims: List<CredentialClaimData>,
    issuer: String,
    issuerIcon: Painter?,
    onWrongData: () -> Unit,
) {
    item {
        ListItemHeader(title = title)
    }

    items(claims) { claim ->
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

    item {
        ListItemHeader(
            modifier = Modifier.padding(top = Sizes.s06),
            title = R.string.tk_displaydelete_displaycredential1_title5,
        )
        IssuerItem(issuer, issuerIcon)
        ItemDivider()
    }

    item {
        Spacer(modifier = Modifier.height(Sizes.s10))
        HorizontalDivider(Modifier.fillMaxWidth())
        WrongDataItem(onWrongData)
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
            .padding(horizontal = Sizes.s04),
    )
}

@Composable
private fun ListItemHeader(modifier: Modifier = Modifier, title: Int) {
    Column(modifier = modifier) {
        ListItem(
            modifier = Modifier.semantics { heading() },
            headlineContent = {
                Text(text = stringResource(id = title))
            }
        )
        HorizontalDivider(Modifier.fillMaxWidth())
    }
}

@Composable
private fun IssuerItem(
    issuer: String,
    issuerIcon: Painter?
) {
    ListItem(
        headlineContent = { Text(text = issuer) },
        leadingContent = { Avatar(imagePainter = issuerIcon, size = AvatarSize.SMALL) },
    )
}

@Composable
private fun WrongDataItem(onWrongData: () -> Unit) {
    ListItem(
        modifier = Modifier
            .clickable(onClick = onWrongData)
            .spaceBarKeyClickable(onWrongData),
        headlineContent = { Text(text = stringResource(id = R.string.tk_global_wrong_data)) },
        leadingContent = {
            Icon(
                painter = painterResource(id = R.drawable.wallet_ic_wrong_data),
                contentDescription = null,
            )
        },
        trailingContent = {
            Icon(
                modifier = Modifier.size(Sizes.s06),
                painter = painterResource(id = R.drawable.wallet_ic_chevron),
                contentDescription = null,
            )
        },
    )
}
