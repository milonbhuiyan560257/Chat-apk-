package com.example.chatapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val messageText: String = "",

    val imageUri: String? = null,

    val timestamp: Long = System.currentTimeMillis()
)
