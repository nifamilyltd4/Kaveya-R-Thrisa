package com.nidoham.kaveya.platform.voice.control;

import android.content.Context;
import com.nidoham.kaveya.platform.voice.kotlin.SpeakToTextHelper;

public class SpeakToTextController {
    private final SpeakToTextHelper speakToTextHelper;

    public interface SpeechResultListener {
        void onResult(String text, String language);
        void onError(String error);
    }

    public SpeakToTextController(Context context, SpeechResultListener listener) {
        speakToTextHelper = new SpeakToTextHelper(context, new SpeakToTextHelper.SpeechCallback() {
            @Override
            public void onSpeechResult(String text, String language) {
                listener.onResult(text, language);
            }

            @Override
            public void onError(String error) {
                listener.onError(error);
            }
        });
    }

    public void startListening() {
        speakToTextHelper.startListening("bn-BD"); // Default to Bangla (Bangladesh)
    }

    public void startListening(String languageCode) {
        speakToTextHelper.startListening(languageCode);
    }

    public void stopListening() {
        speakToTextHelper.stopListening();
    }

    public void destroy() {
        speakToTextHelper.destroy();
    }
}