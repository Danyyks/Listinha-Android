package com.listinha.app.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import android.widget.Toast
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.listinha.app.R
import com.listinha.app.data.Recipe
import com.listinha.app.list.ListViewModel

// ============================================================
//  HomeScreen.kt  —  a raiz do app depois do login
// ------------------------------------------------------------
//  Tem a barra de navegação embaixo (Lista / Perfil) e troca
//  entre as duas telas. É o "esqueleto" que segura o app logado.
// ============================================================

@Composable
fun HomeScreen(
    userId: String,                        // uid do usuário logado (identifica a conta)
    userName: String?,
    userEmail: String?,
    photoUrl: String?,
    themeMode: String,                    // tema atual: "light" | "dark" | "auto"
    onThemeModeChange: (String) -> Unit,   // muda o tema visual (lá em cima, no app)
    onSignOut: () -> Unit,
    onDeleteAccount: () -> Unit,
) {
    // Um único ListViewModel serve às duas abas (lista e histórico/perfil).
    val listViewModel: ListViewModel = viewModel()
    val context = LocalContext.current

    // Reinicia o ViewModel sempre que o usuário logado muda. Sem isto, depois
    // de um logout+login o app mostraria os dados da conta ANTERIOR (o
    // ViewModel sobrevive à troca de tela porque a Activity não é destruída).
    LaunchedEffect(userId) { listViewModel.start() }

    // Se uma ação (adicionar/editar/apagar) falhar, avisa com um Toast.
    LaunchedEffect(listViewModel.errorMessage) {
        listViewModel.errorMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            listViewModel.clearError()
        }
    }

    // Qual aba está aberta: 0 = Lista, 1 = Receitas, 2 = Perfil.
    var tab by rememberSaveable { mutableIntStateOf(0) }

    // A mágica "receita -> lista": joga os ingredientes na lista de compras,
    // cada um já na sua categoria. replace = true apaga a lista antes
    // (usado no "Criar nova lista com esta receita").
    fun sendRecipeToList(recipe: Recipe, replace: Boolean) {
        // Monta os pares (nome, categoria) — o nome vira "Nome (quantidade)".
        val itens = recipe.ingredients.map { ing ->
            val nome = if (ing.quantity.isBlank()) ing.name else "${ing.name} (${ing.quantity})"
            nome to ing.category
        }
        // Uma ação só: limpa (se for o caso) e adiciona EM SEQUÊNCIA, sem corrida.
        listViewModel.addItemsFromRecipe(itens, replace)
        Toast.makeText(
            context,
            if (replace) "Nova lista criada com a receita!" else "Ingredientes adicionados à lista!",
            Toast.LENGTH_SHORT,
        ).show()
        tab = 0 // leva pra aba Lista pra ver o resultado
    }

    // Quando o perfil termina de carregar do banco, sincroniza o tema
    // visual com o que estava salvo (ex: o usuário tinha escolhido "escuro").
    LaunchedEffect(listViewModel.profile.theme) {
        onThemeModeChange(listViewModel.profile.theme)
    }

    Column(Modifier.fillMaxSize()) {
        // A área de cima (peso 1 = ocupa todo o espaço acima da barra).
        Box(Modifier.weight(1f)) {
            when (tab) {
                0 -> ListScreen(
                    userName = userName,
                    items = listViewModel.items,
                    priceHistory = listViewModel.priceHistory,
                    budget = listViewModel.budget,
                    onAddItem = { name, category, price, quantity ->
                        listViewModel.addItem(name, category, price, quantity)
                    },
                    onToggle = { item -> listViewModel.toggle(item) },
                    onDelete = { item -> listViewModel.delete(item) },
                    onEditItem = { id, name, category, price, quantity ->
                        listViewModel.editItem(id, name, category, price, quantity)
                    },
                    onSetBudget = { value -> listViewModel.updateBudget(value) },
                    onClearBought = { listViewModel.clearBought() },
                    onClearAll = { listViewModel.clearAll() },
                )

                1 -> RecipesTab(
                    userId = userId,
                    onAddToList = { sendRecipeToList(it, replace = false) },
                    onCreateListFromRecipe = { sendRecipeToList(it, replace = true) },
                )

                else -> ProfileScreen(
                    userName = userName,
                    userEmail = userEmail,
                    photoUrl = photoUrl,
                    itemCount = listViewModel.items.size,
                    budget = listViewModel.budget,
                    theme = themeMode,
                    onThemeChange = { novoTema ->
                        onThemeModeChange(novoTema)         // muda o visual na hora
                        listViewModel.updateTheme(novoTema)  // salva no banco
                    },
                    onClearPriceHistory = { listViewModel.clearPriceHistory() },
                    onSignOut = onSignOut,
                    onDeleteAccount = onDeleteAccount,
                )
            }
        }

        // A barra de navegação inferior.
        NavigationBar {
            NavigationBarItem(
                selected = tab == 0,
                onClick = { tab = 0 },
                icon = { Icon(painterResource(R.drawable.ic_list), contentDescription = null) },
                label = { Text("Lista") },
            )
            NavigationBarItem(
                selected = tab == 1,
                onClick = { tab = 1 },
                icon = { Icon(painterResource(R.drawable.ic_recipes), contentDescription = null) },
                label = { Text("Receitas") },
            )
            NavigationBarItem(
                selected = tab == 2,
                onClick = { tab = 2 },
                icon = { Icon(painterResource(R.drawable.ic_person), contentDescription = null) },
                label = { Text("Perfil") },
            )
        }
    }
}
