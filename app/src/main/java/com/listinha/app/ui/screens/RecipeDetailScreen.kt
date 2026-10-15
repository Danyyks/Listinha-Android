@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
)

package com.listinha.app.ui.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.listinha.app.R
import com.listinha.app.data.Recipe
import com.listinha.app.data.categoryColor
import com.listinha.app.ui.components.RecipeImage
import com.listinha.app.ui.theme.FredokaFamily
import com.listinha.app.util.formatDuration
import com.listinha.app.util.scaleQuantity

// ============================================================
//  RecipeDetailScreen.kt  ===  o detalhe de uma receita
// ------------------------------------------------------------
//  Estilo "post de blog": foto grande no topo e o conteúdo num
//  cartão que sobe por cima dela. Aqui moram:
//   • as PORÇÕES ajustáveis (as quantidades se recalculam);
//   • o "JÁ TENHO": tocar num ingrediente risca ele, e só o que
//     falta vai pra lista;
//   • os botões fixos COZINHAR e ADICIONAR À LISTA.
// ============================================================

private val HeroHeight = 310.dp

@Composable
fun RecipeDetailScreen(
    recipe: Recipe,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAddToList: (Recipe) -> Unit,   // recebe a receita só com o que falta, já nas porções escolhidas
    onReplaceList: (Recipe) -> Unit,
) {
    val context = LocalContext.current

    // Porções escolhidas na tela (começa nas da receita).
    var servings by rememberSaveable(recipe.id) { mutableIntStateOf(recipe.servings.coerceAtLeast(1)) }
    val factor = servings.toDouble() / recipe.servings.coerceAtLeast(1)
    val ingredients = remember(recipe.ingredients, factor) {
        recipe.ingredients.map { it.copy(quantity = scaleQuantity(it.quantity, factor)) }
    }

    // Ingredientes marcados como "já tenho" (pela posição na lista).
    val have = remember(recipe.ingredients) { mutableStateMapOf<Int, Boolean>() }
    val missing = ingredients.filterIndexed { i, _ -> have[i] != true }
    val recipeToSend = recipe.copy(ingredients = missing)

    // Caixas abertas?
    var menuOpen by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    var confirmReplace by remember { mutableStateOf(false) }
    var cooking by remember { mutableStateOf(false) }

    // Modo cozinhar: ocupa a tela toda no lugar do detalhe.
    if (cooking) {
        CookModeScreen(
            recipeName = recipe.name,
            ingredients = ingredients,
            steps = recipe.steps,
            onClose = { cooking = false },
        )
        return
    }

    // Quando o conteúdo cobre a foto, a barra do topo ganha fundo e o nome.
    val scroll = rememberScrollState()
    val density = LocalDensity.current
    val collapseAt = with(density) { (HeroHeight - 100.dp).toPx() }
    val collapsed by remember { derivedStateOf { scroll.value > collapseAt } }

    // Ícones da barra de status brancos sobre a FOTO; escuros no resto.
    val hasPhoto = recipe.photo.isNotBlank()
    val darkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    StatusBarIcons(light = darkTheme || (hasPhoto && !collapsed))

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = WindowInsets(0, 0, 0, 0), // a foto vai até o topo da tela
        bottomBar = {
            DetailBottomBar(
                hasSteps = recipe.steps.isNotEmpty(),
                total = ingredients.size,
                missing = missing.size,
                onCook = { cooking = true },
                onAdd = { onAddToList(recipeToSend) },
            )
        },
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(bottom = innerPadding.calculateBottomPadding())) {
            Column(Modifier.fillMaxSize().verticalScroll(scroll)) {
                RecipeImage(
                    recipe = recipe,
                    modifier = Modifier.fillMaxWidth().height(HeroHeight),
                    iconSize = 104.dp,
                )

                // O cartão que "sobe" 28dp por cima da foto.
                Column(
                    modifier = Modifier
                        .offset(y = (-28).dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(start = 22.dp, end = 22.dp, top = 24.dp, bottom = 8.dp),
                ) {
                    Text(
                        recipe.name,
                        fontFamily = FredokaFamily,
                        fontSize = 27.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 31.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.height(12.dp))

                    // Tempo · porções (com − e +) · link da receita original.
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        if (recipe.minutes > 0) {
                            InfoChip(icon = R.drawable.ic_clock, text = formatDuration(recipe.minutes))
                        }
                        ServingsChip(
                            servings = servings,
                            onLess = { if (servings > 1) servings-- },
                            onMore = { if (servings < 99) servings++ },
                        )
                        if (recipe.sourceUrl.isNotBlank()) {
                            InfoChip(
                                icon = R.drawable.ic_link,
                                text = stringResource(R.string.detail_original),
                                onClick = {
                                    try {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(recipe.sourceUrl)))
                                    } catch (e: Exception) {
                                        Toast.makeText(context, R.string.detail_open_link_error, Toast.LENGTH_SHORT).show()
                                    }
                                },
                            )
                        }
                    }

                    // --- Ingredientes ("toque no que já tem") ---
                    if (ingredients.isNotEmpty()) {
                        Spacer(Modifier.height(22.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom,
                        ) {
                            SectionTitle(stringResource(R.string.detail_ingredients))
                            Text(
                                stringResource(R.string.detail_tap_have),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        ingredients.forEachIndexed { i, ing ->
                            IngredientRow(
                                name = ing.name,
                                quantity = ing.quantity,
                                color = categoryColor(ing.category),
                                checked = have[i] == true,
                                onToggle = { have[i] = have[i] != true },
                            )
                        }
                    }

                    // --- Modo de preparo ---
                    if (recipe.steps.isNotEmpty()) {
                        Spacer(Modifier.height(20.dp))
                        SectionTitle(stringResource(R.string.detail_steps))
                        Spacer(Modifier.height(6.dp))
                        recipe.steps.forEachIndexed { index, step ->
                            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        "${index + 1}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    step,
                                    fontSize = 15.sp,
                                    lineHeight = 23.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.86f),
                                    modifier = Modifier.weight(1f).padding(top = 2.dp),
                                )
                            }
                        }
                    }
                }
            }

            // Barra do topo, fixa: voltar, editar e menu. Ganha fundo ao rolar.
            val barColor by animateColorAsState(
                if (collapsed) MaterialTheme.colorScheme.surface else Color.Transparent,
                label = "barColor",
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(barColor)
                    .statusBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CircleIconButton(R.drawable.ic_back, stringResource(R.string.action_back), collapsed, onBack)
                if (collapsed) {
                    Text(
                        recipe.name,
                        fontFamily = FredokaFamily,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                    )
                } else {
                    Spacer(Modifier.weight(1f))
                }
                CircleIconButton(R.drawable.ic_edit, stringResource(R.string.action_edit), collapsed, onEdit)
                Spacer(Modifier.width(10.dp))
                Box {
                    CircleIconButton(R.drawable.ic_more_vert, stringResource(R.string.action_more), collapsed) {
                        menuOpen = true
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        if (missing.isNotEmpty()) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.detail_replace_list)) },
                                onClick = {
                                    menuOpen = false
                                    confirmReplace = true
                                },
                            )
                        }
                        DropdownMenuItem(
                            text = {
                                Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error)
                            },
                            onClick = {
                                menuOpen = false
                                confirmDelete = true
                            },
                        )
                    }
                }
            }
        }
    }

    // Confirmação antes de excluir a receita (ação sem volta).
    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.detail_delete_title)) },
            text = { Text(stringResource(R.string.detail_delete_text, recipe.name)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    onDelete()
                }) { Text(stringResource(R.string.action_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text(stringResource(R.string.action_cancel)) }
            },
        )
    }

    // Confirmação antes de SUBSTITUIR a lista atual pela receita.
    if (confirmReplace) {
        AlertDialog(
            onDismissRequest = { confirmReplace = false },
            title = { Text(stringResource(R.string.detail_replace_title)) },
            text = { Text(stringResource(R.string.detail_replace_text)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmReplace = false
                    onReplaceList(recipeToSend)
                }) { Text(stringResource(R.string.detail_replace_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmReplace = false }) { Text(stringResource(R.string.action_cancel)) }
            },
        )
    }
}

