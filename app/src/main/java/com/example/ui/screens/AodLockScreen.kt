package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BatteryInfo
import com.example.ui.components.PhysicalPowerButtonWidget
import com.example.ui.theme.AuraCyan
import com.example.ui.theme.AuraMagenta
import com.example.ui.theme.AuraPurple
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.OledClockSubtext
import com.example.ui.theme.OledClockWhite
import com.example.ui.theme.OledPureBlack
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AodLockScreen(
    batteryInfo: BatteryInfo,
    isFlashlightOn: Boolean,
    powerChargingProgress: Float,
    onPowerButtonDown: () -> Unit,
    onPowerButtonUp: () -> Unit,
    onToggleFlashlight: () -> Unit,
    onLaunchCamera: () -> Unit,
    onUnlock: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentTimeString by remember { mutableStateOf("") }
    var currentDateHindi by remember { mutableStateOf("") }
    var currentDateEnglish by remember { mutableStateOf("") }

    // Live clock ticker
    LaunchedEffect(Unit) {
        while (true) {
            val now = Date()
            currentTimeString = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now)
            currentDateEnglish = SimpleDateFormat("EEEE, MMMM d", Locale.ENGLISH).format(now)
            currentDateHindi = SimpleDateFormat("EEEE, d MMMM", Locale("hi", "IN")).format(now)
            delay(1000L)
        }
    }

    // Subtle OLED pulse for time separator
    val infiniteTransition = rememberInfiniteTransition(label = "aod_pulse")
    val ambientGlow by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ambient_glow"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(OledPureBlack)
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        onUnlock()
                    },
                    onTap = {
                        // Single tap on black OLED screen shows wake hint
                    }
                )
            }
            .testTag("aod_lock_screen")
    ) {
        // Physical Side Power Button accessible on AOD as well!
        PhysicalPowerButtonWidget(
            chargingProgress = powerChargingProgress,
            isAssistantActive = false,
            onPressStart = onPowerButtonDown,
            onPressRelease = onPowerButtonUp,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Status Bar: Lock Icon & Battery Telemetry
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Device Locked",
                        tint = OledClockSubtext,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "OLED AOD",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            color = OledClockSubtext,
                            letterSpacing = 1.sp
                        )
                    )
                }

                // Battery Telemetry
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (batteryInfo.isCharging) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Charging",
                            tint = NeonGreen,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = "${batteryInfo.percentage}%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (batteryInfo.isCharging) NeonGreen else OledClockSubtext
                        )
                    )
                }
            }

            // Center: OLED Always-On Clock & Date in Hindi/English
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = currentTimeString.ifEmpty { "12:00" },
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 72.sp,
                        fontWeight = FontWeight.ExtraLight,
                        color = OledClockWhite.copy(alpha = ambientGlow),
                        fontFamily = FontFamily.SansSerif,
                        letterSpacing = (-2).sp
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = currentDateHindi,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 15.sp,
                        color = AuraCyan.copy(alpha = 0.85f),
                        fontWeight = FontWeight.Medium
                    )
                )

                Text(
                    text = currentDateEnglish,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 13.sp,
                        color = OledClockSubtext
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Nova Assistant Ambient Chip
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .border(0.6.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Brush.sweepGradient(listOf(AuraCyan, AuraPurple, AuraMagenta)))
                    )
                    Text(
                        text = "Nova Assistant Ready • Hold Power Key",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            color = OledClockWhite.copy(alpha = 0.8f)
                        )
                    )
                }
            }

            // Bottom Section: Quick Flashlight / Camera shortcuts & Wake hint
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Wake gesture hint
                Text(
                    text = "Double-tap screen or press power button to wake",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        color = OledClockSubtext
                    )
                )

                // Quick Hardware Action Icons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Flashlight shortcut
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(if (isFlashlightOn) AuraCyan.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.08f))
                            .border(
                                width = 1.dp,
                                color = if (isFlashlightOn) AuraCyan else Color.White.copy(alpha = 0.15f),
                                shape = CircleShape
                            )
                            .clickable { onToggleFlashlight() }
                            .testTag("aod_flashlight_toggle"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isFlashlightOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                            contentDescription = "Flashlight Toggle",
                            tint = if (isFlashlightOn) AuraCyan else OledClockWhite,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Unlock Button Center
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .border(0.8.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
                            .clickable { onUnlock() }
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                            .testTag("aod_unlock_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Unlock",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = OledClockWhite,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }

                    // Camera shortcut
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.08f))
                            .border(
                                width = 1.dp,
                                color = Color.White.copy(alpha = 0.15f),
                                shape = CircleShape
                            )
                            .clickable { onLaunchCamera() }
                            .testTag("aod_camera_launcher"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = "Open Camera",
                            tint = OledClockWhite,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}
