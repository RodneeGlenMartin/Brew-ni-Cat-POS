package com.example.cattasticpos.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
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

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFFF7043),       // Cozy Peach Orange
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFCCBC),
    onPrimaryContainer = Color(0xFF5D1300),
    secondary = Color(0xFF8D6E63),     // Warm Coffee Brown
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD7CCC8),
    onSecondaryContainer = Color(0xFF3E2723),
    tertiary = Color(0xFFFFB74D),      // Honey Gold
    onTertiary = Color(0xFFFFFFFF),
    background = Color(0xFFFAF8F5),    // Milk Cream Soft White
    onBackground = Color(0xFF1C1B1A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1C1B1A),
    surfaceVariant = Color(0xFFF5EFEA),
    onSurfaceVariant = Color(0xFF4E463F),
    outline = Color(0xFF80756C),
    outlineVariant = Color(0xFFD2C4B9),
    error = Color(0xFFD32F2F),
    onError = Color(0xFFFFFFFF)
)

@Composable
fun CattasticTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

                    when (currentScreen) {
                        "dashboard" -> {
                            DashboardScreen(
                                viewModel = dashboardViewModel,
                                onNavigateToHistory = { currentScreen = "history" },
                                onNavigateToInventory = { currentScreen = "inventory" }
                            )
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
