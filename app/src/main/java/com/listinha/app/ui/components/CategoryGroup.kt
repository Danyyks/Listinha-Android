package com.listinha.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.listinha.app.data.Item
import com.listinha.app.data.categoryColor
import com.listinha.app.data.categoryLabel

// ============================================================
//  CategoryGroup.kt  —  um grupo de itens da mesma categoria
// ------------------------------------------------------------
//  Mostra um título com uma bolinha colorida (a cor da categoria)
//  e, abaixo, todos os itens daquele grupo. Espelha o
//  CategoryGroup.jsx do React.
// ============================================================

@Composable
fun CategoryGroup(
    categoryKey: String,
    items: List<Item>,
    onToggle: (Item) -> Unit,
    onDelete: (Item) -> Unit,
    onEdit: (Item) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(vertical = 8.dp)) {

        // Cabeçalho do grupo: bolinha colorida + nome da categoria.
        Row(verticalAlignment = Alignment.CenterVertically) {
            // A "bolinha": um quadradinho pequeno recortado em círculo,
            // pintado com a cor da categoria.
            Spacer(
                Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(categoryColor(categoryKey)),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = categoryLabel(categoryKey),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        // Os itens do grupo, um embaixo do outro.
        items.forEach { item ->
            ShoppingItemRow(
                item = item,
                onToggle = { onToggle(item) },
                onDelete = { onDelete(item) },
                onEdit = { onEdit(item) },
            )
        }
    }
}
