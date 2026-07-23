package com.example.cattasticpos.domain.usecase

import com.example.cattasticpos.domain.model.Category
import com.example.cattasticpos.domain.model.Item
import com.example.cattasticpos.domain.repository.MenuRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetMenuUseCase(
    private val menuRepository: MenuRepository
) {
    operator fun invoke(): Flow<MenuResult> {
        return combine(
            menuRepository.getCategories(),
            menuRepository.getItems()
        ) { categories, items ->
            MenuResult(categories, items)
        }
    }
}

data class MenuResult(
    val categories: List<Category>,
    val items: List<Item>
)
