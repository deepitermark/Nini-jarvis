package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AuraCyan
import com.example.ui.theme.AuraMagenta
import com.example.ui.theme.AuraPurple
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NovaDarkBackground
import com.example.ui.theme.NovaSurfaceCard
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun PhysicalPowerButtonWidget(
    chargingProgress: Float,
    isAssistantActive: Boolean,
    onPressStart: () -> Unit,
    onPressRelease: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressedVisual by remember { mutableStateOf(false) }

    val animatedProgress by animateFloatAsState(
        targetValue = chargingProgress,
        animationSpec = tween(durationMillis = 40),
        label = "power_charge_ring"
    )

    val buttonScale by animateFloatAsState(
        targetValue = if (isPressedVisual) 0.90f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "power_btn_scale"
    )

    Row(
        modifier = modifier
            .testTag("physical_power_button_container"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        // Explanatory badge
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.padding(end = 10.dp)
        ) {
            Text(
                text = "PHYSICAL POWER KEY",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = AuraCyan
                )
            )
            Text(
                text = if (chargingProgress > 0f) "Charging Nova... ${(chargingProgress * 100).toInt()}%"
                else if (isAssistantActive) "2x Tap: Dismiss | 1x Tap: Sleep"
                else "Hold >0.6s: Nova | 1x Tap: Sleep",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 10.sp,
                    color = if (chargingProgress > 0f) AuraMagenta else TextSecondary
                )
            )
        }

        // Circular physical hardware power button with radial charging ring
        Box(
            modifier = Modifier
                .size(68.dp),
            contentAlignment = Alignment.Center
        ) {
            // Background track for radial charging ring
            Canvas(modifier = Modifier.size(64.dp)) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.08f),
                    radius = size.minDimension / 2 - 3.dp.toPx(),
                    style = Stroke(width = 3.dp.toPx())
                )

                // Radial Charging Progress Ring
                if (animatedProgress > 0.01f) {
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(AuraCyan, AuraPurple, AuraMagenta, AuraCyan)
                        ),
                        startAngle = -90f,
                        sweepAngle = animatedProgress * 360f,
                        useCenter = false,
                        style = Stroke(width = 4.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }

            // Tactile Metallic Power Button Center
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .scale(buttonScale)
                    .shadow(
                        elevation = if (isPressedVisual) 2.dp else 8.dp,
                        shape = CircleShape,
                        spotColor = if (chargingProgress > 0f) AuraMagenta else AuraCyan
                    )
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = if (chargingProgress > 0f) {
                                listOf(AuraPurple, NovaSurfaceCard)
                            } else {
                                listOf(NovaSurfaceCard, NovaDarkBackground)
                            }
                        )
                    )
                    .border(
                        width = 1.5.dp,
                        brush = Brush.linearGradient(
                            if (chargingProgress > 0f) listOf(AuraCyan, AuraMagenta)
                            else listOf(Color.White.copy(alpha = 0.35f), Color.White.copy(alpha = 0.08f))
                        ),
                        shape = CircleShape
                    )
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                isPressedVisual = true
                                onPressStart()
                                tryAwaitRelease()
                                isPressedVisual = false
                                onPressRelease()
                            }
                        )
                    }
                    .testTag("physical_power_button_touch"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PowerSettingsNew,
                    contentDescription = "Physical Power Button",
                    tint = if (chargingProgress > 0f) AuraCyan else if (isAssistantActive) AuraMagenta else TextPrimary,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}
