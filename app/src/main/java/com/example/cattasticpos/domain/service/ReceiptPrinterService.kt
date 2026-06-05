package com.example.cattasticpos.domain.service

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import com.example.cattasticpos.domain.model.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.UUID
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.os.Build

class ReceiptPrinterService(private val context: Context) {
    private val standardSerialPortServiceId: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    @SuppressLint("MissingPermission")
    suspend fun printReceipt(order: Order): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    return@withContext Result.failure(Exception("Bluetooth connect permission is required."))
                }
            }
            val adapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
            if (adapter == null || !adapter.isEnabled) {
                return@withContext Result.failure(Exception("Bluetooth is disabled or not supported."))
            }

            val pairedDevices: Set<BluetoothDevice>? = try {
                adapter.bondedDevices
            } catch (e: SecurityException) {
                return@withContext Result.failure(Exception("SecurityException: Bluetooth connect permission is missing.", e))
            }

            if (pairedDevices.isNullOrEmpty()) {
                return@withContext Result.failure(Exception("No paired Bluetooth devices found."))
            }

            val printerDevice = pairedDevices.firstOrNull { 
                val name = try {
                    it.name?.lowercase() ?: ""
                } catch (e: SecurityException) {
                    ""
                }
                name.contains("printer") || name.contains("pos") || name.contains("pt-")
            } ?: pairedDevices.first()

            var socket: BluetoothSocket? = null
            try {
                socket = printerDevice.createRfcommSocketToServiceRecord(standardSerialPortServiceId)
                socket.connect()
                
                val outputStream = socket.outputStream

                val ESC = 0x1B.toByte()
                val GS = 0x1D.toByte()
                val initPrinter = byteArrayOf(ESC, '@'.code.toByte())
                val alignCenter = byteArrayOf(ESC, 'a'.code.toByte(), 1)
                val alignLeft = byteArrayOf(ESC, 'a'.code.toByte(), 0)
                val boldOn = byteArrayOf(ESC, 'E'.code.toByte(), 1)
                val boldOff = byteArrayOf(ESC, 'E'.code.toByte(), 0)
                val cutPaper = byteArrayOf(GS, 'V'.code.toByte(), 66, 0)

                outputStream.write(initPrinter)
                outputStream.write(alignCenter)
                outputStream.write(boldOn)
                outputStream.write("Cat-Tastic Bites & Drinks\n".toByteArray())
                outputStream.write(boldOff)
                outputStream.write("Order: ${order.id.take(8)}\n".toByteArray())
                outputStream.write("Payment: ${order.paymentMethod}\n".toByteArray())
                outputStream.write("--------------------------------\n".toByteArray())
                
                outputStream.write(alignLeft)
                order.items.forEach { item ->
                    val line = "${item.quantity}x ${item.itemName} (${item.variantName})\n"
                    outputStream.write(line.toByteArray())
                    if (!item.flavor.isNullOrBlank()) {
                        outputStream.write("   - ${item.flavor}\n".toByteArray())
                    }
                    outputStream.write("   PHP ${item.totalPrice}\n".toByteArray())
                }
                outputStream.write("--------------------------------\n".toByteArray())
                
                outputStream.write(alignCenter)
                outputStream.write("Subtotal: PHP ${order.subtotal}\n".toByteArray())
                outputStream.write("Discount: -PHP ${order.discountDeduction}\n".toByteArray())
                outputStream.write(boldOn)
                outputStream.write("Total: PHP ${order.total}\n".toByteArray())
                outputStream.write(boldOff)
                
                outputStream.write("\nThank you, meow!\n\n\n\n".toByteArray())
                outputStream.write(cutPaper)
                
                outputStream.flush()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            } finally {
                try {
                    socket?.close()
                } catch (e: IOException) {
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
