package com.listinha.app.util

import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.roundToInt

// ============================================================
//  Cooking.kt  ===  contas da hora de cozinhar
// ------------------------------------------------------------
//  • scaleQuantity: "1/2 xícara" para 4 porções vira "1 xícara"
//    para 8 (o seletor de porções do detalhe da receita).
//  • detectTimerSeconds: acha "asse por 30 minutos" num passo
//    para o modo cozinhar oferecer o timer num toque.
// ============================================================

// Um número no começo da quantidade (mesmos formatos do RecipeText).
private val leadingNumber = Regex(
    """^(\d+\s+(?:e\s+|and\s+)?\d+/\d+|\d+/\d+|\d+[.,]\d+|\d+\s*[½¼¾⅓⅔]|\d+|[½¼¾⅓⅔]|meia|meio|half)""" +
        """(\s*(?:a|-|–|ou|to|or)\s*(?:\d+[.,]\d+|\d+/\d+|\d+))?""",
    RegexOption.IGNORE_CASE,
)

private val unicodeFractions = mapOf('½' to 0.5, '¼' to 0.25, '¾' to 0.75, '⅓' to 1.0 / 3, '⅔' to 2.0 / 3)

// "1 1/2" -> 1.5 | "1,5" -> 1.5 | "2½" -> 2.5 | "meia" -> 0.5
internal fun parseAmount(text: String): Double? {
    val t = text.trim().lowercase()
    if (t == "meia" || t == "meio" || t == "half") return 0.5
    unicodeFractions[t.lastOrNull()]?.let { frac ->
        val whole = t.dropLast(1).trim()
        return frac + (if (whole.isEmpty()) 0.0 else whole.toDoubleOrNull() ?: return null)
    }
    val mixed = Regex("""^(\d+)\s+(?:e\s+|and\s+)?(\d+)/(\d+)$""").find(t)
    if (mixed != null) {
        val (w, n, d) = mixed.destructured
        return w.toDouble() + n.toDouble() / d.toDouble().takeIf { it != 0.0 }!!
    }
    val frac = Regex("""^(\d+)/(\d+)$""").find(t)
    if (frac != null) {
        val (n, d) = frac.destructured
        if (d.toDouble() == 0.0) return null
        return n.toDouble() / d.toDouble()
    }
    return t.replace(',', '.').toDoubleOrNull()
}

// 1.5 -> "1 1/2" | 0.25 -> "1/4" | 2.0 -> "2" | 333.33 -> "333" | 1.2 -> "1,2"
// useFractions = false quando a pessoa escreveu em decimal ("1,25 kg" vira "2,5 kg").
internal fun formatAmount(
    value: Double,
    decimalSeparator: Char = defaultSeparator(),
    useFractions: Boolean = true,
): String {
    if (value >= 10) return value.roundToInt().toString()
    val whole = floor(value)
    val rest = value - whole
    if (rest < 0.05) return whole.toInt().toString()
    if (rest > 0.95) return (whole.toInt() + 1).toString()
    val fractions = listOf(0.25 to "1/4", 1.0 / 3 to "1/3", 0.5 to "1/2", 2.0 / 3 to "2/3", 0.75 to "3/4")
    if (useFractions) fractions.firstOrNull { abs(it.first - rest) < 0.04 }?.let { (_, label) ->
        return if (whole == 0.0) label else "${whole.toInt()} $label"
    }
    val oneDecimal = (value * 10).roundToInt() / 10.0
    return oneDecimal.toString().replace('.', decimalSeparator)
}

private fun defaultSeparator(): Char = DecimalFormatSymbols.getInstance(Locale.getDefault()).decimalSeparator

// Singular <-> plural das unidades mais comuns ("1 xícara" x "2 xícaras").
private val plurals = mapOf(
    "xícara" to "xícaras", "xicara" to "xicaras", "colher" to "colheres", "lata" to "latas",
    "dente" to "dentes", "unidade" to "unidades", "pitada" to "pitadas", "fatia" to "fatias",
    "pacote" to "pacotes", "caixa" to "caixas", "copo" to "copos", "folha" to "folhas",
    "cup" to "cups", "can" to "cans", "clove" to "cloves", "slice" to "slices",
)
private val singulars = plurals.entries.associate { (s, p) -> p to s }

