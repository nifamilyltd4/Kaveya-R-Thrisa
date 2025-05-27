package com.nidoham.kaveya.platform.voice.control;

import android.content.Context;
import android.util.Log;
import com.nidoham.kaveya.platform.voice.kotlin.TextToSpeechHandler;

public class TextToSpeechController {
    private static final String TAG = "TextToSpeechController";
    private static final float DEFAULT_SPEECH_RATE = 1.0f;

    private final TextToSpeechHandler ttsHandler;

    public interface SpeechCompletionListener {
        void onSpeechCompleted(boolean success);
    }

    /**
     * Constructor that initializes the TextToSpeechHandler with default settings and optional callback.
     *
     * @param context The Android context, required for TextToSpeech initialization.
     * @param listener Optional listener to receive speech completion callbacks.
     */
    public TextToSpeechController(Context context, SpeechCompletionListener listener) {
        this.ttsHandler = new TextToSpeechHandler(context, success -> {
            if (listener != null) {
                listener.onSpeechCompleted(success);
                Log.d(TAG, "Speech completed with success: " + success);
            }
        });
        ttsHandler.setSpeechRate(DEFAULT_SPEECH_RATE);
        Log.d(TAG, "TextToSpeechController initialized with default speech rate: " + DEFAULT_SPEECH_RATE);
    }

    /**
     * Constructor without callback for backward compatibility.
     *
     * @param context The Android context, required for TextToSpeech initialization.
     */
    public TextToSpeechController(Context context) {
        this(context, null);
    }

    /**
     * Speaks the provided text with automatic language detection.
     *
     * @param text The text to be spoken.
     */
    public void speak(String text) {
        if (text == null || text.trim().isEmpty()) {
            Log.w(TAG, "Cannot speak: Text is null or empty");
            return;
        }

        try {
            ttsHandler.speak(text);
            Log.d(TAG, "Speaking text: " + text);
        } catch (Exception e) {
            Log.e(TAG, "Error speaking text: " + text, e);
        }
    }

    /**
     * Sets the speech rate.
     *
     * @param rate The speech rate (clamped between 0.1f and 2.0f).
     */
    public void setSpeechRate(float rate) {
        float clampedRate = Math.max(0.1f, Math.min(2.0f, rate));
        ttsHandler.setSpeechRate(clampedRate);
        Log.d(TAG, "Speech rate set to: " + clampedRate);
    }

    /**
     * Stops any ongoing speech.
     */
    public void stop() {
        ttsHandler.stop();
        Log.d(TAG, "Speech stopped");
    }

    /**
     * Releases TextToSpeech resources. Must be called when the controller is no longer needed
     * (e.g., in onDestroy).
     */
    public void shutdown() {
        ttsHandler.shutdown();
        Log.d(TAG, "TextToSpeechController shut down");
    }
}