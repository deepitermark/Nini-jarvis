package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.ui.NovaAssistantViewModel
import com.example.ui.screens.AodLockScreen
import com.example.ui.screens.ChooseVoiceDialog
import com.example.ui.screens.DefaultAssistantGuideDialog
import com.example.ui.screens.GeminiSettingsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.NovaAssistantSheet
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: NovaAssistantViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MainApp(viewModel = viewModel, activity = this)
            }
        }
    }

    // Hardware Key Event Interception (Power button / Assist / Volume down)
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_POWER -> {
                viewModel.onPowerButtonDown()
                return true
            }
            KeyEvent.KEYCODE_ASSIST, KeyEvent.KEYCODE_VOICE_ASSIST -> {
                viewModel.summonAssistant(fromPowerButton = true)
                return true
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                if (event?.isLongPress == true) {
                    viewModel.summonAssistant(fromPowerButton = true)
                    return true
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_POWER -> {
                viewModel.onPowerButtonUp()
                return true
            }
        }
        return super.onKeyUp(keyCode, event)
    }
}

@Composable
fun MainApp(
    viewModel: NovaAssistantViewModel,
    activity: ComponentActivity
) {
    // Collect all states
    val batteryInfo by viewModel.batteryInfo.collectAsState()
    val isFlashlightOn by viewModel.isFlashlightOn.collectAsState()
    val hasFlashlight by viewModel.hasFlashlight.collectAsState()
    val isWakeLockActive by viewModel.isWakeLockActive.collectAsState()

    val isAssistantVisible by viewModel.isAssistantVisible.collectAsState()
    val isListening by viewModel.isListening.collectAsState()
    val isSpeaking by viewModel.isSpeaking.collectAsState()
    val isThinking by viewModel.isThinking.collectAsState()
    val audioRms by viewModel.audioRms.collectAsState()
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val conversation by viewModel.conversation.collectAsState()
    val lastActionFeedback by viewModel.lastActionFeedback.collectAsState()
    val selectedVoice by viewModel.selectedGeminiVoice.collectAsState()
    val isDirectTalkEnabled by viewModel.isDirectTalkEnabled.collectAsState()
    val isNativeAudioActive by viewModel.isNativeAudioActive.collectAsState()
    val customApiKey by viewModel.customApiKey.collectAsState()
    val speechStatus by viewModel.speechStatus.collectAsState()
    val apiStatusMessage by viewModel.apiStatusMessage.collectAsState()
    val settingsState by viewModel.settingsState.collectAsState()
    val showGeminiSettings by viewModel.showGeminiSettings.collectAsState()
    val showChooseVoiceDialog by viewModel.showChooseVoiceDialog.collectAsState()

    val isAodLocked by viewModel.isAodLocked.collectAsState()
    val showSetupGuide by viewModel.showSetupGuide.collectAsState()
    val timerRemainingSeconds by viewModel.timerRemainingSeconds.collectAsState()
    val isTimerRunning by viewModel.isTimerRunning.collectAsState()
    val powerChargingProgress by viewModel.powerChargingProgress.collectAsState()

    // Request permissions for Audio recording and Camera
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    LaunchedEffect(Unit) {
        val needsAudio = ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.RECORD_AUDIO
        ) != PackageManager.PERMISSION_GRANTED

        val needsCamera = ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.CAMERA
        ) != PackageManager.PERMISSION_GRANTED

        if (needsAudio || needsCamera) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.CAMERA
                )
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isAodLocked) {
            // Realistic OLED Always-On Display Lockscreen
            AodLockScreen(
                batteryInfo = batteryInfo,
                isFlashlightOn = isFlashlightOn,
                powerChargingProgress = powerChargingProgress,
                onPowerButtonDown = { viewModel.onPowerButtonDown() },
                onPowerButtonUp = { viewModel.onPowerButtonUp() },
                onToggleFlashlight = { viewModel.toggleFlashlight() },
                onLaunchCamera = { viewModel.hardwareController.launchCamera() },
                onUnlock = { viewModel.toggleAodLock(forceState = false) }
            )
        } else {
            // Main Home Screen with Controls, Gesture Bar, and Power Button
            HomeScreen(
                batteryInfo = batteryInfo,
                isFlashlightOn = isFlashlightOn,
                hasFlashlight = hasFlashlight,
                isWakeLockActive = isWakeLockActive,
                isAssistantActive = isAssistantVisible,
                audioRms = audioRms,
                powerChargingProgress = powerChargingProgress,
                timerRemainingSeconds = timerRemainingSeconds,
                isTimerRunning = isTimerRunning,
                lastActionFeedback = lastActionFeedback,
                onPowerButtonDown = { viewModel.onPowerButtonDown() },
                onPowerButtonUp = { viewModel.onPowerButtonUp() },
                onGesturePillLongPress = { viewModel.onGesturePillLongPress() },
                onGesturePillDoubleTap = { viewModel.onGesturePillTap() },
                onGesturePillSingleTap = { viewModel.onGesturePillTap() },
                onSummonAssistant = { viewModel.summonAssistant() },
                onToggleFlashlight = { viewModel.toggleFlashlight() },
                onToggleWakeLock = { viewModel.toggleWakeLock(activity) },
                onLaunchCamera = { viewModel.hardwareController.launchCamera() },
                onLaunchDialer = { nameOrNumber -> viewModel.hardwareController.launchPhoneDialer(nameOrNumber) },
                onLaunchYouTube = { query -> viewModel.hardwareController.launchYouTube(query) },
                onLaunchGoogle = { query -> viewModel.hardwareController.launchGoogleSearch(query) },
                onLaunchWhatsApp = { query -> viewModel.hardwareController.launchWhatsApp(message = query) },
                onLaunchSms = { query -> viewModel.hardwareController.launchSms(message = query) },
                onStartTimer = { seconds -> viewModel.startTimer(seconds) },
                onCancelTimer = { viewModel.cancelTimer() },
                onOpenDefaultSettings = { viewModel.hardwareController.openDefaultAssistantSettings() },
                onShowSetupGuide = { viewModel.setShowSetupGuide(true) },
                selectedVoice = selectedVoice,
                onVoiceSelect = { voice -> viewModel.selectGeminiVoice(voice) },
                isDirectTalkEnabled = isDirectTalkEnabled,
                onToggleDirectTalk = { viewModel.toggleDirectTalk() },
                customApiKey = customApiKey,
                onSaveApiKey = { key -> viewModel.setCustomApiKey(key) },
                onOpenGeminiSettings = { viewModel.setShowGeminiSettings(true) },
                onOpenChooseVoice = { viewModel.setShowChooseVoiceDialog(true) },
                apiStatusMessage = apiStatusMessage,
                onVerifyApiKey = { key, cb -> viewModel.testCustomApiKey(key, cb) }
            )

            // Nova Assistant HUD Sheet
            AnimatedVisibility(
                visible = isAssistantVisible,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
            ) {
                NovaAssistantSheet(
                    isListening = isListening,
                    isSpeaking = isSpeaking,
                    isThinking = isThinking,
                    audioRms = audioRms,
                    currentLanguage = currentLanguage,
                    conversation = conversation,
                    lastActionFeedback = lastActionFeedback,
                    selectedVoice = selectedVoice,
                    onVoiceSelect = { voice -> viewModel.selectGeminiVoice(voice) },
                    isDirectTalkEnabled = isDirectTalkEnabled,
                    onToggleDirectTalk = { viewModel.toggleDirectTalk() },
                    isNativeAudioActive = isNativeAudioActive,
                    speechStatus = speechStatus,
                    onOpenSettings = { viewModel.setShowGeminiSettings(true) },
                    onOpenChooseVoice = { viewModel.setShowChooseVoiceDialog(true) },
                    onDismiss = { viewModel.dismissAssistant() },
                    onStartListening = { viewModel.voiceManager.startListening() },
                    onStopListening = { viewModel.voiceManager.stopListening() },
                    onLanguageSelect = { lang -> viewModel.setLanguage(lang) },
                    onSendTextMessage = { query -> viewModel.handleUserVoiceQuery(query) }
                )
            }

            // Default Assistant Setup Guide Dialog
            if (showSetupGuide) {
                DefaultAssistantGuideDialog(
                    onDismiss = { viewModel.setShowSetupGuide(false) },
                    onOpenAndroidSettings = { viewModel.hardwareController.openDefaultAssistantSettings() }
                )
            }

            // Full Google Gemini Settings Screen (User Screenshots 1 & 2)
            if (showGeminiSettings) {
                GeminiSettingsScreen(
                    settings = settingsState,
                    apiStatus = apiStatusMessage,
                    customApiKey = customApiKey,
                    onUpdateSettings = { transform -> viewModel.updateSettings(transform) },
                    onOpenChooseVoice = { viewModel.setShowChooseVoiceDialog(true) },
                    onVerifyApiKey = { key, cb -> viewModel.testCustomApiKey(key, cb) },
                    onBack = { viewModel.setShowGeminiSettings(false) }
                )
            }

            // Google Gemini "Choose a voice" Dialog (User Screenshot 3)
            if (showChooseVoiceDialog) {
                ChooseVoiceDialog(
                    currentVoice = selectedVoice,
                    onVoiceConfirmed = { voice -> viewModel.selectGeminiVoice(voice) },
                    onPreviewVoice = { voice -> viewModel.previewVoice(voice) },
                    onDismiss = { viewModel.setShowChooseVoiceDialog(false) }
                )
            }
        }
    }
}
