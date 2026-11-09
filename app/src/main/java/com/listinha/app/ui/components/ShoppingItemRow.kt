package com.listinha.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.listinha.app.R
import com.listinha.app.data.Item
import com.listinha.app.ui.theme.LocalFlavor
import com.listinha.app.ui.theme.LocalPaper
import com.listinha.app.util.formatCurrency

// ============================================================
//  ShoppingItemRow.kt  —  uma linha da notinha (um item)
// ------------------------------------------------------------
//  Caixinha de marcar, nome riscado de caneta quando comprado,
//  quantidade, preço e lixeira. No modo compra a linha inteira
//  marca o item e a lixeira some, para evitar toque errado.
// ============================================================

/**
 * @param shopping true no modo compra: a linha inteira alterna o item
 */
@Composable
fun ShoppingItemRow(
    item: Item,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    shopping: Boolean,
    modifier: Modifier = Modifier,
) {
    val paper = LocalPaper.current
    val flavor = LocalFlavor.current
    val fundoMarcado = flavor.title
    val fonteGrande = LocalDensity.current.fontScale > 1.3f

    // No modo compra a linha toda é a área de toque; fora dele, só a caixinha.
    val linha = if (shopping) {
        modifier.toggleable(value = item.bought, role = Role.Checkbox, onValueChange = { onToggle() })
    } else {
        modifier
    }

    Row(
        modifier = linha.heightIn(min = 52.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Área de toque de 48dp em volta da caixinha de 26dp.
        val caixa = if (shopping) {
            Modifier.size(48.dp)
        } else {
            Modifier
                .size(48.dp)
                .semantics { contentDescription = item.name }
                .toggleable(value = item.bought, role = Role.Checkbox, onValueChange = { onToggle() })
        }
        Box(caixa, contentAlignment = Alignment.CenterStart) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (item.bought) fundoMarcado else Color.Transparent)
                    .border(2.5.dp, if (item.bought) fundoMarcado else paper.muted, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center,
            ) {
                if (item.bought) {
                    Icon(
                        painterResource(R.drawable.ic_check),
                        contentDescription = null,
                        tint = flavor.on,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }

        // Fora do modo compra, tocar no nome abre a edição.
        val textoMod = if (shopping) Modifier.weight(1f) else Modifier.weight(1f).clickable { onEdit() }
        Column(textoMod.padding(vertical = 6.dp)) {
            PenStrikeText(text = item.name, struck = item.bought, penColor = flavor.title)
            if (item.quantity > 1) {
                Text(
                    text = "${item.quantity.toInt()} unidades",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = paper.muted,
                )
            }
            if (fonteGrande && item.price > 0) {
                Text(
                    text = formatCurrency(item.total),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (item.bought) paper.muted else paper.ink,
                )
            }
        }

        if (!fonteGrande && item.price > 0) {
            Spacer(Modifier.width(8.dp))
            Text(
                text = formatCurrency(item.total),
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (item.bought) paper.muted else paper.ink,
            )
        }

        if (!shopping) {
            IconButton(onClick = onDelete) {
                Icon(
                    painter = painterResource(R.drawable.ic_delete),
                    contentDescription = "Apagar ${item.name}",
                    tint = paper.muted,
                    modifier = Modifier.size(19.dp),
                )
            }
        }
    }
}

// O nome do item. Quando comprado, ganha um risco de caneta por cima de cada linha.
@Composable
private fun PenStrikeText(text: String, struck: Boolean, penColor: Color) {
    val paper = LocalPaper.current
    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
    Text(
        text = text,
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold,
        color = if (struck) paper.muted else paper.ink,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        onTextLayout = { layout = it },
        modifier = Modifier.drawWithContent {
            drawContent()
            val resultado = layout
            if (struck && resultado != null) {
                for (linha in 0 until resultado.lineCount) {
                    val meio = (resultado.getLineTop(linha) + resultado.getLineBottom(linha)) / 2
                    // Inclinação de 1,5dp: o traço parece feito à mão, não uma régua.
                    drawLine(
                        color = penColor,
                        start = Offset(resultado.getLineLeft(linha) - 2.dp.toPx(), meio + 1.5.dp.toPx()),
                        end = Offset(minOf(resultado.getLineRight(linha) + 2.dp.toPx(), size.width), meio),
                        strokeWidth = 2.5.dp.toPx(),
                        cap = StrokeCap.Round,
                    )
                }
            }
        },
    )
}
