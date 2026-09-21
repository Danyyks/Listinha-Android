package com.listinha.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.listinha.app.R
import com.listinha.app.data.CATEGORIES
import com.listinha.app.data.Item
import kotlin.math.roundToInt

// ============================================================
//  EditItemDialog.kt  —  janela para editar um item existente
// ------------------------------------------------------------
//  Abre quando o usuário toca num item da lista. Vem com os
//  campos já preenchidos com os valores atuais do item.
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditItemDialog(
    item: Item,
    customCategories: List<String>,
    onSave: (name: String, category: String, price: Double, quantity: Double) -> Unit,
    onDismiss: () -> Unit,
) {
    // Campos começam com os valores atuais do item.
    var name by remember { mutableStateOf(item.name) }
    var category by remember { mutableStateOf(item.category) }
    // O modelo guarda a quantidade como Double, mas o seletor (− / +) trabalha
    // com inteiro. Usamos roundToInt (arredonda) em vez de toInt (que corta pra
    // baixo) para não perder valor à toa caso um item venha com fração.
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
        title = { Text("Editar item") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome") },
                    singleLine = true,
                )

                Spacer(Modifier.height(12.dp))
                Text("Categoria", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(4.dp))

                // Chips: categorias fixas + as personalizadas que já existem.
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(CATEGORIES) { cat ->
                        FilterChip(
                            selected = category == cat.key,
                            onClick = { category = cat.key },
                            label = { Text(cat.label) },
                        )
                    }
                    items(customCategories) { key ->
                        FilterChip(
                            selected = category == key,
                            onClick = { category = key },
                            label = { Text(key) },
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { if (quantity > 1) quantity-- }) {
                        Icon(painterResource(R.drawable.ic_remove), contentDescription = "Diminuir")
                    }
                    Text("$quantity", style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = { quantity++ }) {
                        Icon(painterResource(R.drawable.ic_add), contentDescription = "Aumentar")
                    }

                    Spacer(Modifier.width(12.dp))

                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text("Preço") },
                        prefix = { Text("R$ ") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.width(140.dp),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val nome = name.trim()
                if (nome.isNotEmpty()) {
                    val price = priceText.replace(",", ".").toDoubleOrNull() ?: 0.0
                    onSave(nome, category, price, quantity.toDouble())
                }
            }) { Text("Salvar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
    )
}
