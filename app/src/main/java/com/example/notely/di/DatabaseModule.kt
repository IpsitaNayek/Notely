package com.example.notely.di

import android.content.Context
import androidx.room.Room
import com.example.notely.data.local.NoteDao
import com.example.notely.data.local.NotelyDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): NotelyDatabase =
        Room.databaseBuilder(
            context,
            NotelyDatabase::class.java,
            "notely.db",
        ).build()

    @Provides
    fun provideNoteDao(database: NotelyDatabase): NoteDao = database.noteDao()
}
