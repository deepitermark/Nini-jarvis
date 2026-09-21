package com.example.ui.screens

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Assistant
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.StayCurrentPortrait
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.RecordVoiceOver
import com.example.model.BatteryInfo
import com.example.model.GeminiVoice
import com.example.ui.components.AndroidGestureBar
import com.example.ui.components.PhysicalPowerButtonWidget
import com.example.ui.theme.AuraAmber
import com.example.ui.theme.AuraCyan
import com.example.ui.theme.AuraMagenta
import com.example.ui.theme.AuraPurple
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonRed
import com.example.ui.theme.NovaDarkBackground
import com.example.ui.theme.NovaSurfaceCard
import com.example.ui.theme.NovaSurfaceCardStroke
import com.example.ui.theme.NovaSurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun HomeScreen(
    batteryInfo: BatteryInfo,
    isFlashlightOn: Boolean,
    hasFlashlight: Boolean,
    isWakeLockActive: Boolean,
    isAssistantActive: Boolean,
    audioRms: Float,
    powerChargingProgress: Float,
    timerRemainingSeconds: Int,
    isTimerRunning: Boolean,
    lastActionFeedback: String?,
    onPowerButtonDown: () -> Unit,
    onPowerButtonUp: () -> Unit,
    onGesturePillLongPress: () -> Unit,
    onGesturePillDoubleTap: () -> Unit,
    onGesturePillSingleTap: () -> Unit,
    onSummonAssistant: () -> Unit,
    onToggleFlashlight: () -> Unit,
    onToggleWakeLock: () -> Unit,
    onLaunchCamera: () -> Unit,
    onLaunchDialer: (String) -> Unit,
    onLaunchYouTube: (String) -> Unit,
    onLaunchGoogle: (String) -> Unit,
    onLaunchWhatsApp: (String) -> Unit,
    onLaunchSms: (String) -> Unit,
    onStartTimer: (Int) -> Unit,
    onCancelTimer: () -> Unit,
    onOpenDefaultSettings: () -> Unit,
    onShowSetupGuide: () -> Unit,
    selectedVoice: GeminiVoice = GeminiVoice.ORBIT,
    onVoiceSelect: (GeminiVoice) -> Unit = {},
    isDirectTalkEnabled: Boolean = true,
    onToggleDirectTalk: () -> Unit = {},
    customApiKey: String? = null,
    onSaveApiKey: (String) -> Unit = {},
    onOpenGeminiSettings: () -> Unit = {},
    onOpenChooseVoice: () -> Unit = {},
    apiStatusMessage: String = "",
    onVerifyApiKey: (String, (Boolean, String) -> Unit) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    var youtubeQueryInput by remember { mutableStateOf("") }
    var googleQueryInput by remember { mutableStateOf("") }
    var customKeyInput by remember(customApiKey) { mutableStateOf(customApiKey ?: "") }
    var showApiKeySection by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen"),
        containerColor = NovaDarkBackground,
        bottomBar = {
            AndroidGestureBar(
                isAssistantActive = isAssistantActive,
                audioRms = audioRms,
                onLongPress = onGesturePillLongPress,
                onDoubleTap = onGesturePillDoubleTap,
                onSingleTap = onGesturePillSingleTap
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
        ) {
            // Top Bar: App Branding + "Set Default" Quick Chip + Telemetry Pill
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Brush.sweepGradient(listOf(AuraCyan, AuraPurple, AuraMagenta))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Assistant,
                                contentDescription = "Nova",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "NOVA ASSISTANT",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp,
                                    letterSpacing = 1.2.sp,
                                    color = TextPrimary
                                )
                            )
                            Text(
                                text = "Real Hardware AI Controls",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 10.5.sp,
                                    color = AuraCyan
                                )
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // "Gemini Settings" Button Chip
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFF1E2638))
                                .border(
                                    width = 1.dp,
                                    color = AuraCyan.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .clickable { onOpenGeminiSettings() }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("header_gemini_settings_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Gemini Settings",
                                    tint = AuraCyan,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "Settings",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = TextPrimary
                                    )
                                )
                            }
                        }

                        // "Set Default" Button Chip
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(AuraCyan.copy(alpha = 0.25f), AuraPurple.copy(alpha = 0.25f))
                                    )
                                )
                                .border(
                                    width = 1.dp,
                                    brush = Brush.horizontalGradient(listOf(AuraCyan, AuraPurple)),
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .clickable { onShowSetupGuide() }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("header_set_default_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = AuraCyan,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "Set Default",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = TextPrimary
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Physical Power Button Simulator Card (Exact User Spec)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = NovaSurfaceCard),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(NovaSurfaceCardStroke, AuraPurple.copy(alpha = 0.4f))))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Physical Power Button Control",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = TextPrimary
                                    )
                                )
                                Text(
                                    text = "Hold >0.6s: Summon Nova | 2x Tap: Dismiss | 1x Tap: OLED Sleep",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                )
                            }
                        }

                        // The Hardware Button Widget with Radial Ring
                        PhysicalPowerButtonWidget(
                            chargingProgress = powerChargingProgress,
                            isAssistantActive = isAssistantActive,
                            onPressStart = onPowerButtonDown,
                            onPressRelease = onPowerButtonUp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Gemini 2.5 Flash Native Voice Engine & Brain Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = NovaSurfaceCard),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(AuraCyan.copy(alpha = 0.5f), AuraPurple.copy(alpha = 0.5f))))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = AuraCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = "Gemini Live Native Audio & Brain",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = TextPrimary
                                        )
                                    )
                                    Text(
                                        text = "gemini-3.8-live • responseModalities: AUDIO",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 10.5.sp,
                                            color = AuraCyan
                                        )
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(NeonGreen.copy(alpha = 0.15f))
                                    .border(0.8.dp, NeonGreen.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "LATEST TREND VOICE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = NeonGreen
                                    )
                                )
                            }
                        }

                        // Direct Talk Toggle Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(NovaSurfaceDark)
                                .border(0.8.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Hearing,
                                    contentDescription = null,
                                    tint = AuraCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = "Direct Talk (डायरेक्ट बातें - Hands-free)",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = TextPrimary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                    Text(
                                        text = "Nova bolne ke baad automatic fir se sunega, taaki continuous baat kar sakein",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 10.sp,
                                            color = TextSecondary
                                        )
                                    )
                                }
                            }
                            Switch(
                                checked = isDirectTalkEnabled,
                                onCheckedChange = { onToggleDirectTalk() },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = AuraCyan
                                )
                            )
                        }

                        // Gemini Voice Personality Selector
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Select Gemini Neural Voice Persona:",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            )

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(GeminiVoice.values()) { voice ->
                                    val isSelected = voice == selectedVoice
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(
                                                if (isSelected) {
                                                    Brush.horizontalGradient(listOf(AuraCyan.copy(alpha = 0.35f), AuraPurple.copy(alpha = 0.35f)))
                                                } else {
                                                    Brush.linearGradient(listOf(NovaSurfaceDark, NovaSurfaceDark))
                                                }
                                            )
                                            .border(
                                                width = if (isSelected) 1.5.dp else 0.8.dp,
                                                color = if (isSelected) AuraCyan else Color.White.copy(alpha = 0.1f),
                                                shape = RoundedCornerShape(14.dp)
                                            )
                                            .clickable { onVoiceSelect(voice) }
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.RecordVoiceOver,
                                                contentDescription = null,
                                                tint = if (isSelected) AuraCyan else TextSecondary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = voice.voiceName,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSelected) TextPrimary else TextSecondary,
                                                    fontSize = 12.sp
                                                )
                                            )
                                        }
                                    }
                                }
                            }

                            Text(
                                text = "Active Voice Tone: ${selectedVoice.personaDescription}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 10.5.sp,
                                    color = TextMuted
                                )
                            )

                            // Quick Links to Choose a voice & Gemini Settings (User Screenshots 1-3)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF1B2333))
                                        .border(1.dp, AuraCyan.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                        .clickable { onOpenChooseVoice() }
                                        .padding(vertical = 8.dp, horizontal = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Choose a voice (${selectedVoice.displayName})",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = AuraCyan,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.5.sp
                                        )
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF1B2333))
                                        .border(1.dp, AuraPurple.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                        .clickable { onOpenGeminiSettings() }
                                        .padding(vertical = 8.dp, horizontal = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Gemini Settings",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.5.sp
                                        )
                                    )
                                }
                            }
                        }

                        // Custom API Key & Direct Summon action
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = onSummonAssistant,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("summon_assistant_voice_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                contentPadding = PaddingValues()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Brush.horizontalGradient(listOf(AuraCyan, AuraPurple)))
                                        .padding(vertical = 11.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(Icons.Default.Mic, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        Text(
                                            text = "Talk to Nova Now",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        )
                                    }
                                }
                            }

                            IconButton(
                                onClick = { showApiKeySection = !showApiKeySection },
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(NovaSurfaceDark)
                                    .border(0.8.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Key,
                                    contentDescription = "API Key settings",
                                    tint = if (showApiKeySection) AuraCyan else TextSecondary
                                )
                            }
                        }

                        AnimatedVisibility(visible = showApiKeySection) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(NovaSurfaceDark)
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Custom Gemini API Key (Optional):",
                                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 10.5.sp)
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = customKeyInput,
                                        onValueChange = { customKeyInput = it },
                                        placeholder = { Text("Paste AI Studio API Key", fontSize = 11.sp, color = TextMuted) },
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = AuraCyan,
                                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )
                                    Button(
                                        onClick = { onSaveApiKey(customKeyInput) },
                                        colors = ButtonDefaults.buttonColors(containerColor = AuraCyan)
                                    ) {
                                        Text("Save", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = NovaSurfaceCard),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(NovaSurfaceCardStroke, AuraCyan.copy(alpha = 0.3f))))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "REAL HARDWARE TELEMETRY",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = AuraCyan
                                )
                            )

                            // Status badge
                            Text(
                                text = if (batteryInfo.isCharging) "⚡ Charging Active" else "🔋 On Battery",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    color = if (batteryInfo.isCharging) NeonGreen else TextSecondary
                                )
                            )
                        }

                        // Telemetry Metrics Grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Battery Level & Health
                            TelemetryChip(
                                title = "Battery",
                                value = "${batteryInfo.percentage}%",
                                subtitle = batteryInfo.chargeSource,
                                icon = Icons.Default.Bolt,
                                accentColor = if (batteryInfo.isCharging) NeonGreen else AuraCyan,
                                modifier = Modifier.weight(1f)
                            )

                            // Flashlight Status
                            TelemetryChip(
                                title = "Flashlight",
                                value = if (isFlashlightOn) "ON" else "OFF",
                                subtitle = if (hasFlashlight) "Camera LED" else "Simulated",
                                icon = if (isFlashlightOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                                accentColor = if (isFlashlightOn) AuraCyan else TextMuted,
                                modifier = Modifier.weight(1f)
                            )

                            // Screen WakeLock Status
                            TelemetryChip(
                                title = "WakeLock",
                                value = if (isWakeLockActive) "ACTIVE" else "OFF",
                                subtitle = "Screen Sleep",
                                icon = Icons.Default.StayCurrentPortrait,
                                accentColor = if (isWakeLockActive) AuraMagenta else TextMuted,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Hardware Controls Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Flashlight Toggle Button
                            Button(
                                onClick = onToggleFlashlight,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("hardware_torch_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isFlashlightOn) AuraCyan else NovaSurfaceDark,
                                    contentColor = if (isFlashlightOn) Color.Black else TextPrimary
                                )
                            ) {
                                Icon(
                                    imageVector = if (isFlashlightOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isFlashlightOn) "Torch ON" else "Torch OFF",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Keep Screen Awake Toggle
                            Button(
                                onClick = onToggleWakeLock,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("hardware_wakelock_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isWakeLockActive) AuraMagenta else NovaSurfaceDark,
                                    contentColor = if (isWakeLockActive) Color.White else TextPrimary
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.StayCurrentPortrait,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isWakeLockActive) "Awake: ON" else "Awake: OFF",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Quick Hardware Action Grid: Camera, Phone Dialer, WhatsApp, SMS
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = NovaSurfaceCard),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(NovaSurfaceCardStroke, AuraAmber.copy(alpha = 0.3f))))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "REAL PHONE & DEVICE TOOLS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = AuraAmber
                            )
                        )

                        // 4 Quick tool buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Camera
                            QuickToolSquare(
                                icon = Icons.Default.PhotoCamera,
                                label = "Camera",
                                onClick = onLaunchCamera,
                                accentColor = AuraCyan,
                                modifier = Modifier.weight(1f),
                                testTag = "tool_camera"
                            )

                            // Call Mom
                            QuickToolSquare(
                                icon = Icons.Default.Call,
                                label = "Call Mom",
                                onClick = { onLaunchDialer("Mom") },
                                accentColor = NeonGreen,
                                modifier = Modifier.weight(1f),
                                testTag = "tool_call_mom"
                            )

                            // WhatsApp
                            QuickToolSquare(
                                icon = Icons.Default.Assistant,
                                label = "WhatsApp",
                                onClick = { onLaunchWhatsApp("") },
                                accentColor = NeonGreen,
                                modifier = Modifier.weight(1f),
                                testTag = "tool_whatsapp"
                            )

                            // Phone Dialer
                            QuickToolSquare(
                                icon = Icons.Default.Call,
                                label = "Dialer",
                                onClick = { onLaunchDialer("") },
                                accentColor = AuraPurple,
                                modifier = Modifier.weight(1f),
                                testTag = "tool_dialer"
                            )
                        }
                    }
                }
            }

            // YouTube & Web Browser Controls Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = NovaSurfaceCard),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(NovaSurfaceCardStroke, NeonRed.copy(alpha = 0.3f))))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "YOUTUBE & WEB BROWSER CONTROLS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = NeonRed
                            )
                        )

                        // YouTube Search & Quick launch
                        OutlinedTextField(
                            value = youtubeQueryInput,
                            onValueChange = { youtubeQueryInput = it },
                            placeholder = {
                                Text("YouTube: 'Arijit Singh ke gane' ya song search...", fontSize = 12.sp, color = TextMuted)
                            },
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        onLaunchYouTube(youtubeQueryInput)
                                        youtubeQueryInput = ""
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Search YouTube",
                                        tint = NeonRed
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("youtube_search_input"),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonRed,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                                focusedContainerColor = NovaSurfaceDark,
                                unfocusedContainerColor = NovaSurfaceDark,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            singleLine = true
                        )

                        // YouTube Quick Chips
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(listOf("Arijit Singh Songs", "Gana Bajao", "Top Bollywood 2026", "Trending News")) { query ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(NovaSurfaceDark)
                                        .border(0.6.dp, NeonRed.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                        .clickable { onLaunchYouTube(query) }
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text(text = "▶ $query", fontSize = 10.5.sp, color = TextPrimary)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        // Google Search
                        OutlinedTextField(
                            value = googleQueryInput,
                            onValueChange = { googleQueryInput = it },
                            placeholder = {
                                Text("Google Search: Sawal ya query likhein...", fontSize = 12.sp, color = TextMuted)
                            },
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        if (googleQueryInput.isNotBlank()) {
                                            onLaunchGoogle(googleQueryInput)
                                            googleQueryInput = ""
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search Google",
                                        tint = AuraCyan
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("google_search_input"),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AuraCyan,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                                focusedContainerColor = NovaSurfaceDark,
                                unfocusedContainerColor = NovaSurfaceDark,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            singleLine = true
                        )
                    }
                }
            }

            // Audible Countdown Timer & Alarm Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = NovaSurfaceCard),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(NovaSurfaceCardStroke, AuraPurple.copy(alpha = 0.3f))))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "AUDIBLE TIMER & ALARM CHIME",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = AuraPurple
                                )
                            )

                            if (isTimerRunning) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(AuraMagenta.copy(alpha = 0.2f))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "RUNNING",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AuraMagenta
                                    )
                                }
                            }
                        }

                        // Timer Display
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                val minutes = timerRemainingSeconds / 60
                                val seconds = timerRemainingSeconds % 60
                                val timeFormatted = String.format("%02d:%02d", minutes, seconds)

                                Text(
                                    text = if (isTimerRunning) timeFormatted else "00:00",
                                    style = MaterialTheme.typography.displayMedium.copy(
                                        fontSize = 36.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isTimerRunning) AuraCyan else TextMuted
                                    )
                                )
                                Text(
                                    text = if (isTimerRunning) "Audible chime will sound at 00:00" else "Choose duration or say '5 min timer'",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                )
                            }

                            if (isTimerRunning) {
                                Button(
                                    onClick = onCancelTimer,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = NeonRed,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Text("Stop Timer", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Presets: 30s, 1m, 5m, 10m
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TimerPresetButton("30 Sec", 30, onStartTimer, Modifier.weight(1f))
                            TimerPresetButton("1 Min", 60, onStartTimer, Modifier.weight(1f))
                            TimerPresetButton("5 Min", 300, onStartTimer, Modifier.weight(1f))
                            TimerPresetButton("10 Min", 600, onStartTimer, Modifier.weight(1f))
                        }
                    }
                }
            }

            // Big Central Summon Nova Assistant Floating Action Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSummonAssistant() }
                        .testTag("summon_nova_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = NovaSurfaceCard),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(
                            listOf(AuraCyan, AuraPurple, AuraMagenta)
                        )
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Brush.sweepGradient(listOf(AuraCyan, AuraPurple, AuraMagenta))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Mic",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Start Voice Conversation",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            Text(
                                text = "Tap here or long-press bottom pill / power button",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.5.sp,
                                    color = TextSecondary
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TelemetryChip(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(NovaSurfaceDark)
            .border(0.6.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
            .padding(10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 9.sp,
                    color = TextMuted
                ),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun QuickToolSquare(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    accentColor: Color,
    modifier: Modifier = Modifier,
    testTag: String
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(NovaSurfaceDark)
            .border(0.6.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 6.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
            )
        }
    }
}

@Composable
private fun TimerPresetButton(
    label: String,
    seconds: Int,
    onStartTimer: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(NovaSurfaceDark)
            .border(0.6.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            .clickable { onStartTimer(seconds) }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
    }
}
