package com.example.hardware

import android.app.Activity
import android.app.SearchManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.MediaStore
import android.provider.Settings
import android.view.WindowManager
import com.example.model.BatteryInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HardwareController(private val context: Context) {

    private val _batteryInfo = MutableStateFlow(BatteryInfo())
    val batteryInfo: StateFlow<BatteryInfo> = _batteryInfo.asStateFlow()

    private val _isFlashlightOn = MutableStateFlow(false)
    val isFlashlightOn: StateFlow<Boolean> = _isFlashlightOn.asStateFlow()

    private val _hasFlashlight = MutableStateFlow(false)
    val hasFlashlight: StateFlow<Boolean> = _hasFlashlight.asStateFlow()

    private val _isWakeLockActive = MutableStateFlow(false)
    val isWakeLockActive: StateFlow<Boolean> = _isWakeLockActive.asStateFlow()

    private var cameraManager: CameraManager? = null
    private var rearCameraId: String? = null

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
                val temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10.0f
                val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)

                val percentage = if (level >= 0 && scale > 0) (level * 100) / scale else 100
                val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL

                val plugType = when (plugged) {
                    BatteryManager.BATTERY_PLUGGED_AC -> "Fast AC Charger"
                    BatteryManager.BATTERY_PLUGGED_USB -> "USB Cable"
                    BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless Qi Pad"
                    else -> "Battery Discharging"
                }

                _batteryInfo.value = BatteryInfo(
                    percentage = percentage,
                    isCharging = isCharging,
                    chargeSource = plugType,
                    temperature = temp,
                    voltage = voltage
                )
            }
        }
    }

    init {
        initFlashlight()
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(batteryReceiver, filter)
    }

    private fun initFlashlight() {
        try {
            cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
            cameraManager?.let { manager ->
                val cameraIds = manager.cameraIdList
                for (id in cameraIds) {
                    val characteristics = manager.getCameraCharacteristics(id)
                    val flashAvailable = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                    val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                    if (flashAvailable && facing == CameraCharacteristics.LENS_FACING_BACK) {
                        rearCameraId = id
                        _hasFlashlight.value = true
                        break
                    }
                }
                if (rearCameraId == null && cameraIds.isNotEmpty()) {
                    // Fallback to first camera with flash
                    for (id in cameraIds) {
                        val characteristics = manager.getCameraCharacteristics(id)
                        if (characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true) {
                            rearCameraId = id
                            _hasFlashlight.value = true
                            break
                        }
                    }
                }

                // Register torch callback for real hardware state
                manager.registerTorchCallback(object : CameraManager.TorchCallback() {
                    override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
                        if (cameraId == rearCameraId) {
                            _isFlashlightOn.value = enabled
                        }
                    }
                }, null)
            }
        } catch (e: Exception) {
            _hasFlashlight.value = false
        }
    }

    fun toggleFlashlight(forceState: Boolean? = null): Boolean {
        val target = forceState ?: !_isFlashlightOn.value
        val camId = rearCameraId
        if (cameraManager != null && camId != null) {
            try {
                cameraManager?.setTorchMode(camId, target)
                _isFlashlightOn.value = target
                vibrateClick()
                return true
            } catch (e: CameraAccessException) {
                // Flashlight access error
            } catch (e: Exception) {
                // Fallback simulation toggle
            }
        }
        // Simulated torch state if hardware unavailable
        _isFlashlightOn.value = target
        vibrateClick()
        return target
    }

    fun vibrateClick() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(30)
            }
        } catch (_: Exception) {}
    }

    fun vibrateDoublePulse() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 35, 50, 40)
                val amplitudes = intArrayOf(0, 180, 0, 220)
                vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(100)
            }
        } catch (_: Exception) {}
    }

    fun vibrateAssistantSummon() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 60, 40, 90)
                val amplitudes = intArrayOf(0, 120, 0, 255)
                vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(150)
            }
        } catch (_: Exception) {}
    }

    fun vibrateTick() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(12, 60))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(12)
            }
        } catch (_: Exception) {}
    }

    fun toggleWakeLock(activity: Activity?, forceEnable: Boolean? = null) {
        val target = forceEnable ?: !_isWakeLockActive.value
        _isWakeLockActive.value = target
        activity?.runOnUiThread {
            if (target) {
                activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else {
                activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }

    fun playAudibleAlarmChime() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Play melodic dual-tone chime
                val toneGen = ToneGenerator(AudioManager.STREAM_ALARM, 100)
                for (i in 0..2) {
                    toneGen.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 250)
                    delay(300)
                    toneGen.startTone(ToneGenerator.TONE_PROP_BEEP2, 350)
                    delay(400)
                }
                toneGen.release()
            } catch (e: Exception) {
                // Fallback to notification ringtone
                try {
                    val alertUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    val ringtone = RingtoneManager.getRingtone(context, alertUri)
                    ringtone?.play()
                } catch (_: Exception) {}
            }
        }
    }

    // Native App Launchers and System Intents
    fun launchPhoneDialer(numberOrName: String = "") {
        val cleanNumber = numberOrName.filter { it.isDigit() || it == '+' || it == '*' || it == '#' }
        val uri = if (cleanNumber.isNotEmpty()) Uri.parse("tel:$cleanNumber") else Uri.parse("tel:")
        val intent = Intent(Intent.ACTION_DIAL, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Intent couldn't be launched
        }
    }

    fun launchCamera() {
        val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            val fallback = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            try {
                context.startActivity(fallback)
            } catch (_: Exception) {}
        }
    }

    fun launchYouTube(query: String = "") {
        val intent = if (query.isNotBlank()) {
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://www.youtube.com/results?search_query=" + Uri.encode(query))
            )
        } else {
            val pkgIntent = context.packageManager.getLaunchIntentForPackage("com.google.android.youtube")
            pkgIntent ?: Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com"))
        }.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://m.youtube.com/results?search_query=" + Uri.encode(query))).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(webIntent)
        }
    }

    fun launchGoogleSearch(query: String) {
        val webSearchIntent = Intent(Intent.ACTION_WEB_SEARCH).apply {
            putExtra(SearchManager.QUERY, query)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(webSearchIntent)
        } catch (e: Exception) {
            val browserIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://www.google.com/search?q=" + Uri.encode(query))
            ).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(browserIntent)
        }
    }

    fun launchWhatsApp(phone: String = "", message: String = "") {
        val cleanPhone = phone.filter { it.isDigit() }
        val uri = if (cleanPhone.isNotEmpty()) {
            Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=" + Uri.encode(message))
        } else {
            Uri.parse("https://api.whatsapp.com/send?text=" + Uri.encode(message))
        }
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // WhatsApp not installed, fallback to SMS
            launchSms(cleanPhone, message)
        }
    }

    fun launchSms(phone: String = "", message: String = "") {
        val uri = if (phone.isNotEmpty()) Uri.parse("sms:$phone") else Uri.parse("sms:")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            putExtra("sms_body", message)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {}
    }

    fun openDefaultAssistantSettings() {
        val intents = listOf(
            Intent(Settings.ACTION_VOICE_INPUT_SETTINGS),
            Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS),
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            },
            Intent(Settings.ACTION_SETTINGS)
        )
        for (intent in intents) {
            try {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                return
            } catch (_: Exception) {}
        }
    }

    fun cleanup() {
        try {
            context.unregisterReceiver(batteryReceiver)
        } catch (_: Exception) {}
        if (_isFlashlightOn.value && rearCameraId != null) {
            try {
                cameraManager?.setTorchMode(rearCameraId!!, false)
            } catch (_: Exception) {}
        }
    }
}
