package com.listinha.app.util

import com.listinha.app.data.Recipe
import com.listinha.app.data.RecipeIngredient
import java.text.Normalizer

// ============================================================
//  RecipeText.kt  ===  transforma TEXTO solto em receita
// ------------------------------------------------------------
//  É o que deixa salvar receita sem esforço:
//   • "500 g de peito de frango"  -> qtd "500 g", nome "Peito de frango"
//   • adivinha a categoria (frango -> Carnes) e o ícone (bolo -> bolo)
//   • lê uma receita inteira colada (blog, WhatsApp, Instagram) e
//     separa nome, ingredientes, passos, porções, tempo e link.
//  Tudo por regras simples, sem IA (grátis e funciona offline).
//  Entende português e o básico de inglês (para a versão em inglês).
// ============================================================

// --- Peças de expressão regular reaproveitadas ---

// Um número de receita: "1 1/2", "1 e 1/2", "1/2", "1,5", "2½", "½", "2".
internal const val NUM =
    """(?:\d+\s+(?:e\s+|and\s+)?\d+/\d+|\d+/\d+|\d+[.,]\d+|\d+\s*[½¼¾⅓⅔]|\d+|[½¼¾⅓⅔])"""

// Número por extenso — só vale quando vem seguido de uma unidade
// ("meia xícara", "uma pitada"), senão "um pouco de sal" viraria quantidade.
private const val WORD_NUM = """(?:meia|meio|uma|um|duas|dois|tr[eê]s|half|one|two|three)"""

// Faixa: "2 a 3", "2-3", "2 ou 3".
private const val RANGE = """(?:\s*(?:a|-|–|ou|to|or)\s*$NUM)?"""

// Unidades de medida (português + inglês). A ordem importa: as mais
// longas primeiro, para "colher de sopa" ganhar de "colher".
private const val UNIT =
    """(?:x[ií]caras?(?:\s*\(?\s*(?:de\s+)?ch[aá]\s*\)?)?""" +
        """|colher(?:es)?(?:\s*\(?\s*(?:de\s+)?(?:sopa|ch[aá]|caf[eé]|sobremesa)\s*\)?)?""" +
        """|colherinhas?|kg|quilos?|gramas?|gr|g|mg|ml|litros?|l|cl""" +
        """|dentes?|unidades?|un|latas?|latinhas?|pitadas?|fatias?|pacotes?|caixas?|caixinhas?""" +
        """|copos?|ma[çc]os?|folhas?|ramos?|raminhos?|tabletes?|envelopes?|potes?|potinhos?""" +
        """|gotas?|cubos?|tiras?|peda[çc]os?|punhados?|saquinhos?|sach[eê]s?|garrafas?""" +
        """|cabe[çc]as?|talos?|fil[eé]s?|bandejas?|cups?|tablespoons?|tbsps?|teaspoons?|tsps?""" +
        """|ounces?|oz|pounds?|lbs?|cloves?|cans?|pinch(?:es)?|slices?|packages?|packs?""" +
        """|pieces?|sticks?|bunch(?:es)?|sprigs?|handfuls?|quarts?|pints?)""" +
        // tamanho opcional depois da unidade: "1 pacote pequeno", "1 colher cheia"
        """(?:\s+(?:pequen[oa]s?|grandes?|m[eé]di[oa]s?|cheias?|rasas?|niveladas?|small|large|medium|heaping|level))?"""

// Fim de palavra seguro (para "g" não casar com "gemas").
private const val END = """(?=\s|$|[.,;:)])"""

// "de / do / da / of" entre a quantidade e o nome.
private const val LINK = """(?:\s+(?:de|do|da|dos|das|of)\b)?"""

