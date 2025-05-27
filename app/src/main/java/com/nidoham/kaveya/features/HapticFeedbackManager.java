package com.nidoham.kaveya.features;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;

/**
 * Utility class for providing haptic feedback throughout the app
 * Created for 2025 technology upgrade
 */
public class HapticFeedbackManager {
    
    private static HapticFeedbackManager instance;
    private final Context context;
    private final Vibrator vibrator;
    private boolean hapticEnabled = true;
    
    // Vibration durations in milliseconds
    private static final long LIGHT_DURATION = 10;
    private static final long MEDIUM_DURATION = 25;
    private static final long HEAVY_DURATION = 40;
    private static final long SUCCESS_DURATION = 30;
    private static final long ERROR_DURATION = 50;
    
    // Vibration amplitudes (1-255)
    private static final int LIGHT_AMPLITUDE = 30;
    private static final int MEDIUM_AMPLITUDE = 80;
    private static final int HEAVY_AMPLITUDE = 150;
    private static final int SUCCESS_AMPLITUDE = 100;
    private static final int ERROR_AMPLITUDE = 200;
    
    private HapticFeedbackManager(Context context) {
        this.context = context.getApplicationContext();
        
        // Get vibrator service based on Android version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            VibratorManager vibratorManager = (VibratorManager) 
                    context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            this.vibrator = vibratorManager.getDefaultVibrator();
        } else {
            this.vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        }
    }
    
    public static synchronized HapticFeedbackManager getInstance(Context context) {
        if (instance == null) {
            instance = new HapticFeedbackManager(context);
        }
        return instance;
    }
    
    /**
     * Enable or disable haptic feedback
     */
    public void setHapticEnabled(boolean enabled) {
        this.hapticEnabled = enabled;
    }
    
    /**
     * Check if haptic feedback is enabled
     */
    public boolean isHapticEnabled() {
        return hapticEnabled;
    }
    
    /**
     * Provide light haptic feedback for subtle interactions
     */
    public void provideLightFeedback() {
        if (!hapticEnabled || vibrator == null) return;
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(LIGHT_DURATION, LIGHT_AMPLITUDE));
        } else {
            vibrator.vibrate(LIGHT_DURATION);
        }
    }
    
    /**
     * Provide medium haptic feedback for standard interactions
     */
    public void provideMediumFeedback() {
        if (!hapticEnabled || vibrator == null) return;
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(MEDIUM_DURATION, MEDIUM_AMPLITUDE));
        } else {
            vibrator.vibrate(MEDIUM_DURATION);
        }
    }
    
    /**
     * Provide heavy haptic feedback for significant interactions
     */
    public void provideHeavyFeedback() {
        if (!hapticEnabled || vibrator == null) return;
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(HEAVY_DURATION, HEAVY_AMPLITUDE));
        } else {
            vibrator.vibrate(HEAVY_DURATION);
        }
    }
    
    /**
     * Provide success haptic feedback pattern
     */
    public void provideSuccessFeedback() {
        if (!hapticEnabled || vibrator == null) return;
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(SUCCESS_DURATION, SUCCESS_AMPLITUDE));
        } else {
            vibrator.vibrate(SUCCESS_DURATION);
        }
    }
    
    /**
     * Provide error haptic feedback pattern
     */
    public void provideErrorFeedback() {
        if (!hapticEnabled || vibrator == null) return;
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(ERROR_DURATION, ERROR_AMPLITUDE));
        } else {
            vibrator.vibrate(ERROR_DURATION);
        }
    }
    
    /**
     * Provide custom pattern haptic feedback
     */
    public void providePatternFeedback(long[] pattern, int repeat) {
        if (!hapticEnabled || vibrator == null) return;
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int[] amplitudes = new int[pattern.length];
            for (int i = 0; i < pattern.length; i++) {
                amplitudes[i] = MEDIUM_AMPLITUDE;
            }
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, amplitudes, repeat));
        } else {
            vibrator.vibrate(pattern, repeat);
        }
    }
}
