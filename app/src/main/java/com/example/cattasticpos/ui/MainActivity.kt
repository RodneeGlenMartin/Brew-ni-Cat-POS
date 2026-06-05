package com.example.cattasticpos.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import androidx.compose.material3.*
import androidx.compose.animation.Crossfade
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cattasticpos.ui.dashboard.DashboardScreen
import com.example.cattasticpos.ui.dashboard.DashboardViewModel
import com.example.cattasticpos.ui.history.HistoryScreen
import com.example.cattasticpos.ui.history.HistoryViewModel
import com.example.cattasticpos.ui.inventory.InventoryScreen
import com.example.cattasticpos.ui.inventory.InventoryViewModel
import com.example.cattasticpos.ui.components.PinScreen

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF3E5C49),       // Forest Green
    onPrimary = Color(0xFFF5F1E3),     // Warm Cream
    primaryContainer = Color(0xFFD3A63B), // Golden Mustard
    onPrimaryContainer = Color(0xFF3B2821), // Espresso Brown
    secondary = Color(0xFFE08538),     // Cat Orange
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD7CCC8),
    onSecondaryContainer = Color(0xFF3B2821),
    tertiary = Color(0xFF42A5F5),      // Soft Blue for GCash
    onTertiary = Color(0xFFFFFFFF),
    background = Color(0xFFF5F1E3),    // Warm Cream
    onBackground = Color(0xFF3B2821),  // Espresso Brown
    surface = Color(0xFFFFFFFF),       // White Cards
    onSurface = Color(0xFF3B2821),     // Espresso Brown
    surfaceVariant = Color(0xFFF5EFEA),
    onSurfaceVariant = Color(0xFF3B2821),
    outline = Color(0xFF80756C),
    outlineVariant = Color(0xFFD2C4B9),
    error = Color(0xFFD32F2F),
    onError = Color(0xFFFFFFFF)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF3E5C49),       // Forest Green
    onPrimary = Color(0xFFF5F1E3),     // Warm Cream
    primaryContainer = Color(0xFFD3A63B), // Golden Mustard
    onPrimaryContainer = Color(0xFF3B2821), // Espresso Brown
    secondary = Color(0xFFE08538),     // Cat Orange
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFF5D4037),
    onSecondaryContainer = Color(0xFFD7CCC8),
    tertiary = Color(0xFF64B5F6),      // Soft Blue for GCash
    onTertiary = Color(0xFF003258),
    background = Color(0xFF241E1C),    // Dark, warm brown-gray
    onBackground = Color(0xFFF5F1E3),  // Warm Cream
    surface = Color(0xFF302A28),       // Lighter warm gray/brown cards
    onSurface = Color(0xFFF5F1E3),     // Warm Cream
    surfaceVariant = Color(0xFF3A3330),
    onSurfaceVariant = Color(0xFFF5F1E3),
    outline = Color(0xFF9E9E9E),
    outlineVariant = Color(0xFF424242),
    error = Color(0xFFCF6679),
    onError = Color(0xFF000000)
)

@Composable
fun CattasticTheme(
    darkTheme: Boolean = androidx.compose.foundation.isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
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
            CattasticTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var currentScreen by remember { mutableStateOf("dashboard") }
                    
                    val dashboardViewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.Factory)
                    val historyViewModel: HistoryViewModel = viewModel(factory = HistoryViewModel.Factory)
                    val inventoryViewModel: InventoryViewModel = viewModel(factory = InventoryViewModel.Factory)

                    Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
                        when (screen) {
                            "dashboard" -> {
                                DashboardScreen(
                                    viewModel = dashboardViewModel,
                                    onNavigateToHistory = { currentScreen = "pin_history" },
                                    onNavigateToInventory = { currentScreen = "pin_inventory" }
                                )
                            }
                            "pin_history" -> {
                                val config = historyViewModel.appConfigState.collectAsState().value
                                if (config == null) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                                } else {
                                    PinScreen(
                                        expectedPinHash = config.pinHash,
                                        onPinSuccess = { currentScreen = "history" },
                                        onCancel = { currentScreen = "dashboard" }
                                    )
                                }
                            }
                            "pin_inventory" -> {
                                val config = historyViewModel.appConfigState.collectAsState().value
                                if (config == null) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                                } else {
                                    PinScreen(
                                        expectedPinHash = config.pinHash,
                                        onPinSuccess = { currentScreen = "inventory" },
                                        onCancel = { currentScreen = "dashboard" }
                                    )
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
