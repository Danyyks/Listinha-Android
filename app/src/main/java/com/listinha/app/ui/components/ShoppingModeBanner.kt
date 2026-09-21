package com.listinha.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// ============================================================
//  ShoppingModeBanner.kt  —  painel do "Modo de Compra"
// ------------------------------------------------------------
//  Aparece quando o usuário entra no modo de compra. Mostra o
//  progresso (quantos itens já pegou), o próximo item e uma
//  mensagem de incentivo. Espelha o banner do ListaCompras.jsx.
// ============================================================

@Composable
fun ShoppingModeBanner(
    userName: String?,
    boughtCount: Int,
    totalCount: Int,
    nextItemName: String?,
    modifier: Modifier = Modifier,
) {
    val firstName = userName?.split(" ")?.firstOrNull() ?: "você"
    val allDone = totalCount > 0 && boughtCount == totalCount
    val faltam = totalCount - boughtCount

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Column(Modifier.padding(16.dp)) {
            val onColor = MaterialTheme.colorScheme.onPrimaryContainer

            if (allDone) {
                // Tudo comprado: mensagem de comemoração.
                Text(
                    "Lista completa, $firstName! Arrasou nas compras!",
                    style = MaterialTheme.typography.titleMedium,
                    color = onColor,
                )
            } else {
                // Mensagem de incentivo, muda conforme o progresso.
                Text(
                    text = if (boughtCount == 0)
                        "Vamos lá, $firstName! Próximo item:"
                    else
                        "$firstName, faltam só $faltam ${if (faltam == 1) "item" else "itens"}!",
                    style = MaterialTheme.typography.titleMedium,
                    color = onColor,
                )

                // Próximo item a pegar.
                if (nextItemName != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        nextItemName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = onColor,
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Barra de progresso (fração de itens comprados).
                LinearProgressIndicator(
                    progress = { if (totalCount > 0) boughtCount.toFloat() / totalCount else 0f },
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(4.dp))
                Text(
                    "$boughtCount de $totalCount comprados",
                    style = MaterialTheme.typography.bodySmall,
                    color = onColor,
                )
            }
        }
    }
}
