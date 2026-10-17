package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.Note
import com.example.data.model.UserProfile
import com.example.data.util.OperationType
import com.example.data.util.handleFirestoreError
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID
import kotlin.coroutines.cancellation.CancellationException

class NoteRepository(
    private val db: FirebaseFirestore
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    fun observeNotes(userId: String): Flow<List<Note>> {
        val path = "users/$userId/notes"
        return db.collection("users").document(userId).collection("notes")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(Note::class.java)
            }
    }

    suspend fun getNote(userId: String, noteId: String): Result<Note?> {
        val docRef = db.collection("users").document(userId).collection("notes").document(noteId)
        return try {
            val snapshot = docRef.get().await()
            Result.success(snapshot.toObject(Note::class.java))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun saveNote(userId: String, note: Note): Result<String> {
        val noteId = if (note.id.isNotBlank()) note.id else UUID.randomUUID().toString()
        val docRef = db.collection("users").document(userId).collection("notes").document(noteId)
        return try {
            if (note.id.isBlank()) {
                // New note creation
                val payload = note.copy(
                    id = noteId,
                    userId = userId,
                    createdAt = null,
                    updatedAt = null
                )
                docRef.set(payload).await()
            } else {
                // Existing note update
                requireNotNull(note.createdAt) { "createdAt must not be null for update" }
                val payload = note.copy(
                    userId = userId,
                    updatedAt = null
                )
                docRef.set(payload).await()
            }
            Result.success(noteId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            val op = if (note.id.isBlank()) OperationType.CREATE else OperationType.UPDATE
            handleFirestoreError(e, op, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteNote(userId: String, noteId: String): Result<Unit> {
        val docRef = db.collection("users").document(userId).collection("notes").document(noteId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun getUserProfile(userId: String): Result<UserProfile?> {
        val docRef = db.collection("users").document(userId)
        return try {
            val snapshot = docRef.get().await()
            Result.success(snapshot.toObject(UserProfile::class.java))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun saveUserProfile(userId: String, profile: UserProfile): Result<Unit> {
        val docRef = db.collection("users").document(userId)
        return try {
            val exists = docRef.get().await().exists()
            if (!exists) {
                docRef.set(profile.copy(userId = userId, createdAt = null, updatedAt = null)).await()
            } else {
                docRef.set(profile.copy(userId = userId, updatedAt = null)).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }
}