private fun fixPlural(rest: String, value: Double): String {
    val trimmed = rest.trimStart()
    val word = trimmed.substringBefore(' ')
    val lower = word.lowercase()
    val replacement = when {
        value > 1 -> plurals[lower]
        else -> singulars[lower]
    } ?: return rest
    val spaces = rest.length - trimmed.length
    return " ".repeat(spaces) + replacement + trimmed.removePrefix(word)
}

// Multiplica a quantidade pelo fator (novas porções ÷ porções originais).
// Se não houver número (ex: "a gosto"), devolve igual.
fun scaleQuantity(quantity: String, factor: Double, decimalSeparator: Char = defaultSeparator()): String {
    if (abs(factor - 1.0) < 1e-9 || quantity.isBlank()) return quantity
    val m = leadingNumber.find(quantity.trim()) ?: return quantity
    val first = parseAmount(m.groupValues[1]) ?: return quantity
    val scaled = first * factor
    // Escreveu em decimal ("1,25")? Continua em decimal. Senão, usa frações ("1/2").
    val fractions = !Regex("""\d[.,]\d""").containsMatchIn(m.value)
    var result = formatAmount(scaled, decimalSeparator, fractions)

    // Faixa "2 a 3": escala as duas pontas.
    val range = m.groupValues[2]
    if (range.isNotBlank()) {
        val sep = Regex("""\s*(a|-|–|ou|to|or)\s*""", RegexOption.IGNORE_CASE).find(range)?.value ?: " a "
        val second = parseAmount(range.substring(range.indexOf(sep) + sep.length)) ?: return quantity
        result += sep + formatAmount(second * factor, decimalSeparator, fractions)
    }

    // "meia xícara" vira "1 xícara": troca o número e ajusta o plural.
    val rest = quantity.trim().substring(m.value.length)
    return (result + fixPlural(rest, scaled)).trim()
}

// ------------------------------------------------------------
//  Timer dos passos
// ------------------------------------------------------------

private val hoursRegex = Regex(
    """(\d+)\s*(?:h(?![a-z])|horas?|hours?|hrs?)(?:\s*(?:e|and)?\s*(\d+)\s*(?:min(?:utos?|utes?|s)?)?(?![a-z]))?""",
)
private val minutesRegex = Regex(
    """(\d+)(?:\s*(?:a|-|–|ou|to|or)\s*(\d+))?\s*min(?:utos?|utes?|s)?(?![a-z])""",
)
private val secondsRegex = Regex("""(\d+)\s*(?:segundos?|seconds?|seg|secs?)(?![a-z])""")

// Acha o tempo de um passo, em segundos ("asse por 30 a 35 min" -> 2100).
// Numa faixa usa o maior valor (melhor conferir antes do que queimar).
// Devolve null se o passo não falar de tempo.
fun detectTimerSeconds(step: String): Int? {
    val n = normalize(step)
    val candidates = mutableListOf<Pair<Int, Int>>() // (posição no texto, segundos)

    hoursRegex.find(n)?.let { m ->
        val h = m.groupValues[1].toInt()
        val min = m.groupValues[2].toIntOrNull() ?: 0
        candidates += m.range.first to (h * 3600 + min * 60)
    }
    minutesRegex.find(n)?.let { m ->
        val a = m.groupValues[1].toInt()
        val b = m.groupValues[2].toIntOrNull()
        candidates += m.range.first to (maxOf(a, b ?: a) * 60)
    }
    secondsRegex.find(n)?.let { m ->
        candidates += m.range.first to m.groupValues[1].toInt()
    }

    // O primeiro tempo citado no passo é o que vale. "1 hora e 30 min" é
    // achado pelas horas (que começam antes) e já soma os minutos.
    val seconds = candidates.minByOrNull { it.first }?.second ?: return null
    return seconds.takeIf { it in 1..(24 * 3600) }
}

// 125 -> "2:05" | 3725 -> "1:02:05"
fun formatClock(totalSeconds: Int): String {
    val s = totalSeconds.coerceAtLeast(0)
    val h = s / 3600
    val m = (s % 3600) / 60
    val sec = s % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, sec) else "%d:%02d".format(m, sec)
}

// 30 -> "30 min" | 90 -> "1 h 30" | 120 -> "2 h"
fun formatDuration(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    return when {
        h == 0 -> "$m min"
        m == 0 -> "$h h"
        else -> "$h h $m"
    }
}
