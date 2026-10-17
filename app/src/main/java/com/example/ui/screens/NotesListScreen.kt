package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.Note
import com.example.data.model.NoteCategory
import com.example.ui.components.NoteCard
import com.example.ui.components.UnlockVaultDialog
import com.example.ui.theme.BorderDark
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.ObsidianDark
import com.example.ui.theme.RosePrivate
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.viewmodel.NotesViewModel
import com.example.ui.viewmodel.UiState

@Composable
fun NotesListScreen(
    viewModel: NotesViewModel,
    onNoteClick: (Note) -> Unit,
    onCreateNote: (String) -> Unit
) {
    val notesState by viewModel.notesState.collectAsStateWithLifecycle()
    val filteredNotes by viewModel.filteredNotes.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()
    val isVaultUnlocked by viewModel.isVaultUnlocked.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()

    var showUnlockDialog by remember { mutableStateOf(false) }

    if (showUnlockDialog) {
        UnlockVaultDialog(
            onDismiss = { showUnlockDialog = false },
            onVerifyPin = { pin -> viewModel.unlockVault(pin) },
            onUnlockSuccess = { showUnlockDialog = false }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianDark)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top App Bar / Brand Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .border(1.dp, GoldPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_app_logo),
                            contentDescription = "0 Note",
                            modifier = Modifier.size(34.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "0 Note",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = GoldLight
                            )
                        )
                        Text(
                            text = if (isVaultUnlocked) "Vault Unlocked" else "Vault Secured",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isVaultUnlocked) CyanAccent else RosePrivate,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }

                // Vault Lock/Unlock Toggle Icon
                IconButton(
                    onClick = {
                        if (isVaultUnlocked) {
                            viewModel.lockVault()
                        } else {
                            showUnlockDialog = true
                        }
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SurfaceVariantDark)
                        .border(1.dp, BorderDark, CircleShape)
                        .testTag("vault_toggle_button")
                ) {
                    Icon(
                        imageVector = if (isVaultUnlocked) Icons.Default.LockOpen else Icons.Default.Lock,
                        contentDescription = "Vault Toggle",
                        tint = if (isVaultUnlocked) CyanAccent else RosePrivate,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = {
                    Text("Search memos, strategy, tags...", color = Color(0xFF64748B))
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFF94A3B8))
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search", tint = Color(0xFF94A3B8))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SurfaceDark,
                    unfocusedContainerColor = SurfaceDark,
                    focusedBorderColor = GoldPrimary,
                    unfocusedBorderColor = BorderDark,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .testTag("notes_search_input")
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Filter Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf(
                    "ALL" to "All Notes",
                    "BUSINESS" to "Business 💼",
                    "PRIVATE" to "Private 🔒",
                    "PINNED" to "Pinned 📌",
                    "FAVORITE" to "Favorites ⭐"
                )

                items(filters) { (key, label) ->
                    val isSelected = selectedFilter == key
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (key == "PRIVATE" && !isVaultUnlocked && !userProfile?.vaultPin.isNullOrBlank()) {
                                showUnlockDialog = true
                            }
                            viewModel.setSelectedFilter(key)
                        },
                        label = {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) GoldLight else Color(0xFF94A3B8)
                                )
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GoldPrimary.copy(alpha = 0.2f),
                            selectedLabelColor = GoldLight,
                            containerColor = SurfaceDark
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = BorderDark,
                            selectedBorderColor = GoldPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Content Area
            when (val state = notesState) {
                is UiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = GoldPrimary)
                    }
                }
                is UiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Error: ${state.message}",
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                is UiState.Success -> {
                    // Check if private category is selected but vault is locked
                    if (selectedFilter == "PRIVATE" && !isVaultUnlocked && !userProfile?.vaultPin.isNullOrBlank()) {
                        PrivateVaultLockedCard(
                            onUnlockClick = { showUnlockDialog = true }
                        )
                    } else if (filteredNotes.isEmpty()) {
                        EmptyNotesView(
                            searchQuery = searchQuery,
                            selectedFilter = selectedFilter,
                            onCreateNote = { onCreateNote(NoteCategory.BUSINESS.name) }
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 96.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filteredNotes, key = { it.id }) { note ->
                                NoteCard(
                                    note = note,
                                    onClick = {
                                        if (note.category == NoteCategory.PRIVATE.name && !isVaultUnlocked && !userProfile?.vaultPin.isNullOrBlank()) {
                                            showUnlockDialog = true
                                        } else {
                                            onNoteClick(note)
                                        }
                                    },
                                    onTogglePin = { viewModel.togglePin(note) },
                                    onToggleFavorite = { viewModel.toggleFavorite(note) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = {
                val defaultCategory = when (selectedFilter) {
                    "PRIVATE" -> NoteCategory.PRIVATE.name
                    else -> NoteCategory.BUSINESS.name
                }
                onCreateNote(defaultCategory)
            },
            containerColor = GoldPrimary,
            contentColor = Color(0xFF1E1B00),
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("create_note_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Create Note", modifier = Modifier.size(28.dp))
        }
    }
}

@Composable
private fun PrivateVaultLockedCard(
    onUnlockClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = SurfaceDark,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(RosePrivate.copy(alpha = 0.15f))
                        .border(1.dp, RosePrivate, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Vault Locked",
                        tint = RosePrivate,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Private Vault is Locked",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Your private notes are encrypted and shielded behind your 4-digit PIN.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF94A3B8)),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onUnlockClick,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RosePrivate,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Unlock Vault with PIN", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun EmptyNotesView(
    searchQuery: String,
    selectedFilter: String,
    onCreateNote: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Color(0xFF1E293B)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.BusinessCenter,
                contentDescription = "No Notes",
                tint = GoldPrimary,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = if (searchQuery.isNotEmpty()) "No matching notes found" else "No notes in $selectedFilter",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = if (searchQuery.isNotEmpty()) "Try different keywords or tags." else "Capture business decisions, meeting memos, or private ideas now.",
            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8)),
            textAlign = TextAlign.Center
        )

        if (searchQuery.isEmpty()) {
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onCreateNote,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldPrimary,
                    contentColor = Color(0xFF1E1B00)
                )
            ) {
                Text("Create Note", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
