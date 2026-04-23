package com.example.replynow.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.replynow.domain.repository.MessageRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SnoozeReceiver : BroadcastReceiver() {

    @Inject
    lateinit var repository: MessageRepository

    override fun onReceive(context: Context, intent: Intent) {
        val messageId = intent.getLongExtra("message_id", -1)
        val duration = intent.getLongExtra("snooze_duration", 15 * 60 * 1000L)

        if (messageId == -1L) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository.snooze(messageId, System.currentTimeMillis() + duration)
            } finally {
                pendingResult.finish()
            }
        }

        // Dismiss the notification
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        nm.cancel(messageId.toInt())
    }
}
