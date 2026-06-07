package com.example.cattasticpos.domain.model

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

data class AppConfig(
    val targetSales: Double,
    val startingCashFloat: Double,
    val pinHash: String,
    val cashiers: List<Cashier> = Cashier.defaultCashiers(),
    val gcashAccounts: List<GcashAccount> = GcashAccount.defaultAccounts(),
    val themeAccentId: String = AppThemeAccent.DEFAULT_ID
) {
    companion object {
        const val DEFAULT_PIN_HASH = "otCBSIxSZkk6vcF7SKwqCw==:Seyex1KVzCA7gLC3+1Vi8AHYtjU7A168GCGRihADbp0="
        private const val ITERATIONS = 10000
        private const val KEY_LENGTH = 256

        fun hashPin(pin: String, salt: ByteArray? = null): String {
            val actualSalt = salt ?: ByteArray(16).apply { SecureRandom().nextBytes(this) }
            val spec = PBEKeySpec(pin.toCharArray(), actualSalt, ITERATIONS, KEY_LENGTH)
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val hash = factory.generateSecret(spec).encoded
            return Base64.encodeToString(actualSalt, Base64.NO_WRAP) + ":" + Base64.encodeToString(hash, Base64.NO_WRAP)
        }

        fun verifyPin(pin: String, storedHash: String): Boolean {
            if (!storedHash.contains(":")) {
                val oldHash = MessageDigest.getInstance("SHA-256").digest(pin.toByteArray()).joinToString("") { "%02x".format(it) }
                return oldHash == storedHash
            }
            val parts = storedHash.split(":")
            val salt = Base64.decode(parts[0], Base64.NO_WRAP)
            val newHash = hashPin(pin, salt)
            return newHash == storedHash
        }
    }
}
