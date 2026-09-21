package com.listinha.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.listinha.app.R
import com.listinha.app.ui.theme.FredokaFamily
import com.listinha.app.util.formatCurrency

// ============================================================
//  BudgetSummary.kt  —  o resumo de gastos e orçamento
// ------------------------------------------------------------
//  Mostra o total da lista e, se houver orçamento, quanto ainda
//  sobra — ou o aviso de que passou do limite (fica vermelho).
//  Espelha o BudgetSummary.jsx do React.
// ============================================================

@Composable
fun BudgetSummary(
    total: Double,
    budget: Double?,
    itemCount: Int,
    boughtCount: Int,
    onSetBudget: (Double?) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Controla a janelinha de definir/editar o orçamento.
    var showDialog by remember { mutableStateOf(false) }
    var budgetText by remember { mutableStateOf("") }

    // Passou do orçamento?
    val overBudget = budget != null && total > budget

    // Cores: normal usa o roxo do tema; estourado fica avermelhado.
    val containerColor = if (overBudget)
        MaterialTheme.colorScheme.errorContainer
    else
        MaterialTheme.colorScheme.primaryContainer
    val contentColor = if (overBudget)
        MaterialTheme.colorScheme.onErrorContainer
    else
        MaterialTheme.colorScheme.onPrimaryContainer

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
    ) {
        Column(Modifier.padding(16.dp)) {
            // Total da lista.
            Text(
                "Total da lista",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = contentColor,
            )
            Text(
                formatCurrency(total),
                style = MaterialTheme.typography.headlineMedium,
                fontFamily = FredokaFamily,
                fontWeight = FontWeight.Bold,
                color = contentColor,
            )
            Text(
                "$boughtCount de $itemCount itens marcados",
                style = MaterialTheme.typography.bodySmall,
                color = contentColor,
            )

            Spacer(Modifier.height(12.dp))

            if (budget == null) {
                // Sem orçamento ainda: botão PREENCHIDO bem evidente, com ícone "+".
                Button(
                    onClick = { budgetText = ""; showDialog = true },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Icon(
                        painterResource(R.drawable.ic_add),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Definir orçamento", fontWeight = FontWeight.SemiBold)
                }
            } else {
                // Com orçamento: mostra o valor e o restante (ou o estouro).
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            "Orçamento: ${formatCurrency(budget)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = contentColor,
                        )
                        if (overBudget) {
                            Text(
                                "Passou em ${formatCurrency(total - budget)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = contentColor,
                            )
                        } else {
                            Text(
                                "Restante: ${formatCurrency(budget - total)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = contentColor,
                            )
                        }
                    }
                    TextButton(onClick = {
                        // Prefill: mostra o valor atual (sem ",0" quando é inteiro).
                        budgetText = if (budget % 1.0 == 0.0) budget.toInt().toString() else budget.toString()
                        showDialog = true
                    }) {
                        Text("Editar")
                    }
                }
            }
        }
    }

    // Janelinha para digitar o valor do orçamento.
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(if (budget == null) "Definir orçamento" else "Editar orçamento") },
            text = {
                OutlinedTextField(
                    value = budgetText,
                    onValueChange = { budgetText = it },
                    label = { Text("Valor") },
                    prefix = { Text("R$ ") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    // Converte "12,50" -> 12.5. Se vier vazio/errado, vira null (sem orçamento).
                    val value = budgetText.replace(",", ".").toDoubleOrNull()
                    onSetBudget(value)
                    showDialog = false
                }) { Text("Salvar") }
            },
            dismissButton = {
                Row {
                    // Se já existe orçamento, oferece removê-lo.
                    if (budget != null) {
                        TextButton(onClick = {
                            onSetBudget(null)
                            showDialog = false
                        }) { Text("Remover") }
                    }
                    TextButton(onClick = { showDialog = false }) { Text("Cancelar") }
                }
            },
        )
    }
}
