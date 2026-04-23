package com.example.replynow.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.replynow.MainActivity
import com.example.replynow.R
import com.example.replynow.domain.repository.MessageRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val repository: MessageRepository
) : CoroutineWorker(context, params) {

    companion object {
        const val CHANNEL_ID = "reply_reminders"
        const val WORK_NAME = "reminder_check"
    }

    override suspend fun doWork(): Result {
        // Mark old unreplied messages as pending
        repository.markOldMessagesAsPending(3_600_000L) // 1 hour threshold

        // Get messages that need reminders
        val messages = repository.getRemindableMessages()
        if (messages.isEmpty()) return Result.success()

        createNotificationChannel()

        messages.forEach { message ->
            showReminderNotification(message.id, message.senderName, message.messagePreview, message.appName)
        }

        // Cleanup old replied messages (older than 7 days)
        repository.cleanupOldReplied(7 * 24 * 3_600_000L)

        return Result.success()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Reply Reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Reminders to reply to pending messages"
        }
        val nm = context.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(channel)
    }

    private fun showReminderNotification(id: Long, sender: String, preview: String, appName: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, id.toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Snooze action
        val snoozeIntent = Intent(context, SnoozeReceiver::class.java).apply {
            putExtra("message_id", id)
            putExtra("snooze_duration", 15 * 60 * 1000L) // 15 min default
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context, (id * 10).toInt(), snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Reply to $sender")
            .setContentText(preview)
            .setSubText(appName)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(0, "Reply Now", pendingIntent)
            .addAction(0, "Snooze", snoozePendingIntent)
            .build()

        val nm = context.getSystemService(NotificationManager::class.java)
        nm.notify(id.toInt(), notification)
    }
}
