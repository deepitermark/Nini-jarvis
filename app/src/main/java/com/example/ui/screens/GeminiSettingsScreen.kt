package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assistant
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.ContactPage
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.ScreenSearchDesktop
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VoiceChat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AssistantLanguage
import com.example.model.GeminiSettingsState
import com.example.model.GeminiVoice

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeminiSettingsScreen(
    settings: GeminiSettingsState,
    apiStatus: String,
    customApiKey: String?,
    onUpdateSettings: ((GeminiSettingsState) -> GeminiSettingsState) -> Unit,
    onOpenChooseVoice: () -> Unit,
    onVerifyApiKey: (String, (Boolean, String) -> Unit) -> Unit,
    onBack: () -> Unit
) {
    var apiKeyInput by remember(customApiKey) { mutableStateOf(customApiKey ?: "") }
    var isVerifyingKey by remember { mutableStateOf(false) }
    var verificationResult by remember { mutableStateOf<String?>(null) }
    var verificationSuccess by remember { mutableStateOf<Boolean?>(null) }

    val bgLight = Color(0xFFFAF9FD)
    val textMain = Color(0xFF1B1B1F)
    val textSub = Color(0xFF43474E)
    val primaryBlue = Color(0xFF0B57D0)

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("gemini_settings_screen"),
        containerColor = bgLight,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Gemini Settings",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Normal,
                            color = textMain
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = textMain
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bgLight)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Gemini Live & responses",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = Color(0xFF74777F),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                )
            }

            // 1. Your public links
            item {
                SettingsNavigationRow(
                    icon = Icons.Default.Link,
                    title = "Your public links",
                    subtitle = "Control how you share chats and responses",
                    onClick = {}
                )
            }

            // 2. Gemini's voice -> Opens Choose Voice
            item {
                SettingsNavigationRow(
                    icon = Icons.Default.GraphicEq,
                    title = "Gemini's voice",
                    subtitle = "Choose how Gemini will sound • Currently ${settings.selectedVoice.displayName}",
                    onClick = onOpenChooseVoice
                )
            }

            // 3. Interrupt Live responses (Toggle ON)
            item {
                SettingsToggleRow(
                    icon = Icons.Default.RecordVoiceOver,
                    title = "Interrupt Live responses",
                    subtitle = "Talk to interrupt Gemini in Live chats",
                    isChecked = settings.interruptLiveResponses,
                    onCheckedChange = { checked ->
                        onUpdateSettings { it.copy(interruptLiveResponses = checked) }
                    }
                )
            }

            // 4. Caption preferences
            item {
                SettingsToggleRow(
                    icon = Icons.Default.ClosedCaption,
                    title = "Caption preferences",
                    subtitle = "Manage device captions to see text of Gemini Live's responses",
                    isChecked = settings.captionPreferences,
                    onCheckedChange = { checked ->
                        onUpdateSettings { it.copy(captionPreferences = checked) }
                    }
                )
            }

            // 5. Guided vision in Live (Toggle OFF)
            item {
                SettingsToggleRow(
                    icon = Icons.Default.Visibility,
                    title = "Guided vision in Live",
                    subtitle = "Hear detailed audio descriptions when using your camera",
                    isChecked = settings.guidedVisionInLive,
                    onCheckedChange = { checked ->
                        onUpdateSettings { it.copy(guidedVisionInLive = checked) }
                    }
                )
            }

            // 6. Languages
            item {
                SettingsLanguageRow(
                    currentLanguage = settings.language,
                    onLanguageSelected = { lang ->
                        onUpdateSettings { it.copy(language = lang) }
                    }
                )
            }

            // 7. Talk to Gemini hands-free
            item {
                SettingsToggleRow(
                    icon = Icons.Default.VoiceChat,
                    title = "Talk to Gemini hands-free",
                    subtitle = "Say \"Hey Google\" or \"Hey Nova\" to get help from Gemini",
                    isChecked = settings.handsFreeEnabled,
                    onCheckedChange = { checked ->
                        onUpdateSettings { it.copy(handsFreeEnabled = checked) }
                    }
                )
            }

            // 8. Screen context
            item {
                SettingsToggleRow(
                    icon = Icons.Default.PhoneAndroid,
                    title = "Screen context",
                    subtitle = "Allow Gemini to capture information on your screen",
                    isChecked = settings.screenContextEnabled,
                    onCheckedChange = { checked ->
                        onUpdateSettings { it.copy(screenContextEnabled = checked) }
                    }
                )
            }

            // 9. Contact info from your devices
            item {
                SettingsToggleRow(
                    icon = Icons.Default.ContactPage,
                    title = "Contact info from your devices",
                    subtitle = "Saves info from your signed-in devices about contacts to make them easier to interact with",
                    isChecked = settings.contactInfoEnabled,
                    onCheckedChange = { checked ->
                        onUpdateSettings { it.copy(contactInfoEnabled = checked) }
                    }
                )
            }

            // 10. Gemini on lock screen
            item {
                SettingsToggleRow(
                    icon = Icons.Default.Lock,
                    title = "Gemini on lock screen",
                    subtitle = "Use Gemini when your screen is locked",
                    isChecked = settings.geminiOnLockScreen,
                    onCheckedChange = { checked ->
                        onUpdateSettings { it.copy(geminiOnLockScreen = checked) }
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = Color(0xFFE0E2EC), thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))
            }

            // 11. API Key & Connection Diagnostics
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEEF2FB))
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = null,
                                tint = primaryBlue,
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(
                                    text = "Gemini Cloud API Key",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = textMain
                                    )
                                )
                                Text(
                                    text = apiStatus,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (apiStatus.startsWith("Error", ignoreCase = true)) Color(0xFFBA1A1A) else Color(0xFF006C4C),
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }

                        OutlinedTextField(
                            value = apiKeyInput,
                            onValueChange = {
                                apiKeyInput = it
                                verificationResult = null
                                verificationSuccess = null
                            },
                            placeholder = { Text("Paste Google AI Studio API Key (AIzaSy...)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = primaryBlue,
                                unfocusedBorderColor = Color(0xFFC4C6CF)
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Keys stay private on-device",
                                style = MaterialTheme.typography.labelSmall.copy(color = textSub)
                            )

                            Button(
                                onClick = {
                                    isVerifyingKey = true
                                    onVerifyApiKey(apiKeyInput) { success, message ->
                                        isVerifyingKey = false
                                        verificationSuccess = success
                                        verificationResult = message
                                    }
                                },
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = primaryBlue),
                                enabled = !isVerifyingKey
                            ) {
                                if (isVerifyingKey) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text("Verify & Save", fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        verificationResult?.let { msg ->
                            val color = if (verificationSuccess == true) Color(0xFF006C4C) else Color(0xFFBA1A1A)
                            Text(
                                text = msg,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = color,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SettingsNavigationRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF43474E),
            modifier = Modifier.size(26.dp)
        )
        Spacer(modifier = Modifier.width(18.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1B1B1F)
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 13.5.sp,
                    color = Color(0xFF43474E)
                )
            )
        }
    }
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onCheckedChange(!isChecked) }
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF43474E),
            modifier = Modifier.size(26.dp)
        )
        Spacer(modifier = Modifier.width(18.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1B1B1F)
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 13.5.sp,
                    color = Color(0xFF43474E)
                )
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF0B57D0),
                uncheckedThumbColor = Color(0xFF74777F),
                uncheckedTrackColor = Color(0xFFE0E2EC)
            )
        )
    }
}

@Composable
private fun SettingsLanguageRow(
    currentLanguage: AssistantLanguage,
    onLanguageSelected: (AssistantLanguage) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp, horizontal = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Language,
                contentDescription = null,
                tint = Color(0xFF43474E),
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.width(18.dp))
            Column {
                Text(
                    text = "Languages",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1B1B1F)
                    )
                )
                Text(
                    text = "For speaking to Gemini",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 13.5.sp,
                        color = Color(0xFF43474E)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 44.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                AssistantLanguage.AUTO to "Auto (Hindi/Eng)",
                AssistantLanguage.HINDI to "हिंदी (Hindi)",
                AssistantLanguage.ENGLISH to "English"
            ).forEach { (lang, label) ->
                val isSelected = currentLanguage == lang
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) Color(0xFFD3E3FD) else Color(0xFFE9ECF4))
                        .clickable { onLanguageSelected(lang) }
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color(0xFF041E49) else Color(0xFF43474E)
                        )
                    )
                }
            }
        }
    }
}
