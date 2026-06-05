package com.example.cattasticpos.data.repository

import com.example.cattasticpos.data.local.dao.RecipeDao
import com.example.cattasticpos.data.local.entity.RecipeMappingEntity
import com.example.cattasticpos.domain.model.RecipeMapping
import com.example.cattasticpos.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RecipeRepositoryImpl(
    private val recipeDao: RecipeDao
) : RecipeRepository {

    private fun RecipeMappingEntity.toDomain(): RecipeMapping {
        return RecipeMapping(
            id = id,
            menuItemId = menuItemId,
            variantName = variantName,
            inventoryItemId = inventoryItemId,
            deductionQuantity = deductionQuantity
        )
    }

    private fun RecipeMapping.toEntity(): RecipeMappingEntity {
        return RecipeMappingEntity(
            id = id,
            menuItemId = menuItemId,
            variantName = variantName,
            inventoryItemId = inventoryItemId,
            deductionQuantity = deductionQuantity
        )
    }

    override fun getAllMappings(): Flow<List<RecipeMapping>> {
        return recipeDao.getAllMappings().map { list -> list.map { it.toDomain() } }
    }

    override fun getMappingsForMenu(menuItemId: String): Flow<List<RecipeMapping>> {
        return recipeDao.getMappingsForMenu(menuItemId).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getMappingsForCheckout(menuItemId: String, variantName: String?): List<RecipeMapping> {
        return recipeDao.getMappingsForCheckout(menuItemId, variantName).map { it.toDomain() }
    }

    override suspend fun insertMapping(mapping: RecipeMapping) {
        recipeDao.insertMapping(mapping.toEntity())
    }

    override suspend fun insertMappings(mappings: List<RecipeMapping>) {
        recipeDao.insertMappings(mappings.map { it.toEntity() })
    }

    override suspend fun deleteMapping(mapping: RecipeMapping) {
        recipeDao.deleteMapping(mapping.toEntity())
    }
}
