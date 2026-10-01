package com.example.notely.ui.editor

import com.example.notely.data.local.ChecklistItem
import com.example.notely.data.local.NoteType
import com.example.notely.data.model.NoteContentBlock

/**
 * UI state for the note editor with support for inline cursor-positioned media blocks.
 */
data class EditorUiState(
    val noteId: String? = null,
    val title: String = "",
    val colorId: Int = 0,
    val isPinned: Boolean = false,
    val isTrashed: Boolean = false,
    val createdAt: Long = 0L,
    val noteType: String = NoteType.NORMAL.name,
    val checklist: List<ChecklistItem> = emptyList(),
    val contentBlocks: List<NoteContentBlock> = listOf(NoteContentBlock.Text()),
    val focusedBlockId: String? = null,
    val cursorPositionInBlock: Int = 0,
    val fullscreenImageUri: String? = null,
    val isLoading: Boolean = true,
    val isSaved: Boolean = false,
    val showColorPicker: Boolean = false,
    val showDeleteConfirm: Boolean = false,
)
