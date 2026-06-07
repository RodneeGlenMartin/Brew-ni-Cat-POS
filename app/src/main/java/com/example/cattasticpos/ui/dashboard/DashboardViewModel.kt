package com.example.cattasticpos.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.cattasticpos.CattasticPosApp
import com.example.cattasticpos.domain.model.CartItem
import com.example.cattasticpos.domain.model.CartKey
import com.example.cattasticpos.domain.model.Item
import com.example.cattasticpos.domain.model.Variant
import com.example.cattasticpos.domain.strategy.DiscountStrategy
import com.example.cattasticpos.domain.strategy.NoDiscountStrategy
import java.util.Locale
import com.example.cattasticpos.domain.usecase.CalculateCartUseCase
import com.example.cattasticpos.domain.usecase.CheckoutUseCase
import com.example.cattasticpos.domain.usecase.GetMenuUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import com.example.cattasticpos.domain.repository.AppConfigRepository
import com.example.cattasticpos.domain.repository.ExpenseRepository
import com.example.cattasticpos.domain.repository.InventoryRepository
import com.example.cattasticpos.domain.repository.RecipeRepository
import com.example.cattasticpos.domain.service.ReceiptPrinterService
import com.example.cattasticpos.domain.model.Expense
import java.util.UUID

class DashboardViewModel(
    private val getMenuUseCase: GetMenuUseCase,
    private val calculateCartUseCase: CalculateCartUseCase,
    private val checkoutUseCase: CheckoutUseCase,
    private val expenseRepository: ExpenseRepository,
    private val inventoryRepository: InventoryRepository,
    private val receiptPrinterService: ReceiptPrinterService,
    private val recipeRepository: RecipeRepository,
    private val appConfigRepository: AppConfigRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private var allItems: List<Item> = emptyList()

    init {
        viewModelScope.launch {
            inventoryRepository.getAllInventory().collect { invList ->
                _uiState.update { it.copy(inventory = invList) }
            }
        }
        viewModelScope.launch {
            recipeRepository.getAllMappings().collect { mappings ->
                _uiState.update { it.copy(recipeMappings = mappings) }
            }
        }
        viewModelScope.launch {
            getMenuUseCase().collect { menuResult ->
                val categories = menuResult.categories
                allItems = menuResult.items
                
                _uiState.update { state ->
                    val defaultCatId = state.selectedCategoryId.ifBlank {
                        categories.firstOrNull()?.id ?: ""
                    }
                    state.copy(
                        categories = categories,
                        selectedCategoryId = defaultCatId,
                        menuItems = filterItemsByCategoryId(allItems, defaultCatId)
                    )
                }
            }
        }
        viewModelScope.launch {
            appConfigRepository.getAppConfig().collect { config ->
                val cashiers = config?.cashiers.orEmpty()
                val gcashAccounts = config?.gcashAccounts.orEmpty()
                _uiState.update { state ->
                    val activeFromConfig = config?.activeCashierId
                    val selected = when {
                        !activeFromConfig.isNullOrBlank() && cashiers.any { it.id == activeFromConfig } -> activeFromConfig
                        state.selectedCashierId.isNotBlank() && cashiers.any { it.id == state.selectedCashierId } -> state.selectedCashierId
                        else -> cashiers.firstOrNull()?.id ?: "cashier_default"
                    }
                    val defaultGcash = gcashAccounts.firstOrNull()?.label.orEmpty()
                    val paymentState = if (state.showPaymentDialog) {
                        val currentAccount = state.paymentDialogState.receivingAccount
                        val validAccount = gcashAccounts.any { it.label == currentAccount }
                        state.paymentDialogState.copy(
                            receivingAccount = when {
                                currentAccount.isNotBlank() && validAccount -> currentAccount
                                defaultGcash.isNotBlank() -> defaultGcash
                                else -> currentAccount
                            }
                        )
                    } else {
                        state.paymentDialogState
                    }
                    state.copy(
                        cashiers = cashiers,
                        gcashAccounts = gcashAccounts,
                        selectedCashierId = if (cashiers.any { it.id == selected }) {
                            selected
                        } else {
                            cashiers.firstOrNull()?.id ?: "cashier_default"
                        },
                        paymentDialogState = paymentState
                    )
                }
            }
        }
    }

    private fun filterItemsByCategoryId(items: List<Item>, categoryId: String): List<Item> {
        return if (categoryId.isBlank()) items else items.filter { it.categoryId == categoryId }
    }

    fun selectCategory(categoryId: String) {
        _uiState.update { state ->
            state.copy(
                selectedCategoryId = categoryId,
                menuItems = filterItemsByCategoryId(allItems, categoryId)
            )
        }
    }

    fun showConfigurationSheet(item: Item) {
        _uiState.update { state ->
            state.copy(selectedConfiguringItem = item)
        }
    }

    fun hideConfigurationSheet() {
        _uiState.update { state ->
            state.copy(selectedConfiguringItem = null)
        }
    }

    fun addToCart(variant: Variant, flavor: String?) {
        val currentItem = _uiState.value.selectedConfiguringItem ?: return
        
        _uiState.update { state ->
            val cartKey = CartKey.from(currentItem, variant, flavor)
            val existingIndex = state.activeCart.indexOfFirst { it.key == cartKey }
            
            val updatedCart = if (existingIndex != -1) {
                state.activeCart.mapIndexed { index, cartItem ->
                    if (index == existingIndex) {
                        cartItem.copy(quantity = cartItem.quantity + 1)
                    } else {
                        cartItem
                    }
                }
            } else {
                state.activeCart + CartItem(
                    key = cartKey,
                    item = currentItem,
                    variant = variant,
                    flavor = flavor,
                    quantity = 1
                )
            }
            
            val calculation = calculateCartUseCase(updatedCart, state.selectedDiscountStrategy)
            state.copy(
                activeCart = updatedCart,
                subtotal = calculation.subtotal,
                discountDeduction = calculation.discountDeduction,
                discountLabel = calculation.discountLabel,
                total = calculation.total,
                selectedConfiguringItem = null,
                snackbarMessage = "${currentItem.name} added to cart!"
            )
        }
    }

    fun clearSnackbarMessage() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun changeQuantity(cartItemId: String, delta: Int) {
        _uiState.update { state ->
            val updatedCart = state.activeCart.mapNotNull { cartItem ->
                if (cartItem.id == cartItemId) {
                    val newQty = cartItem.quantity + delta
                    if (newQty <= 0) null else cartItem.copy(quantity = newQty)
                } else {
                    cartItem
                }
            }
            
            val calculation = calculateCartUseCase(updatedCart, state.selectedDiscountStrategy)
            val cartCleared = updatedCart.isEmpty()
            state.copy(
                activeCart = updatedCart,
                activeTableLabel = if (cartCleared) null else state.activeTableLabel,
                currentQueueId = if (cartCleared) null else state.currentQueueId,
                subtotal = calculation.subtotal,
                discountDeduction = calculation.discountDeduction,
                discountLabel = calculation.discountLabel,
                total = calculation.total
            )
        }
    }

    fun selectDiscount(strategy: DiscountStrategy) {
        _uiState.update { state ->
            val calculation = calculateCartUseCase(state.activeCart, strategy)
            state.copy(
                selectedDiscountStrategy = strategy,
                subtotal = calculation.subtotal,
                discountDeduction = calculation.discountDeduction,
                discountLabel = calculation.discountLabel,
                total = calculation.total
            )
        }
    }

    fun confirmCheckout(paymentMethod: String, paymentReference: String?) {
        val currentCart = _uiState.value.activeCart
        val currentStrategy = _uiState.value.selectedDiscountStrategy
        if (currentCart.isEmpty()) return
        
        viewModelScope.launch {
            val state = _uiState.value
            val result = checkoutUseCase(
                currentCart,
                currentStrategy,
                paymentMethod,
                paymentReference,
                cashierId = state.selectedCashierId,
                tableLabel = state.activeTableLabel
            )
            if (result.isSuccess) {
                result.getOrNull()?.let { order ->
                    val printerResult = receiptPrinterService.printReceipt(order)
                    val message = buildString {
                        append("Order placed successfully!")
                        if (printerResult.isFailure) {
                            append("\nPrinter: ${printerResult.exceptionOrNull()?.message}")
                        }
                    }
                    _uiState.update { state ->
                        val freshCalculation = calculateCartUseCase(emptyList(), state.selectedDiscountStrategy)
                        state.copy(
                            activeCart = emptyList(),
                            currentQueueId = null,
                            selectedDiscountStrategy = NoDiscountStrategy(),
                            subtotal = freshCalculation.subtotal,
                            discountDeduction = freshCalculation.discountDeduction,
                            discountLabel = freshCalculation.discountLabel,
                            total = freshCalculation.total,
                            showPaymentDialog = false,
                            paymentDialogState = PaymentDialogState(),
                            activeTableLabel = null,
                            snackbarMessage = message
                        )
                    }
                }
            } else {
                _uiState.update { state ->
                    state.copy(snackbarMessage = "Checkout failed: ${result.exceptionOrNull()?.message}")
                }
            }
        }
    }

    fun clearCheckoutEvent() {
        // Kept for API compatibility; checkout now uses snackbarMessage only.
    }

    // Removed int queueCounter

    fun holdCurrentOrder(tableLabel: String?) {
        val currentCart = _uiState.value.activeCart
        if (currentCart.isEmpty()) return
        
        _uiState.update { state ->
            val queueId = state.currentQueueId ?: UUID.randomUUID().toString().substring(0, 8).uppercase()
            val label = tableLabel?.trim()?.takeIf { it.isNotBlank() }
            val newQueue = HeldQueue(
                id = queueId,
                timestamp = System.currentTimeMillis(),
                items = currentCart,
                tableLabel = label
            )
            val updatedQueues = state.heldQueues.filter { it.id != queueId } + newQueue
            val freshCalculation = calculateCartUseCase(emptyList(), state.selectedDiscountStrategy)
            state.copy(
                heldQueues = updatedQueues,
                currentQueueId = null,
                activeTableLabel = null,
                activeCart = emptyList(),
                subtotal = freshCalculation.subtotal,
                discountDeduction = freshCalculation.discountDeduction,
                discountLabel = freshCalculation.discountLabel,
                total = freshCalculation.total,
                showHoldOrderDialog = false
            )
        }
    }

    fun setShowHoldOrderDialog(show: Boolean) {
        _uiState.update { it.copy(showHoldOrderDialog = show) }
    }

    fun selectCashier(cashierId: String) {
        _uiState.update { it.copy(selectedCashierId = cashierId) }
        viewModelScope.launch {
            appConfigRepository.updateActiveCashier(cashierId)
        }
    }

    fun resumeOrder(queueId: String) {
        _uiState.update { state ->
            val queueToResume = state.heldQueues.find { it.id == queueId } ?: return@update state
            val resumedCart = queueToResume.items
            val updatedQueues = state.heldQueues.filter { it.id != queueId }
            val calculation = calculateCartUseCase(resumedCart, state.selectedDiscountStrategy)
            state.copy(
                heldQueues = updatedQueues,
                currentQueueId = queueId,
                activeTableLabel = queueToResume.tableLabel,
                activeCart = resumedCart,
                subtotal = calculation.subtotal,
                discountDeduction = calculation.discountDeduction,
                discountLabel = calculation.discountLabel,
                total = calculation.total,
                showQueuesDialog = false
            )
        }
    }

    fun setShowQueuesDialog(show: Boolean) {
        _uiState.update { state ->
            state.copy(showQueuesDialog = show)
        }
    }

    fun setShowPaymentDialog(show: Boolean) {
        _uiState.update { state ->
            val defaultAccount = state.gcashAccounts.firstOrNull()?.label.orEmpty()
            state.copy(
                showPaymentDialog = show,
                paymentDialogState = if (show) {
                    val currentAccount = state.paymentDialogState.receivingAccount
                    state.paymentDialogState.copy(
                        receivingAccount = when {
                            currentAccount.isNotBlank() && state.gcashAccounts.any { it.label == currentAccount } -> currentAccount
                            defaultAccount.isNotBlank() -> defaultAccount
                            else -> currentAccount
                        }
                    )
                } else {
                    PaymentDialogState()
                }
            )
        }
    }

    fun updatePaymentDialogState(update: PaymentDialogState.() -> PaymentDialogState) {
        _uiState.update { state ->
            state.copy(paymentDialogState = state.paymentDialogState.update())
        }
    }

    fun setPaymentDialogState(state: PaymentDialogState) {
        _uiState.update { it.copy(paymentDialogState = state) }
    }

    fun setShowExpenseDialog(show: Boolean) {
        _uiState.update { state ->
            state.copy(showExpenseDialog = show)
        }
    }

    fun saveExpense(description: String, amount: Double, recordedBy: String) {
        viewModelScope.launch {
            val expense = Expense(
                id = UUID.randomUUID().toString(),
                timestamp = System.currentTimeMillis(),
                description = description,
                amount = amount,
                recordedBy = recordedBy
            )
            expenseRepository.saveExpense(expense)
            _uiState.update { state ->
                state.copy(showExpenseDialog = false)
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as CattasticPosApp
                return DashboardViewModel(
                    application.container.getMenuUseCase,
                    application.container.calculateCartUseCase,
                    application.container.checkoutUseCase,
                    application.container.expenseRepository,
                    application.container.inventoryRepository,
                    application.container.receiptPrinterService,
                    application.container.recipeRepository,
                    application.container.appConfigRepository
                ) as T
            }
        }
    }
}
