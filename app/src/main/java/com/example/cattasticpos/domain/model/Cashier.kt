package com.example.cattasticpos.domain.model

import org.json.JSONArray
import org.json.JSONObject

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
            return try {
                val array = JSONArray(json)
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

        fun toJson(cashiers: List<Cashier>): String {
            val array = JSONArray()
            cashiers.forEach { cashier ->
                array.put(
                    JSONObject()
                        .put("id", cashier.id)
                        .put("name", cashier.name)
                        .put("pinHash", cashier.pinHash)
                )
            }
            return array.toString()
        }
    }
}
