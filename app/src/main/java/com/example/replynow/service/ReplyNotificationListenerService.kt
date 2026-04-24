package com.example.replynow.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.example.replynow.MainActivity
import com.example.replynow.R
import com.example.replynow.data.local.dao.MessageDao
import com.example.replynow.domain.model.Message
import com.example.replynow.domain.repository.MessageRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

@AndroidEntryPoint
class ReplyNotificationListenerService : NotificationListenerService() {

    @Inject
    lateinit var repository: MessageRepository

    @Inject
    lateinit var dao: MessageDao

    private var scope: CoroutineScope? = null

    // Messaging app package names to monitor
    private val messagingApps = setOf(
        "com.whatsapp",
        "com.whatsapp.w4b",
        "org.telegram.messenger",
        "com.google.android.apps.messaging",
        "com.facebook.orca",
        "com.instagram.android",
        "com.Slack",
        "com.discord",
        "com.viber.voip",
        "com.snapchat.android",
        "org.thoughtcrime.securesms",
        "com.google.android.gm",
        "com.microsoft.office.outlook",
        "com.linkedin.android",
        "com.microsoft.teams",
        "com.tencent.mm",
        "com.google.android.apps.dynamite",
        "us.zoom.videomeetings",
        "com.facebook.katana",
        "com.twitter.android",
        "com.zhiliaoapp.musically"
    )

    companion object {
        private const val TAG = "ReplyNowListener"
        private const val FOREGROUND_CHANNEL_ID = "listener_service"
        private const val FOREGROUND_NOTIFICATION_ID = 9999

        // Cache PendingIntents so the UI can open exact chats
        val pendingIntents = ConcurrentHashMap<String, PendingIntent>()
    }

    override fun onCreate() {
        super.onCreate()
        scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        startForegroundService()
        Log.d(TAG, "Service created")
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "Listener connected — actively monitoring notifications")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.w(TAG, "Listener disconnected — requesting rebind")
        requestRebind(android.content.ComponentName(this, ReplyNotificationListenerService::class.java))
    }

    private fun startForegroundService() {
        val channel = NotificationChannel(
            FOREGROUND_CHANNEL_ID,
            "Notification Monitor",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Keeps ReplyNow monitoring your messages"
            setShowBadge(false)
        }
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(channel)

        val intent = Intent(this, MainActivity::class.java)
        val pi = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = Notification.Builder(this, FOREGROUND_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("ReplyNow is active")
            .setContentText("Monitoring messages in the background")
            .setContentIntent(pi)
            .setOngoing(true)
            .build()

        startForeground(FOREGROUND_NOTIFICATION_ID, notification)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val pkg = sbn.packageName
        if (pkg !in messagingApps) return

        val extras = sbn.notification.extras
        val sender = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: return
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: return

        // Skip group summaries
        if (sbn.notification.flags and Notification.FLAG_GROUP_SUMMARY != 0) return

        // Cache the PendingIntent for deep-linking (open exact chat)
        sbn.notification.contentIntent?.let { pi ->
            pendingIntents["${pkg}_${sender}"] = pi
        }

        val appName = getAppLabel(pkg)
        val isImportant = Message.detectImportance(text)

        scope?.launch {
            // Check if this exact message already exists (same sender, same text)
            val hasPending = dao.isDuplicate(sender, pkg, text)
            if (hasPending) {
                return@launch
            }

            // Check if sender has an existing unreplied entry
            val hasExisting = dao.hasUnrepliedFromSender(sender, pkg)
            if (hasExisting) {
                // Update existing entry with the new message text
                dao.updateExistingSender(sender, pkg, text, sbn.postTime)
            } else {
                // No existing entry — insert new
                repository.addMessage(
                    Message(
                        senderName = sender,
                        messagePreview = text,
                        appName = appName,
                        packageName = pkg,
                        timestamp = sbn.postTime,
                        isImportant = isImportant
                    )
                )
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        // Clean up cached intent
        val pkg = sbn.packageName
        val sender = sbn.notification.extras?.getCharSequence(Notification.EXTRA_TITLE)?.toString()
        if (sender != null) {
            pendingIntents.remove("${pkg}_${sender}")
        }
    }

    private fun getAppLabel(packageName: String): String = try {
        val pm = packageManager
        val appInfo = pm.getApplicationInfo(packageName, 0)
        pm.getApplicationLabel(appInfo).toString()
    } catch (_: Exception) {
        packageName.substringAfterLast('.')
    }

    override fun onDestroy() {
        super.onDestroy()
        scope?.cancel()
        scope = null
        Log.d(TAG, "Service destroyed")
    }
}
