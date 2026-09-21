package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiAssistantService
import com.example.hardware.HardwareController
import com.example.model.AssistantAction
import com.example.model.AssistantLanguage
import com.example.model.BatteryInfo
import com.example.model.ConversationMessage
import com.example.model.GeminiSettingsState
import com.example.model.GeminiVoice
import com.example.model.Sender
import com.example.voice.VoiceInteractionManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NovaAssistantViewModel(application: Application) : AndroidViewModel(application) {

    val hardwareController = HardwareController(application)
    val geminiService = GeminiAssistantService()

    val voiceManager = VoiceInteractionManager(
        context = application,
        onSpeechRecognized = { query ->
            handleUserVoiceQuery(query)
        }
    )

    // Hardware Telemetry & State
    val batteryInfo: StateFlow<BatteryInfo> = hardwareController.batteryInfo
    val isFlashlightOn: StateFlow<Boolean> = hardwareController.isFlashlightOn
    val hasFlashlight: StateFlow<Boolean> = hardwareController.hasFlashlight
    val isWakeLockActive: StateFlow<Boolean> = hardwareController.isWakeLockActive

    // Voice & Interaction Flow
    val isListening: StateFlow<Boolean> = voiceManager.isListening
    val isSpeaking: StateFlow<Boolean> = voiceManager.isSpeaking
    val audioRms: StateFlow<Float> = voiceManager.audioRms
    val currentLanguage: StateFlow<AssistantLanguage> = voiceManager.currentLanguage
    val speechStatus: StateFlow<String?> = voiceManager.speechStatus
    val apiStatusMessage: StateFlow<String> = geminiService.apiStatusMessage

    private val _selectedGeminiVoice = MutableStateFlow(GeminiVoice.ORBIT)
    val selectedGeminiVoice: StateFlow<GeminiVoice> = _selectedGeminiVoice.asStateFlow()

    private val _settingsState = MutableStateFlow(GeminiSettingsState(selectedVoice = GeminiVoice.ORBIT))
    val settingsState: StateFlow<GeminiSettingsState> = _settingsState.asStateFlow()

    private val _showGeminiSettings = MutableStateFlow(false)
    val showGeminiSettings: StateFlow<Boolean> = _showGeminiSettings.asStateFlow()

    private val _showChooseVoiceDialog = MutableStateFlow(false)
    val showChooseVoiceDialog: StateFlow<Boolean> = _showChooseVoiceDialog.asStateFlow()

    private val _isDirectTalkEnabled = MutableStateFlow(true)
    val isDirectTalkEnabled: StateFlow<Boolean> = _isDirectTalkEnabled.asStateFlow()

    private val _isNativeAudioActive = MutableStateFlow(true)
    val isNativeAudioActive: StateFlow<Boolean> = _isNativeAudioActive.asStateFlow()

    private val _customApiKey = MutableStateFlow<String?>(null)
    val customApiKey: StateFlow<String?> = _customApiKey.asStateFlow()

    // Assistant UI State
    private val _isAssistantVisible = MutableStateFlow(false)
    val isAssistantVisible: StateFlow<Boolean> = _isAssistantVisible.asStateFlow()

    private val _isThinking = MutableStateFlow(false)
    val isThinking: StateFlow<Boolean> = _isThinking.asStateFlow()

    private val _conversation = MutableStateFlow<List<ConversationMessage>>(
        listOf(
            ConversationMessage(
                sender = Sender.NOVA,
                text = "Namaste! Main Nova AI Voice Assistant hoon, Gemini Brain aur natural voice ke saath. Mujhse boliye ya message likhiye!",
                isNativeAudio = true,
                voiceName = GeminiVoice.ORBIT.voiceName
            )
        )
    )
    val conversation: StateFlow<List<ConversationMessage>> = _conversation.asStateFlow()

    private val _lastActionFeedback = MutableStateFlow<String?>(null)
    val lastActionFeedback: StateFlow<String?> = _lastActionFeedback.asStateFlow()

    // OLED Always-On Display Lockscreen State
    private val _isAodLocked = MutableStateFlow(false)
    val isAodLocked: StateFlow<Boolean> = _isAodLocked.asStateFlow()

    // Default Assistant Setup Dialog
    private val _showSetupGuide = MutableStateFlow(false)
    val showSetupGuide: StateFlow<Boolean> = _showSetupGuide.asStateFlow()

    // Audible Countdown Timer State
    private val _timerRemainingSeconds = MutableStateFlow(0)
    val timerRemainingSeconds: StateFlow<Int> = _timerRemainingSeconds.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    private var timerJob: Job? = null

    // Power Button & Gesture state tracking
    private val _powerChargingProgress = MutableStateFlow(0f)
    val powerChargingProgress: StateFlow<Float> = _powerChargingProgress.asStateFlow()

    private var powerButtonPressStartTime = 0L
    private var lastPowerButtonTapTime = 0L
    private var powerPressJob: Job? = null

    private var lastPillTapTime = 0L

    init {
        val prefs = application.getSharedPreferences("nova_prefs", Context.MODE_PRIVATE)
        val savedKey = prefs.getString("gemini_api_key", null)
        if (!savedKey.isNullOrBlank()) {
            _customApiKey.value = savedKey
        }
    }

    fun onPowerButtonDown() {
        powerButtonPressStartTime = System.currentTimeMillis()
        powerPressJob?.cancel()
        powerPressJob = viewModelScope.launch {
            val totalDurationMs = 600L
            val stepMs = 20L
            var elapsed = 0L

            while (elapsed < totalDurationMs) {
                delay(stepMs)
                elapsed += stepMs
                val progress = (elapsed.toFloat() / totalDurationMs).coerceIn(0f, 1f)
                _powerChargingProgress.value = progress

                if (progress >= 1f) {
                    hardwareController.vibrateAssistantSummon()
                    summonAssistant(fromPowerButton = true)
                    _powerChargingProgress.value = 0f
                    break
                }
            }
        }
    }

    fun onPowerButtonUp() {
        powerPressJob?.cancel()
        _powerChargingProgress.value = 0f
        val pressDuration = System.currentTimeMillis() - powerButtonPressStartTime
        val now = System.currentTimeMillis()

        if (pressDuration < 400) {
            if (now - lastPowerButtonTapTime < 350) {
                hardwareController.launchCamera()
                hardwareController.vibrateDoublePulse()
                _lastActionFeedback.value = "Camera launched via Double Tap"
                lastPowerButtonTapTime = 0L
            } else {
                lastPowerButtonTapTime = now
            }
        }
    }

    fun onGesturePillLongPress() {
        hardwareController.vibrateClick()
        summonAssistant(fromGesture = true)
    }

    fun onGesturePillTap() {
        val now = System.currentTimeMillis()
        if (now - lastPillTapTime < 350) {
            hardwareController.vibrateClick()
            toggleFlashlight()
            lastPillTapTime = 0L
        } else {
            lastPillTapTime = now
            hardwareController.vibrateClick()
            if (_isAssistantVisible.value) {
                if (voiceManager.isListening.value) {
                    voiceManager.stopListening()
                } else {
                    voiceManager.startListening()
                }
            }
        }
    }

    fun summonAssistant(fromPowerButton: Boolean = false, fromGesture: Boolean = false) {
        if (_isAodLocked.value) {
            _isAodLocked.value = false
        }
        _isAssistantVisible.value = true
        _lastActionFeedback.value = if (fromPowerButton) "Summoned via Power Button Long Press" else if (fromGesture) "Summoned via Navigation Gesture Bar" else null
        voiceManager.startListening()
    }

    fun dismissAssistant() {
        _isAssistantVisible.value = false
        _isThinking.value = false
        voiceManager.stopListening()
        voiceManager.stopSpeaking()
        _lastActionFeedback.value = null
    }

    fun toggleAodLock(forceState: Boolean? = null) {
        val target = forceState ?: !_isAodLocked.value
        _isAodLocked.value = target
        if (target) {
            dismissAssistant()
        }
    }

    fun handleUserVoiceQuery(query: String) {
        if (query.isBlank()) return

        // Append user query to conversation
        val updated = _conversation.value.toMutableList()
        updated.add(ConversationMessage(sender = Sender.USER, text = query))
        _conversation.value = updated

        _isThinking.value = true

        viewModelScope.launch {
            // Process with Gemini Brain & Action Engine
            val response = geminiService.processQuery(
                history = _conversation.value,
                userInput = query,
                selectedVoice = _selectedGeminiVoice.value,
                customApiKey = _customApiKey.value,
                batteryPercent = batteryInfo.value.percentage,
                isCharging = batteryInfo.value.isCharging,
                isTorchOn = isFlashlightOn.value
            )

            _isThinking.value = false
            _isNativeAudioActive.value = response.isNativeAudio

            // Append Nova's reply
            val newConversation = _conversation.value.toMutableList()
            newConversation.add(
                ConversationMessage(
                    sender = Sender.NOVA,
                    text = response.spokenText,
                    actionLabel = response.action?.label,
                    isNativeAudio = response.isNativeAudio,
                    voiceName = response.voiceName ?: _selectedGeminiVoice.value.voiceName
                )
            )
            _conversation.value = newConversation

            // Execute Hardware / Intent action if triggered
            response.action?.let { action ->
                executeAction(action)
            }

            // Callback when voice playback finishes
            val onPlaybackFinished: () -> Unit = {
                if (_isDirectTalkEnabled.value && _isAssistantVisible.value && !_isAodLocked.value) {
                    voiceManager.startListening()
                }
            }

            // Stream Gemini trained model native audio if available; otherwise play acoustic feedback!
            if (response.isNativeAudio && !response.audioBase64.isNullOrBlank()) {
                voiceManager.playGeminiNativeAudio(
                    audioBase64 = response.audioBase64,
                    mimeType = response.audioMimeType,
                    onFinished = onPlaybackFinished
                )
            } else {
                voiceManager.playAcousticFeedback(
                    tone = com.example.voice.FeedbackTone.ACKNOWLEDGEMENT,
                    onFinished = onPlaybackFinished
                )
            }
        }
    }

    private fun containsHindiOrHinglish(text: String): Boolean {
        val lower = text.lowercase()
        val hindiKeywords = listOf(
            "namaste", "aap", "mera", "meri", "hai", "kya", "kaise", "karo", "kholo", "chalu", "band",
            "batao", "sun", "kaam", "nahi", "haan", "theek", "shukriya", "dhanyawad", "samajh", "bhi"
        )
        return text.any { it in '\u0900'..'\u097F' } || hindiKeywords.any { lower.contains(it) }
    }

    fun selectGeminiVoice(voice: GeminiVoice) {
        _selectedGeminiVoice.value = voice
        _settingsState.value = _settingsState.value.copy(selectedVoice = voice)
        hardwareController.vibrateClick()
    }

    fun previewVoice(voice: GeminiVoice) {
        _selectedGeminiVoice.value = voice
        hardwareController.vibrateClick()
        viewModelScope.launch {
            val previewAudio = geminiService.generateVoicePreviewAudio(voice, _customApiKey.value)
            if (previewAudio != null) {
                voiceManager.playGeminiNativeAudio(
                    audioBase64 = previewAudio.first,
                    mimeType = previewAudio.second,
                    onFinished = {}
                )
            } else {
                voiceManager.playAcousticFeedback(com.example.voice.FeedbackTone.ACKNOWLEDGEMENT)
            }
        }
    }

    fun toggleDirectTalk() {
        _isDirectTalkEnabled.value = !_isDirectTalkEnabled.value
        hardwareController.vibrateClick()
    }

    fun setCustomApiKey(key: String) {
        val trimmed = key.trim()
        _customApiKey.value = trimmed.ifEmpty { null }
        val prefs = getApplication<Application>().getSharedPreferences("nova_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("gemini_api_key", trimmed).apply()
        hardwareController.vibrateClick()
    }

    fun testCustomApiKey(key: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = geminiService.testApiKey(key)
            if (result.first) {
                setCustomApiKey(key)
            }
            onResult(result.first, result.second)
        }
    }

    fun setShowGeminiSettings(show: Boolean) {
        _showGeminiSettings.value = show
        hardwareController.vibrateClick()
    }

    fun setShowChooseVoiceDialog(show: Boolean) {
        _showChooseVoiceDialog.value = show
        hardwareController.vibrateClick()
    }

    fun updateSettings(transform: (GeminiSettingsState) -> GeminiSettingsState) {
        val updated = transform(_settingsState.value)
        _settingsState.value = updated
        voiceManager.interruptLiveResponses = updated.interruptLiveResponses
        hardwareController.vibrateClick()
    }

    fun executeAction(action: AssistantAction) {
        _lastActionFeedback.value = action.label
        when (action) {
            is AssistantAction.Flashlight -> {
                hardwareController.toggleFlashlight(forceState = action.turnOn)
            }
            is AssistantAction.Call -> {
                hardwareController.launchPhoneDialer(action.targetNameOrNumber)
            }
            is AssistantAction.OpenCamera -> {
                hardwareController.launchCamera()
            }
            is AssistantAction.StartTimer -> {
                startTimer(action.seconds)
            }
            is AssistantAction.YouTubeSearch -> {
                hardwareController.launchYouTube(action.query)
            }
            is AssistantAction.WebSearch -> {
                hardwareController.launchGoogleSearch(action.query)
            }
            is AssistantAction.OpenWhatsApp -> {
                hardwareController.launchWhatsApp(message = action.query)
            }
            is AssistantAction.SendSms -> {
                hardwareController.launchSms(message = action.phone)
            }
            is AssistantAction.CheckBattery -> {
                hardwareController.vibrateClick()
            }
            else -> {}
        }
    }

    fun toggleFlashlight() {
        hardwareController.toggleFlashlight()
    }

    fun toggleWakeLock(activity: android.app.Activity? = null) {
        hardwareController.toggleWakeLock(activity)
    }

    fun startTimer(seconds: Int) {
        timerJob?.cancel()
        _timerRemainingSeconds.value = seconds
        _isTimerRunning.value = true
        _lastActionFeedback.value = "Timer started for $seconds seconds"

        timerJob = viewModelScope.launch {
            while (_timerRemainingSeconds.value > 0) {
                delay(1000)
                _timerRemainingSeconds.value -= 1
            }
            _isTimerRunning.value = false
            hardwareController.playAudibleAlarmChime()
            hardwareController.vibrateDoublePulse()
            _lastActionFeedback.value = "⏰ Timer Finished!"
            val apiKey = geminiService.resolveApiKey(_customApiKey.value)
            val ttsAudio = geminiService.callGeminiTtsAudio(
                apiKey = apiKey,
                textToSpeak = "Aapka timer pura ho gaya hai!",
                voiceName = geminiService.mapVoiceToTtsName(_selectedGeminiVoice.value)
            )
            if (ttsAudio != null) {
                voiceManager.playGeminiNativeAudio(ttsAudio.first, ttsAudio.second) {}
            } else {
                voiceManager.playAcousticFeedback(com.example.voice.FeedbackTone.SUCCESS)
            }
        }
    }

    fun cancelTimer() {
        timerJob?.cancel()
        _isTimerRunning.value = false
        _timerRemainingSeconds.value = 0
        _lastActionFeedback.value = "Timer cancelled"
        hardwareController.vibrateClick()
    }

    fun setLanguage(language: AssistantLanguage) {
        voiceManager.setLanguage(language)
        _settingsState.value = _settingsState.value.copy(language = language)
        hardwareController.vibrateClick()
    }

    fun setShowSetupGuide(show: Boolean) {
        _showSetupGuide.value = show
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        powerPressJob?.cancel()
        voiceManager.cleanup()
    }
}
