package com.example.replynow.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.replynow.domain.model.Message
import com.example.replynow.domain.repository.MessageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AppMessagesUiState(
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class AppMessagesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: MessageRepository
) : ViewModel() {

    private val packageName: String = savedStateHandle["packageName"] ?: ""

    val uiState: StateFlow<AppMessagesUiState> = repository.getMessagesByPackage(packageName)
        .map { messages ->
            AppMessagesUiState(
                messages = messages.sortedByDescending { it.timestamp },
                isLoading = false
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppMessagesUiState())

    fun markAsReplied(id: Long) {
        viewModelScope.launch { repository.markAsReplied(id) }
    }

    fun snooze(id: Long, durationMillis: Long) {
        viewModelScope.launch {
            repository.snooze(id, System.currentTimeMillis() + durationMillis)
        }
    }
}
