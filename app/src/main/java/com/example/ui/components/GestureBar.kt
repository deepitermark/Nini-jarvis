package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AuraAmber
import com.example.ui.theme.AuraCyan
import com.example.ui.theme.AuraMagenta
import com.example.ui.theme.AuraPurple
import com.example.ui.theme.NovaDarkBackground
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary

@Composable
fun AndroidGestureBar(
    isAssistantActive: Boolean,
    audioRms: Float,
    onLongPress: () -> Unit,
    onDoubleTap: () -> Unit,
    onSingleTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPillPressed by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(NovaDarkBackground.copy(alpha = 0.95f))
            .testTag("android_gesture_bar"),
        contentAlignment = Alignment.BottomCenter
    ) {
        // 4-color glowing ambient voice aura wave animated when Assistant is active
        AnimatedVisibility(
            visible = isAssistantActive,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            AmbientAuraWave(
                isActive = true,
                audioRms = audioRms,
                height = 54.dp
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp, top = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Contextual Gesture hint
            Text(
                text = if (isAssistantActive) "Double-tap pill to turn off Nova" else "Long-press pill to summon Nova",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    color = if (isAssistantActive) AuraCyan else TextMuted,
                    fontWeight = FontWeight.Medium
                )
            )

            // Tactile Android Navigation Pill
            Box(
                modifier = Modifier
                    .width(if (isPillPressed) 150.dp else 136.dp)
                    .height(if (isPillPressed) 8.dp else 6.dp)
                    .shadow(
                        elevation = if (isAssistantActive) 16.dp else 4.dp,
                        shape = CircleShape,
                        spotColor = if (isAssistantActive) AuraCyan else Color.White.copy(alpha = 0.3f)
                    )
                    .clip(CircleShape)
                    .background(
                        if (isAssistantActive) {
                            Brush.horizontalGradient(
                                listOf(AuraCyan, AuraPurple, AuraAmber, AuraMagenta)
                            )
                        } else {
                            Brush.horizontalGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.65f),
                                    Color.White.copy(alpha = 0.85f),
                                    Color.White.copy(alpha = 0.65f)
                                )
                            )
                        }
                    )
                    .border(
                        width = 0.8.dp,
                        color = if (isAssistantActive) AuraCyan.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.2f),
                        shape = CircleShape
                    )
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                isPillPressed = true
                                tryAwaitRelease()
                                isPillPressed = false
                            },
                            onDoubleTap = {
                                onDoubleTap()
                            },
                            onLongPress = {
                                onLongPress()
                            },
                            onTap = {
                                onSingleTap()
                            }
                        )
                    }
                    .testTag("navigation_pill")
            )
        }
    }
}
