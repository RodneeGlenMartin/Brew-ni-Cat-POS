package com.example.cattasticpos.domain.repository

import com.example.cattasticpos.domain.model.Category
import com.example.cattasticpos.domain.model.Item
import kotlinx.coroutines.flow.Flow

interface MenuRepository {
    fun getCategories(): Flow<List<Category>>
    fun getItems(): Flow<List<Item>>
}
