package com.example.voice

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaPlayer
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.regex.Pattern
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * High-fidelity native audio player for Gemini Trained Human Voice Model
 * and direct PCM 24000Hz 16-bit audio stream.
 * 
 * Replaces old Android TTS entirely with pure native hardware audio synthesis.
 */
class GeminiNativeAudioPlayer(private val context: Context) {

    private var audioTrack: AudioTrack? = null
    private var mediaPlayer: MediaPlayer? = null
    private var playbackJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    @Volatile
    private var isPlaying = false

    fun isCurrentlyPlaying(): Boolean = isPlaying

    /**
     * Play Base64 Gemini Native Trained Audio (PCM 24000Hz or container audio)
     */
    suspend fun playAudio(
        base64Data: String,
        mimeType: String? = null,
        onProgressRms: (Float) -> Unit,
        onFinished: () -> Unit
    ) = withContext(Dispatchers.IO) {
        stop()

        if (base64Data.isBlank()) {
            withContext(Dispatchers.Main) { onFinished() }
            return@withContext
        }

        val audioBytes = try {
            Base64.decode(base64Data, Base64.DEFAULT)
        } catch (e: Exception) {
            Log.e("GeminiAudioPlayer", "Failed to decode base64 audio: ${e.message}")
            withContext(Dispatchers.Main) { onFinished() }
            return@withContext
        }

        if (audioBytes.isEmpty()) {
            withContext(Dispatchers.Main) { onFinished() }
            return@withContext
        }

        val cleanMime = (mimeType ?: "").lowercase()
        val isPcm = cleanMime.contains("pcm") || cleanMime.contains("l16") || cleanMime.isEmpty() ||
                (!cleanMime.contains("wav") && !cleanMime.contains("mp3") && !cleanMime.contains("ogg"))

        if (isPcm) {
            var sampleRate = 24000
            val rateMatcher = Pattern.compile("rate=(\\d+)").matcher(cleanMime)
            if (rateMatcher.find()) {
                sampleRate = rateMatcher.group(1)?.toIntOrNull() ?: 24000
            }

            playPcmAudio(audioBytes, sampleRate, onProgressRms, onFinished)
        } else {
            playContainerAudio(audioBytes, cleanMime, onProgressRms, onFinished)
        }
    }

    private suspend fun playPcmAudio(
        pcmBytes: ByteArray,
        sampleRate: Int,
        onProgressRms: (Float) -> Unit,
        onFinished: () -> Unit
    ) = withContext(Dispatchers.IO) {
        // Ensure STREAM_MUSIC volume is audible on the device/emulator
        ensureAudibleVolume()

        val minBufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val bufferSize = maxOf(minBufferSize * 2, 8192)

        val track = try {
            AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()
        } catch (e: Exception) {
            Log.e("GeminiAudioPlayer", "Failed to build AudioTrack: ${e.message}")
            withContext(Dispatchers.Main) { onFinished() }
            return@withContext
        }

        audioTrack = track
        isPlaying = true

        try {
            track.setVolume(1.0f)
            track.play()

            // At sampleRate (e.g. 24000Hz 16-bit Mono), 1 sec = 48,000 bytes.
            // 50ms chunk = 2,400 bytes.
            val bytesPerSecond = sampleRate * 2
            val chunkSize = (bytesPerSecond * 0.06).toInt() // ~60ms chunks
            var offset = 0

            while (offset < pcmBytes.size && isPlaying) {
                val toWrite = minOf(chunkSize, pcmBytes.size - offset)
                val written = track.write(pcmBytes, offset, toWrite, AudioTrack.WRITE_BLOCKING)
                if (written <= 0) break

                // Calculate real-time RMS for aura glow animation
                val rms = calculatePcmRms(pcmBytes, offset, written)
                onProgressRms(rms)

                offset += written

                // Delay according to real-time playback duration so audio stays in sync with aura
                val chunkDurationMs = ((written.toDouble() / bytesPerSecond.toDouble()) * 1000.0).toLong()
                if (chunkDurationMs > 0) {
                    delay(chunkDurationMs)
                }
            }

            // Allow trailing audio to complete playing out of speakers
            delay(300L)
        } catch (e: Exception) {
            Log.e("GeminiAudioPlayer", "Error during PCM playback: ${e.message}")
        } finally {
            try {
                track.pause()
                track.flush()
                track.stop()
                track.release()
            } catch (_: Exception) {}
            audioTrack = null
            isPlaying = false
            onProgressRms(0f)
            withContext(Dispatchers.Main) {
                onFinished()
            }
        }
    }

