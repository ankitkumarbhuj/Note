package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.auth.signOut
import com.example.data.model.NoteCategory
import com.example.ui.components.SetVaultPinDialog
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
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@Composable
fun VaultSettingsScreen(
    notesViewModel: NotesViewModel,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    val user = Firebase.auth.currentUser
    val userProfile by notesViewModel.userProfile.collectAsStateWithLifecycle()
    val isVaultUnlocked by notesViewModel.isVaultUnlocked.collectAsStateWithLifecycle()
    val notesState by notesViewModel.notesState.collectAsStateWithLifecycle()

    var showSetPinDialog by remember { mutableStateOf(false) }

    if (showSetPinDialog) {
        SetVaultPinDialog(
            onDismiss = { showSetPinDialog = false },
            onPinConfigured = { pin ->
                notesViewModel.setVaultPin(pin) {
                    showSetPinDialog = false
                }
            }
        )
    }

    val allNotes = (notesState as? UiState.Success)?.data ?: emptyList()
    val businessCount = allNotes.count { it.category == NoteCategory.BUSINESS.name }
    val privateCount = allNotes.count { it.category == NoteCategory.PRIVATE.name }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianDark)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
            .padding(bottom = 80.dp)
    ) {
        Text(
            text = "Vault & Settings",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = GoldLight
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        // User Account Card
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = SurfaceDark,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp)
            ) {
                // User Avatar initial
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(GoldPrimary.copy(alpha = 0.2f))
                        .border(1.5.dp, GoldPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    val initial = user?.displayName?.firstOrNull()?.uppercase()
                        ?: user?.email?.firstOrNull()?.uppercase()
                        ?: "P"
                    Text(
                        text = initial,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = GoldLight
                        )
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user?.displayName ?: "Executive User",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = user?.email ?: "Signed in via Google",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF94A3B8)
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CloudDone,
                            contentDescription = "Cloud Synced",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Firestore Cloud Sync Active",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF10B981),
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Private Vault Security Section
        Text(
            text = "VAULT SECURITY",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = GoldLight,
                letterSpacing = 1.sp
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = SurfaceDark,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(RosePrivate.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isVaultUnlocked) Icons.Default.LockOpen else Icons.Default.Lock,
                                contentDescription = "Lock",
                                tint = RosePrivate,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Vault Status",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = if (userProfile?.vaultPin.isNullOrBlank()) {
                                    "No PIN configured yet"
                                } else if (isVaultUnlocked) {
                                    "Unlocked for this session"
                                } else {
                                    "Locked & Encrypted"
                                },
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (isVaultUnlocked) CyanAccent else RosePrivate
                                )
                            )
                        }
                    }

                    if (!userProfile?.vaultPin.isNullOrBlank() && isVaultUnlocked) {
                        Button(
                            onClick = { notesViewModel.lockVault() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SurfaceVariantDark,
                                contentColor = RosePrivate
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Lock Now", fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { showSetPinDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldPrimary,
                        contentColor = Color(0xFF1E1B00)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Password, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (userProfile?.vaultPin.isNullOrBlank()) "Set 4-Digit Vault PIN" else "Change Vault PIN",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Notes Statistics Card
        Text(
            text = "STORAGE OVERVIEW",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = GoldLight,
                letterSpacing = 1.sp
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = SurfaceDark,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatItem(label = "Total Notes", count = allNotes.size, color = GoldLight)
                StatItem(label = "Business 💼", count = businessCount, color = GoldPrimary)
                StatItem(label = "Private 🔒", count = privateCount, color = RosePrivate)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Sign Out Button
        Button(
            onClick = {
                signOut(
                    context = context,
                    credentialManager = credentialManager,
                    onSignOutComplete = onSignOut,
                    scope = coroutineScope
                )
            },
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = SurfaceVariantDark,
                contentColor = Color(0xFFEF4444)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("sign_out_button")
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Sign Out", modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sign Out", fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "0 Note v1.0 • Enterprise Cloud Edition",
            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF64748B)),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun StatItem(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = color
            )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                color = Color(0xFF94A3B8)
            )
        )
    }
}