private val numericStart = Regex("""^($NUM$RANGE)\s*(?:($UNIT)$END$LINK)?\s*(.*)$""", RegexOption.IGNORE_CASE)
private val wordStart = Regex("""^($WORD_NUM)\s+($UNIT)$END$LINK\s*(.*)$""", RegexOption.IGNORE_CASE)
private val toTasteEnd = Regex("""^(.*?)[\s,–-]*\b(a\s+gosto|q\.?\s*b\.?|to\s+taste|opcional|optional)\.?$""", RegexOption.IGNORE_CASE)
private val parensEnd = Regex("""^(.+?)\s*\(\s*((?:$NUM|$WORD_NUM)[^()]*)\)$""", RegexOption.IGNORE_CASE)
// Atenção: no Android (motor ICU) "[:" abre a sintaxe [:propriedade:] —
// por isso o ":" nunca vem logo depois do "[" nestas regras.
private val trailingQty = Regex("""^(.+?)\s*[–:-]?\s+($NUM$RANGE\s*(?:$UNIT)?)$END$""", RegexOption.IGNORE_CASE)
private val bullet = Regex("""^[\s•·\-–—*▪◦●○✓✔☐□>]+""")
private val stepNumber = Regex(
    """^\s*(?:(?:passo|etapa|step)\s*\d+\s*[.):\-–]?|\d+\s*[.)º°ª\-–:]+|[•·\-–—*▪◦●○>])\s*""",
    RegexOption.IGNORE_CASE,
)
// Tira emojis e enfeites, MAS mantém símbolos úteis em receita (°, ©, ®, ™).
// Feito caractere a caractere (sem regex) porque o motor de regex do
// Android (ICU) não entende a sintaxe de "subtrair" conjuntos do Java.
private val keepSymbols = setOf('°'.code, '©'.code, '®'.code, '™'.code)
private val invisibleJoiners = setOf(0xFE0F, 0x200D, 0x20E3)

internal fun stripEmoji(text: String): String {
    val out = StringBuilder(text.length)
    var i = 0
    while (i < text.length) {
        val cp = text.codePointAt(i)
        val type = Character.getType(cp)
        val decor = (type == Character.OTHER_SYMBOL.toInt() || type == Character.MODIFIER_SYMBOL.toInt()) &&
            cp !in keepSymbols
        if (!decor && cp !in invisibleJoiners) out.appendCodePoint(cp)
        i += Character.charCount(cp)
    }
    return out.toString()
}
private val url = Regex("""https?://\S+""")

// Minúsculas e sem acento: "Feijão" -> "feijao". Base de toda comparação.
internal fun normalize(s: String): String =
    Normalizer.normalize(s.lowercase(), Normalizer.Form.NFD).replace(Regex("""\p{Mn}+"""), "")

private fun String.capitalizeFirst(): String =
    replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

// Junta espaços repetidos e tira enfeites das pontas.
private fun tidy(s: String): String = s.replace(Regex("""\s+"""), " ").trim().trim(';', ',').trim()

// ------------------------------------------------------------
//  1 linha -> 1 ingrediente
// ------------------------------------------------------------

// Lê uma linha como "2 xícaras de farinha" e devolve o ingrediente
// já com a categoria adivinhada. Linha vazia devolve null.
fun parseIngredientLine(raw: String): RecipeIngredient? {
    val line = tidy(stripEmoji(raw).replace(bullet, "")).trimEnd('.')
    if (line.isEmpty()) return null

    var name = line
    var qty = ""

    val num = numericStart.find(line)
    val word = wordStart.find(line)
    when {
        // Começa com número: "500 g de frango", "3 ovos", "1/2 xícara (chá) de leite".
        num != null && num.groupValues[3].isNotBlank() -> {
            val unit = num.groupValues[2]
            qty = tidy(num.groupValues[1] + " " + unit)
            name = num.groupValues[3]
        }
        // Começa com número por extenso + unidade: "meia xícara de leite".
        word != null && word.groupValues[3].isNotBlank() -> {
            qty = tidy(word.groupValues[1] + " " + word.groupValues[2])
            name = word.groupValues[3]
        }
        else -> {
            // Quantidade no fim: "Sal a gosto", "Farinha (500 g)", "Ovos: 3".
            val taste = toTasteEnd.find(line)
            val parens = parensEnd.find(line)
            val trailing = trailingQty.find(line)
            when {
                taste != null && taste.groupValues[1].isNotBlank() -> {
                    name = taste.groupValues[1]
                    qty = taste.groupValues[2].lowercase()
                }
                parens != null -> {
                    name = parens.groupValues[1]
                    qty = parens.groupValues[2]
                }
                trailing != null -> {
                    name = trailing.groupValues[1]
                    qty = trailing.groupValues[2]
                }
            }
        }
    }

    name = tidy(name).trimEnd(':', '-').trim().capitalizeFirst()
    qty = tidy(qty)
    if (name.isEmpty()) return null
    return RecipeIngredient(name = name, category = guessCategory(name), quantity = qty)
}

