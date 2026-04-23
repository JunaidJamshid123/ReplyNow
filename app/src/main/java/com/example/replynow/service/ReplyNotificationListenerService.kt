package com.example.replynow.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.example.replynow.domain.model.Message
import com.example.replynow.domain.repository.MessageRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ReplyNotificationListenerService : NotificationListenerService() {

    @Inject
    lateinit var repository: MessageRepository

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
        "com.snapchat.android"
    )

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val pkg = sbn.packageName
        if (pkg !in messagingApps) return

        val extras = sbn.notification.extras
        val sender = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: return
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: return

        // Skip group summaries
        if (sbn.notification.flags and Notification.FLAG_GROUP_SUMMARY != 0) return

        val appName = getAppLabel(pkg)
        val isImportant = Message.detectImportance(text)

        scope.launch {
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

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        // Could track reply detection here in future
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
