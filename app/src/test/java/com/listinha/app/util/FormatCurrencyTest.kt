package com.listinha.app.util

import org.junit.Assert.assertEquals
import org.junit.Test

class FormatCurrencyTest {
    // O Java usa espaço rígido (U+00A0) depois do "R$"; normaliza para comparar.
    private fun f(v: Double) = formatCurrency(v).replace('\u00A0', ' ')

    @Test fun `deve formatar zero em reais`() = assertEquals("R$ 0,00", f(0.0))
    @Test fun `deve usar virgula nos centavos`() = assertEquals("R$ 12,50", f(12.5))
    @Test fun `deve usar ponto nos milhares`() = assertEquals("R$ 1.234,56", f(1234.56))
    @Test fun `deve arredondar fracao de centavo`() = assertEquals("R$ 0,11", f(0.105 + 0.0049))
    @Test fun `deve formatar valor negativo`() = assertEquals("-R$ 5,00", f(-5.0))
    @Test fun `deve formatar milhoes sem perder digitos`() = assertEquals("R$ 1.000.000,00", f(1_000_000.0))
    @Test fun `soma de centavos de ponto flutuante nao vaza para a tela`() = assertEquals("R$ 0,30", f(0.1 + 0.2))
}
