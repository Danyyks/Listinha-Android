package com.listinha.app.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import android.widget.Toast
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.listinha.app.R
import com.listinha.app.data.Recipe
import com.listinha.app.list.ListViewModel
import com.listinha.app.ui.components.capFontScale
import com.listinha.app.ui.theme.Area
import com.listinha.app.ui.theme.AreaTheme
import com.listinha.app.ui.theme.LocalFlavor
import com.listinha.app.ui.theme.LocalPaper

// ============================================================
//  HomeScreen.kt  —  a raiz do app depois do login
// ------------------------------------------------------------
//  Tem a barra de navegação embaixo (Lista / Receitas / Perfil) e
//  troca entre as telas. É o "esqueleto" que segura o app logado.
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
    sharedText: String? = null,            // texto compartilhado de outro app (vira receita)
    onSharedTextConsumed: () -> Unit = {},
) {
    // Um único ListViewModel serve às duas abas (lista e histórico/perfil).
    val listViewModel: ListViewModel = viewModel()
    val context = LocalContext.current

    // Reinicia o ViewModel sempre que o usuário logado muda. Sem isto, depois
    // de um logout+login o app mostraria os dados da conta ANTERIOR (o
    // ViewModel sobrevive à troca de tela porque a Activity não é destruída).
    LaunchedEffect(userId) { listViewModel.start(userId) }

    // Se uma ação (adicionar/editar/apagar) falhar, avisa com um Toast.
    LaunchedEffect(listViewModel.errorMessage) {
        listViewModel.errorMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            listViewModel.clearError()
        }
    }

    // Qual aba está aberta: 0 = Lista, 1 = Receitas, 2 = Perfil.
    var tab by rememberSaveable { mutableIntStateOf(0) }

    // Modo compra fica aqui porque a barra de abas também muda de cor com ele.
    var shoppingMode by rememberSaveable { mutableStateOf(false) }

    // Detalhe/formulário de receita escondem a barra de abas (mais foco).
    var recipesImmersive by rememberSaveable { mutableStateOf(false) }

    // Chegou texto de outro app ("Compartilhar -> Listinha"): vai pra aba Receitas.
    LaunchedEffect(sharedText) { if (sharedText != null) tab = 1 }

    // A mágica "receita -> lista": joga os ingredientes na lista de compras,
    // cada um já na sua categoria. replace = true apaga a lista antes
    // (usado no "Criar nova lista com esta receita").
    fun sendRecipeToList(recipe: Recipe, replace: Boolean) {
        if (recipe.ingredients.isEmpty()) return
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
                    shoppingMode = shoppingMode,
                    onShoppingModeChange = { shoppingMode = it },
                    onOpenRecipes = { tab = 1 },
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
                    sharedText = sharedText,
                    onSharedTextConsumed = onSharedTextConsumed,
                    onImmersiveChange = { recipesImmersive = it },
                    onAddToList = { sendRecipeToList(it, replace = false) },
                    onReplaceList = { sendRecipeToList(it, replace = true) },
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

        // A barra de abas some no detalhe/formulário de receita.
        if (!(tab == 1 && recipesImmersive)) {
            val area = when {
                tab == 0 && shoppingMode && listViewModel.items.isNotEmpty() -> Area.MERCADO
                tab == 0 -> Area.LISTA
                tab == 1 -> Area.RECEITAS
                else -> Area.ACAI
            }
            AreaTheme(area) { TabBar(tab) { tab = it } }
        }
    }
}

// Barra de abas sobre o papel; a aba atual ganha uma pílula na cor da área.
@Composable
internal fun TabBar(selected: Int, onSelect: (Int) -> Unit) {
    val paper = LocalPaper.current
    Column(Modifier.fillMaxWidth().background(paper.paper).navigationBarsPadding()) {
        HorizontalDivider(color = paper.line)
        Row(
            modifier = Modifier.fillMaxWidth().height(72.dp).padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TabItem("Lista", R.drawable.ic_list, selected == 0, Modifier.weight(1f)) { onSelect(0) }
            TabItem("Receitas", R.drawable.ic_recipes, selected == 1, Modifier.weight(1f)) { onSelect(1) }
            TabItem("Perfil", R.drawable.ic_person, selected == 2, Modifier.weight(1f)) { onSelect(2) }
        }
    }
}

@Composable
private fun TabItem(
    label: String,
    @DrawableRes icon: Int,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val paper = LocalPaper.current
    val flavor = LocalFlavor.current
    Column(
        modifier = modifier
            .height(64.dp)
            .clip(RoundedCornerShape(20.dp))
            .selectable(selected = selected, role = Role.Tab, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
    ) {
        Box(
            modifier = Modifier
                .size(width = 62.dp, height = 32.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(if (selected) flavor.title else Color.Transparent),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painterResource(icon),
                contentDescription = null,
                tint = if (selected) flavor.on else paper.muted,
                modifier = Modifier.size(22.dp),
            )
        }
        Text(
            label,
            fontSize = capFontScale(13.sp, 1.15f),
            maxLines = 1,
            fontWeight = if (selected) FontWeight.Black else FontWeight.Bold,
            color = if (selected) paper.ink else paper.muted,
        )
    }
}
