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
import com.example.cattasticpos.domain.model.ZReadingSummary
import com.example.cattasticpos.domain.service.ReceiptPrinterService
import com.example.cattasticpos.domain.usecase.ExportDataUseCase
import com.example.cattasticpos.domain.usecase.VoidOrderUseCase
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
    private val appConfigRepository: AppConfigRepository,
    private val voidOrderUseCase: VoidOrderUseCase,
    private val receiptPrinterService: ReceiptPrinterService
) : ViewModel() {

    private val todayStart: Long
    private val todayEnd: Long
    private val allTimeStart: Long = 0L
    private val allTimeEnd: Long = Long.MAX_VALUE

    private val _startDate: MutableStateFlow<Long>
    private val _endDate: MutableStateFlow<Long>
    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    private val _showDateRangeDialog = MutableStateFlow(false)
    private val _canLoadMore = MutableStateFlow(false)

    val showDateRangeDialog: StateFlow<Boolean> = _showDateRangeDialog.asStateFlow()
    val canLoadMore: StateFlow<Boolean> = _canLoadMore.asStateFlow()

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

        _startDate = MutableStateFlow(todayStart)
        _endDate = MutableStateFlow(todayEnd)

        viewModelScope.launch {
            combine(_startDate, _endDate) { start, end -> start to end }.collect {
                refreshOrders()
            }
        }
    }

    val ordersState: StateFlow<List<Order>> = _orders.asStateFlow()

    fun loadMoreOrders() {
        if (!_canLoadMore.value) return
        viewModelScope.launch {
            val beforeTimestamp = _orders.value.minOfOrNull { it.timestamp } ?: return@launch
            val nextPage = orderRepository.getOrdersPage(
                startDate = _startDate.value,
                endDate = _endDate.value,
                beforeTimestamp = beforeTimestamp,
                limit = PAGE_SIZE
            )
            if (nextPage.isEmpty()) {
                _canLoadMore.value = false
            } else {
                _orders.value = _orders.value + nextPage
                _canLoadMore.value = nextPage.size == PAGE_SIZE
            }
        }
    }

    private suspend fun refreshOrders() {
        _canLoadMore.value = false
        val firstPage = orderRepository.getOrdersPage(
            startDate = _startDate.value,
            endDate = _endDate.value,
            beforeTimestamp = Long.MAX_VALUE,
            limit = PAGE_SIZE
        )
        _orders.value = firstPage
        _canLoadMore.value = firstPage.size == PAGE_SIZE
    }

    fun setDateRange(start: Long?, end: Long?) {
        if (start == null && end == null) {
            _startDate.value = allTimeStart
            _endDate.value = allTimeEnd
        } else {
            _startDate.value = start ?: todayStart
            _endDate.value = end ?: todayEnd
        }
    }

    fun setShowDateRangeDialog(show: Boolean) {
        _showDateRangeDialog.value = show
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val grossSalesState: StateFlow<Double?> = combine(_startDate, _endDate) { start, end ->
        start to end
    }.flatMapLatest { (start, end) -> orderRepository.getGrossSalesForDay(start, end) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val discountsState: StateFlow<Double?> = combine(_startDate, _endDate) { start, end ->
        start to end
    }.flatMapLatest { (start, end) -> orderRepository.getDiscountsGivenForDay(start, end) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val netRevenueState: StateFlow<Double?> = combine(_startDate, _endDate) { start, end ->
        start to end
    }.flatMapLatest { (start, end) -> orderRepository.getNetRevenueForDay(start, end) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val cashSalesState: StateFlow<Double?> = combine(_startDate, _endDate) { start, end ->
        start to end
    }.flatMapLatest { (start, end) -> orderRepository.getCashSalesForDay(start, end) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val gcashSalesState: StateFlow<Double?> = combine(_startDate, _endDate) { start, end ->
        start to end
    }.flatMapLatest { (start, end) -> orderRepository.getGcashSalesForDay(start, end) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val topSellingItemState: StateFlow<Pair<String, Int>?> = combine(_startDate, _endDate) { start, end ->
        start to end
    }.flatMapLatest { (start, end) -> orderRepository.getTopSellingItemForDay(start, end) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val expensesListState: StateFlow<List<Expense>> = combine(_startDate, _endDate) { start, end ->
        start to end
    }.flatMapLatest { (start, end) -> expenseRepository.getExpensesForDay(start, end) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val totalExpensesState: StateFlow<Double?> = combine(_startDate, _endDate) { start, end ->
        start to end
    }.flatMapLatest { (start, end) -> expenseRepository.getTotalExpensesForDay(start, end) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val appConfigState: StateFlow<AppConfig?> = appConfigRepository.getAppConfig()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun updateConfig(targetSales: Double, startingCashFloat: Double, pinHash: String) {
        viewModelScope.launch {
            appConfigRepository.updateConfig(targetSales, startingCashFloat, pinHash)
        }
    }

    fun voidOrder(orderId: String, reason: String) {
        viewModelScope.launch {
            val result = voidOrderUseCase(orderId, reason, cashierId = null)
            if (result.isFailure) {
                _exportMessage.value = "Void failed: ${result.exceptionOrNull()?.message}"
            } else {
                refreshOrders()
            }
        }
    }

    fun printZReading(summary: ZReadingSummary) {
        viewModelScope.launch {
            val result = receiptPrinterService.printZReading(summary)
            _exportMessage.value = if (result.isSuccess) {
                "Z-Reading sent to printer."
            } else {
                "Z-Reading print failed: ${result.exceptionOrNull()?.message}"
            }
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
        private const val PAGE_SIZE = 50

        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as CattasticPosApp
                return HistoryViewModel(
                    application.container.orderRepository,
                    application.container.expenseRepository,
                    application.container.exportDataUseCase,
                    application.container.appConfigRepository,
                    application.container.voidOrderUseCase,
                    application.container.receiptPrinterService
                ) as T
            }
        }
    }
}
