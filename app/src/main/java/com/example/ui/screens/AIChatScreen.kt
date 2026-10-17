package com.example.ui.screens

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ai.ChatMessage
import com.example.data.model.Note
import com.example.data.model.NoteCategory
import com.example.ui.theme.BorderDark
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.ObsidianDark
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.viewmodel.ChatViewModel

@Composable
fun AIChatScreen(
    chatViewModel: ChatViewModel,
    onSaveToNote: (Note) -> Unit
) {
    val messages by chatViewModel.messages.collectAsStateWithLifecycle()
    val isLoading by chatViewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by chatViewModel.errorMessage.collectAsStateWithLifecycle()

    var inputMessage by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val suggestionChips = listOf(
        "Draft a business memo for Q4 OKRs",
        "Structure confidential meeting minutes",
        "5 strategic client retention ideas",
        "Private daily reflection template"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianDark)
            .imePadding()
    ) {
        // Chat Header
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
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(CyanAccent.copy(alpha = 0.15f))
                        .border(1.dp, CyanAccent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "PrivaAI",
                        tint = CyanAccent,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "PrivaAI Assistant",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = GoldLight
                        )
                    )
                    Text(
                        text = "Executive & Confidential Advisor",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF94A3B8)
                        )
                    )
                }
            }

            IconButton(
                onClick = { chatViewModel.clearChat() },
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(SurfaceVariantDark)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = "Clear Chat",
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Suggestion Chips (horizontal scroll)
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            items(suggestionChips) { suggestion ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SurfaceDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                    modifier = Modifier.clickable {
                        chatViewModel.sendMessage(suggestion)
                    }
                ) {
                    Text(
                        text = suggestion,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFFE2E8F0)
                        ),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }

        // Messages Thread
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(messages, key = { it.id }) { msg ->
                ChatBubble(
                    message = msg,
                    onSaveAsNote = {
                        val newNote = Note(
                            title = "AI Draft • " + msg.content.take(30) + "...",
                            content = msg.content,
                            category = NoteCategory.BUSINESS.name,
                            tags = listOf("AI-Draft", "PrivaAI")
                        )
                        onSaveToNote(newNote)
                    }
                )
            }

            if (isLoading) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 8.dp, top = 6.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = CyanAccent,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "PrivaAI is reasoning...",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }

            if (errorMessage != null) {
                item {
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
        }

        // Message Input Field
        Surface(
            color = SurfaceDark,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                OutlinedTextField(
                    value = inputMessage,
                    onValueChange = { inputMessage = it },
                    placeholder = {
                        Text("Ask PrivaAI to draft, analyze, or plan...", color = Color(0xFF64748B), fontSize = 14.sp)
                    },
                    maxLines = 4,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF0F172A),
                        unfocusedContainerColor = Color(0xFF0F172A),
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = BorderDark,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ai_chat_input")
                )

                Spacer(modifier = Modifier.width(10.dp))

                IconButton(
                    onClick = {
                        if (inputMessage.isNotBlank()) {
                            val textToSend = inputMessage
                            inputMessage = ""
                            chatViewModel.sendMessage(textToSend)
                        }
                    },
                    enabled = inputMessage.isNotBlank() && !isLoading,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (inputMessage.isNotBlank()) GoldPrimary else Color(0xFF1E293B))
                        .testTag("ai_chat_send_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send Message",
                        tint = if (inputMessage.isNotBlank()) Color(0xFF1E1B00) else Color(0xFF64748B),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(
    message: ChatMessage,
    onSaveAsNote: () -> Unit
) {
    val isUser = message.role == "user"

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 4.dp)
        ) {
            Text(
                text = if (isUser) "You" else "PrivaAI",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isUser) GoldPrimary else CyanAccent,
                    fontSize = 11.sp
                )
            )
        }

        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            color = if (isUser) Color(0xFF1E293B) else SurfaceDark,
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = if (isUser) Color(0xFF334155) else BorderDark
            ),
            modifier = Modifier.fillMaxWidth(if (isUser) 0.85f else 0.95f)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White,
                        lineHeight = 22.sp
                    )
                )

                // Option to save AI response directly to notes
                if (!isUser) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF0F172A),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, GoldPrimary.copy(alpha = 0.5f)),
                            modifier = Modifier.clickable(onClick = onSaveAsNote)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BookmarkAdd,
                                    contentDescription = "Save to Note",
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Save as Note",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = GoldLight,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
