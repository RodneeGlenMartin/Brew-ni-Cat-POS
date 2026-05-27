package com.example.cattasticpos.domain.repository

import com.example.cattasticpos.domain.model.Expense
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {
    fun getExpensesForDay(startOfDay: Long, endOfDay: Long): Flow<List<Expense>>
    fun getTotalExpensesForDay(startOfDay: Long, endOfDay: Long): Flow<Double?>
    suspend fun saveExpense(expense: Expense)
}
