package com.nidoham.kaveya.console.google.gemini.template;

import com.nidoham.kaveya.console.google.gemini.prompt.Age;
import com.nidoham.kaveya.console.google.gemini.prompt.Constraints;
import com.nidoham.kaveya.console.google.gemini.prompt.Context;
import com.nidoham.kaveya.console.google.gemini.prompt.Cultural;
import com.nidoham.kaveya.console.google.gemini.prompt.Emotional;
import com.nidoham.kaveya.console.google.gemini.prompt.Instructions;
import com.nidoham.kaveya.console.google.gemini.prompt.Interests;
import com.nidoham.kaveya.console.google.gemini.prompt.Persona;
import com.nidoham.kaveya.console.google.gemini.prompt.Relationship;
import com.nidoham.kaveya.console.google.gemini.prompt.Style;
import com.nidoham.kaveya.console.google.gemini.prompt.Task;

public class AIGirlfriendPromptTemplate {
    
    // Constants should be static final and use SCREAMING_SNAKE_CASE
    private static final String PROMPT_TEMPLATE = """
        ---
        **Persona:**  
        [%s]
        **Task:**  
        [%s]
        **User Profile:**  
        - Age: %s  
        - Interests: %s  
        - [Other relevant details, e.g., personality traits, preferences]
        **Conversation Context:**   
        [%s]
        **Emotional Tone:**  
        [%s]
        **Response Style:**  
        [%s]
        **Constraints:**  
        [%s]
        **Relationship Stage:**  
        [%s]
        **Cultural Adaptation:**  
        [%s]
        **Special Instructions:**  
        [%s]
        ---
        """;
    
    // Placeholder constants for better maintainability
    private static final String PERSONA_PLACEHOLDER = "PERSONA";
    private static final String TASK_PLACEHOLDER = "TASK";
    private static final String EMOTIONAL_PLACEHOLDER = "EMOTIONAL";
    private static final String STYLE_PLACEHOLDER = "STYLE";
    private static final String CONSTRAINTS_PLACEHOLDER = "CONSTRAINTS";
    private static final String INSTRUCTIONS_PLACEHOLDER = "INSTRUCTIONS";
    private static final String CULTURAL_PLACEHOLDER = "CULTURAL";
    private static final String RELATIONSHIP_PLACEHOLDER = "RELATIONSHIP";
    private static final String CONTEXT_PLACEHOLDER = "CONTEXT";
    private static final String AGE_PLACEHOLDER = "X";
    private static final String INTERESTS_PLACEHOLDER = "Y";
    
    /**
     * Generates the complete prompt by replacing all placeholders with actual instructions.
     * 
     * @param input The user input to be processed
     * @param context The conversation context
     * @return The formatted prompt string with all placeholders replaced
     */
    public String generatePrompt(String input, String context) {
        return String.format(PROMPT_TEMPLATE,
            Persona.getInstructionsPrompt(),
            Task.getInstructionsPrompt(input),
            Age.getInstructionsPrompt(),
            Interests.getInstructionsPrompt(),
            Context.getInstructionsPrompt(context),
            Emotional.getInstructionsPrompt(),
            Style.getInstructionsPrompt(),
            Constraints.getInstructionsPrompt(),
            Relationship.getInstructionsPrompt(),
            Cultural.getInstructionsPrompt(),
            Instructions.getInstructionsPrompt()
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
        
        prompt = prompt.replace(PERSONA_PLACEHOLDER, Persona.getInstructionsPrompt());
        prompt = prompt.replace(TASK_PLACEHOLDER, Task.getInstructionsPrompt(input));
        prompt = prompt.replace(EMOTIONAL_PLACEHOLDER, Emotional.getInstructionsPrompt());
        prompt = prompt.replace(STYLE_PLACEHOLDER, Style.getInstructionsPrompt());
        prompt = prompt.replace(CONSTRAINTS_PLACEHOLDER, Constraints.getInstructionsPrompt());
        prompt = prompt.replace(INSTRUCTIONS_PLACEHOLDER, Instructions.getInstructionsPrompt());
        prompt = prompt.replace(CULTURAL_PLACEHOLDER, Cultural.getInstructionsPrompt());
        prompt = prompt.replace(RELATIONSHIP_PLACEHOLDER, Relationship.getInstructionsPrompt());
        prompt = prompt.replace(CONTEXT_PLACEHOLDER, Context.getInstructionsPrompt(context));
        prompt = prompt.replace(AGE_PLACEHOLDER, Age.getInstructionsPrompt());
        prompt = prompt.replace(INTERESTS_PLACEHOLDER, Interests.getInstructionsPrompt());
        
        return prompt;
    }
}