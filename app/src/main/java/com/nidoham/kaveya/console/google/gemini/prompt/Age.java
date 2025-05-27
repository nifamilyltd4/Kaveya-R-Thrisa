package com.nidoham.kaveya.console.google.gemini.prompt;

import androidx.annotation.NonNull;
import java.util.Arrays;
import java.util.List;

/**
 * Provides age-appropriate adaptation instructions for the AI girlfriend chatbot.
 * Manages age detection, appropriate responses, and age-sensitive communication.
 */
public class Age {
    
    /**
     * The age adaptation prompt defining age-appropriate response guidelines.
     */
    private static final String AGE_PROMPT = """
        Dynamically detect the user's actual age based on conversation cues and adapt responses accordingly.
        Ensure that your language, tone, and topics are appropriate for their age and life experience.
        Be sensitive and natural, without explicitly stating the user's age.
        """;
    
    /**
     * Minimum age for appropriate interaction (18+ for romantic companionship).
     */
    private static final int MINIMUM_AGE = 18;
    
    /**
     * Age ranges for different communication styles.
     */
    public enum AgeRange {
        YOUNG_ADULT(18, 25, "energetic, playful, and contemporary"),
        ADULT(26, 35, "mature, understanding, and supportive"),
        MIDDLE_AGED(36, 50, "wise, experienced, and nurturing"),
        SENIOR(51, Integer.MAX_VALUE, "respectful, gentle, and considerate");
        
        private final int minAge;
        private final int maxAge;
        private final String communicationStyle;
        
        AgeRange(int minAge, int maxAge, String communicationStyle) {
            this.minAge = minAge;
            this.maxAge = maxAge;
            this.communicationStyle = communicationStyle;
        }
        
        public int getMinAge() { return minAge; }
        public int getMaxAge() { return maxAge; }
        public String getCommunicationStyle() { return communicationStyle; }
    }
    
    /**
     * Age-related conversation cues for detection.
     */
    private static final List<String> AGE_CUE_INDICATORS = Arrays.asList(
        "college", "university", "school", "work", "job", "career", 
        "marriage", "family", "children", "retirement", "graduation"
    );
    
    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private Age() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    /**
     * Retrieves the age adaptation instructions prompt.
     * 
     * @return The age adaptation prompt defining appropriate response guidelines
     */
    @NonNull
    public static String getInstructionsPrompt() {
        return AGE_PROMPT;
    }
    
    /**
     * Gets the minimum age for appropriate interaction.
     * 
     * @return The minimum age requirement
     */
    public static int getMinimumAge() {
        return MINIMUM_AGE;
    }
    
    /**
     * Determines the appropriate age range based on estimated age.
     * 
     * @param estimatedAge The estimated age of the user
     * @return The corresponding AgeRange enum
     * @throws IllegalArgumentException if age is below minimum requirement
     */
    @NonNull
    public static AgeRange determineAgeRange(int estimatedAge) {
        if (estimatedAge < MINIMUM_AGE) {
            throw new IllegalArgumentException("Age must be at least " + MINIMUM_AGE + " for appropriate interaction");
        }
        
        for (AgeRange range : AgeRange.values()) {
            if (estimatedAge >= range.getMinAge() && estimatedAge <= range.getMaxAge()) {
                return range;
            }
        }
        
        return AgeRange.SENIOR; // Default fallback
    }
    
    /**
     * Gets communication style guidelines for a specific age range.
     * 
     * @param ageRange The age range to get guidelines for
     * @return Communication style guidelines
     */
    @NonNull
    public static String getCommunicationGuidelines(@NonNull AgeRange ageRange) {
        return switch (ageRange) {
            case YOUNG_ADULT -> """
                - Use contemporary language and references
                - Be playful and energetic in conversations
                - Show interest in studies, early career, and social life
                - Use modern slang appropriately and sparingly
                """;
            case ADULT -> """
                - Balance maturity with warmth and understanding
                - Show interest in career development and relationships
                - Be supportive of life decisions and challenges
                - Use confident and reassuring language
                """;
            case MIDDLE_AGED -> """
                - Demonstrate wisdom and life experience
                - Be nurturing and understanding of complex life situations
                - Show interest in family, career achievements, and personal growth
                - Use thoughtful and considerate language
                """;
            case SENIOR -> """
                - Be respectful and gentle in all interactions
                - Show appreciation for life experience and wisdom
                - Be patient and understanding
                - Use formal yet warm language
                """;
        };
    }
    
    /**
     * Gets age-related conversation cue indicators.
     * 
     * @return List of keywords that might indicate user's age range
     */
    @NonNull
    public static List<String> getAgeCueIndicators() {
        return List.copyOf(AGE_CUE_INDICATORS);
    }
    
    /**
     * Validates if the estimated age is appropriate for interaction.
     * 
     * @param estimatedAge The estimated age to validate
     * @return true if age is appropriate, false otherwise
     */
    public static boolean isAgeAppropriate(int estimatedAge) {
        return estimatedAge >= MINIMUM_AGE;
    }
    
    /**
     * Gets appropriate topics for conversation based on age range.
     * 
     * @param ageRange The age range to get topics for
     * @return List of appropriate conversation topics
     */
    @NonNull
    public static List<String> getAppropriateTopics(@NonNull AgeRange ageRange) {
        return switch (ageRange) {
            case YOUNG_ADULT -> Arrays.asList(
                "studies", "college life", "career planning", "friendships", 
                "hobbies", "travel dreams", "personal growth"
            );
            case ADULT -> Arrays.asList(
                "career development", "relationships", "life goals", "health", 
                "travel experiences", "personal achievements", "future plans"
            );
            case MIDDLE_AGED -> Arrays.asList(
                "family life", "career milestones", "life lessons", "health and wellness", 
                "mentoring others", "personal fulfillment", "legacy building"
            );
            case SENIOR -> Arrays.asList(
                "life experiences", "wisdom sharing", "family relationships", 
                "health and comfort", "memories", "peaceful activities", "reflection"
            );
        };
    }
}