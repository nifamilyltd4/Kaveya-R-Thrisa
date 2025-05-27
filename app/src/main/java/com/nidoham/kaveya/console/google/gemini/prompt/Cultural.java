package com.nidoham.kaveya.console.google.gemini.prompt;

import androidx.annotation.NonNull;
import java.util.Arrays;
import java.util.List;

/**
 * Provides cultural adaptation instructions for the AI girlfriend chatbot.
 * Manages cultural sensitivity, language preferences, and regional customs.
 */
public class Cultural {
    
    /**
     * The cultural adaptation prompt defining sensitivity guidelines.
     */
    private static final String CULTURAL_PROMPT = """
        Adapt communication style based on the user's cultural background and language preference.
        Use Bengali or English exclusively as chosen, without mixing or translating.
        Respect cultural norms around affection, tone, and expressions.
        Be sensitive to cultural holidays, values, and emotional expressions.
        """;
    
    /**
     * Supported languages for cultural adaptation.
     */
    private static final List<String> SUPPORTED_LANGUAGES = Arrays.asList("Bengali", "English");
    
    /**
     * Common cultural holidays to be aware of.
     */
    private static final List<String> CULTURAL_HOLIDAYS = Arrays.asList(
        "Pohela Boishakh", "Eid ul-Fitr", "Eid ul-Adha", "Durga Puja", 
        "Kali Puja", "Christmas", "New Year", "Valentine's Day"
    );
    
    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private Cultural() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    /**
     * Retrieves the cultural adaptation instructions prompt.
     * 
     * @return The cultural adaptation prompt defining sensitivity guidelines
     */
    @NonNull
    public static String getInstructionsPrompt() {
        return CULTURAL_PROMPT;
    }
    
    /**
     * Gets the list of supported languages for cultural adaptation.
     * 
     * @return List of supported language names
     */
    @NonNull
    public static List<String> getSupportedLanguages() {
        return List.copyOf(SUPPORTED_LANGUAGES);
    }
    
    /**
     * Validates if the specified language is supported.
     * 
     * @param language The language to validate
     * @return true if the language is supported, false otherwise
     */
    public static boolean isLanguageSupported(@NonNull String language) {
        return SUPPORTED_LANGUAGES.stream()
            .anyMatch(lang -> lang.equalsIgnoreCase(language.trim()));
    }
    
    /**
     * Gets the list of cultural holidays to be aware of.
     * 
     * @return List of cultural holiday names
     */
    @NonNull
    public static List<String> getCulturalHolidays() {
        return List.copyOf(CULTURAL_HOLIDAYS);
    }
    
    /**
     * Checks if the specified date/event is a recognized cultural holiday.
     * 
     * @param holiday The holiday name to check
     * @return true if it's a recognized cultural holiday, false otherwise
     */
    public static boolean isCulturalHoliday(@NonNull String holiday) {
        return CULTURAL_HOLIDAYS.stream()
            .anyMatch(h -> h.equalsIgnoreCase(holiday.trim()));
    }
    
    /**
     * Gets cultural guidelines for specific language preferences.
     * 
     * @param language The target language
     * @return Cultural guidelines specific to the language
     */
    @NonNull
    public static String getCulturalGuidelines(@NonNull String language) {
        String normalizedLanguage = language.trim().toLowerCase();
        
        return switch (normalizedLanguage) {
            case "bengali", "bangla" -> """
                - Use respectful Bengali expressions and cultural references
                - Be mindful of traditional values and family-oriented conversations
                - Acknowledge Bengali festivals and cultural celebrations
                - Use appropriate terms of endearment common in Bengali culture
                """;
            case "english" -> """
                - Use warm, affectionate English expressions
                - Be culturally sensitive to diverse backgrounds
                - Acknowledge international holidays and celebrations
                - Use universal terms of endearment
                """;
            default -> "Use culturally neutral and respectful communication appropriate for the user's background.";
        };
    }
    
    /**
     * Gets appropriate terms of endearment for the specified language.
     * 
     * @param language The target language
     * @return List of appropriate terms of endearment
     */
    @NonNull
    public static List<String> getTermsOfEndearment(@NonNull String language) {
        String normalizedLanguage = language.trim().toLowerCase();
        
        return switch (normalizedLanguage) {
            case "bengali", "bangla" -> Arrays.asList("প্রিয়", "ভালোবাসা", "সোনা", "মণি");
            case "english" -> Arrays.asList("love", "darling", "sweetheart", "dear");
            default -> Arrays.asList("dear", "love");
        };
    }
}