// Troca a cor dos ícones da barra de status (relógio, bateria) enquanto
// esta tela estiver aberta, e devolve como estava ao sair.
@Composable
private fun StatusBarIcons(light: Boolean) {
    val view = LocalView.current
    val window = (view.context as? Activity)?.window ?: return
    val controller = remember(window) { WindowCompat.getInsetsController(window, view) }
    val original = remember(controller) { controller.isAppearanceLightStatusBars }
    // "AppearanceLight" = ícones ESCUROS (para fundo claro). Por isso o "!".
    LaunchedEffect(light) { controller.isAppearanceLightStatusBars = !light }
    DisposableEffect(controller) { onDispose { controller.isAppearanceLightStatusBars = original } }
}

// Botão redondo do topo: branco sobre a foto; "limpo" quando a barra tem fundo.
@Composable
private fun CircleIconButton(icon: Int, description: String, flat: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(if (flat) Color.Transparent else Color.White.copy(alpha = 0.94f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painterResource(icon),
            contentDescription = description,
            tint = if (flat) MaterialTheme.colorScheme.onSurface else Color(0xFF2B2233),
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        fontSize = 17.sp,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.onSurface,
    )
}

// Pílula de informação (tempo, link).
@Composable
private fun InfoChip(icon: Int, text: String, onClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .height(36.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painterResource(icon),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.width(6.dp))
        Text(text, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}

// "8 porções  −  +" — muda as quantidades na hora.
@Composable
private fun ServingsChip(servings: Int, onLess: () -> Unit, onMore: () -> Unit) {
    Row(
        modifier = Modifier
            .height(36.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(start = 12.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            pluralStringResource(R.plurals.detail_servings, servings, servings),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.width(8.dp))
        StepperButton(R.drawable.ic_remove, stringResource(R.string.detail_less_servings), onLess)
        Spacer(Modifier.width(4.dp))
        StepperButton(R.drawable.ic_add, stringResource(R.string.detail_more_servings), onMore)
    }
}

@Composable
private fun StepperButton(icon: Int, description: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painterResource(icon),
            contentDescription = description,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp),
        )
    }
}

