package com.example.notely.ui.notes

import com.example.notely.data.local.NoteEntity

/**
 * UI state for the Notes screen.
 */
data class NotesUiState(
    val notes: List<NoteEntity> = emptyList(),
    val trashedNotes: List<NoteEntity> = emptyList(),
    val activeNoteCount: Int = 0,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val selectedChip: NoteChip = NoteChip.ALL,
    val selectedDockItem: DockItem = DockItem.HOME,
    val isLoading: Boolean = true,
)

enum class NoteChip(val label: String) {
    ALL("All"),
    PINNED("Pinned"),
    TERRACOTTA("Terracotta"),
    SAGE("Sage"),
    DUSTY_BLUE("Dusty blue"),
    SAND("Sand"),
    CLAY_ROSE("Clay rose"),
    PLUM_TAUPE("Plum taupe"),
}

enum class DockItem(val label: String, val icon: String) {
    HOME("Home", "home"),
    SEARCH("Search", "search"),
    TRASH("Trash", "delete"),
    SETTINGS("Settings", "settings"),
}
