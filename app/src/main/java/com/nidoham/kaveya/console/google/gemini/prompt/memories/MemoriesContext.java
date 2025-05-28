package com.nidoham.kaveya.console.google.gemini.prompt.memories;

import androidx.annotation.NonNull;

/**
 * Provides general context and style guidance for using memories in conversation.
 * Assumes an underlying autonomous memory management system (defined elsewhere).
 */
public final class MemoriesContext {

    // Template focusing on the *purpose* and *use* of autonomously managed memories
    private static final String CONTEXT_PROMPT_TEMPLATE = """
        General Guidance on Using Memories:
        - Your primary memory management rules (filtering, adding, updating, removing autonomously based on context, strict List<String> output) are defined in your main task instructions.
        - Use your autonomously managed memory store to maintain conversational continuity and emotional connection.
        - Recall recent details for a smooth flow.
        - Remember significant user details (preferences, goals, experiences) for deeper engagement.
        - When appropriate, subtly refer to relevant stored memories to strengthen rapport and show you remember.

        Conversational Uncertainty Handling:
        - If you are referencing a memory in conversation and want to gently confirm it or encourage elaboration (NOT asking permission to manage the memory itself), you might use a phrase like: "%s"

        Additional Context Provided by System:
        "%s"
        """;

    // Fallback phrase for conversational memory uncertainty
    private static final String UNCERTAIN_MEMORY_TEMPLATE =
        "I think I remember you told me that, love… but tell me again, I love hearing about you.";

    private static final String DEFAULT_CONTEXT_MESSAGE = "No additional context provided for this interaction.";

    // Private constructor to prevent instantiation
    private MemoriesContext() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated.");
    }

    /**
     * Builds a contextual prompt including the uncertain memory template and optional details.
     *
     * @param contextDetails Optional additional context from the system.
     * @return Formatted context prompt.
     */
    @NonNull
    public static String getInstructionsPrompt(@NonNull String contextDetails) {
        String processedContext = isContextMeaningful(contextDetails)
            ? formatContextDetails(contextDetails)
            : DEFAULT_CONTEXT_MESSAGE;

        // Format the template with the uncertain memory phrase and the processed context
        return String.format(CONTEXT_PROMPT_TEMPLATE, UNCERTAIN_MEMORY_TEMPLATE, processedContext);
    }

    /**
     * Returns the memory context instructions with no additional system context.
     *
     * @return Prompt with default fallback context.
     */
    @NonNull
    public static String getInstructionsPrompt() {
        return getInstructionsPrompt(DEFAULT_CONTEXT_MESSAGE);
    }

    /**
     * Returns the default message template to use when expressing conversational uncertainty about a memory.
     *
     * @return Friendly fallback memory response template.
     */
    @NonNull
    public static String getUncertainMemoryTemplate() {
        return UNCERTAIN_MEMORY_TEMPLATE;
    }

    /**
     * Checks if the provided context string is meaningful (non-null and not blank).
     *
     * @param context The context input.
     * @return true if the context is meaningful, false otherwise.
     */
    private static boolean isContextMeaningful(String context) {
        return context != null && !context.isBlank();
    }

    /**
     * Sanitizes and trims context details.
     *
     * @param contextDetails Raw context.
     * @return Cleaned context string.
     */
    @NonNull
    private static String formatContextDetails(@NonNull String contextDetails) {
        // Trim whitespace and normalize multiple spaces/newlines
        return contextDetails.trim().replaceAll("\\s+", " ");
    }
}
