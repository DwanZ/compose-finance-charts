package com.dwanz.financecharts

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.click
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dwanz.financecharts.charts.LineChart
import com.dwanz.financecharts.model.SampleMarketData
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LineChartSemanticsTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun lineChartExposesSemanticsAndRespondsToPress() {
        composeRule.setContent {
            LineChart(
                series = SampleMarketData.lineSeries(days = 20),
                title = "Test line",
                modifier = Modifier.fillMaxSize(),
            )
        }

        composeRule.onNodeWithContentDescription("Test line").assertIsDisplayed()
        composeRule.onNodeWithTag("line_chart_plot").assertIsDisplayed()
        composeRule.onNodeWithTag("line_chart_plot").performTouchInput {
            click(center)
        }
        composeRule.onNodeWithTag("line_chart_legend").assertIsDisplayed()
    }
}
