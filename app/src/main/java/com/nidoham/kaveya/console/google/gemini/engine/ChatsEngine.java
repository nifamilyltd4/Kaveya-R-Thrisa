package com.nidoham.kaveya.console.google.gemini.engine;

import android.util.Log;
import com.nidoham.kaveya.AdministrationLogger;
import com.nidoham.kaveya.console.google.gemini.control.GeminiController;
import com.nidoham.kaveya.console.google.gemini.template.AIGirlfriendPromptTemplate;
import com.nidoham.kaveya.liberies.SketchwareUtil;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

/**
 * Engine class for managing chat interactions with the Gemini AI API.
 * Specialized for AI girlfriend chatbot functionality.
 * Last updated: 2025-05-26
 * @author nifamilyltd4
 */
public class ChatsEngine implements AutoCloseable {
    
    private static final String TAG = "ChatsEngine";
    private static final String DEFAULT_SYSTEM_INSTRUCTION = "**AI Girlfriend Chatbot Prompt Template**";
    
    private final GeminiController controller;
    private final List<String> conversationHistory;
    private final AIGirlfriendPromptTemplate promptTemplate;

    /**
     * Callback interface for chat operations.
     */
    public interface ChatCallback {
        void onResponse(@Nonnull String response);
        void onError(@Nonnull Throwable error);
    }
    
    /**
     * Creates a new ChatsEngine with AI girlfriend functionality.
     */
    public ChatsEngine() {
        this.promptTemplate = new AIGirlfriendPromptTemplate();
        this.controller = new GeminiController(DEFAULT_SYSTEM_INSTRUCTION, 30); // timeout 30s
        this.conversationHistory = new ArrayList<>();
    }

    /**
     * Sends a user message and receives an AI response using the girlfriend prompt template.
     */
    public void sendMessage(@Nonnull String message, @Nonnull ChatCallback callback) {
        try {
            validateInput(message);
            
            // Add user message to history
            conversationHistory.add("User: " + message);
            
            // Generate context from conversation history
            String context = buildConversationContext();
            
            // Generate the AI girlfriend prompt
            String enhancedPrompt = promptTemplate.generatePrompt(message, context);

            // Generate response using GeminiController
            controller.generateResponse(enhancedPrompt, new GeminiController.GeminiCallback() {
                @Override
                public void onSuccess(@Nonnull String result) {
                    // Add AI response to history
                    conversationHistory.add("AI: " + result);
                    // Update controller's conversation history
                    controller.addToConversationHistory(conversationHistory);
                    callback.onResponse(result);
                }

                @Override
                public void onError(@Nonnull Throwable error) {
                    Log.e(TAG, "Error generating response", error);
                    callback.onError(error);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error sending message", e);
            callback.onError(e);
        }
    }

    /**
     * Builds conversation context from recent history.
     */
    private String buildConversationContext() {
        if (conversationHistory.isEmpty()) {
            return "Beginning of conversation";
        }
        
        StringBuilder context = new StringBuilder();
        // Get last 5 exchanges for context (or all if less than 5)
        int startIndex = Math.max(0, conversationHistory.size() - 10);
        
        for (int i = startIndex; i < conversationHistory.size(); i++) {
            context.append(conversationHistory.get(i)).append("\n");
        }
        
        return context.toString().trim();
    }

    /**
     * Adds a memory to the conversation context.
     */
    public void addMemory(@Nonnull String memory) {
        try {
            validateInput(memory);
            controller.setMemories(memory);
        } catch (Exception e) {
            Log.e(TAG, "Error adding memory", e);
        }
    }

    /**
     * Clears the conversation history.
     */
    public void clearConversation() {
        try {
            conversationHistory.clear();
            controller.addToConversationHistory(conversationHistory);
        } catch (Exception e) {
            Log.e(TAG, "Error clearing conversation", e);
        }
    }

    /**
     * Gets the current conversation history.
     */
    public List<String> getConversationHistory() {
        return new ArrayList<>(conversationHistory);
    }

    /**
     * Validates input strings.
     */
    private void validateInput(String input) {
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException("Input cannot be null or empty");
        }
    }

    /**
     * Cancels any ongoing chat operations.
     */
    public void cancel() {
        try {
            controller.cancelOperation();
        } catch (Exception e) {
            Log.e(TAG, "Error cancelling operation", e);
        }
    }

    /**
     * Closes and cleans up resources.
     */
    @Override
    public void close() {
        try {
            controller.close();
            conversationHistory.clear();
        } catch (Exception e) {
            Log.e(TAG, "Error closing ChatsEngine", e);
        }
    }

    /**
     * Returns whether the engine has been shut down.
     */
    public boolean isShutdown() {
        return controller.isShutdown();
    }
    
    /**
     * Gets the current AI girlfriend prompt template instance.
     */
    public AIGirlfriendPromptTemplate getPromptTemplate() {
        return promptTemplate;
    }
}