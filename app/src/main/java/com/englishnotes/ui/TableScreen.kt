package com.englishnotes.ui

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.englishnotes.data.NoteEntity
import com.englishnotes.viewmodel.NoteViewModel
import com.opencsv.CSVReader
import java.io.File
import java.io.InputStreamReader

@Composable
fun TableScreen(viewModel: NoteViewModel) {
    val notes by viewModel.allNotes.collectAsState()
    val context = LocalContext.current
    val isRecording by viewModel.isRecording.collectAsState()
    val recordingSeconds by viewModel.recordingSeconds.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var recordingForNote by remember { mutableStateOf<NoteEntity?>(null) }
    var playingNoteId by remember { mutableStateOf<Long?>(null) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var editingNote by remember { mutableStateOf<NoteEntity?>(null) }
    var deleteConfirmNote by remember { mutableStateOf<NoteEntity?>(null) }

    val csvLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { importCsv(context, it, viewModel) }
    }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            recordingForNote?.let { note ->
                viewModel.startRecording(context, note.id)
            }
        }
    }

    Column(Modifier.fillMaxSize()) {
        // ── Toolbar ─────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(HeaderBg)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "共 ${notes.size} 筆",
                color = Color.White.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodySmall
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { csvLauncher.launch("text/*") },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.FileUpload, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("匯入 CSV", fontSize = 12.sp)
                }
                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = PrimaryBlue),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Add, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("新增", fontSize = 12.sp)
                }
            }
        }

        // ── Table Header ────────────────────────────────────
        TableHeader()
        HorizontalDivider(color = DividerGray)

        if (notes.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.NoteAdd, null,
                        Modifier.size(64.dp), tint = DividerGray)
                    Spacer(Modifier.height(12.dp))
                    Text("尚無筆記", color = TextSecondary)
                    Text("點擊「新增」或「匯入 CSV」開始",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall)
                }
            }
        } else {
            LazyColumn {
                items(notes, key = { it.id }) { note ->
                    NoteRow(
                        note = note,
                        isPlayingThis = playingNoteId == note.id,
                        onRecord = {
                            recordingForNote = note
                            permissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                        },
                        onPlay = {
                            if (playingNoteId == note.id) {
                                mediaPlayer?.stop()
                                mediaPlayer?.release()
                                mediaPlayer = null
                                playingNoteId = null
                            } else {
                                mediaPlayer?.stop()
                                mediaPlayer?.release()
                                note.recordingPath?.let { path ->
                                    mediaPlayer = MediaPlayer().apply {
                                        setDataSource(path)
                                        prepare()
                                        start()
                                        setOnCompletionListener {
                                            playingNoteId = null
                                            it.release()
                                            mediaPlayer = null
                                        }
                                    }
                                    playingNoteId = note.id
                                }
                            }
                        },
                        onShare = { shareRecording(context, note) },
                        onEdit = { editingNote = note },
                        onDelete = { deleteConfirmNote = note }
                    )
                    HorizontalDivider(color = DividerGray, thickness = 0.5.dp)
                }
            }
        }
    }

    // ── Add Dialog ──────────────────────────────────────────
    if (showAddDialog) {
        AddNoteDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { en, zh -> viewModel.addNote(en, zh) }
        )
    }

    // ── Edit Dialog ─────────────────────────────────────────
    editingNote?.let { note ->
        EditNoteDialog(
            note = note,
            onDismiss = { editingNote = null },
            onConfirm = { en, zh ->
                viewModel.updateNote(note.copy(english = en, chinese = zh))
                editingNote = null
            }
        )
    }

    // ── Delete Confirm ──────────────────────────────────────
    deleteConfirmNote?.let { note ->
        AlertDialog(
            onDismissRequest = { deleteConfirmNote = null },
            title = { Text("確認刪除") },
            text = { Text("確定要刪除這條筆記嗎？錄音也會一併刪除。") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteNote(note)
                    deleteConfirmNote = null
                }) { Text("刪除", color = RedRecording) }
            },
            dismissButton = {
                TextButton(onClick = { deleteConfirmNote = null }) { Text("取消") }
            }
        )
    }

    // ── Recording Dialog ────────────────────────────────────
    recordingForNote?.let { note ->
        RecordingDialog(
            noteEnglish = note.english,
            isRecording = isRecording,
            seconds = recordingSeconds,
            hasExistingRecording = note.recordingPath != null,
            onStartRecording = {
                viewModel.startRecording(context, note.id)
            },
            onStopRecording = { viewModel.stopRecording(note.id) },
            onCancel = { viewModel.cancelRecording() },
            onDismiss = {
                if (!isRecording) recordingForNote = null
            }
        )
        // auto-dismiss after stop
        if (!isRecording && recordingForNote != null) {
            LaunchedEffect(isRecording) {
                if (!isRecording) recordingForNote = null
            }
        }
    }
}

