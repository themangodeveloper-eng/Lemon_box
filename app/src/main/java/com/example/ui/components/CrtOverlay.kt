package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun CrtScanlineOverlay(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    scanlineAlpha: Float = 0.07f,
    tintColor: Color = Color.Black
) {
    if (!enabled) return

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val step = 4.dp.toPx()

        // Draw scanlines
        var y = 0f
        while (y < height) {
            drawLine(
                color = tintColor.copy(alpha = scanlineAlpha),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1.5f
            )
            y += step
        }

        // CRT vignette around borders
        val vignette = Brush.radialGradient(
            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.28f)),
            center = Offset(width / 2f, height / 2f),
            radius = (width.coerceAtLeast(height)) * 0.75f
        )
        drawRect(brush = vignette)
    }
}
