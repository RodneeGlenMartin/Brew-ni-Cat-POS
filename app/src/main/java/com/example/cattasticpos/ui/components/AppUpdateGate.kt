package com.example.cattasticpos.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.cattasticpos.domain.model.UpdateInfo
import com.example.cattasticpos.service.AppUpdateManager
import kotlinx.coroutines.launch

/**
 * Drop-in overlay: on first composition it asks the cloud whether a newer build exists
 * and, if so, shows an install prompt. Silent and invisible when the app is up to date.
 */
@Composable
fun AppUpdateGate() {
    val context = LocalContext.current
    val manager = remember { AppUpdateManager(context) }
    val scope = rememberCoroutineScope()

    var info by remember { mutableStateOf<UpdateInfo?>(null) }
    var dismissed by remember { mutableStateOf(false) }
    var downloading by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        info = manager.checkForUpdate()
    }

    val update = info
    if (update == null || dismissed) return

    AlertDialog(
        onDismissRequest = { if (!update.mandatory && !downloading) dismissed = true },
        title = { Text("Update available") },
        text = {
            Column {
                Text(
                    if (update.versionName.isNotBlank())
                        "Version ${update.versionName} is ready to install."
                    else
                        "A new version is ready to install."
                )
                if (!update.notes.isNullOrBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(update.notes)
                }
                if (downloading) {
                    Spacer(Modifier.height(14.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp))
                        Text(
                            "Downloading… ${(progress * 100).toInt()}%",
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !downloading,
                onClick = {
                    scope.launch {
                        downloading = true
                        progress = 0f
                        val file = manager.downloadApk(update.apkUrl) { progress = it }
                        downloading = false
                        if (file != null) manager.installApk(file)
                    }
                }
            ) { Text(if (downloading) "Downloading…" else "Update now") }
        },
        dismissButton = {
            if (!update.mandatory) {
                TextButton(enabled = !downloading, onClick = { dismissed = true }) {
                    Text("Later")
                }
            }
        }
    )
}
