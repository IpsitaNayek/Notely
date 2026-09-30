package com.example.notely.di

import com.example.notely.data.repository.DefaultNoteRepository
import com.example.notely.data.repository.NoteRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindNoteRepository(impl: DefaultNoteRepository): NoteRepository
}
