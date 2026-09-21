package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AssistantLanguage
import com.example.model.ConversationMessage
import com.example.model.GeminiVoice
import com.example.model.Sender
import com.example.ui.components.AmbientAuraWave
import com.example.ui.theme.AuraAmber
import com.example.ui.theme.AuraCyan
import com.example.ui.theme.AuraMagenta
import com.example.ui.theme.AuraPurple
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NovaDarkBackground
import com.example.ui.theme.NovaSurfaceCard
import com.example.ui.theme.NovaSurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun NovaAssistantSheet(
    isListening: Boolean,
    isSpeaking: Boolean,
    isThinking: Boolean,
    audioRms: Float,
    currentLanguage: AssistantLanguage,
    conversation: List<ConversationMessage>,
    lastActionFeedback: String?,
    selectedVoice: GeminiVoice = GeminiVoice.ORBIT,
    onVoiceSelect: (GeminiVoice) -> Unit = {},
    isDirectTalkEnabled: Boolean = true,
    onToggleDirectTalk: () -> Unit = {},
    isNativeAudioActive: Boolean = true,
    speechStatus: String? = null,
    onOpenSettings: () -> Unit = {},
    onOpenChooseVoice: () -> Unit = {},
    onDismiss: () -> Unit,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    onLanguageSelect: (AssistantLanguage) -> Unit,
    onSendTextMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var textInput by remember { mutableStateOf("") }
    var showVoiceSelector by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val listState = rememberLazyListState()

    // Auto-scroll to latest message
    LaunchedEffect(conversation.size) {
        if (conversation.isNotEmpty()) {
            listState.animateScrollToItem(conversation.size - 1)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_assistant")
    val micPulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mic_pulse"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        onDismiss()
                    }
                )
            }
            .testTag("nova_assistant_sheet")
    ) {
        // Bottom-anchored Assistant HUD Card
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            NovaSurfaceDark.copy(alpha = 0.98f),
                            NovaDarkBackground
                        )
                    )
                )
                .border(
                    width = 1.2.dp,
                    brush = Brush.horizontalGradient(
                        listOf(AuraCyan.copy(alpha = 0.8f), AuraPurple.copy(alpha = 0.8f), AuraMagenta.copy(alpha = 0.8f))
                    ),
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                )
                .padding(bottom = 8.dp)
        ) {
            // Header Bar with Nova Identity and Dismiss button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(Brush.sweepGradient(listOf(AuraCyan, AuraPurple, AuraMagenta)))
                    )
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "NOVA ASSISTANT",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    letterSpacing = 1.sp
                                )
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(AuraCyan.copy(alpha = 0.15f))
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "GEMINI LIVE NATIVE AUDIO",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AuraCyan
                                    )
                                )
                            }
                        }
                        Text(
                            text = speechStatus ?: when {
                                isListening -> "Listening to your voice... (Speak now)"
                                isThinking -> "Gemini Brain reasoning & generating voice..."
                                isSpeaking -> "Nova is speaking (${selectedVoice.displayName})..."
                                else -> "Double-tap anywhere to dismiss"
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                color = if (speechStatus != null) AuraCyan else if (isListening) AuraCyan else if (isThinking) AuraAmber else if (isSpeaking) NeonGreen else TextSecondary
                            )
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Gemini Settings",
                            tint = AuraCyan,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                    IconButton(
                        onClick = { showVoiceSelector = !showVoiceSelector }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Voice settings",
                            tint = if (showVoiceSelector) AuraCyan else TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("assistant_dismiss_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss Assistant",
                            tint = TextSecondary
                        )
                    }
                }
            }

            // Expandable Gemini Voice & Direct Talk Controls
            if (showVoiceSelector) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(NovaSurfaceCard)
                        .border(1.dp, AuraPurple.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Hearing, contentDescription = null, tint = AuraCyan, modifier = Modifier.size(16.dp))
                            Text(
                                text = "Direct Talk (डायरेक्ट बातें - Hands-free)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                        Switch(
                            checked = isDirectTalkEnabled,
                            onCheckedChange = { onToggleDirectTalk() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = AuraCyan
                            ),
                            modifier = Modifier.scale(0.8f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Select Gemini Neural Voice Persona:",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 10.5.sp)
                        )
                        Text(
                            text = "All Voices →",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = AuraCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            modifier = Modifier
                                .clickable { onOpenChooseVoice() }
                                .padding(4.dp)
                        )
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(GeminiVoice.values()) { voice ->
                            val isSelected = voice == selectedVoice
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) AuraCyan.copy(alpha = 0.25f) else NovaSurfaceDark)
                                    .border(
                                        width = if (isSelected) 1.5.dp else 0.8.dp,
                                        color = if (isSelected) AuraCyan else Color.White.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { onVoiceSelect(voice) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.RecordVoiceOver,
                                        contentDescription = null,
                                        tint = if (isSelected) AuraCyan else TextSecondary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = voice.voiceName,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else TextSecondary
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Language Selector Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(AssistantLanguage.values()) { lang ->
                    val isSelected = lang == currentLanguage
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) AuraCyan.copy(alpha = 0.2f) else NovaSurfaceCard)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) AuraCyan else Color.White.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { onLanguageSelect(lang) }
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = lang.displayName,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) AuraCyan else TextSecondary
                            )
                        )
                    }
                }
            }

            // Conversation Messages Stream
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 240.dp)
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(conversation) { msg ->
                    ConversationBubble(msg)
                }
            }

            // Last Executed Action Badge
            if (!lastActionFeedback.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(AuraCyan.copy(alpha = 0.12f))
                        .border(1.dp, AuraCyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Action Executed",
                            tint = AuraCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Action: $lastActionFeedback",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = AuraCyan,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }

            // Quick Voice Command Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val suggestions = listOf(
                    "Flashlight on karo",
                    "Flashlight off karo",
                    "Call Mom",
                    "Camera open karo",
                    "5 minute ka timer lagao",
                    "YouTube par Arijit Singh ke gane chalao",
                    "Google par weather search karo",
                    "Mera naam Rahul hai",
                    "Mera naam kya bataya tha maine?",
                    "Battery kitni hai?"
                )
                items(suggestions) { prompt ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(NovaSurfaceCard)
                            .border(0.7.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
                            .clickable {
                                onSendTextMessage(prompt)
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = prompt,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                color = TextPrimary
                            )
                        )
                    }
                }
            }

            // Input Row: Text field + Central Listening Mic Button + Send Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Text input for quiet prompting
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = {
                        Text(
                            text = if (isDirectTalkEnabled) "Direct baatein karein ya type karein..." else "Boliye ya type kijiye...",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                        )
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AuraCyan,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                        cursorColor = AuraCyan,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = NovaSurfaceDark,
                        unfocusedContainerColor = NovaSurfaceDark
                    ),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("assistant_text_input")
                )

                // Central Mic Button with pulsing effect
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .scale(if (isListening) micPulseScale else 1f)
                        .clip(CircleShape)
                        .background(
                            if (isListening) {
                                Brush.radialGradient(listOf(AuraCyan, AuraPurple, NovaDarkBackground))
                            } else if (isSpeaking) {
                                Brush.radialGradient(listOf(NeonGreen, AuraCyan, NovaDarkBackground))
                            } else {
                                Brush.radialGradient(listOf(AuraPurple, NovaSurfaceCard))
                            }
                        )
                        .border(
                            width = 2.dp,
                            color = if (isListening) AuraCyan else if (isSpeaking) NeonGreen else Color.White.copy(alpha = 0.25f),
                            shape = CircleShape
                        )
                        .clickable {
                            if (isListening) onStopListening() else onStartListening()
                        }
                        .testTag("assistant_mic_button"),
                    contentAlignment = Alignment.Center
                ) {
                    if (isThinking) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Icon(
                            imageVector = if (isListening) Icons.Default.Mic else if (isSpeaking) Icons.Default.RecordVoiceOver else Icons.Default.MicOff,
                            contentDescription = if (isListening) "Stop Listening" else "Start Listening",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                // Send icon for text input
                if (textInput.isNotBlank()) {
                    IconButton(
                        onClick = {
                            onSendTextMessage(textInput)
                            textInput = ""
                            focusManager.clearFocus()
                        },
                        modifier = Modifier.testTag("assistant_send_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send Message",
                            tint = AuraCyan
                        )
                    }
                }
            }

            // 4-Color Glowing Ambient Voice Aura Wave at the bottom of the HUD
            AmbientAuraWave(
                isActive = true,
                audioRms = audioRms,
                height = 42.dp
            )
        }
    }
}

@Composable
private fun ConversationBubble(msg: ConversationMessage) {
    val isUser = msg.sender == Sender.USER

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .background(
                    if (isUser) {
                        Brush.horizontalGradient(listOf(AuraPurple.copy(alpha = 0.6f), AuraCyan.copy(alpha = 0.5f)))
                    } else {
                        Brush.horizontalGradient(listOf(NovaSurfaceCard, NovaSurfaceDark))
                    }
                )
                .border(
                    width = 0.8.dp,
                    color = if (isUser) AuraCyan.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (!isUser && msg.isNativeAudio) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = AuraCyan,
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = "Gemini Native Audio (${msg.voiceName ?: "Aoede"})",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = AuraCyan,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                Text(
                    text = msg.text,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextPrimary,
                        fontSize = 13.5.sp
                    )
                )

                if (!msg.actionLabel.isNullOrBlank()) {
                    Text(
                        text = "⚡ ${msg.actionLabel}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = AuraCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }
    }
}
