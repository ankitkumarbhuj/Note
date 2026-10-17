package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Note
import com.example.data.model.NoteCategory
import com.example.ui.theme.BorderDark
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.ObsidianDark
import com.example.ui.theme.RosePrivate
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.viewmodel.NotesViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NoteDetailEditScreen(
    initialNote: Note?,
    defaultCategory: String,
    viewModel: NotesViewModel,
    onNavigateBack: () -> Unit
) {
    BackHandler { onNavigateBack() }

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var title by remember { mutableStateOf(initialNote?.title ?: "") }
    var content by remember { mutableStateOf(initialNote?.content ?: "") }
    var category by remember { mutableStateOf(initialNote?.category ?: defaultCategory) }
    var isPinned by remember { mutableStateOf(initialNote?.isPinned ?: false) }
    var isFavorite by remember { mutableStateOf(initialNote?.isFavorite ?: false) }
    var colorHex by remember { mutableStateOf(initialNote?.colorHex ?: "#0F172A") }

    val checklist = remember { mutableStateListOf<String>().apply { addAll(initialNote?.checklist ?: emptyList()) } }
    var newChecklistText by remember { mutableStateOf("") }

    val tags = remember { mutableStateListOf<String>().apply { addAll(initialNote?.tags ?: emptyList()) } }
    var newTagText by remember { mutableStateOf("") }

    var showAiSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val aiProcessing by viewModel.aiProcessing.collectAsStateWithLifecycle()
    val aiResult by viewModel.aiResult.collectAsStateWithLifecycle()

    var isSaving by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (initialNote == null) "New Note" else "Edit Note",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    // AI Assistant trigger
                    IconButton(
                        onClick = { showAiSheet = true },
                        modifier = Modifier.testTag("ai_tools_button")
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "AI Tools", tint = CyanAccent)
                    }

                    // Pin toggle
                    IconButton(onClick = { isPinned = !isPinned }) {
                        Icon(
                            imageVector = if (isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                            contentDescription = "Pin",
                            tint = if (isPinned) GoldPrimary else Color(0xFF94A3B8)
                        )
                    }

                    // Favorite toggle
                    IconButton(onClick = { isFavorite = !isFavorite }) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) RosePrivate else Color(0xFF94A3B8)
                        )
                    }

                    // Delete (if existing note)
                    if (initialNote != null && initialNote.id.isNotBlank()) {
                        IconButton(onClick = {
                            viewModel.deleteNote(initialNote.id) {
                                onNavigateBack()
                            }
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444))
                        }
                    }

                    // Save Button
                    IconButton(
                        onClick = {
                            if (title.isBlank() && content.isBlank()) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Note cannot be completely empty")
                                }
                                return@IconButton
                            }
                            isSaving = true
                            val toSave = Note(
                                id = initialNote?.id ?: "",
                                title = title.trim(),
                                content = content.trim(),
                                category = category,
                                isPinned = isPinned,
                                isFavorite = isFavorite,
                                colorHex = colorHex,
                                tags = tags.toList(),
                                checklist = checklist.toList(),
                                createdAt = initialNote?.createdAt,
                                updatedAt = null
                            )
                            viewModel.saveNote(toSave) { success ->
                                isSaving = false
                                if (success) {
                                    onNavigateBack()
                                }
                            }
                        },
                        enabled = !isSaving,
                        modifier = Modifier.testTag("save_note_button")
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = GoldPrimary, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Check, contentDescription = "Save Note", tint = GoldPrimary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ObsidianDark)
            )
        },
        containerColor = ObsidianDark
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            // Category Selector Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CategoryChip(
                    label = "Business 💼",
                    selected = category == NoteCategory.BUSINESS.name,
                    color = GoldPrimary,
                    onClick = { category = NoteCategory.BUSINESS.name }
                )
                CategoryChip(
                    label = "Private 🔒",
                    selected = category == NoteCategory.PRIVATE.name,
                    color = RosePrivate,
                    onClick = { category = NoteCategory.PRIVATE.name }
                )
                CategoryChip(
                    label = "General 📄",
                    selected = category == NoteCategory.GENERAL.name,
                    color = Color(0xFF64748B),
                    onClick = { category = NoteCategory.GENERAL.name }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Note Title
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = {
                    Text(
                        text = if (category == NoteCategory.BUSINESS.name) "Business Memo Title..." else "Private Note Title...",
                        style = MaterialTheme.typography.titleLarge.copy(color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                    )
                },
                singleLine = true,
                textStyle = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("note_title_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Note Body / Content
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                placeholder = {
                    Text(
                        text = "Start writing your confidential thoughts, meeting specs, or strategic action plans...",
                        style = MaterialTheme.typography.bodyLarge.copy(color = Color(0xFF64748B))
                    )
                },
                minLines = 8,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color(0xFFE2E8F0)),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SurfaceDark.copy(alpha = 0.5f),
                    unfocusedContainerColor = SurfaceDark.copy(alpha = 0.3f),
                    focusedBorderColor = BorderDark,
                    unfocusedBorderColor = Color.Transparent
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("note_content_input")
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Action Items / Checklist Section
            Text(
                text = "ACTION ITEMS & CHECKLIST",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = GoldLight, letterSpacing = 1.sp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Existing checklist items
            checklist.forEachIndexed { index, item ->
                val isChecked = item.startsWith("[x] ")
                val cleanText = item.removePrefix("[x] ").removePrefix("[ ] ")

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    IconButton(
                        onClick = {
                            val toggled = if (isChecked) "[ ] $cleanText" else "[x] $cleanText"
                            checklist[index] = toggled
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isChecked) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                            contentDescription = "Toggle Checklist",
                            tint = if (isChecked) GoldPrimary else Color(0xFF64748B),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        text = cleanText,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = if (isChecked) Color(0xFF64748B) else Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = { checklist.removeAt(index) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Remove item", tint = Color(0xFF64748B), modifier = Modifier.size(16.dp))
                    }
                }
            }

            // Add checklist item field
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
            ) {
                OutlinedTextField(
                    value = newChecklistText,
                    onValueChange = { newChecklistText = it },
                    placeholder = { Text("Add task item...", color = Color(0xFF64748B), fontSize = 14.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SurfaceDark,
                        unfocusedContainerColor = SurfaceDark,
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = BorderDark,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (newChecklistText.isNotBlank()) {
                            checklist.add("[ ] ${newChecklistText.trim()}")
                            newChecklistText = ""
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(GoldPrimary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add task", tint = Color(0xFF1E1B00))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Tags Section
            Text(
                text = "TAGS & CATEGORIZATION",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = GoldLight, letterSpacing = 1.sp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tags.forEach { tag ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceDark)
                            .border(1.dp, BorderDark, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("#$tag", color = Color(0xFF94A3B8), style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove tag",
                            tint = Color(0xFF64748B),
                            modifier = Modifier
                                .size(14.dp)
                                .clickable { tags.remove(tag) }
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                OutlinedTextField(
                    value = newTagText,
                    onValueChange = { newTagText = it.filter { ch -> !ch.isWhitespace() && ch != '#' } },
                    placeholder = { Text("New tag (e.g. Q4, Finance, Secret)", color = Color(0xFF64748B), fontSize = 14.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SurfaceDark,
                        unfocusedContainerColor = SurfaceDark,
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = BorderDark,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (newTagText.isNotBlank() && tags.size < 10) {
                            tags.add(newTagText.trim())
                            newTagText = ""
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariantDark, contentColor = CyanAccent)
                ) {
                    Text("+ Tag")
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }

        // Gemini AI Tools Bottom Sheet
        if (showAiSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showAiSheet = false
                    viewModel.clearAiResult()
                },
                sheetState = sheetState,
                containerColor = SurfaceDark,
                contentColor = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                        .padding(bottom = 36.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = CyanAccent)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "PrivaAI Intelligence Tools",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = GoldLight)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (aiProcessing) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = CyanAccent)
                                Spacer(modifier = Modifier.height(14.dp))
                                Text("PrivaAI is analyzing and crafting...", color = Color(0xFF94A3B8), fontSize = 13.sp)
                            }
                        }
                    } else if (aiResult != null) {
                        // Display AI result with action options
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF0F172A),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyanAccent.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "AI Result",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = CyanAccent)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = aiResult ?: "",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    content = aiResult ?: ""
                                    showAiSheet = false
                                    viewModel.clearAiResult()
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color(0xFF1E1B00)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Replace Body", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    content = content + "\n\n--- AI Analysis ---\n" + (aiResult ?: "")
                                    showAiSheet = false
                                    viewModel.clearAiResult()
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariantDark, contentColor = Color.White),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Append", fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    val lines = (aiResult ?: "").lines().filter { it.isNotBlank() }
                                    for (line in lines) {
                                        val clean = line.removePrefix("- ").removePrefix("* ").removePrefix("• ")
                                        checklist.add("[ ] $clean")
                                    }
                                    showAiSheet = false
                                    viewModel.clearAiResult()
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7), contentColor = Color.White),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Add Tasks", fontSize = 12.sp)
                            }
                        }
                    } else {
                        // AI Tool Action Buttons
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            AiToolCard(
                                icon = Icons.Default.TrendingUp,
                                iconColor = GoldPrimary,
                                title = "Executive Polish",
                                description = "Refine tone, enhance clarity and impact for C-suite presentation",
                                onClick = {
                                    viewModel.executeAiTool(
                                        toolPrompt = "Rewrite this note in crisp, authoritative executive language. Improve clarity, structure, and impact.",
                                        noteContent = content
                                    )
                                }
                            )

                            AiToolCard(
                                icon = Icons.Default.FormatListBulleted,
                                iconColor = CyanAccent,
                                title = "Extract Action Items",
                                description = "Identify deliverables and generate checklist items",
                                onClick = {
                                    viewModel.executeAiTool(
                                        toolPrompt = "Extract all clear, actionable tasks and next steps from this note as a numbered list of tasks.",
                                        noteContent = content
                                    )
                                }
                            )

                            AiToolCard(
                                icon = Icons.Default.BusinessCenter,
                                iconColor = Color(0xFF10B981),
                                title = "Executive Summary",
                                description = "Generate a concise 3-bullet summary with key decisions",
                                onClick = {
                                    viewModel.executeAiTool(
                                        toolPrompt = "Provide a high-impact, 3-bullet executive summary focusing on core takeaways and key decisions.",
                                        noteContent = content
                                    )
                                }
                            )

                            AiToolCard(
                                icon = Icons.Default.Translate,
                                iconColor = Color(0xFFA855F7),
                                title = "Translate (Hindi / English)",
                                description = "Translate content between Hindi and English seamlessly",
                                onClick = {
                                    viewModel.executeAiTool(
                                        toolPrompt = "Translate the content clearly into Hindi (or English if the note is already in Hindi).",
                                        noteContent = content
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryChip(
    label: String,
    selected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    color = if (selected) color else Color(0xFF94A3B8)
                )
            )
        },
        shape = RoundedCornerShape(12.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = color.copy(alpha = 0.15f),
            containerColor = SurfaceDark
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = BorderDark,
            selectedBorderColor = color
        )
    )
}

@Composable
private fun AiToolCard(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF1E293B),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = iconColor, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color.White))
                Text(text = description, style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8)))
            }
        }
    }
}
