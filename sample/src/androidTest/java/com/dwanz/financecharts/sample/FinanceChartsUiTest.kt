package com.dwanz.financecharts.sample

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.click
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FinanceChartsUiTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun demoShowsBrandAndBothCharts() {
        composeRule.onNodeWithTag("demo_brand").assertIsDisplayed()
        composeRule.onNodeWithTag("finance_demo_root").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("NVDA vs QQQ").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Daily candles + volume").assertIsDisplayed()
        composeRule.onNodeWithText("NVDA").assertExists()
        composeRule.onNodeWithText("QQQ").assertExists()
    }

    @Test
    fun lineChartPlotAcceptsPressForCrosshairSemantics() {
        composeRule.onNodeWithTag("line_chart_plot").assertIsDisplayed()
        composeRule.onNodeWithTag("line_chart_plot").performTouchInput {
            click(center)
        }
        // Chart remains interactive and tagged after gesture
        composeRule.onNodeWithTag("line_chart").assertIsDisplayed()
        composeRule.onNodeWithTag("line_chart_legend").assertIsDisplayed()
    }
}