    private suspend fun playContainerAudio(
        audioBytes: ByteArray,
        mimeType: String,
        onProgressRms: (Float) -> Unit,
        onFinished: () -> Unit
    ) = withContext(Dispatchers.IO) {
        ensureAudibleVolume()

        val extension = when {
            mimeType.contains("mp3") -> ".mp3"
            mimeType.contains("wav") -> ".wav"
            mimeType.contains("ogg") -> ".ogg"
            else -> ".audio"
        }

        val tempFile = File(context.cacheDir, "gemini_voice_native$extension")
        try {
            FileOutputStream(tempFile).use { it.write(audioBytes) }
        } catch (e: Exception) {
            Log.e("GeminiAudioPlayer", "Error writing temp audio: ${e.message}")
            withContext(Dispatchers.Main) { onFinished() }
            return@withContext
        }

        val player = MediaPlayer()
        mediaPlayer = player
        isPlaying = true

        try {
            player.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            player.setDataSource(tempFile.absolutePath)
            player.prepare()
            player.start()

            playbackJob = scope.launch {
                var step = 0
                while (isActive && isPlaying && player.isPlaying) {
                    delay(50L)
                    step++
                    val pulse = 0.35f + 0.45f * (sin(step * 0.4).toFloat() + 1f) / 2f
                    onProgressRms(pulse)
                }
            }

            val durationMs = player.duration.toLong().coerceAtLeast(150L)
            delay(durationMs + 100L)
        } catch (e: Exception) {
            Log.e("GeminiAudioPlayer", "MediaPlayer playback error: ${e.message}")
        } finally {
            playbackJob?.cancel()
            try {
                if (player.isPlaying) player.stop()
                player.release()
            } catch (_: Exception) {}
            mediaPlayer = null
            isPlaying = false
            onProgressRms(0f)
            tempFile.delete()
            withContext(Dispatchers.Main) {
                onFinished()
            }
        }
    }

    /**
     * Synthesizes native melodic acoustic voice chime and feedback tones
     * directly in PCM without using old robotic TTS.
     */
    suspend fun playAcousticFeedback(
        type: FeedbackTone,
        onProgressRms: (Float) -> Unit,
        onFinished: () -> Unit
    ) = withContext(Dispatchers.IO) {
        val sampleRate = 24000
        val frequencies = when (type) {
            FeedbackTone.SUCCESS -> doubleArrayOf(523.25, 659.25, 783.99, 1046.50) // C5, E5, G5, C6
            FeedbackTone.ACKNOWLEDGEMENT -> doubleArrayOf(587.33, 880.00) // D5, A5
            FeedbackTone.LISTENING_START -> doubleArrayOf(440.00, 659.25) // A4, E5
            FeedbackTone.ERROR -> doubleArrayOf(349.23, 261.63) // F4, C4
        }
        val noteDuration = 0.09 // 90ms per note
        val totalSamples = (frequencies.size * noteDuration * sampleRate).toInt()
        val pcmBytes = ByteArray(totalSamples * 2)

        var sampleIndex = 0
        for (freq in frequencies) {
            val samplesInNote = (noteDuration * sampleRate).toInt()
            for (i in 0 until samplesInNote) {
                val time = i.toDouble() / sampleRate.toDouble()
                val envelope = sin(Math.PI * (i.toDouble() / samplesInNote.toDouble())) // Smooth bell envelope
                val sampleValue = (sin(2.0 * Math.PI * freq * time) * envelope * 24000.0).toInt().coerceIn(-32767, 32767).toShort()

                val byteIndex = sampleIndex * 2
                pcmBytes[byteIndex] = (sampleValue.toInt() and 0xFF).toByte()
                pcmBytes[byteIndex + 1] = ((sampleValue.toInt() shr 8) and 0xFF).toByte()
                sampleIndex++
            }
        }

        playPcmAudio(pcmBytes, sampleRate, onProgressRms, onFinished)
    }

    private fun ensureAudibleVolume() {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
            val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            if (current < (max * 0.7f).toInt()) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (max * 0.85f).toInt(), 0)
            }
        } catch (_: Exception) {}
    }

    private fun calculatePcmRms(bytes: ByteArray, offset: Int, length: Int): Float {
        var sum = 0.0
        val sampleCount = length / 2
        if (sampleCount == 0) return 0f

        for (i in 0 until sampleCount) {
            val idx = offset + i * 2
            if (idx + 1 >= bytes.size) break
            val sample = (bytes[idx].toInt() and 0xFF) or (bytes[idx + 1].toInt() shl 8)
            val shortSample = sample.toShort()
            sum += shortSample * shortSample
        }

        val mean = sum / sampleCount
        val rms = sqrt(mean).toFloat() / 32768f
        return (rms * 3.5f).coerceIn(0f, 1f)
    }

    fun stop() {
        isPlaying = false
        playbackJob?.cancel()
        playbackJob = null

        try {
            audioTrack?.let {
                it.pause()
                it.flush()
                it.stop()
                it.release()
            }
        } catch (_: Exception) {}
        audioTrack = null

        try {
            mediaPlayer?.let {
                if (it.isPlaying) it.stop()
                it.release()
            }
        } catch (_: Exception) {}
        mediaPlayer = null
    }

    fun cleanup() {
        stop()
    }
}

enum class FeedbackTone {
    SUCCESS,
    ACKNOWLEDGEMENT,
    LISTENING_START,
    ERROR
}
