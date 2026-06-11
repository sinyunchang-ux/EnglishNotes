package com.englishnotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.TableRows
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.englishnotes.ui.CalendarScreen
import com.englishnotes.ui.EnglishNotesTheme
import com.englishnotes.ui.TableScreen
import com.englishnotes.viewmodel.NoteViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: NoteViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EnglishNotesTheme {
                MainApp(viewModel)
            }
        }
    }
}

enum class Tab(val label: String, val icon: ImageVector) {
    TABLE("列表", Icons.Default.TableRows),
    CALENDAR("月曆", Icons.Default.CalendarMonth)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(viewModel: NoteViewModel) {
    var selectedTab by remember { mutableStateOf(Tab.TABLE) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📚 英文筆記") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = com.englishnotes.ui.HeaderBg,
                    titleContentColor = androidx.compose.ui.graphics.Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar {
                Tab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (selectedTab) {
                Tab.TABLE -> TableScreen(viewModel)
                Tab.CALENDAR -> CalendarScreen(viewModel)
            }
        }
    }
}
