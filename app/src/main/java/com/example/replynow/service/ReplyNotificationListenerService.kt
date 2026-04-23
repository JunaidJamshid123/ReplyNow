package com.example.replynow.service

import android.app.Notification
import android.app.PendingIntent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
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

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

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
        // Cache PendingIntents so the UI can open exact chats
        val pendingIntents = ConcurrentHashMap<String, PendingIntent>()
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

        scope.launch {
            // Check if this sender already has a pending message from this app
            val hasPending = dao.isDuplicate(sender, pkg, text)
            if (hasPending) {
                // Same sender, same text — skip completely
                return@launch
            }

            // Try to update existing sender entry (new message from same person)
            dao.updateExistingSender(sender, pkg, text, sbn.postTime)

            // If no existing entry was updated, insert new
            val existingCheck = dao.isDuplicate(sender, pkg, text)
            if (!existingCheck) {
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
        scope.cancel()
    }
}
