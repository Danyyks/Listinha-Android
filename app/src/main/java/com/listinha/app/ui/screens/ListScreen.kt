package com.listinha.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.listinha.app.R
import com.listinha.app.data.Item
import com.listinha.app.data.categoryByKey
import com.listinha.app.data.sortCategoryKeys
import com.listinha.app.ui.components.AddItemForm
import com.listinha.app.ui.components.BudgetSummary
import com.listinha.app.ui.components.CategoryGroup
import com.listinha.app.ui.components.EditItemDialog
import com.listinha.app.ui.components.ShoppingModeBanner
import com.listinha.app.ui.theme.FredokaFamily

// ============================================================
//  ListScreen.kt  —  a tela principal: a lista de compras
// ------------------------------------------------------------
//  Junta o formulário, os grupos por categoria e o total.
//  Espelha a página ListaCompras.jsx do React.
// ============================================================

@OptIn(ExperimentalMaterial3Api::class) // TopAppBar ainda é marcada como "experimental"
@Composable
fun ListScreen(
    userName: String?,
    items: List<Item>,
    priceHistory: Map<String, Double>,
    budget: Double?,
    onAddItem: (name: String, category: String, price: Double, quantity: Double) -> Unit,
    onToggle: (Item) -> Unit,
    onDelete: (Item) -> Unit,
    onEditItem: (id: String, name: String, category: String, price: Double, quantity: Double) -> Unit,
    onSetBudget: (Double?) -> Unit,
    onClearBought: () -> Unit,
    onClearAll: () -> Unit,
) {
    // Controla se a caixa de confirmação de "Nova lista" está aberta.
    var showClearAllDialog by remember { mutableStateOf(false) }
    // Item sendo editado no momento (null = nenhum diálogo de edição aberto).
    var editingItem by remember { mutableStateOf<Item?>(null) }
    // Modo de compra ligado? (rememberSaveable = sobrevive a girar a tela).
    var shoppingMode by rememberSaveable { mutableStateOf(false) }

    // --- Cálculos derivados da lista ---
    val total = items.sumOf { it.total }                 // soma de tudo
    val boughtCount = items.count { it.bought }           // quantos já foram pegos
    val grouped = items.groupBy { it.category }            // itens agrupados por categoria
    val sortedKeys = sortCategoryKeys(grouped.keys)        // categorias na ordem certa
    val firstName = userName?.split(" ")?.firstOrNull() ?: userName
    // Categorias personalizadas = as que aparecem nos itens mas não são fixas.
    val customCategories = items.map { it.category }
        .filter { categoryByKey(it) == null }
        .distinct()

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
                title = {
                    Column {
                        Text(
                            "Listinha",
                            fontFamily = FredokaFamily,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        // Saudação embaixo do nome.
                        if (firstName != null) {
                            Text(
                                "Olá, $firstName!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                actions = {
                    // Modo de compra como botão tonal evidente (só com itens).
                    if (items.isNotEmpty()) {
                        FilledTonalButton(
                            onClick = { shoppingMode = !shoppingMode },
                            modifier = Modifier.padding(end = 8.dp),
                        ) {
                            Icon(
                                painterResource(R.drawable.ic_cart),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(if (shoppingMode) "Concluir" else "Comprar")
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()), // deixa a tela rolar
        ) {
            Spacer(Modifier.height(8.dp))

            if (shoppingMode) {
                // No modo de compra: em vez do formulário, mostra o progresso.
                if (items.isNotEmpty()) {
                    ShoppingModeBanner(
                        userName = userName,
                        boughtCount = boughtCount,
                        totalCount = items.size,
                        nextItemName = items.firstOrNull { !it.bought }?.name,
                    )
                }
            } else {
                // Fora do modo de compra: formulário de adicionar item.
                AddItemForm(
                    onAddItem = onAddItem,
                    customCategories = customCategories,
                    priceHistory = priceHistory,
                )
            }

            Spacer(Modifier.height(8.dp))

            if (items.isEmpty()) {
                // Lista vazia: mensagem simpática.
                Text(
                    text = if (firstName != null)
                        "$firstName, sua lista está vazia.\nAdicione o primeiro item acima!"
                    else
                        "Sua lista está vazia.\nAdicione o primeiro item acima!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                )
            } else {
                // Um grupo para cada categoria, na ordem definida.
                sortedKeys.forEach { categoryKey ->
                    CategoryGroup(
                        categoryKey = categoryKey,
                        items = grouped[categoryKey].orEmpty(),
                        onToggle = onToggle,
                        onDelete = onDelete,
                        onEdit = { editingItem = it },
                    )
                }

                Spacer(Modifier.height(8.dp))

                // Resumo de total + orçamento (fica vermelho se estourar).
                BudgetSummary(
                    total = total,
                    budget = budget,
                    itemCount = items.size,
                    boughtCount = boughtCount,
                    onSetBudget = onSetBudget,
                )

                Spacer(Modifier.height(12.dp))

                // Botões de limpar (só aparecem quando fazem sentido).
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (boughtCount > 0) {
                        OutlinedButton(
                            onClick = onClearBought,
                            modifier = Modifier.weight(1f),
                            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                        ) {
                            Text("Limpar comprados")
                        }
                    }
                    OutlinedButton(
                        onClick = { showClearAllDialog = true },
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                    ) {
                        Text("Nova lista")
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    // Caixa de confirmação antes de apagar tudo (ação sem volta).
    if (showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllDialog = false },
            title = { Text("Apagar toda a lista?") },
            text = { Text("Isso remove todos os itens. Essa ação não pode ser desfeita.") },
            confirmButton = {
                TextButton(onClick = {
                    onClearAll()
                    showClearAllDialog = false
                }) { Text("Apagar") }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllDialog = false }) { Text("Cancelar") }
            },
        )
    }

    // Diálogo de edição: aparece quando um item foi tocado.
    editingItem?.let { item ->
        EditItemDialog(
            item = item,
            customCategories = customCategories,
            onSave = { name, category, price, quantity ->
                onEditItem(item.id, name, category, price, quantity)
                editingItem = null
            },
            onDismiss = { editingItem = null },
        )
    }
}
