package com.listinha.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ============================================================
//  Theme.kt  —  o tema que embrulha o app inteiro
// ------------------------------------------------------------
//  ListinhaTheme define claro ou escuro. AreaTheme troca o sabor
//  dentro de uma tela: como ele também monta o colorScheme do
//  Material, diálogos, campos e botões já nascem na cor da área.
// ============================================================

/** Indica se o tema escuro está ativo; o tema do app pode diferir do tema do aparelho. */
val LocalDarkTheme = compositionLocalOf { false }

/** Cores neutras do papel para a composição atual. */
val LocalPaper = compositionLocalOf { LightPaper }

/** Sabor da área atual. */
val LocalFlavor = compositionLocalOf { flavorOf(Area.LISTA, false) }

// Traduz sabor + papel para os papéis que o Material 3 espera.
private fun schemeFor(flavor: Flavor, paper: Paper, dark: Boolean): ColorScheme {
    // O destaque sobre o papel precisa de um texto que contraste com ele:
    // no açaí claro o destaque é uva (escuro), então o texto vira papel.
    val onAccent = if (flavor.titleOnPaper == flavor.title) flavor.on else paper.paper
    return (if (dark) darkColorScheme() else lightColorScheme()).copy(
        primary = flavor.titleOnPaper,
        onPrimary = onAccent,
        primaryContainer = flavor.soft,
        onPrimaryContainer = flavor.text,
        secondary = flavor.titleOnPaper,
        onSecondary = onAccent,
        secondaryContainer = paper.field,
        onSecondaryContainer = paper.ink,
        tertiary = flavor.titleOnPaper,
        onTertiary = onAccent,
        background = flavor.bg,
        onBackground = flavor.text,
        surface = paper.paper,
        onSurface = paper.ink,
        surfaceVariant = paper.field,
        onSurfaceVariant = paper.muted,
        surfaceContainer = paper.paper,
        surfaceContainerLow = paper.paper,
        surfaceContainerLowest = paper.paper,
        surfaceContainerHigh = paper.paper,
        surfaceContainerHighest = paper.field,
        surfaceTint = Color.Transparent,
        inverseSurface = TostBg,
        inverseOnSurface = TostText,
        outline = paper.line,
        outlineVariant = paper.line,
        error = paper.danger,
        onError = paper.onDanger,
    )
}

/** Tema raiz do app: define claro ou escuro e começa no sabor manteiga. */
@Composable
fun ListinhaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalDarkTheme provides darkTheme,
        LocalPaper provides if (darkTheme) DarkPaper else LightPaper,
    ) {
        AreaTheme(Area.LISTA, content = content)
    }
}

/**
 * Aplica o sabor de uma área a tudo o que estiver dentro de [content].
 *
 * @param lightStatusIcons força a cor dos ícones da barra de status, para telas
 * em que uma foto fica atrás deles; null decide pela cor do fundo do sabor.
 */
@Composable
fun AreaTheme(
    area: Area,
    lightStatusIcons: Boolean? = null,
    content: @Composable () -> Unit,
) {
    val dark = LocalDarkTheme.current
    val paper = LocalPaper.current
    val flavor = remember(area, dark) { flavorOf(area, dark) }
    val scheme = remember(flavor, paper, dark) { schemeFor(flavor, paper, dark) }

    // A hora e a bateria acompanham o fundo da área, não o tema do aparelho:
    // no Perfil açaí, por exemplo, precisam ficar claras mesmo no tema claro.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            val insets = WindowCompat.getInsetsController(window, view)
            insets.isAppearanceLightStatusBars = lightStatusIcons?.not() ?: (flavor.bg.luminance() > 0.5f)
            insets.isAppearanceLightNavigationBars = paper.paper.luminance() > 0.5f
        }
    }

    CompositionLocalProvider(LocalFlavor provides flavor) {
        MaterialTheme(colorScheme = scheme, typography = Typography, content = content)
    }
}
