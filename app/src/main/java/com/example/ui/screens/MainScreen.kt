package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.model.Note
import com.example.data.model.NoteCategory
import com.example.data.repository.NoteRepository
import com.example.ui.theme.BorderDark
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.ObsidianDark
import com.example.ui.theme.SurfaceDark
import com.example.ui.viewmodel.ChatViewModel
import com.example.ui.viewmodel.NotesViewModel
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun MainScreen(
    currentUserId: String,
    databaseId: String,
    onSignOut: () -> Unit
) {
    val firestore = remember(databaseId) {
        FirebaseFirestore.getInstance(databaseId)
    }
    val noteRepository = remember(firestore) {
        NoteRepository(firestore)
    }

    val notesViewModel: NotesViewModel = viewModel(
        key = currentUserId,
        factory = androidx.lifecycle.viewmodel.viewModelFactory {
            initializer {
                NotesViewModel(noteRepository, currentUserId)
            }
        }
    )

    val chatViewModel: ChatViewModel = viewModel()

    var selectedTab by remember { mutableIntStateOf(0) }
    var activeNoteForEdit by remember { mutableStateOf<Note?>(null) }
    var isCreatingNote by remember { mutableStateOf(false) }
    var defaultCreateCategory by remember { mutableStateOf(NoteCategory.BUSINESS.name) }

    if (activeNoteForEdit != null || isCreatingNote) {
        NoteDetailEditScreen(
            initialNote = activeNoteForEdit,
            defaultCategory = defaultCreateCategory,
            viewModel = notesViewModel,
            onNavigateBack = {
                activeNoteForEdit = null
                isCreatingNote = false
            }
        )
    } else {
        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = SurfaceDark,
                    tonalElevation = 8.dp,
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                        .border(1.dp, BorderDark, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                ) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.BusinessCenter,
                                contentDescription = "Notes",
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                "Notes",
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF1E1B00),
                            selectedTextColor = GoldLight,
                            indicatorColor = GoldPrimary,
                            unselectedIconColor = Color(0xFF94A3B8),
                            unselectedTextColor = Color(0xFF94A3B8)
                        ),
                        modifier = Modifier.testTag("tab_notes")
                    )

                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "PrivaAI Chat",
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                "PrivaAI",
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF00363F),
                            selectedTextColor = CyanAccent,
                            indicatorColor = CyanAccent,
                            unselectedIconColor = Color(0xFF94A3B8),
                            unselectedTextColor = Color(0xFF94A3B8)
                        ),
                        modifier = Modifier.testTag("tab_ai_chat")
                    )

                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Vault Settings",
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                "Vault",
                                fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF1E1B00),
                            selectedTextColor = GoldLight,
                            indicatorColor = GoldPrimary,
                            unselectedIconColor = Color(0xFF94A3B8),
                            unselectedTextColor = Color(0xFF94A3B8)
                        ),
                        modifier = Modifier.testTag("tab_vault")
                    )
                }
            },
            containerColor = ObsidianDark
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (selectedTab) {
                    0 -> NotesListScreen(
                        viewModel = notesViewModel,
                        onNoteClick = { note ->
                            activeNoteForEdit = note
                        },
                        onCreateNote = { cat ->
                            defaultCreateCategory = cat
                            activeNoteForEdit = null
                            isCreatingNote = true
                        }
                    )
                    1 -> AIChatScreen(
                        chatViewModel = chatViewModel,
                        onSaveToNote = { draftNote ->
                            activeNoteForEdit = draftNote
                            isCreatingNote = true
                        }
                    )
                    2 -> VaultSettingsScreen(
                        notesViewModel = notesViewModel,
                        onSignOut = onSignOut
                    )
                }
            }
        }
    }
}
