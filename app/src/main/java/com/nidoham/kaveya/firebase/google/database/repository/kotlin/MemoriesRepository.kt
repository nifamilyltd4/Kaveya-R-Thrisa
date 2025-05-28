package com.nidoham.kaveya.firebase.google.database.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class MemoriesRepository(database: FirebaseDatabase, character: String) {
    
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        ?: throw IllegalStateException("User must be logged in")
    
    private val chatsRef = database.getReference("memories")
        .child(currentUserId)
        .child(character)
    
    private var messageListener: ValueEventListener? = null
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    
    fun listenToMessages(
        onMessagesChanged: (String) -> Unit,
        onError: (DatabaseError) -> Unit
    ) {
        messageListener = chatsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.getValue(String::class.java) ?: ""
                onMessagesChanged(value)
            }
            override fun onCancelled(error: DatabaseError) {
                onError(error)
            }
        })
    }
    
    fun insertMessage(
        message: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        chatsRef.setValue(message)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }
    
    fun removeMessage(
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        chatsRef.removeValue()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }
    
    fun cleanup() {
        messageListener?.let { chatsRef.removeEventListener(it) }
        messageListener = null
    }
    
    fun getCurrentUtcTime(): String = dateFormat.format(Date())
}