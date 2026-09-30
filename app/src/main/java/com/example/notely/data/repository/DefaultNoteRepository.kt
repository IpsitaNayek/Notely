package com.example.notely.data.repository

import com.example.notely.data.local.NoteDao
import com.example.notely.data.local.NoteEntity
import com.example.notely.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Default implementation of [NoteRepository] backed by Room via [NoteDao].
 *
 * - All Flow queries are moved to the IO dispatcher.
 * - All suspend writes run on the IO dispatcher.
 * - [saveNote] automatically stamps [NoteEntity.updatedAt] and marks sync pending.
 */
@Singleton
class DefaultNoteRepository @Inject constructor(
    private val noteDao: NoteDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : NoteRepository {

    // ── Observe ──

    override fun observeActiveNotes(): Flow<List<NoteEntity>> =
        noteDao.observeActiveNotes().flowOn(ioDispatcher)

    override fun observeTrashedNotes(): Flow<List<NoteEntity>> =
        noteDao.observeTrashedNotes().flowOn(ioDispatcher)

    override fun observeNoteById(noteId: String): Flow<NoteEntity?> =
        noteDao.observeNoteById(noteId).flowOn(ioDispatcher)

    override fun observeActiveNoteCount(): Flow<Int> =
        noteDao.observeActiveNoteCount().flowOn(ioDispatcher)

    override fun search(query: String): Flow<List<NoteEntity>> =
        noteDao.search(query).flowOn(ioDispatcher)

    // ── Write ──

    override suspend fun saveNote(note: NoteEntity) = withContext(ioDispatcher) {
        noteDao.upsert(
            note.copy(
                updatedAt = System.currentTimeMillis(),
                syncStatus = NoteEntity.SYNC_PENDING_UPLOAD,
            )
        )
    }

    override suspend fun moveToTrash(noteId: String) = withContext(ioDispatcher) {
        noteDao.moveToTrash(noteId)
    }

    override suspend fun restoreFromTrash(noteId: String) = withContext(ioDispatcher) {
        noteDao.restoreFromTrash(noteId)
    }

    override suspend fun togglePin(noteId: String) = withContext(ioDispatcher) {
        noteDao.togglePin(noteId)
    }

    override suspend fun emptyTrash() = withContext(ioDispatcher) {
        noteDao.emptyTrash()
    }

    override suspend fun deleteNote(note: NoteEntity) = withContext(ioDispatcher) {
        noteDao.delete(note)
    }

    // ── Sync helpers ──

    override suspend fun getNoteById(noteId: String): NoteEntity? = withContext(ioDispatcher) {
        noteDao.getNoteById(noteId)
    }

    override suspend fun getUnsyncedNotes(): List<NoteEntity> = withContext(ioDispatcher) {
        noteDao.getUnsyncedNotes()
    }

    override suspend fun markSynced(noteId: String) = withContext(ioDispatcher) {
        noteDao.markSynced(noteId)
    }
}
