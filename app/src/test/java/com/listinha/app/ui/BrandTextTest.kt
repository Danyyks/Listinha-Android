package com.listinha.app.ui

import androidx.compose.ui.text.font.FontStyle
import com.listinha.app.ui.components.brandText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BrandTextTest {
    @Test fun `palavra entre asteriscos vira italico e os asteriscos somem`() {
        val t = brandText("O que *falta*")
        assertEquals("O que falta", t.text)
        assertEquals(1, t.spanStyles.size)
        assertEquals(FontStyle.Italic, t.spanStyles[0].item.fontStyle)
        assertEquals("falta", t.text.substring(t.spanStyles[0].start, t.spanStyles[0].end))
    }

    @Test fun `texto sem asterisco nao tem estilo`() {
        val t = brandText("Lista")
        assertEquals("Lista", t.text)
        assertTrue(t.spanStyles.isEmpty())
    }

    @Test fun `texto vazio nao quebra`() {
        val t = brandText("")
        assertEquals("", t.text)
        assertTrue(t.spanStyles.isEmpty())
    }

    @Test fun `nome com um asterisco so nao vira italico`() {
        val t = brandText("Ana*Maria")
        assertEquals("AnaMaria", t.text)
        assertTrue("asterisco sem par nao deve estilizar", t.spanStyles.isEmpty())
    }

    @Test fun `asterisco unico no fim e descartado sem italico`() {
        val t = brandText("Ana*")
        assertEquals("Ana", t.text)
        assertTrue(t.spanStyles.isEmpty())
    }

    @Test fun `so um asterisco resulta em texto vazio`() {
        assertEquals("", brandText("*").text)
    }

    @Test fun `dois pares geram dois italicos`() {
        val t = brandText("*a* b *c*")
        assertEquals("a b c", t.text)
        assertEquals(2, t.spanStyles.size)
    }

    @Test fun `par vazio entre asteriscos nao quebra`() {
        assertEquals("ab", brandText("a**b").text)
    }
}
