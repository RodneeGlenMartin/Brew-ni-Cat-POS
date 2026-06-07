package com.example.cattasticpos.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.*
import androidx.compose.animation.Crossfade
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cattasticpos.CattasticPosApp
import com.example.cattasticpos.domain.model.AppThemeAccent
import com.example.cattasticpos.ui.dashboard.DashboardScreen
import com.example.cattasticpos.ui.dashboard.DashboardViewModel
import com.example.cattasticpos.ui.history.HistoryScreen
import com.example.cattasticpos.ui.history.HistoryViewModel
import com.example.cattasticpos.ui.inventory.InventoryScreen
import com.example.cattasticpos.ui.inventory.InventoryViewModel
import com.example.cattasticpos.ui.components.PinScreen
import com.example.cattasticpos.ui.config.AppConfigViewModel
import com.example.cattasticpos.ui.config.AppConfigUiState
import com.example.cattasticpos.ui.theme.darkColorSchemeFor
import com.example.cattasticpos.ui.theme.lightColorSchemeFor

@Composable
fun CattasticTheme(
    accent: AppThemeAccent = AppThemeAccent.EMERALD,
    darkTheme: Boolean = androidx.compose.foundation.isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) darkColorSchemeFor(accent) else lightColorSchemeFor(accent)
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val connectGranted = permissions[android.Manifest.permission.BLUETOOTH_CONNECT] ?: false
        val scanGranted = permissions[android.Manifest.permission.BLUETOOTH_SCAN] ?: false
        if (!connectGranted || !scanGranted) {
            android.widget.Toast.makeText(
                this,
                "Bluetooth permissions are required for receipt printing.",
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val permissions = arrayOf(
                android.Manifest.permission.BLUETOOTH_CONNECT,
                android.Manifest.permission.BLUETOOTH_SCAN
            )
            val missingPermissions = permissions.filter {
                androidx.core.content.ContextCompat.checkSelfPermission(this, it) != android.content.pm.PackageManager.PERMISSION_GRANTED
            }
            if (missingPermissions.isNotEmpty()) {
                requestPermissionLauncher.launch(missingPermissions.toTypedArray())
            }
        }

        setContent {
            val app = application as CattasticPosApp
            val appConfig by app.container.appConfigRepository.getAppConfig()
                .collectAsState(initial = null)
            val accent = AppThemeAccent.fromId(appConfig?.themeAccentId)

            CattasticTheme(accent = accent) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var currentScreen by remember { mutableStateOf("dashboard") }
                    
                    val dashboardViewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.Factory)
                    val historyViewModel: HistoryViewModel = viewModel(factory = HistoryViewModel.Factory)
                    val inventoryViewModel: InventoryViewModel = viewModel(factory = InventoryViewModel.Factory)
                    val appConfigViewModel: AppConfigViewModel = viewModel(factory = AppConfigViewModel.Factory)

                    Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
                        when (screen) {
                            "dashboard" -> {
                                DashboardScreen(
                                    viewModel = dashboardViewModel,
                                    onNavigateToHistory = { currentScreen = "pin_history" },
                                    onNavigateToInventory = { currentScreen = "pin_inventory" }
                                )
                            }
                            "pin_history", "pin_inventory" -> {
                                val configState by appConfigViewModel.uiState.collectAsState()
                                when (val state = configState) {
                                    AppConfigUiState.Loading -> {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator()
                                        }
                                    }
                                    is AppConfigUiState.Ready -> {
                                        PinScreen(
                                            expectedPinHash = state.config.pinHash,
                                            onPinSuccess = {
                                                currentScreen = if (screen == "pin_history") "history" else "inventory"
                                            },
                                            onCancel = { currentScreen = "dashboard" }
                                        )
                                    }
                                    is AppConfigUiState.Error -> {
                                        Column(
                                            modifier = Modifier.fillMaxSize().padding(24.dp),
                                            verticalArrangement = Arrangement.Center,
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(state.message, textAlign = TextAlign.Center)
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                                Button(onClick = { appConfigViewModel.loadConfig() }) {
                                                    Text("Retry")
                                                }
                                                OutlinedButton(onClick = { currentScreen = "dashboard" }) {
                                                    Text("Go Back")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            "history" -> {
                                HistoryScreen(
                                    viewModel = historyViewModel,
                                    onNavigateBack = { currentScreen = "dashboard" }
                                )
                            }
                            "inventory" -> {
                                InventoryScreen(
                                    viewModel = inventoryViewModel,
                                    onNavigateBack = { currentScreen = "dashboard" }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
