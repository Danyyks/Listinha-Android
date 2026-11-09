@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.listinha.app.ui.screens

import android.app.Activity
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.listinha.app.R
import com.listinha.app.data.Recipe
import com.listinha.app.recipes.RecipeViewModel
import com.listinha.app.ui.components.BrandField
import com.listinha.app.ui.components.BrandTitle
import com.listinha.app.ui.components.EmptyArt
import com.listinha.app.ui.components.FilledPill
import com.listinha.app.ui.components.OutlinePill
import com.listinha.app.ui.components.RecipeImage
import com.listinha.app.ui.components.RoundIconButton
import com.listinha.app.ui.components.capFontScale
import com.listinha.app.ui.theme.Area
import com.listinha.app.ui.theme.AreaTheme
import com.listinha.app.ui.theme.FrauncesFamily
import com.listinha.app.ui.theme.LocalFlavor
import com.listinha.app.ui.theme.LocalPaper
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

// As "telas" possíveis dentro da aba Receitas.
internal sealed interface RecipesRoute {
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

// A rota mora num ViewModel porque guarda a receita inteira, com a foto em base64,
// grande demais para o estado salvo do Android. Assim ela sobrevive a girar a tela.
internal class RecipesNavState : ViewModel() {
    var route by mutableStateOf<RecipesRoute>(RecipesRoute.Grid)
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
    LaunchedEffect(userId) { vm.start(userId) }

    // Se salvar/editar/excluir uma receita falhar, avisa com um Toast.
    val context = LocalContext.current
    LaunchedEffect(vm.errorMessage) {
        vm.errorMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            vm.clearError()
        }
    }

    val nav: RecipesNavState = viewModel()
    var route by nav::route

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
    DisposableEffect(Unit) {
        onDispose {
            onImmersiveChange(false)
            // Sair da aba volta para a grade; girar a tela mantém onde a pessoa estava.
            if ((context as? Activity)?.isChangingConfigurations != true) nav.route = RecipesRoute.Grid
        }
    }

    // O botão "voltar" do celular volta para a tela anterior da aba
    // (antes ele fechava o app direto).
    BackHandler(enabled = route !is RecipesRoute.Grid) {
        route = when (val r = route) {
            is RecipesRoute.Form -> r.editing?.let { RecipesRoute.Detail(it.id) } ?: RecipesRoute.Grid
            else -> RecipesRoute.Grid
        }
    }

    // A grade e o detalhe são morango; criar e editar receita é mirtilo.
    AreaTheme(if (route is RecipesRoute.Form) Area.NOVA_RECEITA else Area.RECEITAS) {
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
}

// Alturas das fotos na grade, alternadas para o efeito "mosaico".
private val cardHeights = listOf(214.dp, 166.dp, 190.dp, 174.dp)

@Composable
internal fun RecipesGridScreen(
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

    val flavor = LocalFlavor.current

    if (recipes.isEmpty()) {
        EmptyRecipes(onNew = onNew, onPaste = onPaste)
        return
    }

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        modifier = Modifier.fillMaxSize().background(flavor.bg).statusBarsPadding(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 32.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalItemSpacing = 18.dp,
    ) {
        item(span = StaggeredGridItemSpan.FullLine) {
            RecipesHeader(onNew)
        }

        // A busca só aparece quando ajuda (4 receitas ou mais).
        if (recipes.size >= 4) {
            item(span = StaggeredGridItemSpan.FullLine) {
                BrandField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = stringResource(R.string.recipes_search),
                    description = stringResource(R.string.recipes_search),
                    modifier = Modifier.fillMaxWidth(),
                    height = 54.dp,
                    fontSize = 16.sp,
                    shadow = true,
                    leadingIcon = R.drawable.ic_search,
                )
            }
        }

        if (filtered.isEmpty()) {
            item(span = StaggeredGridItemSpan.FullLine) {
                Text(
                    stringResource(R.string.recipes_none_found),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = flavor.secondary,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 24.dp),
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

// Título da aba com o "+" inclinado ao lado, no lugar do antigo botão flutuante.
@Composable
private fun RecipesHeader(onNew: () -> Unit) {
    val paper = LocalPaper.current
    val forma = RoundedCornerShape(18.dp)
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BrandTitle(stringResource(R.string.recipes_title), fontSize = 44.sp, modifier = Modifier.weight(1f))
        RoundIconButton(
            icon = R.drawable.ic_add,
            description = stringResource(R.string.recipes_new),
            onClick = onNew,
            size = 52.dp,
            shape = forma,
            modifier = Modifier
                .rotate(-4f)
                .shadow(10.dp, forma, ambientColor = paper.shadow, spotColor = paper.shadow),
        )
    }
}

// Um card da grade: foto grande (ou capa) + nome + tempo/itens.
@Composable
private fun RecipeCard(
    recipe: Recipe,
    imageHeight: Dp,
    onOpen: () -> Unit,
    onQuickAdd: () -> Unit,
) {
    val flavor = LocalFlavor.current
    val paper = LocalPaper.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable { onOpen() },
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(imageHeight)
                .clip(RoundedCornerShape(24.dp)),
        ) {
            RecipeImage(recipe = recipe, modifier = Modifier.fillMaxSize(), iconSize = 72.dp)

            // Manda os ingredientes para a lista num toque.
            RoundIconButton(
                icon = R.drawable.ic_cart,
                description = stringResource(R.string.recipes_add_to_list),
                onClick = onQuickAdd,
                size = 48.dp,
                iconSize = 20.dp,
                container = paper.paper,
                contentColor = flavor.titleOnPaper,
                modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp),
            )
        }

        Spacer(Modifier.height(8.dp))
        Text(
            recipe.name,
            fontFamily = FrauncesFamily,
            fontWeight = FontWeight.Bold,
            fontSize = capFontScale(19.sp, 1.25f),
            lineHeight = capFontScale(21.sp, 1.25f),
            letterSpacing = (-0.01).em,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            color = flavor.text,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
        Text(
            recipeMeta(recipe),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = flavor.secondary,
            modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
        )
    }
}

// "40 min · 6 itens" (o tempo só aparece se foi informado).
@Composable
private fun recipeMeta(recipe: Recipe): String {
    val items = pluralStringResource(R.plurals.recipe_items, recipe.itemCount, recipe.itemCount)
    return if (recipe.minutes > 0) "${formatDuration(recipe.minutes)} · $items" else items
}

// Primeiro acesso: convite para criar ou colar a primeira receita.
@Composable
private fun EmptyRecipes(onNew: () -> Unit, onPaste: () -> Unit) {
    val flavor = LocalFlavor.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(flavor.bg)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        BrandTitle(stringResource(R.string.recipes_title), fontSize = 44.sp)
        EmptyArt("sua *receita*", R.drawable.empty_strawberry)
        BrandTitle(stringResource(R.string.recipes_empty_title), fontSize = 34.sp)
        Text(
            stringResource(R.string.recipes_empty_text),
            fontSize = 16.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.SemiBold,
            color = flavor.secondary,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinePill(stringResource(R.string.recipes_empty_paste), onPaste, Modifier.weight(1f), height = 52.dp)
            FilledPill(stringResource(R.string.recipes_empty_create), onNew, Modifier.weight(1f), height = 52.dp)
        }
    }
}
