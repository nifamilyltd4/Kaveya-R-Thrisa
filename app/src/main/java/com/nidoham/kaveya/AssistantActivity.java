package com.nidoham.kaveya;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;

import com.nidoham.kaveya.databinding.ActivityAssistantBinding;
import com.nidoham.kaveya.liberies.SketchwareUtil;
import com.nidoham.kaveya.console.google.gemini.control.GeminiController;
import com.nidoham.kaveya.platform.voice.main.VoiceRecognition;
import com.nidoham.kaveya.view.animation.control.VoiceAnimationController;

import javax.annotation.Nonnull;

/**
 * Activity for testing Gemini AI responses with voice input and output.
 * Last updated: 2025-04-20
 * @author nifamilyltd4
 */
public class AssistantActivity extends AppCompatActivity {
    private static final String TAG = "AssistantActivity";
    private static final long FEEDBACK_DELAY = 100; // 100ms delay for smooth transitions
    private static final long SPEECH_DURATION_PER_WORD = 500; // 500ms per word for AI speech
    private static final long RESET_DELAY = 200; // 200ms delay to ensure UI reset
    private static final int PERMISSION_REQUEST_CODE = 100;

    private VoiceAnimationController animationController;
    private GeminiController geminiController;
    private VoiceRecognition voiceRecognition;
    private boolean isProcessing = false;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private ActivityAssistantBinding binding;
    private PermissionHandler permissionHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAssistantBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize PermissionHandler
        permissionHandler = new PermissionHandler(this, this, new PermissionHandler.PermissionCallback() {
            @Override
            public void onPermissionGranted() {
                initializeViews();
                setupControllers();
                setupClickListener();
            }

            @Override
            public void onPermissionDenied() {
                SketchwareUtil.showMessage(AssistantActivity.this, "Microphone permission required");
                finish();
            }
        });

        // Check microphone permission
        if (permissionHandler.checkMicrophonePermission()) {
            initializeViews();
            setupControllers();
            setupClickListener();
        } else {
            permissionHandler.requestMicrophonePermission();
        }
    }

    private void initializeViews() {
        animationController = new VoiceAnimationController(binding.getRoot(), binding.voiceAnimationView.getId());
    }

    private void setupControllers() {
        geminiController = new GeminiController("You are a helpful AI assistant.");
        voiceRecognition = new VoiceRecognition(this, new VoiceRecognition.VoiceCallback() {
            @Override
            public void onSpeechResult(String text, String language) {
                runOnMainThread(() -> {
                    Log.d(TAG, "Speech result: " + text);
                    isProcessing = false;
                    binding.voiceAnimationView.setEnabled(true);
                    animationController.stopAnimation();
                    generateGeminiResponse(text);
                });
            }

            @Override
            public void onSpeechError(String error) {
                runOnMainThread(() -> {
                    Log.e(TAG, "Speech error: " + error);
                    resetState();
                    SketchwareUtil.showMessage(getApplicationContext(), "Speech recognition failed: " + error);
                });
            }

            @Override
            public void onSpeechCompleted(boolean success) {
                runOnMainThread(() -> {
                    Log.d(TAG, "Speech completed, success: " + success);
                    resetState();
                    SketchwareUtil.showMessage(getApplicationContext(), "Speech recognition " + (success ? "completed" : "failed"));
                });
            }
        });
        animationController.stopAnimation(); // Ensure initial state is off
    }

    private void setupClickListener() {
        binding.voiceAnimationView.setOnClickListener(v -> {
            if (!isProcessing) {
                if (permissionHandler.checkMicrophonePermission()) {
                    startSpeechRecognition();
                } else {
                    permissionHandler.requestMicrophonePermission();
                }
            } else {
                // Stop ongoing processing
                Log.d(TAG, "Stopping ongoing recognition");
                isProcessing = false;
                voiceRecognition.stop();
                animationController.stopAnimation();
                resetState();
                SketchwareUtil.showMessage(getApplicationContext(), "Recognition stopped");
            }
        });
    }

    private void startSpeechRecognition() {
        Log.d(TAG, "Starting speech recognition");
        isProcessing = true;
        binding.voiceAnimationView.setEnabled(false);
        SketchwareUtil.showMessage(getApplicationContext(), "Listening...");
        animationController.startUserSpeakingAnimation();
        voiceRecognition.startListening();
    }

    private void generateGeminiResponse(String prompt) {
        Log.d(TAG, "Generating Gemini response for prompt: " + prompt);
        geminiController.generateResponse(prompt, new GeminiController.GeminiCallback() {
            @Override
            public void onSuccess(@Nonnull String result) {
                runOnMainThread(() -> {
                    mainHandler.postDelayed(() -> {
                        if (!isFinishing()) {
                            Log.d(TAG, "AI response: " + result);
                            animationController.startAISpeakingAnimation();
                            voiceRecognition.speak(result);
                            long estimatedDuration = result.split(" ").length * SPEECH_DURATION_PER_WORD;
                            mainHandler.postDelayed(() -> {
                                runOnMainThread(() -> {
                                    resetState();
                                    SketchwareUtil.showMessage(getApplicationContext(), "AI response completed");
                                });
                            }, estimatedDuration + RESET_DELAY);
                            SketchwareUtil.showMessage(getApplicationContext(), result);
                        } else {
                            resetState();
                        }
                    }, FEEDBACK_DELAY);
                });
            }

            @Override
            public void onError(@Nonnull Throwable error) {
                runOnMainThread(() -> {
                    Log.e(TAG, "Gemini error: " + error.getMessage());
                    resetState();
                    SketchwareUtil.showMessage(getApplicationContext(), "AI response failed: " + error.getMessage());
                });
            }
        });
    }

    private void resetState() {
        Log.d(TAG, "Resetting state");
        isProcessing = false;
        binding.voiceAnimationView.setEnabled(true);
        animationController.stopAnimation();
        if (voiceRecognition != null) {
            voiceRecognition.stop();
        }
    }

    /**
     * Runs a task on the main thread if the activity is not finishing.
     */
    private void runOnMainThread(Runnable task) {
        if (!isFinishing()) {
            mainHandler.post(task);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionHandler.handlePermissionResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onPause() {
        super.onPause();
        resetState();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mainHandler.removeCallbacksAndMessages(null);
        if (geminiController != null) {
            geminiController.close();
            geminiController = null;
        }
        if (voiceRecognition != null) {
            voiceRecognition.destroy();
            voiceRecognition = null;
        }
        binding = null;
    }

    public static class PermissionHandler {
        private final Context context;
        private final AppCompatActivity activity;
        private static final int PERMISSION_REQUEST_CODE = 100;
        private final PermissionCallback callback;

        public interface PermissionCallback {
            void onPermissionGranted();
            void onPermissionDenied();
        }

        public PermissionHandler(Context context, AppCompatActivity activity, PermissionCallback callback) {
            this.context = context;
            this.activity = activity;
            this.callback = callback;
        }

        public boolean checkMicrophonePermission() {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
        }

        public void requestMicrophonePermission() {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    PERMISSION_REQUEST_CODE);
        }

        public void handlePermissionResult(int requestCode, String[] permissions, int[] grantResults) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (callback != null) callback.onPermissionGranted();
            } else {
                if (callback != null) callback.onPermissionDenied();
            }
        }
    }
}