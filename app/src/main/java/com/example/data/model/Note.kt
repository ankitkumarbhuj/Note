package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp

enum class NoteCategory {
    BUSINESS,
    PRIVATE,
    GENERAL
}

@IgnoreExtraProperties
data class Note(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val content: String = "",
    val category: String = NoteCategory.BUSINESS.name,
    @get:PropertyName("isPinned")
    val isPinned: Boolean = false,
    @get:PropertyName("isFavorite")
    val isFavorite: Boolean = false,
    val colorHex: String = "#0F172A",
    val tags: List<String> = emptyList(),
    val checklist: List<String> = emptyList(),
    @ServerTimestamp
    val createdAt: Timestamp? = null,
    @ServerTimestamp
    val updatedAt: Timestamp? = null
)
