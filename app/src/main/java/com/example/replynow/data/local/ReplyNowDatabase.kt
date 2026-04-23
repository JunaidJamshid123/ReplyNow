package com.example.replynow.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.replynow.data.local.dao.MessageDao
import com.example.replynow.data.local.entity.MessageEntity

@Database(entities = [MessageEntity::class], version = 1, exportSchema = false)
abstract class ReplyNowDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
}
