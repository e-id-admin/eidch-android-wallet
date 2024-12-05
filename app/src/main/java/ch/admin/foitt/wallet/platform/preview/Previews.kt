package ch.admin.foitt.wallet.platform.preview

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview
import ch.admin.foitt.wallet.platform.preview.PreviewConst.fontScale

internal object PreviewConst {
    const val LightPreviewBackgroundColor: Long = 0xFFCCCCCC
    const val DarkPreviewBackgroundColor: Long = 0xFF333333

    const val SmallWidthDp: Int = 300
    const val SmallHeightDp: Int = 535

    const val MediumWidthDp: Int = 360
    const val MediumHeightDp: Int = 640

    const val LargeWidthDp: Int = 411
    const val LargeHeightDp: Int = 891
    const val fontScale = 1f
}

@Preview(
    name = "default dark",
    group = "default",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    backgroundColor = PreviewConst.DarkPreviewBackgroundColor,
    fontScale = fontScale,
)
@Preview(
    name = "default light",
    group = "default",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    showBackground = true,
    backgroundColor = PreviewConst.LightPreviewBackgroundColor,
    fontScale = fontScale,
)
internal annotation class WalletDefaultPreview

@Preview(
    name = "small",
    group = "phone portrait",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    showBackground = true,
    backgroundColor = PreviewConst.LightPreviewBackgroundColor,
    fontScale = fontScale,
    widthDp = PreviewConst.SmallWidthDp,
    heightDp = PreviewConst.SmallHeightDp,
)
internal annotation class PhoneSmallScreenPreview

@Preview(
    name = "medium",
    group = "phone portrait",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    showBackground = true,
    backgroundColor = PreviewConst.LightPreviewBackgroundColor,
    fontScale = fontScale,
    widthDp = PreviewConst.MediumWidthDp,
    heightDp = PreviewConst.MediumHeightDp,
)
internal annotation class PhoneMediumScreenPreview

@Preview(
    name = "large",
    group = "phone portrait",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    showBackground = true,
    backgroundColor = PreviewConst.LightPreviewBackgroundColor,
    fontScale = fontScale,
    widthDp = PreviewConst.LargeWidthDp,
    heightDp = PreviewConst.LargeHeightDp,
)
internal annotation class PhoneLargeScreenPreview

@Preview(
    name = "small landscape",
    group = "phone landscape",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    showBackground = true,
    backgroundColor = PreviewConst.LightPreviewBackgroundColor,
    fontScale = fontScale,
    device = "spec:" +
        "width = ${PreviewConst.SmallWidthDp}dp," +
        "height = ${PreviewConst.SmallHeightDp}dp," +
        "orientation = landscape," +
        "dpi = 420",
)
internal annotation class PhoneSmallLandscapeScreenPreview

@Preview(
    name = "medium landscape",
    group = "phone landscape",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    showBackground = true,
    backgroundColor = PreviewConst.LightPreviewBackgroundColor,
    fontScale = fontScale,
    device = "spec:" +
        "width = ${PreviewConst.MediumWidthDp}dp," +
        "height = ${PreviewConst.MediumHeightDp}dp," +
        "orientation = landscape," +
        "dpi = 420",
)
internal annotation class PhoneMediumLandscapeScreenPreview

@Preview(
    name = "large landscape",
    group = "phone landscape",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    showBackground = true,
    backgroundColor = PreviewConst.LightPreviewBackgroundColor,
    fontScale = fontScale,
    device = "spec:" +
        "width = ${PreviewConst.LargeWidthDp}dp," +
        "height = ${PreviewConst.LargeHeightDp}dp," +
        "orientation = landscape," +
        "dpi = 420",
)
internal annotation class PhoneLargeLandscapeScreenPreview

@WalletDefaultPreview
@PhoneSmallScreenPreview
@PhoneMediumScreenPreview
@PhoneLargeScreenPreview
@PhoneSmallLandscapeScreenPreview
@PhoneMediumLandscapeScreenPreview
@PhoneLargeLandscapeScreenPreview
internal annotation class WalletAllScreenPreview

@WalletDefaultPreview
@PhoneSmallScreenPreview
@PhoneMediumScreenPreview
@PhoneLargeScreenPreview
internal annotation class AllCompactScreensPreview

@WalletDefaultPreview
@PhoneSmallLandscapeScreenPreview
@PhoneMediumLandscapeScreenPreview
@PhoneLargeLandscapeScreenPreview
@TabletSmallScreenPreview
@TabletMediumScreenPreview
@TabletLargeScreenPreview
@TabletSmallLandscapeScreenPreview
@TabletMediumLandscapeScreenPreview
@TabletLargeLandscapeScreenPreview
internal annotation class AllLargeScreensPreview

@WalletDefaultPreview
@PhoneSmallScreenPreview
@PhoneMediumScreenPreview
@PhoneLargeScreenPreview
@PhoneSmallLandscapeScreenPreview
@PhoneMediumLandscapeScreenPreview
@PhoneLargeLandscapeScreenPreview
@TabletSmallScreenPreview
@TabletMediumScreenPreview
@TabletLargeScreenPreview
@TabletSmallLandscapeScreenPreview
@TabletMediumLandscapeScreenPreview
@TabletLargeLandscapeScreenPreview
internal annotation class AllScreensPreview

@Preview(
    name = "default light",
    group = "default",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    showBackground = true,
    backgroundColor = PreviewConst.LightPreviewBackgroundColor,
    fontScale = fontScale,
)
@Preview(
    name = "default dark",
    group = "default",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    backgroundColor = PreviewConst.DarkPreviewBackgroundColor,
    fontScale = fontScale,
)
internal annotation class WalletComponentPreview
