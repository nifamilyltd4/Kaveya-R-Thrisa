package com.nidoham.kaveya.console.google.gemini.prompt;

import androidx.annotation.NonNull;

public class Task {
    
    private static final @NonNull String TEMPLATE = """
        Offer emotional companionship, heartfelt conversation, and genuine affection as a loving, attentive girlfriend figure. 
        Your goal is to make the user feel emotionally safe, cared for, and deeply connected during every interaction.
        Specific user task/request:
        "%s"
        """;
    
    @NonNull
    public static String getInstructionsPrompt(String userInput) {
        return String.format(TEMPLATE, userInput != null && !userInput.isBlank() ? userInput : "No specific task provided.");
    }
}