package com.example.cattasticpos.domain.model

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class Cashier(
    val id: String,
    val name: String,
    val pinHash: String
) {
    companion object {
        fun defaultCashiers(): List<Cashier> = listOf(
            Cashier(
                id = "cashier_default",
                name = "Popot",
                pinHash = AppConfig.DEFAULT_PIN_HASH
            )
        )

        fun fromJson(json: String): List<Cashier> {
            if (json.isBlank()) return defaultCashiers()
            val trimmed = json.trim()
            if (trimmed.startsWith("{")) {
                return PaymentConfigJson.fromStoredJson(trimmed).cashiers
            }
            return fromJsonArray(JSONArray(trimmed))
        }

        fun fromJsonArray(array: JSONArray?): List<Cashier> {
            if (array == null || array.length() == 0) return defaultCashiers()
            return try {
                buildList {
                    for (i in 0 until array.length()) {
                        val obj = array.getJSONObject(i)
                        add(
                            Cashier(
                                id = obj.getString("id"),
                                name = obj.getString("name"),
                                pinHash = obj.getString("pinHash")
                            )
                        )
                    }
                }.ifEmpty { defaultCashiers() }
            } catch (_: Exception) {
                defaultCashiers()
            }
        }

        fun toJsonArray(cashiers: List<Cashier>): JSONArray {
            val array = JSONArray()
            cashiers.forEach { cashier ->
                array.put(
                    JSONObject()
                        .put("id", cashier.id)
                        .put("name", cashier.name)
                        .put("pinHash", cashier.pinHash)
                )
            }
            return array
        }

        fun toJson(cashiers: List<Cashier>): String = toJsonArray(cashiers).toString()

        fun newId(): String = "cashier_${UUID.randomUUID().toString().substring(0, 8)}"
    }
}
