package com.example.notely.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Room entity representing a single note.
 *
 * - [id]: UUID string, generated at creation time.
 * - [colorId]: Index into [NotelyColors.noteColors] (0..5). Default 0 = Terracotta.
 * - [isPinned]: Pinned notes float to the top of the grid.
 * - [isTrashed]: Soft-deleted notes are hidden from the main view but recoverable.
 * - [createdAt]: Epoch millis when the note was first created.
 * - [updatedAt]: Epoch millis of the last content edit.
 * - [syncStatus]: 0 = synced, 1 = pending upload, 2 = pending delete.
 */
@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val body: String = "",
    val colorId: Int = 0,
    val isPinned: Boolean = false,
    val isTrashed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncStatus: Int = SYNC_PENDING_UPLOAD,
) {
    companion object {
        const val SYNC_SYNCED = 0
        const val SYNC_PENDING_UPLOAD = 1
        const val SYNC_PENDING_DELETE = 2
    }
}
