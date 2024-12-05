package ch.admin.foitt.wallet.platform.composables.presentation.layout

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.constraintlayout.compose.Visibility
import androidx.window.core.layout.WindowHeightSizeClass
import ch.admin.foitt.wallet.platform.composables.SpacerBottom
import ch.admin.foitt.wallet.platform.composables.SpacerTop
import ch.admin.foitt.wallet.platform.composables.presentation.HeightReportingLayout
import ch.admin.foitt.wallet.platform.composables.presentation.horizontalSafeDrawing
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTheme

object WalletLayouts {
    //region Layout constants
    private val paddingStickySmall = Sizes.s02
    val paddingStickyMedium = Sizes.s04
    val paddingContentBottom = Sizes.s06

    val stickyBottomPaddingValuesPortrait = PaddingValues(
        top = Sizes.s03,
        start = paddingStickyMedium,
        end = paddingStickyMedium,
        bottom = paddingStickyMedium,
    )

    private val stickyBottomPaddingValuesLandscape = PaddingValues(
        vertical = paddingStickySmall,
        horizontal = paddingStickyMedium,
    )

    private val stickStartPaddingValuesLandscape = PaddingValues(
        start = paddingStickySmall,
        bottom = paddingStickySmall
    )

    const val cardCompactScreenRatio = 0.33f
    const val cardLargeScreenRatio = 0.5f
    //endregion

    @Composable
    fun isHeightCompact() = currentWindowAdaptiveInfo().windowSizeClass.windowHeightSizeClass == WindowHeightSizeClass.COMPACT

    @Composable
    private fun getCardScreenRatio() = if (isHeightCompact()) {
        cardCompactScreenRatio
    } else {
        cardLargeScreenRatio
    }

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    fun LargeContainer(
        modifier: Modifier = Modifier,
        onBottomHeightMeasured: (Dp) -> Unit,
        stickyBottomPadding: PaddingValues = stickyBottomPaddingValuesLandscape,
        stickyBottomContent: (@Composable () -> Unit)?,
        stickyStartPadding: PaddingValues = stickStartPaddingValuesLandscape,
        stickyStartContent: @Composable ColumnScope.() -> Unit,
        contentScrollState: ScrollState,
        content: @Composable ColumnScope.() -> Unit,
    ) = ConstraintLayout(
        modifier = modifier
            .fillMaxSize()
    ) {
        val (
            topSpacerRef,
            stickyStartRef,
            mainContentRef,
            stickyBottomRef,
        ) = createRefs()

        val cardScreenRatio = getCardScreenRatio()

        SpacerTop(
            backgroundColor = WalletTheme.colorScheme.surfaceTransparent,
            modifier = Modifier.constrainAs(topSpacerRef) {
                top.linkTo(parent.top)
            },
            useStatusBarInsets = false,
        )

        Column(
            modifier = Modifier
                .scrollable(
                    contentScrollState,
                    orientation = Orientation.Vertical,
                    reverseDirection = true,
                )
                .padding(stickyStartPadding)
                .constrainAs(stickyStartRef) {
                    start.linkTo(parent.start)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    height = Dimension.fillToConstraints
                    width = Dimension.percent(cardScreenRatio)
                }
        ) {
            stickyStartContent()
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .constrainAs(mainContentRef) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(stickyStartRef.end)
                    end.linkTo(parent.end)
                    height = Dimension.fillToConstraints
                    width = Dimension.fillToConstraints
                }
        ) {
            content()
        }

