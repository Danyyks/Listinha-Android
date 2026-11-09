package com.listinha.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.listinha.app.R
import com.listinha.app.data.CATEGORIES
import com.listinha.app.data.categoryByKey
import com.listinha.app.data.categoryColor
import com.listinha.app.ui.theme.LocalFlavor
import com.listinha.app.ui.theme.LocalPaper
import com.listinha.app.util.formatCurrency

// ============================================================
//  AddItemForm.kt  —  adicionar item à lista
// ------------------------------------------------------------
//  Só o campo e o botão "+" enquanto ninguém digita. Categoria,
//  quantidade e preço aparecem quando o nome começa a ser escrito:
//  a tela fica limpa e quem tem pressa só digita e confirma.
// ============================================================

@Composable
fun AddItemForm(
    onAddItem: (name: String, category: String, price: Double, quantity: Double) -> Unit,
    customCategories: List<String> = emptyList(),
    priceHistory: Map<String, Double> = emptyMap(),
    modifier: Modifier = Modifier,
) {
    val paper = LocalPaper.current
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("outros") }
    var quantity by remember { mutableIntStateOf(1) }
    var priceText by remember { mutableStateOf("") }

    // Confirma o item e limpa o formulário; nome em branco não é adicionado.
    fun enviar() {
        val nome = name.trim()
        if (nome.isEmpty()) return
        val price = priceText.replace(",", ".").toDoubleOrNull() ?: 0.0
        onAddItem(nome, category, price, quantity.toDouble())
        name = ""
        priceText = ""
        quantity = 1
        category = "outros"
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            BrandField(
                value = name,
                onValueChange = { name = it },
                placeholder = "Adicionar item",
                description = "Novo item",
                modifier = Modifier.weight(1f),
                shadow = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { enviar() }),
            )
            RoundIconButton(
                icon = R.drawable.ic_add,
                description = "Adicionar à lista",
                onClick = { enviar() },
                size = 56.dp,
                enabled = name.isNotBlank(),
            )
        }

        if (name.isNotEmpty()) {
            val localCustoms = remember { mutableStateListOf<String>() }
            val allCustoms = (customCategories + localCustoms).distinct()
            var showNewCategory by remember { mutableStateOf(false) }
            var newCategoryText by remember { mutableStateOf("") }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 2.dp),
            ) {
                items(CATEGORIES) { cat ->
                    CategoryChip(cat.label, categoryColor(cat.key), selected = category == cat.key) { category = cat.key }
                }
                items(allCustoms) { key ->
                    CategoryChip(key, categoryColor(key), selected = category == key) { category = key }
                }
                item { CategoryChip("+ Nova", null, selected = false) { showNewCategory = true } }
            }

            if (showNewCategory) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    BrandField(
                        value = newCategoryText,
                        onValueChange = { newCategoryText = it },
                        placeholder = "Nome da categoria",
                        description = "Nome da nova categoria",
                        modifier = Modifier.weight(1f),
                        height = 48.dp,
                        fontSize = 16.sp,
                    )
                    RoundIconButton(
                        icon = R.drawable.ic_add,
                        description = "Criar categoria",
                        size = 48.dp,
                        iconSize = 20.dp,
                        onClick = {
                            val nova = newCategoryText.trim()
                            if (nova.isNotEmpty()) {
                                if (categoryByKey(nova) == null && !allCustoms.contains(nova)) localCustoms.add(nova)
                                category = nova
                            }
                            newCategoryText = ""
                            showNewCategory = false
                        },
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                // Seletor de quantidade.
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(paper.paper)
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = { if (quantity > 1) quantity-- }, modifier = Modifier.size(48.dp)) {
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
                    IconButton(onClick = { quantity++ }, modifier = Modifier.size(48.dp)) {
                        Icon(painterResource(R.drawable.ic_add), contentDescription = "Aumentar", tint = paper.ink, modifier = Modifier.size(18.dp))
                    }
                }
                BrandField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    placeholder = "Preço",
                    description = "Preço",
                    modifier = Modifier.weight(1f),
                    height = 48.dp,
                    fontSize = 16.sp,
                    prefix = "R$",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { enviar() }),
                )
            }

            // Sugere o último preço pago por este item.
            val suggested = priceHistory[name.trim().lowercase()]
            if (suggested != null && priceText.isBlank()) {
                val flavor = LocalFlavor.current
                Box(
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .clickable {
                            priceText = if (suggested % 1.0 == 0.0) suggested.toInt().toString() else suggested.toString()
                        }
                        .padding(horizontal = 4.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Text(
                        "Última vez: ${formatCurrency(suggested)} · usar",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = flavor.text,
                        textDecoration = TextDecoration.Underline,
                    )
                }
            }
        }
    }
}

// Chip de categoria em pílula, com a bolinha na cor da categoria.
@Composable
internal fun CategoryChip(label: String, dot: Color?, selected: Boolean, onClick: () -> Unit) {
    val flavor = LocalFlavor.current
    val paper = LocalPaper.current
    val forma = RoundedCornerShape(999.dp)
    Row(
        modifier = Modifier
            .heightIn(min = 40.dp)
            .clip(forma)
            .background(if (selected) flavor.title else paper.paper)
            .clickable { onClick() }
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (dot != null) {
            Spacer(Modifier.size(9.dp).clip(CircleShape).background(dot))
            Spacer(Modifier.width(7.dp))
        }
        Text(
            label,
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (selected) flavor.on else paper.ink,
        )
    }
}
