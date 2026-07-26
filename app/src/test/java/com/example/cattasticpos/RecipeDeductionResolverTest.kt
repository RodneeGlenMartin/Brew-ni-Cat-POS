package com.example.cattasticpos

import com.example.cattasticpos.domain.model.RecipeMapping
import com.example.cattasticpos.domain.usecase.RecipeDeductionResolver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Which BOM rows fire at checkout — wrong answers here silently drift stock levels. */
class RecipeDeductionResolverTest {

    private fun mapping(id: String, target: String?, inventoryId: String = "inv_$id", qty: Double = 1.0) =
        RecipeMapping(
            id = id,
            menuItemId = "bite_takoyaki",
            variantName = target,
            inventoryItemId = inventoryId,
            deductionQuantity = qty
        )

    private val base = mapping("base", null)
    private val fourPcs = mapping("4pcs", "4pcs")
    private val eightPcs = mapping("8pcs", "8pcs")
    private val shrimp = mapping("shrimp", "Shrimp")
    private val shrimpFourPcs = mapping("composite", "4pcs|Shrimp")
    private val takeoutBox = mapping("box", "Take-out Box")

    private fun idsOf(result: List<RecipeMapping>) = result.map { it.id }.sorted()

    @Test
    fun `base mappings always apply`() {
        val result = RecipeDeductionResolver.resolve(listOf(base), "4pcs", null)
        assertEquals(listOf("base"), idsOf(result))
    }

    @Test
    fun `only the selected size matches`() {
        val result = RecipeDeductionResolver.resolve(listOf(base, fourPcs, eightPcs), "8pcs", null)
        assertEquals(listOf("8pcs", "base"), idsOf(result))
    }

    @Test
    fun `flavor mappings stack on top of size mappings`() {
        val result = RecipeDeductionResolver.resolve(listOf(base, fourPcs, shrimp), "4pcs", "Shrimp")
        assertEquals(listOf("4pcs", "base", "shrimp"), idsOf(result))
    }

    @Test
    fun `a composite size-plus-flavor target replaces the size-only rows`() {
        val all = listOf(base, fourPcs, shrimp, shrimpFourPcs)
        val result = RecipeDeductionResolver.resolve(all, "4pcs", "Shrimp")
        assertEquals(listOf("composite"), idsOf(result))
    }

    @Test
    fun `a composite target for another size does not match`() {
        val result = RecipeDeductionResolver.resolve(listOf(base, eightPcs, shrimpFourPcs), "8pcs", "Shrimp")
        assertEquals(listOf("8pcs", "base"), idsOf(result))
    }

    @Test
    fun `add-ons in the flavor string pull in their own mappings`() {
        val all = listOf(base, fourPcs, takeoutBox)
        val result = RecipeDeductionResolver.resolve(all, "4pcs", "Shrimp + Take-out Box")
        assertEquals(listOf("4pcs", "base", "box"), idsOf(result))
    }

    @Test
    fun `multiple add-ons each resolve`() {
        val nata = mapping("nata", "Nata de coco")
        val jelly = mapping("jelly", "Rainbow Jelly")
        val result = RecipeDeductionResolver.resolve(
            listOf(base, nata, jelly),
            "16oz",
            " + Nata de coco, Rainbow Jelly"
        )
        assertEquals(listOf("base", "jelly", "nata"), idsOf(result))
    }

    @Test
    fun `sub-category prefixes are tolerated on both sides`() {
        val prefixed = mapping("americano", "Classic: Americano")
        val result = RecipeDeductionResolver.resolve(listOf(prefixed), "16oz", "Americano")
        assertEquals(listOf("americano"), idsOf(result))
    }

    @Test
    fun `an unmapped size deducts nothing but the base`() {
        val result = RecipeDeductionResolver.resolve(listOf(fourPcs, eightPcs), "16pcs", null)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `the recipe editor shows base plus the selected target only`() {
        val visible = RecipeDeductionResolver.forRecipeEditorTarget(
            listOf(base, fourPcs, eightPcs),
            "4pcs"
        )
        assertEquals(listOf("4pcs", "base"), idsOf(visible))
    }
}
