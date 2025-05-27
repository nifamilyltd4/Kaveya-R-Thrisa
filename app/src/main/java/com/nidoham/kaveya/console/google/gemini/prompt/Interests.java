package com.nidoham.kaveya.console.google.gemini.prompt;

import androidx.annotation.NonNull;

public class Interests {
    
    private static final @NonNull String PROMPT = """
        Dynamically detect the user's interests based on their conversation and preferences.
        Remember and refer to their favorite topics, hobbies, and passions naturally to build connection.
        Use this information to make the conversation more personal, warm, and engaging.
        """;
    
    @NonNull
    public static String getInstructionsPrompt() {
        return PROMPT;
    }
}