package com.listinha.app.debug

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.listinha.app.R
import com.listinha.app.data.Item
import com.listinha.app.data.Recipe
import com.listinha.app.data.RecipeIngredient
import com.listinha.app.ui.screens.CookModeScreen
import com.listinha.app.ui.screens.ListScreen
import com.listinha.app.ui.screens.LoginScreen
import com.listinha.app.ui.screens.ProfileScreen
import com.listinha.app.ui.screens.RecipeDetailScreen
import com.listinha.app.ui.screens.RecipeFormScreen
import com.listinha.app.ui.screens.RecipesGridScreen
import com.listinha.app.ui.screens.TabBar
import com.listinha.app.ui.theme.Area
import com.listinha.app.ui.theme.AreaTheme
import com.listinha.app.ui.theme.ListinhaTheme
import java.io.ByteArrayOutputStream
import java.io.File

// ============================================================
//  PreviewActivity.kt  —  vitrine das telas com dados falsos
// ------------------------------------------------------------
//  Só existe na versão de desenvolvimento (pasta src/debug) e não
//  fala com o Firebase: serve para conferir o visual e testar sem
//  encostar em nenhuma conta real. Abra pelo adb:
//    adb shell am start -n com.danyks.listinha/com.listinha.app.debug.PreviewActivity \
//      --es tela lista --es tema claro
//  Telas: boasvindas, lista, vazia, compra, receitas, semreceitas,
//  detalhe, nova, colada, cozinhar, perfil. Temas: claro, escuro.
// ============================================================

class PreviewActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val tela = intent.getStringExtra("tela") ?: "lista"
        val escuro = intent.getStringExtra("tema") == "escuro"
        val foto = fotoDeExemplo()
        val fotosReais = fotosDosPrints()
        setContent {
            ListinhaTheme(darkTheme = escuro) { Vitrine(tela, foto, fotosReais) }
        }
    }

    // Fotos opcionais para os prints: JPEGs copiados para files/fotos com run-as.
    private fun fotosDosPrints(): Map<String, String> =
        (File(filesDir, "fotos").listFiles() ?: emptyArray()).associate {
            it.nameWithoutExtension to Base64.encodeToString(it.readBytes(), Base64.NO_WRAP)
        }

    // Reaproveita uma foto do app no mesmo formato em que as receitas guardam (base64).
    private fun fotoDeExemplo(): String {
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.empty_strawberry)
        val saida = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, saida)
        return Base64.encodeToString(saida.toByteArray(), Base64.NO_WRAP)
    }
}

private val itensExemplo = listOf(
    Item("1", "Espaguete", "mercearia", 12.90, 1.0),
    Item("2", "Bacon em cubos", "carnes", 18.50, 1.0, bought = true),
    Item("3", "Ovos caipira", "laticinios", 16.0, 2.0),
    Item("4", "Parmesão ralado", "laticinios", 14.90, 1.0),
    Item("5", "Morango", "frutas", 9.90, 2.0, bought = true),
)

private fun receita(id: String, nome: String, foto: String, minutos: Int) = Recipe(
    id = id,
    name = nome,
    servings = 4,
    minutes = minutos,
    photo = foto,
    ingredients = listOf(
        RecipeIngredient("Espaguete", "mercearia", "500 g"),
        RecipeIngredient("Bacon em cubos", "carnes", "200 g"),
        RecipeIngredient("Ovos", "laticinios", "3 unidades"),
        RecipeIngredient("Parmesão ralado", "laticinios", "100 g"),
    ),
    steps = listOf(
        "Cozinhe o espaguete em água fervente com sal por 10 minutos.",
        "Doure o bacon na frigideira até ficar crocante.",
        "Misture os ovos com o parmesão e junte à massa fora do fogo.",
    ),
)

