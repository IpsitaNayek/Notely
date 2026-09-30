package com.example.notely.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notely.data.local.NoteEntity
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
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class NotesViewModel @Inject constructor(
    private val repository: NoteRepository,
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _isSearchActive = MutableStateFlow(false)
    private val _selectedChip = MutableStateFlow(NoteChip.ALL)
    private val _selectedDockItem = MutableStateFlow(DockItem.HOME)

    /** Notes filtered by search or chip, reactively. */
    private val filteredNotes = combine(
        _searchQuery,
        _selectedChip,
        _selectedDockItem,
    ) { query, chip, dock ->
        Triple(query, chip, dock)
    }.flatMapLatest { (query, chip, dock) ->
        when (dock) {
            DockItem.TRASH -> repository.observeTrashedNotes()
            DockItem.SEARCH -> {
                if (query.isBlank()) repository.observeActiveNotes()
                else repository.search(query)
            }
            else -> {
                if (query.isNotBlank()) {
                    repository.search(query)
                } else {
                    repository.observeActiveNotes()
                }
            }
        }
    }

    val uiState: StateFlow<NotesUiState> = combine(
        filteredNotes,
        repository.observeTrashedNotes(),
        repository.observeActiveNoteCount(),
        _searchQuery,
        _isSearchActive,
        _selectedChip,
        _selectedDockItem,
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        val notes = values[0] as List<NoteEntity>
        @Suppress("UNCHECKED_CAST")
        val trashedNotes = values[1] as List<NoteEntity>
        val activeCount = values[2] as Int
        val query = values[3] as String
        val searchActive = values[4] as Boolean
        val chip = values[5] as NoteChip
        val dock = values[6] as DockItem

        val chipFiltered = applyChipFilter(notes, chip)

        NotesUiState(
            notes = chipFiltered,
            trashedNotes = trashedNotes,
            activeNoteCount = activeCount,
            searchQuery = query,
            isSearchActive = searchActive,
            selectedChip = chip,
            selectedDockItem = dock,
            isLoading = false,
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

    fun onChipSelected(chip: NoteChip) {
        _selectedChip.update { chip }
    }

    fun onDockItemSelected(item: DockItem) {
        _selectedDockItem.update { item }
        if (item != DockItem.SEARCH) {
            _isSearchActive.update { false }
            _searchQuery.update { "" }
        } else {
            _isSearchActive.update { true }
        }
    }

    fun onMoveToTrash(noteId: String) {
        viewModelScope.launch { repository.moveToTrash(noteId) }
    }

    fun onRestoreFromTrash(noteId: String) {
        viewModelScope.launch { repository.restoreFromTrash(noteId) }
    }

    fun onTogglePin(noteId: String) {
        viewModelScope.launch { repository.togglePin(noteId) }
    }

    fun onEmptyTrash() {
        viewModelScope.launch { repository.emptyTrash() }
    }

    // ── Helpers ──

    private fun applyChipFilter(notes: List<NoteEntity>, chip: NoteChip): List<NoteEntity> =
        when (chip) {
            NoteChip.ALL -> notes
            NoteChip.PINNED -> notes.filter { it.isPinned }
            NoteChip.TERRACOTTA -> notes.filter { it.colorId == 0 }
            NoteChip.SAGE -> notes.filter { it.colorId == 1 }
            NoteChip.DUSTY_BLUE -> notes.filter { it.colorId == 2 }
            NoteChip.SAND -> notes.filter { it.colorId == 3 }
            NoteChip.CLAY_ROSE -> notes.filter { it.colorId == 4 }
            NoteChip.PLUM_TAUPE -> notes.filter { it.colorId == 5 }
        }
}
