package com.example.cattasticpos

import android.app.Application
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.cattasticpos.data.local.PosDatabase
import com.example.cattasticpos.data.repository.MenuRepositoryImpl
import com.example.cattasticpos.data.repository.OrderRepositoryImpl
import com.example.cattasticpos.domain.repository.MenuRepository
import com.example.cattasticpos.domain.repository.OrderRepository
import com.example.cattasticpos.domain.repository.ExpenseRepository
import com.example.cattasticpos.data.repository.ExpenseRepositoryImpl
import com.example.cattasticpos.domain.repository.InventoryRepository
import com.example.cattasticpos.data.repository.InventoryRepositoryImpl
import com.example.cattasticpos.domain.repository.RecipeRepository
import com.example.cattasticpos.data.repository.RecipeRepositoryImpl
import com.example.cattasticpos.domain.repository.AppConfigRepository
import com.example.cattasticpos.data.repository.AppConfigRepositoryImpl
import com.example.cattasticpos.domain.repository.VoidRepository
import com.example.cattasticpos.data.repository.VoidRepositoryImpl
import com.example.cattasticpos.domain.repository.TransactionProvider
import com.example.cattasticpos.data.local.RoomTransactionProvider
import com.example.cattasticpos.domain.usecase.CalculateCartUseCase
import com.example.cattasticpos.domain.usecase.CheckoutUseCase
import com.example.cattasticpos.domain.usecase.RestockItemUseCase
import com.example.cattasticpos.domain.usecase.GetMenuUseCase
import com.example.cattasticpos.domain.usecase.ExportDataUseCase
import com.example.cattasticpos.domain.usecase.UpdateOrderUseCase
import com.example.cattasticpos.domain.usecase.VoidOrderUseCase
import com.example.cattasticpos.domain.service.ReceiptPrinterService
import com.example.cattasticpos.worker.LowStockCheckWorker
import android.util.Log
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import java.util.concurrent.TimeUnit

class CattasticPosApp : Application(), Configuration.Provider {
    
    private val applicationScope = CoroutineScope(SupervisorJob())

    lateinit var container: AppContainer

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().build()

    override fun onCreate() {
        super.onCreate()

        if (isCrashProcess()) {
            return
        }
        
        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
                try {
                    val stackTraceString = Log.getStackTraceString(throwable)
                    val intent = Intent(this, com.example.cattasticpos.ui.CrashActivity::class.java).apply {
                        putExtra("error_message", stackTraceString)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    }
                    startActivity(intent)
                } catch (e: Exception) {
                    // Fallback
                } finally {
                    android.os.Process.killProcess(android.os.Process.myPid())
                    System.exit(1)
                }
            }

        container = AppContainerImpl(this, applicationScope)
        scheduleLowStockChecks()
    }

    private fun scheduleLowStockChecks() {
        LowStockCheckWorker.ensureChannel(this)
        val request = PeriodicWorkRequestBuilder<LowStockCheckWorker>(6, TimeUnit.HOURS).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            LowStockCheckWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    private fun isCrashProcess(): Boolean {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            return Application.getProcessName().endsWith(":crash")
        }
        try {
            val pid = android.os.Process.myPid()
            val am = getSystemService(android.content.Context.ACTIVITY_SERVICE) as android.app.ActivityManager
            val runningProcesses = am.runningAppProcesses
            if (runningProcesses != null) {
                for (processInfo in runningProcesses) {
                    if (processInfo.pid == pid) {
                        return processInfo.processName.endsWith(":crash")
                    }
                }
            }
        } catch (e: Exception) {
            // ignore
        }
        return false
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
    val recipeRepository: RecipeRepository
    val appConfigRepository: AppConfigRepository
    val voidRepository: VoidRepository
    val voidOrderUseCase: VoidOrderUseCase
    val updateOrderUseCase: UpdateOrderUseCase
    val restockItemUseCase: RestockItemUseCase
    val receiptPrinterService: ReceiptPrinterService
    val transactionProvider: TransactionProvider
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
        OrderRepositoryImpl(database)
    }

    override val getMenuUseCase: GetMenuUseCase by lazy {
        GetMenuUseCase(menuRepository)
    }

    override val calculateCartUseCase: CalculateCartUseCase by lazy {
        CalculateCartUseCase()
    }

    override val checkoutUseCase: CheckoutUseCase by lazy {
        CheckoutUseCase(orderRepository, inventoryRepository, recipeRepository, transactionProvider, calculateCartUseCase)
    }

    override val expenseRepository: ExpenseRepository by lazy {
        ExpenseRepositoryImpl(database.expenseDao())
    }

    override val inventoryRepository: InventoryRepository by lazy {
        InventoryRepositoryImpl(database.inventoryDao())
    }

    override val recipeRepository: RecipeRepository by lazy {
        RecipeRepositoryImpl(database.recipeDao())
    }

    override val appConfigRepository: AppConfigRepository by lazy {
        AppConfigRepositoryImpl(database.appConfigDao())
    }

    override val voidRepository: VoidRepository by lazy {
        VoidRepositoryImpl(database.voidDao())
    }

    override val voidOrderUseCase: VoidOrderUseCase by lazy {
        VoidOrderUseCase(orderRepository, voidRepository, recipeRepository, inventoryRepository, transactionProvider)
    }

    override val updateOrderUseCase: UpdateOrderUseCase by lazy {
        UpdateOrderUseCase(orderRepository, inventoryRepository, recipeRepository, transactionProvider, calculateCartUseCase)
    }

    override val exportDataUseCase: ExportDataUseCase by lazy {
        ExportDataUseCase(context)
    }

    override val restockItemUseCase: RestockItemUseCase by lazy {
        RestockItemUseCase(inventoryRepository)
    }

    override val receiptPrinterService: ReceiptPrinterService by lazy {
        ReceiptPrinterService(context)
    }

    override val transactionProvider: TransactionProvider by lazy {
        RoomTransactionProvider(database)
    }
}
