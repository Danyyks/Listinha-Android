@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.listinha.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.listinha.app.R
import com.listinha.app.data.Recipe
import com.listinha.app.recipes.RecipeViewModel
import com.listinha.app.util.decodeBase64ToImageBitmap
import com.listinha.app.ui.theme.FredokaFamily

// ============================================================
//  RecipesScreen.kt  ===  a aba "Receitas"
// ------------------------------------------------------------
//  RecipesTab é o "cérebro": mostra a LISTA de receitas ou, se
//  o usuário tocar numa, mostra o DETALHE. RecipesListScreen é
//  a tela da lista em si (busca + botões + cards).
// ============================================================

// Ícones disponíveis para as receitas (miniaturas).
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

// Qual miniatura usar para uma receita (pelo índice guardado nela).
internal fun iconForRecipe(recipe: Recipe): Int =
    recipeIconResList[recipe.iconIndex.coerceIn(0, recipeIconResList.lastIndex)]

// As "telas" possíveis dentro da aba Receitas.
private sealed interface RecipesRoute {
    data object List : RecipesRoute
    data class Detail(val recipe: Recipe) : RecipesRoute
    data class Form(val recipe: Recipe?) : RecipesRoute // null = nova receita
}

@Composable
fun RecipesTab(
    userId: String,                       // uid do usuário logado (identifica a conta)
    onAddToList: (Recipe) -> Unit,
    onCreateListFromRecipe: (Recipe) -> Unit,
) {
    // O ViewModel segura as receitas (vindas do Firestore) e as ações.
    val vm: RecipeViewModel = viewModel()
    // Reinicia as receitas sempre que a conta logada muda (mesmo motivo da
    // HomeScreen: o ViewModel sobrevive à troca de tela).
    LaunchedEffect(userId) { vm.start() }

    // Se salvar/editar/excluir uma receita falhar, avisa com um Toast.
    val context = LocalContext.current
    LaunchedEffect(vm.errorMessage) {
        vm.errorMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            vm.clearError()
        }
    }
    // Em qual "tela" da aba estamos (lista, detalhe ou formulário).
    var route by remember { mutableStateOf<RecipesRoute>(RecipesRoute.List) }

    when (val r = route) {
        RecipesRoute.List -> RecipesListScreen(
            recipes = vm.recipes,
            onOpen = { route = RecipesRoute.Detail(it) },
            onNew = { route = RecipesRoute.Form(null) },
            onQuickAdd = { onAddToList(it) },
        )

        is RecipesRoute.Detail -> RecipeDetailScreen(
            recipe = r.recipe,
            onBack = { route = RecipesRoute.List },
            onEdit = { route = RecipesRoute.Form(r.recipe) },
            onDelete = {
                vm.delete(r.recipe)
                route = RecipesRoute.List
            },
            onAddToList = {
                onAddToList(r.recipe)
                route = RecipesRoute.List
            },
            onCreateList = {
                onCreateListFromRecipe(r.recipe)
                route = RecipesRoute.List
            },
        )

        is RecipesRoute.Form -> RecipeFormScreen(
            initial = r.recipe,
            onCancel = {
                route = if (r.recipe != null) RecipesRoute.Detail(r.recipe) else RecipesRoute.List
            },
            onSave = { novo ->
                if (r.recipe == null) vm.add(novo) else vm.update(r.recipe.id, novo)
                route = RecipesRoute.List
            },
        )
    }
}

@Composable
private fun RecipesListScreen(
    recipes: List<Recipe>,
    onOpen: (Recipe) -> Unit,
    onNew: () -> Unit,
    onQuickAdd: (Recipe) -> Unit,
) {
    // Texto digitado na busca.
    var query by remember { mutableStateOf("") }

    // Receitas que batem com a busca (ignora maiúsculas/minúsculas).
    val filtered = recipes.filter { it.name.contains(query.trim(), ignoreCase = true) }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
                title = {
                    Text(
                        "Receitas",
                        fontFamily = FredokaFamily,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                "Todas as suas receitas em um só lugar",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 14.dp),
            )

            // Campo de busca.
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Buscar receita...") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                leadingIcon = {
                    Icon(
                        painterResource(R.drawable.ic_search),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(14.dp))

            // Botão primário: Nova receita (grátis) — abre o formulário.
            Button(
                onClick = onNew,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(15.dp),
            ) {
                Icon(painterResource(R.drawable.ic_add), contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Nova receita", fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(18.dp))

            if (recipes.isEmpty()) {
                // Lista totalmente vazia (primeiro acesso): estado convidativo.
                EmptyRecipes()
            } else {
                // Cabeçalho da lista.
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Text(
                        "Minhas receitas",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(
                        "${filtered.size} receitas",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                if (filtered.isEmpty()) {
                    // Busca sem resultado.
                    Text(
                        "Nenhuma receita encontrada.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                    )
                } else {
                    // Um card para cada receita.
                    filtered.forEach { recipe ->
                        RecipeCard(
                            recipe = recipe,
                            onOpen = { onOpen(recipe) },
                            onQuickAdd = { onQuickAdd(recipe) },
                        )
                        Spacer(Modifier.height(10.dp))
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// Um card de receita: miniatura + nome + info + botão de "mandar pra lista".
@Composable
private fun RecipeCard(
    recipe: Recipe,
    onOpen: () -> Unit,
    onQuickAdd: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onOpen() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Miniatura: a foto da receita ou, se não tiver, o ícone.
            val thumb = remember(recipe.photo) { decodeBase64ToImageBitmap(recipe.photo) }
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                if (thumb != null) {
                    Image(
                        bitmap = thumb,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Icon(
                        painterResource(iconForRecipe(recipe)),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp),
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            // Nome + informações (porções, itens).
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    recipe.name,
                    fontFamily = FredokaFamily,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painterResource(R.drawable.ic_cart),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(13.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "${recipe.itemCount} itens",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Botão redondo: mandar os ingredientes pra lista.
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable { onQuickAdd() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painterResource(R.drawable.ic_cart),
                    contentDescription = "Adicionar à lista",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(19.dp),
                )
            }
        }
    }
}

// Estado vazio (primeiro acesso): convite para criar a primeira receita.
@Composable
private fun EmptyRecipes() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 40.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(108.dp)
                .clip(RoundedCornerShape(50))
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
        Spacer(Modifier.height(16.dp))
        Text(
            "Nenhuma receita ainda",
            fontFamily = FredokaFamily,
            fontSize = 21.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "Crie sua primeira receita e mande os ingredientes pra lista num toque.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
