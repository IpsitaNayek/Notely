package com.example.notely.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for [NoteEntity].
 *
 * All list queries return [Flow] for reactive UI updates.
 * Write operations are suspend functions run on IO via the repository.
 */
@Dao
interface NoteDao {

    // ── Reads (reactive) ──

    /** All non-trashed notes, pinned first, then by [updatedAt] descending. */
    @Query(
        """
        SELECT * FROM notes 
        WHERE isTrashed = 0 
        ORDER BY isPinned DESC, updatedAt DESC
        """
    )
    fun observeActiveNotes(): Flow<List<NoteEntity>>

    /** All trashed notes, most recently trashed first. */
    @Query(
        """
        SELECT * FROM notes 
        WHERE isTrashed = 1 
        ORDER BY updatedAt DESC
        """
    )
    fun observeTrashedNotes(): Flow<List<NoteEntity>>

    /** Single note by ID (reactive, for editor). */
    @Query("SELECT * FROM notes WHERE id = :noteId")
    fun observeNoteById(noteId: String): Flow<NoteEntity?>

    // ── Reads (one-shot) ──

    /** Single note by ID (suspend, for sync). */
    @Query("SELECT * FROM notes WHERE id = :noteId")
    suspend fun getNoteById(noteId: String): NoteEntity?

    /** All notes that need to be synced upstream. */
    @Query("SELECT * FROM notes WHERE syncStatus != 0")
    suspend fun getUnsyncedNotes(): List<NoteEntity>

    /** Count of active (non-trashed) notes. */
    @Query("SELECT COUNT(*) FROM notes WHERE isTrashed = 0")
    fun observeActiveNoteCount(): Flow<Int>

    // ── Writes ──

    /** Insert or update a note. */
    @Upsert
    suspend fun upsert(note: NoteEntity)

    /** Hard-delete a note (used after sync confirms remote deletion). */
    @Delete
    suspend fun delete(note: NoteEntity)

    /** Hard-delete all trashed notes (empty trash). */
    @Query("DELETE FROM notes WHERE isTrashed = 1")
    suspend fun emptyTrash()

    /** Soft-delete: move to trash. */
    @Query("UPDATE notes SET isTrashed = 1, updatedAt = :now, syncStatus = 1 WHERE id = :noteId")
    suspend fun moveToTrash(noteId: String, now: Long = System.currentTimeMillis())

    /** Restore from trash. */
    @Query("UPDATE notes SET isTrashed = 0, updatedAt = :now, syncStatus = 1 WHERE id = :noteId")
    suspend fun restoreFromTrash(noteId: String, now: Long = System.currentTimeMillis())

    /** Toggle pin status. */
    @Query("UPDATE notes SET isPinned = NOT isPinned, updatedAt = :now, syncStatus = 1 WHERE id = :noteId")
    suspend fun togglePin(noteId: String, now: Long = System.currentTimeMillis())

    /** Mark a note as synced. */
    @Query("UPDATE notes SET syncStatus = 0 WHERE id = :noteId")
    suspend fun markSynced(noteId: String)

    /** Hard-delete a single note by ID. */
    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun deleteById(noteId: String)

    /** Full-text search across title, body, and checklist content. */
    @Query(
        """
        SELECT * FROM notes 
        WHERE isTrashed = 0 
          AND (title LIKE '%' || :query || '%' OR body LIKE '%' || :query || '%' OR checklistJson LIKE '%' || :query || '%') 
        ORDER BY isPinned DESC, updatedAt DESC
        """
    )
    fun search(query: String): Flow<List<NoteEntity>>
}
