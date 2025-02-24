package ch.admin.foitt.wallet.theme

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp

@Suppress("detekt:TooManyFunctions")
object WalletTexts {

    @Composable
    fun Button(
        text: String,
        modifier: Modifier = Modifier,
    ) = Text(
        text = text,
        style = WalletTheme.typography.labelLarge,
        textAlign = TextAlign.Start,
        overflow = TextOverflow.Ellipsis,
        maxLines = 2,
        modifier = modifier,
    )

    @Composable
    fun Headline(
        text: String,
        modifier: Modifier = Modifier,
    ) = Text(
        text = text,
        color = MaterialTheme.colorScheme.onSurface,
        style = MaterialTheme.typography.headlineLarge,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )

    @Composable
    fun LargeCredentialTitle(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = WalletTheme.colorScheme.onPrimary,
        maxLines: Int = Int.MAX_VALUE
    ) = Text(
        text = text,
        color = color,
        style = WalletTheme.typography.credentialLarge,
        textAlign = TextAlign.Start,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )

    @Composable
    fun LargeCredentialSubtitle(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = WalletTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
        maxLines: Int = Int.MAX_VALUE,
    ) = Text(
        text = text,
        color = color,
        style = WalletTheme.typography.credentialLarge,
        textAlign = TextAlign.Start,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )

    @Composable
    fun MediumCredentialTitle(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = WalletTheme.colorScheme.onPrimary,
        maxLines: Int = Int.MAX_VALUE
    ) = Text(
        text = text,
        color = color,
        style = WalletTheme.typography.credentialMedium,
        textAlign = TextAlign.Start,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )

    @Composable
    fun MediumCredentialSubtitle(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = WalletTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
        maxLines: Int = Int.MAX_VALUE,
    ) = Text(
        text = text,
        color = color,
        style = WalletTheme.typography.credentialMedium,
        textAlign = TextAlign.Start,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )

    @Composable
    fun TitleScreenMultiLine(
        text: String,
        modifier: Modifier = Modifier,
    ) = Text(
        text = text,
        color = MaterialTheme.colorScheme.onBackground,
        style = MaterialTheme.typography.titleLarge,
        textAlign = TextAlign.Start,
        overflow = TextOverflow.Ellipsis,
        minLines = 2,
        maxLines = 2,
        modifier = modifier,
    )

    @Composable
    fun TitleMedium(
        text: String,
        modifier: Modifier = Modifier,
        textAlign: TextAlign = TextAlign.Start,
        color: Color = WalletTheme.colorScheme.onSurface,
    ) = Text(
        text = text,
        color = color,
        style = WalletTheme.typography.titleMedium,
        textAlign = textAlign,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )

    @Composable
    fun Body(
        text: String,
        modifier: Modifier = Modifier,
        textAlign: TextAlign = TextAlign.Start,
        color: Color = MaterialTheme.colorScheme.onBackground,
    ) = Text(
        text = text,
        color = color,
        style = MaterialTheme.typography.bodyMedium,
        textAlign = textAlign,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )

    @Composable
    fun BodySmallCentered(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = MaterialTheme.colorScheme.onBackground,
    ) = Text(
        text = text,
        color = color,
        style = MaterialTheme.typography.bodySmall,
        textAlign = TextAlign.Center,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )

    @Composable
    fun LabelMedium(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = MaterialTheme.colorScheme.onSurface,
    ) = Text(
        text = text,
        color = color,
        style = MaterialTheme.typography.labelMedium,
        textAlign = TextAlign.Center,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )

    @Composable
    fun LabelMedium(
        text: AnnotatedString,
        modifier: Modifier = Modifier,
        color: Color = MaterialTheme.colorScheme.onSurface,
    ) = Text(
        text = text,
        color = color,
        style = MaterialTheme.typography.labelMedium,
        textAlign = TextAlign.Start,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )

    @Composable
    fun LabelSmall(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = MaterialTheme.colorScheme.onSurface,
    ) = Text(
        text = text,
        color = color,
        style = MaterialTheme.typography.labelSmall,
        textAlign = TextAlign.Start,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )

    @Composable
    fun InfoLabel(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = MaterialTheme.colorScheme.onSecondary,
    ) = Text(
        text = text,
        color = color,
        style = MaterialTheme.typography.labelSmall,
        lineHeight = 18.sp,
        textAlign = TextAlign.Start,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
    )

    //region PublicWallet Texts
    @Composable
    fun TitleTopBar(
        modifier: Modifier = Modifier,
        text: String,
        color: Color = WalletTheme.colorScheme.onSurface,
    ) = Text(
        text = text,
        color = color,
        style = WalletTheme.typography.titleLarge,
        textAlign = TextAlign.Start,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )

    @Composable
    fun TitleScreen(
        text: String,
        modifier: Modifier = Modifier,
        textAlign: TextAlign = TextAlign.Start,
        maxLines: Int = Int.MAX_VALUE,
        color: Color = WalletTheme.colorScheme.onSurface
    ) = Text(
        text = text,
        color = color,
        style = WalletTheme.typography.headlineMedium,
        textAlign = textAlign,
        overflow = TextOverflow.Ellipsis,
        maxLines = maxLines,
        modifier = modifier
            .fillMaxWidth()
            .semantics { heading() }
            .testTag("titleText"),
    )

    @Composable
    fun TitleLarge(
        text: String,
        modifier: Modifier = Modifier,
        textAlign: TextAlign = TextAlign.Start,
        maxLines: Int = 3,
        color: Color = WalletTheme.colorScheme.onSurface
    ) = Text(
        text = text,
        color = color,
        style = WalletTheme.typography.titleLarge,
        textAlign = textAlign,
        overflow = TextOverflow.Ellipsis,
        maxLines = maxLines,
        modifier = modifier
            .semantics { heading() },
    )

    @Composable
    fun TitleSmall(
        modifier: Modifier = Modifier,
        text: String,
        textAlign: TextAlign = TextAlign.Start,
        color: Color = WalletTheme.colorScheme.primary,
    ) = Text(
        text = text,
        color = color,
        style = WalletTheme.typography.titleSmall,
        textAlign = textAlign,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )

    @Composable
    fun BodyLarge(
        text: String,
        modifier: Modifier = Modifier,
        textAlign: TextAlign = TextAlign.Start,
        color: Color = WalletTheme.colorScheme.secondary
    ) = Text(
        text = text,
        color = color,
        style = WalletTheme.typography.bodyLarge,
        textAlign = textAlign,
        overflow = TextOverflow.Ellipsis,
        maxLines = Int.MAX_VALUE,
        modifier = modifier,
    )

    @Composable
    fun BodySmall(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = WalletTheme.colorScheme.onBackground,
    ) = Text(
        text = text,
        color = color,
        style = WalletTheme.typography.bodySmall,
        textAlign = TextAlign.Start,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )

    @Composable
    fun LabelLarge(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = WalletTheme.colorScheme.secondary,
    ) = Text(
        text = text,
        color = color,
        style = WalletTheme.typography.labelLarge,
        textAlign = TextAlign.Start,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )
//endregion
}
