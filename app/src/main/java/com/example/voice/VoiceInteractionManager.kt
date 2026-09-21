package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.example.model.AssistantLanguage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Pure voice interaction controller powered exclusively by Gemini Trained Human Voice Model
 * and native hardware microphone recognition.
 *
 * Old Android TextToSpeech (TTS) is completely removed per user instruction.
 */
class VoiceInteractionManager(
    private val context: Context,
    private val onSpeechRecognized: (String) -> Unit
) {

    private var speechRecognizer: SpeechRecognizer? = null
    private val geminiAudioPlayer = GeminiNativeAudioPlayer(context)
    private val scope = CoroutineScope(Dispatchers.Main)
    private var simulatedRmsJob: Job? = null

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _audioRms = MutableStateFlow(0f)
    val audioRms: StateFlow<Float> = _audioRms.asStateFlow()

    private val _speechStatus = MutableStateFlow<String?>(null)
    val speechStatus: StateFlow<String?> = _speechStatus.asStateFlow()

    private val _currentLanguage = MutableStateFlow(AssistantLanguage.AUTO)
    val currentLanguage: StateFlow<AssistantLanguage> = _currentLanguage.asStateFlow()

    var interruptLiveResponses: Boolean = true

    init {
        initSpeechRecognizer()
    }

    private fun initSpeechRecognizer() {
        try {
            speechRecognizer?.destroy()
        } catch (_: Exception) {}

        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            try {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(object : RecognitionListener {
                        override fun onReadyForSpeech(params: Bundle?) {
                            _isListening.value = true
                            _speechStatus.value = "Listening... Speak now"
                        }

                        override fun onBeginningOfSpeech() {
                            if (interruptLiveResponses) {
                                stopSpeaking()
                            }
                            _isListening.value = true
                            _speechStatus.value = "Hearing you..."
                        }

                        override fun onRmsChanged(rmsdB: Float) {
                            val normalized = ((rmsdB + 2f) / 12f).coerceIn(0.1f, 1f)
                            _audioRms.value = normalized
                        }

                        override fun onBufferReceived(buffer: ByteArray?) {}

                        override fun onEndOfSpeech() {
                            _isListening.value = false
                            _audioRms.value = 0f
                            _speechStatus.value = "Processing..."
                        }

                        override fun onError(error: Int) {
                            _isListening.value = false
                            _audioRms.value = 0f
                            val message = when (error) {
                                SpeechRecognizer.ERROR_NO_MATCH -> "Didn't catch that. Tap mic or type below."
                                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Listening timed out. Tap mic to speak."
                                SpeechRecognizer.ERROR_AUDIO -> "Audio error. Check microphone."
                                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required."
                                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> {
                                    initSpeechRecognizer()
                                    "Ready. Tap mic to speak."
                                }
                                SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network connection issue."
                                else -> "Tap mic or type a message."
                            }
                            _speechStatus.value = message
                        }

                        override fun onResults(results: Bundle?) {
                            _isListening.value = false
                            _audioRms.value = 0f
                            _speechStatus.value = null
                            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            val text = matches?.firstOrNull() ?: ""
                            if (text.isNotBlank()) {
                                onSpeechRecognized(text)
                            }
                        }

                        override fun onPartialResults(partialResults: Bundle?) {
                            if (interruptLiveResponses) {
                                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                                if (!matches.isNullOrEmpty()) {
                                    stopSpeaking()
                                }
                            }
                        }

                        override fun onEvent(eventType: Int, params: Bundle?) {}
                    })
                }
            } catch (e: Exception) {
                speechRecognizer = null
            }
        }
    }

    fun startListening(language: AssistantLanguage = _currentLanguage.value) {
        stopSpeaking()

        if (speechRecognizer == null) {
            initSpeechRecognizer()
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            when (language) {
                AssistantLanguage.HINDI -> {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
                    putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf("hi-IN", "en-IN"))
                }
                AssistantLanguage.ENGLISH -> {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-IN")
                    putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf("en-US", "hi-IN"))
                }
                AssistantLanguage.AUTO -> {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
                    putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf("hi-IN", "en-IN", "en-US"))
                }
            }
        }

        try {
            _speechStatus.value = "Listening..."
            speechRecognizer?.startListening(intent)
            _isListening.value = true
        } catch (e: Exception) {
            _isListening.value = false
            _speechStatus.value = "Mic unavailable. Type your message below."
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (_: Exception) {}
        _isListening.value = false
        _audioRms.value = 0f
    }

    fun setLanguage(language: AssistantLanguage) {
        _currentLanguage.value = language
    }

    /**
     * Play Gemini Live / Trained Model Native Human Voice directly to speaker via PCM AudioTrack.
     */
    fun playGeminiNativeAudio(
        audioBase64: String,
        mimeType: String?,
        onFinished: () -> Unit
    ) {
        stopSpeaking()

        _isSpeaking.value = true
        scope.launch {
            geminiAudioPlayer.playAudio(
                base64Data = audioBase64,
                mimeType = mimeType,
                onProgressRms = { rms ->
                    _audioRms.value = rms
                },
                onFinished = {
                    _isSpeaking.value = false
                    _audioRms.value = 0f
                    onFinished()
                }
            )
        }
    }

    /**
     * Play native acoustic chime feedback directly via hardware AudioTrack (no robotic TTS).
     */
    fun playAcousticFeedback(
        tone: FeedbackTone,
        onFinished: () -> Unit = {}
    ) {
        stopSpeaking()
        _isSpeaking.value = true
        scope.launch {
            geminiAudioPlayer.playAcousticFeedback(
                type = tone,
                onProgressRms = { rms ->
                    _audioRms.value = rms
                },
                onFinished = {
                    _isSpeaking.value = false
                    _audioRms.value = 0f
                    onFinished()
                }
            )
        }
    }

    /**
     * Barge-in: Discards current audio buffer immediately.
     */
    fun stopSpeaking() {
        geminiAudioPlayer.stop()
        simulatedRmsJob?.cancel()
        simulatedRmsJob = null
        _isSpeaking.value = false
        _audioRms.value = 0f
    }

    fun cleanup() {
        stopSpeaking()
        try {
            speechRecognizer?.destroy()
        } catch (_: Exception) {}
        geminiAudioPlayer.cleanup()
    }
}
