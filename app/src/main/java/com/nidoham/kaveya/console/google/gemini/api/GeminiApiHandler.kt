package com.nidoham.kaveya.console.google.gemini.api

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.java.ChatFutures
import com.google.ai.client.generativeai.java.GenerativeModelFutures
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.GenerateContentResponse
import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.nidoham.kaveya.BuildConfig
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

// Keep the original extension function since it's needed for your implementation
fun Content.Builder.addText(text: String): Content.Builder {
    val method = this.javaClass.getMethod("addText", String::class.java)
    return method.invoke(this, text) as Content.Builder
}

class GeminiApiHandler(systemInstruction: String) {
    private val executor: ExecutorService = Executors.newCachedThreadPool()
    private val chat: ChatFutures
    private val maxHistorySize = 10
    private var memories: MutableList<String> = mutableListOf()
    private var shortTermHistory: MutableList<String> = mutableListOf()
    private val isShutdown = AtomicBoolean(false)

    companion object {
        private const val AI_MODEL = "gemini-2.5-flash-preview-04-17"
        private const val TAG = "GeminiHandlerKt"
    }

    interface GeminiResponseCallback {
        fun onSuccess(result: String)
        fun onError(throwable: Throwable)
    }

    init {
        val basePrompt = buildString {
            appendLine(systemInstruction)
            if (memories.isNotEmpty()) {
                appendLine("\nMemories:\n" + memories.joinToString("\n"))
            }
        }
        
        val systemContent = Content.Builder().apply {
            addText(basePrompt)
        }.build()
        
        val generativeModel = GenerativeModel(AI_MODEL, BuildConfig.GEMINI_API_KEY)
        chat = GenerativeModelFutures.from(generativeModel).startChat(listOf(systemContent))
    }

    fun generateResponse(userInput: String, callback: GeminiResponseCallback) {
        if (isShutdown.get()) {
            callback.onError(IllegalStateException("GeminiApiHandler has been shut down"))
            return
        }
        
        if (userInput.trim().isEmpty()) {
            callback.onError(IllegalArgumentException("ইউজার ইনপুট খালি হতে পারে না"))
            return
        }

        // Add to history before making the request
        synchronized(shortTermHistory) {
            shortTermHistory.add(userInput)
            if (shortTermHistory.size > maxHistorySize) {
                shortTermHistory = shortTermHistory.takeLast(maxHistorySize).toMutableList()
            }
        }

        try {
            val contextText = buildContextText()
            
            val content = Content.Builder().apply {
                // Add context information if available
                if (contextText.isNotEmpty()) {
                    addText("$contextText\n\n")
                }
                
                // Add the user input
                addText(userInput)
            }.build()

            val response: ListenableFuture<GenerateContentResponse> = chat.sendMessage(content)
            
            Futures.addCallback(response, object : FutureCallback<GenerateContentResponse> {
                override fun onSuccess(result: GenerateContentResponse?) {
                    if (isShutdown.get()) return
                    
                    val resultText = result?.text
                    if (resultText != null) {
                        try {
                            callback.onSuccess(resultText)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error in success callback", e)
                        }
                    } else {
                        try {
                            callback.onError(IllegalStateException("জেনারেটেড কনটেন্টে কোনো টেক্সট নেই"))
                        } catch (e: Exception) {
                            Log.e(TAG, "Error in error callback", e)
                        }
                    }
                }

                override fun onFailure(t: Throwable) {
                    if (isShutdown.get()) return
                    
                    Log.e(TAG, "রেসপন্স জেনারেট করতে ত্রুটি: ", t)
                    try {
                        callback.onError(t)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error in failure callback", e)
                    }
                }
            }, executor)
        } catch (e: Exception) {
            Log.e(TAG, "কনটেন্ট জেনারেশনের সময় ত্রুটি: ", e)
            try {
                callback.onError(e)
            } catch (callbackError: Exception) {
                Log.e(TAG, "Error in exception callback", callbackError)
            }
        }
    }

    private fun buildContextText(): String {
        return buildString {
            synchronized(memories) {
                if (memories.isNotEmpty()) {
                    appendLine("Memories:")
                    memories.forEach { appendLine("- $it") }
                    appendLine()
                }
            }
            
            synchronized(shortTermHistory) {
                if (shortTermHistory.size > 1) { // Don't include the current message
                    appendLine("Recent Conversation:")
                    shortTermHistory.dropLast(1).forEach { appendLine("- $it") }
                }
            }
        }.trim()
    }

    fun addToConversationHistory(history: List<String>) {
        if (isShutdown.get()) return
        
        synchronized(shortTermHistory) {
            shortTermHistory.addAll(history.takeLast(maxHistorySize))
            if (shortTermHistory.size > maxHistorySize) {
                shortTermHistory = shortTermHistory.takeLast(maxHistorySize).toMutableList()
            }
        }
    }

    fun addMemories(memoryLines: List<String>) {
        if (isShutdown.get()) return
        
        synchronized(memories) {
            memories.addAll(memoryLines.takeLast(maxHistorySize))
            if (memories.size > maxHistorySize) {
                memories = memories.takeLast(maxHistorySize).toMutableList()
            }
        }
    }

    fun setMemories(memory: String) {
        if (isShutdown.get()) return
        
        synchronized(memories) {
            memories.clear()
            memories.add(memory)
        }
    }

    fun shutdown() {
        if (isShutdown.compareAndSet(false, true)) {
            try {
                executor.shutdown()
                if (!executor.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)) {
                    executor.shutdownNow()
                }
            } catch (e: InterruptedException) {
                executor.shutdownNow()
                Thread.currentThread().interrupt()
            }
        }
    }
}