package com.nidoham.kaveya.platform.voice.kotlin

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.languageid.LanguageIdentificationOptions
import java.util.Locale

class TextToSpeechHandler(context: Context, private val callback: SpeechCallback? = null) {
    private var textToSpeech: TextToSpeech? = null
    private var isInitialized = false
    private val languageIdentifier = LanguageIdentification.getClient(
        LanguageIdentificationOptions.Builder()
            .setConfidenceThreshold(0.7f)
            .build()
    )

    interface SpeechCallback {
        fun onSpeechCompleted(success: Boolean)
    }

    init {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech?.setLanguage(Locale.US)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "Default language (US English) not supported or missing data")
                } else {
                    isInitialized = true
                    setupUtteranceListener()
                    Log.d(TAG, "TextToSpeech initialized successfully")
                }
            } else {
                Log.e(TAG, "TextToSpeech initialization failed with status: $status")
            }
        }
    }

    private fun setupUtteranceListener() {
        textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                Log.d(TAG, "Speech started for utterance: $utteranceId")
            }

            override fun onDone(utteranceId: String?) {
                Log.d(TAG, "Speech completed successfully for utterance: $utteranceId")
                callback?.onSpeechCompleted(true)
            }

            override fun onError(utteranceId: String?) {
                Log.e(TAG, "Speech error for utterance: $utteranceId")
                callback?.onSpeechCompleted(false)
            }
        })
    }

    fun speak(text: String) {
        if (!isInitialized) {
            Log.e(TAG, "TextToSpeech not initialized")
            callback?.onSpeechCompleted(false)
            return
        }

        if (text.isEmpty()) {
            Log.w(TAG, "Empty text provided for speech")
            callback?.onSpeechCompleted(false)
            return
        }

        languageIdentifier.identifyLanguage(text)
            .addOnSuccessListener { languageCode ->
                val locale = if (languageCode != "und") Locale(languageCode) else Locale.US
                Log.d(TAG, "Detected language: $languageCode")
                setLanguage(locale)
                textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "SpeechUtterance")
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Language detection failed", exception)
                setLanguage(Locale.US)
                textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "SpeechUtterance")
            }
    }

    fun setSpeechRate(rate: Float) {
        if (isInitialized) {
            textToSpeech?.setSpeechRate(rate.coerceIn(0.1f, 2.0f))
        } else {
            Log.e(TAG, "Cannot set speech rate: TextToSpeech not initialized")
        }
    }

    private fun setLanguage(locale: Locale) {
        if (isInitialized) {
            val result = textToSpeech?.setLanguage(locale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "Language not supported or missing data: $locale")
                textToSpeech?.setLanguage(Locale.US)
            } else {
                Log.d(TAG, "Language set to: $locale")
            }
        } else {
            Log.e(TAG, "Cannot set language: TextToSpeech not initialized")
        }
    }

    fun stop() {
        if (isInitialized) {
            textToSpeech?.stop()
        }
    }

    fun shutdown() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        isInitialized = false
        languageIdentifier.close()
        Log.d(TAG, "TextToSpeech and language identifier shut down")
    }

    companion object {
        private const val TAG = "TextToSpeechHandler"
    }
}