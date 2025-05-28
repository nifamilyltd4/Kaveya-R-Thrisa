package com.nidoham.kaveya.console.google.gemini.engine;

import android.util.Log;
import com.nidoham.kaveya.console.google.gemini.control.GeminiController;
import com.nidoham.kaveya.console.google.gemini.template.AIGirlfriendPromptTemplate;
import javax.annotation.Nonnull;

/**
 * Engine class for managing chat interactions with the Gemini AI API.
 * Specialized for AI girlfriend chatbot functionality.
 * Last updated: 2025-05-26
 * @author nifamilyltd4
 */
public class MemoriesEngine implements AutoCloseable {
    
    private static final String TAG = "MemoriesEngine";
    private static final String DEFAULT_SYSTEM_INSTRUCTION = "You are a caring, loving AI girlfriend companion.";
    
    private final GeminiController controller;
    private final StringBuilder memories;
    private final AIGirlfriendPromptTemplate promptTemplate;

    /**
     * Callback interface for chat operations.
     */
    public interface ChatCallback {
        void onResponse(@Nonnull String response);
        void onError(@Nonnull Throwable error);
    }
    
    /**
     * Creates a new MemoriesEngine with AI girlfriend functionality.
     */
    public MemoriesEngine() {
        this.promptTemplate = new AIGirlfriendPromptTemplate();
        this.controller = new GeminiController(DEFAULT_SYSTEM_INSTRUCTION, 30);
        this.memories = new StringBuilder();
    }

    /**
     * Sends a user message and receives an AI response using the girlfriend prompt template.
     */
    public void sendMessage(@Nonnull String message, @Nonnull ChatCallback callback) {
        if (message == null || message.trim().isEmpty()) {
            callback.onError(new IllegalArgumentException("Message cannot be null or empty"));
            return;
        }

        try {
            String enhancedPrompt = promptTemplate.generatePrompt(message, memories.toString());

            controller.generateResponse(enhancedPrompt, new GeminiController.GeminiCallback() {
                @Override
                public void onSuccess(@Nonnull String result) {
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
     * Adds a memory to the AI memories.
     */
    public void addMemory(@Nonnull String memory) {
        if (memory == null || memory.trim().isEmpty()) {
            Log.w(TAG, "Attempted to add null or empty memory");
            return;
        }
        
        memories.append(memory).append("\n");
    }

    /**
     * Clears all memories.
     */
    public void clearMemories() {
        memories.setLength(0);
        Log.d(TAG, "Memories cleared");
    }
    
    /**
     * Gets all memories as text.
     */
    @Nonnull
    public String getMemories() {
        return memories.toString();
    }
    
    /**
     * Sets memories from text.
     */
    public void setMemories(@Nonnull String memoriesText) {
        if (memoriesText == null) {
            throw new IllegalArgumentException("Memories text cannot be null");
        }
        
        memories.setLength(0);
        memories.append(memoriesText);
    }

    /**
     * Cancels any ongoing chat operations.
     */
    public void cancel() {
        controller.cancelOperation();
    }

    /**
     * Closes and cleans up resources.
     */
    @Override
    public void close() {
        try {
            controller.close();
            memories.setLength(0);
        } catch (Exception e) {
            Log.e(TAG, "Error closing MemoriesEngine", e);
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
    @Nonnull
    public AIGirlfriendPromptTemplate getPromptTemplate() {
        return promptTemplate;
    }
}