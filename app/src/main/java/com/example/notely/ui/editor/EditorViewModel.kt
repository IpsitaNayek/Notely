package com.example.notely.ui.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notely.data.local.ChecklistItem
import com.example.notely.data.local.NoteEntity
import com.example.notely.data.local.NoteType
import com.example.notely.data.model.NoteContentBlock
import com.example.notely.data.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class EditorViewModel @Inject constructor(
    private val repository: NoteRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val noteId: String? = savedStateHandle["noteId"]
    private val initialType: String? = savedStateHandle["type"]

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    init {
        if (noteId != null) {
            loadNote(noteId)
        } else {
            val type = initialType ?: NoteType.NORMAL.name
            val initialChecklist = if (type == NoteType.TODO.name) {
                listOf(ChecklistItem(text = ""))
            } else {
                emptyList()
            }
            val initialBlock = NoteContentBlock.Text()
            _uiState.update {
                it.copy(
                    noteType = type,
                    checklist = initialChecklist,
                    contentBlocks = listOf(initialBlock),
                    focusedBlockId = initialBlock.id,
                    cursorPositionInBlock = 0,
                    isLoading = false
                )
            }
        }
    }

    private fun loadNote(id: String) {
        viewModelScope.launch {
            val note = repository.getNoteById(id)
            if (note != null) {
                val parsedBlocks = NoteContentBlock.parseBody(note.body).toMutableList()

                // Backward-compatibility: if imageUri or audioPath were set outside body, append them
                if (note.imageUri.isNotBlank() && parsedBlocks.none { it is NoteContentBlock.Image && it.uri == note.imageUri }) {
                    parsedBlocks.add(NoteContentBlock.Image(uri = note.imageUri))
                }
                if (note.audioPath.isNotBlank() && parsedBlocks.none { it is NoteContentBlock.Audio && it.path == note.audioPath }) {
                    parsedBlocks.add(NoteContentBlock.Audio(path = note.audioPath, durationSec = note.audioDurationSec))
                }

                if (parsedBlocks.isEmpty() || parsedBlocks.last() !is NoteContentBlock.Text) {
                    parsedBlocks.add(NoteContentBlock.Text(content = ""))
                }

                _uiState.update {
                    it.copy(
                        noteId = note.id,
                        title = note.title,
                        colorId = note.colorId,
                        isPinned = note.isPinned,
                        isTrashed = note.isTrashed,
                        createdAt = note.createdAt,
                        noteType = note.noteType,
                        checklist = note.getChecklist(),
                        contentBlocks = parsedBlocks,
                        focusedBlockId = parsedBlocks.firstOrNull { b -> b is NoteContentBlock.Text }?.id,
                        cursorPositionInBlock = 0,
                        isLoading = false,
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    // ── Title & Text Block Edits ──

    fun onTitleChanged(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun onUpdateTextBlock(blockId: String, newContent: String, cursorPosition: Int) {
        _uiState.update { current ->
            val updated = current.contentBlocks.map { block ->
                if (block is NoteContentBlock.Text && block.id == blockId) {
                    block.copy(content = newContent)
                } else {
                    block
                }
            }
            current.copy(
                contentBlocks = updated,
                focusedBlockId = blockId,
                cursorPositionInBlock = cursorPosition
            )
        }
    }

    fun onSetFocusedBlock(blockId: String, cursorPosition: Int) {
        _uiState.update {
            it.copy(
                focusedBlockId = blockId,
                cursorPositionInBlock = cursorPosition
            )
        }
    }

    // ── Cursor-Positioned Media Insertion ──

    fun onInsertImageAtCursor(uri: String) {
        _uiState.update { current ->
            val blocks = current.contentBlocks.toMutableList()
            val focusedId = current.focusedBlockId
            val cursor = current.cursorPositionInBlock

            val targetIndex = blocks.indexOfFirst { it.id == focusedId }
            val newImageBlock = NoteContentBlock.Image(uri = uri)
            val newAfterTextBlock = NoteContentBlock.Text(content = "")

            if (targetIndex >= 0 && blocks[targetIndex] is NoteContentBlock.Text) {
                val currentText = (blocks[targetIndex] as NoteContentBlock.Text).content
                val pos = cursor.coerceIn(0, currentText.length)
                val before = currentText.substring(0, pos)
                val after = currentText.substring(pos)

                blocks[targetIndex] = NoteContentBlock.Text(
                    id = blocks[targetIndex].id,
                    content = before
                )
                blocks.add(targetIndex + 1, newImageBlock)
                blocks.add(targetIndex + 2, NoteContentBlock.Text(id = newAfterTextBlock.id, content = after))
            } else {
                // If cursor not in a text block, append at the end
                blocks.add(newImageBlock)
                blocks.add(newAfterTextBlock)
            }

            current.copy(
                contentBlocks = blocks,
                focusedBlockId = newAfterTextBlock.id,
                cursorPositionInBlock = 0
            )
        }
    }

    fun onInsertAudioAtCursor(file: File, durationSec: Int) {
        _uiState.update { current ->
            val blocks = current.contentBlocks.toMutableList()
            val focusedId = current.focusedBlockId
            val cursor = current.cursorPositionInBlock

            val targetIndex = blocks.indexOfFirst { it.id == focusedId }
            val newAudioBlock = NoteContentBlock.Audio(path = file.absolutePath, durationSec = durationSec)
            val newAfterTextBlock = NoteContentBlock.Text(content = "")

            if (targetIndex >= 0 && blocks[targetIndex] is NoteContentBlock.Text) {
                val currentText = (blocks[targetIndex] as NoteContentBlock.Text).content
                val pos = cursor.coerceIn(0, currentText.length)
                val before = currentText.substring(0, pos)
                val after = currentText.substring(pos)

                blocks[targetIndex] = NoteContentBlock.Text(
                    id = blocks[targetIndex].id,
                    content = before
                )
                blocks.add(targetIndex + 1, newAudioBlock)
                blocks.add(targetIndex + 2, NoteContentBlock.Text(id = newAfterTextBlock.id, content = after))
            } else {
                blocks.add(newAudioBlock)
                blocks.add(newAfterTextBlock)
            }

            current.copy(
                contentBlocks = blocks,
                focusedBlockId = newAfterTextBlock.id,
                cursorPositionInBlock = 0,
                noteType = if (current.noteType == NoteType.NORMAL.name) NoteType.AUDIO.name else current.noteType
            )
        }
    }

    fun onRemoveBlock(blockId: String) {
        _uiState.update { current ->
            val updated = current.contentBlocks.filterNot { it.id == blockId }.toMutableList()
            if (updated.isEmpty()) {
                updated.add(NoteContentBlock.Text(content = ""))
            }
            current.copy(contentBlocks = updated)
        }
    }

    fun onViewImage(uri: String?) {
        _uiState.update { it.copy(fullscreenImageUri = uri) }
    }

    // ── Formatting Actions at Cursor ──

    fun onInsertBullet() {
        _uiState.update { current ->
            insertPrefixAtCursor(current, "• ")
        }
    }

    fun onInsertNumbering() {
        _uiState.update { current ->
            insertPrefixAtCursor(current, "1. ")
        }
    }

    private fun insertPrefixAtCursor(current: EditorUiState, prefix: String): EditorUiState {
        val blocks = current.contentBlocks.toMutableList()
        val focusedId = current.focusedBlockId ?: blocks.firstOrNull { it is NoteContentBlock.Text }?.id
        val targetIndex = blocks.indexOfFirst { it.id == focusedId }

        if (targetIndex >= 0 && blocks[targetIndex] is NoteContentBlock.Text) {
            val block = blocks[targetIndex] as NoteContentBlock.Text
            val pos = current.cursorPositionInBlock.coerceIn(0, block.content.length)
            val newContent = block.content.substring(0, pos) + prefix + block.content.substring(pos)
            blocks[targetIndex] = block.copy(content = newContent)
            return current.copy(
                contentBlocks = blocks,
                cursorPositionInBlock = pos + prefix.length
            )
        } else {
            val newBlock = NoteContentBlock.Text(content = prefix)
            blocks.add(newBlock)
            return current.copy(
                contentBlocks = blocks,
                focusedBlockId = newBlock.id,
                cursorPositionInBlock = prefix.length
            )
        }
    }

    // ── Checkbox / Checklist Actions ──

    fun onAddChecklistItem() {
        _uiState.update { current ->
            current.copy(
                checklist = current.checklist + ChecklistItem(text = ""),
                noteType = NoteType.TODO.name
            )
        }
    }

    fun onUpdateChecklistItem(id: String, text: String) {
        _uiState.update { current ->
            current.copy(
                checklist = current.checklist.map {
                    if (it.id == id) it.copy(text = text) else it
                }
            )
        }
    }

    fun onToggleChecklistItem(id: String) {
        _uiState.update { current ->
            current.copy(
                checklist = current.checklist.map {
                    if (it.id == id) it.copy(isChecked = !it.isChecked) else it
                }
            )
        }
    }

    fun onRemoveChecklistItem(id: String) {
        _uiState.update { current ->
            val updated = current.checklist.filterNot { it.id == id }
            current.copy(
                checklist = updated,
                noteType = if (updated.isEmpty() && current.contentBlocks.isNotEmpty()) NoteType.NORMAL.name else current.noteType
            )
        }
    }

    // ── Color, Pin, Trash, Save ──

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

    fun save(): Boolean {
        val state = _uiState.value
        val serializedBody = NoteContentBlock.serializeBody(state.contentBlocks)
        val hasChecklist = state.checklist.any { it.text.isNotBlank() }
        val hasContent = state.title.isNotBlank() || serializedBody.isNotBlank() || hasChecklist

        if (!hasContent) return false

        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val validChecklist = state.checklist.filter { it.text.isNotBlank() }
            val allImages = NoteContentBlock.extractAllImages(serializedBody)
            val allAudio = NoteContentBlock.extractAllAudio(serializedBody)

            val determinedType = when {
                allAudio.isNotEmpty() -> NoteType.AUDIO.name
                validChecklist.isNotEmpty() -> NoteType.TODO.name
                else -> NoteType.NORMAL.name
            }

            val entity = NoteEntity(
                id = state.noteId ?: UUID.randomUUID().toString(),
                title = state.title.trim(),
                body = serializedBody,
                colorId = state.colorId,
                isPinned = state.isPinned,
                isTrashed = state.isTrashed,
                createdAt = if (state.createdAt > 0L) state.createdAt else now,
                updatedAt = now,
                noteType = determinedType,
                checklistJson = ChecklistItem.listToJson(validChecklist),
                imageUri = allImages.firstOrNull() ?: "",
                audioPath = allAudio.firstOrNull()?.first ?: "",
                audioDurationSec = allAudio.firstOrNull()?.second ?: 0,
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
