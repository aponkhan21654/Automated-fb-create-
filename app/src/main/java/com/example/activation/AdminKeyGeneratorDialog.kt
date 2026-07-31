package com.example.activation

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AdminKeyGeneratorDialog(
    onDismiss: () -> Unit,
    onActivationSuccess: () -> Unit
) {
    val context = LocalContext.current
    var pinEntered by remember { mutableStateOf("") }
    var isPinVerified by remember { mutableStateOf(false) }

    var targetDeviceId by remember { mutableStateOf(ActivationManager.getDeviceId(context)) }
    var selectedDays by remember { mutableStateOf(30) }
    var generatedKey by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = if (isPinVerified) "Admin License Generator 🔑" else "Admin Access Required 🔒")
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (!isPinVerified) {
                    Text(
                        text = "Enter Admin Security PIN:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = pinEntered,
                        onValueChange = { pinEntered = it },
                        label = { Text("Admin PIN") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text(
                        text = "Generate activation key for any device:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = targetDeviceId,
                        onValueChange = { targetDeviceId = it },
                        label = { Text("Client Device ID") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "Select Duration:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(7, 30, 90, 365, 9999).forEach { days ->
                            val label = if (days == 9999) "Life" else "${days}d"
                            val isSelected = selectedDays == days
                            Button(
                                onClick = { selectedDays = days },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                            ) {
                                Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (targetDeviceId.isNotBlank()) {
                                    generatedKey = ActivationManager.generateActivationKey(targetDeviceId, selectedDays)
                                } else {
                                    Toast.makeText(context, "Please enter Device ID!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Key, contentDescription = null, modifier = Modifier.height(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Activation Key", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                if (targetDeviceId.isNotBlank()) {
                                    generatedKey = ActivationManager.generateBanKey(targetDeviceId)
                                } else {
                                    Toast.makeText(context, "Please enter Device ID!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.height(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Ban Key 🚫", fontSize = 12.sp)
                        }
                    }

                    if (generatedKey.isNotBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Generated Code:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = generatedKey,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            clipboard.setPrimaryClip(ClipData.newPlainText("Activation Key", generatedKey))
                                            Toast.makeText(context, "Key copied to clipboard! 📋", Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.height(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Copy Key", fontSize = 12.sp)
                                    }

                                    Button(
                                        onClick = {
                                            val success = ActivationManager.activateWithKey(context, generatedKey)
                                            if (success) {
                                                Toast.makeText(context, "Device Activated Successfully! 🎉", Toast.LENGTH_SHORT).show()
                                                onActivationSuccess()
                                                onDismiss()
                                            } else {
                                                Toast.makeText(context, "Failed to activate device with key.", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.height(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Activate This Device", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (!isPinVerified) {
                Button(
                    onClick = {
                        if (pinEntered.trim() == "5630" || pinEntered.trim() == "1234") {
                            isPinVerified = true
                        } else {
                            Toast.makeText(context, "Invalid PIN!", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Unlock Admin")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
