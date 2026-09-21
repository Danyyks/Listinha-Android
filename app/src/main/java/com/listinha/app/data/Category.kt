package com.listinha.app.data

import androidx.compose.ui.graphics.Color

// ============================================================
//  Category.kt  —  as categorias de produtos
// ------------------------------------------------------------
//  Espelha o arquivo categories.js do seu app React: cada
//  categoria tem uma CHAVE (usada no banco), um RÓTULO (o que
//  aparece na tela) e uma COR (a bolinha colorida do grupo).
// ============================================================

data class Category(
    val key: String,     // chave salva no banco (ex: "frutas")
    val label: String,   // texto mostrado (ex: "Frutas")
    val color: Color,    // cor do grupo na tela
)

// Lista fixa de categorias, na ordem em que aparecem.
// As cores são as mesmas do design atual (index.css).
val CATEGORIES = listOf(
    Category("frutas",     "Frutas",      Color(0xFFE07090)),
    Category("verduras",   "Verduras",    Color(0xFF7FC8A9)),
    Category("carnes",     "Carnes",      Color(0xFFD46060)),
    Category("laticinios", "Laticínios",  Color(0xFFF0A060)),
    Category("padaria",    "Padaria",     Color(0xFFB08050)),
    Category("mercearia",  "Mercearia",   Color(0xFFC9A24C)), // arroz, feijão, macarrão, miojo, óleo, enlatados
    Category("congelados", "Congelados",  Color(0xFF5FB0C4)), // pizza, lasanha, nuggets, sorvete
    Category("snacks",     "Snacks",      Color(0xFFE8C24A)), // salgadinhos, bolacha, amendoim, pipoca
    Category("doces",      "Doces",       Color(0xFFCE6DA6)), // chocolate, achocolatado, balas, sobremesas
    Category("saudaveis",  "Fitness",     Color(0xFF8DB84A)), // whey, barra de proteína, granola, integrais
    Category("bebidas",    "Bebidas",     Color(0xFF7094C4)),
    Category("limpeza",    "Limpeza",     Color(0xFF9B87C4)),
    Category("higiene",    "Higiene",     Color(0xFF9B70D6)),
    Category("outros",     "Outros",      Color(0xFF9A9590)),
)

// Cor usada por categorias "personalizadas" (criadas pelo usuário).
private val CUSTOM_COLOR = Color(0xFF7FC8A9)

// Acha uma categoria pela chave (ou null se não existir na lista fixa).
fun categoryByKey(key: String): Category? = CATEGORIES.find { it.key == key }

// Rótulo para mostrar: usa o da lista, ou a própria chave se for personalizada.
fun categoryLabel(key: String): String = categoryByKey(key)?.label ?: key

// Cor da categoria: a da lista, ou a cor de "personalizada".
fun categoryColor(key: String): Color = categoryByKey(key)?.color ?: CUSTOM_COLOR

// Ordena as chaves de categoria: as fixas primeiro (na ordem da lista),
// "outros" sempre por último, e as personalizadas em ordem alfabética.
// Mesma regra do sortCategoryKeys() do seu React.
fun sortCategoryKeys(keys: Collection<String>): List<String> {
    val order = CATEGORIES.map { it.key }
    return keys.sortedWith(Comparator { a, b ->
        // Se os dois forem "outros", são iguais (0). Isso mantém o contrato do
        // Comparator: compare(x, x) tem que dar 0, senão a ordenação pode
        // lançar "Comparison method violates its general contract!".
        if (a == "outros" && b == "outros") return@Comparator 0
        if (a == "outros") return@Comparator 1
        if (b == "outros") return@Comparator -1
        val ai = order.indexOf(a)
        val bi = order.indexOf(b)
        when {
            ai == -1 && bi == -1 -> a.compareTo(b) // duas personalizadas: alfabético
            ai == -1 -> 1                           // 'a' é personalizada -> vai depois
            bi == -1 -> -1                          // 'b' é personalizada -> vai depois
            else -> ai - bi                          // ambas fixas: ordem da lista
        }
    })
}
