package com.example.notely.ui.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notely.data.local.NoteEntity
import com.example.notely.data.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class EditorViewModel @Inject constructor(
    private val repository: NoteRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val noteId: String? = savedStateHandle["noteId"]

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    init {
        if (noteId != null) {
            loadNote(noteId)
        } else {
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun loadNote(id: String) {
        viewModelScope.launch {
            val note = repository.getNoteById(id)
            if (note != null) {
                _uiState.update {
                    it.copy(
                        noteId = note.id,
                        title = note.title,
                        body = note.body,
                        colorId = note.colorId,
                        isPinned = note.isPinned,
                        isTrashed = note.isTrashed,
                        createdAt = note.createdAt,
                        isLoading = false,
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    // ── Field edits ──

    fun onTitleChanged(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun onBodyChanged(body: String) {
        _uiState.update { it.copy(body = body) }
    }

    fun onColorSelected(colorId: Int) {
        _uiState.update { it.copy(colorId = colorId, showColorPicker = false) }
    }

    fun onToggleColorPicker() {
        _uiState.update { it.copy(showColorPicker = !it.showColorPicker) }
    }

    fun onDismissColorPicker() {
        _uiState.update { it.copy(showColorPicker = false) }
    }

    fun onShowDeleteConfirm() {
        _uiState.update { it.copy(showDeleteConfirm = true) }
    }

    fun onDismissDeleteConfirm() {
        _uiState.update { it.copy(showDeleteConfirm = false) }
    }

    // ── Save / delete ──

    /**
     * Saves the current note (upsert). Returns true if save succeeded.
     * A note is only saved if it has at least a title or body.
     */
    fun save(): Boolean {
        val state = _uiState.value
        if (state.title.isBlank() && state.body.isBlank()) return false

        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val entity = NoteEntity(
                id = state.noteId ?: UUID.randomUUID().toString(),
                title = state.title.trim(),
                body = state.body.trim(),
                colorId = state.colorId,
                isPinned = state.isPinned,
                isTrashed = state.isTrashed,
                createdAt = if (state.createdAt > 0L) state.createdAt else now,
                updatedAt = now,
            )
            repository.saveNote(entity)
            _uiState.update { it.copy(noteId = entity.id, isSaved = true) }
        }
        return true
    }

    fun onTogglePin() {
        val state = _uiState.value
        if (state.noteId != null) {
            viewModelScope.launch { repository.togglePin(state.noteId) }
        }
        _uiState.update { it.copy(isPinned = !it.isPinned) }
    }

    fun onMoveToTrash() {
        val state = _uiState.value
        if (state.noteId != null) {
            viewModelScope.launch { repository.moveToTrash(state.noteId) }
        }
        _uiState.update { it.copy(isTrashed = true, showDeleteConfirm = false) }
    }
}
