package com.listinha.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.listinha.app.R
import com.listinha.app.data.Item
import com.listinha.app.data.categoryByKey
import com.listinha.app.data.sortCategoryKeys
import com.listinha.app.ui.components.AddItemForm
import com.listinha.app.ui.components.BrandAlert
import com.listinha.app.ui.components.BrandTitle
import com.listinha.app.ui.components.BudgetSummary
import com.listinha.app.ui.components.CategoryGroup
import com.listinha.app.ui.components.EditItemDialog
import com.listinha.app.ui.components.EmptyArt
import com.listinha.app.ui.components.FilledPill
import com.listinha.app.ui.components.OutlinePill
import com.listinha.app.ui.components.PaperTicket
import com.listinha.app.ui.components.ShoppingModeBanner
import com.listinha.app.ui.theme.Area
import com.listinha.app.ui.theme.AreaTheme
import com.listinha.app.ui.theme.LocalFlavor

// ============================================================
//  ListScreen.kt  —  a tela principal: a lista de compras
// ------------------------------------------------------------
//  A lista é uma notinha de papel picotada sobre o fundo manteiga.
//  No modo compra, a tela muda para o pistache e mostra o próximo
//  item em destaque. Quem guarda o modo compra é a HomeScreen,
//  porque a barra de abas também muda de cor com ele.
// ============================================================

@Composable
fun ListScreen(
    userName: String?,
    items: List<Item>,
    priceHistory: Map<String, Double>,
    budget: Double?,
    shoppingMode: Boolean,
    onShoppingModeChange: (Boolean) -> Unit,
    onAddItem: (name: String, category: String, price: Double, quantity: Double) -> Unit,
    onToggle: (Item) -> Unit,
    onDelete: (Item) -> Unit,
    onEditItem: (id: String, name: String, category: String, price: Double, quantity: Double) -> Unit,
    onSetBudget: (Double?) -> Unit,
    onClearBought: () -> Unit,
    onClearAll: () -> Unit,
    onOpenRecipes: () -> Unit,
) {
    var showClearAllDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<Item?>(null) }

    val total = items.sumOf { it.total }
    val boughtCount = items.count { it.bought }
    val grouped = items.groupBy { it.category }
    val sortedKeys = sortCategoryKeys(grouped.keys)
    // Sem itens não há o que comprar, mesmo que o modo tenha ficado ligado.
    val shopping = shoppingMode && items.isNotEmpty()
    val customCategories = items.map { it.category }
        .filter { categoryByKey(it) == null }
        .distinct()

    AreaTheme(if (shopping) Area.MERCADO else Area.LISTA) {
        val flavor = LocalFlavor.current
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(flavor.bg)
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BrandTitle(
                    if (shopping) "No *mercado*" else "O que *falta*",
                    fontSize = 44.sp,
                    modifier = Modifier.weight(1f),
                )
                if (items.isNotEmpty()) {
                    Spacer(Modifier.width(12.dp))
                    FilledPill(
                        text = if (shopping) "Concluir" else "Comprar",
                        onClick = { onShoppingModeChange(!shopping) },
                        icon = if (shopping) R.drawable.ic_check else R.drawable.ic_cart,
                        height = 48.dp,
                    )
                }
            }

            if (shopping) {
                ShoppingModeBanner(
                    userName = userName,
                    boughtCount = boughtCount,
                    totalCount = items.size,
                    nextItemName = items.firstOrNull { !it.bought }?.name,
                )
            } else {
                AddItemForm(
                    onAddItem = onAddItem,
                    customCategories = customCategories,
                    priceHistory = priceHistory,
                )
            }

            if (items.isEmpty()) {
                EmptyList(onOpenRecipes)
            } else {
                PaperTicket(Modifier.fillMaxWidth()) {
                    sortedKeys.forEachIndexed { indice, categoryKey ->
                        CategoryGroup(
                            categoryKey = categoryKey,
                            items = grouped[categoryKey].orEmpty(),
                            onToggle = onToggle,
                            onDelete = onDelete,
                            onEdit = { editingItem = it },
                            first = indice == 0,
                            shopping = shopping,
                        )
                    }
                    BudgetSummary(
                        total = total,
                        budget = budget,
                        itemCount = items.size,
                        boughtCount = boughtCount,
                        onSetBudget = onSetBudget,
                    )
                }

                // Limpar e recomeçar não aparecem no corredor do mercado.
                if (!shopping) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (boughtCount > 0) {
                            OutlinePill("Limpar comprados", onClearBought, Modifier.weight(1f))
                        }
                        OutlinePill("Nova lista", { showClearAllDialog = true }, Modifier.weight(1f))
                    }
                }
            }
        }

        if (showClearAllDialog) {
            BrandAlert(
                title = "Apagar toda a *lista?*",
                text = "Isso remove todos os itens. Essa ação não pode ser desfeita.",
                confirmLabel = "Apagar",
                danger = true,
                onConfirm = {
                    onClearAll()
                    showClearAllDialog = false
                },
                onDismiss = { showClearAllDialog = false },
            )
        }

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
}

// Estado vazio: foto e notinha inclinadas, um convite e o atalho para as receitas.
@Composable
private fun EmptyList(onOpenRecipes: () -> Unit) {
    val flavor = LocalFlavor.current
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        EmptyArt("sua *lista*", R.drawable.empty_cookies)
        BrandTitle("Lista *limpinha.*", fontSize = 38.sp)
        Text(
            "Escreva o primeiro item ali em cima ou mande os ingredientes de uma receita.",
            fontSize = 16.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.SemiBold,
            color = flavor.secondary,
        )
        OutlinePill("Ver receitas", onOpenRecipes)
    }
}
