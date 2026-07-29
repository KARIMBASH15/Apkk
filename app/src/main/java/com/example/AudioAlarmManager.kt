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

    fun startAlarm(context: Context) {
        if (isAlarmPlaying) return
        isAlarmPlaying = true

        try {
            // Try default notification or alarm ringtone first
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

        // Fallback or additional loud ToneGenerator beep loop
        try {
            toneGen = ToneGenerator(AudioManager.STREAM_ALARM, 100)
            beepRunnable = object : Runnable {
                override fun run() {
                    if (!isAlarmPlaying) return
                    toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, 400)
                    handler.postDelayed(this, 800)
                }
            }
            handler.post(beepRunnable!!)
        } catch (e: Exception) {
            Log.e("AudioAlarmManager", "Failed ToneGenerator", e)
        }

        // Vibrate pattern
        try {
            val pattern = longArrayOf(0, 500, 300, 500, 300)
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

    fun stopAlarm(context: Context) {
        isAlarmPlaying = false
        beepRunnable?.let { handler.removeCallbacks(it) }
        beepRunnable = null

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
