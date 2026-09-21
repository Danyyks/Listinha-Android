package com.listinha.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.listinha.app.R
import com.listinha.app.data.Item
import com.listinha.app.util.formatCurrency

// ============================================================
//  ShoppingItemRow.kt  —  uma linha da lista (um item)
// ------------------------------------------------------------
//  Mostra: uma caixinha de marcar (comprado), o nome, a
//  quantidade, o preço total do item e um botão de apagar.
//  Espelha o ShoppingItem.jsx do React.
// ============================================================

@Composable
fun ShoppingItemRow(
    item: Item,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Caixinha de "comprado". Marcar/desmarcar chama onToggle.
        Checkbox(
            checked = item.bought,
            onCheckedChange = { onToggle() },
        )

        // Nome + quantidade, ocupando o espaço do meio (weight = 1f).
        // Tocar nessa área abre a edição do item.
        Column(
            Modifier
                .weight(1f)
                .clickable { onEdit() }
                .padding(vertical = 4.dp),
        ) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (item.bought) FontWeight.Normal else FontWeight.SemiBold,
                // Se comprado, risca o texto (efeito "riscado da lista").
                textDecoration = if (item.bought) TextDecoration.LineThrough else null,
                color = if (item.bought)
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                else
                    MaterialTheme.colorScheme.onSurface,
            )
            // Só mostra "x unidades" quando for mais de 1.
            if (item.quantity > 1) {
                Text(
                    text = "${item.quantity.toInt()} unidades",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }
        }

        // Preço total do item (preço x quantidade), quando houver preço.
        if (item.price > 0) {
            Text(
                text = formatCurrency(item.total),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        // Botão de apagar o item.
        IconButton(onClick = onDelete) {
            Icon(
                painter = painterResource(R.drawable.ic_delete),
                contentDescription = "Apagar ${item.name}",
                tint = MaterialTheme.colorScheme.error,
            )
        }
    }
}
