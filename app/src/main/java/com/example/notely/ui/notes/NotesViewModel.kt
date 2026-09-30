package com.example.notely.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notely.data.local.NoteEntity
import com.example.notely.data.local.NoteType
import com.example.notely.data.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class NotesViewModel @Inject constructor(
    private val repository: NoteRepository,
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _isSearchActive = MutableStateFlow(false)
    private val _selectedCategory = MutableStateFlow(NoteCategory.ALL)
    private val _selectedDockItem = MutableStateFlow(DockItem.HOME)
    private val _noteForActions = MutableStateFlow<NoteEntity?>(null)
    private val _showCreateChoiceDialog = MutableStateFlow(false)
    private val _showAudioRecorderDialog = MutableStateFlow(false)
    private val _showEmptyBinDialog = MutableStateFlow(false)

    /** Notes stream based on search query or active notes. */
    private val notesStream = combine(
        _searchQuery,
        _selectedDockItem,
    ) { query, dock ->
        Pair(query, dock)
    }.flatMapLatest { (query, dock) ->
        if (dock == DockItem.SEARCH && query.isNotBlank()) {
            repository.search(query)
        } else if (query.isNotBlank()) {
            repository.search(query)
        } else {
            repository.observeActiveNotes()
        }
    }

    val uiState: StateFlow<NotesUiState> = combine(
        notesStream,
        repository.observeTrashedNotes(),
        repository.observeActiveNoteCount(),
        _searchQuery,
        _isSearchActive,
        _selectedCategory,
        _selectedDockItem,
        _noteForActions,
        _showCreateChoiceDialog,
        _showAudioRecorderDialog,
        _showEmptyBinDialog,
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        val activeNotes = values[0] as List<NoteEntity>
        @Suppress("UNCHECKED_CAST")
        val trashedNotes = values[1] as List<NoteEntity>
        val activeCount = values[2] as Int
        val query = values[3] as String
        val searchActive = values[4] as Boolean
        val category = values[5] as NoteCategory
        val dock = values[6] as DockItem
        val actionNote = values[7] as NoteEntity?
        val showChoice = values[8] as Boolean
        val showAudio = values[9] as Boolean
        val showEmptyBin = values[10] as Boolean

        val displayedNotes = when (category) {
            NoteCategory.BIN -> trashedNotes
            else -> applyCategoryFilter(activeNotes, category)
        }

        NotesUiState(
            notes = displayedNotes,
            trashedNotes = trashedNotes,
            activeNoteCount = activeCount,
            searchQuery = query,
            isSearchActive = searchActive,
            selectedCategory = category,
            selectedDockItem = dock,
            isLoading = false,
            noteForActions = actionNote,
            showCreateChoiceDialog = showChoice,
            showAudioRecorderDialog = showAudio,
            showEmptyBinDialog = showEmptyBin,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = NotesUiState(),
    )

    // ── Actions ──

    fun onSearchQueryChanged(query: String) {
        _searchQuery.update { query }
    }

    fun onSearchToggle(active: Boolean) {
        _isSearchActive.update { active }
        if (!active) _searchQuery.update { "" }
    }

    fun onCategorySelected(category: NoteCategory) {
        _selectedCategory.update { category }
    }

    fun onDockItemSelected(item: DockItem) {
        if (item == DockItem.AUDIO_NOTE) {
            _showAudioRecorderDialog.update { true }
            return
        }
        _selectedDockItem.update { item }
        if (item == DockItem.SEARCH) {
            _isSearchActive.update { true }
        } else {
            _isSearchActive.update { false }
            _searchQuery.update { "" }
        }
    }

    fun onShowNoteActions(note: NoteEntity?) {
        _noteForActions.update { note }
    }

    fun onShowCreateChoice(show: Boolean) {
        _showCreateChoiceDialog.update { show }
    }

    fun onShowAudioRecorder(show: Boolean) {
        _showAudioRecorderDialog.update { show }
    }

    fun onShowEmptyBinDialog(show: Boolean) {
        _showEmptyBinDialog.update { show }
    }

    fun onMoveToTrash(noteId: String) {
        viewModelScope.launch {
            repository.moveToTrash(noteId)
        }
    }

    fun onRestoreFromTrash(noteId: String) {
        viewModelScope.launch {
            repository.restoreFromTrash(noteId)
        }
    }

    fun onTogglePin(noteId: String) {
        viewModelScope.launch {
            repository.togglePin(noteId)
        }
    }

    fun onDeletePermanently(noteId: String) {
        viewModelScope.launch {
            repository.deleteNoteById(noteId)
        }
    }

    fun onEmptyTrash() {
        viewModelScope.launch {
            repository.emptyTrash()
            _showEmptyBinDialog.update { false }
        }
    }

    fun onSaveAudioNote(title: String, file: File, durationSec: Int) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val note = NoteEntity(
                id = UUID.randomUUID().toString(),
                title = title,
                body = "Audio recording (${durationSec / 60}:${String.format("%02d", durationSec % 60)})",
                colorId = 0,
                isPinned = false,
                isTrashed = false,
                createdAt = now,
                updatedAt = now,
                noteType = NoteType.AUDIO.name,
                audioPath = file.absolutePath,
                audioDurationSec = durationSec,
            )
            repository.saveNote(note)
        }
    }

    // ── Helper ──

    private fun applyCategoryFilter(notes: List<NoteEntity>, category: NoteCategory): List<NoteEntity> =
        when (category) {
            NoteCategory.ALL -> notes
            NoteCategory.PINNED -> notes.filter { it.isPinned }
            NoteCategory.TODO -> notes.filter { it.isTodoNote() }
            NoteCategory.NOTES -> notes.filter { it.noteType == NoteType.NORMAL.name && !it.isTodoNote() && !it.isAudioNote() }
            NoteCategory.AUDIO -> notes.filter { it.isAudioNote() }
            NoteCategory.BIN -> emptyList() // Handled in combine
        }
}
