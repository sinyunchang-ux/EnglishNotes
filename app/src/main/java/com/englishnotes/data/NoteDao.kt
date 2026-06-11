package com.englishnotes.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY id DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE addedDate = :date ORDER BY id DESC")
    fun getNotesByDate(date: String): Flow<List<NoteEntity>>

    @Query("SELECT DISTINCT addedDate FROM notes")
    fun getAllAddedDates(): Flow<List<String>>

    @Query("SELECT DISTINCT recordingDate FROM notes WHERE recordingDate IS NOT NULL")
    fun getAllRecordingDates(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotes(notes: List<NoteEntity>)

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNoteById(id: Long)
}
