package com.example.cattasticpos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.cattasticpos.data.local.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenses(expenses: List<ExpenseEntity>)

    @Query("SELECT * FROM expenses")
    suspend fun getAllExpensesOnce(): List<ExpenseEntity>

    @Query("SELECT * FROM expenses WHERE timestamp >= :startOfDay AND timestamp <= :endOfDay ORDER BY timestamp DESC")
    fun getExpensesForDay(startOfDay: Long, endOfDay: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT SUM(amount) FROM expenses WHERE timestamp >= :startOfDay AND timestamp <= :endOfDay")
    fun getTotalExpensesForDay(startOfDay: Long, endOfDay: Long): Flow<Double?>
}
