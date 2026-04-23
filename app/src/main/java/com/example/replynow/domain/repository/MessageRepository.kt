package com.example.replynow.domain.repository

import com.example.replynow.domain.model.Message
import kotlinx.coroutines.flow.Flow

interface MessageRepository {
    fun getPendingMessages(): Flow<List<Message>>
    fun getAllMessages(): Flow<List<Message>>
    fun getMessagesByApp(appName: String): Flow<List<Message>>
    suspend fun addMessage(message: Message): Long
    suspend fun markAsReplied(id: Long)
    suspend fun snooze(id: Long, snoozedUntil: Long)
    suspend fun markOldMessagesAsPending(thresholdMillis: Long)
    fun getRemindableMessages(): List<Message>
    suspend fun cleanupOldReplied(olderThanMillis: Long)
}
