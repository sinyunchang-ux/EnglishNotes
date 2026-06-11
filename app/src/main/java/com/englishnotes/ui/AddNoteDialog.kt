package com.englishnotes.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AddNoteDialog(
    onDismiss: () -> Unit,
    onConfirm: (english: String, chinese: String) -> Unit
) {
    var english by remember { mutableStateOf("") }
    var chinese by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新增筆記") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = english,
                    onValueChange = { english = it; showError = false },
                    label = { Text("英文句子") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    maxLines = 4,
                    isError = showError && english.isBlank()
                )
                OutlinedTextField(
                    value = chinese,
                    onValueChange = { chinese = it },
                    label = { Text("中文翻譯") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    maxLines = 4
                )
                if (showError && english.isBlank()) {
                    Text("英文欄位不可為空", color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (english.isBlank()) { showError = true; return@TextButton }
                onConfirm(english.trim(), chinese.trim())
                onDismiss()
            }) { Text("新增") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}
