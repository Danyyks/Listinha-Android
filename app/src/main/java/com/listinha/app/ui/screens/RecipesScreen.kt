@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.listinha.app.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.listinha.app.R
import com.listinha.app.data.Recipe
import com.listinha.app.recipes.RecipeViewModel
import com.listinha.app.ui.components.RecipeImage
import com.listinha.app.ui.theme.FredokaFamily
import com.listinha.app.util.formatDuration
import com.listinha.app.util.normalize
import com.listinha.app.util.parseRecipeText

// ============================================================
//  RecipesScreen.kt  ===  a aba "Receitas"
// ------------------------------------------------------------
//  RecipesTab é o "cérebro": decide se mostra a GRADE, o DETALHE
//  de uma receita ou o FORMULÁRIO. RecipesGridScreen é a grade
//  estilo Pinterest (fotos grandes, duas colunas).
// ============================================================

// Ícones disponíveis para as receitas (capa automática quando não há foto).
// A ORDEM importa: o índice fica salvo na receita e o guessIconIndex
// (util/RecipeText.kt) usa os mesmos números.
internal val recipeIconResList = listOf(
    R.drawable.ic_food_pot,
    R.drawable.ic_food_pancake,
    R.drawable.ic_food_salad,
    R.drawable.ic_food_layers,
    R.drawable.ic_food_pasta,
    R.drawable.ic_food_pizza,
    R.drawable.ic_food_burger,
    R.drawable.ic_food_bread,
    R.drawable.ic_food_cake,
    R.drawable.ic_food_drink,
    R.drawable.ic_food_coffee,
    R.drawable.ic_food_fish,
)

// Qual ícone usar para uma receita (pelo índice guardado nela).
internal fun iconForRecipe(recipe: Recipe): Int =
    recipeIconResList[recipe.iconIndex.coerceIn(0, recipeIconResList.lastIndex)]

// As "telas" possíveis dentro da aba Receitas.
private sealed interface RecipesRoute {
    data object Grid : RecipesRoute
    // Guarda só o ID: o detalhe lê a receita atualizada do ViewModel
    // (assim, depois de editar, o detalhe já mostra a versão nova).
    data class Detail(val recipeId: String) : RecipesRoute
    data class Form(
        val editing: Recipe?,           // null = receita nova
        val prefill: Recipe? = null,    // rascunho vindo de texto compartilhado
        val openPaste: Boolean = false, // já abre a caixa "Colar receita"
    ) : RecipesRoute
}

@Composable
fun RecipesTab(
    userId: String,                          // uid do usuário logado (identifica a conta)
    sharedText: String?,                     // texto compartilhado de outro app (ou null)
    onSharedTextConsumed: () -> Unit,
    onImmersiveChange: (Boolean) -> Unit,    // true = esconder a barra de abas (detalhe/formulário)
    onAddToList: (Recipe) -> Unit,
    onReplaceList: (Recipe) -> Unit,
) {
    // O ViewModel segura as receitas (vindas do Firestore) e as ações.
    val vm: RecipeViewModel = viewModel()
    // Reinicia as receitas sempre que a conta logada muda (o ViewModel
    // sobrevive à troca de tela, então sem isto vazaria dado entre contas).
    LaunchedEffect(userId) { vm.start() }

    // Se salvar/editar/excluir uma receita falhar, avisa com um Toast.
    val context = LocalContext.current
    LaunchedEffect(vm.errorMessage) {
        vm.errorMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            vm.clearError()
        }
    }

    var route by remember { mutableStateOf<RecipesRoute>(RecipesRoute.Grid) }

    // Texto compartilhado de outro app (Instagram, navegador, WhatsApp):
    // vira um rascunho de receita já preenchido.
    LaunchedEffect(sharedText) {
        if (sharedText != null) {
            route = RecipesRoute.Form(editing = null, prefill = parseRecipeText(sharedText))
            onSharedTextConsumed()
        }
    }

    // Fora da grade, a barra de abas some (mais foco). Ao sair da aba, volta.
    LaunchedEffect(route) { onImmersiveChange(route !is RecipesRoute.Grid) }
    DisposableEffect(Unit) { onDispose { onImmersiveChange(false) } }

    // O botão "voltar" do celular volta para a tela anterior da aba
    // (antes ele fechava o app direto).
    BackHandler(enabled = route !is RecipesRoute.Grid) {
        route = when (val r = route) {
            is RecipesRoute.Form -> r.editing?.let { RecipesRoute.Detail(it.id) } ?: RecipesRoute.Grid
            else -> RecipesRoute.Grid
        }
    }

    when (val r = route) {
        RecipesRoute.Grid -> RecipesGridScreen(
            recipes = vm.recipes,
            onOpen = { route = RecipesRoute.Detail(it.id) },
            onNew = { route = RecipesRoute.Form(editing = null) },
            onPaste = { route = RecipesRoute.Form(editing = null, openPaste = true) },
            onQuickAdd = onAddToList,
        )

        is RecipesRoute.Detail -> {
            val recipe = vm.recipes.find { it.id == r.recipeId }
            if (recipe == null) {
                // Receita sumiu (excluída aqui ou em outro aparelho): volta pra grade.
                LaunchedEffect(Unit) { route = RecipesRoute.Grid }
            } else {
                RecipeDetailScreen(
                    recipe = recipe,
                    onBack = { route = RecipesRoute.Grid },
                    onEdit = { route = RecipesRoute.Form(editing = recipe) },
                    onDelete = {
                        vm.delete(recipe)
                        route = RecipesRoute.Grid
                    },
                    onAddToList = onAddToList,
                    onReplaceList = onReplaceList,
                )
            }
        }

        is RecipesRoute.Form -> RecipeFormScreen(
            initial = r.editing,
            prefill = r.prefill,
            openPaste = r.openPaste,
            onCancel = {
                route = r.editing?.let { RecipesRoute.Detail(it.id) } ?: RecipesRoute.Grid
            },
            onSave = { nova ->
                if (r.editing == null) {
                    vm.add(nova)
                    route = RecipesRoute.Grid
                } else {
                    vm.update(r.editing.id, nova)
                    route = RecipesRoute.Detail(r.editing.id)
                }
            },
        )
    }
}

