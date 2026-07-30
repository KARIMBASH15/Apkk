package com.example

import android.content.Context
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

object AudioAlarmManager {
    private var ringtone: Ringtone? = null
    private var toneGen: ToneGenerator? = null
    private var isAlarmPlaying = false
    private val handler = Handler(Looper.getMainLooper())
    private var beepRunnable: Runnable? = null
    private var previewTimeoutRunnable: Runnable? = null

    fun startAlarm(
        context: Context,
        soundTone: String = "default",
        vibrationPattern: String = "standard"
    ) {
        if (isAlarmPlaying) return
        isAlarmPlaying = true

        try {
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            ringtone = RingtoneManager.getRingtone(context.applicationContext, alarmUri)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ringtone?.isLooping = true
            }
            ringtone?.play()
        } catch (e: Exception) {
            Log.e("AudioAlarmManager", "Failed playing default ringtone", e)
        }

        // Custom ToneGenerator cadence loop based on soundTone
        try {
            toneGen = ToneGenerator(AudioManager.STREAM_ALARM, 100)
            beepRunnable = object : Runnable {
                var step = 0
                override fun run() {
                    if (!isAlarmPlaying) return

                    when (soundTone) {
                        "chime" -> {
                            toneGen?.startTone(ToneGenerator.TONE_PROP_PROMPT, 200)
                            handler.postDelayed({
                                if (isAlarmPlaying) toneGen?.startTone(ToneGenerator.TONE_PROP_ACK, 300)
                            }, 250)
                            handler.postDelayed(this, 700)
                        }
                        "siren" -> {
                            val tone = if (step % 2 == 0) ToneGenerator.TONE_SUP_RINGTONE else ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK
                            toneGen?.startTone(tone, 250)
                            step++
                            handler.postDelayed(this, 350)
                        }
                        "soft" -> {
                            toneGen?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 350)
                            handler.postDelayed(this, 1200)
                        }
                        "triple" -> {
                            toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
                            handler.postDelayed({
                                if (isAlarmPlaying) toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
                            }, 200)
                            handler.postDelayed({
                                if (isAlarmPlaying) toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
                            }, 400)
                            handler.postDelayed(this, 1000)
                        }
                        else -> { // "default"
                            toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, 400)
                            handler.postDelayed(this, 800)
                        }
                    }
                }
            }
            handler.post(beepRunnable!!)
        } catch (e: Exception) {
            Log.e("AudioAlarmManager", "Failed ToneGenerator", e)
        }

        // Custom Vibrator pattern
        if (vibrationPattern != "none") {
            try {
                val pattern = when (vibrationPattern) {
                    "strong" -> longArrayOf(0, 1000, 200, 1000, 200)
                    "soft" -> longArrayOf(0, 200, 400, 200, 400)
                    else -> longArrayOf(0, 500, 300, 500, 300) // "standard"
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                    vibratorManager?.defaultVibrator?.vibrate(android.os.VibrationEffect.createWaveform(pattern, 0))
                } else {
                    @Suppress("DEPRECATION")
                    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator?.vibrate(android.os.VibrationEffect.createWaveform(pattern, 0))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator?.vibrate(pattern, 0)
                    }
                }
            } catch (e: Exception) {
                Log.e("AudioAlarmManager", "Failed Vibrator", e)
            }
        }
    }

    fun playPreview(
        context: Context,
        soundTone: String,
        vibrationPattern: String
    ) {
        stopAlarm(context)
        startAlarm(context, soundTone, vibrationPattern)

        previewTimeoutRunnable?.let { handler.removeCallbacks(it) }
        previewTimeoutRunnable = Runnable {
            stopAlarm(context)
        }
        handler.postDelayed(previewTimeoutRunnable!!, 3500)
    }

    fun stopAlarm(context: Context) {
        isAlarmPlaying = false
        beepRunnable?.let { handler.removeCallbacks(it) }
        beepRunnable = null

        previewTimeoutRunnable?.let { handler.removeCallbacks(it) }
        previewTimeoutRunnable = null

        try {
            ringtone?.stop()
            ringtone = null
        } catch (e: Exception) {
            Log.e("AudioAlarmManager", "Error stopping ringtone", e)
        }

        try {
            toneGen?.release()
            toneGen = null
        } catch (e: Exception) {
            Log.e("AudioAlarmManager", "Error releasing ToneGenerator", e)
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.cancel()
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                vibrator?.cancel()
            }
        } catch (e: Exception) {
            Log.e("AudioAlarmManager", "Error stopping Vibrator", e)
        }
    }

    fun isPlaying(): Boolean = isAlarmPlaying
}

