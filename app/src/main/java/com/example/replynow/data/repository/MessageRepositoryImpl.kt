package com.example.replynow.data.repository

import com.example.replynow.data.local.dao.MessageDao
import com.example.replynow.data.local.entity.MessageEntity
import com.example.replynow.domain.model.Message
import com.example.replynow.domain.repository.MessageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageRepositoryImpl @Inject constructor(
    private val dao: MessageDao
) : MessageRepository {

    override fun getPendingMessages(): Flow<List<Message>> =
        dao.getPendingMessages().map { list -> list.map { it.toDomain() } }

    override fun getAllMessages(): Flow<List<Message>> =
        dao.getAllMessages().map { list -> list.map { it.toDomain() } }

    override fun getMessagesByApp(appName: String): Flow<List<Message>> =
        dao.getMessagesByApp(appName).map { list -> list.map { it.toDomain() } }

    override fun getMessagesByPackage(packageName: String): Flow<List<Message>> =
        dao.getMessagesByPackage(packageName).map { list -> list.map { it.toDomain() } }

    override fun getPendingCountsByPackage(): Flow<Map<String, Int>> =
        dao.getPendingCountsByPackage().map { list -> list.associate { it.packageName to it.count } }

    override suspend fun addMessage(message: Message): Long =
        dao.insert(message.toEntity())

    override suspend fun markAsReplied(id: Long) =
        dao.markAsReplied(id)

    override suspend fun snooze(id: Long, snoozedUntil: Long) =
        dao.snooze(id, snoozedUntil)

    override suspend fun markOldMessagesAsPending(thresholdMillis: Long) =
        dao.markOldMessagesAsPending(System.currentTimeMillis() - thresholdMillis)

    override fun getRemindableMessages(): List<Message> =
        dao.getRemindableMessages(System.currentTimeMillis()).map { it.toDomain() }

    override suspend fun cleanupOldReplied(olderThanMillis: Long) =
        dao.cleanupOldReplied(System.currentTimeMillis() - olderThanMillis)

    private fun MessageEntity.toDomain() = Message(
        id = id,
        senderName = senderName,
        messagePreview = messagePreview,
        appName = appName,
        packageName = packageName,
        timestamp = timestamp,
        isReplied = isReplied,
        isPending = isPending,
        snoozedUntil = snoozedUntil,
        isImportant = isImportant,
        messageCount = messageCount
    )

    private fun Message.toEntity() = MessageEntity(
        id = id,
        senderName = senderName,
        messagePreview = messagePreview,
        appName = appName,
        packageName = packageName,
        timestamp = timestamp,
        isReplied = isReplied,
        isPending = isPending,
        snoozedUntil = snoozedUntil,
        isImportant = isImportant,
        messageCount = messageCount
    )
}