// Conjunto usado nos prints da loja: a carbonara abre o detalhe e o modo cozinhar.
private fun receitasDosPrints(fotos: Map<String, String>): List<Recipe> {
    fun demo(nome: String, chave: String, minutos: Int) = receita(chave, nome, fotos[chave].orEmpty(), minutos)
    return listOf(
        demo("Carbonara", "carbonara", 25).copy(
            servings = 2,
            ingredients = listOf(
                RecipeIngredient("Espaguete", "mercearia", "200 g"),
                RecipeIngredient("Bacon em cubos", "carnes", "100 g"),
                RecipeIngredient("Gemas", "laticinios", "3 unidades"),
                RecipeIngredient("Parmesão ralado", "laticinios", "50 g"),
                RecipeIngredient("Pimenta-do-reino", "mercearia", "a gosto"),
            ),
            steps = listOf(
                "Cozinhe o espaguete em água fervente com sal por 10 minutos.",
                "Doure o bacon na frigideira até ficar crocante.",
                "Misture as gemas com o parmesão e junte à massa fora do fogo.",
            ),
        ),
        demo("Sorvete de 2 ingredientes", "sorvete", 10),
        demo("Donut de air fryer", "donuts", 25),
        demo("Morango com chocolate", "morango", 15),
        demo("Lasanha de frigideira", "lasanha", 30),
        demo("Cookie de frigideira", "cookies", 20),
    )
}

private val rascunhoColado = Recipe(
    name = "Cookie de frigideira",
    servings = 4,
    minutes = 20,
    ingredients = listOf(
        RecipeIngredient("Manteiga", "laticinios", "100 g"),
        RecipeIngredient("Açúcar mascavo", "mercearia", "1 xícara"),
        RecipeIngredient("Ovo", "laticinios", "1 unidade"),
        RecipeIngredient("Farinha de trigo", "mercearia", "1 e 1/2 xícara"),
        RecipeIngredient("Chocolate", "doces", "150 g"),
    ),
    steps = listOf("Misture a manteiga com o açúcar e o ovo.", "Junte a farinha e o chocolate.", "Asse na frigideira por 20 minutos."),
)

@Composable
private fun Vitrine(tela: String, foto: String, fotosReais: Map<String, String>) {
    var itens by remember { mutableStateOf(if (tela == "vazia") emptyList() else itensExemplo) }
    var compra by remember { mutableStateOf(tela == "compra") }
    val receitas = remember {
        if (fotosReais.isNotEmpty()) receitasDosPrints(fotosReais) else listOf(
            receita("1", "Morango com chocolate", foto, 15),
            receita("2", "Carbonara", "", 25),
            receita("3", "Sorvete de 2 ingredientes", foto, 10),
            receita("4", "Smash burger", "", 20),
        )
    }

    when (tela) {
        "boasvindas" -> LoginScreen(isLoading = false, errorMessage = null, onSignIn = {})
        "detalhe" -> RecipeDetailScreen(receitas[0], {}, {}, {}, {}, {})
        "nova" -> AreaTheme(Area.NOVA_RECEITA) { RecipeFormScreen(null, null, false, {}, {}) }
        "colada" -> AreaTheme(Area.NOVA_RECEITA) { RecipeFormScreen(null, rascunhoColado, false, {}, {}) }
        "cozinhar" -> CookModeScreen(receitas[0].name, receitas[0].ingredients, receitas[0].steps, {})
        else -> Column(Modifier.fillMaxSize()) {
            Box(Modifier.weight(1f)) {
                when (tela) {
                    "receitas" -> AreaTheme(Area.RECEITAS) { RecipesGridScreen(receitas, {}, {}, {}, {}) }
                    "semreceitas" -> AreaTheme(Area.RECEITAS) { RecipesGridScreen(emptyList(), {}, {}, {}, {}) }
                    "perfil" -> ProfileScreen("Ana Souza", "ana@exemplo.com", null, 12, 150.0, "auto", {}, {}, {}, {})
                    else -> ListScreen(
                        userName = "Ana Souza",
                        items = itens,
                        priceHistory = emptyMap(),
                        budget = 150.0,
                        shoppingMode = compra,
                        onShoppingModeChange = { compra = it },
                        onAddItem = { _, _, _, _ -> },
                        onToggle = { alvo -> itens = itens.map { if (it.id == alvo.id) it.copy(bought = !it.bought) else it } },
                        onDelete = { alvo -> itens = itens - alvo },
                        onEditItem = { _, _, _, _, _ -> },
                        onSetBudget = {},
                        onClearBought = {},
                        onClearAll = {},
                        onOpenRecipes = {},
                    )
                }
            }
            val area = when (tela) {
                "receitas", "semreceitas" -> Area.RECEITAS
                "perfil" -> Area.ACAI
                else -> if (compra && itens.isNotEmpty()) Area.MERCADO else Area.LISTA
            }
            val aba = when (tela) {
                "receitas", "semreceitas" -> 1
                "perfil" -> 2
                else -> 0
            }
            AreaTheme(area) { TabBar(aba) {} }
        }
    }
}
