@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
)

package com.listinha.app.ui.screens

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
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.listinha.app.R
import com.listinha.app.data.Recipe
import com.listinha.app.data.categoryColor
import com.listinha.app.ui.components.BrandAlert
import com.listinha.app.ui.components.BrandTitle
import com.listinha.app.ui.components.FilledPill
import com.listinha.app.ui.components.PaperTicket
import com.listinha.app.ui.components.RecipeImage
import com.listinha.app.ui.theme.Area
import com.listinha.app.ui.theme.AreaTheme
import com.listinha.app.ui.theme.Creme
import com.listinha.app.ui.theme.FredokaFamily
import com.listinha.app.ui.theme.FrauncesFamily
import com.listinha.app.ui.theme.LightPaper
import com.listinha.app.ui.theme.LocalFlavor
import com.listinha.app.ui.theme.LocalPaper
import com.listinha.app.util.formatDuration
import com.listinha.app.util.scaleQuantity

// ============================================================
//  RecipeDetailScreen.kt  ===  o detalhe de uma receita
// ------------------------------------------------------------
//  Foto grande no topo, conteúdo no fundo morango e os ingredientes
//  numa notinha picotada. Aqui moram:
//   • as PORÇÕES ajustáveis (as quantidades se recalculam);
//   • o "JÁ TENHO": tocar num ingrediente risca ele, e só o que
//     falta vai pra lista;
//   • os botões fixos COZINHAR e ADICIONAR À LISTA.
// ============================================================

private val HeroHeight = 372.dp

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
    val have = rememberSaveable(
        recipe.ingredients,
        saver = listSaver<SnapshotStateMap<Int, Boolean>, Int>(
            save = { marcados -> marcados.filterValues { it }.keys.toList() },
            restore = { lista -> mutableStateMapOf<Int, Boolean>().apply { lista.forEach { put(it, true) } } },
        ),
    ) { mutableStateMapOf() }
    val missing = ingredients.filterIndexed { i, _ -> have[i] != true }
    val recipeToSend = recipe.copy(ingredients = missing)

    // Caixas abertas?
    var menuOpen by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    var confirmReplace by remember { mutableStateOf(false) }
    var cooking by rememberSaveable(recipe.id) { mutableStateOf(false) }

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
    val hasPhoto = recipe.photo.isNotBlank()

    // Hora e bateria ficam claras sobre a foto; no resto, seguem o fundo.
    AreaTheme(Area.RECEITAS, lightStatusIcons = if (hasPhoto && !collapsed) true else null) {
        val flavor = LocalFlavor.current
        val paper = LocalPaper.current
        Column(Modifier.fillMaxSize().background(flavor.bg)) {
            Box(Modifier.weight(1f).fillMaxWidth()) {
                Column(Modifier.fillMaxSize().verticalScroll(scroll)) {
                    RecipeImage(
                        recipe = recipe,
                        modifier = Modifier.fillMaxWidth().height(HeroHeight),
                        iconSize = 140.dp,
                    )

                    // O conteúdo sobe 34dp por cima da foto.
                    Column(
                        modifier = Modifier
                            .offset(y = (-34).dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(topStart = 34.dp, topEnd = 34.dp))
                            .background(flavor.bg)
                            .padding(start = 20.dp, end = 20.dp, top = 28.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp),
                    ) {
                        BrandTitle(recipe.name.replace("*", ""), fontSize = 46.sp)

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

                        if (ingredients.isNotEmpty()) {
                            PaperTicket(Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        stringResource(R.string.detail_ingredients),
                                        fontFamily = FrauncesFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 25.sp,
                                        letterSpacing = (-0.02).em,
                                        color = paper.ink,
                                    )
                                    Text(
                                        stringResource(R.string.detail_tap_have),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = paper.muted,
                                    )
                                }
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
                        }

                        if (recipe.steps.isNotEmpty()) {
                            BrandTitle(
                                stringResource(R.string.detail_steps),
                                fontSize = 32.sp,
                                modifier = Modifier.padding(top = 10.dp),
                            )
                            recipe.steps.forEachIndexed { index, step ->
                                Row(Modifier.fillMaxWidth()) {
                                    Box(
                                        modifier = Modifier.size(32.dp).clip(CircleShape).background(flavor.title),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            "${index + 1}",
                                            fontFamily = FredokaFamily,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = flavor.on,
                                        )
                                    }
                                    Spacer(Modifier.width(14.dp))
                                    Text(
                                        step,
                                        fontSize = 16.sp,
                                        lineHeight = 25.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = flavor.text,
                                        modifier = Modifier.weight(1f).padding(top = 3.dp),
                                    )
                                }
                            }
                        }
                        // Folga final: o offset de -34dp não encurta a coluna.
                        Spacer(Modifier.height(8.dp))
                    }
                }

                // Barra do topo, fixa: voltar, editar e menu. Ganha fundo ao rolar.
                val barColor by animateColorAsState(
                    if (collapsed) flavor.bg else Color.Transparent,
                    label = "barColor",
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(barColor)
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircleIconButton(R.drawable.ic_back, stringResource(R.string.action_back), collapsed, onBack)
                    if (collapsed) {
                        Text(
                            recipe.name,
                            fontFamily = FrauncesFamily,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = flavor.title,
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
                                text = { Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    menuOpen = false
                                    confirmDelete = true
                                },
                            )
                        }
                    }
                }
            }

            DetailBottomBar(
                hasSteps = recipe.steps.isNotEmpty(),
                total = ingredients.size,
                missing = missing.size,
                onCook = { cooking = true },
                onAdd = { onAddToList(recipeToSend) },
            )
        }

        // Confirmação antes de excluir a receita (ação sem volta).
        if (confirmDelete) {
            BrandAlert(
                title = stringResource(R.string.detail_delete_title),
                text = stringResource(R.string.detail_delete_text, recipe.name),
                confirmLabel = stringResource(R.string.action_delete),
                danger = true,
                onConfirm = {
                    confirmDelete = false
                    onDelete()
                },
                onDismiss = { confirmDelete = false },
            )
        }

        // Confirmação antes de SUBSTITUIR a lista atual pela receita.
        if (confirmReplace) {
            BrandAlert(
                title = stringResource(R.string.detail_replace_title),
                text = stringResource(R.string.detail_replace_text),
                confirmLabel = stringResource(R.string.detail_replace_confirm),
                danger = true,
                onConfirm = {
                    confirmReplace = false
                    onReplaceList(recipeToSend)
                },
                onDismiss = { confirmReplace = false },
            )
        }
    }
}

