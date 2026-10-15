package com.listinha.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.listinha.app.R

// ============================================================
//  Type.kt  —  as fontes do app
// ------------------------------------------------------------
//  Duas famílias (Google Fonts, gratuitas):
//   • Nunito  -> corpo e interface (super legível, cantos macios)
//   • Fredoka -> a marca "Listinha", títulos e nomes de receita
//
//  Um arquivo POR PESO (res/font/nunito_bold.ttf etc.). Antes era
//  um arquivo "variável" só, mas o Android ignorava o peso pedido e
//  desenhava tudo no peso mais fino do arquivo (Nunito 200, Fredoka
//  300). Os arquivos fixos foram gerados a partir dos variáveis com
//  o fontTools (instancer), então o desenho das letras é o mesmo.
// ============================================================

// A família Nunito com os pesos que usamos.
val NunitoFamily = FontFamily(
    Font(R.font.nunito_regular, FontWeight.Normal),     // 400
    Font(R.font.nunito_medium, FontWeight.Medium),      // 500
    Font(R.font.nunito_semibold, FontWeight.SemiBold),  // 600
    Font(R.font.nunito_bold, FontWeight.Bold),          // 700
    Font(R.font.nunito_extrabold, FontWeight.ExtraBold), // 800
)

// A família Fredoka (para a marca e destaques).
val FredokaFamily = FontFamily(
    Font(R.font.fredoka_medium, FontWeight.Medium),     // 500
    Font(R.font.fredoka_semibold, FontWeight.SemiBold), // 600
    Font(R.font.fredoka_bold, FontWeight.Bold),         // 700
)

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
