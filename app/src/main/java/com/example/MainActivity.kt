package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.auth.attemptAutoSignIn
import com.example.auth.authStateFlow
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.MainScreen
import com.example.ui.theme.ObsidianDark
import com.example.ui.theme.PrivaNoteTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            PrivaNoteTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = ObsidianDark
                ) {
                    AppContent()
                }
            }
        }
    }
}

@Composable
fun AppContent() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    val currentUser by Firebase.auth.authStateFlow()
        .collectAsStateWithLifecycle(initialValue = Firebase.auth.currentUser)

    // Attempt silent auto-sign-in on initial startup
    LaunchedEffect(Unit) {
        attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = { },
            onUnauthenticated = { },
            scope = coroutineScope
        )
    }

    val user = currentUser
    if (user == null) {
        AuthScreen(
            onAuthSuccess = {
                // authStateFlow will automatically update currentUser
            }
        )
    } else {
        val databaseId = context.getString(R.string.firestore_database_id)
        MainScreen(
            currentUserId = user.uid,
            databaseId = databaseId,
            onSignOut = {
                // authStateFlow will transition to AuthScreen
            }
        )
    }
}
