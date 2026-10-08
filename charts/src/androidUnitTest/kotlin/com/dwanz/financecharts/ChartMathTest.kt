package com.dwanz.financecharts

import com.dwanz.financecharts.model.SampleMarketData
import com.dwanz.financecharts.primitives.clampTo
import com.dwanz.financecharts.primitives.niceTicks
import com.dwanz.financecharts.primitives.viewportForCandles
import com.dwanz.financecharts.primitives.viewportForSeries
import com.dwanz.financecharts.primitives.zoom
import kotlin.test.Test
import kotlin.test.assertTrue

class ChartMathTest {
    @Test
    fun viewportForLineSeriesHasPositiveRanges() {
        val vp = viewportForSeries(SampleMarketData.lineSeries(days = 10))
        assertTrue(vp.xRange > 0f)
        assertTrue(vp.yRange > 0f)
    }

    @Test
    fun candleViewportContainsHighLow() {
        val candles = SampleMarketData.candles(days = 12)
        val vp = viewportForCandles(candles)
        assertTrue(vp.yMin <= candles.minOf { it.low })
        assertTrue(vp.yMax >= candles.maxOf { it.high })
    }

    @Test
    fun zoomThenClampStaysWithinBounds() {
        val bounds = viewportForSeries(SampleMarketData.lineSeries(days = 30))
        val zoomed = bounds.zoom(0.5f, bounds.xMin + bounds.xRange / 2f).clampTo(bounds)
        assertTrue(zoomed.xMin >= bounds.xMin - 0.001f)
        assertTrue(zoomed.xMax <= bounds.xMax + 0.001f)
    }

    @Test
    fun niceTicksReturnsValuesInsideRange() {
        val ticks = niceTicks(10f, 50f, 5)
        assertTrue(ticks.isNotEmpty())
        assertTrue(ticks.first() <= 10f + 1f)
        assertTrue(ticks.last() >= 50f - 1f)
    }
}
