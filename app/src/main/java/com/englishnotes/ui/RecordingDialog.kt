package com.englishnotes.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RecordingDialog(
    noteEnglish: String,
    isRecording: Boolean,
    seconds: Int,
    hasExistingRecording: Boolean,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onCancel: () -> Unit,
    onDismiss: () -> Unit
) {
    val progress = seconds / 180f

    AlertDialog(
        onDismissRequest = { if (!isRecording) onDismiss() },
        title = { Text("錄音", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = noteEnglish,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                // Big mic button
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(if (isRecording) RedRecording else PrimaryBlue)
                ) {
                    Icon(
                        imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(Modifier.height(16.dp))

                if (isRecording) {
                    Text(
                        text = formatSeconds(seconds),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = RedRecording
                    )
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth(),
                        color = RedRecording,
                        trackColor = DividerGray
                    )
                    Text(
                        text = "最長 180 秒",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                } else if (hasExistingRecording) {
                    Text(
                        text = "✅ 已有錄音，可重新錄製",
                        style = MaterialTheme.typography.bodySmall,
                        color = GreenRecorded
                    )
                }
            }
        },
        confirmButton = {
            if (isRecording) {
                TextButton(onClick = onStopRecording) { Text("停止錄音", color = RedRecording) }
            } else {
                TextButton(onClick = onStartRecording) { Text("開始錄音") }
            }
        },
        dismissButton = {
            if (isRecording) {
                TextButton(onClick = onCancel) { Text("取消", color = TextSecondary) }
            } else {
                TextButton(onClick = onDismiss) { Text("關閉") }
            }
        }
    )
}

fun formatSeconds(s: Int): String {
    val m = s / 60
    val sec = s % 60
    return "%02d:%02d".format(m, sec)
}
