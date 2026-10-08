package com.dwanz.financecharts.charts

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import com.dwanz.financecharts.model.Candle
import com.dwanz.financecharts.model.ChartStyle
import com.dwanz.financecharts.model.formatPrice
import com.dwanz.financecharts.model.formatVolume
import com.dwanz.financecharts.primitives.ChartGestureState
import com.dwanz.financecharts.primitives.chartGestures
import com.dwanz.financecharts.primitives.drawAxesAndGrid
import com.dwanz.financecharts.primitives.drawCandles
import com.dwanz.financecharts.primitives.drawChartBackground
import com.dwanz.financecharts.primitives.drawCrosshair
import com.dwanz.financecharts.primitives.drawTooltipBox
import com.dwanz.financecharts.primitives.drawVolumeStrip
import com.dwanz.financecharts.primitives.mapX
import com.dwanz.financecharts.primitives.mapY
import com.dwanz.financecharts.primitives.nearestCandle
import com.dwanz.financecharts.primitives.plotRect
import com.dwanz.financecharts.primitives.viewportForCandles

/**
 * OHLC candlestick chart with optional volume strip, pan/zoom, and crosshair tooltip.
 */
@Composable
fun CandlestickChart(
    candles: List<Candle>,
    modifier: Modifier = Modifier,
    style: ChartStyle = ChartStyle(),
    title: String? = null,
    showVolume: Boolean = true,
) {
    require(candles.isNotEmpty()) { "CandlestickChart requires candles" }
    val bounds = remember(candles) { viewportForCandles(candles) }
    val gesture = remember(candles) { ChartGestureState(bounds, bounds) }
    val progress = remember { Animatable(0f) }
    val textMeasurer = rememberTextMeasurer()

    LaunchedEffect(candles) {
        gesture.reset(bounds)
        progress.snapTo(0f)
        progress.animateTo(1f, tween(850, easing = FastOutSlowInEasing))
    }

    Layout(
        modifier = modifier
            .semantics { contentDescription = title ?: "Candlestick chart" }
            .testTag("candlestick_chart"),
        content = {
            if (title != null) {
                Text(
                    text = title,
                    color = style.tokens.colors.tooltipText,
                    fontSize = style.tokens.typography.chartTitle,
                    modifier = Modifier
                        .padding(bottom = style.tokens.spacing.sm)
                        .testTag("candle_chart_title"),
                )
            }
            Box(
                Modifier
                    .fillMaxWidth()
                    .chartGestures(gesture, style.enablePanZoom)
                    .testTag("candlestick_chart_plot"),
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    val padL = style.tokens.spacing.plotPaddingStart.toPx()
                    val padT = style.tokens.spacing.plotPaddingTop.toPx()
                    val padR = style.tokens.spacing.plotPaddingEnd.toPx()
                    val padB = style.tokens.spacing.plotPaddingBottom.toPx()
                    val full = plotRect(size, padL, padT, padR, padB)
                    val volumeFraction = if (showVolume) style.volumeHeightFraction else 0f
                    val gap = if (showVolume) 8f else 0f
                    val priceHeight = full.height * (1f - volumeFraction) - gap
                    val pricePlot = Rect(full.left, full.top, full.right, full.top + priceHeight)
                    val volumePlot = Rect(
                        full.left,
                        pricePlot.bottom + gap,
                        full.right,
                        full.bottom,
                    )
                    gesture.plotBounds = full

                    drawChartBackground(full, style)
                    drawAxesAndGrid(pricePlot, gesture.viewport, style, textMeasurer)
                    drawCandles(candles, gesture.viewport, pricePlot, style, progress.value)
                    if (showVolume) {
                        drawVolumeStrip(candles, gesture.viewport, volumePlot, style)
                    }

                    val crossX = gesture.crosshairX
                    if (style.showCrosshair && crossX != null) {
                        drawCrosshair(crossX, gesture.viewport, pricePlot, style)
                        val candle = nearestCandle(candles, crossX)
                        if (style.showTooltip && candle != null) {
                            val anchor = Offset(
                                mapX(candle.x, gesture.viewport, pricePlot),
                                mapY(candle.close, gesture.viewport, pricePlot),
                            )
                            drawTooltipBox(
                                title = candle.label.ifEmpty { "Bar ${candle.x.toInt()}" },
                                lines = listOf(
                                    "O ${formatPrice(candle.open)}",
                                    "H ${formatPrice(candle.high)}",
                                    "L ${formatPrice(candle.low)}",
                                    "C ${formatPrice(candle.close)}",
                                    "Vol ${formatVolume(candle.volume)}",
                                ),
                                anchor = anchor,
                                style = style,
                                textMeasurer = textMeasurer,
                            )
                        }
                    }
                }
            }
        },
    ) { measurables, constraints ->
        val titleMeasurable = if (title != null) measurables.first() else null
        val plotMeasurable = if (title != null) measurables[1] else measurables.first()
        val titlePlaceable = titleMeasurable?.measure(constraints.copy(minHeight = 0))
        val titleH = titlePlaceable?.height ?: 0
        val plotH = (constraints.maxHeight - titleH).coerceAtLeast(0)
        val plotPlaceable = plotMeasurable.measure(
            Constraints.fixed(constraints.maxWidth, plotH),
        )
        layout(constraints.maxWidth, titleH + plotH) {
            titlePlaceable?.placeRelative(0, 0)
            plotPlaceable.placeRelative(0, titleH)
        }
    }
}
