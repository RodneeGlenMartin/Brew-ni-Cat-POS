package com.example.cattasticpos.ui.config

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.cattasticpos.CattasticPosApp
import com.example.cattasticpos.domain.model.AppConfig
import com.example.cattasticpos.domain.repository.AppConfigRepository
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

sealed interface AppConfigUiState {
    data object Loading : AppConfigUiState
    data class Ready(val config: AppConfig) : AppConfigUiState
    data class Error(val message: String) : AppConfigUiState
}

class AppConfigViewModel(
    private val appConfigRepository: AppConfigRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AppConfigUiState>(AppConfigUiState.Loading)
    val uiState: StateFlow<AppConfigUiState> = _uiState.asStateFlow()

    init {
        loadConfig()
    }

    fun loadConfig() {
        viewModelScope.launch {
            _uiState.value = AppConfigUiState.Loading
            try {
                withTimeout(10_000) {
                    val config = appConfigRepository.getAppConfig().first { it != null }
                    _uiState.value = AppConfigUiState.Ready(config!!)
                }
            } catch (_: TimeoutCancellationException) {
                _uiState.value = AppConfigUiState.Error("Settings timed out. Tap retry or go back.")
            } catch (e: Exception) {
                _uiState.value = AppConfigUiState.Error(e.message ?: "Failed to load settings.")
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as CattasticPosApp
                return AppConfigViewModel(application.container.appConfigRepository) as T
            }
        }
    }
}
