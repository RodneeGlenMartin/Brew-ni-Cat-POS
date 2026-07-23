package com.example.cattasticpos.data.repository

import com.example.cattasticpos.data.local.dao.ExpenseDao
import com.example.cattasticpos.data.local.entity.ExpenseEntity
import com.example.cattasticpos.domain.model.Expense
import com.example.cattasticpos.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ExpenseRepositoryImpl(
    private val expenseDao: ExpenseDao
) : ExpenseRepository {

    override fun getExpensesForDay(startOfDay: Long, endOfDay: Long): Flow<List<Expense>> {
        return expenseDao.getExpensesForDay(startOfDay, endOfDay).map { list ->
            list.map { entity ->
                Expense(
                    id = entity.id,
                    timestamp = entity.timestamp,
                    description = entity.description,
                    amount = entity.amount,
                    recordedBy = entity.recordedBy
                )
            }
        }
    }

    override fun getTotalExpensesForDay(startOfDay: Long, endOfDay: Long): Flow<Double?> {
        return expenseDao.getTotalExpensesForDay(startOfDay, endOfDay)
    }

    override suspend fun saveExpense(expense: Expense) {
        val entity = ExpenseEntity(
            id = expense.id,
            timestamp = expense.timestamp,
            description = expense.description,
            amount = expense.amount,
            recordedBy = expense.recordedBy
        )
        expenseDao.insertExpense(entity)
    }
}
