package com.example.model

data class BatteryInfo(
    val percentage: Int = 100,
    val isCharging: Boolean = false,
    val chargeSource: String = "Battery",
    val temperature: Float = 28.0f,
    val voltage: Int = 4100
)

enum class AssistantLanguage(val displayName: String, val speechCode: String) {
    AUTO("Auto (Hindi/Eng)", "hi-IN"),
    HINDI("हिंदी (Hindi)", "hi-IN"),
    ENGLISH("English (India)", "en-IN")
}

sealed class AssistantAction(val label: String) {
    data class Flashlight(val turnOn: Boolean) : AssistantAction(if (turnOn) "Flashlight ON" else "Flashlight OFF")
    data class Call(val targetNameOrNumber: String) : AssistantAction("Call: $targetNameOrNumber")
    object OpenCamera : AssistantAction("Open Camera")
    data class YouTubeSearch(val query: String) : AssistantAction("YouTube: $query")
    data class WebSearch(val query: String) : AssistantAction("Google: $query")
    data class StartTimer(val seconds: Int) : AssistantAction("Timer: ${seconds}s")
    data class OpenWhatsApp(val query: String = "") : AssistantAction("WhatsApp")
    data class SendSms(val phone: String = "") : AssistantAction("SMS")
    object CheckBattery : AssistantAction("Battery Telemetry")
    data class GeneralReply(val text: String) : AssistantAction("Nova Response")
}

enum class GeminiVoice(
    val voiceName: String,
    val displayName: String,
    val personaDescription: String,
    val pitch: Float = 1.0f,
    val speed: Float = 1.0f
) {
    ORBIT("Aoede", "Orbit", "Calm and conversational, friendly companion", pitch = 1.05f, speed = 1.0f),
    FLARE("Puck", "Flare", "Upbeat and radiant, enthusiastic tone", pitch = 1.15f, speed = 1.05f),
    GLOW("Kore", "Glow", "Warm, soothing and gentle tone", pitch = 0.95f, speed = 0.95f),
    CAPELLA("Fenrir", "Capella", "Polished, clear and professional voice", pitch = 1.0f, speed = 1.0f),
    ORION("Charon", "Orion", "Deep, resonant and confident presence", pitch = 0.82f, speed = 0.95f),
    URSA("Fenrir", "Ursa", "Rich, steady and dependable tone", pitch = 0.88f, speed = 0.98f),
    ECLIPSE("Charon", "Eclipse", "Mellow, relaxed and thoughtful tone", pitch = 0.85f, speed = 0.92f),
    VEGA("Aoede", "Vega", "Bright, articulate and engaging voice", pitch = 1.1f, speed = 1.02f),
    DIPPER("Puck", "Dipper", "Playful, lighthearted and energetic tone", pitch = 1.2f, speed = 1.05f),
    AOEDE("Aoede", "Aoede (Trending)", "Expressive, melodious, natural & warm conversational tone", pitch = 1.05f, speed = 1.0f),
    KORE("Kore", "Kore (Soothing)", "Calm, polite, soothing & empathetic voice", pitch = 0.95f, speed = 0.95f),
    PUCK("Puck", "Puck (Energetic)", "Playful, spirited, quick-witted conversational tone", pitch = 1.15f, speed = 1.05f),
    CHARON("Charon", "Charon (Deep)", "Deep, calm, resonant & confident tone", pitch = 0.8f, speed = 0.95f),
    FENRIR("Fenrir", "Fenrir (Crisp)", "Crisp, bold, energetic tone", pitch = 1.0f, speed = 1.0f)
}

data class GeminiSettingsState(
    val selectedVoice: GeminiVoice = GeminiVoice.ORBIT,
    val interruptLiveResponses: Boolean = true,
    val captionPreferences: Boolean = true,
    val guidedVisionInLive: Boolean = false,
    val language: AssistantLanguage = AssistantLanguage.AUTO,
    val handsFreeEnabled: Boolean = true,
    val screenContextEnabled: Boolean = true,
    val contactInfoEnabled: Boolean = true,
    val geminiOnLockScreen: Boolean = true
)

data class ConversationMessage(
    val id: Long = System.currentTimeMillis(),
    val sender: Sender,
    val text: String,
    val actionLabel: String? = null,
    val isNativeAudio: Boolean = false,
    val voiceName: String? = null
)

enum class Sender {
    USER,
    NOVA
}
