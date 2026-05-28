package com.example.cattasticpos.ui.dashboard

import com.example.cattasticpos.domain.model.CartItem
import com.example.cattasticpos.domain.model.Category
import com.example.cattasticpos.domain.model.Item
import com.example.cattasticpos.domain.strategy.DiscountStrategy
import com.example.cattasticpos.domain.strategy.NoDiscountStrategy
import com.example.cattasticpos.domain.model.InventoryItem

data class HeldQueue(
    val id: String,
    val timestamp: Long,
    val items: List<CartItem>
)

data class DashboardUiState(
    val categories: List<Category> = emptyList(),
    val menuItems: List<Item> = emptyList(),
    val selectedCategoryId: String = "",
    val activeCart: List<CartItem> = emptyList(),
    val selectedDiscountStrategy: DiscountStrategy = NoDiscountStrategy(),
    val subtotal: Double = 0.0,
    val discountDeduction: Double = 0.0,
    val discountLabel: String = "None",
    val total: Double = 0.0,
    val selectedConfiguringItem: Item? = null,
    val checkoutSuccessEvent: String? = null,
    val snackbarMessage: String? = null,
    val heldQueues: List<HeldQueue> = emptyList(),
    val showQueuesDialog: Boolean = false,
    val currentQueueId: String? = null,
    val showPaymentDialog: Boolean = false,
    val showExpenseDialog: Boolean = false,
    val showInventoryDialog: Boolean = false,
    val inventory: List<InventoryItem> = emptyList()
)
