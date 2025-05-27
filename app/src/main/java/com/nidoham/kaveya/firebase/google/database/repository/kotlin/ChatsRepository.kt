package com.nidoham.kaveya.firebase.google.database.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.nidoham.kaveya.firebase.google.database.model.Messages
import java.text.SimpleDateFormat
import java.util.*

class ChatRepository(database: FirebaseDatabase) {

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        ?: throw IllegalStateException("User must be logged in")

    private val chatsRef = database.getReference("chats")
        .child(currentUserId)
        .child("system")

    private var messageListener: ValueEventListener? = null

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    fun listenToMessages(
        onMessagesChanged: (List<Messages>) -> Unit,
        onError: (DatabaseError) -> Unit
    ) {
        messageListener = chatsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = snapshot.children.mapNotNull { it.getValue(Messages::class.java) }
                    .sortedBy { it.timestamp }
                onMessagesChanged(messages)
            }

            override fun onCancelled(error: DatabaseError) {
                onError(error)
            }
        })
    }

    fun insertMessage(
        message: Messages,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        message.id ?: return onError(IllegalArgumentException("Message ID cannot be null"))
        chatsRef.child(message.id).setValue(message)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun removeMessage(
        messageId: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        chatsRef.child(messageId).removeValue()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun cleanup() {
        messageListener?.let { chatsRef.removeEventListener(it) }
        messageListener = null
    }

    fun getCurrentUtcTime(): String = dateFormat.format(Date())
}