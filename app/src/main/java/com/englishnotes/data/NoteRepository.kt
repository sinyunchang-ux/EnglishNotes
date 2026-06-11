package com.englishnotes.data

import kotlinx.coroutines.flow.Flow

class NoteRepository(private val dao: NoteDao) {
    val allNotes: Flow<List<NoteEntity>> = dao.getAllNotes()
    val allAddedDates: Flow<List<String>> = dao.getAllAddedDates()
    val allRecordingDates: Flow<List<String>> = dao.getAllRecordingDates()

    suspend fun insert(note: NoteEntity): Long = dao.insertNote(note)
    suspend fun insertAll(notes: List<NoteEntity>) = dao.insertNotes(notes)
    suspend fun update(note: NoteEntity) = dao.updateNote(note)
    suspend fun delete(note: NoteEntity) = dao.deleteNote(note)
    suspend fun deleteById(id: Long) = dao.deleteNoteById(id)
    fun getNotesByDate(date: String): Flow<List<NoteEntity>> = dao.getNotesByDate(date)
}
