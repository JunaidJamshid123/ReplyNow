package com.example.replynow.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.replynow.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Query("SELECT * FROM messages WHERE isReplied = 0 ORDER BY timestamp DESC")
    fun getPendingMessages(): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages ORDER BY timestamp DESC")
    fun getAllMessages(): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE isReplied = 0 AND isPending = 1 AND (snoozedUntil IS NULL OR snoozedUntil < :currentTime) ORDER BY timestamp DESC")
    fun getRemindableMessages(currentTime: Long): List<MessageEntity>

    @Query("SELECT * FROM messages WHERE appName = :appName AND isReplied = 0 ORDER BY timestamp DESC")
    fun getMessagesByApp(appName: String): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: MessageEntity): Long

    @Update
    suspend fun update(message: MessageEntity)

    @Query("UPDATE messages SET isReplied = 1 WHERE id = :id")
    suspend fun markAsReplied(id: Long)

    @Query("UPDATE messages SET snoozedUntil = :snoozedUntil WHERE id = :id")
    suspend fun snooze(id: Long, snoozedUntil: Long)

    @Query("UPDATE messages SET isPending = 1 WHERE isReplied = 0 AND isPending = 0 AND timestamp < :threshold")
    suspend fun markOldMessagesAsPending(threshold: Long)

    @Query("DELETE FROM messages WHERE isReplied = 1 AND timestamp < :olderThan")
    suspend fun cleanupOldReplied(olderThan: Long)
}
