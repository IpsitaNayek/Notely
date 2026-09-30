package com.example.notely.data.repository

import com.example.notely.data.local.NoteEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository contract for note operations.
 *
 * All reads return [Flow] for reactive observation.
 * All writes are suspend functions.
 * The repository is the single source of truth — ViewModels never touch the DAO directly.
 */
interface NoteRepository {

    // ── Observe ──

    /** Active (non-trashed) notes, pinned first, then newest. */
    fun observeActiveNotes(): Flow<List<NoteEntity>>

    /** Trashed notes, newest first. */
    fun observeTrashedNotes(): Flow<List<NoteEntity>>

    /** Single note by ID (reactive). */
    fun observeNoteById(noteId: String): Flow<NoteEntity?>

    /** Count of active notes. */
    fun observeActiveNoteCount(): Flow<Int>

    /** Search notes by title/body. */
    fun search(query: String): Flow<List<NoteEntity>>

    // ── Write ──

    /** Create or update a note. Sets syncStatus to pending. */
    suspend fun saveNote(note: NoteEntity)

    /** Soft-delete: move note to trash. */
    suspend fun moveToTrash(noteId: String)

    /** Restore a note from trash. */
    suspend fun restoreFromTrash(noteId: String)

    /** Toggle pin status. */
    suspend fun togglePin(noteId: String)

    /** Permanently delete all trashed notes. */
    suspend fun emptyTrash()

    /** Hard-delete a single note. */
    suspend fun deleteNote(note: NoteEntity)

    // ── Sync helpers ──

    /** Get a note by ID (one-shot, for sync). */
    suspend fun getNoteById(noteId: String): NoteEntity?

    /** Get all notes pending sync. */
    suspend fun getUnsyncedNotes(): List<NoteEntity>

    /** Mark a note as synced. */
    suspend fun markSynced(noteId: String)
}
