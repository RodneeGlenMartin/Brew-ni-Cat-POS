package com.example.cattasticpos

import android.app.Application
import com.example.cattasticpos.data.local.PosDatabase
import com.example.cattasticpos.data.repository.MenuRepositoryImpl
import com.example.cattasticpos.data.repository.OrderRepositoryImpl
import com.example.cattasticpos.domain.repository.MenuRepository
import com.example.cattasticpos.domain.repository.OrderRepository
import com.example.cattasticpos.domain.repository.ExpenseRepository
import com.example.cattasticpos.data.repository.ExpenseRepositoryImpl
import com.example.cattasticpos.domain.repository.InventoryRepository
import com.example.cattasticpos.data.repository.InventoryRepositoryImpl
import com.example.cattasticpos.domain.usecase.CalculateCartUseCase
import com.example.cattasticpos.domain.usecase.CheckoutUseCase
import com.example.cattasticpos.domain.usecase.RestockItemUseCase
import com.example.cattasticpos.domain.usecase.GetMenuUseCase
import com.example.cattasticpos.domain.usecase.ExportDataUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class CattasticPosApp : Application() {
    
    // Global scope for application tasks like database seeding
    private val applicationScope = CoroutineScope(SupervisorJob())

    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainerImpl(this, applicationScope)
    }
}

interface AppContainer {
    val database: PosDatabase
    val menuRepository: MenuRepository
    val orderRepository: OrderRepository
    val getMenuUseCase: GetMenuUseCase
    val calculateCartUseCase: CalculateCartUseCase
    val checkoutUseCase: CheckoutUseCase
    val exportDataUseCase: ExportDataUseCase
    val expenseRepository: ExpenseRepository
    val inventoryRepository: InventoryRepository
    val restockItemUseCase: RestockItemUseCase
}

class AppContainerImpl(
    private val context: android.content.Context,
    private val scope: CoroutineScope
) : AppContainer {

    override val database: PosDatabase by lazy {
        PosDatabase.getDatabase(context, scope)
    }

    override val menuRepository: MenuRepository by lazy {
        MenuRepositoryImpl(database.menuDao())
    }

    override val orderRepository: OrderRepository by lazy {
        OrderRepositoryImpl(database.orderDao())
    }

    override val getMenuUseCase: GetMenuUseCase by lazy {
        GetMenuUseCase(menuRepository)
    }

    override val calculateCartUseCase: CalculateCartUseCase by lazy {
        CalculateCartUseCase()
    }

    override val checkoutUseCase: CheckoutUseCase by lazy {
        CheckoutUseCase(orderRepository, database.inventoryDao(), database.recipeDao(), calculateCartUseCase)
    }

    override val expenseRepository: ExpenseRepository by lazy {
        ExpenseRepositoryImpl(database.expenseDao())
    }

    override val inventoryRepository: InventoryRepository by lazy {
        InventoryRepositoryImpl(database.inventoryDao())
    }

    override val exportDataUseCase: ExportDataUseCase by lazy {
        ExportDataUseCase(context)
    }

    override val restockItemUseCase: RestockItemUseCase by lazy {
        RestockItemUseCase(inventoryRepository)
    }
}
