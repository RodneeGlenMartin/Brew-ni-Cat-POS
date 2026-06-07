package com.example.cattasticpos.ui.config

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.cattasticpos.CattasticPosApp
import com.example.cattasticpos.domain.model.AppConfig
import com.example.cattasticpos.domain.repository.AppConfigRepository
import kotlinx.coroutines.flow.first

class PinGateViewModel(
    private val appConfigRepository: AppConfigRepository
) : ViewModel() {

    suspend fun verifyPin(inputPin: String): Boolean {
        val normalizedPin = inputPin.trim().filter { it.isDigit() }
        if (normalizedPin.length != 4) {
            return false
        }
        val config = appConfigRepository.getAppConfig().first { it != null } ?: return false
        return AppConfig.verifyPin(normalizedPin, config.pinHash)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as CattasticPosApp
                return PinGateViewModel(application.container.appConfigRepository) as T
            }
        }
    }
}
