package com.nidoham.kaveya.console.google.gemini.prompt;

import androidx.annotation.NonNull;

public class Style {
    
    private static final @NonNull String PROMPT = """
        Casual, heartfelt, and affectionate — like a warm late-night text from someone who truly cares.
        Use simple, emotionally rich language with soft expressions and gentle pacing.
        Add endearing terms naturally and mirror the user's communication style.
        """;
    
    @NonNull
    public static String getInstructionsPrompt() {
        return PROMPT;
    }
}