// Botão redondo do topo: creme sobre a foto; "limpo" quando a barra tem fundo.
@Composable
private fun CircleIconButton(icon: Int, description: String, flat: Boolean, onClick: () -> Unit) {
    val flavor = LocalFlavor.current
    Surface(
        onClick = onClick,
        modifier = Modifier.size(48.dp),
        shape = CircleShape,
        color = if (flat) Color.Transparent else Creme,
        contentColor = if (flat) flavor.title else LightPaper.ink,
        shadowElevation = if (flat) 0.dp else 6.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(painterResource(icon), contentDescription = description, modifier = Modifier.size(21.dp))
        }
    }
}

// Pílula de informação (tempo, link).
@Composable
private fun InfoChip(icon: Int, text: String, onClick: (() -> Unit)? = null) {
    val paper = LocalPaper.current
    Row(
        modifier = Modifier
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(paper.paper)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(painterResource(icon), contentDescription = null, tint = paper.ink, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = paper.ink)
    }
}

// "8 porções  −  +": muda as quantidades na hora.
@Composable
private fun ServingsChip(servings: Int, onLess: () -> Unit, onMore: () -> Unit) {
    val paper = LocalPaper.current
    Row(
        modifier = Modifier
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(paper.paper)
            .padding(start = 16.dp, end = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            pluralStringResource(R.plurals.detail_servings, servings, servings),
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            color = paper.ink,
        )
        Spacer(Modifier.width(4.dp))
        StepperButton(R.drawable.ic_remove, stringResource(R.string.detail_less_servings), onLess)
        StepperButton(R.drawable.ic_add, stringResource(R.string.detail_more_servings), onMore)
    }
}

// Círculo visível de 36dp dentro de uma área de toque de 44dp.
@Composable
private fun StepperButton(icon: Int, description: String, onClick: () -> Unit) {
    val flavor = LocalFlavor.current
    Box(
        modifier = Modifier.size(44.dp).clip(CircleShape).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(Modifier.size(36.dp).clip(CircleShape).background(flavor.soft), contentAlignment = Alignment.Center) {
            Icon(painterResource(icon), contentDescription = description, tint = flavor.titleOnPaper, modifier = Modifier.size(16.dp))
        }
    }
}

// Uma linha de ingrediente: círculo na cor da categoria, que vira "visto" ao tocar.
@Composable
private fun IngredientRow(
    name: String,
    quantity: String,
    color: Color,
    checked: Boolean,
    onToggle: () -> Unit,
) {
    val paper = LocalPaper.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 50.dp)
            .toggleable(value = checked, role = Role.Checkbox, onValueChange = { onToggle() })
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .then(if (checked) Modifier.background(paper.muted) else Modifier.border(2.5.dp, color, CircleShape)),
            contentAlignment = Alignment.Center,
        ) {
            if (checked) {
                Icon(painterResource(R.drawable.ic_check), contentDescription = null, tint = paper.paper, modifier = Modifier.size(14.dp))
            }
        }
        Spacer(Modifier.width(14.dp))
        Text(
            name,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = if (checked) paper.muted else paper.ink,
            textDecoration = if (checked) TextDecoration.LineThrough else null,
            modifier = Modifier.weight(1f),
        )
        if (quantity.isNotBlank()) {
            Spacer(Modifier.width(10.dp))
            Text(quantity, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = paper.muted)
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
    val flavor = LocalFlavor.current
    val paper = LocalPaper.current
    Surface(color = paper.paper, shadowElevation = 10.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (hasSteps) {
                FilledPill(
                    text = stringResource(R.string.detail_cook),
                    onClick = onCook,
                    icon = R.drawable.ic_cook,
                    height = 56.dp,
                    container = flavor.soft,
                    contentColor = flavor.titleOnPaper,
                    fontSize = 16.sp,
                    horizontalPadding = 12.dp,
                )
            }
            if (total > 0) {
                FilledPill(
                    text = when {
                        missing == 0 -> stringResource(R.string.detail_have_all)
                        missing == total -> stringResource(R.string.detail_add_all)
                        else -> stringResource(R.string.detail_add_some, missing)
                    },
                    onClick = onAdd,
                    enabled = missing > 0,
                    icon = if (missing > 0) R.drawable.ic_cart else null,
                    height = 56.dp,
                    fontSize = 16.sp,
                    horizontalPadding = 12.dp,
                    modifier = Modifier.weight(1f),
                )
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
