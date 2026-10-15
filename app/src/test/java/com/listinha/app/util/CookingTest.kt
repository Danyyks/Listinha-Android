package com.listinha.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

// ============================================================
//  Testes das contas de cozinha (Cooking.kt): porções e timer.
// ============================================================

class CookingTest {

    private fun scale(q: String, f: Double) = scaleQuantity(q, f, decimalSeparator = ',')

    @Test
    fun `dobra e divide quantidades`() {
        assertEquals("1000 g", scale("500 g", 2.0))
        assertEquals("1 xícara", scale("1/2 xícara", 2.0))
        assertEquals("3 xícaras", scale("1 1/2 xícara", 2.0))
        assertEquals("1/2 xícara", scale("1 xícara", 0.5))
        assertEquals("6", scale("3", 2.0))
        assertEquals("1 1/2", scale("3", 0.5))
        assertEquals("2,5 kg", scale("1,25 kg", 2.0))
        assertEquals("1 xícara", scale("meia xícara", 2.0))
        assertEquals("4 a 6 dentes", scale("2 a 3 dentes", 2.0))
    }

    @Test
    fun `nao mexe no que nao tem numero`() {
        assertEquals("a gosto", scale("a gosto", 2.0))
        assertEquals("", scale("", 2.0))
        assertEquals("500 g", scale("500 g", 1.0))
    }

    @Test
    fun `detecta o tempo do passo`() {
        assertEquals(25 * 60, detectTimerSeconds("Despeje na forma e asse por 25 minutos."))
        assertEquals(35 * 60, detectTimerSeconds("Asse de 30 a 35 min até dourar."))
        assertEquals(90 * 60, detectTimerSeconds("Cozinhe por 1 hora e 30 minutos."))
        assertEquals(90 * 60, detectTimerSeconds("Deixe na geladeira por 1h30."))
        assertEquals(2 * 3600, detectTimerSeconds("Leve à geladeira por 2 horas."))
        assertEquals(30, detectTimerSeconds("Bata por 30 segundos."))
        assertEquals(10 * 60, detectTimerSeconds("Bake for 10 minutes."))
        assertNull(detectTimerSeconds("Pré-aqueça o forno a 180 °C."))
        assertNull(detectTimerSeconds("Misture 3 ovos."))
    }

    @Test
    fun `formata relogio e duracao`() {
        assertEquals("2:05", formatClock(125))
        assertEquals("1:02:05", formatClock(3725))
        assertEquals("30 min", formatDuration(30))
        assertEquals("1 h 20", formatDuration(80))
        assertEquals("2 h", formatDuration(120))
    }
}