// Um ingrediente salvo -> a linha de texto que o usuário edita.
// O formato é pensado para voltar IGUAL quando for lido de novo.
fun formatIngredientLine(ing: RecipeIngredient): String {
    val name = ing.name.trim()
    val qty = ing.quantity.trim()
    if (qty.isEmpty()) return name
    // "Frango" -> "frango" no meio da frase (mas "TNT" continua "TNT").
    val lowerName = if (name.length > 1 && name[1].isLowerCase()) name.replaceFirstChar { it.lowercase() } else name

    val candidate = when {
        // "a gosto", "opcional"...: vai no fim ("Sal a gosto").
        tasteOnly.matches(qty) -> "$name $qty"
        // "2 cups" + "Flour" -> "2 cups flour" (inglês não usa "de").
        qtyWithUnit.matches(qty) && englishUnit.containsMatchIn(qty) -> "$qty $lowerName"
        // "500 g" + "Frango" -> "500 g de frango".
        qtyWithUnit.matches(qty) -> "$qty de $lowerName"
        // "3" + "Ovos" -> "3 ovos".
        qtyNumberOnly.matches(qty) -> "$qty $lowerName"
        else -> "$name ($qty)"
    }
    // Confere a "ida e volta": se ler a linha de novo não der o mesmo
    // ingrediente (ex: "2" + "Latas de milho"), usa parênteses no fim.
    val back = parseIngredientLine(candidate)
    val sameBack = back != null && back.name.equals(name, ignoreCase = true) && back.quantity == qty
    return if (sameBack) candidate else "$name ($qty)"
}

private val tasteOnly = Regex("""^(a\s+gosto|q\.?\s*b\.?|to\s+taste|opcional|optional)$""", RegexOption.IGNORE_CASE)
private val qtyWithUnit = Regex("""^$NUM$RANGE\s*$UNIT$""", RegexOption.IGNORE_CASE)
private val qtyNumberOnly = Regex("""^$NUM$RANGE$""", RegexOption.IGNORE_CASE)
private val englishUnit = Regex(
    """\b(cups?|tablespoons?|tbsps?|teaspoons?|tsps?|ounces?|oz|pounds?|lbs?|cloves?|cans?|pinch(es)?|slices?|packages?|packs?|pieces?|sticks?|bunch(es)?|sprigs?|handfuls?|quarts?|pints?)\b""",
    RegexOption.IGNORE_CASE,
)

// Texto com um ingrediente por linha -> lista de ingredientes.
fun parseIngredients(text: String): List<RecipeIngredient> =
    text.lines().mapNotNull { parseIngredientLine(it) }

// Lista de ingredientes -> texto (um por linha), para editar.
fun ingredientsToText(list: List<RecipeIngredient>): String =
    list.joinToString("\n") { formatIngredientLine(it) }

// ------------------------------------------------------------
//  Passos do modo de preparo (um por linha)
// ------------------------------------------------------------

// Tira a numeração que o usuário colou ("1.", "Passo 2:", "•") — o app
// numera sozinho na tela.
fun parseSteps(text: String): List<String> =
    text.lines()
        .map { tidy(stripEmoji(it).replace(stepNumber, "")) }
        .filter { it.isNotEmpty() }
        .map { it.capitalizeFirst() }

fun stepsToText(steps: List<String>): String = steps.joinToString("\n")

// ------------------------------------------------------------
//  Receita inteira colada (blog, WhatsApp, legenda do Instagram)
// ------------------------------------------------------------

private val ingredientsHeader = Regex(
    """^(ingredientes?|lista de ingredientes|voce vai precisar|ingredients?|what you.?ll need|you will need)\b.*""",
)
private val stepsHeader = Regex(
    """^(modo de preparo|modo de fazer|preparo|como fazer|preparacao|instrucoes|passo a passo|method|directions|instructions|preparation|steps|how to make)\b.*""",
)
private val servingsLine = Regex("""(rende|rendimento|serve|porcoes|porcao|serves|servings|yield)\D{0,15}(\d+)""")
private val timeLine = Regex("""(tempo|pronto em|total|time|ready in)\D{0,25}(\d+)\s*(min|h|hora|hour)""")

