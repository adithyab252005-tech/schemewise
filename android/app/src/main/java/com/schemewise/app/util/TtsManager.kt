package com.schemewise.app.util

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

/**
 * TtsManager — Android Text-to-Speech accessibility engine for SchemeWise.
 *
 * Usage:
 *   val tts = TtsManager(context)
 *   tts.speak("You are eligible for PM-KISAN because...")
 *   tts.stop()
 *   tts.release() // on onDestroy
 */
class TtsManager(context: Context) {

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking

    private var tts: TextToSpeech? = null
    private var isReady = false

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale("hi", "IN") // Prefer Hindi; falls back to English
                if (tts?.isLanguageAvailable(Locale("hi", "IN")) == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts?.language = Locale.ENGLISH
                }
                tts?.setSpeechRate(0.9f)   // Slightly slower for clarity
                tts?.setPitch(1.0f)
                isReady = true
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) { _isSpeaking.value = true }
                    override fun onDone(utteranceId: String?)  { _isSpeaking.value = false }
                    override fun onError(utteranceId: String?) { _isSpeaking.value = false }
                })
            }
        }
    }

    fun speak(text: String) {
        if (!isReady) return
        val cleaned = text
            .replace(Regex("[*_`#]"), "")   // strip markdown
            .take(4000)                      // TTS char limit
        tts?.stop()
        val params = Bundle().apply { putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "sw_tts") }
        tts?.speak(cleaned, TextToSpeech.QUEUE_FLUSH, params, "sw_tts")
    }

    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
    }

    fun toggle(text: String) {
        if (_isSpeaking.value) stop() else speak(text)
    }

    fun release() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        _isSpeaking.value = false
    }
}
