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
    val themeAccentId: String = AppThemeAccent.DEFAULT_ID,
    val activeCashierId: String? = null,
    val supabaseUrl: String = "",
    val supabaseAnonKey: String = "",
    val deviceId: String = ""
) {
    companion object {
        const val DEFAULT_PIN_HASH = "otCBSIxSZkk6vcF7SKwqCw==:Seyex1KVzCA7gLC3+1Vi8AHYtjU7A168GCGRihADbp0="
        private const val ITERATIONS = 10000
        private const val KEY_LENGTH = 256

        fun hashPin(pin: String, salt: ByteArray? = null): String {
            val normalizedPin = pin.trim()
            if (normalizedPin.isBlank()) {
                throw IllegalArgumentException("Cannot hash blank PIN")
            }
            val actualSalt = salt ?: ByteArray(16).apply { SecureRandom().nextBytes(this) }
            val hashBytes = derivePinHashBytes(normalizedPin, actualSalt)
            return encodePinHash(actualSalt, hashBytes)
        }

        fun verifyPin(pin: String, storedHash: String): Boolean {
            val normalizedPin = pin.trim()
            val normalizedHash = storedHash.trim()
            if (normalizedPin.isBlank() || normalizedHash.isBlank()) {
                return false
            }
            if (!normalizedHash.contains(":")) {
                val oldHash = MessageDigest.getInstance("SHA-256")
                    .digest(normalizedPin.toByteArray(Charsets.UTF_8))
                    .joinToString("") { "%02x".format(it) }
                return oldHash == normalizedHash
            }

            val colonIndex = normalizedHash.indexOf(':')
            if (colonIndex <= 0 || colonIndex >= normalizedHash.lastIndex) {
                return false
            }

            val saltSegment = normalizedHash.substring(0, colonIndex)
            val hashSegment = normalizedHash.substring(colonIndex + 1)
            if (saltSegment.isBlank() || hashSegment.isBlank()) {
                return false
            }

            val salt = try {
                Base64.decode(saltSegment, Base64.NO_WRAP)
            } catch (_: IllegalArgumentException) {
                return false
            }
            if (salt.isEmpty()) {
                return false
            }

            val expectedHashBytes = try {
                Base64.decode(hashSegment, Base64.NO_WRAP)
            } catch (_: IllegalArgumentException) {
                return false
            }

            val derivedHashBytes = try {
                derivePinHashBytes(normalizedPin, salt)
            } catch (_: Exception) {
                return false
            }

            return expectedHashBytes.contentEquals(derivedHashBytes)
        }

        private fun derivePinHashBytes(pin: String, salt: ByteArray): ByteArray {
            val spec = PBEKeySpec(pin.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
            return try {
                SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec)
                    .encoded
            } finally {
                spec.clearPassword()
            }
        }

        private fun encodePinHash(salt: ByteArray, hashBytes: ByteArray): String {
            return Base64.encodeToString(salt, Base64.NO_WRAP) +
                ":" +
                Base64.encodeToString(hashBytes, Base64.NO_WRAP)
        }
    }
}
