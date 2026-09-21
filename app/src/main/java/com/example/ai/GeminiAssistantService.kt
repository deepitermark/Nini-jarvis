package com.example.ai

import android.util.Log
import com.example.BuildConfig
import com.example.model.AssistantAction
import com.example.model.ConversationMessage
import com.example.model.GeminiVoice
import com.example.model.Sender
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

data class AssistantResponse(
    val spokenText: String,
    val action: AssistantAction?,
    val audioBase64: String? = null,
    val audioMimeType: String? = null,
    val isNativeAudio: Boolean = false,
    val voiceName: String? = null,
    val errorMessage: String? = null
)

/**
 * Gemini AI Intelligence and Trained Human Voice Engine.
 * Combines Gemini 3.5 Flash Brain for conversational intelligence and device actions
 * with Gemini 2.5 Flash Preview TTS for trained human neural voice synthesis.
 */
class GeminiAssistantService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private val _apiStatusMessage = MutableStateFlow<String>("Gemini Trained Voice Ready")
    val apiStatusMessage: StateFlow<String> = _apiStatusMessage.asStateFlow()

    companion object {
        const val MODEL_TEXT_BRAIN = "gemini-3.5-flash"
        const val MODEL_TTS = "gemini-2.5-flash-preview-tts"
    }

    suspend fun testApiKey(keyToTest: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val key = keyToTest.trim()
        if (key.isBlank()) {
            return@withContext Pair(false, "API Key cannot be empty.")
        }
        try {
            val testBody = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", "Hi") })
                        })
                    })
                })
            }
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_TEXT_BRAIN:generateContent?key=$key"
            val request = Request.Builder()
                .url(url)
                .post(testBody.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""

            if (response.isSuccessful) {
                _apiStatusMessage.value = "Connected to Gemini 3.5 Flash & Native Voice"
                return@withContext Pair(true, "API Key Verified! Connected to Gemini.")
            } else {
                val errorMsg = parseErrorMessage(body, response.code)
                _apiStatusMessage.value = "Error: $errorMsg"
                return@withContext Pair(false, errorMsg)
            }
        } catch (e: Exception) {
            val msg = e.localizedMessage ?: "Network connection error"
            _apiStatusMessage.value = "Connection error: $msg"
            return@withContext Pair(false, msg)
        }
    }

    /**
     * Maps user-facing GeminiVoice to the prebuilt voice names supported by Gemini TTS.
     */
    fun mapVoiceToTtsName(voice: GeminiVoice): String {
        return when (voice) {
            GeminiVoice.ORBIT -> "aoede"
            GeminiVoice.FLARE -> "puck"
            GeminiVoice.GLOW -> "kore"
            GeminiVoice.CAPELLA -> "fenrir"
            GeminiVoice.ORION -> "charon"
            GeminiVoice.URSA -> "despina"
            GeminiVoice.ECLIPSE -> "orus"
            GeminiVoice.VEGA -> "leda"
            GeminiVoice.DIPPER -> "zephyrus"
            GeminiVoice.AOEDE -> "aoede"
            GeminiVoice.KORE -> "kore"
            GeminiVoice.PUCK -> "puck"
            GeminiVoice.CHARON -> "charon"
            GeminiVoice.FENRIR -> "fenrir"
        }
    }

    suspend fun processQuery(
        history: List<ConversationMessage>,
        userInput: String,
        selectedVoice: GeminiVoice = GeminiVoice.ORBIT,
        customApiKey: String? = null,
        batteryPercent: Int,
        isCharging: Boolean,
        isTorchOn: Boolean
    ): AssistantResponse = withContext(Dispatchers.IO) {
        val trimmed = userInput.trim()

        // 1. Fast local action parsing for instantaneous device command detection
        val localAction = parseLocalAction(trimmed)

        // 2. Resolve Gemini API Key (Secrets panel, environment, or custom user input)
        val apiKey = resolveApiKey(customApiKey)
        val ttsVoiceName = mapVoiceToTtsName(selectedVoice)

        var spokenText: String
        var resolvedAction: AssistantAction? = localAction

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            // Local fallback brain
            val offline = generateOfflineResponse(
                history = history,
                input = trimmed,
                action = localAction,
                batteryPercent = batteryPercent,
                isCharging = isCharging,
                isTorchOn = isTorchOn
            )
            spokenText = offline.spokenText
            resolvedAction = offline.action
        } else {
            // Call Gemini 3.5 Flash Brain for smart contextual understanding
            val systemPrompt = """
                You are Nova, an advanced native Android AI Voice Assistant with a warm, natural human voice and ultra-intelligent brain.
                You speak fluent Hindi, Hinglish, and English.
                Always reply in the EXACT SAME language the user used (Hindi, Hinglish, or English).
                Keep spoken replies concise: exactly 1-2 friendly, conversational, and direct sentences.
                Device context: Battery: $batteryPercent% (${if (isCharging) "Charging" else "On battery"}), Torch: ${if (isTorchOn) "ON" else "OFF"}.
                
                Action codes to append at the very end of your response if asked to perform device commands:
                - [ACTION:FLASHLIGHT_ON]
                - [ACTION:FLASHLIGHT_OFF]
                - [ACTION:CALL:name_or_number]
                - [ACTION:CAMERA]
                - [ACTION:TIMER:seconds]
                - [ACTION:YOUTUBE:query]
                - [ACTION:GOOGLE:query]
                - [ACTION:WHATSAPP:query]
                - [ACTION:BATTERY]
            """.trimIndent()

            val brainResult = callGeminiTextBrain(
                apiKey = apiKey,
                systemPrompt = systemPrompt,
                history = history,
                userInput = trimmed,
                localAction = localAction,
                selectedVoice = selectedVoice
            )

            if (brainResult != null) {
                spokenText = brainResult.spokenText
                resolvedAction = brainResult.action ?: localAction
            } else {
                val offline = generateOfflineResponse(
                    history = history,
                    input = trimmed,
                    action = localAction,
                    batteryPercent = batteryPercent,
                    isCharging = isCharging,
                    isTorchOn = isTorchOn
                )
                spokenText = offline.spokenText
                resolvedAction = offline.action
            }
        }

        // 3. Generate Trained Model Human Voice via Gemini TTS (gemini-2.5-flash-preview-tts)
        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            val audioData = callGeminiTtsAudio(
                apiKey = apiKey,
                textToSpeak = spokenText,
                voiceName = ttsVoiceName
            )

            if (audioData != null) {
                _apiStatusMessage.value = "Gemini Native Audio • ${selectedVoice.displayName}"
                return@withContext AssistantResponse(
                    spokenText = spokenText,
                    action = resolvedAction,
                    audioBase64 = audioData.first,
                    audioMimeType = audioData.second,
                    isNativeAudio = true,
                    voiceName = selectedVoice.displayName
                )
            }
        }

        // Fallback response without audio buffer
        return@withContext AssistantResponse(
            spokenText = spokenText,
            action = resolvedAction,
            isNativeAudio = false,
            voiceName = selectedVoice.displayName
        )
    }

    /**
     * Synthesizes audio using Gemini's trained neural speech model (gemini-2.5-flash-preview-tts).
     * Returns Pair(base64Data, mimeType).
     */
    suspend fun callGeminiTtsAudio(
        apiKey: String,
        textToSpeak: String,
        voiceName: String
    ): Pair<String, String>? = withContext(Dispatchers.IO) {
        if (textToSpeak.isBlank()) return@withContext null

        try {
            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", textToSpeak.take(500)) })
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("responseModalities", JSONArray().apply {
                        put("AUDIO")
                    })
                    put("speechConfig", JSONObject().apply {
                        put("voiceConfig", JSONObject().apply {
                            put("prebuiltVoiceConfig", JSONObject().apply {
                                put("voiceName", voiceName.lowercase())
                            })
                        })
                    })
                })
            }

            val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_TTS:generateContent?key=$apiKey"
            val request = Request.Builder()
                .url(endpoint)
                .post(requestJson.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val bodyString = response.body?.string() ?: ""

            if (!response.isSuccessful || bodyString.isEmpty()) {
                Log.w("GeminiTTS", "TTS error code ${response.code}: $bodyString")
                return@withContext null
            }

            val root = JSONObject(bodyString)
            val candidates = root.optJSONArray("candidates") ?: return@withContext null
            val candidate = candidates.optJSONObject(0) ?: return@withContext null
            val content = candidate.optJSONObject("content") ?: return@withContext null
            val parts = content.optJSONArray("parts") ?: return@withContext null

            for (i in 0 until parts.length()) {
                val part = parts.optJSONObject(i) ?: continue
                val inlineData = part.optJSONObject("inlineData")
                if (inlineData != null) {
                    val data = inlineData.optString("data", "")
                    val mime = inlineData.optString("mimeType", "audio/pcm;rate=24000")
                    if (data.isNotBlank()) {
                        return@withContext Pair(data, mime)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("GeminiTTS", "Failed to generate TTS audio: ${e.message}")
        }
        return@withContext null
    }

    /**
     * Preview sample voice for the "Choose a Voice" Dialog.
     */
    suspend fun generateVoicePreviewAudio(
        voice: GeminiVoice,
        customApiKey: String? = null
    ): Pair<String, String>? = withContext(Dispatchers.IO) {
        val apiKey = resolveApiKey(customApiKey)
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") return@withContext null

        val sampleText = when (voice) {
            GeminiVoice.ORBIT -> "Namaste, main Orbit hoon. Calm aur friendly voice ke saath."
            GeminiVoice.FLARE -> "Hello! Main Flare hoon, energetic aur upbeat companion."
            GeminiVoice.GLOW -> "Namaste, main Glow hoon. Ekdum shant aur soothing tone."
            GeminiVoice.CAPELLA -> "Hello! Main Capella hoon. Clear, crisp aur professional."
            GeminiVoice.ORION -> "Namaste, main Orion hoon. Deep aur confident tone."
            else -> "Namaste, main ${voice.displayName} hoon, aapka Gemini native voice assistant."
        }

        return@withContext callGeminiTtsAudio(
            apiKey = apiKey,
            textToSpeak = sampleText,
            voiceName = mapVoiceToTtsName(voice)
        )
    }

    private fun callGeminiTextBrain(
        apiKey: String,
        systemPrompt: String,
        history: List<ConversationMessage>,
        userInput: String,
        localAction: AssistantAction?,
        selectedVoice: GeminiVoice
    ): AssistantResponse? {
        try {
            val contentsArray = buildContentsJsonArray(history, userInput)

            val requestJson = JSONObject().apply {
                put("contents", contentsArray)
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", systemPrompt) })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                    put("maxOutputTokens", 350)
                })
            }

            val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_TEXT_BRAIN:generateContent?key=$apiKey"
            val request = Request.Builder()
                .url(endpoint)
                .post(requestJson.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val bodyString = response.body?.string() ?: ""

            if (!response.isSuccessful || bodyString.isEmpty()) {
                val errorMsg = parseErrorMessage(bodyString, response.code)
                _apiStatusMessage.value = "API Error: $errorMsg"
                return null
            }

            val root = JSONObject(bodyString)
            val candidates = root.optJSONArray("candidates") ?: return null
            val firstCandidate = candidates.optJSONObject(0) ?: return null
            val content = firstCandidate.optJSONObject("content") ?: return null
            val parts = content.optJSONArray("parts") ?: return null
            val textPart = parts.optJSONObject(0)?.optString("text", "") ?: ""

            if (textPart.isNotBlank()) {
                val parsed = extractActionFromText(textPart, localAction)
                return AssistantResponse(
                    spokenText = parsed.spokenText,
                    action = parsed.action,
                    isNativeAudio = false,
                    voiceName = selectedVoice.displayName
                )
            }
        } catch (e: Exception) {
            Log.e("GeminiService", "Text brain error: ${e.localizedMessage}")
        }
        return null
    }

    private fun parseErrorMessage(bodyString: String, responseCode: Int): String {
        try {
            if (bodyString.isNotBlank()) {
                val obj = JSONObject(bodyString)
                val err = obj.optJSONObject("error")
                if (err != null) {
                    val msg = err.optString("message", "")
                    val status = err.optString("status", "")
                    return when {
                        msg.contains("API_KEY_INVALID", ignoreCase = true) || responseCode == 400 -> "Invalid API Key"
                        status == "PERMISSION_DENIED" || responseCode == 403 -> "Permission Denied / Quota Exceeded"
                        responseCode == 404 -> "Model not found"
                        else -> msg.take(80)
                    }
                }
            }
        } catch (_: Exception) {}
        return "HTTP Error $responseCode"
    }

    private fun buildContentsJsonArray(history: List<ConversationMessage>, currentInput: String): JSONArray {
        val array = JSONArray()
        val recentHistory = history.takeLast(8).filter { it.text.isNotBlank() }

        var expectedRole = "user"
        for (msg in recentHistory) {
            val msgRole = if (msg.sender == Sender.USER) "user" else "model"
            if (msgRole == expectedRole) {
                array.put(JSONObject().apply {
                    put("role", msgRole)
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", msg.text) })
                    })
                })
                expectedRole = if (expectedRole == "user") "model" else "user"
            }
        }

        array.put(JSONObject().apply {
            put("role", "user")
            put("parts", JSONArray().apply {
                put(JSONObject().apply { put("text", currentInput) })
            })
        })

        return array
    }

    private fun extractActionFromText(rawText: String, fallbackAction: AssistantAction?): AssistantResponse {
        var cleanText = rawText
        var extractedAction: AssistantAction? = fallbackAction

        val actionPattern = Pattern.compile("\\[ACTION:([A-Z_]+)(?::([^\\]]+))?\\]")
        val matcher = actionPattern.matcher(rawText)

        if (matcher.find()) {
            val actionType = matcher.group(1) ?: ""
            val param = matcher.group(2)?.trim() ?: ""
            cleanText = rawText.replace(matcher.group(0) ?: "", "").trim()

            extractedAction = when (actionType) {
                "FLASHLIGHT_ON" -> AssistantAction.Flashlight(true)
                "FLASHLIGHT_OFF" -> AssistantAction.Flashlight(false)
                "CALL" -> AssistantAction.Call(param.ifEmpty { "Contacts" })
                "CAMERA" -> AssistantAction.OpenCamera
                "TIMER" -> {
                    val sec = param.toIntOrNull() ?: 60
                    AssistantAction.StartTimer(sec)
                }
                "YOUTUBE" -> AssistantAction.YouTubeSearch(param.ifEmpty { "Songs" })
                "GOOGLE" -> AssistantAction.WebSearch(param)
                "WHATSAPP" -> AssistantAction.OpenWhatsApp(param)
                "BATTERY" -> AssistantAction.CheckBattery
                else -> fallbackAction
            }
        }

        return AssistantResponse(
            spokenText = cleanText.ifEmpty { "Ji, main Nova aapke sath hoon." },
            action = extractedAction
        )
    }

    fun parseLocalAction(input: String): AssistantAction? {
        val lower = input.lowercase()

        // Flashlight / Torch
        if (lower.contains("flashlight on") || lower.contains("torch on") ||
            lower.contains("light on") || lower.contains("torch jalao") ||
            lower.contains("flashlight jalao") || lower.contains("flashlight chalu") ||
            lower.contains("torch chalu") || lower.contains("light chalu")
        ) {
            return AssistantAction.Flashlight(true)
        }
        if (lower.contains("flashlight off") || lower.contains("torch off") ||
            lower.contains("light off") || lower.contains("torch band") ||
            lower.contains("flashlight band") || lower.contains("light bujhao") ||
            lower.contains("light band")
        ) {
            return AssistantAction.Flashlight(false)
        }

        // Camera
        if (lower.contains("camera") && (lower.contains("open") || lower.contains("kholo") || lower.contains("chalu") || lower.contains("photo"))) {
            return AssistantAction.OpenCamera
        }

        // Phone call
        if (lower.startsWith("call") || lower.contains("ko phone") || lower.contains("ko call") || lower.contains("dial")) {
            val target = lower
                .replace("call", "")
                .replace("ko phone", "")
                .replace("ko call", "")
                .replace("lagao", "")
                .replace("dial", "")
                .trim()
            return AssistantAction.Call(target.ifEmpty { "Contacts" })
        }

        // Timer
        if (lower.contains("timer")) {
            val numPattern = Pattern.compile("(\\d+)")
            val numMatcher = numPattern.matcher(lower)
            var seconds = 60
            if (numMatcher.find()) {
                val value = numMatcher.group(1)?.toIntOrNull() ?: 1
                seconds = if (lower.contains("minute") || lower.contains("min")) {
                    value * 60
                } else {
                    value
                }
            }
            return AssistantAction.StartTimer(seconds)
        }

        // YouTube
        if (lower.contains("youtube") || lower.contains("gaana") || lower.contains("song") || lower.contains("video")) {
            val query = lower
                .replace("youtube", "")
                .replace("par", "")
                .replace("play", "")
                .replace("chalao", "")
                .replace("kholo", "")
                .trim()
            return AssistantAction.YouTubeSearch(query.ifEmpty { "Latest songs" })
        }

        // WhatsApp
        if (lower.contains("whatsapp")) {
            val query = lower.replace("whatsapp", "").replace("par", "").replace("kholo", "").trim()
            return AssistantAction.OpenWhatsApp(query)
        }

        // Web Search
        if (lower.contains("google") || lower.contains("search") || lower.contains("kya hai") || lower.contains("meaning")) {
            val query = lower
                .replace("google", "")
                .replace("search", "")
                .replace("karo", "")
                .replace("batao", "")
                .trim()
            return AssistantAction.WebSearch(query)
        }

        // Battery telemetry
        if (lower.contains("battery") || lower.contains("charge") || lower.contains("charging")) {
            return AssistantAction.CheckBattery
        }

        return null
    }

    private fun generateOfflineResponse(
        history: List<ConversationMessage>,
        input: String,
        action: AssistantAction?,
        batteryPercent: Int,
        isCharging: Boolean,
        isTorchOn: Boolean
    ): AssistantResponse {
        val lower = input.lowercase()

        // Immediate Action Feedback
        if (action != null) {
            return when (action) {
                is AssistantAction.Flashlight -> {
                    val status = if (action.turnOn) "Flashlight chalu kar di hai." else "Flashlight band kar di hai."
                    AssistantResponse(status, action)
                }
                is AssistantAction.Call -> {
                    AssistantResponse("${action.targetNameOrNumber} ko call mila raha hoon.", action)
                }
                is AssistantAction.OpenCamera -> {
                    AssistantResponse("Camera open kar diya hai.", action)
                }
                is AssistantAction.StartTimer -> {
                    val mins = action.seconds / 60
                    val secs = action.seconds % 60
                    val timeStr = if (mins > 0) "$mins minute" else "$secs second"
                    AssistantResponse("$timeStr ka timer chalu kar diya hai.", action)
                }
                is AssistantAction.YouTubeSearch -> {
                    AssistantResponse("YouTube par ${action.query} open kar raha hoon.", action)
                }
                is AssistantAction.WebSearch -> {
                    AssistantResponse("Google par ${action.query} search kar raha hoon.", action)
                }
                is AssistantAction.OpenWhatsApp -> {
                    AssistantResponse("WhatsApp khol raha hoon.", action)
                }
                is AssistantAction.CheckBattery -> {
                    val status = if (isCharging) "charging par hai" else "battery par chal raha hai"
                    AssistantResponse("Aapke phone ki battery $batteryPercent% hai aur device $status.", action)
                }
                else -> AssistantResponse("Ji, kaam ho gaya hai.", action)
            }
        }

        // Conversational intelligence
        val responseText = when {
            lower.contains("mera naam") && (lower.contains("kya") || lower.contains("batao")) -> {
                val foundName = findNameInHistory(history)
                if (foundName != null) "Aapka naam $foundName hai! Main sab yaad rakhta hoon."
                else "Aapne abhi tak apna naam nahi bataya. Aapka kya naam hai?"
            }
            lower.contains("mera naam") || lower.contains("my name is") -> {
                val name = lower.replace("mera naam", "").replace("hai", "").replace("my name is", "").trim()
                "Namaste $name! Main aapka naam yaad rakhunga. Bataiye aaj kya madad karoon?"
            }
            lower.contains("namaste") || lower.contains("hello") || lower.contains("hi") -> {
                "Namaste! Main Nova AI Voice Assistant hoon. Main direct baatein kar sakta hoon aur Flashlight, Camera, Call, ya Timer lagane jaise sare kaam kar sakta hoon."
            }
            lower.contains("kya haal hai") || lower.contains("how are you") || lower.contains("kaise ho") -> {
                "Main bilkul badhiya hoon aur aapki sewa ke liye tayar hoon! Aap bataiye, aaj kya plan hai?"
            }
            lower.contains("tum kaun ho") || lower.contains("who are you") || lower.contains("kya kar sakte ho") -> {
                "Main Nova Voice Assistant hoon, Gemini Trained Human Voice Model aur powerful brain ke saath. Aap mujhse direct baatein kar sakte hain ya device controls bol sakte hain."
            }
            lower.contains("dhanyawad") || lower.contains("shukriya") || lower.contains("thank") -> {
                "Aapka swagat hai! Kabhi bhi koi kaam ho to power button dabayein ya pill par long press karein."
            }
            lower.contains("mausam") || lower.contains("weather") -> {
                "Aaj ka mausam suhana hai! Detail dekhne ke liye 'Google weather' bol sakte hain."
            }
            else -> {
                "Ji, main Nova hoon. Main Flashlight on karne, Call lagane, Timer set karne, ya kisi bhi sawal ka jawab dene ke liye tayar hoon."
            }
        }

        return AssistantResponse(responseText, null)
    }

    private fun findNameInHistory(history: List<ConversationMessage>): String? {
        for (msg in history.reversed()) {
            if (msg.sender == Sender.USER) {
                val text = msg.text.lowercase()
                if (text.contains("mera naam") || text.contains("my name is")) {
                    val clean = text.replace("mera naam", "").replace("hai", "").replace("my name is", "").trim()
                    if (clean.isNotBlank()) return clean.replaceFirstChar { it.uppercase() }
                }
            }
        }
        return null
    }

    fun resolveApiKey(customKey: String?): String {
        if (!customKey.isNullOrBlank() && customKey.trim().length > 10 && customKey.trim() != "MY_GEMINI_API_KEY") {
            return customKey.trim()
        }
        val envKey = System.getenv("GEMINI_API_KEY")
        if (!envKey.isNullOrBlank() && envKey.trim().length > 10 && envKey.trim() != "MY_GEMINI_API_KEY") {
            return envKey.trim()
        }
        try {
            val buildKey = BuildConfig.GEMINI_API_KEY
            if (buildKey.isNotBlank() && buildKey != "MY_GEMINI_API_KEY") {
                return buildKey.trim()
            }
        } catch (_: Exception) {}
        return ""
    }
}
