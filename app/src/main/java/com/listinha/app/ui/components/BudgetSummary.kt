package com.listinha.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.listinha.app.ui.theme.FredokaFamily
import com.listinha.app.ui.theme.LocalFlavor
import com.listinha.app.ui.theme.LocalPaper
import com.listinha.app.util.formatCurrency

// ============================================================
//  BudgetSummary.kt  —  o rodapé de cupom da notinha
// ------------------------------------------------------------
//  Total em letras grandes e, se houver orçamento, uma barra de
//  quanto já foi gasto, verde enquanto couber e vermelha ao passar.
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
    val paper = LocalPaper.current
    val flavor = LocalFlavor.current
    var showSheet by remember { mutableStateOf(false) }
    var budgetText by remember { mutableStateOf("") }

    val overBudget = budget != null && total > budget
    val corOrcamento = if (overBudget) paper.bad else paper.ok

    Column(modifier = modifier.fillMaxWidth().padding(top = 14.dp)) {
        DashedDivider()
        Spacer(Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Column(Modifier.weight(1f)) {
                SectionLabel("Total")
                Text(
                    "$boughtCount de $itemCount ${if (itemCount == 1) "item marcado" else "itens marcados"}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = paper.muted,
                )
            }
            Spacer(Modifier.padding(horizontal = 6.dp))
            Text(
                formatCurrency(total),
                fontFamily = FredokaFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 40.sp,
                lineHeight = 40.sp,
                color = paper.ink,
                maxLines = 1,
            )
        }

        if (budget == null) {
            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .border(2.dp, paper.line, RoundedCornerShape(999.dp))
                    .clickable { budgetText = ""; showSheet = true },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "Definir orçamento",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = flavor.titleOnPaper,
                )
            }
        } else {
            val fracao = if (budget > 0) (total / budget).toFloat().coerceIn(0f, 1f) else 1f
            Spacer(Modifier.height(14.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(paper.field),
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(fracao)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(99.dp))
                        .background(corOrcamento),
                )
            }
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    if (overBudget) "Passou em ${formatCurrency(total - budget)}" else "Restante: ${formatCurrency(budget - total)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = corOrcamento,
                    modifier = Modifier.weight(1f),
                )
                Row(
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .clickable {
                            // Preenche com o valor atual, sem ",0" quando é inteiro.
                            budgetText = if (budget % 1.0 == 0.0) budget.toInt().toString() else budget.toString()
                            showSheet = true
                        },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("de ${formatCurrency(budget)} · ", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = paper.muted)
                    Text(
                        "Editar",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = flavor.titleOnPaper,
                        textDecoration = TextDecoration.Underline,
                    )
                }
            }
        }
    }

    if (showSheet) {
        BudgetSheet(
            value = budgetText,
            onValueChange = { budgetText = it },
            hasBudget = budget != null,
            onRemove = { onSetBudget(null); showSheet = false },
            onDismiss = { showSheet = false },
            onSave = {
                // Converte "12,50" em 12.5; valor vazio ou inválido remove o orçamento.
                onSetBudget(budgetText.replace(",", ".").toDoubleOrNull())
                showSheet = false
            },
        )
    }
}

// Folha que sobe de baixo para digitar o valor do orçamento.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BudgetSheet(
    value: String,
    onValueChange: (String) -> Unit,
    hasBudget: Boolean,
    onRemove: () -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
) {
    val paper = LocalPaper.current
    val flavor = LocalFlavor.current
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = paper.paper,
        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(start = 22.dp, end = 22.dp, bottom = 26.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            BrandTitle("Quanto dá pra *gastar?*", fontSize = 32.sp, color = paper.ink)
            BrandField(
                value = value,
                onValueChange = onValueChange,
                placeholder = "0,00",
                description = "Valor do orçamento",
                modifier = Modifier.fillMaxWidth(),
                height = 58.dp,
                fontSize = 20.sp,
                background = paper.field,
                shape = RoundedCornerShape(18.dp),
                prefix = "R$",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                if (hasBudget) {
                    TextButton(onClick = onRemove, modifier = Modifier.heightIn(min = 52.dp)) {
                        Text("Remover", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = paper.muted)
                    }
                }
                OutlinePill("Cancelar", onDismiss, Modifier.weight(1f), height = 52.dp, color = paper.ink)
                FilledPill(
                    "Salvar",
                    onSave,
                    Modifier.weight(1f),
                    height = 52.dp,
                    container = flavor.titleOnPaper,
                    contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}
