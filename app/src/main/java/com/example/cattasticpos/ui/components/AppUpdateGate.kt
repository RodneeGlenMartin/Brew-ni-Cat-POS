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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.cattasticpos.domain.model.UpdateInfo
import com.example.cattasticpos.service.AppUpdateManager

private enum class UpdatePhase { Downloading, NeedsPermission, Failed, Launched }

/**
 * Fully automatic self-update. On launch it checks the cloud for a newer build and, if one
 * exists, downloads it and hands it straight to the system installer — no "Update now" tap.
 * The only interactions Android cannot waive are the one-time "allow install from this app"
 * toggle and the OS's final install confirmation; everything else happens on its own.
 */
@Composable
fun AppUpdateGate() {
    val context = LocalContext.current
    val manager = remember { AppUpdateManager(context) }

    var info by remember { mutableStateOf<UpdateInfo?>(null) }
    var phase by remember { mutableStateOf(UpdatePhase.Downloading) }
    var progress by remember { mutableStateOf(0f) }
    var dismissed by remember { mutableStateOf(false) }
    var runKey by remember { mutableStateOf(0) }

    // Check once for an update.
    LaunchedEffect(Unit) {
        info = manager.checkForUpdate()
    }

    // As soon as we have update info (and on each retry), download then launch the installer.
    LaunchedEffect(info, runKey) {
        val u = info ?: return@LaunchedEffect
        phase = UpdatePhase.Downloading
        progress = 0f
        val file = manager.downloadApk(u.apkUrl) { progress = it }
        phase = when {
            file == null -> UpdatePhase.Failed
            manager.installApk(file) -> UpdatePhase.Launched
            else -> UpdatePhase.NeedsPermission
        }
    }

    val u = info
    // Nothing to show until an update is found, after the OS installer takes over, or if dismissed.
    if (u == null || dismissed || phase == UpdatePhase.Launched) return

    AlertDialog(
        onDismissRequest = { /* don't let an outside tap cancel an in-flight update */ },
        title = {
            Text(
                when (phase) {
                    UpdatePhase.NeedsPermission -> "Allow updates"
                    UpdatePhase.Failed -> "Update failed"
                    else -> "Updating app"
                }
            )
        },
        text = {
            Column {
                when (phase) {
                    UpdatePhase.Downloading -> {
                        Text(
                            if (u.versionName.isNotBlank())
                                "Downloading version ${u.versionName}…"
                            else
                                "Downloading the latest version…"
                        )
                        Spacer(Modifier.height(14.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp))
                            Text(
                                "${(progress * 100).toInt()}%",
                                modifier = Modifier.padding(start = 12.dp)
                            )
                        }
                    }
                    UpdatePhase.NeedsPermission -> Text(
                        "Please allow “Install unknown apps” for Brew ni Cat in the screen that just " +
                            "opened, then tap Retry. This is a one-time step."
                    )
                    UpdatePhase.Failed -> Text(
                        "Couldn’t download the update. Check the internet connection and try again."
                    )
                    else -> Text("Preparing update…")
                }
            }
        },
        confirmButton = {
            if (phase == UpdatePhase.NeedsPermission || phase == UpdatePhase.Failed) {
                TextButton(onClick = { runKey++ }) { Text("Retry") }
            }
        },
        dismissButton = {
            if (phase == UpdatePhase.NeedsPermission || phase == UpdatePhase.Failed) {
                TextButton(onClick = { dismissed = true }) { Text("Later") }
            }
        }
    )
}
