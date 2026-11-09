package com.listinha.app.util

import org.junit.Assert.assertEquals
import org.junit.Test

class CategoriaPorIngredienteTest {
    @Test fun `tomate e verdura ou fruta e nunca outros`() {
        val c = guessCategory("2 tomates")
        assertEquals(true, c == "verduras" || c == "frutas")
    }
    @Test fun `ingrediente desconhecido cai em outros`() = assertEquals("outros", guessCategory("xablau zzz"))
    @Test fun `texto vazio cai em outros`() = assertEquals("outros", guessCategory(""))
    @Test fun `acentos e maiusculas nao atrapalham`() = assertEquals(guessCategory("limao"), guessCategory("LIMÃO"))
    @Test fun `chantilly nao vira cha`() = assertEquals(false, guessCategory("chantilly") == guessCategory("cha"))
    @Test fun `nome de receita sem palavra conhecida usa a panela`() = assertEquals(0, guessIconIndex("Receita da vovo"))
    @Test fun `bolo usa o icone de bolo`() = assertEquals(8, guessIconIndex("Bolo de cenoura"))
}