// Alturas das fotos na grade, alternadas para o efeito "mosaico".
private val cardHeights = listOf(214.dp, 166.dp, 190.dp, 174.dp)

@Composable
private fun RecipesGridScreen(
    recipes: List<Recipe>,
    onOpen: (Recipe) -> Unit,
    onNew: () -> Unit,
    onPaste: () -> Unit,
    onQuickAdd: (Recipe) -> Unit,
) {
    // Texto digitado na busca (sobrevive a girar a tela).
    var query by rememberSaveable { mutableStateOf("") }
    // Busca ignorando maiúsculas E acentos ("feijao" acha "Feijão").
    // As mais novas aparecem primeiro.
    val filtered = remember(recipes, query) {
        val q = normalize(query.trim())
        val newestFirst = recipes.reversed()
        if (q.isEmpty()) newestFirst else newestFirst.filter { normalize(it.name).contains(q) }
    }

    Scaffold(
        // A TopAppBar já cuida da barra de status; embaixo fica a barra de
        // abas do app — então nenhum espaço extra aqui.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
                title = {
                    Text(
                        stringResource(R.string.recipes_title),
                        fontFamily = FredokaFamily,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                },
            )
        },
        floatingActionButton = {
            // Só aparece quando já há receitas (no vazio, os botões ficam no meio).
            if (recipes.isNotEmpty()) {
                FloatingActionButton(
                    onClick = onNew,
                    shape = RoundedCornerShape(20.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Icon(
                        painterResource(R.drawable.ic_add),
                        contentDescription = stringResource(R.string.recipes_new),
                    )
                }
            }
        },
    ) { innerPadding ->
        if (recipes.isEmpty()) {
            EmptyRecipes(
                onNew = onNew,
                onPaste = onPaste,
                modifier = Modifier.padding(innerPadding),
            )
            return@Scaffold
        }

        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 96.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalItemSpacing = 18.dp,
        ) {
            // A busca só aparece quando ajuda (4+ receitas) — menos poluição.
            if (recipes.size >= 4) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    SearchField(query = query, onQueryChange = { query = it })
                }
            }

            if (filtered.isEmpty()) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    Text(
                        stringResource(R.string.recipes_none_found),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                    )
                }
            }

            itemsIndexed(filtered, key = { _, recipe -> recipe.id }) { index, recipe ->
                RecipeCard(
                    recipe = recipe,
                    imageHeight = cardHeights[index % cardHeights.size],
                    onOpen = { onOpen(recipe) },
                    onQuickAdd = { onQuickAdd(recipe) },
                )
            }
        }
    }
}

// Campo de busca arredondado, sem borda.
@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text(stringResource(R.string.recipes_search)) },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        leadingIcon = {
            Icon(
                painterResource(R.drawable.ic_search),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        ),
        modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp),
    )
}

// Um card da grade: foto grande (ou capa) + nome + tempo/itens.
@Composable
private fun RecipeCard(
    recipe: Recipe,
    imageHeight: Dp,
    onOpen: () -> Unit,
    onQuickAdd: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .clickable { onOpen() },
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(imageHeight)
                .clip(RoundedCornerShape(22.dp)),
        ) {
            RecipeImage(recipe = recipe, modifier = Modifier.fillMaxSize(), iconSize = 60.dp)

            // Botão redondo: mandar os ingredientes pra lista num toque.
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(10.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.94f))
                    .clickable { onQuickAdd() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painterResource(R.drawable.ic_cart),
                    contentDescription = stringResource(R.string.recipes_add_to_list),
                    tint = Color(0xFF6E5A94), // roxo da marca (o fundo é sempre branco)
                    modifier = Modifier.size(19.dp),
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        Text(
            recipe.name,
            fontFamily = FredokaFamily,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 19.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
        Text(
            recipeMeta(recipe),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 2.dp, bottom = 4.dp),
        )
    }
}

// "40 min · 6 itens" (o tempo só aparece se foi informado).
@Composable
private fun recipeMeta(recipe: Recipe): String {
    val items = pluralStringResource(R.plurals.recipe_items, recipe.itemCount, recipe.itemCount)
    return if (recipe.minutes > 0) "${formatDuration(recipe.minutes)} · $items" else items
}

// Estado vazio (primeiro acesso): convite para criar ou colar a primeira receita.
@Composable
private fun EmptyRecipes(onNew: () -> Unit, onPaste: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(112.dp)
                .clip(RoundedCornerShape(36.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painterResource(R.drawable.ic_recipes),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(52.dp),
            )
        }
        Spacer(Modifier.height(20.dp))
        Text(
            stringResource(R.string.recipes_empty_title),
            fontFamily = FredokaFamily,
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.recipes_empty_text),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FilledTonalButton(onClick = onPaste, modifier = Modifier.height(50.dp)) {
                Icon(painterResource(R.drawable.ic_clipboard), contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.recipes_empty_paste), fontWeight = FontWeight.Bold)
            }
            Button(onClick = onNew, modifier = Modifier.height(50.dp)) {
                Icon(painterResource(R.drawable.ic_add), contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.recipes_empty_create), fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(48.dp))
    }
}
