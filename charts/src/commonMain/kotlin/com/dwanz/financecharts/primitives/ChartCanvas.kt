package com.dwanz.financecharts.primitives

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import com.dwanz.financecharts.model.AxisSpec
import com.dwanz.financecharts.model.Candle
import com.dwanz.financecharts.model.ChartStyle
import com.dwanz.financecharts.model.ChartViewport
import com.dwanz.financecharts.model.LineSeries

fun DrawScope.drawChartBackground(plot: Rect, style: ChartStyle) {
    drawRect(style.tokens.colors.surface, topLeft = Offset.Zero, size = size)
    drawRect(style.tokens.colors.surfaceElevated, topLeft = plot.topLeft, size = plot.size)
}

fun DrawScope.drawAxesAndGrid(
    plot: Rect,
    viewport: ChartViewport,
    style: ChartStyle,
    textMeasurer: TextMeasurer,
    xAxis: AxisSpec = style.xAxis,
    yAxis: AxisSpec = style.yAxis,
) {
    val yTicks = niceTicks(viewport.yMin, viewport.yMax, yAxis.tickCount)
    val xTicks = niceTicks(viewport.xMin, viewport.xMax, xAxis.tickCount)
    val labelStyle = TextStyle(
        color = style.tokens.colors.label,
        fontSize = style.tokens.typography.axisLabel,
    )

    yTicks.forEach { y ->
        val py = mapY(y, viewport, plot)
        if (yAxis.showGrid) {
            drawLine(
                color = style.tokens.colors.grid,
                start = Offset(plot.left, py),
                end = Offset(plot.right, py),
                strokeWidth = 1f,
            )
        }
        val label = yAxis.format(y)
        val layout = textMeasurer.measure(label, labelStyle)
        drawText(
            textLayoutResult = layout,
            topLeft = Offset(
                (plot.left - layout.size.width - 6f).coerceAtLeast(0f),
                py - layout.size.height / 2f,
            ),
        )
    }

    xTicks.forEach { x ->
        val px = mapX(x, viewport, plot)
        if (xAxis.showGrid) {
            drawLine(
                color = style.tokens.colors.grid,
                start = Offset(px, plot.top),
                end = Offset(px, plot.bottom),
                strokeWidth = 1f,
            )
        }
        val label = xAxis.format(x)
        val layout = textMeasurer.measure(label, labelStyle)
        drawText(
            textLayoutResult = layout,
            topLeft = Offset(
                px - layout.size.width / 2f,
                plot.bottom + 6f,
            ),
        )
    }

    drawLine(style.tokens.colors.axis, Offset(plot.left, plot.top), Offset(plot.left, plot.bottom), 1.5f)
    drawLine(style.tokens.colors.axis, Offset(plot.left, plot.bottom), Offset(plot.right, plot.bottom), 1.5f)
}

