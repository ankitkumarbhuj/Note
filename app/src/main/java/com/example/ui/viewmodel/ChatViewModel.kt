package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.ChatMessage
import com.example.data.ai.GeminiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel(
    private val geminiService: GeminiService = GeminiService()
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                role = "model",
                content = "Greetings. I am PrivaAI, your executive business strategist and private confidential advisor. How may I assist you with your memos, strategy, or private thoughts today?"
            )
        )
    )
    val messages = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    fun sendMessage(userText: String) {
        if (userText.isBlank() || _isLoading.value) return

        val userMessage = ChatMessage(role = "user", content = userText.trim())
        val updatedHistory = _messages.value + userMessage
        _messages.value = updatedHistory

        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            val result = geminiService.sendMultiTurnChat(
                history = updatedHistory.dropLast(1),
                newMessage = userMessage.content,
                model = GeminiService.MODEL_CHAT
            )
            _isLoading.value = false
            if (result.isSuccess) {
                val modelReply = ChatMessage(
                    role = "model",
                    content = result.getOrNull() ?: "I have processed your request."
                )
                _messages.value = _messages.value + modelReply
            } else {
                _errorMessage.value = result.exceptionOrNull()?.localizedMessage ?: "Failed to generate AI response"
            }
        }
    }

    fun clearChat() {
        _messages.value = listOf(
            ChatMessage(
                role = "model",
                content = "Chat cleared. What would you like to discuss next?"
            )
        )
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
