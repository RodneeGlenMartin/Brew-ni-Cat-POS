package com.example.cattasticpos.domain.usecase

import com.example.cattasticpos.domain.model.RecipeMapping

/**
 * Resolves which recipe BOM rows apply at checkout for a size + optional flavor selection.
 * Base mappings (variantName = null) always apply; size and flavor mappings stack additively.
 * Composite targets like "4pcs|Shrimp" match size + flavor together and replace size-only rows.
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
    ): List<RecipeMapping> {
        val flavorKey = flavor?.substringBefore(" —")?.trim()?.takeIf { it.isNotEmpty() }

        val compositeMatches = mappings.filter { mapping ->
            val target = mapping.variantName
            target != null && target.contains('|') && compositeTargetMatches(target, sizeVariantName, flavorKey)
        }
        if (compositeMatches.isNotEmpty()) return compositeMatches

        return mappings.filter { mapping ->
            when (val target = mapping.variantName) {
                null -> true
                else -> !target.contains('|') && targetMatches(target, sizeVariantName, flavorKey)
            }
        }
    }

    private fun compositeTargetMatches(
        mappingTarget: String,
        sizeVariantName: String?,
        flavor: String?
    ): Boolean {
        val parts = mappingTarget.split("|", limit = 2)
        if (parts.size != 2) return false
        return sizeVariantName == parts[0].trim() && flavorMatches(parts[1].trim(), flavor)
    }

    private fun targetMatches(
        mappingTarget: String,
        sizeVariantName: String?,
        flavor: String?
    ): Boolean {
        if (sizeVariantName != null && mappingTarget == sizeVariantName) return true
        return flavorMatches(mappingTarget, flavor)
    }

    private fun flavorMatches(mappingTarget: String, flavor: String?): Boolean {
        if (flavor == null) return false
        if (mappingTarget == flavor) return true
        val cleanMapping = mappingTarget.substringAfter(": ").trim().ifEmpty { mappingTarget }
        val cleanFlavor = flavor.substringAfter(": ").trim().ifEmpty { flavor }
        return cleanMapping == cleanFlavor
    }
}
