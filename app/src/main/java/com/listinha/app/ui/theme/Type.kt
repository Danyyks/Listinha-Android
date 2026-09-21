@file:OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)

package com.listinha.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.listinha.app.R

// ============================================================
//  Type.kt  —  as fontes do app
// ------------------------------------------------------------
//  Duas famílias (Google Fonts, gratuitas):
//   • Nunito  -> corpo e interface (super legível, cantos macios)
//   • Fredoka -> a marca "Listinha" e números em destaque
//
//  São fontes VARIÁVEIS: um único arquivo cobre vários pesos.
//  O FontVariation.weight() escolhe o "peso" (400=normal, 700=negrito).
// ============================================================

// Cria uma "instância" da fonte Nunito num peso específico.
private fun nunito(weight: Int) = Font(
    R.font.nunito,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

// Mesma coisa para a Fredoka.
private fun fredoka(weight: Int) = Font(
    R.font.fredoka,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

// A família Nunito com os pesos que usamos (normal, médio, semibold, negrito).
val NunitoFamily = FontFamily(nunito(400), nunito(500), nunito(600), nunito(700))

// A família Fredoka (para a marca e destaques).
val FredokaFamily = FontFamily(fredoka(500), fredoka(600), fredoka(700))

// Aplica uma família de fonte em TODOS os estilos de texto do Material 3.
private fun Typography.withFamily(f: FontFamily) = Typography(
    displayLarge = displayLarge.copy(fontFamily = f),
    displayMedium = displayMedium.copy(fontFamily = f),
    displaySmall = displaySmall.copy(fontFamily = f),
    headlineLarge = headlineLarge.copy(fontFamily = f),
    headlineMedium = headlineMedium.copy(fontFamily = f),
    headlineSmall = headlineSmall.copy(fontFamily = f),
    titleLarge = titleLarge.copy(fontFamily = f),
    titleMedium = titleMedium.copy(fontFamily = f),
    titleSmall = titleSmall.copy(fontFamily = f),
    bodyLarge = bodyLarge.copy(fontFamily = f),
    bodyMedium = bodyMedium.copy(fontFamily = f),
    bodySmall = bodySmall.copy(fontFamily = f),
    labelLarge = labelLarge.copy(fontFamily = f),
    labelMedium = labelMedium.copy(fontFamily = f),
    labelSmall = labelSmall.copy(fontFamily = f),
)

// A tipografia do app: tudo em Nunito por padrão.
val Typography = Typography().withFamily(NunitoFamily)
