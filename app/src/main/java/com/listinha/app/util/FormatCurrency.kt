package com.listinha.app.util

import java.text.NumberFormat
import java.util.Locale

// ============================================================
//  FormatCurrency.kt  —  formata números como dinheiro (R$)
// ------------------------------------------------------------
//  Faz o mesmo papel do seu formatCurrency.js: transforma
//  12.5 em "R$ 12,50", já no padrão brasileiro (vírgula decimal).
// ============================================================

// Locale = "conjunto de regras" de um país. pt-BR usa vírgula e "R$".
private val brl = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

fun formatCurrency(value: Double): String = brl.format(value)
