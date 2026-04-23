package com.example.replynow.domain.model

data class Message(
    val id: Long = 0,
    val senderName: String,
    val messagePreview: String,
    val appName: String,
    val packageName: String,
    val timestamp: Long,
    val isReplied: Boolean = false,
    val isPending: Boolean = false,
    val snoozedUntil: Long? = null,
    val isImportant: Boolean = false
) {
    companion object {
        private val IMPORTANT_KEYWORDS = listOf("urgent", "asap", "important", "emergency", "help", "critical")

        fun detectImportance(text: String): Boolean =
            IMPORTANT_KEYWORDS.any { text.contains(it, ignoreCase = true) }
    }
}
