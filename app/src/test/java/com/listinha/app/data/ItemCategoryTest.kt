package com.listinha.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ItemCategoryTest {
    @Test fun `total do item e preco vezes quantidade`() =
        assertEquals(7.5, Item(price = 2.5, quantity = 3.0).total, 1e-9)

    @Test fun `item sem preco tem total zero`() = assertEquals(0.0, Item(quantity = 10.0).total, 1e-9)

    @Test fun `quantidade fracionada multiplica certo`() =
        assertEquals(5.0, Item(price = 10.0, quantity = 0.5).total, 1e-9)

    @Test fun `soma de itens da lista fecha em centavos`() {
        val itens = listOf(Item(price = 0.1, quantity = 1.0), Item(price = 0.2, quantity = 1.0))
        assertEquals(0.3, itens.sumOf { it.total }, 1e-9)
    }

    @Test fun `chaves de categoria sao unicas`() =
        assertEquals(CATEGORIES.size, CATEGORIES.map { it.key }.toSet().size)

    @Test fun `categoria conhecida devolve rotulo`() = assertEquals("Laticínios", categoryLabel("laticinios"))
    @Test fun `categoria personalizada usa a propria chave como rotulo`() = assertEquals("pet", categoryLabel("pet"))
    @Test fun `categoria inexistente nao achada devolve null`() = assertNull(categoryByKey("xyz"))
    @Test fun `categoria conhecida e achada`() = assertNotNull(categoryByKey("frutas"))

    @Test fun `outros fica sempre por ultimo`() {
        val ordem = sortCategoryKeys(listOf("outros", "pet", "frutas", "limpeza"))
        assertEquals(listOf("frutas", "limpeza", "pet", "outros"), ordem)
    }

    @Test fun `personalizadas ficam em ordem alfabetica depois das fixas`() =
        assertEquals(listOf("bebidas", "ave", "zebra"), sortCategoryKeys(listOf("zebra", "ave", "bebidas")))

    @Test fun `ordenar lista com varias outros nao lanca erro de contrato do Comparator`() {
        val muitas = List(200) { if (it % 2 == 0) "outros" else "frutas" }
        val r = sortCategoryKeys(muitas)
        assertTrue(r.takeLast(100).all { it == "outros" })
    }

    @Test fun `lista vazia ordena para vazia`() = assertEquals(emptyList<String>(), sortCategoryKeys(emptyList()))
}
