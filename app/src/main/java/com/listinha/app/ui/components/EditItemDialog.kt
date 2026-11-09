package com.listinha.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.listinha.app.R
import com.listinha.app.data.CATEGORIES
import com.listinha.app.data.Item
import com.listinha.app.data.categoryColor
import com.listinha.app.ui.theme.LocalPaper
import kotlin.math.roundToInt

// ============================================================
//  EditItemDialog.kt  —  janela para editar um item existente
// ------------------------------------------------------------
//  Abre quando o usuário toca num item da lista, com os campos
//  já preenchidos com os valores atuais.
// ============================================================

@Composable
fun EditItemDialog(
    item: Item,
    customCategories: List<String>,
    onSave: (name: String, category: String, price: Double, quantity: Double) -> Unit,
    onDismiss: () -> Unit,
) {
    val paper = LocalPaper.current
    var name by remember { mutableStateOf(item.name) }
    var category by remember { mutableStateOf(item.category) }
    // O seletor trabalha com inteiro; roundToInt evita cortar para baixo uma fração.
    var quantity by remember { mutableIntStateOf(item.quantity.roundToInt().coerceAtLeast(1)) }
    var priceText by remember {
        mutableStateOf(
            if (item.price == 0.0) ""
            else if (item.price % 1.0 == 0.0) item.price.toInt().toString()
            else item.price.toString()
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        containerColor = paper.paper,
        title = { BrandTitle("Editar *item*", fontSize = 30.sp, color = paper.ink) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                BrandField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = "Nome",
                    description = "Nome",
                    modifier = Modifier.fillMaxWidth(),
                    height = 52.dp,
                    fontSize = 16.sp,
                    background = paper.field,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(CATEGORIES) { cat ->
                        EditChip(cat.label, categoryColor(cat.key), category == cat.key) { category = cat.key }
                    }
                    items(customCategories) { key ->
                        EditChip(key, categoryColor(key), category == key) { category = key }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { if (quantity > 1) quantity-- }) {
                        Icon(painterResource(R.drawable.ic_remove), contentDescription = "Diminuir", tint = paper.ink, modifier = Modifier.size(18.dp))
                    }
                    Text(
                        "$quantity",
                        modifier = Modifier.widthIn(min = 22.dp),
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = paper.ink,
                    )
                    IconButton(onClick = { quantity++ }) {
                        Icon(painterResource(R.drawable.ic_add), contentDescription = "Aumentar", tint = paper.ink, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(8.dp))
                    BrandField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        placeholder = "Preço",
                        description = "Preço",
                        modifier = Modifier.weight(1f),
                        height = 48.dp,
                        fontSize = 16.sp,
                        background = paper.field,
                        prefix = "R$",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    )
                }
            }
        },
        confirmButton = {
            FilledPill(
                text = "Salvar",
                container = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                onClick = {
                    val nome = name.trim()
                    if (nome.isNotEmpty()) {
                        val price = priceText.replace(",", ".").toDoubleOrNull() ?: 0.0
                        onSave(nome, category, price, quantity.toDouble())
                    }
                },
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.heightIn(min = 48.dp)) {
                Text("Cancelar", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = paper.ink)
            }
        },
    )
}

// Dentro da caixa o chip fica sobre o papel, então o não selecionado usa o tom de campo.
@Composable
private fun EditChip(label: String, dot: androidx.compose.ui.graphics.Color, selected: Boolean, onClick: () -> Unit) {
    val paper = LocalPaper.current
    androidx.compose.runtime.CompositionLocalProvider(
        LocalPaper provides paper.copy(paper = paper.field),
    ) {
        CategoryChip(label, dot, selected, onClick)
    }
}
