package com.dwanz.financecharts.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Shared design tokens for finance charts.
 * Keep palette market-neutral (teal / slate) — not a purple/indigo AI default.
 */
@Immutable
data class FinanceColorTokens(
    val background: Color = Color(0xFF0F1419),
    val surface: Color = Color(0xFF1A222C),
    val surfaceElevated: Color = Color(0xFF243040),
    val grid: Color = Color(0x334A5A6A),
    val axis: Color = Color(0xFF6B7C8C),
    val label: Color = Color(0xFFB8C4CE),
    val labelMuted: Color = Color(0xFF7A8A98),
    val seriesPrimary: Color = Color(0xFF3DD6C6),
    val seriesSecondary: Color = Color(0xFF7EB6FF),
    val seriesTertiary: Color = Color(0xFFF0B429),
    val areaFill: Color = Color(0x333DD6C6),
    val candleUp: Color = Color(0xFF3DDC97),
    val candleDown: Color = Color(0xFFFF6B6B),
    val wick: Color = Color(0xFF9AABBA),
    val volume: Color = Color(0x554A6A8A),
    val crosshair: Color = Color(0xCCE8F0F5),
    val tooltipBackground: Color = Color(0xF01A222C),
    val tooltipBorder: Color = Color(0xFF3DD6C6),
    val tooltipText: Color = Color(0xFFE8F0F5),
)

@Immutable
data class FinanceSpacingTokens(
    val xxs: Dp = 2.dp,
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 12.dp,
    val lg: Dp = 16.dp,
    val xl: Dp = 24.dp,
    val xxl: Dp = 32.dp,
    val plotPaddingStart: Dp = 48.dp,
    val plotPaddingEnd: Dp = 12.dp,
    val plotPaddingTop: Dp = 12.dp,
    val plotPaddingBottom: Dp = 28.dp,
)

@Immutable
data class FinanceTypographyTokens(
    val axisLabel: TextUnit = 10.sp,
    val tooltipTitle: TextUnit = 12.sp,
    val tooltipBody: TextUnit = 11.sp,
    val legend: TextUnit = 12.sp,
    val chartTitle: TextUnit = 16.sp,
)

@Immutable
data class FinanceShapeTokens(
    val tooltipCorner: Dp = 8.dp,
    val legendDot: Dp = 8.dp,
    val candleBodyMinWidth: Dp = 2.dp,
    val candleGapFraction: Float = 0.25f,
)

@Immutable
data class FinanceTokens(
    val colors: FinanceColorTokens = FinanceColorTokens(),
    val spacing: FinanceSpacingTokens = FinanceSpacingTokens(),
    val typography: FinanceTypographyTokens = FinanceTypographyTokens(),
    val shapes: FinanceShapeTokens = FinanceShapeTokens(),
)

val DefaultFinanceTokens = FinanceTokens()
