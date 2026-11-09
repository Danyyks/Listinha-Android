package com.listinha.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.listinha.app.ui.theme.LocalFlavor
import com.listinha.app.ui.theme.LocalPaper

// ============================================================
//  ShoppingModeBanner.kt  —  painel do "Modo de Compra"
// ------------------------------------------------------------
//  Cartão inclinado com o próximo item em letras grandes, para
//  ler de relance no corredor, e um segmento por item da lista.
// ============================================================

@Composable
fun ShoppingModeBanner(
    userName: String?,
    boughtCount: Int,
    totalCount: Int,
    nextItemName: String?,
    modifier: Modifier = Modifier,
) {
    val flavor = LocalFlavor.current
    val paper = LocalPaper.current
    val firstName = userName?.split(" ")?.firstOrNull()?.replace("*", "")?.takeIf { it.isNotBlank() } ?: "você"
    val allDone = totalCount > 0 && boughtCount == totalCount
    val faltam = totalCount - boughtCount
    val forma = RoundedCornerShape(30.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp, vertical = 4.dp)
            .rotate(-1.5f)
            .shadow(14.dp, forma, ambientColor = paper.shadow, spotColor = paper.shadow)
            .clip(forma)
            .background(flavor.title)
            .padding(start = 22.dp, end = 22.dp, top = 22.dp, bottom = 20.dp),
    ) {
        if (allDone) {
            Text("Lista completa, $firstName!", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = flavor.bg)
            Spacer(Modifier.height(8.dp))
            BrandTitle("Arrasou nas *compras.*", fontSize = 38.sp, color = flavor.bg)
        } else {
            Text(
                text = if (boughtCount == 0) {
                    "Vamos lá, $firstName! Próximo item:"
                } else {
                    "$firstName, faltam só $faltam ${if (faltam == 1) "item" else "itens"}!"
                },
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = flavor.bg,
            )
            if (nextItemName != null) {
                Spacer(Modifier.height(6.dp))
                BrandTitle(nextItemName.replace("*", ""), fontSize = 40.sp, color = flavor.bg, maxLines = 2)
            }
        }

        Spacer(Modifier.height(18.dp))

        // Um segmento por item; limitado para não virar traços finos em listas enormes.
        val segmentos = totalCount.coerceAtMost(MAX_SEGMENTS)
        val cheios = if (totalCount == 0) 0 else boughtCount * segmentos / totalCount
        Row(
            modifier = Modifier.fillMaxWidth().clearAndSetSemantics { },
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            repeat(segmentos) { i ->
                Box(
                    Modifier
                        .weight(1f)
                        .height(7.dp)
                        .clip(RoundedCornerShape(99.dp))
                        .background(flavor.bg.copy(alpha = if (i < cheios) 1f else 0.3f)),
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        Text(
            "$boughtCount de $totalCount comprados",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = flavor.bg,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private const val MAX_SEGMENTS = 30
