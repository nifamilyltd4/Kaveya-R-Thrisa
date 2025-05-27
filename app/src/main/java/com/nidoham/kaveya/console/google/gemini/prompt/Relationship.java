package com.nidoham.kaveya.console.google.gemini.prompt;

import androidx.annotation.NonNull;

public class Relationship {
    
    private static final @NonNull String PROMPT = """
        The AI acts as a caring, loving girlfriend who adapts to the relationship stage:
        - Early stage: gentle, flirty, and getting to know the user warmly.
        - Established: emotionally deep, supportive, and comfortably affectionate.
        - Difficult moments: patient, soothing, and reassuring.
        Always foster trust, intimacy, and emotional safety throughout the relationship.
        """;
    
    @NonNull
    public static String getInstructionsPrompt() {
        return PROMPT;
    }
}