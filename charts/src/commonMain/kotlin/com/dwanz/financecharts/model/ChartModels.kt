package com.dwanz.financecharts.model

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import com.dwanz.financecharts.theme.DefaultFinanceTokens
import com.dwanz.financecharts.theme.FinanceTokens

/** A single X/Y sample for line / area series. */
@Immutable
data class ChartPoint(
    val x: Float,
    val y: Float,
    val label: String = "",
)

/** OHLC bar with optional volume for candlestick charts. */
@Immutable
data class Candle(
    val x: Float,
    val open: Float,
    val high: Float,
    val low: Float,
    val close: Float,
    val volume: Float = 0f,
    val label: String = "",
) {
    val isBullish: Boolean get() = close >= open
}

@Immutable
data class LineSeries(
    val id: String,
    val name: String,
    val points: List<ChartPoint>,
    val color: Color? = null,
    val showArea: Boolean = false,
)

@Immutable
data class ChartViewport(
    val xMin: Float,
    val xMax: Float,
    val yMin: Float,
    val yMax: Float,
) {
    val xRange: Float get() = (xMax - xMin).coerceAtLeast(1e-6f)
    val yRange: Float get() = (yMax - yMin).coerceAtLeast(1e-6f)

    fun containsX(x: Float): Boolean = x in xMin..xMax
}

@Immutable
data class AxisSpec(
    val tickCount: Int = 5,
    val format: (Float) -> String = { value -> formatAxisValue(value) },
    val showGrid: Boolean = true,
)

@Immutable
data class ChartStyle(
    val tokens: FinanceTokens = DefaultFinanceTokens,
    val lineStrokeWidth: Float = 2.5f,
    val areaEnabledDefault: Boolean = true,
    val showLegend: Boolean = true,
    val showCrosshair: Boolean = true,
    val showTooltip: Boolean = true,
    val enablePanZoom: Boolean = true,
    val xAxis: AxisSpec = AxisSpec(),
    val yAxis: AxisSpec = AxisSpec(),
    val volumeHeightFraction: Float = 0.22f,
)

@Stable
interface ChartInteractionState {
    val viewport: ChartViewport
    val crosshairX: Float?
    val isInteracting: Boolean
}

@Immutable
data class TooltipPayload(
    val title: String,
    val lines: List<String>,
    val x: Float,
    val y: Float,
)

object SampleMarketData {
    fun lineSeries(
        days: Int = 60,
        startPrice: Float = 142f,
        seed: Int = 7,
    ): List<LineSeries> {
        val primary = mutableListOf<ChartPoint>()
        val secondary = mutableListOf<ChartPoint>()
        var price = startPrice
        var benchmark = startPrice * 0.97f
        var rng = seed
        fun next(): Float {
            rng = (rng * 1103515245 + 12345) and 0x7fffffff
            return (rng % 1000) / 1000f
        }
        repeat(days) { i ->
            val drift = (next() - 0.48f) * 2.4f
            price = (price + drift).coerceAtLeast(80f)
            benchmark = (benchmark + (next() - 0.5f) * 1.6f).coerceAtLeast(75f)
            primary += ChartPoint(i.toFloat(), price, "D$i")
            secondary += ChartPoint(i.toFloat(), benchmark, "D$i")
        }
        return listOf(
            LineSeries(
                id = "asset",
                name = "NVDA",
                points = primary,
                showArea = true,
            ),
            LineSeries(
                id = "bench",
                name = "QQQ",
                points = secondary,
                showArea = false,
            ),
        )
    }

    fun candles(days: Int = 40, start: Float = 180f, seed: Int = 11): List<Candle> {
        val out = mutableListOf<Candle>()
        var close = start
        var rng = seed
        fun next(): Float {
            rng = (rng * 1664525 + 1013904223) and 0x7fffffff
            return (rng % 1000) / 1000f
        }
        repeat(days) { i ->
            val open = close
            val move = (next() - 0.48f) * 6f
            close = (open + move).coerceAtLeast(50f)
            val high = maxOf(open, close) + next() * 2.5f
            val low = minOf(open, close) - next() * 2.5f
            val volume = 800_000f + next() * 2_200_000f
            out += Candle(
                x = i.toFloat(),
                open = open,
                high = high,
                low = low.coerceAtLeast(1f),
                close = close,
                volume = volume,
                label = "D$i",
            )
        }
        return out
    }
}
