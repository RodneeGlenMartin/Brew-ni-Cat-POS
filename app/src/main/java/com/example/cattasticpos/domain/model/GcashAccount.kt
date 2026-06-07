package com.example.cattasticpos.domain.model

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class GcashAccount(
    val id: String,
    val label: String
) {
    companion object {
        fun defaultAccounts(): List<GcashAccount> = listOf(
            GcashAccount("gcash_main", "Main GCash (0917...)"),
            GcashAccount("gcash_store", "Store GCash (0999...)"),
            GcashAccount("gcash_personal", "Personal GCash")
        )

        fun fromJsonArray(array: JSONArray?): List<GcashAccount> {
            if (array == null || array.length() == 0) return defaultAccounts()
            return buildList {
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    add(
                        GcashAccount(
                            id = obj.getString("id"),
                            label = obj.getString("label")
                        )
                    )
                }
            }.ifEmpty { defaultAccounts() }
        }

        fun toJsonArray(accounts: List<GcashAccount>): JSONArray {
            val array = JSONArray()
            accounts.forEach { account ->
                array.put(
                    JSONObject()
                        .put("id", account.id)
                        .put("label", account.label)
                )
            }
            return array
        }

        fun newId(): String = "gcash_${UUID.randomUUID().toString().substring(0, 8)}"
    }
}
