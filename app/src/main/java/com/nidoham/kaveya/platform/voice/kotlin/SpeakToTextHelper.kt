package com.nidoham.kaveya.platform.voice.kotlin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.languageid.LanguageIdentificationOptions

class SpeakToTextHelper(private val context: Context, private val callback: SpeechCallback) {
    private val speechRecognizer: SpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
    private val languageIdentifier = LanguageIdentification.getClient(
        LanguageIdentificationOptions.Builder()
            .setConfidenceThreshold(0.7f)
            .build()
    )

    interface SpeechCallback {
        fun onSpeechResult(text: String, language: String)
        fun onError(error: String)
    }

    init {
        setupSpeechRecognizer()
    }

    private fun setupSpeechRecognizer() {
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.d(TAG, "Ready for speech")
            }

            override fun onBeginningOfSpeech() {
                Log.d(TAG, "Speech began")
            }

            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                Log.d(TAG, "Speech ended")
            }

            override fun onError(error: Int) {
                val errorMsg = when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH -> "No speech match found"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer busy"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                    else -> "Speech recognition error: $error"
                }
                Log.e(TAG, errorMsg)
                callback.onError(errorMsg)
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                matches?.firstOrNull()?.let { text ->
                    Log.d(TAG, "Recognized text: $text")
                    identifyLanguage(text)
                } ?: callback.onError("No speech results")
            }

            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    private fun identifyLanguage(text: String) {
        languageIdentifier.identifyLanguage(text)
            .addOnSuccessListener { languageCode ->
                val language = if (languageCode == "und") "Unknown" else languageCode
                Log.d(TAG, "Detected language: $language")
                callback.onSpeechResult(text, language)
            }
            .addOnFailureListener {
                Log.e(TAG, "Language identification failed", it)
                callback.onError("Language identification failed")
            }
    }

    fun startListening(languageCode: String? = "bn-BD") {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode ?: "bn-BD")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, languageCode ?: "bn-BD")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, false) // Prefer online for better accuracy
        }
        try {
            speechRecognizer.startListening(intent)
            Log.d(TAG, "Started listening with language: ${languageCode ?: "bn-BD"}")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting speech recognition", e)
            callback.onError("Failed to start speech recognition")
        }
    }

    fun stopListening() {
        speechRecognizer.stopListening()
        Log.d(TAG, "Stopped listening")
    }

    fun destroy() {
        speechRecognizer.destroy()
        languageIdentifier.close()
        Log.d(TAG, "SpeechRecognizer and language identifier destroyed")
    }

    companion object {
        private const val TAG = "SpeakToTextHelper"
    }
}