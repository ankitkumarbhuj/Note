package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.RosePrivate
import com.example.ui.theme.SurfaceDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnlockVaultDialog(
    onDismiss: () -> Unit,
    onVerifyPin: (String) -> Boolean,
    onUnlockSuccess: () -> Unit
) {
    var enteredPin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    BasicAlertDialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = SurfaceDark,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(RosePrivate.copy(alpha = 0.15f))
                        .border(1.dp, RosePrivate, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Vault Locked",
                        tint = RosePrivate,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Private Vault Locked",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )

                Text(
                    text = "Enter your 4-digit PIN to access private notes",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF94A3B8)
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // PIN Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until 4) {
                        val isFilled = i < enteredPin.length
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isError -> RosePrivate
                                        isFilled -> GoldPrimary
                                        else -> Color(0xFF334155)
                                    }
                                )
                        )
                    }
                }

                if (isError) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Incorrect PIN. Please try again.",
                        color = RosePrivate,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Numeric Keypad
                NumericKeypad(
                    onNumberClick = { num ->
                        if (enteredPin.length < 4) {
                            isError = false
                            val newPin = enteredPin + num
                            enteredPin = newPin
                            if (newPin.length == 4) {
                                val success = onVerifyPin(newPin)
                                if (success) {
                                    onUnlockSuccess()
                                } else {
                                    isError = true
                                    enteredPin = ""
                                }
                            }
                        }
                    },
                    onBackspace = {
                        if (enteredPin.isNotEmpty()) {
                            enteredPin = enteredPin.dropLast(1)
                            isError = false
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetVaultPinDialog(
    onDismiss: () -> Unit,
    onPinConfigured: (String) -> Unit
) {
    var step by remember { mutableStateOf(1) } // 1: Enter, 2: Confirm
    var firstPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    val currentInput = if (step == 1) firstPin else confirmPin

    BasicAlertDialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = SurfaceDark,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(GoldPrimary.copy(alpha = 0.15f))
                        .border(1.dp, GoldPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Security PIN",
                        tint = GoldPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (step == 1) "Create Vault PIN" else "Confirm Vault PIN",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )

                Text(
                    text = if (step == 1) "Choose a 4-digit code to protect private notes" else "Re-enter the 4-digit code to confirm",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8)),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // PIN Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until 4) {
                        val isFilled = i < currentInput.length
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isFilled) GoldPrimary else Color(0xFF334155)
                                )
                        )
                    }
                }

                if (errorText != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = errorText ?: "",
                        color = RosePrivate,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                NumericKeypad(
                    onNumberClick = { num ->
                        errorText = null
                        if (step == 1) {
                            if (firstPin.length < 4) {
                                val next = firstPin + num
                                firstPin = next
                                if (next.length == 4) {
                                    step = 2
                                }
                            }
                        } else {
                            if (confirmPin.length < 4) {
                                val next = confirmPin + num
                                confirmPin = next
                                if (next.length == 4) {
                                    if (next == firstPin) {
                                        onPinConfigured(next)
                                    } else {
                                        errorText = "PINs do not match. Try again."
                                        step = 1
                                        firstPin = ""
                                        confirmPin = ""
                                    }
                                }
                            }
                        }
                    },
                    onBackspace = {
                        errorText = null
                        if (step == 1 && firstPin.isNotEmpty()) {
                            firstPin = firstPin.dropLast(1)
                        } else if (step == 2 && confirmPin.isNotEmpty()) {
                            confirmPin = confirmPin.dropLast(1)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            }
        }
    }
}

@Composable
private fun NumericKeypad(
    onNumberClick: (String) -> Unit,
    onBackspace: () -> Unit
) {
    val keys = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("", "0", "DEL")
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        for (row in keys) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (key in row) {
                    when (key) {
                        "" -> {
                            Spacer(modifier = Modifier.size(60.dp))
                        }
                        "DEL" -> {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .clickable(onClick = onBackspace),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Backspace,
                                    contentDescription = "Backspace",
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        else -> {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1E293B))
                                    .clickable { onNumberClick(key) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = key,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White,
                                        fontSize = 22.sp
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
