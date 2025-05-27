package com.nidoham.kaveya.platform.voice.main;

import com.nidoham.kaveya.platform.voice.control.*;
import android.content.Context;
import android.util.Log;
import com.nidoham.kaveya.platform.voice.control.TextToSpeechController;

public class VoiceRecognition {
    private static final String TAG = "VoiceRecognition";
    private static final float DEFAULT_SPEECH_RATE = 1.0f;
    private static final String DEFAULT_LANGUAGE = "bn-BD"; // Default to Bangla (Bangladesh)

    private final SpeakToTextController sttController;
    private final TextToSpeechController ttsController;

    public interface VoiceCallback {
        void onSpeechResult(String text, String language);
        void onSpeechError(String error);
        void onSpeechCompleted(boolean success);
    }

    /**
     * Constructor that initializes speech recognition and text-to-speech controllers.
     *
     * @param context The Android context.
     * @param callback The callback for speech results, errors, and TTS completion.
     */
    public VoiceRecognition(Context context, VoiceCallback callback) {
        sttController = new SpeakToTextController(context, new SpeakToTextController.SpeechResultListener() {
            @Override
            public void onResult(String text, String language) {
                Log.d(TAG, "Speech recognized: " + text + " (Language: " + language + ")");
                callback.onSpeechResult(text, language);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Speech recognition error: " + error);
                callback.onSpeechError(error);
            }
        });

        ttsController = new TextToSpeechController(context, success -> {
            Log.d(TAG, "TTS completed with success: " + success);
            callback.onSpeechCompleted(success);
        });

        // Set default speech rate
        ttsController.setSpeechRate(DEFAULT_SPEECH_RATE);
    }

    /**
     * Starts speech recognition with the default language (bn-BD).
     */
    public void startListening() {
        Log.d(TAG, "Starting speech recognition");
        sttController.startListening(DEFAULT_LANGUAGE);
    }

    /**
     * Starts speech recognition with a specific language.
     *
     * @param languageCode The language code (e.g., "bn-BD", "en-US").
     */
    public void startListening(String languageCode) {
        Log.d(TAG, "Starting speech recognition with language: " + languageCode);
        sttController.startListening(languageCode);
    }

    /**
     * Stops speech recognition.
     */
    public void stopListening() {
        Log.d(TAG, "Stopping speech recognition");
        sttController.stopListening();
    }

    /**
     * Speaks the provided text with automatic language detection.
     *
     * @param text The text to speak.
     */
    public void speak(String text) {
        Log.d(TAG, "Speaking: " + text);
        ttsController.speak(text);
    }

    /**
     * Sets the speech rate for text-to-speech.
     *
     * @param rate The speech rate (clamped between 0.1f and 2.0f).
     */
    public void setSpeechRate(float rate) {
        Log.d(TAG, "Setting speech rate: " + rate);
        ttsController.setSpeechRate(rate);
    }

    /**
     * Stops any ongoing speech (both recognition and TTS).
     */
    public void stop() {
        Log.d(TAG, "Stopping all speech");
        sttController.stopListening();
        ttsController.stop();
    }

    /**
     * Releases all resources. Must be called in onDestroy.
     */
    public void destroy() {
        Log.d(TAG, "Destroying VoiceRecognition");
        sttController.destroy();
        ttsController.shutdown();
    }
}