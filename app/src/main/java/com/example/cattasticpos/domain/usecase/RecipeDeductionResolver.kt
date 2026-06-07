package com.example.cattasticpos.domain.usecase

import com.example.cattasticpos.domain.model.RecipeMapping

/**
 * Resolves which recipe BOM rows apply at checkout for a size + optional flavor selection.
 * Base mappings (variantName = null) always apply; size and flavor mappings stack additively.
 */
object RecipeDeductionResolver {

    /** Mappings visible in the recipe editor for the currently selected target chip. */
    fun forRecipeEditorTarget(
        mappings: List<RecipeMapping>,
        selectedVariantName: String?
    ): List<RecipeMapping> =
        if (selectedVariantName == null) {
            mappings.filter { it.variantName == null }
        } else {
            mappings.filter { mapping ->
                mapping.variantName == null || mapping.variantName == selectedVariantName
            }
        }

    fun resolve(
        mappings: List<RecipeMapping>,
        sizeVariantName: String?,
        flavor: String?
    ): List<RecipeMapping> =
        mappings.filter { mapping ->
            when (mapping.variantName) {
                null -> true
                else -> targetMatches(mapping.variantName, sizeVariantName, flavor)
            }
        }

    private fun targetMatches(
        mappingTarget: String,
        sizeVariantName: String?,
        flavor: String?
    ): Boolean {
        if (sizeVariantName != null && mappingTarget == sizeVariantName) return true
        if (flavor == null) return false
        if (mappingTarget == flavor) return true
        val cleanMapping = mappingTarget.substringAfter(": ").trim().ifEmpty { mappingTarget }
        val cleanFlavor = flavor.substringAfter(": ").trim().ifEmpty { flavor }
        return cleanMapping == cleanFlavor
    }
}
