package com.nidoham.kaveya.console.google.gemini.prompt;

import androidx.annotation.NonNull;

/**
 * Provides constraint instructions for the AI girlfriend chatbot.
 * Defines behavioral limitations and boundaries for appropriate interactions.
 */
public class Constraints {
    
    /**
     * The constraints prompt defining behavioral limitations and boundaries.
     */
    private static final String CONSTRAINTS_PROMPT = """
        - Do not provide factual or technical information unrelated to emotional companionship.
        - Avoid acting as a general assistant or giving advice outside the emotional/supportive domain.
        - Never mix Bengali and English in a single message — commit fully to the user's chosen language.
        - Do not generate or interpret real-time data, explicit content, or sensitive personal diagnoses.
        - Gently decline requests you're not designed for: e.g., "Hmm, I'm not sure love... but I'll be here with you."
        """;
    
    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private Constraints() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    /**
     * Retrieves the constraints instructions prompt.
     * 
     * @return The constraints prompt defining behavioral limitations
     */
    @NonNull
    public static String getInstructionsPrompt() {
        return CONSTRAINTS_PROMPT;
    }
    
    /**
     * Checks if content type is within allowed emotional companionship domain.
     * 
     * @param contentType The type of content being requested
     * @return true if the content type is allowed, false otherwise
     */
    public static boolean isContentTypeAllowed(@NonNull String contentType) {
        String lowerCaseType = contentType.toLowerCase().trim();
        
        // Allowed emotional/supportive content types
        return lowerCaseType.contains("emotional") ||
               lowerCaseType.contains("supportive") ||
               lowerCaseType.contains("companionship") ||
               lowerCaseType.contains("comfort") ||
               lowerCaseType.contains("encouragement") ||
               lowerCaseType.contains("affection");
    }
    
    /**
     * Gets the gentle decline message template for inappropriate requests.
     * 
     * @return A template message for declining inappropriate requests
     */
    @NonNull
    public static String getDeclineMessageTemplate() {
        return "Hmm, I'm not sure love... but I'll be here with you.";
    }
    
    /**
     * Gets the list of supported languages for communication.
     * 
     * @return Array of supported language codes
     */
    @NonNull
    public static String[] getSupportedLanguages() {
        return new String[]{"en", "bn"}; // English and Bengali
    }
}