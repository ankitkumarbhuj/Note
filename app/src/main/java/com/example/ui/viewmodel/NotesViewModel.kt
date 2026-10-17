package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.GeminiService
import com.example.data.model.Note
import com.example.data.model.NoteCategory
import com.example.data.model.UserProfile
import com.example.data.repository.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

class NotesViewModel(
    private val repository: NoteRepository,
    private val currentUserId: String,
    private val geminiService: GeminiService = GeminiService()
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow("ALL") // ALL, BUSINESS, PRIVATE, PINNED, FAVORITE
    val selectedFilter = _selectedFilter.asStateFlow()

    private val _isVaultUnlocked = MutableStateFlow(false)
    val isVaultUnlocked = _isVaultUnlocked.asStateFlow()

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile = _userProfile.asStateFlow()

    private val _aiProcessing = MutableStateFlow(false)
    val aiProcessing = _aiProcessing.asStateFlow()

    private val _aiResult = MutableStateFlow<String?>(null)
    val aiResult = _aiResult.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            val result = repository.getUserProfile(currentUserId)
            if (result.isSuccess) {
                _userProfile.value = result.getOrNull()
                // If user has never set a vault PIN, default to unlocked
                if (result.getOrNull()?.vaultPin.isNullOrBlank()) {
                    _isVaultUnlocked.value = true
                }
            }
        }
    }

    val notesState: StateFlow<UiState<List<Note>>> = repository.observeNotes(currentUserId)
        .map<List<Note>, UiState<List<Note>>> { UiState.Success(it) }
        .catch { error ->
            Log.w(TAG, "Error observing notes", error)
            emit(UiState.Error(error.localizedMessage ?: "Failed to load notes"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UiState.Loading
        )

    val filteredNotes: StateFlow<List<Note>> = combine(
        notesState,
        searchQuery,
        selectedFilter,
        isVaultUnlocked
    ) { state, query, filter, unlocked ->
        if (state !is UiState.Success) return@combine emptyList()
        val allNotes = state.data

        val matchingQuery = if (query.isBlank()) {
            allNotes
        } else {
            allNotes.filter { note ->
                note.title.contains(query, ignoreCase = true) ||
                        note.content.contains(query, ignoreCase = true) ||
                        note.tags.any { it.contains(query, ignoreCase = true) }
            }
        }

        val filteredByCategory = when (filter) {
            "BUSINESS" -> matchingQuery.filter { it.category == NoteCategory.BUSINESS.name }
            "PRIVATE" -> matchingQuery.filter { it.category == NoteCategory.PRIVATE.name }
            "PINNED" -> matchingQuery.filter { it.isPinned }
            "FAVORITE" -> matchingQuery.filter { it.isFavorite }
            else -> matchingQuery
        }

        // Sort: pinned first, then updated/created descending
        filteredByCategory.sortedWith(
            compareByDescending<Note> { it.isPinned }
                .thenByDescending { it.updatedAt ?: it.createdAt }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = emptyList()
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedFilter(filter: String) {
        _selectedFilter.value = filter
    }

    fun unlockVault(pin: String): Boolean {
        val expectedPin = _userProfile.value?.vaultPin
        if (expectedPin.isNullOrBlank() || expectedPin == pin) {
            _isVaultUnlocked.value = true
            return true
        }
        return false
    }

    fun lockVault() {
        if (!_userProfile.value?.vaultPin.isNullOrBlank()) {
            _isVaultUnlocked.value = false
        }
    }

    fun setVaultPin(newPin: String, onDone: () -> Unit) {
        viewModelScope.launch {
            val updated = (_userProfile.value ?: UserProfile(userId = currentUserId)).copy(vaultPin = newPin)
            val result = repository.saveUserProfile(currentUserId, updated)
            if (result.isSuccess) {
                _userProfile.value = updated
                _isVaultUnlocked.value = true
                onDone()
            }
        }
    }

    fun saveNote(note: Note, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = repository.saveNote(currentUserId, note)
            onComplete(result.isSuccess)
            if (result.isFailure) {
                _errorMessage.value = result.exceptionOrNull()?.localizedMessage ?: "Failed to save note"
            }
        }
    }

    fun deleteNote(noteId: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val result = repository.deleteNote(currentUserId, noteId)
            if (result.isSuccess) {
                onComplete()
            }
        }
    }

    fun togglePin(note: Note) {
        viewModelScope.launch {
            repository.saveNote(currentUserId, note.copy(isPinned = !note.isPinned))
        }
    }

    fun toggleFavorite(note: Note) {
        viewModelScope.launch {
            repository.saveNote(currentUserId, note.copy(isFavorite = !note.isFavorite))
        }
    }

    fun executeAiTool(toolPrompt: String, noteContent: String) {
        if (noteContent.isBlank()) {
            _errorMessage.value = "Note content is empty"
            return
        }
        _aiProcessing.value = true
        _aiResult.value = null
        viewModelScope.launch {
            val result = geminiService.generateNoteEnhancement(
                prompt = toolPrompt,
                noteContent = noteContent,
                model = GeminiService.MODEL_CHAT
            )
            _aiProcessing.value = false
            if (result.isSuccess) {
                _aiResult.value = result.getOrNull()
            } else {
                _errorMessage.value = result.exceptionOrNull()?.localizedMessage ?: "AI enhancement failed"
            }
        }
    }

    fun clearAiResult() {
        _aiResult.value = null
    }

    fun clearError() {
        _errorMessage.value = null
    }

    companion object {
        private const val TAG = "NotesViewModel"
    }
}
