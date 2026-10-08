package com.dwanz.financecharts.sample

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dwanz.financecharts.charts.CandlestickChart
import com.dwanz.financecharts.charts.LineChart
import com.dwanz.financecharts.model.ChartStyle
import com.dwanz.financecharts.model.SampleMarketData
import com.dwanz.financecharts.theme.DefaultFinanceTokens

/**
 * Shared demo surface used by the Android sample and the iOS host.
 */
@Composable
fun FinanceChartsDemo(modifier: Modifier = Modifier) {
    val tokens = DefaultFinanceTokens
    val lineSeries = remember { SampleMarketData.lineSeries() }
    val candles = remember { SampleMarketData.candles() }
    var ready by remember { mutableStateOf(false) }
    val fade by animateFloatAsState(
        targetValue = if (ready) 1f else 0f,
        animationSpec = tween(700),
        label = "demoFade",
    )
    androidx.compose.runtime.LaunchedEffect(Unit) { ready = true }

    MaterialTheme(
        colorScheme = darkColorScheme(
            background = tokens.colors.background,
            surface = tokens.colors.surface,
            primary = tokens.colors.seriesPrimary,
            onBackground = tokens.colors.tooltipText,
            onSurface = tokens.colors.label,
        ),
    ) {
        Surface(
            modifier = modifier
                .fillMaxSize()
                .testTag("finance_demo_root"),
            color = Color.Transparent,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF0B1218),
                                Color(0xFF15202B),
                                Color(0xFF0F1419),
                            ),
                        ),
                    )
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 20.dp)
                    .graphicsLayer { alpha = fade },
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Column {
                    Text(
                        text = "Finance Charts",
                        color = tokens.colors.seriesPrimary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.testTag("demo_brand"),
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Interactive CMP line & candlestick charts",
                        color = tokens.colors.labelMuted,
                        fontSize = 14.sp,
                    )
                }

                ChartCard(label = "Multi-series performance") {
                    LineChart(
                        series = lineSeries,
                        style = ChartStyle(),
                        title = "NVDA vs QQQ",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp),
                    )
                }

                ChartCard(label = "OHLC session") {
                    CandlestickChart(
                        candles = candles,
                        style = ChartStyle(),
                        title = "Daily candles + volume",
                        showVolume = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(320.dp),
                    )
                }

                Text(
                    text = "Press for crosshair · pinch to zoom · drag to pan",
                    color = tokens.colors.labelMuted,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 24.dp),
                )
            }
        }
    }
}

@Composable
private fun ChartCard(label: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1A222C))
            .padding(12.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = label, color = Color(0xFFB8C4CE), fontSize = 13.sp)
        }
        content()
    }
}
