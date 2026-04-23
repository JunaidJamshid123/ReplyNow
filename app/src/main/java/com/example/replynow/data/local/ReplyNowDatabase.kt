package com.example.replynow.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.replynow.data.local.dao.MessageDao
import com.example.replynow.data.local.entity.MessageEntity

@Database(entities = [MessageEntity::class], version = 2, exportSchema = false)
abstract class ReplyNowDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE messages ADD COLUMN messageCount INTEGER NOT NULL DEFAULT 1")
            }
        }
    }
}
