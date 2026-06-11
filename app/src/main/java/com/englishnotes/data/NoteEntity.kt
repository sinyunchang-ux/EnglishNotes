package com.englishnotes.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val english: String,
    val chinese: String,
    val recordingPath: String? = null,
    val addedDate: String,       // "2026-06-11"
    val recordingDate: String? = null  // "2026-06-11"
)
