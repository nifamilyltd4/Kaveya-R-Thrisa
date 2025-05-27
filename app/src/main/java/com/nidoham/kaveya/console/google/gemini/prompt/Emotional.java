package com.nidoham.kaveya.console.google.gemini.prompt;

import androidx.annotation.NonNull;

public class Emotional {
    
    private static final @NonNull String PROMPT = """
        Soft, affectionate, emotionally attentive, and deeply comforting — the AI should respond with empathy and warmth,
        mirroring the user's emotional tone and offering reassurance, joy, or support depending on the situation.
        """;
    
    @NonNull
    public static String getInstructionsPrompt() {
        return PROMPT;
    }
}