package com.example

import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.min
import kotlin.math.sin

object AudioAlarmManager {
    @Volatile
    private var isAlarmPlaying = false

    private var playbackThread: Thread? = null
    private var currentAudioTrack: AudioTrack? = null
    private val lock = Any()

    private const val SAMPLE_RATE = 44100

    data class Note(val freq: Double, val durationMs: Int, val volume: Double = 0.85, val decayRate: Double = 3.5)

    fun startAlarm(
        context: Context,
        soundTone: String = "default",
        vibrationPattern: String = "standard"
    ) {
        synchronized(lock) {
            stopAlarmInternal(context)
            isAlarmPlaying = true

            // 1. Start In-App Audio Synthesizer Loop
            val pcmData = generateMelodyPcm(soundTone)
            val pauseBetweenMs = when (soundTone) {
                "siren" -> 400L
                "chime" -> 800L
                "soft" -> 1200L
                "triple" -> 900L
                else -> 900L // "default"
            }

            playbackThread = Thread {
                var audioTrack: AudioTrack? = null
                try {
                    val bufferSize = AudioTrack.getMinBufferSize(
                        SAMPLE_RATE,
                        AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT
                    ).coerceAtLeast(pcmData.size * 2)

                    audioTrack = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        AudioTrack.Builder()
                            .setAudioAttributes(
                                AudioAttributes.Builder()
                                    .setUsage(AudioAttributes.USAGE_MEDIA)
                                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                    .build()
                            )
                            .setAudioFormat(
                                AudioFormat.Builder()
                                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                                    .setSampleRate(SAMPLE_RATE)
                                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                                    .build()
                            )
                            .setBufferSizeInBytes(bufferSize)
                            .setTransferMode(AudioTrack.MODE_STREAM)
                            .build()
                    } else {
                        @Suppress("DEPRECATION")
                        AudioTrack(
                            AudioManager.STREAM_MUSIC,
                            SAMPLE_RATE,
                            AudioFormat.CHANNEL_OUT_MONO,
                            AudioFormat.ENCODING_PCM_16BIT,
                            bufferSize,
                            AudioTrack.MODE_STREAM
                        )
                    }

                    synchronized(lock) {
                        currentAudioTrack = audioTrack
                    }

                    audioTrack.play()

                    while (isAlarmPlaying && !Thread.currentThread().isInterrupted) {
                        var offset = 0
                        while (offset < pcmData.size && isAlarmPlaying && !Thread.currentThread().isInterrupted) {
                            val written = audioTrack.write(pcmData, offset, pcmData.size - offset)
                            if (written <= 0) break
                            offset += written
                        }

                        if (!isAlarmPlaying || Thread.currentThread().isInterrupted) break

                        try {
                            Thread.sleep(pauseBetweenMs)
                        } catch (e: InterruptedException) {
                            break
                        }
                    }
                } catch (e: Exception) {
                    Log.e("AudioAlarmManager", "Playback error", e)
                } finally {
                    try {
                        audioTrack?.pause()
                        audioTrack?.flush()
                        audioTrack?.stop()
                        audioTrack?.release()
                    } catch (e: Exception) {
                        // ignore release exceptions
                    }
                    synchronized(lock) {
                        if (currentAudioTrack == audioTrack) {
                            currentAudioTrack = null
                        }
                    }
                }
            }.apply {
                isDaemon = true
                name = "InAppAlarmSoundThread"
                start()
            }

            // 2. Start Vibration
            if (vibrationPattern != "none") {
                try {
                    val pattern = when (vibrationPattern) {
                        "strong" -> longArrayOf(0, 900, 200, 900, 200)
                        "soft" -> longArrayOf(0, 200, 400, 200, 400)
                        else -> longArrayOf(0, 450, 250, 450, 250) // "standard"
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
    }

    fun playPreview(
        context: Context,
        soundTone: String,
        vibrationPattern: String
    ) {
        startAlarm(context, soundTone, vibrationPattern)
        Thread {
            try {
                Thread.sleep(3200)
            } catch (_: InterruptedException) {}
            stopAlarm(context)
        }.start()
    }

    fun stopAlarm(context: Context) {
        synchronized(lock) {
            stopAlarmInternal(context)
        }
    }

    private fun stopAlarmInternal(context: Context) {
        isAlarmPlaying = false

        // Interrupt and join thread briefly
        playbackThread?.let {
            it.interrupt()
            playbackThread = null
        }

        // Immediately silence audio track
        currentAudioTrack?.let {
            try {
                it.pause()
                it.flush()
                it.stop()
                it.release()
            } catch (e: Exception) {
                Log.e("AudioAlarmManager", "Error stopping AudioTrack", e)
            }
            currentAudioTrack = null
        }

        // Cancel vibrator
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

        // Cancel order alert notifications if active
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.cancel(OrderForegroundService.NOTIF_ID_NEW_ORDER)
        } catch (e: Exception) {
            Log.e("AudioAlarmManager", "Error cancelling notification", e)
        }
    }

    fun isPlaying(): Boolean = isAlarmPlaying

    /**
     * Synthesizes smooth, rich harmonic bells & chimes (16-bit Mono PCM).
     * Avoids harsh device alarm sounds, creating a pleasant shop bell & notification chime.
     */
    private fun generateMelodyPcm(soundTone: String): ShortArray {
        val notes: List<Note> = when (soundTone) {
            "chime" -> listOf(
                // Modern upbeat delivery chime (E5 -> G#5 -> B5 -> E6)
                Note(659.25, 140, 0.7, 4.0),
                Note(830.61, 140, 0.75, 4.0),
                Note(987.77, 160, 0.8, 4.0),
                Note(1318.51, 550, 0.9, 2.8)
            )
            "siren" -> listOf(
                // Energetic double-chime pulse (A5 & C#6)
                Note(880.0, 180, 0.85, 4.5),
                Note(1108.73, 180, 0.9, 4.5),
                Note(880.0, 180, 0.85, 4.5),
                Note(1108.73, 350, 0.9, 3.2)
            )
            "soft" -> listOf(
                // Calming warm harp / marimba chord (D5 -> F#5 -> A5 -> D6)
                Note(587.33, 200, 0.65, 3.5),
                Note(739.99, 200, 0.7, 3.5),
                Note(880.0, 220, 0.75, 3.5),
                Note(1174.66, 600, 0.8, 2.2)
            )
            "triple" -> listOf(
                // Three crisp bright crystal dings
                Note(1046.50, 150, 0.85, 5.0),
                Note(1318.51, 150, 0.85, 5.0),
                Note(1567.98, 450, 0.9, 3.0)
            )
            else -> listOf( // "default": Al Dwaar Classic Store Bell (C6 -> E6 -> G6)
                Note(1046.50, 160, 0.8, 4.0),
                Note(1318.51, 180, 0.85, 4.0),
                Note(1567.98, 700, 0.95, 2.5)
            )
        }

        val totalSamples = notes.sumOf { (SAMPLE_RATE * it.durationMs) / 1000 }
        val buffer = ShortArray(totalSamples)
        var sampleIndex = 0

        for (note in notes) {
            val count = (SAMPLE_RATE * note.durationMs) / 1000
            for (i in 0 until count) {
                val t = i.toDouble() / SAMPLE_RATE
                // 6ms linear attack to eliminate clicks/pops
                val attack = min(1.0, t / 0.006)
                val decay = exp(-t * note.decayRate)

                // Multi-harmonic bell synthesis (fundamental + octave + 3rd + bell overtone)
                val f = note.freq
                val signal = sin(2.0 * PI * f * t) +
                        0.32 * sin(2.0 * PI * (2.0 * f) * t) +
                        0.14 * sin(2.0 * PI * (3.0 * f) * t) +
                        0.07 * sin(2.0 * PI * (4.2 * f) * t)

                val sampleVal = (signal * attack * decay * note.volume * 22000.0)
                    .toInt()
                    .coerceIn(-32767, 32767)
                    .toShort()

                if (sampleIndex < buffer.size) {
                    buffer[sampleIndex++] = sampleVal
                }
            }
        }

        return buffer
    }
}
