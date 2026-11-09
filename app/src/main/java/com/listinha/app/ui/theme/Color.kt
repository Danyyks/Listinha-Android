package com.listinha.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

// ============================================================
//  Color.kt  —  os SABORES do Listinha (Estilo Dany)
// ------------------------------------------------------------
//  Cada área do app tem um par chapado de fundo + título, com
//  nome de comida: manteiga, pistache, morango, mirtilo, açaí e
//  noite. Por cima dos sabores fica o "papel" da notinha, que é
//  o mesmo em todas as áreas. Tudo tem versão clara e escura.
// ============================================================

/** As áreas do app; cada uma usa um sabor. */
enum class Area { LISTA, MERCADO, RECEITAS, NOVA_RECEITA, ACAI, COZINHAR }

/**
 * Um sabor: as cores de uma área.
 *
 * @property bg fundo chapado da tela
 * @property title título e botões sobre o fundo
 * @property titleOnPaper cor de destaque quando o elemento fica sobre o papel
 * @property secondary texto de apoio sobre o fundo
 * @property text texto de leitura sobre o fundo
 * @property on texto e ícone sobre um botão da cor [title]
 * @property soft tom suave para chips e superfícies leves
 */
@Immutable
data class Flavor(
    val bg: Color,
    val title: Color,
    val titleOnPaper: Color,
    val secondary: Color,
    val text: Color,
    val on: Color,
    val soft: Color,
)

/** O papel da notinha e as cores neutras que acompanham todas as áreas. */
@Immutable
data class Paper(
    val paper: Color,
    val ink: Color,
    val muted: Color,
    val line: Color,
    val field: Color,
    val shadow: Color,
    val brand: Color,
    val danger: Color,
    val onDanger: Color,
    val ok: Color,
    val bad: Color,
)

// Cores fixas da marca, iguais em qualquer tema (ícone, boas-vindas).
val Acai = Color(0xFF2B0B3A)
val AcaiRose = Color(0xFFFFC4E4)
val AcaiMuted = Color(0xFFD9B8CF)
val Uva = Color(0xFF3A1670)
val Creme = Color(0xFFFFFDF6)
val TostText = Color(0xFFFFF6E5)
val TostBg = Color(0xFF17121E)

private fun c(hex: Long) = Color(hex or 0xFF000000)

val LightPaper = Paper(
    paper = c(0xFFFDF6), ink = c(0x2B2233), muted = c(0x6B6178), line = c(0xE3D9C6), field = c(0xF3EDE1),
    shadow = Color(0x29462A0A), brand = c(0x3A1670), danger = c(0xB3261E), onDanger = c(0xFFFDF6),
    ok = c(0x2E6B2A), bad = c(0xB3261E),
)

val DarkPaper = Paper(
    paper = c(0x2A2430), ink = c(0xF3ECE2), muted = c(0xBDB2C6), line = c(0x463C50), field = c(0x352D3C),
    shadow = Color(0x73000000), brand = c(0xFFC4E4), danger = c(0xF2B8B5), onDanger = c(0x2A0A08),
    ok = c(0xA8E0A0), bad = c(0xFFB4A8),
)

private val LightFlavors = mapOf(
    Area.LISTA to Flavor(c(0xFFDE85), c(0x3A1670), c(0x3A1670), c(0x5B4A1E), c(0x2A1050), c(0xFFFDF6), c(0xFFEDB8)),
    Area.MERCADO to Flavor(c(0xDDEBA8), c(0x1F3D14), c(0x1F3D14), c(0x3F5530), c(0x1F3D14), c(0xFFFDF6), c(0xEEF5D3)),
    Area.RECEITAS to Flavor(c(0xFFD0D8), c(0xC81E12), c(0xC81E12), c(0x7A2A30), c(0x4A0D0A), c(0xFFFDF6), c(0xFFE6EA)),
    Area.NOVA_RECEITA to Flavor(c(0xCFDBFF), c(0x16246E), c(0x16246E), c(0x3A4580), c(0x16246E), c(0xFFFDF6), c(0xE6ECFF)),
    Area.ACAI to Flavor(c(0x2B0B3A), c(0xFFC4E4), c(0x3A1670), c(0xD9B8CF), c(0xFFE3F1), c(0x2B0B3A), c(0x43205A)),
    Area.COZINHAR to Flavor(c(0x17121E), c(0xFFC49B), c(0xFFC49B), c(0xC9BFD2), c(0xF3ECE2), c(0x17121E), c(0x3A3142)),
)

private val DarkFlavors = mapOf(
    Area.LISTA to Flavor(c(0x2A2110), c(0xFFDE85), c(0xFFDE85), c(0xD9CBA6), c(0xFFF1C7), c(0x2A2110), c(0x3D3118)),
    Area.MERCADO to Flavor(c(0x162110), c(0xDDEBA8), c(0xDDEBA8), c(0xB9C7A0), c(0xEEF5D3), c(0x162110), c(0x24331B)),
    Area.RECEITAS to Flavor(c(0x2E0F15), c(0xFFB8C4), c(0xFFB8C4), c(0xE3B9C0), c(0xFFE1E6), c(0x2E0F15), c(0x4A1A23)),
    Area.NOVA_RECEITA to Flavor(c(0x0F1534), c(0xCFDBFF), c(0xCFDBFF), c(0xAEB9DD), c(0xE6ECFF), c(0x0F1534), c(0x1C2550)),
    Area.ACAI to Flavor(c(0x170A1F), c(0xFFC4E4), c(0xFFC4E4), c(0xD9B8CF), c(0xFFE3F1), c(0x170A1F), c(0x3A1A4A)),
    Area.COZINHAR to Flavor(c(0x17121E), c(0xFFC49B), c(0xFFC49B), c(0xC9BFD2), c(0xF3ECE2), c(0x17121E), c(0x3A3142)),
)

/** Devolve o sabor de uma área no tema pedido. */
fun flavorOf(area: Area, dark: Boolean): Flavor =
    (if (dark) DarkFlavors else LightFlavors).getValue(area)
