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
import com.example.cattasticpos.domain.repository.AppConfigRepository
import com.example.cattasticpos.domain.model.AppConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import java.util.Calendar

class HistoryViewModel(
    private val orderRepository: OrderRepository,
    private val expenseRepository: ExpenseRepository,
    private val exportDataUseCase: ExportDataUseCase,
    private val appConfigRepository: AppConfigRepository
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

    private val _startDate = MutableStateFlow<Long>(0L)
    private val _endDate = MutableStateFlow<Long>(Long.MAX_VALUE)
    private val _limit = MutableStateFlow<Int>(50)

    @OptIn(ExperimentalCoroutinesApi::class)
    val ordersState: StateFlow<List<Order>> = combine(_startDate, _endDate, _limit) { start, end, limit -> 
        Triple(start, end, limit) 
    }.flatMapLatest { (start, end, limit) ->
        orderRepository.getOrdersWithItems(start, end, limit, 0)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun loadMoreOrders() {
        _limit.value += 50
    }

    fun setDateRange(start: Long?, end: Long?) {
        _startDate.value = start ?: 0L
        _endDate.value = end ?: Long.MAX_VALUE
        _limit.value = 50
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val grossSalesState: StateFlow<Double?> = combine(_startDate, _endDate) { start, end ->
        if (start == 0L && end == Long.MAX_VALUE) todayStart to todayEnd else start to end
    }.flatMapLatest { (start, end) -> orderRepository.getGrossSalesForDay(start, end) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val discountsState: StateFlow<Double?> = combine(_startDate, _endDate) { start, end ->
        if (start == 0L && end == Long.MAX_VALUE) todayStart to todayEnd else start to end
    }.flatMapLatest { (start, end) -> orderRepository.getDiscountsGivenForDay(start, end) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val netRevenueState: StateFlow<Double?> = combine(_startDate, _endDate) { start, end ->
        if (start == 0L && end == Long.MAX_VALUE) todayStart to todayEnd else start to end
    }.flatMapLatest { (start, end) -> orderRepository.getNetRevenueForDay(start, end) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val cashSalesState: StateFlow<Double?> = combine(_startDate, _endDate) { start, end ->
        if (start == 0L && end == Long.MAX_VALUE) todayStart to todayEnd else start to end
    }.flatMapLatest { (start, end) -> orderRepository.getCashSalesForDay(start, end) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val gcashSalesState: StateFlow<Double?> = combine(_startDate, _endDate) { start, end ->
        if (start == 0L && end == Long.MAX_VALUE) todayStart to todayEnd else start to end
    }.flatMapLatest { (start, end) -> orderRepository.getGcashSalesForDay(start, end) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val topSellingItemState: StateFlow<Pair<String, Int>?> = combine(_startDate, _endDate) { start, end ->
        if (start == 0L && end == Long.MAX_VALUE) todayStart to todayEnd else start to end
    }.flatMapLatest { (start, end) -> orderRepository.getTopSellingItemForDay(start, end) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val expensesListState: StateFlow<List<Expense>> = combine(_startDate, _endDate) { start, end ->
        if (start == 0L && end == Long.MAX_VALUE) todayStart to todayEnd else start to end
    }.flatMapLatest { (start, end) -> expenseRepository.getExpensesForDay(start, end) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val totalExpensesState: StateFlow<Double?> = combine(_startDate, _endDate) { start, end ->
        if (start == 0L && end == Long.MAX_VALUE) todayStart to todayEnd else start to end
    }.flatMapLatest { (start, end) -> expenseRepository.getTotalExpensesForDay(start, end) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val appConfigState: StateFlow<AppConfig?> = appConfigRepository.getAppConfig()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun updateConfig(targetSales: Double, startingCashFloat: Double, pinHash: String) {
        viewModelScope.launch {
            appConfigRepository.updateConfig(targetSales, startingCashFloat, pinHash)
        }
    }

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
                    application.container.exportDataUseCase,
                    application.container.appConfigRepository
                ) as T
            }
        }
    }
}
