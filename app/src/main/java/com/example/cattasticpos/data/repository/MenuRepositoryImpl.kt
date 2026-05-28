package com.example.cattasticpos.data.repository

import com.example.cattasticpos.data.local.dao.MenuDao
import com.example.cattasticpos.domain.model.Category
import com.example.cattasticpos.domain.model.Item
import com.example.cattasticpos.domain.model.Variant
import com.example.cattasticpos.domain.repository.MenuRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray

class MenuRepositoryImpl(
    private val menuDao: MenuDao
) : MenuRepository {

    override fun getCategories(): Flow<List<Category>> {
        return menuDao.getCategories().map { entities ->
            entities.map { Category(it.id, it.name) }
        }
    }

    override fun getItems(): Flow<List<Item>> {
        return menuDao.getItems().map { entities ->
            entities.map { entity ->
                Item(
                    id = entity.id,
                    categoryId = entity.categoryId,
                    name = entity.name,
                    flavors = if (entity.flavors.isEmpty()) emptyList() else entity.flavors.split("|"),
                    variants = parseVariants(entity.variantsJson)
                )
            }
        }
    }

    private fun parseVariants(jsonStr: String): List<Variant> {
        val list = mutableListOf<Variant>()
        if (jsonStr.isBlank()) return list
        try {
            val jsonArray = JSONArray(jsonStr)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val id = obj.getString("id")
                val name = obj.getString("name")
                val basePrice = obj.getDouble("basePrice")
                val priceByFlavorObj = obj.optJSONObject("priceByFlavor")
                val priceMap = mutableMapOf<String, Double>()
                if (priceByFlavorObj != null) {
                    val keys = priceByFlavorObj.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        priceMap[key] = priceByFlavorObj.getDouble(key)
                    }
                }
                val description = if (obj.has("description")) obj.getString("description") else null
                list.add(Variant(id, name, basePrice, priceMap, description))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }
}