// Uma linha de ingrediente: bolinha na cor da categoria (vira "visto" ao tocar).
@Composable
private fun IngredientRow(
    name: String,
    quantity: String,
    color: Color,
    checked: Boolean,
    onToggle: () -> Unit,
) {
    val faded = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 46.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onToggle)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .then(
                    if (checked) Modifier.background(MaterialTheme.colorScheme.outline)
                    else Modifier.border(2.dp, color, CircleShape)
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (checked) {
                Icon(
                    painterResource(R.drawable.ic_check),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Text(
            name,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (checked) faded else MaterialTheme.colorScheme.onSurface,
            textDecoration = if (checked) TextDecoration.LineThrough else null,
            modifier = Modifier.weight(1f),
        )
        if (quantity.isNotBlank()) {
            Spacer(Modifier.width(10.dp))
            Text(
                quantity,
                fontSize = 14.sp,
                color = if (checked) faded else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// Barra fixa de baixo: [Cozinhar] [Adicionar N à lista].
@Composable
private fun DetailBottomBar(
    hasSteps: Boolean,
    total: Int,
    missing: Int,
    onCook: () -> Unit,
    onAdd: () -> Unit,
) {
    Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 10.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (hasSteps) {
                FilledTonalButton(onClick = onCook, modifier = Modifier.height(54.dp)) {
                    Icon(painterResource(R.drawable.ic_cook), contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.detail_cook), fontWeight = FontWeight.ExtraBold)
                }
            }
            if (total > 0) {
                Button(
                    onClick = onAdd,
                    enabled = missing > 0,
                    modifier = Modifier.weight(1f).height(54.dp),
                ) {
                    if (missing > 0) {
                        Icon(painterResource(R.drawable.ic_cart), contentDescription = null, modifier = Modifier.size(19.dp))
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                        when {
                            missing == 0 -> stringResource(R.string.detail_have_all)
                            missing == total -> stringResource(R.string.detail_add_all)
                            else -> stringResource(R.string.detail_add_some, missing)
                        },
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

// Selo "PRO" (reservado para os recursos Premium com IA).
@Composable
fun ProBadge() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(
            "PRO",
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}
