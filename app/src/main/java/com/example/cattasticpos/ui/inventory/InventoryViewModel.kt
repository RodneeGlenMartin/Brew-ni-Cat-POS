package com.example.cattasticpos.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.cattasticpos.CattasticPosApp
import com.example.cattasticpos.data.local.dao.RecipeDao
import com.example.cattasticpos.data.local.entity.InventoryEntity
import com.example.cattasticpos.data.local.entity.RecipeMappingEntity
import com.example.cattasticpos.domain.repository.InventoryRepository
import com.example.cattasticpos.domain.usecase.GetMenuUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class InventoryViewModel(
    private val inventoryRepository: InventoryRepository,
    private val recipeDao: RecipeDao,
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
    }

    fun restockItem(itemId: String, amount: Int) {
        viewModelScope.launch {
            if (amount > 0) {
                inventoryRepository.restockItem(itemId, amount)
            }
        }
    }

    fun addNewRawMaterial(name: String, unit: String, startingStock: Int, threshold: Int) {
        viewModelScope.launch {
            val newItem = InventoryEntity(
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
        loadMappings(menuItemId)
    }

    fun selectVariant(variantName: String?) {
        _uiState.update { it.copy(selectedVariantName = variantName) }
    }

    private fun loadMappings(menuItemId: String) {
        viewModelScope.launch {
            recipeDao.getMappingsForMenu(menuItemId).collect { mappings ->
                _uiState.update { it.copy(currentRecipeMappings = mappings) }
            }
        }
    }

    fun linkIngredient(inventoryItemId: String, deductionQuantity: Double) {
        val state = _uiState.value
        val menuItemId = state.selectedMenuItemId ?: return
        val variantName = state.selectedVariantName
        
        viewModelScope.launch {
            val mapping = RecipeMappingEntity(
                id = "r_${UUID.randomUUID()}",
                menuItemId = menuItemId,
                variantName = variantName,
                inventoryItemId = inventoryItemId,
                deductionQuantity = deductionQuantity
            )
            recipeDao.insertMapping(mapping)
        }
    }

    fun removeMapping(mapping: RecipeMappingEntity) {
        viewModelScope.launch {
            recipeDao.deleteMapping(mapping)
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
                    application.container.database.recipeDao(),
                    application.container.getMenuUseCase
                ) as T
            }
        }
    }
}
