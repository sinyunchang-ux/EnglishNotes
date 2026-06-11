package com.englishnotes.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.englishnotes.data.NoteEntity
import com.englishnotes.viewmodel.NoteViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarScreen(viewModel: NoteViewModel) {
    val notes by viewModel.allNotes.collectAsState()
    val recordingDates by viewModel.allRecordingDates.collectAsState()

    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    // Build maps: date -> count, date -> hasRecording
    val dateNoteCount = remember(notes) {
        notes.groupBy { it.addedDate }.mapValues { it.value.size }
    }
    val datesWithRecording = remember(notes) {
        notes.filter { it.recordingDate != null }.map { it.recordingDate!! }.toSet()
    }

    val selectedDateNotes = remember(selectedDate, notes) {
        selectedDate?.let { d ->
            val ds = d.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            notes.filter { it.addedDate == ds }
        } ?: emptyList()
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Month navigator
        Row(
            Modifier
                .fillMaxWidth()
                .background(HeaderBg)
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                Icon(Icons.Default.ChevronLeft, null, tint = Color.White)
            }
            Text(
                "${currentMonth.year} 年 ${currentMonth.monthValue} 月",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                Icon(Icons.Default.ChevronRight, null, tint = Color.White)
            }
        }

        // Day of week headers
        Row(
            Modifier
                .fillMaxWidth()
                .background(SurfaceGray)
                .padding(vertical = 6.dp)
        ) {
            listOf("一", "二", "三", "四", "五", "六", "日").forEach { day ->
                Text(
                    day,
                    Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )
            }
        }

        // Calendar grid
        val firstDay = currentMonth.atDay(1)
        // Monday = 1, so offset: Monday=0, Tuesday=1, ..., Sunday=6
        val startOffset = (firstDay.dayOfWeek.value - 1) % 7
        val daysInMonth = currentMonth.lengthOfMonth()
        val today = LocalDate.now()

        val cells = startOffset + daysInMonth
        val rows = (cells + 6) / 7

        Column(Modifier.padding(horizontal = 4.dp)) {
            for (row in 0 until rows) {
                Row(Modifier.fillMaxWidth()) {
                    for (col in 0 until 7) {
                        val cellIndex = row * 7 + col
                        val dayNum = cellIndex - startOffset + 1
                        if (dayNum < 1 || dayNum > daysInMonth) {
                            Box(Modifier.weight(1f).aspectRatio(1f))
                        } else {
                            val date = currentMonth.atDay(dayNum)
                            val ds = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                            val count = dateNoteCount[ds] ?: 0
                            val hasRecording = datesWithRecording.contains(ds)
                            val isSelected = selectedDate == date
                            val isToday = date == today

                            CalendarCell(
                                day = dayNum,
                                count = count,
                                hasRecording = hasRecording,
                                isSelected = isSelected,
                                isToday = isToday,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    selectedDate = if (selectedDate == date) null else date
                                }
                            )
                        }
                    }
                }
                HorizontalDivider(color = DividerGray, thickness = 0.5.dp)
            }
        }

        // Legend
        Row(
            Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LegendItem(GreenRecorded, "有錄音")
            LegendItem(PrimaryBlue.copy(alpha = 0.15f), "有筆記（無錄音）")
            LegendItem(Color.White, "無筆記", border = true)
        }

        // Selected date detail
        selectedDate?.let { d ->
            val ds = d.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = DividerGray)
            Text(
                "$ds 的筆記（${selectedDateNotes.size} 筆）",
                Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            if (selectedDateNotes.isEmpty()) {
                Text("這天沒有筆記",
                    Modifier.padding(horizontal = 16.dp),
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall)
            } else {
                selectedDateNotes.forEach { note ->
                    CalendarNoteCard(note)
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun CalendarCell(
    day: Int,
    count: Int,
    hasRecording: Boolean,
    isSelected: Boolean,
    isToday: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val bgColor = when {
        isSelected -> PrimaryBlue.copy(alpha = 0.2f)
        hasRecording -> GreenRecorded.copy(alpha = 0.25f)
        count > 0 -> PrimaryBlue.copy(alpha = 0.08f)
        else -> Color.Transparent
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(
                width = if (isSelected) 2.dp else if (isToday) 1.5.dp else 0.dp,
                color = if (isSelected) PrimaryBlue else if (isToday) PrimaryBlue.copy(alpha = 0.5f) else Color.Transparent,
                shape = RoundedCornerShape(6.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                day.toString(),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) PrimaryBlue else TextPrimary,
                fontSize = 13.sp
            )
            if (count > 0) {
                Box(
                    Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(if (hasRecording) GreenRecorded else PrimaryBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (count > 9) "9+" else count.toString(),
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String, border: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(
            Modifier
                .size(14.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color)
                .then(if (border) Modifier.border(1.dp, DividerGray, RoundedCornerShape(3.dp)) else Modifier)
        )
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
    }
}

@Composable
fun CalendarNoteCard(note: NoteEntity) {
    Card(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceGray),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(note.english,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = TextPrimary)
            if (note.chinese.isNotBlank()) {
                Text(note.chinese,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary)
            }
            if (note.recordingDate != null) {
                Spacer(Modifier.height(4.dp))
                Text("🎙 錄音日期：${note.recordingDate}",
                    style = MaterialTheme.typography.labelSmall,
                    color = GreenRecorded)
            }
        }
    }
}
