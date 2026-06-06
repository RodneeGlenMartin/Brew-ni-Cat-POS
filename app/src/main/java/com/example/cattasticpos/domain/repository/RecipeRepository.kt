package com.example.cattasticpos.domain.repository

import com.example.cattasticpos.domain.model.RecipeMapping
import kotlinx.coroutines.flow.Flow

interface RecipeRepository {
    fun getAllMappings(): Flow<List<RecipeMapping>>
    fun getMappingsForMenu(menuItemId: String): Flow<List<RecipeMapping>>
    suspend fun getMappingsForCheckout(menuItemId: String, variantName: String?): List<RecipeMapping>
    suspend fun insertMapping(mapping: RecipeMapping)
    suspend fun insertMappings(mappings: List<RecipeMapping>)
    suspend fun deleteMapping(mapping: RecipeMapping)
    suspend fun deleteMappingsForInventory(inventoryItemId: String)
}
