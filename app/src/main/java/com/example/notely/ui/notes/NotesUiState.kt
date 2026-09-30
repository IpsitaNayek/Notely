package com.example.notely.ui.notes

import com.example.notely.data.local.NoteEntity

/**
 * Meaningful note categories per requirements:
 * 1. All - All active notes except deleted
 * 2. Pinned - Only pinned notes
 * 3. To-do - Task/checklist notes
 * 4. Notes - Normal text notes
 * 5. Audio Notes - Notes containing/centered around audio
 * 6. Bin - Soft-deleted notes
 */
enum class NoteCategory(val label: String) {
    ALL("All"),
    PINNED("Pinned"),
    TODO("To-do"),
    NOTES("Notes"),
    AUDIO("Audio Notes"),
    BIN("Bin"),
}

/**
 * Clean 4-item bottom navigation:
 * - Home
 * - Search
 * - Audio Note (quick recording action)
 * - Settings
 */
enum class DockItem(val label: String) {
    HOME("Home"),
    SEARCH("Search"),
    AUDIO_NOTE("Audio Note"),
    SETTINGS("Settings"),
}

data class NotesUiState(
    val notes: List<NoteEntity> = emptyList(),
    val trashedNotes: List<NoteEntity> = emptyList(),
    val activeNoteCount: Int = 0,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val selectedCategory: NoteCategory = NoteCategory.ALL,
    val selectedDockItem: DockItem = DockItem.HOME,
    val isLoading: Boolean = true,
    val noteForActions: NoteEntity? = null,
    val showCreateChoiceDialog: Boolean = false,
    val showAudioRecorderDialog: Boolean = false,
    val showEmptyBinDialog: Boolean = false,
)
