package com.listinha.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.listinha.app.data.Item
import com.listinha.app.data.categoryColor
import com.listinha.app.data.categoryLabel

// ============================================================
//  CategoryGroup.kt  —  um grupo de itens dentro da notinha
// ------------------------------------------------------------
//  Título com a bolinha na cor da categoria e, abaixo, os itens.
// ============================================================

/**
 * @param first true no primeiro grupo, que não precisa de folga em cima
 * @param shopping repassado às linhas para o comportamento do modo compra
 */
@Composable
fun CategoryGroup(
    categoryKey: String,
    items: List<Item>,
    onToggle: (Item) -> Unit,
    onDelete: (Item) -> Unit,
    onEdit: (Item) -> Unit,
    first: Boolean,
    shopping: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(top = if (first) 0.dp else 14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Spacer(
                Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(categoryColor(categoryKey)),
            )
            Spacer(Modifier.width(8.dp))
            SectionLabel(categoryLabel(categoryKey))
        }
        items.forEach { item ->
            ShoppingItemRow(
                item = item,
                onToggle = { onToggle(item) },
                onDelete = { onDelete(item) },
                onEdit = { onEdit(item) },
                shopping = shopping,
            )
        }
    }
}
