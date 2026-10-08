package com.dwanz.financecharts.model

import kotlin.math.abs
import kotlin.math.round

internal fun formatAxisValue(value: Float): String {
    return when {
        abs(value) >= 1_000_000f -> "${trimDecimals(value / 1_000_000f, 1)}M"
        abs(value) >= 1_000f -> "${trimDecimals(value / 1_000f, 1)}k"
        abs(value) >= 100f -> trimDecimals(value, 0)
        else -> trimDecimals(value, 2)
    }
}

internal fun formatPrice(value: Float): String = trimDecimals(value, 2)

internal fun formatVolume(value: Float): String {
    return when {
        value >= 1_000_000f -> "${trimDecimals(value / 1_000_000f, 2)}M"
        value >= 1_000f -> "${trimDecimals(value / 1_000f, 1)}k"
        else -> trimDecimals(value, 0)
    }
}

private fun trimDecimals(value: Float, decimals: Int): String {
    if (decimals <= 0) return round(value).toInt().toString()
    var factor = 1f
    repeat(decimals) { factor *= 10f }
    val scaled = round(value * factor) / factor
    val text = scaled.toString()
    val dot = text.indexOf('.')
    if (dot < 0) return text
    val end = (dot + 1 + decimals).coerceAtMost(text.length)
    return text.substring(0, end).trimEnd('0').trimEnd('.')
}
