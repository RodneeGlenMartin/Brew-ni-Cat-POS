package com.example.cattasticpos.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.cattasticpos.data.local.entity.RecipeMappingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Query("SELECT * FROM recipe_mappings")
    fun getAllMappings(): Flow<List<RecipeMappingEntity>>

    @Query("SELECT * FROM recipe_mappings WHERE menuItemId = :menuItemId")
    fun getMappingsForMenu(menuItemId: String): Flow<List<RecipeMappingEntity>>

    @Query("SELECT * FROM recipe_mappings WHERE menuItemId = :menuItemId AND (variantName = :variantName OR variantName IS NULL)")
    suspend fun getMappingsForCheckout(menuItemId: String, variantName: String?): List<RecipeMappingEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMapping(mapping: RecipeMappingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMappings(mappings: List<RecipeMappingEntity>)

    @Delete
    suspend fun deleteMapping(mapping: RecipeMappingEntity)
}
