package com.example.replynow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.replynow.domain.model.Message
import com.example.replynow.domain.repository.MessageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val messages: List<Message> = emptyList(),
    val groupedMessages: Map<String, List<Message>> = emptyMap(),
    val isGrouped: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: MessageRepository
) : ViewModel() {

    private val _isGrouped = MutableStateFlow(false)

    private fun sampleMessages(): List<Message> {
        val now = System.currentTimeMillis()
        return listOf(
            Message(
                id = -1, senderName = "Ali Khan",
                messagePreview = "Hey! Are we still meeting today? Let me know ASAP \uD83D\uDE4F",
                appName = "WhatsApp", packageName = "com.whatsapp",
                timestamp = now - 15 * 60 * 1000, isPending = true, isImportant = true
            ),
            Message(
                id = -2, senderName = "Sarah Ahmed",
                messagePreview = "Can you send me the project files?",
                appName = "Telegram", packageName = "org.telegram.messenger",
                timestamp = now - 45 * 60 * 1000, isPending = true, isImportant = false
            ),
            Message(
                id = -3, senderName = "Mom",
                messagePreview = "Call me when you're free, it's urgent!",
                appName = "WhatsApp", packageName = "com.whatsapp",
                timestamp = now - 2 * 60 * 60 * 1000, isPending = true, isImportant = true
            ),
            Message(
                id = -4, senderName = "Team Lead",
                messagePreview = "Please review the PR before EOD",
                appName = "Slack", packageName = "com.Slack",
                timestamp = now - 3 * 60 * 60 * 1000, isPending = true, isImportant = false
            ),
            Message(
                id = -5, senderName = "Bilal",
                messagePreview = "Bro check out this meme \uD83D\uDE02",
                appName = "Instagram", packageName = "com.instagram.android",
                timestamp = now - 5 * 60 * 60 * 1000, isPending = true, isImportant = false
            )
        )
    }

    val uiState: StateFlow<HomeUiState> = combine(
        repository.getPendingMessages(),
        _isGrouped
    ) { dbMessages, grouped ->
        val messages = if (dbMessages.isEmpty()) sampleMessages() else dbMessages
        HomeUiState(
            messages = messages,
            groupedMessages = if (grouped) messages.groupBy { it.appName } else emptyMap(),
            isGrouped = grouped,
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    fun markAsReplied(id: Long) {
        viewModelScope.launch { repository.markAsReplied(id) }
    }

    fun snooze(id: Long, durationMillis: Long) {
        viewModelScope.launch {
            repository.snooze(id, System.currentTimeMillis() + durationMillis)
        }
    }

    fun toggleGrouping() {
        _isGrouped.value = !_isGrouped.value
    }
}
