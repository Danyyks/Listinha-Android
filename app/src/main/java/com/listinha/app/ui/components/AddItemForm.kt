package com.listinha.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.listinha.app.R
import com.listinha.app.data.CATEGORIES
import com.listinha.app.data.categoryByKey
import com.listinha.app.util.formatCurrency

// ============================================================
//  AddItemForm.kt  —  o formulário "Novo item" (redesenhado)
// ------------------------------------------------------------
//  Card branco arredondado, categorias em "pílulas", campos com
//  cantos redondos e botão preenchido. Espelha o Listinha Redesign.
// ============================================================

@Composable
fun AddItemForm(
    onAddItem: (name: String, category: String, price: Double, quantity: Double) -> Unit,
    customCategories: List<String> = emptyList(),
    priceHistory: Map<String, Double> = emptyMap(),
    modifier: Modifier = Modifier,
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("outros") }
    var quantity by remember { mutableIntStateOf(1) }
    var priceText by remember { mutableStateOf("") }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(16.dp)) {

            Text(
                "Novo item",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nome do produto") },
                placeholder = { Text("Ex: Leite, Arroz...") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(12.dp))
            Text(
                "Categoria",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(6.dp))

            // Categorias personalizadas (as que já existem + as recém-criadas).
            val localCustoms = remember { mutableStateListOf<String>() }
            val allCustoms = (customCategories + localCustoms).distinct()
            var showNewCategory by remember { mutableStateOf(false) }
            var newCategoryText by remember { mutableStateOf("") }

            // Chips em "pílula": fixas, depois personalizadas, e "+ Nova".
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(CATEGORIES) { cat ->
                    CategoryChip(cat.label, selected = category == cat.key) { category = cat.key }
                }
                items(allCustoms) { key ->
                    CategoryChip(key, selected = category == key) { category = key }
                }
                item {
                    CategoryChip("+ Nova", selected = false) { showNewCategory = true }
                }
            }

            // Campo para criar categoria nova.
            if (showNewCategory) {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newCategoryText,
                        onValueChange = { newCategoryText = it },
                        label = { Text("Nome da nova categoria") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f),
                    )
                    StepBox(R.drawable.ic_add) {
                        val nova = newCategoryText.trim()
                        if (nova.isNotEmpty()) {
                            if (categoryByKey(nova) == null && !allCustoms.contains(nova)) {
                                localCustoms.add(nova)
                            }
                            category = nova
                        }
                        newCategoryText = ""
                        showNewCategory = false
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Quantidade (em caixas arredondadas) + preço.
            Row(verticalAlignment = Alignment.CenterVertically) {
                StepBox(R.drawable.ic_remove) { if (quantity > 1) quantity-- }
                Spacer(Modifier.width(12.dp))
                Text(
                    "$quantity",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.width(12.dp))
                StepBox(R.drawable.ic_add) { quantity++ }

                Spacer(Modifier.width(14.dp))

                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("Preço") },
                    prefix = { Text("R$ ") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                )
            }

            // Sugestão de último preço.
            val suggested = priceHistory[name.trim().lowercase()]
            if (suggested != null && priceText.isBlank()) {
                TextButton(onClick = {
                    priceText = if (suggested % 1.0 == 0.0) suggested.toInt().toString() else suggested.toString()
                }) {
                    Text("Última vez: ${formatCurrency(suggested)} — usar")
                }
            }

            Spacer(Modifier.height(14.dp))

            Button(
                onClick = {
                    val nome = name.trim()
                    if (nome.isEmpty()) return@Button
                    val price = priceText.replace(",", ".").toDoubleOrNull() ?: 0.0
                    onAddItem(nome, category, price, quantity.toDouble())
                    name = ""
                    priceText = ""
                    quantity = 1
                    category = "outros"
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(15.dp),
            ) {
                Icon(painterResource(R.drawable.ic_add), contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Adicionar à lista", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// Um chip de categoria em formato de "pílula".
@Composable
private fun CategoryChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    val fg = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    var chip = Modifier
        .clip(RoundedCornerShape(50))
    if (!selected) {
        chip = chip.border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(50))
    }
    Box(
        modifier = chip
            .background(bg)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = fg)
    }
}

// Botão quadrado arredondado do seletor de quantidade (− e +).
@Composable
private fun StepBox(iconRes: Int, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painterResource(iconRes),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp),
        )
    }
}
