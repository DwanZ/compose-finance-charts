package com.dwanz.financecharts.primitives

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import com.dwanz.financecharts.model.ChartInteractionState
import com.dwanz.financecharts.model.ChartViewport
import kotlin.math.abs

@Stable
class ChartGestureState(
    initial: ChartViewport,
    private val bounds: ChartViewport,
) : ChartInteractionState {
    override var viewport by mutableStateOf(initial)
        private set
    override var crosshairX by mutableStateOf<Float?>(null)
        private set
    override var isInteracting by mutableStateOf(false)
        private set

    var plotBounds: Rect = Rect.Zero

    fun reset(viewport: ChartViewport) {
        this.viewport = viewport.clampTo(bounds)
        crosshairX = null
        isInteracting = false
    }

    fun updateFromGesture(
        pan: Offset,
        zoom: Float,
        centroid: Offset,
        pressing: Boolean,
        pointerCount: Int,
    ) {
        if (plotBounds.width <= 0f) return
        if (pointerCount >= 2 || abs(zoom - 1f) > 0.01f || abs(pan.x) > 0.5f) {
            val anchorX = unmapX(centroid.x, viewport, plotBounds)
            var next = viewport
            if (abs(zoom - 1f) > 0.01f) {
                next = next.zoom(1f / zoom, anchorX)
            }
            if (abs(pan.x) > 0.5f) {
                val domainDelta = -pan.x / plotBounds.width * next.xRange
                next = next.pan(domainDelta)
            }
            viewport = next.clampTo(bounds)
            crosshairX = null
            isInteracting = true
        } else if (pressing) {
            crosshairX = unmapX(centroid.x, viewport, plotBounds)
            isInteracting = true
        }
    }

    fun endGesture() {
        isInteracting = false
    }

    fun setCrosshair(x: Float?) {
        crosshairX = x
    }
}

fun Modifier.chartGestures(
    state: ChartGestureState,
    enabled: Boolean,
): Modifier {
    if (!enabled) return this
    return this.pointerInput(state) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            state.updateFromGesture(
                pan = Offset.Zero,
                zoom = 1f,
                centroid = down.position,
                pressing = true,
                pointerCount = 1,
            )
            do {
                val event = awaitPointerEvent()
                val pressed = event.changes.any { it.pressed }
                val zoom = event.calculateZoom()
                val pan = event.calculatePan()
                val centroid = event.calculateCentroid(useCurrent = true)
                if (event.changes.any { it.positionChanged() }) {
                    state.updateFromGesture(
                        pan = pan,
                        zoom = zoom,
                        centroid = centroid,
                        pressing = pressed,
                        pointerCount = event.changes.count { it.pressed },
                    )
                    event.changes.forEach { it.consume() }
                }
                if (!pressed) {
                    state.endGesture()
                }
            } while (event.changes.any { it.pressed })
            state.endGesture()
        }
    }
}
