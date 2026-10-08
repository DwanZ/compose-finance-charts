package com.dwanz.financecharts.primitives

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import com.dwanz.financecharts.model.Candle
import com.dwanz.financecharts.model.ChartPoint
import com.dwanz.financecharts.model.ChartViewport
import com.dwanz.financecharts.model.LineSeries
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

fun viewportForSeries(
    series: List<LineSeries>,
    yPaddingFraction: Float = 0.08f,
): ChartViewport {
    val points = series.flatMap { it.points }
    require(points.isNotEmpty()) { "Line series must contain points" }
    val xMin = points.minOf { it.x }
    val xMax = points.maxOf { it.x }
    val yMinRaw = points.minOf { it.y }
    val yMaxRaw = points.maxOf { it.y }
    val pad = ((yMaxRaw - yMinRaw).coerceAtLeast(1f)) * yPaddingFraction
    return ChartViewport(xMin, xMax.coerceAtLeast(xMin + 1f), yMinRaw - pad, yMaxRaw + pad)
}

fun viewportForCandles(
    candles: List<Candle>,
    yPaddingFraction: Float = 0.08f,
): ChartViewport {
    require(candles.isNotEmpty()) { "Candles must not be empty" }
    val xMin = candles.minOf { it.x } - 0.5f
    val xMax = candles.maxOf { it.x } + 0.5f
    val yMinRaw = candles.minOf { it.low }
    val yMaxRaw = candles.maxOf { it.high }
    val pad = ((yMaxRaw - yMinRaw).coerceAtLeast(1f)) * yPaddingFraction
    return ChartViewport(xMin, xMax, yMinRaw - pad, yMaxRaw + pad)
}

fun ChartViewport.zoom(factor: Float, anchorX: Float): ChartViewport {
    val f = factor.coerceIn(0.25f, 8f)
    val newRange = xRange * f
    val t = ((anchorX - xMin) / xRange).coerceIn(0f, 1f)
    var newMin = anchorX - newRange * t
    var newMax = anchorX + newRange * (1f - t)
    if (newMax - newMin < 1f) {
        val mid = (newMin + newMax) / 2f
        newMin = mid - 0.5f
        newMax = mid + 0.5f
    }
    return copy(xMin = newMin, xMax = newMax)
}

fun ChartViewport.pan(deltaXDomain: Float): ChartViewport {
    return copy(xMin = xMin + deltaXDomain, xMax = xMax + deltaXDomain)
}

fun ChartViewport.clampTo(bounds: ChartViewport, minVisibleFraction: Float = 0.15f): ChartViewport {
    val minWidth = bounds.xRange * minVisibleFraction
    var width = xRange.coerceAtLeast(minWidth).coerceAtMost(bounds.xRange)
    var min = xMin
    var max = min + width
    if (min < bounds.xMin) {
        min = bounds.xMin
        max = min + width
    }
    if (max > bounds.xMax) {
        max = bounds.xMax
        min = max - width
    }
    val yMin = yMin.coerceAtMost(bounds.yMin)
    val yMax = yMax.coerceAtLeast(bounds.yMax)
    return ChartViewport(min, max, yMin, yMax)
}

fun mapX(x: Float, viewport: ChartViewport, plot: Rect): Float {
    return plot.left + ((x - viewport.xMin) / viewport.xRange) * plot.width
}

fun mapY(y: Float, viewport: ChartViewport, plot: Rect): Float {
    return plot.bottom - ((y - viewport.yMin) / viewport.yRange) * plot.height
}

fun unmapX(px: Float, viewport: ChartViewport, plot: Rect): Float {
    val t = ((px - plot.left) / plot.width.coerceAtLeast(1f)).coerceIn(0f, 1f)
    return viewport.xMin + t * viewport.xRange
}

fun ChartPoint.toOffset(viewport: ChartViewport, plot: Rect): Offset =
    Offset(mapX(x, viewport, plot), mapY(y, viewport, plot))

fun niceTicks(min: Float, max: Float, count: Int): List<Float> {
    if (count <= 0 || max <= min) return listOf(min, max)
    val range = niceNum(max - min, round = false)
    val step = niceNum(range / (count - 1).coerceAtLeast(1), round = true)
    if (step <= 0f) return listOf(min, max)
    val niceMin = floor(min / step) * step
    val niceMax = ceil(max / step) * step
    val ticks = mutableListOf<Float>()
    var v = niceMin
    var guard = 0
    while (v <= niceMax + step * 0.5f && guard < 50) {
        if (v >= min - step * 0.01f && v <= max + step * 0.01f) {
            ticks += v
        }
        v += step
        guard++
    }
    return ticks.ifEmpty { listOf(min, max) }
}

private fun niceNum(range: Float, round: Boolean): Float {
    if (range <= 0f) return 1f
    val exponent = floor(log10(range.toDouble())).toInt()
    val fraction = range / 10f.pow(exponent)
    val niceFraction = if (round) {
        when {
            fraction < 1.5f -> 1f
            fraction < 3f -> 2f
            fraction < 7f -> 5f
            else -> 10f
        }
    } else {
        when {
            fraction <= 1f -> 1f
            fraction <= 2f -> 2f
            fraction <= 5f -> 5f
            else -> 10f
        }
    }
    return niceFraction * 10f.pow(exponent)
}

fun plotRect(
    size: Size,
    paddingLeft: Float,
    paddingTop: Float,
    paddingRight: Float,
    paddingBottom: Float,
): Rect {
    return Rect(
        left = paddingLeft,
        top = paddingTop,
        right = (size.width - paddingRight).coerceAtLeast(paddingLeft + 1f),
        bottom = (size.height - paddingBottom).coerceAtLeast(paddingTop + 1f),
    )
}

fun nearestPoint(series: List<LineSeries>, x: Float): Pair<LineSeries, ChartPoint>? {
    var best: Pair<LineSeries, ChartPoint>? = null
    var bestDist = Float.MAX_VALUE
    series.forEach { s ->
        s.points.forEach { p ->
            val d = kotlin.math.abs(p.x - x)
            if (d < bestDist) {
                bestDist = d
                best = s to p
            }
        }
    }
    return best
}

fun nearestCandle(candles: List<Candle>, x: Float): Candle? {
    return candles.minByOrNull { kotlin.math.abs(it.x - x) }
}
