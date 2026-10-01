package com.example.notely.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Room database for Notely.
 *
 * - Version 1: initial schema with [NoteEntity].
 * - exportSchema = true so Room writes JSON schemas to `app/schemas/`
 *   (configured via KSP arg in build.gradle.kts).
 */
@Database(
    entities = [NoteEntity::class],
    version = 2,
    exportSchema = true,
)
abstract class NotelyDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
}
