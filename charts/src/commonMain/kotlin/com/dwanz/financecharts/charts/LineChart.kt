package com.dwanz.financecharts.charts

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.dwanz.financecharts.model.ChartStyle
import com.dwanz.financecharts.model.LineSeries
import com.dwanz.financecharts.model.formatPrice
import com.dwanz.financecharts.primitives.ChartGestureState
import com.dwanz.financecharts.primitives.chartGestures
import com.dwanz.financecharts.primitives.drawAxesAndGrid
import com.dwanz.financecharts.primitives.drawChartBackground
import com.dwanz.financecharts.primitives.drawCrosshair
import com.dwanz.financecharts.primitives.drawLineSeries
import com.dwanz.financecharts.primitives.drawTooltipBox
import com.dwanz.financecharts.primitives.mapX
import com.dwanz.financecharts.primitives.mapY
import com.dwanz.financecharts.primitives.nearestPoint
import com.dwanz.financecharts.primitives.plotRect
import com.dwanz.financecharts.primitives.seriesColor
import com.dwanz.financecharts.primitives.viewportForSeries
import kotlin.math.abs

/**
 * Multi-series line/area chart with pan/zoom, crosshair, and animated entrance.
 */
@Composable
fun LineChart(
    series: List<LineSeries>,
    modifier: Modifier = Modifier,
    style: ChartStyle = ChartStyle(),
    title: String? = null,
) {
    require(series.isNotEmpty()) { "LineChart requires at least one series" }
    val bounds = remember(series) { viewportForSeries(series) }
    val gesture = remember(series) { ChartGestureState(bounds, bounds) }
    val progress = remember { Animatable(0f) }
    val textMeasurer = rememberTextMeasurer()
    val crosshairVisible = remember {
        MutableTransitionState(false)
    }.apply { targetState = gesture.crosshairX != null }
    val crosshairTransition = rememberTransition(crosshairVisible, label = "crosshair")
    val crosshairAlpha = crosshairTransition.animateFloat(
        transitionSpec = { tween(160) },
        label = "crosshairAlpha",
    ) { visible -> if (visible) 1f else 0f }

    LaunchedEffect(series) {
        gesture.reset(bounds)
        progress.snapTo(0f)
        progress.animateTo(1f, tween(900, easing = FastOutSlowInEasing))
    }

    val colors = series.mapIndexed { index, s -> seriesColor(index, style, s.color) }

    SubcomposeLayout(
        modifier = modifier
            .semantics { contentDescription = title ?: "Line chart" }
            .testTag("line_chart"),
    ) { constraints ->
        val legendPlaceables = if (style.showLegend) {
            subcompose("legend") {
                ChartLegend(series = series, colors = colors, style = style)
            }.map { it.measure(constraints.copy(minHeight = 0)) }
        } else {
            emptyList()
        }
        val legendHeight = legendPlaceables.maxOfOrNull { it.height } ?: 0
        val titlePlaceables = if (title != null) {
            subcompose("title") {
                Text(
                    text = title,
                    color = style.tokens.colors.tooltipText,
                    fontSize = style.tokens.typography.chartTitle,
                    modifier = Modifier
                        .padding(bottom = style.tokens.spacing.sm)
                        .testTag("line_chart_title"),
                )
            }.map { it.measure(constraints.copy(minHeight = 0)) }
        } else {
            emptyList()
        }
        val titleHeight = titlePlaceables.maxOfOrNull { it.height } ?: 0
        val plotHeight = (constraints.maxHeight - legendHeight - titleHeight).coerceAtLeast(0)
        val plotPlaceable = subcompose("plot") {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(plotHeight.toDp())
                    .chartGestures(gesture, style.enablePanZoom)
                    .testTag("line_chart_plot"),
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    val padL = style.tokens.spacing.plotPaddingStart.toPx()
                    val padT = style.tokens.spacing.plotPaddingTop.toPx()
                    val padR = style.tokens.spacing.plotPaddingEnd.toPx()
                    val padB = style.tokens.spacing.plotPaddingBottom.toPx()
                    val plot = plotRect(size, padL, padT, padR, padB)
                    gesture.plotBounds = plot

                    drawChartBackground(plot, style)
                    drawAxesAndGrid(plot, gesture.viewport, style, textMeasurer)

                    series.forEachIndexed { index, s ->
                        drawLineSeries(
                            series = s,
                            color = colors[index],
                            viewport = gesture.viewport,
                            plot = plot,
                            strokeWidth = style.lineStrokeWidth,
                            progress = progress.value,
                            showArea = s.showArea || (style.areaEnabledDefault && index == 0),
                            areaColor = style.tokens.colors.areaFill.copy(
                                alpha = if (index == 0) 0.28f else 0.12f,
                            ),
                        )
                    }

                    val crossX = gesture.crosshairX
                    if (style.showCrosshair && crossX != null && crosshairAlpha.value > 0.01f) {
                        // Transition-driven fade for crosshair presence
                        val faded = style.copy(
                            tokens = style.tokens.copy(
                                colors = style.tokens.colors.copy(
                                    crosshair = style.tokens.colors.crosshair.copy(
                                        alpha = style.tokens.colors.crosshair.alpha * crosshairAlpha.value,
                                    ),
                                    tooltipBackground = style.tokens.colors.tooltipBackground.copy(
                                        alpha = style.tokens.colors.tooltipBackground.alpha * crosshairAlpha.value,
                                    ),
                                ),
                            ),
                        )
                        drawCrosshair(crossX, gesture.viewport, plot, faded)
                        val nearest = nearestPoint(series, crossX)
                        if (style.showTooltip && nearest != null) {
                            val (_, p) = nearest
                            val anchor = Offset(
                                mapX(p.x, gesture.viewport, plot),
                                mapY(p.y, gesture.viewport, plot),
                            )
                            val lines = series.map { ser ->
                                val pt = ser.points.minByOrNull { abs(it.x - p.x) }
                                "${ser.name}: ${pt?.let { formatPrice(it.y) } ?: "--"}"
                            }
                            drawTooltipBox(
                                title = p.label.ifEmpty { "x=${formatPrice(p.x)}" },
                                lines = lines,
                                anchor = anchor,
                                style = faded,
                                textMeasurer = textMeasurer,
                            )
                        }
                    }
                }
            }
        }.first().measure(Constraints.fixed(constraints.maxWidth, plotHeight))

        layout(constraints.maxWidth, titleHeight + plotHeight + legendHeight) {
            var y = 0
            titlePlaceables.forEach {
                it.placeRelative(0, y)
                y += it.height
            }
            plotPlaceable.placeRelative(0, y)
            y += plotPlaceable.height
            legendPlaceables.forEach {
                it.placeRelative(0, y)
            }
        }
    }
}

@Composable
private fun ChartLegend(
    series: List<LineSeries>,
    colors: List<Color>,
    style: ChartStyle,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = style.tokens.spacing.md, vertical = style.tokens.spacing.sm)
            .testTag("line_chart_legend"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        series.forEachIndexed { index, s ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(style.tokens.shapes.legendDot)
                        .clip(CircleShape)
                        .background(colors[index]),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = s.name,
                    color = style.tokens.colors.label,
                    fontSize = style.tokens.typography.legend,
                )
            }
            Spacer(Modifier.width(style.tokens.spacing.lg))
        }
    }
}
