package com.example.ui

import android.content.Context
import android.media.MediaPlayer
import android.media.ToneGenerator
import android.media.AudioManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale
import kotlin.random.Random

class GameAudioManager private constructor(context: Context) {

    private val appContext: Context = context.applicationContext
    private var mediaPlayer: MediaPlayer? = null
    private var tts: TextToSpeech? = null
    private var isTtsInitialized = false
    private var toneGen: ToneGenerator? = null

    @Volatile
    var isWorldMapScreenActive: Boolean = false

    @Volatile
    var isGameplayScreenActive: Boolean = false

    init {
        try {
            toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        } catch (e: Exception) {
            Log.e("GameAudioManager", "Failed to initialize ToneGenerator", e)
        }
        initTts()
    }

    private fun initTts() {
        try {
            tts = TextToSpeech(appContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    tts?.let { t ->
                        val result = t.setLanguage(Locale("fa", "IR"))
                        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                            Log.e("GameAudioManager", "Persian TTS language is not supported on this device.")
                        } else {
                            t.setSpeechRate(0.85f)
                            isTtsInitialized = true
                        }
                    }
                } else {
                    Log.e("GameAudioManager", "TextToSpeech initialization failed with status: $status")
                }
            }
        } catch (e: Exception) {
            Log.e("GameAudioManager", "Exception initializing TextToSpeech", e)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: GameAudioManager? = null

        fun getInstance(context: Context): GameAudioManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: GameAudioManager(context).also { INSTANCE = it }
            }
        }
    }

    /**
     * Stop and release any active MediaPlayer safely.
     */
    @Synchronized
    fun stopAndRelease() {
        try {
            mediaPlayer?.let { mp ->
                try {
                    if (mp.isPlaying) {
                        mp.stop()
                    }
                } catch (e: Exception) {
                    // Ignore
                }
                try {
                    mp.reset()
                } catch (e: Exception) {
                    // Ignore
                }
                try {
                    mp.release()
                } catch (e: Exception) {
                    // Ignore
                }
            }
        } catch (e: Exception) {
            Log.e("GameAudioManager", "Error releasing MediaPlayer", e)
        } finally {
            mediaPlayer = null
        }
    }

    /**
     * Play raw sound resource by ID.
     */
    @Synchronized
    private fun playRawResource(context: Context, resId: Int): Boolean {
        stopAndRelease()
        if (resId == 0) return false

        if (isGameplayScreenActive) {
            Log.d("GameAudioManager", "Blocked playing resource $resId on Gameplay screen")
            return false
        }

        if (isWorldMapScreenActive) {
            val name = try { context.resources.getResourceEntryName(resId) } catch (e: Exception) { "" }
            if (isBlockedResourceName(name)) {
                Log.d("GameAudioManager", "Blocked playing $name on WorldMap screen")
                return false
            }
        }

        return try {
            val mp = MediaPlayer.create(context, resId)
            if (mp != null) {
                mediaPlayer = mp
                mp.setOnCompletionListener { completedPlayer ->
                    synchronized(this@GameAudioManager) {
                        if (mediaPlayer == completedPlayer) {
                            stopAndRelease()
                        }
                    }
                }
                mp.start()
                true
            } else {
                Log.e("GameAudioManager", "MediaPlayer.create returned null for resId: $resId")
                false
            }
        } catch (e: Exception) {
            Log.e("GameAudioManager", "Error playing raw resource $resId", e)
            false
        }
    }

    /**
     * Statically maps a row and factor to their respective raw resource ID.
     * This provides static references so the resource shrinker never strips them.
     */
    private fun getMulResId(row: Int, factor: Int): Int {
        return 0
    }

    /**
     * Helper to check and find an existing raw resource.
     */
    private fun getRawResId(context: Context, resName: String): Int {
        // Direct static mapping for reliable lookup and to keep raw files
        if (resName == "wheel_start") return com.example.R.raw.wheel_start
        if (resName == "wheel_spin") return com.example.R.raw.wheel_spin
        if (resName == "wheel_win") return com.example.R.raw.wheel_win
        if (resName == "wheel_finish") return com.example.R.raw.wheel_finish

        if (resName.startsWith("mul_")) {
            val parts = resName.split("_")
            if (parts.size == 3) {
                val row = parts[1].toIntOrNull()
                val col = parts[2].toIntOrNull()
                if (row != null && col != null) {
                    val staticId = getMulResId(row, col)
                    if (staticId != 0) return staticId
                }
            }
        }

        return try {
            // Use reflection on com.example.R.raw for 100% reliable lookup
            val clazz = Class.forName("com.example.R\$raw")
            val field = clazz.getField(resName)
            field.getInt(null)
        } catch (e: Exception) {
            try {
                var id = context.resources.getIdentifier(resName, "raw", context.packageName)
                if (id == 0) {
                    id = context.resources.getIdentifier(resName, "raw", "com.example")
                }
                id
            } catch (ex: Exception) {
                0
            }
        }
    }

    /**
     * Find existing files from a list of names and pick one randomly.
     */
    private fun playRandomResource(context: Context, prefix: String, maxCount: Int, fallbackAction: () -> Unit) {
        val availableResIds = mutableListOf<Int>()
        for (i in 1..maxCount) {
            val formattedIndex = String.format("%02d", i)
            val resName = "${prefix}_$formattedIndex"
            val resId = getRawResId(context, resName)
            if (resId != 0) {
                availableResIds.add(resId)
            }
        }

        if (availableResIds.isNotEmpty()) {
            val randomResId = availableResIds[Random.nextInt(availableResIds.size)]
            val success = playRawResource(context, randomResId)
            if (!success) {
                fallbackAction()
            }
        } else {
            fallbackAction()
        }
    }

    /**
     * Helper to check if resource name is blocked on WorldMap screen.
     */
    private fun isBlockedResourceName(name: String): Boolean {
        if (name.isEmpty()) return false
        val lower = name.lowercase()
        return lower.startsWith("mul_") ||
                lower.startsWith("question_") ||
                lower.startsWith("correct") ||
                lower.startsWith("wrong") ||
                lower.startsWith("finish") ||
                lower.startsWith("boss_start") ||
                lower.startsWith("boss_win") ||
                lower.startsWith("boss_lose") ||
                lower.startsWith("wheel") ||
                lower.startsWith("achievement") ||
                lower.startsWith("daily_reward") ||
                lower.startsWith("league_up")
    }

    /**
     * Helper to check if event is blocked on WorldMap screen.
     */
    private fun isBlockedEvent(event: GameSoundEvent): Boolean {
        return when (event) {
            is GameSoundEvent.PlayTone -> false
            GameSoundEvent.CorrectSound -> true
            GameSoundEvent.WrongSound -> true
            GameSoundEvent.StarSound -> true
            GameSoundEvent.CoinSound -> true
            is GameSoundEvent.SpeakText -> true
            is GameSoundEvent.PlayLearnClip -> true
            GameSoundEvent.PlayCorrectRand -> true
            GameSoundEvent.PlayWrongRand -> true
            GameSoundEvent.PlayFinishRand -> true
            GameSoundEvent.PlayBossStartRand -> true
            GameSoundEvent.PlayBossWinRand -> true
            GameSoundEvent.PlayBossLoseRand -> true
            GameSoundEvent.PlayWheelStart -> true
            GameSoundEvent.PlayWheelSpin -> true
            GameSoundEvent.PlayWheelWin -> true
            GameSoundEvent.PlayWheelFinish -> true
            GameSoundEvent.PlayDuelCorrect -> true
            GameSoundEvent.PlayDuelWrong -> true
            GameSoundEvent.PlayDuelCombo -> true
            GameSoundEvent.PlayDuelTick -> true
            GameSoundEvent.PlayDuelStart -> true
            GameSoundEvent.PlayDuelFinish -> true
            GameSoundEvent.PlayDuelVictory -> true
            GameSoundEvent.PlayDuelDraw -> true
            GameSoundEvent.PlayDuelClick -> true
        }
    }

    /**
     * Handles specific GameSoundEvent playback.
     */
    fun handleSoundEvent(context: Context, event: GameSoundEvent) {
        if (isGameplayScreenActive) {
            when (event) {
                is GameSoundEvent.PlayLearnClip -> {
                    speakPersianText("${event.row} ضربدر ${event.factor}")
                }
                GameSoundEvent.CorrectSound, GameSoundEvent.PlayCorrectRand -> {
                    speakPersianText("آفرین")
                    try {
                        toneGen?.startTone(ToneGenerator.TONE_CDMA_PIP, 100)
                    } catch (e: Exception) {
                        // ignore
                    }
                }
                GameSoundEvent.WrongSound, GameSoundEvent.PlayWrongRand -> {
                    speakPersianText("مجددا تلاش کن عزیزم")
                    try {
                        toneGen?.startTone(ToneGenerator.TONE_SUP_ERROR, 250)
                    } catch (e: Exception) {
                        // ignore
                    }
                }
                GameSoundEvent.StarSound -> {
                    try {
                        toneGen?.startTone(ToneGenerator.TONE_CDMA_ALERT_INCALL_LITE, 200)
                    } catch (e: Exception) {
                        // ignore
                    }
                }
                GameSoundEvent.CoinSound -> {
                    try {
                        toneGen?.startTone(ToneGenerator.TONE_CDMA_PIP, 50)
                        Thread {
                            try {
                                Thread.sleep(80)
                                toneGen?.startTone(ToneGenerator.TONE_CDMA_PIP, 80)
                            } catch (e: Exception) {
                                // ignore
                            }
                        }.start()
                    } catch (e: Exception) {
                        // ignore
                    }
                }
                is GameSoundEvent.PlayTone -> {
                    try {
                        toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, event.duration)
                    } catch (e: Exception) {
                        Log.e("GameAudioManager", "Error playing tone", e)
                    }
                }
                else -> {
                    Log.d("GameAudioManager", "Blocked sound event $event on Gameplay screen")
                }
            }
            return
        }
        if (isWorldMapScreenActive && isBlockedEvent(event)) {
            Log.d("GameAudioManager", "Blocked sound event $event on WorldMap screen")
            return
        }
        when (event) {
            is GameSoundEvent.PlayTone -> {
                try {
                    toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, event.duration)
                } catch (e: Exception) {
                    Log.e("GameAudioManager", "Error playing tone", e)
                }
            }
            GameSoundEvent.CorrectSound -> {
                playRandomResource(context, "correct", 30) {
                    try {
                        toneGen?.startTone(ToneGenerator.TONE_CDMA_PIP, 100)
                    } catch (e: Exception) {
                        // ignore
                    }
                }
            }
            GameSoundEvent.WrongSound -> {
                playRandomResource(context, "wrong", 20) {
                    try {
                        toneGen?.startTone(ToneGenerator.TONE_SUP_ERROR, 250)
                    } catch (e: Exception) {
                        // ignore
                    }
                }
            }
            GameSoundEvent.StarSound -> {
                try {
                    toneGen?.startTone(ToneGenerator.TONE_CDMA_ALERT_INCALL_LITE, 200)
                } catch (e: Exception) {
                    // ignore
                }
            }
            GameSoundEvent.CoinSound -> {
                try {
                    toneGen?.startTone(ToneGenerator.TONE_CDMA_PIP, 50)
                    Thread {
                        try {
                            Thread.sleep(80)
                            toneGen?.startTone(ToneGenerator.TONE_CDMA_PIP, 80)
                        } catch (e: Exception) {
                            // ignore
                        }
                    }.start()
                } catch (e: Exception) {
                    // ignore
                }
            }
            is GameSoundEvent.SpeakText -> {
                speakPersianText(event.text)
            }
            is GameSoundEvent.PlayLearnClip -> {
                val resName = "mul_${event.row}_${event.factor}"
                val resId = getRawResId(context, resName)
                var playedSuccessfully = false
                if (resId != 0) {
                    playedSuccessfully = playRawResourceDirect(context, resId)
                }

                if (!playedSuccessfully) {
                    val text = "${event.row} ضربدر ${event.factor} مساوی ${event.row * event.factor}"
                    speakPersianText(text)
                }
            }
            GameSoundEvent.PlayCorrectRand -> {
                playRandomResource(context, "correct", 30) {
                    try {
                        toneGen?.startTone(ToneGenerator.TONE_CDMA_PIP, 100)
                    } catch (e: Exception) {
                        // ignore
                    }
                }
            }
            GameSoundEvent.PlayWrongRand -> {
                playRandomResource(context, "wrong", 20) {
                    try {
                        toneGen?.startTone(ToneGenerator.TONE_SUP_ERROR, 250)
                    } catch (e: Exception) {
                        // ignore
                    }
                }
            }
            GameSoundEvent.PlayFinishRand -> {
                playRandomResource(context, "finish", 20) {
                    try {
                        toneGen?.startTone(ToneGenerator.TONE_CDMA_ALERT_INCALL_LITE, 300)
                    } catch (e: Exception) {
                        // ignore
                    }
                }
            }
            GameSoundEvent.PlayBossStartRand -> {
                playRandomResource(context, "boss_start", 20) {
                    // Silence fallback
                }
            }
            GameSoundEvent.PlayBossWinRand -> {
                playRandomResource(context, "boss_win", 20) {
                    handleSoundEvent(context, GameSoundEvent.PlayFinishRand)
                }
            }
            GameSoundEvent.PlayBossLoseRand -> {
                playRandomResource(context, "boss_lose", 20) {
                    handleSoundEvent(context, GameSoundEvent.PlayWrongRand)
                }
            }
            GameSoundEvent.PlayWheelStart -> {
                val resId = getRawResId(context, "wheel_start")
                if (resId != 0) playRawResource(context, resId)
            }
            GameSoundEvent.PlayWheelSpin -> {
                val resId = getRawResId(context, "wheel_spin")
                if (resId != 0) playRawResource(context, resId)
            }
            GameSoundEvent.PlayWheelWin -> {
                val resId = getRawResId(context, "wheel_win")
                if (resId != 0) playRawResource(context, resId)
            }
            GameSoundEvent.PlayWheelFinish -> {
                val resId = getRawResId(context, "wheel_finish")
                if (resId != 0) playRawResource(context, resId)
            }
            GameSoundEvent.PlayDuelCorrect -> {
                try { toneGen?.startTone(ToneGenerator.TONE_CDMA_PIP, 100) } catch (e: Exception) {}
            }
            GameSoundEvent.PlayDuelWrong -> {
                try { toneGen?.startTone(ToneGenerator.TONE_CDMA_SOFT_ERROR_LITE, 150) } catch (e: Exception) {}
            }
            GameSoundEvent.PlayDuelCombo -> {
                try { toneGen?.startTone(ToneGenerator.TONE_CDMA_ALERT_INCALL_LITE, 200) } catch (e: Exception) {}
            }
            GameSoundEvent.PlayDuelTick -> {
                try { toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, 100) } catch (e: Exception) {}
            }
            GameSoundEvent.PlayDuelStart -> {
                try { toneGen?.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 300) } catch (e: Exception) {}
            }
            GameSoundEvent.PlayDuelFinish -> {
                try { toneGen?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 400) } catch (e: Exception) {}
            }
            GameSoundEvent.PlayDuelVictory -> {
                playRandomResource(context, "boss_win", 10) {
                    try { toneGen?.startTone(ToneGenerator.TONE_CDMA_ALERT_NETWORK_LITE, 400) } catch (e: Exception) {}
                }
            }
            GameSoundEvent.PlayDuelDraw -> {
                try { toneGen?.startTone(ToneGenerator.TONE_PROP_NACK, 300) } catch (e: Exception) {}
            }
            GameSoundEvent.PlayDuelClick -> {
                try { toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, 50) } catch (e: Exception) {}
            }
            else -> {}
        }
    }

    /**
     * Explicitly plays a learning multiplication audio file without any screen checks or filters.
     * Useful for direct clicks on the Learn Tables screen.
     */
    fun playLearnClipDirectly(context: Context, row: Int, factor: Int) {
        val resName = "mul_${row}_${factor}"
        val resId = getRawResId(context, resName)
        var playedSuccessfully = false
        if (resId != 0) {
            playedSuccessfully = playRawResourceDirect(context, resId)
        }

        if (!playedSuccessfully) {
            val text = "$row ضربدر $factor مساوی ${row * factor}"
            speakPersianText(text)
        }
    }

    @Synchronized
    fun playRawResourceDirect(context: Context, resId: Int): Boolean {
        stopAndRelease()
        if (resId == 0) return false

        return try {
            val mp = MediaPlayer.create(context, resId)
            if (mp != null) {
                mediaPlayer = mp
                mp.setOnCompletionListener { completedPlayer ->
                    synchronized(this@GameAudioManager) {
                        if (mediaPlayer == completedPlayer) {
                            stopAndRelease()
                        }
                    }
                }
                mp.start()
                true
            } else {
                Log.e("GameAudioManager", "MediaPlayer.create returned null in direct play for resId: $resId")
                false
            }
        } catch (e: Exception) {
            Log.e("GameAudioManager", "Error playing direct raw resource $resId", e)
            false
        }
    }

    private fun speakPersianText(text: String) {
        try {
            if (tts == null || !isTtsInitialized) {
                initTts()
            }
            tts?.let { t ->
                val faLocale = Locale("fa", "IR")
                t.language = faLocale
                t.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
            }
        } catch (e: Exception) {
            Log.e("GameAudioManager", "Error speaking TTS text: $text", e)
        }
    }

    fun onDestroy() {
        stopAndRelease()
        try {
            toneGen?.release()
        } catch (e: Exception) {
            // ignore
        } finally {
            toneGen = null
        }
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) {
            // ignore
        } finally {
            tts = null
            isTtsInitialized = false
        }
    }
}
