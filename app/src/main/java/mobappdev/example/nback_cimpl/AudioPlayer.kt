package mobappdev.example.nback_cimpl

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.*

class AudioPlayer(context: Context) {
    private var tts: TextToSpeech? = null
    private var isReady = false

    private val letters = listOf("A", "B", "C", "D", "E", "F", "G", "H")

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale.US)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("AudioPlayer", "Language not supported")
                } else {
                    isReady = true
                    Log.d("AudioPlayer", "TTS initialized successfully")
                }
            } else {
                Log.e("AudioPlayer", "TTS initialization failed")
            }
        }
    }

    fun playLetter(index: Int) {
        if (isReady && index in 1..letters.size) {
            val letter = letters[index - 1]
            tts?.speak(letter, TextToSpeech.QUEUE_FLUSH, null, null)
            Log.d("AudioPlayer", "Playing letter: $letter")
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}