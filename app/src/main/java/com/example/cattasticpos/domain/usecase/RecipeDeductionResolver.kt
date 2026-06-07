package com.example.cattasticpos.domain.usecase

import com.example.cattasticpos.domain.model.RecipeMapping

/**
 * Resolves which recipe BOM rows apply at checkout for a size + optional flavor selection.
 * Base mappings (variantName = null) always apply; size and flavor mappings stack additively.
 */
object RecipeDeductionResolver {
    fun resolve(
        mappings: List<RecipeMapping>,
        sizeVariantName: String?,
        flavor: String?
    ): List<RecipeMapping> =
        mappings.filter { mapping ->
            when (mapping.variantName) {
                null -> true
                else ->
                    (flavor != null && mapping.variantName == flavor) ||
                        (sizeVariantName != null && mapping.variantName == sizeVariantName)
            }
        }
}
