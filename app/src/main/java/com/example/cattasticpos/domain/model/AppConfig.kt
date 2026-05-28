package com.example.cattasticpos.domain.model

import java.security.MessageDigest

data class AppConfig(
    val targetSales: Double,
    val startingCashFloat: Double,
    val pinHash: String
) {
    companion object {
        fun hashPin(pin: String): String {
            val bytes = MessageDigest.getInstance("SHA-256").digest(pin.toByteArray())
            return bytes.joinToString("") { "%02x".format(it) }
        }
    }
}
