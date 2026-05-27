package com.example.cattasticpos.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.cattasticpos.CattasticPosApp
import com.example.cattasticpos.domain.model.Order
import com.example.cattasticpos.domain.repository.OrderRepository
import com.example.cattasticpos.domain.repository.ExpenseRepository
import com.example.cattasticpos.domain.model.Expense
import com.example.cattasticpos.domain.usecase.ExportDataUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class HistoryViewModel(
    private val orderRepository: OrderRepository,
    private val expenseRepository: ExpenseRepository,
    private val exportDataUseCase: ExportDataUseCase
) : ViewModel() {

    private val todayStart: Long
    private val todayEnd: Long

    init {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        todayStart = calendar.timeInMillis
        
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        todayEnd = calendar.timeInMillis
    }

    val ordersState: StateFlow<List<Order>> = orderRepository.getOrdersWithItems()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val grossSalesState: StateFlow<Double?> = orderRepository.getGrossSalesForDay(todayStart, todayEnd)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val discountsState: StateFlow<Double?> = orderRepository.getDiscountsGivenForDay(todayStart, todayEnd)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val netRevenueState: StateFlow<Double?> = orderRepository.getNetRevenueForDay(todayStart, todayEnd)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val cashSalesState: StateFlow<Double?> = orderRepository.getCashSalesForDay(todayStart, todayEnd)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val gcashSalesState: StateFlow<Double?> = orderRepository.getGcashSalesForDay(todayStart, todayEnd)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val topSellingItemState: StateFlow<Pair<String, Int>?> = orderRepository.getTopSellingItemForDay(todayStart, todayEnd)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val expensesListState: StateFlow<List<Expense>> = expenseRepository.getExpensesForDay(todayStart, todayEnd)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalExpensesState: StateFlow<Double?> = expenseRepository.getTotalExpensesForDay(todayStart, todayEnd)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun deleteOrder(orderId: String) {
        viewModelScope.launch {
            orderRepository.deleteOrder(orderId)
        }
    }

    private val _exportMessage = MutableStateFlow<String?>(null)
    val exportMessage: StateFlow<String?> = _exportMessage.asStateFlow()

    fun exportData() {
        viewModelScope.launch {
            val orders = ordersState.value
            val expenses = expensesListState.value
            val result = exportDataUseCase(orders, expenses)
            if (result.isSuccess) {
                _exportMessage.value = "Exported to Downloads: ${result.getOrNull()}"
            } else {
                _exportMessage.value = "Export Failed: ${result.exceptionOrNull()?.message}"
            }
        }
    }
    
    fun clearExportMessage() {
        _exportMessage.value = null
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as CattasticPosApp
                return HistoryViewModel(
                    application.container.orderRepository,
                    application.container.expenseRepository,
                    application.container.exportDataUseCase
                ) as T
            }
        }
    }
}