// Lê o texto colado e devolve uma receita "rascunho" (sem id).
fun parseRecipeText(text: String): Recipe {
    val sourceUrl = url.find(text)?.value?.trimEnd('.', ',', ')') ?: ""

    // Linhas limpas: sem link, sem emoji, sem hashtags soltas.
    val lines = text.replace(url, "").lines()
        .map { tidy(stripEmoji(it)) }
        .filter { it.isNotEmpty() && !it.startsWith("#") }

    var name = ""
    var servings = 1
    var minutes = 0
    val ingredientLines = mutableListOf<String>()
    val stepLines = mutableListOf<String>()
    val looseLines = mutableListOf<String>() // linhas antes de qualquer título

    // 0 = antes dos títulos, 1 = dentro de "Ingredientes", 2 = dentro de "Modo de preparo"
    var section = 0
    var sawHeader = false

    for (line in lines) {
        val n = normalize(line).trim(':', '.', ' ')
        val short = line.length <= 40

        // Títulos de seção.
        if (short && ingredientsHeader.matches(n)) { section = 1; sawHeader = true; continue }
        if (short && stepsHeader.matches(n)) { section = 2; sawHeader = true; continue }

        // Rendimento e tempo (linhas curtas tipo "Rende 4 porções", "Tempo: 40 min").
        if (line.length <= 50) {
            val s = servingsLine.find(n)
            if (s != null) {
                servings = s.groupValues[2].toIntOrNull()?.coerceIn(1, 99) ?: servings
                continue
            }
            val t = timeLine.find(n)
            if (t != null) {
                val v = t.groupValues[2].toIntOrNull() ?: 0
                minutes = if (t.groupValues[3].startsWith("h")) v * 60 else v
                continue
            }
        }

        // Subtítulos dentro de uma seção ("Massa:", "Cobertura:") são pulados.
        if (section != 0 && line.endsWith(":") && line.length <= 30 && !line.any { it.isDigit() }) continue

        when (section) {
            1 -> ingredientLines += line
            2 -> stepLines += line
            // A 1ª linha (curta e sem quantidade na frente) é o nome do prato.
            else -> if (name.isEmpty() && looseLines.isEmpty() && line.length <= 60 && !startsWithQuantity(line)) {
                name = line.trimEnd(':', '.', '!')
            } else {
                looseLines += line
            }
        }
    }

    // Sem títulos: decide linha a linha (curta/com número = ingrediente; frase = passo).
    if (!sawHeader) {
        for (line in looseLines) {
            if (looksLikeIngredient(line)) ingredientLines += line else stepLines += line
        }
    }

    val ingredients = ingredientLines.mapNotNull { parseIngredientLine(it) }
    val steps = parseSteps(stepLines.joinToString("\n"))
    return Recipe(
        name = name,
        servings = servings,
        minutes = minutes,
        iconIndex = guessIconIndex(name),
        ingredients = ingredients,
        steps = steps,
        sourceUrl = sourceUrl,
    )
}

// Uma linha "parece" ingrediente? (começa com número, termina em "a gosto"
// ou é curtinha e sem ponto final, tipo "Sal" ou "Cebola picada").
private val quantityStart = Regex("""^($NUM|$WORD_NUM\s+$UNIT$END)""", RegexOption.IGNORE_CASE)

private fun startsWithQuantity(line: String): Boolean =
    quantityStart.containsMatchIn(line.replace(bullet, "").trim())

private fun looksLikeIngredient(line: String): Boolean {
    val clean = line.replace(bullet, "").trim()
    if (quantityStart.containsMatchIn(clean)) {
        // "1. Bata os ovos..." é passo numerado, não ingrediente.
        return !Regex("""^\d+\s*[.)º°]\s""").containsMatchIn(clean)
    }
    if (toTasteEnd.containsMatchIn(clean)) return true
    val words = clean.split(" ").size
    return clean.length <= 35 && words <= 5 && !clean.endsWith(".")
}

// ------------------------------------------------------------
//  Adivinhar categoria e ícone
// ------------------------------------------------------------

