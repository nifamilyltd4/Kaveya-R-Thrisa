package com.nidoham.kaveya.console.google.gemini.control;

import android.util.Log;
import com.nidoham.kaveya.console.google.gemini.api.GeminiApiHandler;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nonnull;

/**
 * Controller class for managing interactions with the Gemini AI API with callback support.
 * Last updated: 2025-04-18 12:19:10 UTC
 * @author nifamilyltd4
 */
public class GeminiController implements AutoCloseable {
    private static final String TAG = "GeminiController";
    public static final long DEFAULT_TIMEOUT_SECONDS = 30;

    private final GeminiApiHandler handler;
    private final long timeoutSeconds;
    private final AtomicBoolean isShutdown = new AtomicBoolean(false);
    private final ScheduledExecutorService timeoutExecutor;
    private final AtomicReference<ScheduledFuture<?>> currentTimeoutTask = new AtomicReference<>();

    /**
     * Simple callback interface for Gemini operations
     */
    public interface GeminiCallback {
        void onSuccess(@Nonnull String result);
        void onError(@Nonnull Throwable error);
    }

    /**
     * Creates a new GeminiController with default timeout.
     */
    public GeminiController(@Nonnull String systemInstruction) {
        this(systemInstruction, DEFAULT_TIMEOUT_SECONDS);
    }

    /**
     * Creates a new GeminiController with specified timeout.
     */
    public GeminiController(@Nonnull String systemInstruction, long timeoutSeconds) {
        validateParameters(systemInstruction, timeoutSeconds);
        this.handler = new GeminiApiHandler(systemInstruction);
        this.timeoutSeconds = timeoutSeconds;
        this.timeoutExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "GeminiTimeout");
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * Generates an AI response with callback.
     */
    public void generateResponse(@Nonnull String userInput, @Nonnull GeminiCallback callback) {
        try {
            checkShutdown();
            validateUserInput(userInput);

            // Create a wrapper callback to handle timeout cancellation
            final AtomicBoolean completed = new AtomicBoolean(false);
            
            GeminiApiHandler.GeminiResponseCallback wrappedCallback = new GeminiApiHandler.GeminiResponseCallback() {
                @Override
                public void onSuccess(@Nonnull String result) {
                    if (completed.compareAndSet(false, true)) {
                        cancelCurrentTimeout();
                        try {
                            callback.onSuccess(result);
                        } catch (Exception e) {
                            Log.e(TAG, "Error in success callback", e);
                        }
                    }
                }

                @Override
                public void onError(@Nonnull Throwable throwable) {
                    if (completed.compareAndSet(false, true)) {
                        cancelCurrentTimeout();
                        try {
                            callback.onError(throwable);
                        } catch (Exception e) {
                            Log.e(TAG, "Error in error callback", e);
                        }
                    }
                }
            };

            // Schedule timeout before making the request
            scheduleTimeout(callback, completed);
            
            // Make the request
            handler.generateResponse(userInput, wrappedCallback);

        } catch (Exception e) {
            try {
                callback.onError(e);
            } catch (Exception callbackError) {
                Log.e(TAG, "Error in exception callback", callbackError);
            }
        }
    }

    /**
     * Adds conversation history.
     */
    public void addToConversationHistory(@Nonnull List<String> history) {
        try {
            checkShutdown();
            validateList(history, "Conversation history");
            handler.addToConversationHistory(history);
        } catch (Exception e) {
            Log.e(TAG, "Error adding conversation history", e);
        }
    }

    /**
     * Adds memories.
     */
    public void addMemories(@Nonnull List<String> memories) {
        try {
            checkShutdown();
            validateList(memories, "Memories");
            handler.addMemories(memories);
        } catch (Exception e) {
            Log.e(TAG, "Error adding memories", e);
        }
    }

    /**
     * Sets memories.
     */
    public void setMemories(@Nonnull String memory) {
        try {
            checkShutdown();
            if (memory == null) {
                throw new IllegalArgumentException("Memory cannot be null");
            }
            handler.setMemories(memory);
        } catch (Exception e) {
            Log.e(TAG, "Error setting memories", e);
        }
    }

    /**
     * Schedules a timeout for the current operation.
     */
    private void scheduleTimeout(GeminiCallback callback, AtomicBoolean completed) {
        if (isShutdown.get()) return;
        
        ScheduledFuture<?> timeoutTask = timeoutExecutor.schedule(() -> {
            if (completed.compareAndSet(false, true)) {
                try {
                    callback.onError(new TimeoutException("Operation timed out after " + timeoutSeconds + " seconds"));
                } catch (Exception e) {
                    Log.e(TAG, "Error in timeout callback", e);
                }
            }
        }, timeoutSeconds, TimeUnit.SECONDS);
        
        currentTimeoutTask.set(timeoutTask);
    }

    /**
     * Cancels the current timeout task.
     */
    private void cancelCurrentTimeout() {
        ScheduledFuture<?> task = currentTimeoutTask.getAndSet(null);
        if (task != null && !task.isDone()) {
            task.cancel(false);
        }
    }

    /**
     * Validates the constructor parameters.
     */
    private void validateParameters(String systemInstruction, long timeoutSeconds) {
        if (systemInstruction == null || systemInstruction.trim().isEmpty()) {
            throw new IllegalArgumentException("System instruction cannot be null or empty");
        }
        if (timeoutSeconds <= 0) {
            throw new IllegalArgumentException("Timeout must be positive");
        }
    }

    /**
     * Validates the user input.
     */
    private void validateUserInput(String userInput) {
        if (userInput == null || userInput.trim().isEmpty()) {
            throw new IllegalArgumentException("User input cannot be null or empty");
        }
    }

    /**
     * Validates a list parameter.
     */
    private void validateList(List<String> list, String paramName) {
        if (list == null) {
            throw new IllegalArgumentException(paramName + " cannot be null");
        }
    }

    /**
     * Checks if the controller has been shut down.
     */
    private void checkShutdown() {
        if (isShutdown.get()) {
            throw new IllegalStateException("GeminiController has been shut down");
        }
    }

    /**
     * Cancels any ongoing operations.
     */
    public void cancelOperation() {
        cancelCurrentTimeout();
    }

    /**
     * Closes and cleans up resources.
     */
    @Override
    public void close() {
        if (isShutdown.compareAndSet(false, true)) {
            try {
                // Cancel any pending timeout
                cancelCurrentTimeout();
                
                // Shutdown handler
                handler.shutdown();
                
                // Shutdown timeout executor
                timeoutExecutor.shutdown();
                try {
                    if (!timeoutExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                        timeoutExecutor.shutdownNow();
                        if (!timeoutExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                            Log.w(TAG, "Timeout executor did not terminate gracefully");
                        }
                    }
                } catch (InterruptedException e) {
                    timeoutExecutor.shutdownNow();
                    Thread.currentThread().interrupt();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error during shutdown", e);
            }
        }
    }

    /**
     * @deprecated Use {@link #close()} instead.
     */
    @Deprecated
    public void shutdown() {
        close();
    }

    /**
     * Returns whether the controller has been shut down.
     */
    public boolean isShutdown() {
        return isShutdown.get();
    }
}