package com.example.data

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.Note
import com.example.data.model.NoteCategory
import com.example.data.repository.NoteRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class NoteRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun createNote_validBusinessNote_succeeds() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = NoteRepository(firestore)

        val note = Note(
            title = "Board Strategy 2026",
            content = "Detailed business expansion plan",
            category = NoteCategory.BUSINESS.name,
            isPinned = true,
            isFavorite = false,
            colorHex = "#0F172A",
            tags = listOf("Strategy", "Q4")
        )

        val result = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.saveNote(uid, note)
        }
        assertTrue(result.isSuccess)
        val noteId = result.getOrThrow()
        assertTrue(noteId.isNotBlank())

        val fetched = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.getNote(uid, noteId)
        }
        assertTrue(fetched.isSuccess)
        val fetchedNote = fetched.getOrThrow()
        assertNotNull(fetchedNote)
        assertEquals("Board Strategy 2026", fetchedNote?.title)
        assertEquals(NoteCategory.BUSINESS.name, fetchedNote?.category)
    }

    @Test
    fun observeNotes_authenticatedUser_emitsNotes() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = NoteRepository(firestore)

        val note = Note(
            title = "Private Vault Note ${UUID.randomUUID()}",
            content = "Encrypted personal reflections",
            category = NoteCategory.PRIVATE.name,
            isPinned = false,
            isFavorite = true,
            colorHex = "#4338CA",
            tags = listOf("Personal")
        )

        val saveResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.saveNote(uid, note)
        }
        val noteId = saveResult.getOrThrow()

        val emitted = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeNotes(uid).first { notes ->
                notes.any { it.id == noteId }
            }
        }
        assertTrue(emitted.any { it.id == noteId })
    }

    @Test
    fun getNote_crossUser_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = NoteRepository(firestore)

        val note = Note(
            title = "Alice Secret",
            content = "Top secret credentials",
            category = NoteCategory.PRIVATE.name
        )
        val noteId = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.saveNote(aliceUid, note).getOrThrow()
        }

        // Sign in as Bob and attempt to read Alice's note
        signInTestUser(BOB_EMAIL)
        val bobRepo = NoteRepository(firestore)

        val result = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobRepo.getNote(aliceUid, noteId)
        }
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception is FirebaseFirestoreException)
        assertEquals(
            FirebaseFirestoreException.Code.PERMISSION_DENIED,
            (exception as FirebaseFirestoreException).code
        )
    }

    @Test
    fun getNote_unauthenticated_failsWithPermissionDenied() = runBlocking {
        // Sign out to test unauthenticated access
        auth.signOut()
        val repository = NoteRepository(firestore)

        val result = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.getNote("alice_uid_123", "note_123")
        }
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception is FirebaseFirestoreException)
        assertEquals(
            FirebaseFirestoreException.Code.PERMISSION_DENIED,
            (exception as FirebaseFirestoreException).code
        )
    }

    private companion object {
        const val ALICE_EMAIL = "alice@privanote.test"
        const val BOB_EMAIL = "bob@privanote.test"
        const val DEFAULT_TIMEOUT_MS = 6000L
        const val FLOW_TIMEOUT_MS = 6000L
    }
}
