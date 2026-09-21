package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AuraAmber
import com.example.ui.theme.AuraCyan
import com.example.ui.theme.AuraMagenta
import com.example.ui.theme.AuraPurple
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun AmbientAuraWave(
    modifier: Modifier = Modifier,
    isActive: Boolean = true,
    audioRms: Float = 0f,
    height: Dp = 48.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "aura_wave")
    val phase1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase1"
    )
    val phase2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase2"
    )
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        if (!isActive) return@Canvas

        val width = size.width
        val canvasHeight = size.height
        val centerY = canvasHeight * 0.7f

        // Amplitude expands based on voice audio rms
        val activeMultiplier = if (audioRms > 0.05f) 1f + audioRms * 2.8f else 0.45f
        val baseAmp1 = (canvasHeight * 0.35f) * activeMultiplier
        val baseAmp2 = (canvasHeight * 0.28f) * activeMultiplier

        // 4-Color Gradient: Cyan, Purple, Amber, Magenta
        val auraGradient = Brush.horizontalGradient(
            colors = listOf(
                AuraCyan.copy(alpha = 0.85f * glowPulse),
                AuraPurple.copy(alpha = 0.9f * glowPulse),
                AuraAmber.copy(alpha = 0.85f * glowPulse),
                AuraMagenta.copy(alpha = 0.9f * glowPulse),
                AuraCyan.copy(alpha = 0.85f * glowPulse)
            )
        )

        val auraSoftGradient = Brush.horizontalGradient(
            colors = listOf(
                AuraMagenta.copy(alpha = 0.4f * glowPulse),
                AuraCyan.copy(alpha = 0.45f * glowPulse),
                AuraPurple.copy(alpha = 0.45f * glowPulse),
                AuraAmber.copy(alpha = 0.4f * glowPulse)
            )
        )

        // Draw Layer 1: Background Soft Glow Wave
        drawWavePath(
            width = width,
            centerY = centerY,
            amplitude = baseAmp1 * 1.15f,
            wavelength = width * 0.75f,
            phase = phase2,
            brush = auraSoftGradient,
            fillBottom = true,
            canvasHeight = canvasHeight
        )

        // Draw Layer 2: Foreground Crisp Vibrant Wave
        drawWavePath(
            width = width,
            centerY = centerY,
            amplitude = baseAmp2,
            wavelength = width * 0.55f,
            phase = phase1,
            brush = auraGradient,
            fillBottom = true,
            canvasHeight = canvasHeight
        )

        // Ambient bottom glow bar
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    AuraCyan.copy(alpha = 0.35f * glowPulse),
                    AuraPurple.copy(alpha = 0.6f * glowPulse)
                ),
                startY = canvasHeight * 0.4f,
                endY = canvasHeight
            ),
            topLeft = Offset(0f, canvasHeight * 0.4f),
            size = androidx.compose.ui.geometry.Size(width, canvasHeight * 0.6f)
        )
    }
}

private fun DrawScope.drawWavePath(
    width: Float,
    centerY: Float,
    amplitude: Float,
    wavelength: Float,
    phase: Float,
    brush: Brush,
    fillBottom: Boolean,
    canvasHeight: Float
) {
    val path = Path()
    path.moveTo(0f, centerY)

    val step = 8f
    var x = 0f
    while (x <= width) {
        val normalizedX = (x / wavelength) * (2 * PI).toFloat()
        // Compound wave with two harmonics for organic fluidity
        val y = centerY + amplitude * (sin(normalizedX + phase) * 0.7f + sin(normalizedX * 1.6f - phase) * 0.3f)
        path.lineTo(x, y)
        x += step
    }

    if (fillBottom) {
        path.lineTo(width, canvasHeight)
        path.lineTo(0f, canvasHeight)
        path.close()
        drawPath(path = path, brush = brush)
    }
}