// Expressões de várias palavras são checadas ANTES das palavras soltas
// (assim "molho de tomate" vai pra Mercearia, e não pra Verduras).
private val categoryPhrases = listOf(
    "creme de leite" to "laticinios", "cream cheese" to "laticinios", "leite condensado" to "doces",
    "doce de leite" to "doces", "condensed milk" to "doces", "leite de coco" to "mercearia",
    "molho de tomate" to "mercearia", "extrato de tomate" to "mercearia", "coco ralado" to "mercearia",
    "milho verde" to "mercearia", "grao de bico" to "mercearia", "batata palha" to "snacks",
    "chocolate em po" to "doces", "fermento em po" to "mercearia", "pimenta do reino" to "mercearia",
    "noz moscada" to "mercearia", "cheiro verde" to "verduras", "olive oil" to "mercearia",
    "baking powder" to "mercearia", "baking soda" to "mercearia", "bell pepper" to "verduras",
    "ice cream" to "congelados", "ground beef" to "carnes", "carne moida" to "carnes",
    "papel aluminio" to "outros", "papel manteiga" to "outros",
)

private val categoryWords = mapOf(
    "frutas" to listOf(
        "banana", "maca", "laranja", "limao", "morango", "uva", "abacaxi", "manga", "mamao", "melancia",
        "melao", "pera", "pessego", "kiwi", "abacate", "maracuja", "goiaba", "ameixa", "mirtilo",
        "framboesa", "amora", "tangerina", "mexerica", "caju", "coco", "apple", "lemon", "lime", "orange",
        "strawberry", "strawberries", "grape", "pineapple", "mango", "avocado", "blueberries", "peach",
    ),
    "verduras" to listOf(
        "alface", "tomate", "cebola", "alho", "batata", "cenoura", "abobrinha", "abobora", "pimentao",
        "brocolis", "couve", "repolho", "espinafre", "rucula", "pepino", "beterraba", "mandioca", "aipim",
        "macaxeira", "inhame", "chuchu", "quiabo", "berinjela", "salsinha", "salsa", "cebolinha", "coentro",
        "manjericao", "hortela", "gengibre", "cogumelo", "champignon", "vagem", "salsao", "alho-poro",
        "onion", "garlic", "tomato", "tomatoes", "potato", "potatoes", "carrot", "lettuce", "spinach",
        "broccoli", "cucumber", "zucchini", "mushroom", "parsley", "cilantro", "basil", "ginger", "celery",
    ),
    "carnes" to listOf(
        "carne", "frango", "peito", "coxa", "sobrecoxa", "file", "bife", "patinho", "alcatra", "picanha",
        "acem", "musculo", "costela", "linguica", "salsicha", "bacon", "presunto", "mortadela", "salame",
        "peixe", "salmao", "tilapia", "camarao", "bacalhau", "porco", "lombo", "pernil", "calabresa",
        "peru", "chester", "chicken", "beef", "pork", "ham", "sausage", "fish", "salmon", "shrimp", "steak",
    ),
    "laticinios" to listOf(
        "leite", "queijo", "mussarela", "mucarela", "parmesao", "requeijao", "iogurte", "manteiga",
        "margarina", "nata", "ricota", "catupiry", "ovo", "gema", "clara", "milk", "cheese", "butter",
        "yogurt", "cream", "egg",
    ),
    "padaria" to listOf("pao", "paes", "bisnaguinha", "torrada", "croissant", "bread", "bun", "tortilla"),
    "mercearia" to listOf(
        "farinha", "fermento", "acucar", "sal", "arroz", "feijao", "macarrao", "massa", "espaguete",
        "talharim", "oleo", "azeite", "vinagre", "milho", "ervilha", "atum", "sardinha", "azeitona",
        "amido", "maisena", "fuba", "polvilho", "tapioca", "lentilha", "caldo", "tempero", "oregano",
        "colorau", "paprica", "paprika", "canela", "cominho", "louro", "curry", "mostarda", "ketchup",
        "maionese", "shoyu", "molho", "flour", "sugar", "salt", "rice", "beans", "pasta", "oil",
        "vinegar", "yeast", "cinnamon",
    ),
    "congelados" to listOf("congelado", "congelada", "sorvete", "nuggets", "frozen"),
    "snacks" to listOf("salgadinho", "amendoim", "pipoca", "biscoito", "bolacha", "chips", "crackers", "popcorn", "peanut"),
    "doces" to listOf(
        "chocolate", "achocolatado", "nescau", "granulado", "goiabada", "gelatina", "nutella", "cacau",
        "mel", "candy", "honey", "sprinkles", "cocoa",
    ),
    "saudaveis" to listOf("whey", "granola", "aveia", "chia", "linhaca", "quinoa", "integral", "proteina", "oats", "protein"),
    "bebidas" to listOf(
        "agua", "suco", "refrigerante", "cerveja", "vinho", "cafe", "cha", "cachaca", "vodka", "rum",
        "water", "juice", "soda", "beer", "wine", "coffee", "tea",
    ),
    "limpeza" to listOf("detergente", "sabao", "esponja", "desinfetante"),
    "higiene" to listOf("sabonete", "shampoo", "papel higienico", "creme dental"),
)

