package ch.admin.foitt.wallet.feature.settings.presentation.licences

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.SpacerBottom
import ch.admin.foitt.wallet.platform.composables.SpacerTop
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme
import com.mikepenz.aboutlibraries.ui.compose.LibrariesContainer
import com.mikepenz.aboutlibraries.ui.compose.LibraryDefaults.libraryColors
import com.ramcosta.composedestinations.annotation.Destination

@Destination
@Composable
fun LicencesScreen(viewModel: LicencesViewModel) {
    LicencesScreenContent(
        onMoreInformation = viewModel::onMoreInformation
    )
}

@Composable
private fun LicencesScreenContent(
    onMoreInformation: () -> Unit,
) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Sizes.s02)
            .background(MaterialTheme.colorScheme.background),
    ) {
        val (
            topSpacerRef,
            mainContentRef,
            bottomSpacerRef,
        ) = createRefs()

        SpacerTop(
            backgroundColor = MaterialTheme.colorScheme.background,
            modifier = Modifier.constrainAs(topSpacerRef) {
                top.linkTo(parent.top)
            },
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .constrainAs(mainContentRef) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    height = Dimension.fillToConstraints
                }
        ) {
            Licences(
                onMoreInformation = onMoreInformation,
            )
        }
        SpacerBottom(
            backgroundColor = MaterialTheme.colorScheme.background,
            modifier = Modifier.constrainAs(bottomSpacerRef) {
                bottom.linkTo(parent.bottom)
            },
            useNavigationBarInsets = true,
        )
    }
}

@Composable
private fun Licences(
    onMoreInformation: () -> Unit
) {
    LibrariesContainer(
        modifier = Modifier
            .fillMaxSize(),
        header = {
            item {
                Column(
                    modifier = Modifier.padding(top = Sizes.s05, start = Sizes.s04, end = Sizes.s04)
                ) {
                    Image(
                        modifier = Modifier.fillMaxWidth(),
                        painter = painterResource(id = R.drawable.pilot_ic_licences),
                        contentDescription = null,
                        alignment = Alignment.Center,
                        contentScale = ContentScale.Fit,
                    )
                    Spacer(modifier = Modifier.height(Sizes.s06))
                    WalletTexts.Body(text = stringResource(id = R.string.licences_text))
                    Spacer(modifier = Modifier.height(Sizes.s06))
                    Buttons.TextLink(
                        text = stringResource(id = R.string.licences_more_information_text),
                        endIcon = painterResource(id = R.drawable.pilot_ic_link),
                        onClick = onMoreInformation,
                    )
                    Spacer(modifier = Modifier.height(Sizes.s06))
                }
            }
        },
        colors = libraryColors(
            badgeBackgroundColor = MaterialTheme.colorScheme.primary,
            badgeContentColor = MaterialTheme.colorScheme.onPrimary
        ),
        itemContentPadding = PaddingValues(horizontal = Sizes.s04, vertical = Sizes.s04),
        showLicenseBadges = false,
        showAuthor = false,
        contentPadding = WindowInsets.navigationBars.asPaddingValues()
    )
}

@WalletAllScreenPreview
@Composable
fun LicencesScreenPreview() {
    WalletTheme {
        LicencesScreenContent(
            onMoreInformation = {}
        )
    }
}