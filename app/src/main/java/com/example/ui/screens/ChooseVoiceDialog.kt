package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.GeminiVoice

@Composable
fun ChooseVoiceDialog(
    currentVoice: GeminiVoice,
    onVoiceConfirmed: (GeminiVoice) -> Unit,
    onPreviewVoice: (GeminiVoice) -> Unit,
    onDismiss: () -> Unit
) {
    var tempSelectedVoice by remember { mutableStateOf(currentVoice) }

    val allVoices = listOf(
        GeminiVoice.FLARE,
        GeminiVoice.ORBIT,
        GeminiVoice.GLOW,
        GeminiVoice.CAPELLA,
        GeminiVoice.ORION,
        GeminiVoice.URSA,
        GeminiVoice.ECLIPSE,
        GeminiVoice.VEGA,
        GeminiVoice.DIPPER,
        GeminiVoice.AOEDE,
        GeminiVoice.PUCK,
        GeminiVoice.KORE,
        GeminiVoice.CHARON,
        GeminiVoice.FENRIR
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(28.dp))
                .testTag("choose_voice_dialog"),
            color = Color(0xFFF9F9FC),
            contentColor = Color(0xFF1B1B1F)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                // Top Header matching Screenshot 3
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Choose a voice",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color(0xFF1E1F24)
                        )
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Waveform pill icon from screenshot
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0xFFE2E7EF))
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = "Voice sample waveform",
                                tint = Color(0xFF43474E),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More options",
                                tint = Color(0xFF43474E)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Voice list
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(allVoices) { voice ->
                        val isSelected = (voice == tempSelectedVoice)
                        val textColor = if (isSelected) Color(0xFF001D35) else Color(0xFF43474E)

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(24.dp))
                                .background(if (isSelected) Color(0xFFD3E3FD) else Color.Transparent)
                                .clickable {
                                    tempSelectedVoice = voice
                                    onPreviewVoice(voice)
                                }
                                .padding(horizontal = 20.dp, vertical = 14.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = voice.displayName,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontSize = 28.sp,
                                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                                        color = textColor
                                    )
                                )

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected voice",
                                        tint = Color(0xFF001D35),
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Preview speech bubble card matching Screenshot 3
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF1F8))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "one of my voice options If you like what you hear, choose this voice to start talking.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                                color = Color(0xFF1E1F24)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Bottom actions matching Screenshot 3: Cancel and Confirm
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Text(
                            text = "Cancel",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF0B57D0)
                            )
                        )
                    }

                    Button(
                        onClick = {
                            onVoiceConfirmed(tempSelectedVoice)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFA8C7FA),
                            contentColor = Color(0xFF001D35)
                        ),
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        Text(
                            text = "Confirm",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
