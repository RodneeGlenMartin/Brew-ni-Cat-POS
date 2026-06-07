package com.example.cattasticpos.domain.model

import org.json.JSONObject

data class PaymentConfigJson(
    val cashiers: List<Cashier>,
    val gcashAccounts: List<GcashAccount>,
    val themeAccentId: String = AppThemeAccent.DEFAULT_ID,
    val activeCashierId: String? = null
) {
    companion object {
        fun fromStoredJson(json: String): PaymentConfigJson {
            val trimmed = json.trim()
            if (trimmed.startsWith("{")) {
                return try {
                    val obj = JSONObject(trimmed)
                    val activeId = obj.optString("activeCashierId", "").ifBlank { null }
                    PaymentConfigJson(
                        cashiers = Cashier.fromJsonArray(obj.optJSONArray("cashiers")),
                        gcashAccounts = GcashAccount.fromJsonArray(obj.optJSONArray("gcashAccounts")),
                        themeAccentId = obj.optString("themeAccentId", AppThemeAccent.DEFAULT_ID),
                        activeCashierId = activeId
                    )
                } catch (_: Exception) {
                    PaymentConfigJson(Cashier.defaultCashiers(), GcashAccount.defaultAccounts())
                }
            }
            val cashiers = Cashier.fromJson(trimmed)
            return PaymentConfigJson(
                cashiers = cashiers,
                gcashAccounts = GcashAccount.defaultAccounts(),
                activeCashierId = cashiers.firstOrNull()?.id
            )
        }

        fun toStoredJson(
            cashiers: List<Cashier>,
            gcashAccounts: List<GcashAccount>,
            themeAccentId: String = AppThemeAccent.DEFAULT_ID,
            activeCashierId: String? = null
        ): String {
            val resolvedActiveId = activeCashierId?.takeIf { id ->
                cashiers.any { it.id == id }
            } ?: cashiers.firstOrNull()?.id
            return JSONObject()
                .put("cashiers", Cashier.toJsonArray(cashiers))
                .put("gcashAccounts", GcashAccount.toJsonArray(gcashAccounts))
                .put("themeAccentId", themeAccentId)
                .put("activeCashierId", resolvedActiveId.orEmpty())
                .toString()
        }
    }
}
