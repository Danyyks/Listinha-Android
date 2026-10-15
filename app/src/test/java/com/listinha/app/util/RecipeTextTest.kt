package com.listinha.app.util

import com.listinha.app.data.RecipeIngredient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

// ============================================================
//  Testes das regras de texto das receitas (RecipeText.kt).
//  Rodam no computador, sem emulador: gradle testDebugUnitTest
// ============================================================

class RecipeTextTest {

    private fun ing(line: String) = parseIngredientLine(line)!!

    @Test
    fun `separa quantidade e nome`() {
        assertEquals("500 g" to "Peito de frango", ing("500 g de peito de frango").let { it.quantity to it.name })
        assertEquals("500 g" to "Farinha", ing("500g farinha").let { it.quantity to it.name })
        assertEquals("3" to "Ovos", ing("3 ovos").let { it.quantity to it.name })
        assertEquals("2 dentes" to "Alho", ing("2 dentes de alho").let { it.quantity to it.name })
        assertEquals("1 lata" to "Leite condensado", ing("1 lata de leite condensado").let { it.quantity to it.name })
        assertEquals("1/2 xícara (chá)" to "Óleo", ing("1/2 xícara (chá) de óleo").let { it.quantity to it.name })
        assertEquals("2 colheres de sopa" to "Manteiga", ing("2 colheres de sopa de manteiga").let { it.quantity to it.name })
        assertEquals("meia xícara" to "Leite", ing("meia xícara de leite").let { it.quantity to it.name })
        assertEquals("1 1/2 kg" to "Carne", ing("1 1/2 kg de carne").let { it.quantity to it.name })
        assertEquals("2 cups" to "Flour", ing("2 cups flour").let { it.quantity to it.name })
    }

    @Test
    fun `quantidade no fim e a gosto`() {
        assertEquals("a gosto" to "Sal", ing("Sal a gosto").let { it.quantity to it.name })
        assertEquals("500 g" to "Farinha", ing("Farinha (500 g)").let { it.quantity to it.name })
        assertEquals("3" to "Ovos", ing("Ovos: 3").let { it.quantity to it.name })
        assertEquals("" to "Cebola picada", ing("- cebola picada").let { it.quantity to it.name })
    }

    @Test
    fun `nao confunde palavras parecidas com unidades`() {
        assertEquals("2" to "Gemas", ing("2 gemas").let { it.quantity to it.name })
        assertEquals("1" to "Limão", ing("1 limão").let { it.quantity to it.name })
        assertEquals("3" to "Peitos de frango", ing("3 peitos de frango").let { it.quantity to it.name })
        assertNull(parseIngredientLine("   "))
    }

    @Test
    fun `adivinha a categoria`() {
        assertEquals("carnes", guessCategory("Peito de frango"))
        assertEquals("verduras", guessCategory("Tomates"))
        assertEquals("mercearia", guessCategory("Molho de tomate"))
        assertEquals("laticinios", guessCategory("Creme de leite"))
        assertEquals("doces", guessCategory("Leite condensado"))
        assertEquals("laticinios", guessCategory("Ovos"))
        assertEquals("mercearia", guessCategory("Açúcar"))
        assertEquals("verduras", guessCategory("Salsinha"))
        assertEquals("bebidas", guessCategory("Suco de laranja"))
        assertEquals("snacks", guessCategory("Batata palha"))
        assertEquals("outros", guessCategory("Chantilly"))
    }

    @Test
    fun `adivinha o icone pelo nome`() {
        assertEquals(8, guessIconIndex("Bolo de cenoura"))
        assertEquals(4, guessIconIndex("Macarrão à carbonara"))
        assertEquals(5, guessIconIndex("Pizza caseira"))
        assertEquals(3, guessIconIndex("Torta salgada de frango"))
        assertEquals(7, guessIconIndex("Pão de queijo"))
        assertEquals(0, guessIconIndex("Strogonoff"))
    }

    @Test
    fun `ingrediente vai e volta igual pelo texto`() {
        val originais = listOf(
            RecipeIngredient("Peito de frango", "carnes", "500 g"),
            RecipeIngredient("Ovos", "laticinios", "3"),
            RecipeIngredient("Sal", "mercearia", "a gosto"),
            RecipeIngredient("Latas de milho", "mercearia", "2"),
            RecipeIngredient("Leite", "laticinios", "1 pct"),
            RecipeIngredient("Cebola", "verduras", ""),
            RecipeIngredient("Flour", "mercearia", "2 cups"),
        )
        val devolta = parseIngredients(ingredientsToText(originais))
        assertEquals(originais.map { it.name to it.quantity }, devolta.map { it.name to it.quantity })
    }

    @Test
    fun `passos tiram a numeracao colada`() {
        val passos = parseSteps("1. Bata os ovos.\n2) Misture a farinha.\n\nPasso 3: Asse por 40 minutos.\n• Sirva.")
        assertEquals(listOf("Bata os ovos.", "Misture a farinha.", "Asse por 40 minutos.", "Sirva."), passos)
    }

    @Test
    fun `le receita colada com titulos`() {
        val texto = """
            Bolo de cenoura fofinho 🥕
            Rende 12 porções
            Tempo: 50 min

            Ingredientes:
            • 3 cenouras médias
            • 4 ovos
            • 1 xícara de óleo
            • 2 xícaras de açúcar
            • 2 xícaras de farinha de trigo
            • 1 colher de sopa de fermento em pó

            Modo de preparo:
            1. Bata no liquidificador a cenoura, os ovos e o óleo.
            2. Misture o açúcar e a farinha.
            3. Asse por 40 minutos a 180 °C.

            https://exemplo.com/bolo-de-cenoura
            #bolo #receita
        """.trimIndent()
        val r = parseRecipeText(texto)
        assertEquals("Bolo de cenoura fofinho", r.name)
        assertEquals(12, r.servings)
        assertEquals(50, r.minutes)
        assertEquals(6, r.ingredients.size)
        assertEquals("3" to "Cenouras médias", r.ingredients[0].let { it.quantity to it.name })
        assertEquals("verduras", r.ingredients[0].category)
        assertEquals("1 colher de sopa" to "Fermento em pó", r.ingredients[5].let { it.quantity to it.name })
        assertEquals(3, r.steps.size)
        assertEquals("Asse por 40 minutos a 180 °C.", r.steps[2])
        assertEquals("https://exemplo.com/bolo-de-cenoura", r.sourceUrl)
        assertEquals(8, r.iconIndex)
    }

    @Test
    fun `le receita colada sem titulos`() {
        val texto = """
            Brigadeiro
            1 lata de leite condensado
            1 colher de sopa de manteiga
            2 colheres de sopa de chocolate em pó
            Leve tudo ao fogo baixo mexendo sem parar até desgrudar do fundo da panela.
            Espere esfriar, enrole e passe no granulado.
        """.trimIndent()
        val r = parseRecipeText(texto)
        assertEquals("Brigadeiro", r.name)
        assertEquals(3, r.ingredients.size)
        assertEquals(2, r.steps.size)
    }
}
