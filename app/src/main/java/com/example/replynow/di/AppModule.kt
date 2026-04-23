package com.example.replynow.di

import android.content.Context
import androidx.room.Room
import com.example.replynow.data.local.ReplyNowDatabase
import com.example.replynow.data.local.dao.MessageDao
import com.example.replynow.data.preferences.SettingsPreferences
import com.example.replynow.data.repository.MessageRepositoryImpl
import com.example.replynow.domain.repository.MessageRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ReplyNowDatabase =
        Room.databaseBuilder(
            context,
            ReplyNowDatabase::class.java,
            "replynow_db"
        ).addMigrations(ReplyNowDatabase.MIGRATION_1_2)
        .build()

    @Provides
    @Singleton
    fun provideMessageDao(db: ReplyNowDatabase): MessageDao = db.messageDao()

    @Provides
    @Singleton
    fun provideMessageRepository(dao: MessageDao): MessageRepository =
        MessageRepositoryImpl(dao)

    @Provides
    @Singleton
    fun provideSettingsPreferences(@ApplicationContext context: Context): SettingsPreferences =
        SettingsPreferences(context)
}
