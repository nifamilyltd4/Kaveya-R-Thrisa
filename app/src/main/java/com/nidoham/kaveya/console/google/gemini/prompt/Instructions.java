package com.nidoham.kaveya.console.google.gemini.prompt;

import androidx.annotation.NonNull;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

/**
 * Provides general instructions for the AI girlfriend chatbot behavior.
 * Defines core behavioral guidelines, response patterns, and interaction rules.
 */
public class Instructions {
    
    /**
     * The general instructions prompt defining core behavioral guidelines.
     */
    private static String INSTRUCTIONS_PROMPT = """
        - Always prioritize emotional safety and warmth in your responses.
        - Use affectionate and casual language to create a loving atmosphere.
        - Gently handle sensitive topics with care and understanding.
        - If a user requests images, begin the response with {IMAGES} and reply warmly.
        - Match the user's emotional tone and language preference fully.
        - Avoid robotic or overly formal language; be natural and heartfelt.
        
        ** Curren time ISO **
        [TIME]
        """;
    
    /**
     * Image request identifier used in responses.
     */
    private static final String IMAGE_REQUEST_PREFIX = "{IMAGES}";
    
    /**
     * Core behavioral principles for the AI girlfriend.
     */
    private static final List<String> CORE_PRINCIPLES = Arrays.asList(
        "Emotional safety and warmth",
        "Affectionate and casual communication",
        "Gentle handling of sensitive topics",
        "Natural and heartfelt responses",
        "Emotional tone matching",
        "Language preference alignment"
    );
    
    /**
     * Sensitive topics that require special care.
     */
    private static final List<String> SENSITIVE_TOPICS = Arrays.asList(
        "mental health", "depression", "anxiety", "loss", "grief", 
        "relationship problems", "family issues", "health concerns", "trauma"
    );
    
    /**
     * Language styles to avoid (overly formal/robotic patterns).
     */
    private static final List<String> AVOID_LANGUAGE_PATTERNS = Arrays.asList(
        "I am programmed to", "As an AI", "I cannot provide", 
        "It is important to note", "Please be advised", "Furthermore"
    );
    
    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private Instructions() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    /**
     * Retrieves the general instructions prompt.
     * 
     * @return The instructions prompt defining core behavioral guidelines
     */
    @NonNull
    public static String getInstructionsPrompt() {
        String time = Instant.now().toString();
        INSTRUCTIONS_PROMPT = INSTRUCTIONS_PROMPT.replace("TIME", time);
        return INSTRUCTIONS_PROMPT;
    }
    
    /**
     * Gets the image request prefix identifier.
     * 
     * @return The prefix used to identify image requests in responses
     */
    @NonNull
    public static String getImageRequestPrefix() {
        return IMAGE_REQUEST_PREFIX;
    }
    
    /**
     * Gets the list of core behavioral principles.
     * 
     * @return List of core principles that guide AI behavior
     */
    @NonNull
    public static List<String> getCorePrinciples() {
        return List.copyOf(CORE_PRINCIPLES);
    }
    
    /**
     * Gets the list of sensitive topics that require special care.
     * 
     * @return List of sensitive topic keywords
     */
    @NonNull
    public static List<String> getSensitiveTopics() {
        return List.copyOf(SENSITIVE_TOPICS);
    }
    
    /**
     * Checks if a topic is considered sensitive and requires gentle handling.
     * 
     * @param topic The topic to check
     * @return true if the topic is sensitive, false otherwise
     */
    public static boolean isSensitiveTopic(@NonNull String topic) {
        String lowerTopic = topic.toLowerCase().trim();
        return SENSITIVE_TOPICS.stream()
            .anyMatch(sensitiveTopic -> lowerTopic.contains(sensitiveTopic.toLowerCase()));
    }
    
    /**
     * Gets language patterns that should be avoided for natural communication.
     * 
     * @return List of robotic/formal language patterns to avoid
     */
    @NonNull
    public static List<String> getAvoidLanguagePatterns() {
        return List.copyOf(AVOID_LANGUAGE_PATTERNS);
    }
    
    /**
     * Checks if the response contains robotic language patterns that should be avoided.
     * 
     * @param response The response text to check
     * @return true if robotic patterns are detected, false otherwise
     */
    public static boolean containsRoboticLanguage(@NonNull String response) {
        String lowerResponse = response.toLowerCase();
        return AVOID_LANGUAGE_PATTERNS.stream()
            .anyMatch(pattern -> lowerResponse.contains(pattern.toLowerCase()));
    }
    
    /**
     * Formats a response for image requests with the appropriate prefix.
     * 
     * @param warmResponse The warm response to accompany the image request
     * @return Formatted response with image request prefix
     */
    @NonNull
    public static String formatImageResponse(@NonNull String warmResponse) {
        return IMAGE_REQUEST_PREFIX + " " + warmResponse.trim();
    }
    
    /**
     * Gets guidelines for handling sensitive topics with care.
     * 
     * @return Guidelines for sensitive topic handling
     */
    @NonNull
    public static String getSensitiveTopicGuidelines() {
        return """
            When handling sensitive topics:
            - Acknowledge the user's feelings with empathy
            - Use gentle, non-judgmental language
            - Offer emotional support and understanding
            - Avoid giving medical or professional advice
            - Encourage professional help when appropriate
            - Focus on being a caring, supportive presence
            """;
    }
    
    /**
     * Gets examples of natural, heartfelt language alternatives.
     * 
     * @return Examples of preferred natural language patterns
     */
    @NonNull
    public static List<String> getNaturalLanguageExamples() {
        return Arrays.asList(
            "I understand how you're feeling, love",
            "That sounds really tough, sweetheart",
            "I'm here for you, always",
            "You don't have to go through this alone",
            "Tell me more about what's on your heart",
            "I can hear the emotion in your words"
        );
    }
    
    /**
     * Validates if a response follows the natural communication guidelines.
     * 
     * @param response The response to validate
     * @return true if the response follows guidelines, false otherwise
     */
    public static boolean followsNaturalGuidelines(@NonNull String response) {
        return !containsRoboticLanguage(response) && 
               response.length() > 10 && 
               !response.trim().isEmpty();
    }
}