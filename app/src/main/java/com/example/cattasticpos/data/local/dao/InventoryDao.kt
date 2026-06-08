package com.example.cattasticpos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.cattasticpos.data.local.entity.InventoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {
    @Query("SELECT * FROM inventory")
    fun getAllInventory(): Flow<List<InventoryEntity>>

    @Query("SELECT COUNT(*) FROM inventory")
    suspend fun getInventoryCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventoryItems(items: List<InventoryEntity>)

    @Update
    suspend fun updateInventoryItem(item: InventoryEntity)

    @Query("UPDATE inventory SET currentStock = currentStock - :amount WHERE id = :inventoryId")
    suspend fun decrementStock(inventoryId: String, amount: Double)

    @Query("UPDATE inventory SET currentStock = currentStock + :addedAmount WHERE id = :itemId")
    suspend fun restockItem(itemId: String, addedAmount: Double)

    @Query("DELETE FROM inventory WHERE id = :itemId")
    suspend fun deleteInventoryItem(itemId: String)
}
