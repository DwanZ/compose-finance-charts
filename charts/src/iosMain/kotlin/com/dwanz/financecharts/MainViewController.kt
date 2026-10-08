package com.dwanz.financecharts

import androidx.compose.ui.window.ComposeUIViewController
import com.dwanz.financecharts.sample.FinanceChartsDemo
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController =
    ComposeUIViewController { FinanceChartsDemo() }
