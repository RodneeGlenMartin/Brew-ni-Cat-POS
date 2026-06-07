package com.example.cattasticpos.domain.model

import org.json.JSONObject

data class PaymentConfigJson(
    val cashiers: List<Cashier>,
    val gcashAccounts: List<GcashAccount>,
    val themeAccentId: String = AppThemeAccent.DEFAULT_ID
) {
    companion object {
        fun fromStoredJson(json: String): PaymentConfigJson {
            val trimmed = json.trim()
            if (trimmed.startsWith("{")) {
                return try {
                    val obj = JSONObject(trimmed)
                    PaymentConfigJson(
                        cashiers = Cashier.fromJsonArray(obj.optJSONArray("cashiers")),
                        gcashAccounts = GcashAccount.fromJsonArray(obj.optJSONArray("gcashAccounts")),
                        themeAccentId = obj.optString("themeAccentId", AppThemeAccent.DEFAULT_ID)
                    )
                } catch (_: Exception) {
                    PaymentConfigJson(Cashier.defaultCashiers(), GcashAccount.defaultAccounts())
                }
            }
            return PaymentConfigJson(
                cashiers = Cashier.fromJson(trimmed),
                gcashAccounts = GcashAccount.defaultAccounts()
            )
        }

        fun toStoredJson(
            cashiers: List<Cashier>,
            gcashAccounts: List<GcashAccount>,
            themeAccentId: String = AppThemeAccent.DEFAULT_ID
        ): String {
            return JSONObject()
                .put("cashiers", Cashier.toJsonArray(cashiers))
                .put("gcashAccounts", GcashAccount.toJsonArray(gcashAccounts))
                .put("themeAccentId", themeAccentId)
                .toString()
        }
    }
}