@Composable
fun TableHeader() {
    Row(
        Modifier
            .fillMaxWidth()
            .background(SurfaceGray)
            .padding(vertical = 8.dp)
    ) {
        HeaderCell("英文句子", Modifier.weight(2.5f))
        HeaderCell("中文翻譯", Modifier.weight(2f))
        HeaderCell("錄音", Modifier.weight(1.2f))
        HeaderCell("新增日期", Modifier.weight(1.5f))
        HeaderCell("錄音日期", Modifier.weight(1.5f))
        HeaderCell("", Modifier.width(72.dp))
    }
}

@Composable
fun HeaderCell(text: String, modifier: Modifier) {
    Text(
        text = text,
        modifier = modifier.padding(horizontal = 6.dp),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = TextSecondary,
        textAlign = TextAlign.Center
    )
}

@Composable
fun NoteRow(
    note: NoteEntity,
    isPlayingThis: Boolean,
    onRecord: () -> Unit,
    onPlay: () -> Unit,
    onShare: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(if (note.recordingPath != null) GreenLight.copy(alpha = 0.3f) else Color.White)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // English
        Text(
            note.english,
            Modifier
                .weight(2.5f)
                .padding(horizontal = 6.dp),
            style = MaterialTheme.typography.bodySmall,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            color = TextPrimary
        )
        // Chinese
        Text(
            note.chinese.ifBlank { "—" },
            Modifier
                .weight(2f)
                .padding(horizontal = 6.dp),
            style = MaterialTheme.typography.bodySmall,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            color = TextSecondary
        )
        // Recording cell
        Column(
            Modifier.weight(1.2f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            if (note.recordingPath != null) {
                IconButton(onClick = onPlay, modifier = Modifier.size(28.dp)) {
                    Icon(
                        if (isPlayingThis) Icons.Default.Stop else Icons.Default.PlayArrow,
                        null,
                        tint = GreenRecorded,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = onShare, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Share, null,
                        tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                }
            }
            IconButton(onClick = onRecord, modifier = Modifier.size(28.dp)) {
                Icon(
                    Icons.Default.Mic,
                    null,
                    tint = if (note.recordingPath != null) TextSecondary else RedRecording,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        // Added date
        Text(
            note.addedDate,
            Modifier
                .weight(1.5f)
                .padding(horizontal = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
        // Recording date
        Text(
            note.recordingDate ?: "—",
            Modifier
                .weight(1.5f)
                .padding(horizontal = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = if (note.recordingDate != null) GreenRecorded else TextSecondary,
            textAlign = TextAlign.Center
        )
        // Actions
        Row(Modifier.width(72.dp), horizontalArrangement = Arrangement.Center) {
            IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Edit, null,
                    tint = TextSecondary, modifier = Modifier.size(15.dp))
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Delete, null,
                    tint = RedRecording.copy(alpha = 0.7f), modifier = Modifier.size(15.dp))
            }
        }
    }
}

// ── Helpers ─────────────────────────────────────────────────────────────────

fun shareRecording(context: Context, note: NoteEntity) {
    val path = note.recordingPath ?: return
    val file = File(path)
    if (!file.exists()) return
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "audio/*"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_TEXT, "📝 ${note.english}\n${note.chinese}")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "分享錄音到…"))
}

fun importCsv(context: Context, uri: Uri, viewModel: NoteViewModel) {
    try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return
        val reader = CSVReader(InputStreamReader(inputStream))
        val rows = reader.readAll()
        reader.close()
        val today = viewModel.todayString()
        val notes = mutableListOf<NoteEntity>()
        for (row in rows) {
            if (row.isEmpty()) continue
            val english = row.getOrElse(0) { "" }.trim()
            val chinese = row.getOrElse(1) { "" }.trim()
            if (english.isBlank()) continue
            // Skip header row
            if (english.equals("english", ignoreCase = true) ||
                english.equals("英文", ignoreCase = true)) continue
            notes.add(NoteEntity(english = english, chinese = chinese, addedDate = today))
        }
        if (notes.isNotEmpty()) viewModel.importNotes(notes)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
