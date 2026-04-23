package com.example.replynow.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val senderName: String,
    val messagePreview: String,
    val appName: String,
    val packageName: String,
    val timestamp: Long,
    val isReplied: Boolean = false,
    val isPending: Boolean = false,
    val snoozedUntil: Long? = null,
    val isImportant: Boolean = false
)
