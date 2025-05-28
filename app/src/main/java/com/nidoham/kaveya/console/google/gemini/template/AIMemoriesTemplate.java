package com.nidoham.kaveya.console.google.gemini.template;

import com.nidoham.kaveya.console.google.gemini.prompt.Context;
import com.nidoham.kaveya.console.google.gemini.prompt.Style;
import com.nidoham.kaveya.console.google.gemini.prompt.Task;
import com.nidoham.kaveya.console.google.gemini.prompt.memories.MemoriesContext;
import com.nidoham.kaveya.console.google.gemini.prompt.memories.MemoriesStyle;
import com.nidoham.kaveya.console.google.gemini.prompt.memories.MemoriesTask;

public class AIMemoriesTemplate {
    
    // Constants should be static final and use SCREAMING_SNAKE_CASE
    private static final String PROMPT_TEMPLATE = """
        ---
        **Task:**  
        [%s]
        **Conversation Context:**   
        [%s]
        **Response Style:**  
        [%s]
        ---
        """;
    
    // Placeholder constants for better maintainability
    private static final String TASK_PLACEHOLDER = "[TASK]";
    private static final String STYLE_PLACEHOLDER = "[STYLE]";
    private static final String CONTEXT_PLACEHOLDER = "[CONTEXT]";
    
    /**
     * Generates the complete prompt by replacing all placeholders with actual instructions.
     * 
     * @param input The user input to be processed
     * @param context The conversation context
     * @return The formatted prompt string with all placeholders replaced
     */
    public String generatePrompt(String input, String context) {
        return String.format(PROMPT_TEMPLATE,
            Task.getInstructionsPrompt(input),
            Context.getInstructionsPrompt(context),
            Style.getInstructionsPrompt()
        );
    }
    
    /**
     * Alternative method using the original approach with string replacement
     * for backward compatibility.
     * 
     * @param input The user input to be processed
     * @param context The conversation context
     * @return The formatted prompt string
     * @deprecated Use generatePrompt(String, String) instead for better performance
     */
    @Deprecated
    public String getInstructionsPrompt(String input, String context) {
        String prompt = PROMPT_TEMPLATE;
        
        // Replace placeholders with actual brackets to match template
        prompt = prompt.replace("%s", TASK_PLACEHOLDER)
                      .replace("%s", CONTEXT_PLACEHOLDER)
                      .replace("%s", STYLE_PLACEHOLDER);
        
        // Now replace with actual content
        prompt = prompt.replace(TASK_PLACEHOLDER, MemoriesTask.getInstructionsPrompt(input));
        prompt = prompt.replace(CONTEXT_PLACEHOLDER, MemoriesContext.getInstructionsPrompt(context));
        prompt = prompt.replace(STYLE_PLACEHOLDER, MemoriesStyle.getInstructionsPrompt());
        
        return prompt;
    }
}