package ch.admin.foitt.wallet.platform.preview

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview
import ch.admin.foitt.wallet.platform.preview.PreviewConst.fontScale

internal object TabletPreviewConst {
    const val SmallWidthDp: Int = 500
    const val SmallHeightDp: Int = 650

    const val MediumWidthDp: Int = 800
    const val MediumHeightDp: Int = 800

    const val LargeWidthDp: Int = 950
    const val LargeHeightDp: Int = 1200
}

@Preview(
    name = "small tablet",
    group = "tablet portrait",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    showBackground = true,
    backgroundColor = PreviewConst.LightPreviewBackgroundColor,
    fontScale = fontScale,
    widthDp = TabletPreviewConst.SmallWidthDp,
    heightDp = TabletPreviewConst.SmallHeightDp,
)
internal annotation class TabletSmallScreenPreview

@Preview(
    name = "medium tablet",
    group = "tablet portrait",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    showBackground = true,
    backgroundColor = PreviewConst.LightPreviewBackgroundColor,
    fontScale = fontScale,
    widthDp = TabletPreviewConst.MediumWidthDp,
    heightDp = TabletPreviewConst.MediumHeightDp,
)
internal annotation class TabletMediumScreenPreview

@Preview(
    name = "large tablet",
    group = "tablet portrait",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    showBackground = true,
    backgroundColor = PreviewConst.LightPreviewBackgroundColor,
    fontScale = fontScale,
    widthDp = TabletPreviewConst.LargeWidthDp,
    heightDp = TabletPreviewConst.LargeHeightDp,
)
internal annotation class TabletLargeScreenPreview

@Preview(
    name = "small tablet landscape",
    group = "tablet landscape",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    showBackground = true,
    backgroundColor = PreviewConst.LightPreviewBackgroundColor,
    fontScale = fontScale,
    device = "spec:" +
        "width = ${TabletPreviewConst.SmallWidthDp}dp," +
        "height = ${TabletPreviewConst.SmallHeightDp}dp," +
        "orientation = landscape," +
        "dpi = 420",
)
internal annotation class TabletSmallLandscapeScreenPreview

@Preview(
    name = "medium tablet landscape",
    group = "tablet landscape",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    showBackground = true,
    backgroundColor = PreviewConst.LightPreviewBackgroundColor,
    fontScale = fontScale,
    device = "spec:" +
        "width = ${TabletPreviewConst.MediumWidthDp}dp," +
        "height = ${TabletPreviewConst.MediumHeightDp}dp," +
        "orientation = landscape," +
        "dpi = 420",
)
internal annotation class TabletMediumLandscapeScreenPreview

@Preview(
    name = "large tablet landscape",
    group = "tablet landscape",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    showBackground = true,
    backgroundColor = PreviewConst.LightPreviewBackgroundColor,
    fontScale = fontScale,
    device = "spec:" +
        "width = ${TabletPreviewConst.LargeWidthDp}dp," +
        "height = ${TabletPreviewConst.LargeHeightDp}dp," +
        "orientation = landscape," +
        "dpi = 420",
)
internal annotation class TabletLargeLandscapeScreenPreview
