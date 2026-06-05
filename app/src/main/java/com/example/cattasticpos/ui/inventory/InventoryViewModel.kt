package com.example.cattasticpos.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.cattasticpos.CattasticPosApp
import com.example.cattasticpos.domain.model.RecipeMapping
import com.example.cattasticpos.domain.repository.RecipeRepository
import com.example.cattasticpos.domain.repository.InventoryRepository
import com.example.cattasticpos.domain.usecase.GetMenuUseCase
import com.example.cattasticpos.domain.model.InventoryItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import java.util.UUID

class InventoryViewModel(
    private val inventoryRepository: InventoryRepository,
    private val recipeRepository: RecipeRepository,
    private val getMenuUseCase: GetMenuUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(InventoryUiState())
    val uiState: StateFlow<InventoryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            inventoryRepository.getAllInventory().collect { items ->
                _uiState.update { it.copy(inventoryItems = items) }
            }
        }
        viewModelScope.launch {
            getMenuUseCase().collect { menuResult ->
                _uiState.update { it.copy(menuItems = menuResult.items) }
            }
        }
        @OptIn(ExperimentalCoroutinesApi::class)
        viewModelScope.launch {
            _uiState.map { it.selectedMenuItemId }
                .distinctUntilChanged()
                .flatMapLatest { id ->
                    if (id != null) recipeRepository.getMappingsForMenu(id)
                    else kotlinx.coroutines.flow.flowOf(emptyList())
                }
                .collect { mappings ->
                    _uiState.update { it.copy(currentRecipeMappings = mappings) }
                }
        }
    }

    fun restockItem(itemId: String, amount: Double) {
        viewModelScope.launch {
            if (amount > 0) {
                inventoryRepository.restockItem(itemId, amount)
            }
        }
    }

    fun addNewRawMaterial(name: String, unit: String, startingStock: Double, threshold: Double) {
        viewModelScope.launch {
            val newItem = InventoryItem(
                id = "inv_${UUID.randomUUID()}",
                itemName = name,
                unit = unit,
                currentStock = startingStock,
                reorderThreshold = threshold
            )
            inventoryRepository.insertInventoryItems(listOf(newItem))
            _uiState.update { it.copy(showAddRawMaterialDialog = false) }
        }
    }

    fun selectMenuItem(menuItemId: String) {
        _uiState.update { it.copy(selectedMenuItemId = menuItemId, selectedVariantName = null) }
    }

    fun selectVariant(variantName: String?) {
        _uiState.update { it.copy(selectedVariantName = variantName) }
    }



    fun linkIngredient(inventoryItemId: String, deductionQuantity: Double) {
        val state = _uiState.value
        val menuItemId = state.selectedMenuItemId ?: return
        val variantName = state.selectedVariantName
        
        viewModelScope.launch {
            val mapping = RecipeMapping(
                id = "r_${UUID.randomUUID()}",
                menuItemId = menuItemId,
                variantName = variantName,
                inventoryItemId = inventoryItemId,
                deductionQuantity = deductionQuantity
            )
            recipeRepository.insertMapping(mapping)
        }
    }

    fun removeMapping(mapping: RecipeMapping) {
        viewModelScope.launch {
            recipeRepository.deleteMapping(mapping)
        }
    }

    fun setShowAddRawMaterialDialog(show: Boolean) {
        _uiState.update { it.copy(showAddRawMaterialDialog = show) }
    }

    fun setShowLinkIngredientDialog(show: Boolean) {
        _uiState.update { it.copy(showLinkIngredientDialog = show) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as CattasticPosApp
                return InventoryViewModel(
                    application.container.inventoryRepository,
                    application.container.recipeRepository,
                    application.container.getMenuUseCase
                ) as T
            }
        }
    }
}
