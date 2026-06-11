package com.englishnotes.viewmodel

import android.app.Application
import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.englishnotes.data.NoteDatabase
import com.englishnotes.data.NoteEntity
import com.englishnotes.data.NoteRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class NoteViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = NoteRepository(NoteDatabase.getDatabase(application).noteDao())

    val allNotes: StateFlow<List<NoteEntity>> = repo.allNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAddedDates: StateFlow<List<String>> = repo.allAddedDates
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRecordingDates: StateFlow<List<String>> = repo.allRecordingDates
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Recording state
    private var mediaRecorder: MediaRecorder? = null
    private var currentRecordingFile: File? = null
    private var recordingStartTime: Long = 0L

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording

    private val _recordingSeconds = MutableStateFlow(0)
    val recordingSeconds: StateFlow<Int> = _recordingSeconds

    private var recordingJob: kotlinx.coroutines.Job? = null

    fun todayString(): String =
        LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

    fun addNote(english: String, chinese: String) {
        viewModelScope.launch {
            repo.insert(
                NoteEntity(
                    english = english,
                    chinese = chinese,
                    addedDate = todayString()
                )
            )
        }
    }

    fun importNotes(notes: List<NoteEntity>) {
        viewModelScope.launch { repo.insertAll(notes) }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            note.recordingPath?.let { File(it).delete() }
            repo.delete(note)
        }
    }

    fun updateNote(note: NoteEntity) {
        viewModelScope.launch { repo.update(note) }
    }

    // ── Recording ──────────────────────────────────────────────────────────
    fun startRecording(context: Context, noteId: Long) {
        if (_isRecording.value) return
        val dir = File(context.filesDir, "recordings").also { it.mkdirs() }
        val file = File(dir, "note_${noteId}_${System.currentTimeMillis()}.m4a")
        currentRecordingFile = file

        mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(file.absolutePath)
            setMaxDuration(180_000) // 180 seconds
            setOnInfoListener { _, what, _ ->
                if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                    stopRecording(noteId)
                }
            }
            prepare()
            start()
        }

        _isRecording.value = true
        _recordingSeconds.value = 0
        recordingStartTime = System.currentTimeMillis()

        recordingJob = viewModelScope.launch {
            while (_isRecording.value) {
                kotlinx.coroutines.delay(1000)
                _recordingSeconds.value = ((System.currentTimeMillis() - recordingStartTime) / 1000).toInt()
                if (_recordingSeconds.value >= 180) stopRecording(noteId)
            }
        }
    }

    fun stopRecording(noteId: Long) {
        if (!_isRecording.value) return
        recordingJob?.cancel()
        try {
            mediaRecorder?.stop()
        } catch (_: Exception) {}
        mediaRecorder?.release()
        mediaRecorder = null
        _isRecording.value = false
        _recordingSeconds.value = 0

        currentRecordingFile?.let { file ->
            viewModelScope.launch {
                val notes = allNotes.value
                val note = notes.find { it.id == noteId } ?: return@launch
                // Delete old recording if exists
                note.recordingPath?.let { File(it).delete() }
                repo.update(
                    note.copy(
                        recordingPath = file.absolutePath,
                        recordingDate = todayString()
                    )
                )
            }
        }
        currentRecordingFile = null
    }

    fun cancelRecording() {
        recordingJob?.cancel()
        try { mediaRecorder?.stop() } catch (_: Exception) {}
        mediaRecorder?.release()
        mediaRecorder = null
        _isRecording.value = false
        _recordingSeconds.value = 0
        currentRecordingFile?.delete()
        currentRecordingFile = null
    }

    override fun onCleared() {
        super.onCleared()
        mediaRecorder?.release()
    }
}
