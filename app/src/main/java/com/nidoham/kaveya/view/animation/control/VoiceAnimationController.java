package com.nidoham.kaveya.view.animation.control;

import android.graphics.Color;
import android.view.View;

import com.nidoham.kaveya.view.animation.voice.VoiceAnimationView;

public class VoiceAnimationController {
    private final VoiceAnimationView voiceAnimationView;
    private static final long DEFAULT_ANIMATION_DURATION = 1000L; // Matches VoiceAnimationView default

    public VoiceAnimationController(View view, int voiceAnimationViewId) {
        this.voiceAnimationView = view.findViewById(voiceAnimationViewId);
        if (this.voiceAnimationView == null) {
            throw new IllegalArgumentException("VoiceAnimationView with ID " + voiceAnimationViewId + " not found.");
        }
    }

    /**
     * Starts the AI speaking animation with the default duration (1000ms).
     */
    public void startAISpeakingAnimation() {
        voiceAnimationView.startAISpeakingAnimation(DEFAULT_ANIMATION_DURATION);
    }

    /**
     * Starts the AI speaking animation with a custom duration.
     *
     * @param duration Duration of the animation cycle in milliseconds.
     */
    public void startAISpeakingAnimation(long duration) {
        voiceAnimationView.startAISpeakingAnimation(duration);
    }

    /**
     * Starts the user speaking animation with the default duration (1000ms).
     */
    public void startUserSpeakingAnimation() {
        voiceAnimationView.startUserSpeakingAnimation(DEFAULT_ANIMATION_DURATION);
    }

    /**
     * Starts the user speaking animation with a custom duration.
     *
     * @param duration Duration of the animation cycle in milliseconds.
     */
    public void startUserSpeakingAnimation(long duration) {
        voiceAnimationView.startUserSpeakingAnimation(duration);
    }

    /**
     * Sets the amplitude for the user speaking animation (e.g., from microphone input).
     *
     * @param amplitude Amplitude value between 0 and 100.
     */
    public void setAmplitude(float amplitude) {
        voiceAnimationView.setAmplitude(amplitude);
    }

    /**
     * Sets custom colors for the animation.
     *
     * @param coreStartColor  Start color for the core gradient (e.g., Color.WHITE).
     * @param coreEndColor    End color for the core gradient (e.g., Color.GREEN).
     * @param glowColor       Color for the glow effect (e.g., Color.GREEN).
     * @param rippleColor     Color for the ripple effect (e.g., Color.CYAN).
     */
    public void setCustomColors(int coreStartColor, int coreEndColor, int glowColor, int rippleColor) {
        voiceAnimationView.setCustomColors(coreStartColor, coreEndColor, glowColor, rippleColor);
    }

    /**
     * Restores default colors based on the current mode (AI or user).
     *
     * @param isUserMode True for user mode colors, false for AI mode colors.
     */
    public void restoreDefaultColors(boolean isUserMode) {
        // Since setColors is private, we can mimic default colors
        if (isUserMode) {
            voiceAnimationView.setCustomColors(
                    Color.WHITE, Color.YELLOW, Color.YELLOW, Color.YELLOW
            );
        } else {
            voiceAnimationView.setCustomColors(
                    Color.WHITE, // Using android.R.color.white is not directly accessible here
                    Color.parseColor("#FF33B5E5"), // holo_blue_light approximation
                    Color.parseColor("#FF33B5E5"),
                    Color.parseColor("#FF33B5E5")
            );
        }
    }

    /**
     * Stops the animation, allowing ripples to fade out gracefully.
     */
    public void stopAnimation() {
        voiceAnimationView.stopAnimation();
    }

    /**
     * Checks if the animation is currently running.
     *
     * @return True if the animation is active, false otherwise.
     */
    public boolean isAnimating() {
        return voiceAnimationView.isAnimating();
    }
}