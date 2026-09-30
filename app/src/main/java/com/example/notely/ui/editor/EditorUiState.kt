package com.example.notely.ui.editor

/**
 * UI state for the Note Editor screen.
 */
data class EditorUiState(
    val noteId: String? = null,
    val title: String = "",
    val body: String = "",
    val colorId: Int = 0,
    val isPinned: Boolean = false,
    val isTrashed: Boolean = false,
    val createdAt: Long = 0L,
    val isLoading: Boolean = true,
    val isSaved: Boolean = false,
    val showColorPicker: Boolean = false,
    val showDeleteConfirm: Boolean = false,
)
