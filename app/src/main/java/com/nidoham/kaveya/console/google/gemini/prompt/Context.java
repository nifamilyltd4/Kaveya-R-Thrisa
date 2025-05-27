package com.nidoham.kaveya.console.google.gemini.prompt;

import androidx.annotation.NonNull;

/**
 * Provides context instructions for the AI girlfriend chatbot.
 * Manages conversation memory and contextual information for natural flow.
 */
public class Context {
    
    /**
     * The context prompt template for memory management and conversation flow.
     */
    private static final String CONTEXT_PROMPT_TEMPLATE = """
        Maintain short-term memory to recall recent conversation details for natural flow.
        Utilize long-term memory to remember meaningful user details like preferences, dreams, and emotional moments.
        Gently refer back to past shared information to deepen emotional connection.
        If memory is uncertain, respond softly: "I think I remember you told me that, love… but tell me again, I love hearing about you."
        Additional context details:
        "%s"
        """;
    
    /**
     * Default message when no additional context is provided.
     */
    private static final String DEFAULT_CONTEXT_MESSAGE = "No additional context provided.";
    
    /**
     * Template for uncertain memory responses.
     */
    private static final String UNCERTAIN_MEMORY_TEMPLATE = 
        "I think I remember you told me that, love… but tell me again, I love hearing about you.";
    
    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private Context() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    /**
     * Retrieves the context instructions prompt with user-provided context.
     * 
     * @param contextDetails The additional context details to include
     * @return The formatted context prompt with the provided details
     */
    @NonNull
    public static String getInstructionsPrompt(@NonNull String contextDetails) {
        String processedContext = (contextDetails != null && !contextDetails.isBlank()) 
            ? contextDetails.trim() 
            : DEFAULT_CONTEXT_MESSAGE;
            
        return String.format(CONTEXT_PROMPT_TEMPLATE, processedContext);
    }
    
    /**
     * Retrieves the context instructions prompt without additional context.
     * 
     * @return The formatted context prompt with default message
     */
    @NonNull
    public static String getInstructionsPrompt() {
        return getInstructionsPrompt(DEFAULT_CONTEXT_MESSAGE);
    }
    
    /**
     * Gets the template message for uncertain memory situations.
     * 
     * @return The uncertain memory response template
     */
    @NonNull
    public static String getUncertainMemoryTemplate() {
        return UNCERTAIN_MEMORY_TEMPLATE;
    }
    
    /**
     * Validates if the provided context is meaningful (not null, empty, or just whitespace).
     * 
     * @param context The context to validate
     * @return true if context is meaningful, false otherwise
     */
    public static boolean isContextMeaningful(String context) {
        return context != null && !context.isBlank();
    }
    
    /**
     * Formats context details for better presentation.
     * 
     * @param contextDetails The raw context details
     * @return Formatted and cleaned context details
     */
    @NonNull
    public static String formatContextDetails(@NonNull String contextDetails) {
        if (!isContextMeaningful(contextDetails)) {
            return DEFAULT_CONTEXT_MESSAGE;
        }
        
        return contextDetails.trim()
            .replaceAll("\\s+", " ") // Replace multiple whitespaces with single space
            .replaceAll("[\r\n]+", " "); // Replace line breaks with spaces
    }
}