        Box(
            modifier = Modifier
                .constrainAs(stickyBottomRef) {
                    bottom.linkTo(parent.bottom)
                    start.linkTo(stickyStartRef.end)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                }
        ) {
            HeightReportingLayout(
                onContentHeightMeasured = onBottomHeightMeasured,
            ) {
                if (stickyBottomContent == null) {
                    SpacerBottom(
                        useNavigationBarInsets = true,
                        backgroundColor = WalletTheme.colorScheme.surfaceTransparent,
                    )
                } else {
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(WalletTheme.colorScheme.surface.copy(alpha = 0.85f))
                            .padding(stickyBottomPadding)
                            .navigationBarsPadding()
                            .focusGroup(),
                        horizontalArrangement = Arrangement.spacedBy(Sizes.s02, Alignment.End),
                        verticalArrangement = Arrangement.spacedBy(Sizes.s02, Alignment.Top),
                        maxItemsInEachRow = 2,
                    ) {
                        stickyBottomContent()
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    fun CompactContainer(
        modifier: Modifier = Modifier,
        onBottomHeightMeasured: ((Dp) -> Unit)?,
        stickyBottomPadding: PaddingValues = stickyBottomPaddingValuesPortrait,
        stickyBottomContent: (@Composable () -> Unit)?,
        useStatusBarInsets: Boolean,
        useNavigationBarInsets: Boolean,
        content: @Composable BoxScope.() -> Unit,
    ) = ConstraintLayout(
        modifier = modifier
            .fillMaxSize()
    ) {
        val (
            topSpacerRef,
            mainContentRef,
            bottomSpacerRef,
            stickyBottomRef,
        ) = createRefs()

        SpacerTop(
            backgroundColor = WalletTheme.colorScheme.surfaceTransparent,
            modifier = Modifier.constrainAs(topSpacerRef) {
                top.linkTo(parent.top)
            },
            useStatusBarInsets = useStatusBarInsets,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .constrainAs(mainContentRef) {
                    top.linkTo(parent.top)
                    onBottomHeightMeasured?.let {
                        bottom.linkTo(parent.bottom)
                    } ?: bottom.linkTo(stickyBottomRef.top)

                    height = Dimension.fillToConstraints
                }
        ) {
            content()
        }
        SpacerBottom(
            backgroundColor = WalletTheme.colorScheme.surfaceTransparent.copy(alpha = 0.85f),
            modifier = Modifier.constrainAs(bottomSpacerRef) {
                bottom.linkTo(stickyBottomRef.top)
            },
            useNavigationBarInsets = useNavigationBarInsets,
        )
        Box(
            modifier = Modifier
                .constrainAs(stickyBottomRef) {
                    bottom.linkTo(parent.bottom)
                    visibility = if (stickyBottomContent != null) {
                        Visibility.Visible
                    } else {
                        Visibility.Gone
                    }
                }
        ) {
            HeightReportingLayout(
                onContentHeightMeasured = onBottomHeightMeasured ?: {},
            ) {
                FlowRow(
                    modifier = Modifier
                        .background(WalletTheme.colorScheme.surface.copy(alpha = 0.85f))
                        .fillMaxWidth()
                        .padding(stickyBottomPadding)
                        .navigationBarsPadding()
                        .focusGroup(),
                    horizontalArrangement = Arrangement.spacedBy(Sizes.s02, Alignment.End),
                    verticalArrangement = Arrangement.spacedBy(Sizes.s02, Alignment.Top),
                    maxItemsInEachRow = 2,
                ) {
                    stickyBottomContent?.invoke()
                }
            }
        }
    }

    @Composable
    fun CompactContainerFloatingBottom(
        modifier: Modifier = Modifier,
        topBar: (@Composable () -> Unit)? = null,
        verticalArrangement: Arrangement.Vertical = Arrangement.Center,
        content: @Composable ColumnScope.() -> Unit,
        auxiliaryContent: (@Composable () -> Unit)? = null,
        stickyBottomHorizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
        stickyBottomContent: @Composable ColumnScope.() -> Unit,
    ) = ConstraintLayout(
        modifier = modifier.fillMaxSize()
    ) {
        var bottomBlockHeightDp by remember {
            mutableStateOf(0.dp)
        }

        val (
            topBarRef,
            contentRef,
            auxContentRef,
            stickyBottomRef,
        ) = createRefs()

        topBar?.let {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .constrainAs(topBarRef) {
                        top.linkTo(parent.top)
                    }
            ) {
                topBar()
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .constrainAs(contentRef) {
                    top.linkTo(if (topBar == null) parent.top else topBarRef.bottom)
                    bottom.linkTo(parent.bottom)
                    height = Dimension.fillToConstraints
                }
                .padding(horizontal = Sizes.s04)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = verticalArrangement,
        ) {
            Spacer(Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top)))
            content()
            Spacer(Modifier.height(bottomBlockHeightDp))
            Spacer(Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)))
        }

        auxiliaryContent?.let {
            Column(
                modifier = Modifier
                    .constrainAs(auxContentRef) {
                        bottom.linkTo(stickyBottomRef.top)
                    }
            ) {
                auxiliaryContent()
            }
        }

        HeightReportingLayout(
            modifier = Modifier
                .fillMaxWidth()
                .constrainAs(stickyBottomRef) {
                    bottom.linkTo(parent.bottom)
                },
            onContentHeightMeasured = { height -> bottomBlockHeightDp = height },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
                    .padding(Sizes.s04),
                horizontalAlignment = stickyBottomHorizontalAlignment,
            ) {
                stickyBottomContent()
            }
        }
    }

    @Composable
    fun LargeContainerFloatingBottom(
        modifier: Modifier = Modifier,
        topBar: (@Composable () -> Unit)? = null,
        useStatusBarPadding: Boolean = true,
        verticalArrangement: Arrangement.Vertical = Arrangement.Center,
        content: @Composable ColumnScope.() -> Unit,
        auxiliaryContent: (@Composable () -> Unit)? = null,
        stickyBottomHorizontalArrangement: Arrangement.Horizontal = Arrangement.Center,
        stickyBottomContent: (@Composable RowScope.() -> Unit)? = null,
    ) = ConstraintLayout(
        modifier = modifier
            .fillMaxSize()
            .horizontalSafeDrawing()
    ) {
        var bottomBlockHeightDp by remember {
            mutableStateOf(0.dp)
        }

        val (
            topBarRef,
            contentRef,
            auxContentRef,
            stickyBottomRef,
        ) = createRefs()

        topBar?.let {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .constrainAs(topBarRef) {
                        top.linkTo(parent.top)
                    }
            ) {
                topBar()
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .constrainAs(contentRef) {
                    top.linkTo(if (topBar == null) parent.top else topBarRef.bottom)
                    bottom.linkTo(parent.bottom)
                    height = Dimension.fillToConstraints
                }
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = verticalArrangement,
        ) {
            if (useStatusBarPadding) {
                Spacer(Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top)))
            }
            content()
            stickyBottomContent?.let {
                Spacer(Modifier.height(bottomBlockHeightDp))
            }
            Spacer(Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)))
        }

        stickyBottomContent?.let {
            HeightReportingLayout(
                modifier = Modifier
                    .fillMaxWidth()
                    .constrainAs(stickyBottomRef) {
                        bottom.linkTo(parent.bottom)
                    }
                    .background(Color.Transparent),
                onContentHeightMeasured = { height -> bottomBlockHeightDp = height },
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Sizes.s04),
                    horizontalArrangement = stickyBottomHorizontalArrangement,
                ) {
                    stickyBottomContent()
                }
            }
        }

        auxiliaryContent?.let {
            Column(
                modifier = Modifier
                    .constrainAs(auxContentRef) {
                        bottom.linkTo(parent.bottom)
                    }
            ) {
                auxiliaryContent()
            }
        }
    }
}
