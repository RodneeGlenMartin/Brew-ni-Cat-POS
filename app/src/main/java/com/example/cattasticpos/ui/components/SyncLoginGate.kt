package com.example.cattasticpos.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.cattasticpos.CattasticPosApp
import com.example.cattasticpos.worker.SyncWorker
import kotlinx.coroutines.launch

/**
 * One-time cloud sign-in for this device. The database rejects unauthenticated
 * traffic, so until the store account is signed in nothing syncs (orders and
 * expenses still record locally). Shown on launch while signed out; the store
 * owner enters the shared POS account once and the session persists.
 */
@Composable
fun SyncLoginGate() {
    val context = LocalContext.current
    val app = context.applicationContext as CattasticPosApp
    val authManager = remember { app.container.supabaseAuthManager }
    val scope = rememberCoroutineScope()

    var signedIn by remember { mutableStateOf(authManager.isSignedIn()) }
    var dismissed by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    if (signedIn || dismissed) return

    AlertDialog(
        onDismissRequest = { /* explicit buttons only */ },
        title = { Text("Cloud sync sign-in") },
        text = {
            Column {
                Text(
                    "This device needs the store account before orders and expenses " +
                        "can sync. Selling works offline in the meantime.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(14.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    enabled = !busy,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    singleLine = true,
                    enabled = !busy,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )
                val e = error
                if (e != null) {
                    Spacer(Modifier.height(10.dp))
                    Text(e, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                if (busy) {
                    Spacer(Modifier.height(12.dp))
                    CircularProgressIndicator(modifier = Modifier.height(20.dp))
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !busy && email.isNotBlank() && password.isNotBlank(),
                onClick = {
                    busy = true
                    error = null
                    scope.launch {
                        val result = authManager.signIn(email, password)
                        busy = false
                        if (result == null) {
                            signedIn = true
                            // Push everything recorded while signed out.
                            SyncWorker.triggerImmediateSync(context)
                        } else {
                            error = result
                        }
                    }
                }
            ) { Text("Sign in") }
        },
        dismissButton = {
            TextButton(enabled = !busy, onClick = { dismissed = true }) { Text("Later") }
        }
    )
}
