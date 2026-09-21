@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.listinha.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.listinha.app.R
import com.listinha.app.data.Recipe
import com.listinha.app.data.categoryColor
import com.listinha.app.util.decodeBase64ToImageBitmap
import com.listinha.app.ui.theme.FredokaFamily

// ============================================================
//  RecipeDetailScreen.kt  ===  o detalhe de uma receita
// ------------------------------------------------------------
//  Onde mora a mágica "receita -> lista": mostra os ingredientes
//  (com a bolinha da categoria, igual à tela de Lista) e o modo
//  de preparo, e embaixo os botões que jogam tudo na lista.
//  Por enquanto os botões só avisam "em breve" (o vínculo real
//  com a lista chega nas próximas fases).
// ============================================================

@Composable
fun RecipeDetailScreen(
    recipe: Recipe,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAddToList: () -> Unit,
    onCreateList: () -> Unit,
) {
    // Caixas de confirmação abertas?
    var showDelete by remember { mutableStateOf(false) }
    var showCreateList by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painterResource(R.drawable.ic_back),
                            contentDescription = "Voltar",
                            tint = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                },
                title = {
                    Text(
                        "Detalhes da receita",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                actions = {
                    // Editar e excluir a receita.
                    IconButton(onClick = onEdit) {
                        Icon(
                            painterResource(R.drawable.ic_edit),
                            contentDescription = "Editar",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                    IconButton(onClick = { showDelete = true }) {
                        Icon(
                            painterResource(R.drawable.ic_delete),
                            contentDescription = "Excluir",
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
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
            // --- Cabeçalho: foto (banner) OU ícone + nome ---
            val headerPhoto = remember(recipe.photo) { decodeBase64ToImageBitmap(recipe.photo) }
            if (headerPhoto != null) {
                Image(
                    bitmap = headerPhoto,
                    contentDescription = recipe.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(20.dp)),
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    recipe.name,
                    fontFamily = FredokaFamily,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    "${recipe.itemCount} ingredientes",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(66.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                painterResource(iconForRecipe(recipe)),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(34.dp),
                            )
                        }
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text(
                                recipe.name,
                                fontFamily = FredokaFamily,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                "${recipe.itemCount} ingredientes",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // --- Ingredientes: bolinha da categoria + nome + quantidade ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Ingredientes (${recipe.itemCount})",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.height(8.dp))
                    recipe.ingredients.forEach { ing ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            // A "bolinha" da categoria (mesma cor da tela de Lista).
                            Spacer(
                                Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(categoryColor(ing.category)),
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                ing.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                ing.quantity,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            // --- Modo de preparo (se a receita tiver passos) ---
            if (recipe.steps.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "Modo de preparo",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(Modifier.height(10.dp))
                        recipe.steps.forEachIndexed { index, step ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 7.dp),
                            ) {
                                // Numerozinho do passo, num círculo roxo claro.
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        "${index + 1}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    step,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // --- Ações mágicas: receita vira lista ---
            Button(
                onClick = onAddToList,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(15.dp),
            ) {
                Icon(painterResource(R.drawable.ic_cart), contentDescription = null, modifier = Modifier.size(19.dp))
                Spacer(Modifier.width(8.dp))
                Text("Adicionar à minha lista", fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(10.dp))

            OutlinedButton(
                onClick = { showCreateList = true },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(15.dp),
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
            ) {
                Text("Criar nova lista com esta receita", fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    // Confirmação antes de excluir a receita (ação sem volta).
    if (showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            title = { Text("Excluir receita?") },
            text = { Text("A receita \"${recipe.name}\" será removida. Essa ação não pode ser desfeita.") },
            confirmButton = {
                TextButton(onClick = {
                    showDelete = false
                    onDelete()
                }) { Text("Excluir") }
            },
            dismissButton = {
                TextButton(onClick = { showDelete = false }) { Text("Cancelar") }
            },
        )
    }

    // Confirmação antes de SUBSTITUIR a lista atual pela receita.
    if (showCreateList) {
        AlertDialog(
            onDismissRequest = { showCreateList = false },
            title = { Text("Criar nova lista?") },
            text = { Text("Isso apaga os itens da sua lista atual e coloca os ingredientes desta receita no lugar.") },
            confirmButton = {
                TextButton(onClick = {
                    showCreateList = false
                    onCreateList()
                }) { Text("Criar") }
            },
            dismissButton = {
                TextButton(onClick = { showCreateList = false }) { Text("Cancelar") }
            },
        )
    }
}


// Selo "PRO" (usado para marcar recursos Premium).
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
