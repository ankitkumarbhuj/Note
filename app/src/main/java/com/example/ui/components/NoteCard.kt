package com.example.ui.components

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Note
import com.example.data.model.NoteCategory
import com.example.ui.theme.BorderDark
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.RosePrivate
import com.example.ui.theme.SurfaceDark
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NoteCard(
    note: Note,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isBusiness = note.category == NoteCategory.BUSINESS.name
    val isPrivate = note.category == NoteCategory.PRIVATE.name

    val categoryColor = when {
        isBusiness -> GoldPrimary
        isPrivate -> RosePrivate
        else -> Color(0xFF64748B)
    }

    val cardAccentColor = try {
        Color(android.graphics.Color.parseColor(note.colorHex))
    } catch (_: Exception) {
        SurfaceDark
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceDark
        ),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(
                width = if (note.isPinned) 1.5.dp else 1.dp,
                color = if (note.isPinned) GoldPrimary.copy(alpha = 0.8f) else BorderDark,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .testTag("note_card_${note.id}")
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header Row: Category Badge & Actions (Pin, Favorite)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Chip
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(categoryColor.copy(alpha = 0.15f))
                        .border(0.5.dp, categoryColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = when {
                            isBusiness -> Icons.Default.BusinessCenter
                            isPrivate -> Icons.Default.Lock
                            else -> Icons.Default.Note
                        },
                        contentDescription = note.category,
                        tint = categoryColor,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = when {
                            isBusiness -> "BUSINESS"
                            isPrivate -> "PRIVATE"
                            else -> "GENERAL"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = categoryColor,
                            letterSpacing = 0.5.sp
                        )
                    )
                }

                // Action Icons (Pin & Favorite)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (note.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (note.isFavorite) RosePrivate else Color(0xFF64748B),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onTogglePin,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (note.isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                            contentDescription = "Pin",
                            tint = if (note.isPinned) GoldPrimary else Color(0xFF64748B),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Note Title
            Text(
                text = note.title.ifBlank { "Untitled Note" },
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (note.content.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF94A3B8)
                    ),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Checklist Progress if present
            if (note.checklist.isNotEmpty()) {
                val completedCount = note.checklist.count { it.startsWith("[x] ") }
                val totalCount = note.checklist.size
                val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckBox,
                        contentDescription = "Tasks",
                        tint = GoldPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$completedCount/$totalCount tasks",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF94A3B8),
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(CircleShape),
                        color = GoldPrimary,
                        trackColor = Color(0xFF334155)
                    )
                }
            }

            // Tags Row
            if (note.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    note.tags.take(4).forEach { tag ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF1E293B))
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "#$tag",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF94A3B8),
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }
            }

            // Footer: Timestamp
            Spacer(modifier = Modifier.height(10.dp))
            val date = (note.updatedAt ?: note.createdAt)?.toDate() ?: Date()
            val formattedDate = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault()).format(date)
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color(0xFF64748B),
                    fontSize = 11.sp
                )
            )
        }
    }
}