// "tomates" casa com "tomate"; "ovos" com "ovo"; palavras curtas
// (sal, cha, mel) só casam inteiras, para "chantilly" não virar chá.
private fun wordMatches(token: String, kw: String): Boolean =
    token == kw || token == kw + "s" || token == kw + "es" || (kw.length >= 5 && token.startsWith(kw))

// Adivinha a categoria pelo nome do ingrediente ("outros" se não souber).
fun guessCategory(name: String): String {
    val n = " " + normalize(name).replace(Regex("""[^a-z0-9 \-]"""), " ").replace(Regex("""\s+"""), " ").trim() + " "
    categoryPhrases.firstOrNull { (kw, _) -> n.contains(" $kw") }?.let { return it.second }
    // A palavra mais "à esquerda" decide: em "suco de laranja" vale "suco".
    for (token in n.trim().split(" ")) {
        for ((category, words) in categoryWords) {
            if (words.any { wordMatches(token, it) }) return category
        }
    }
    return "outros"
}

// Índices batem com recipeIconResList (RecipesScreen.kt):
// 0 panela, 1 panqueca, 2 salada, 3 lasanha, 4 massa, 5 pizza,
// 6 hambúrguer, 7 pão, 8 bolo, 9 bebida, 10 café, 11 peixe.
private val iconWords = listOf(
    1 to listOf("panqueca", "crepe", "waffle", "pancake", "tapioca"),
    2 to listOf("salada", "salad", "bowl", "poke"),
    3 to listOf("lasanha", "escondidinho", "lasagna", "torta salgada", "quiche"),
    4 to listOf("macarrao", "espaguete", "massa", "talharim", "nhoque", "penne", "pasta", "spaghetti", "yakisoba", "risoto", "risotto"),
    5 to listOf("pizza"),
    6 to listOf("hamburguer", "burger", "sanduiche", "lanche", "sandwich", "wrap"),
    7 to listOf("pao", "broa", "focaccia", "bread", "toast", "rosca"),
    8 to listOf(
        "bolo", "brigadeiro", "pudim", "mousse", "brownie", "cookie", "doce", "sobremesa", "torta",
        "cake", "pie", "dessert", "cupcake", "beijinho", "pave", "cheesecake",
    ),
    9 to listOf("suco", "drink", "vitamina", "smoothie", "caipirinha", "coquetel", "limonada", "juice", "milkshake"),
    10 to listOf("cafe", "cappuccino", "coffee", "latte", "cha"),
    11 to listOf("peixe", "salmao", "tilapia", "camarao", "bacalhau", "moqueca", "fish", "shrimp", "sushi"),
)

// Adivinha a miniatura pelo nome da receita (0 = panela, o coringa).
fun guessIconIndex(recipeName: String): Int {
    val n = " " + normalize(recipeName).replace(Regex("""[^a-z0-9 ]"""), " ") + " "
    val tokens = n.trim().split(Regex("""\s+"""))
    for ((index, words) in iconWords) {
        for (kw in words) {
            val hit = if (kw.contains(" ")) n.contains(" $kw ") else tokens.any { wordMatches(it, kw) }
            if (hit) return index
        }
    }
    return 0
}
