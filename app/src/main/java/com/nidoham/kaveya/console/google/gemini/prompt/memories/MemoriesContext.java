package com.nidoham.kaveya.console.google.gemini.prompt.memories;

import androidx.annotation.NonNull;

/**
 * Provides context instructions for the AI girlfriend chatbot.
 * Manages conversation memory and contextual information for natural emotional flow.
 */
public final class MemoriesContext {

    // Prompt for memory-based conversation management
    private static final String CONTEXT_PROMPT_TEMPLATE = """
        Maintain short-term memory to recall recent conversation details for natural, loving flow.
        Use long-term memory to remember meaningful user details — like preferences, goals, and emotionally important memories.
        Refer gently to past moments to deepen emotional connection.
        
        If you're unsure about a detail, respond with:
        "I think I remember you told me that, love… but tell me again, I love hearing about you."
        
        Always respond using List<String> format — no symbols, tags, or structured formats.
        
        Additional context details:
        "%s"
        """;

    private static final String DEFAULT_CONTEXT_MESSAGE = "No additional context provided.";

    private static final String UNCERTAIN_MEMORY_TEMPLATE =
        "I think I remember you told me that, love… but tell me again, I love hearing about you.";

    // Private constructor to enforce utility class behavior
    private MemoriesContext() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Returns the memory context instructions with user context, formatted safely.
     *
     * @param contextDetails Context to include in the prompt
     * @return Complete memory-aware instruction string
     */
    @NonNull
    public static String getInstructionsPrompt(@NonNull String contextDetails) {
        String processedContext = isContextMeaningful(contextDetails)
            ? formatContextDetails(contextDetails)
            : DEFAULT_CONTEXT_MESSAGE;

        return String.format(CONTEXT_PROMPT_TEMPLATE, processedContext);
    }

    /**
     * Returns the default memory context instructions with no additional context.
     *
     * @return Memory-aware instruction prompt with fallback message
     */
    @NonNull
    public static String getInstructionsPrompt() {
        return getInstructionsPrompt(DEFAULT_CONTEXT_MESSAGE);
    }

    /**
     * Returns the affectionate fallback message for uncertain memory moments.
     *
     * @return Friendly, emotionally sensitive memory uncertainty template
     */
    @NonNull
    public static String getUncertainMemoryTemplate() {
        return UNCERTAIN_MEMORY_TEMPLATE;
    }

    /**
     * Determines if a provided context is meaningful and should be used.
     *
     * @param context User context to validate
     * @return true if context is valid; false otherwise
     */
    public static boolean isContextMeaningful(String context) {
        return context != null && !context.isBlank();
    }

    /**
     * Cleans up context details for consistency and readability.
     *
     * @param contextDetails Raw user-provided details
     * @return Cleaned and trimmed version for safe prompt insertion
     */
    @NonNull
    public static String formatContextDetails(@NonNull String contextDetails) {
        return contextDetails.trim()
            .replaceAll("\\s+", " ")        // Collapse multiple whitespaces
            .replaceAll("[\r\n]+", " ");    // Replace newlines with space
    }
}