fun DrawScope.drawLineSeries(
    series: LineSeries,
    color: Color,
    viewport: ChartViewport,
    plot: Rect,
    strokeWidth: Float,
    progress: Float,
    showArea: Boolean,
    areaColor: Color,
) {
    val pts = series.points.filter { it.x in viewport.xMin - viewport.xRange * 0.05f..viewport.xMax + viewport.xRange * 0.05f }
    if (pts.size < 2) return
    val visibleCount = (pts.size * progress.coerceIn(0f, 1f)).toInt().coerceAtLeast(2)
    val slice = pts.take(visibleCount)
    val path = Path()
    val first = slice.first().toOffset(viewport, plot)
    path.moveTo(first.x, first.y)
    for (i in 1 until slice.size) {
        val o = slice[i].toOffset(viewport, plot)
        path.lineTo(o.x, o.y)
    }
    if (showArea) {
        val area = Path().apply {
            addPath(path)
            lineTo(slice.last().toOffset(viewport, plot).x, plot.bottom)
            lineTo(first.x, plot.bottom)
            close()
        }
        drawPath(area, areaColor)
    }
    drawPath(path, color, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
}

fun DrawScope.drawCandles(
    candles: List<Candle>,
    viewport: ChartViewport,
    plot: Rect,
    style: ChartStyle,
    progress: Float,
) {
    val visible = candles.filter { it.x in viewport.xMin..viewport.xMax }
    if (visible.isEmpty()) return
    val count = (visible.size * progress.coerceIn(0f, 1f)).toInt().coerceAtLeast(1)
    val gap = style.tokens.shapes.candleGapFraction
    val stepPx = plot.width / viewport.xRange
    val bodyWidth = (stepPx * (1f - gap)).coerceAtLeast(style.tokens.shapes.candleBodyMinWidth.toPx())

    visible.take(count).forEach { c ->
        val cx = mapX(c.x, viewport, plot)
        val yHigh = mapY(c.high, viewport, plot)
        val yLow = mapY(c.low, viewport, plot)
        val yOpen = mapY(c.open, viewport, plot)
        val yClose = mapY(c.close, viewport, plot)
        val color = if (c.isBullish) style.tokens.colors.candleUp else style.tokens.colors.candleDown
        drawLine(
            color = style.tokens.colors.wick,
            start = Offset(cx, yHigh),
            end = Offset(cx, yLow),
            strokeWidth = 1.5f,
        )
        val top = minOf(yOpen, yClose)
        val bottom = maxOf(yOpen, yClose)
        val height = (bottom - top).coerceAtLeast(1.5f)
        drawRect(
            color = color,
            topLeft = Offset(cx - bodyWidth / 2f, top),
            size = Size(bodyWidth, height),
        )
    }
}

fun DrawScope.drawVolumeStrip(
    candles: List<Candle>,
    viewport: ChartViewport,
    volumePlot: Rect,
    style: ChartStyle,
) {
    val visible = candles.filter { it.x in viewport.xMin..viewport.xMax && it.volume > 0f }
    if (visible.isEmpty()) return
    val maxVol = visible.maxOf { it.volume }.coerceAtLeast(1f)
    val stepPx = volumePlot.width / viewport.xRange
    val barWidth = (stepPx * 0.7f).coerceAtLeast(1f)
    visible.forEach { c ->
        val cx = mapX(c.x, viewport, volumePlot)
        val h = (c.volume / maxVol) * volumePlot.height
        val color = if (c.isBullish) style.tokens.colors.candleUp.copy(alpha = 0.35f)
        else style.tokens.colors.candleDown.copy(alpha = 0.35f)
        drawRect(
            color = color,
            topLeft = Offset(cx - barWidth / 2f, volumePlot.bottom - h),
            size = Size(barWidth, h),
        )
    }
}

fun DrawScope.drawCrosshair(
    x: Float,
    viewport: ChartViewport,
    plot: Rect,
    style: ChartStyle,
) {
    val px = mapX(x, viewport, plot)
    if (px !in plot.left..plot.right) return
    drawLine(
        color = style.tokens.colors.crosshair,
        start = Offset(px, plot.top),
        end = Offset(px, plot.bottom),
        strokeWidth = 1.2f,
    )
}

fun seriesColor(index: Int, style: ChartStyle, explicit: Color?): Color {
    if (explicit != null) return explicit
    val colors = listOf(
        style.tokens.colors.seriesPrimary,
        style.tokens.colors.seriesSecondary,
        style.tokens.colors.seriesTertiary,
    )
    return colors[index % colors.size]
}

fun DrawScope.drawTooltipBox(
    title: String,
    lines: List<String>,
    anchor: Offset,
    style: ChartStyle,
    textMeasurer: TextMeasurer,
) {
    val titleStyle = TextStyle(color = style.tokens.colors.tooltipText, fontSize = style.tokens.typography.tooltipTitle)
    val bodyStyle = TextStyle(color = style.tokens.colors.label, fontSize = style.tokens.typography.tooltipBody)
    val titleLayout = textMeasurer.measure(title, titleStyle)
    val bodyLayouts = lines.map { textMeasurer.measure(it, bodyStyle) }
    val pad = 10f
    val width = maxOf(titleLayout.size.width, bodyLayouts.maxOfOrNull { it.size.width } ?: 0) + pad * 2
    val height = titleLayout.size.height + bodyLayouts.fold(0) { acc, layout ->
        acc + layout.size.height + 4
    } + pad * 2
    var left = anchor.x + 12f
    var top = anchor.y - height - 8f
    if (left + width > size.width) left = anchor.x - width - 12f
    if (top < 0f) top = anchor.y + 12f
    val rect = Rect(left, top, left + width, top + height)
    drawRoundRect(
        color = style.tokens.colors.tooltipBackground,
        topLeft = rect.topLeft,
        size = rect.size,
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(
            style.tokens.shapes.tooltipCorner.toPx(),
            style.tokens.shapes.tooltipCorner.toPx(),
        ),
    )
    drawRoundRect(
        color = style.tokens.colors.tooltipBorder,
        topLeft = rect.topLeft,
        size = rect.size,
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(
            style.tokens.shapes.tooltipCorner.toPx(),
            style.tokens.shapes.tooltipCorner.toPx(),
        ),
        style = Stroke(width = 1.2f),
    )
    var y = top + pad
    drawText(titleLayout, topLeft = Offset(left + pad, y))
    y += titleLayout.size.height + 4f
    bodyLayouts.forEach {
        drawText(it, topLeft = Offset(left + pad, y))
        y += it.size.height + 4f
    }
}
