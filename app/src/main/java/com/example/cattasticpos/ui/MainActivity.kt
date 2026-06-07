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
import androidx.compose.foundation.isSystemInDarkTheme
import com.example.cattasticpos.ui.adaptive.AdaptiveTheme
import com.example.cattasticpos.ui.adaptive.BionicHaptic
import com.example.cattasticpos.ui.adaptive.ParallaxNavHost
import com.example.cattasticpos.ui.adaptive.isPushNavigation
import com.example.cattasticpos.ui.adaptive.rememberBionicHaptic
import com.example.cattasticpos.ui.theme.AdaptiveSystemBarStyle

@Composable
fun CattasticTheme(
    accent: AppThemeAccent = AppThemeAccent.EMERALD,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    AdaptiveSystemBarStyle(darkTheme = darkTheme)
    AdaptiveTheme(accent = accent, darkTheme = darkTheme, content = content)
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
                    var isPushing by remember { mutableStateOf(true) }
                    val performHaptic = rememberBionicHaptic()

                    fun navigateTo(target: String) {
                        isPushing = isPushNavigation(currentScreen, target)
                        if (isPushing) {
                            performHaptic(BionicHaptic.Light)
                        }
                        currentScreen = target
                    }
                    
                    val dashboardViewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.Factory)
                    val historyViewModel: HistoryViewModel = viewModel(factory = HistoryViewModel.Factory)
                    val inventoryViewModel: InventoryViewModel = viewModel(factory = InventoryViewModel.Factory)

                    ParallaxNavHost(
                        targetScreen = currentScreen,
                        isPushing = isPushing
                    ) { screen ->
                        when (screen) {
                            "dashboard" -> {
                                DashboardScreen(
                                    viewModel = dashboardViewModel,
                                    onNavigateToHistory = { navigateTo("pin_history") },
                                    onNavigateToInventory = { navigateTo("pin_inventory") }
                                )
                            }
                            "pin_history", "pin_inventory" -> {
                                PinScreen(
                                    onPinSuccess = {
                                        navigateTo(if (screen == "pin_history") "history" else "inventory")
                                    },
                                    onCancel = { navigateTo("dashboard") }
                                )
                            }
                            "history" -> {
                                HistoryScreen(
                                    viewModel = historyViewModel,
                                    onNavigateBack = { navigateTo("dashboard") }
                                )
                            }
                            "inventory" -> {
                                InventoryScreen(
                                    viewModel = inventoryViewModel,
                                    onNavigateBack = { navigateTo("dashboard") }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
