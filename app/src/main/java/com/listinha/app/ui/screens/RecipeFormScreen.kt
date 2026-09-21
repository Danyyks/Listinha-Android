@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
)

package com.listinha.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.layout.ContentScale
import com.listinha.app.R
import com.listinha.app.data.CATEGORIES
import com.listinha.app.data.Recipe
import com.listinha.app.data.RecipeIngredient
import com.listinha.app.data.categoryLabel
import com.listinha.app.ui.theme.FredokaFamily
import com.listinha.app.util.compressImageToBase64
import com.listinha.app.util.decodeBase64ToImageBitmap
import kotlinx.coroutines.launch

// ============================================================
//  RecipeFormScreen.kt  ===  criar / editar uma receita
// ------------------------------------------------------------
//  Um formulário: nome, porções, ícone, ingredientes (nome +
//  categoria + quantidade) e passos do preparo. Ao salvar, monta
//  um objeto Recipe e devolve via onSave — quem grava no banco é
//  o RecipeViewModel.
// ============================================================

// Rascunho editável de um ingrediente (cada campo "observável" pela tela).
private class IngredientDraft(name: String = "", category: String = "outros", quantity: String = "") {
    var name by mutableStateOf(name)
    var category by mutableStateOf(category)
    var quantity by mutableStateOf(quantity)
}

// Rascunho editável de um passo do preparo.
private class StepDraft(text: String = "") {
    var text by mutableStateOf(text)
}

@Composable
fun RecipeFormScreen(
    initial: Recipe?,            // null = nova receita; senão = editando
    onCancel: () -> Unit,
    onSave: (Recipe) -> Unit,
) {
    val context = LocalContext.current

    // --- Estado do formulário (começa vazio ou com os dados da receita) ---
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var iconIndex by remember { mutableIntStateOf(initial?.iconIndex ?: 0) }
    // Foto da receita (base64) — "" = sem foto (aí mostra o ícone).
    var photo by remember { mutableStateOf(initial?.photo ?: "") }
    var loadingPhoto by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    // Abre a galeria de fotos (sem precisar de permissão no Android moderno).
    val pickImage = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            loadingPhoto = true
            scope.launch {
                val b64 = compressImageToBase64(context, uri)
                if (b64 != null) photo = b64
                else Toast.makeText(context, "Não consegui carregar a foto.", Toast.LENGTH_SHORT).show()
                loadingPhoto = false
            }
        }
    }

    val ingredients = remember {
        mutableStateListOf<IngredientDraft>().apply {
            initial?.ingredients?.forEach { add(IngredientDraft(it.name, it.category, it.quantity)) }
            if (isEmpty()) add(IngredientDraft()) // começa com uma linha em branco
        }
    }
    val steps = remember {
        mutableStateListOf<StepDraft>().apply {
            initial?.steps?.forEach { add(StepDraft(it)) }
        }
    }

    // Monta a receita a partir do formulário (ou avisa se faltar o nome).
    fun montarReceita(): Recipe? {
        val nome = name.trim()
        if (nome.isEmpty()) {
            Toast.makeText(context, "Dê um nome à receita.", Toast.LENGTH_SHORT).show()
            return null
        }
        val ings = ingredients
            .filter { it.name.isNotBlank() }
            .map { RecipeIngredient(it.name.trim(), it.category, it.quantity.trim()) }
        val stps = steps.map { it.text.trim() }.filter { it.isNotBlank() }
        return Recipe(
            id = initial?.id ?: "",
            name = nome,
            servings = initial?.servings ?: 1,
            iconIndex = iconIndex,
            photo = photo,
            ingredients = ings,
            steps = stps,
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(
                            painterResource(R.drawable.ic_back),
                            contentDescription = "Cancelar",
                            tint = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                },
                title = {
                    Text(
                        if (initial == null) "Nova receita" else "Editar receita",
                        fontFamily = FredokaFamily,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                },
                actions = {
                    TextButton(onClick = { montarReceita()?.let(onSave) }) {
                        Text("Salvar", fontWeight = FontWeight.Bold)
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
            Spacer(Modifier.height(4.dp))

            // --- Foto (opcional) ---
            val photoBitmap = remember(photo) { decodeBase64ToImageBitmap(photo) }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable {
                        pickImage.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                contentAlignment = Alignment.Center,
            ) {
                when {
                    loadingPhoto -> CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    photoBitmap != null -> Image(
                        bitmap = photoBitmap,
                        contentDescription = "Foto da receita",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                    else -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            painterResource(R.drawable.ic_camera),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(34.dp),
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Adicionar foto",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
            if (photo.isNotBlank()) {
                Row {
                    TextButton(onClick = {
                        pickImage.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }) { Text("Trocar foto") }
                    TextButton(onClick = { photo = "" }) {
                        Text("Remover", color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // --- Nome ---
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nome da receita") },
                placeholder = { Text("Ex: Strogonoff de frango") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(16.dp))

            // --- Ícone (usado quando a receita não tem foto) ---
            Text(
                "Ícone (quando não tiver foto)",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                recipeIconResList.forEachIndexed { i, iconRes ->
                    val selected = i == iconIndex
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.primaryContainer
                            )
                            .clickable { iconIndex = i },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painterResource(iconRes),
                            contentDescription = null,
                            tint = if (selected) Color.White else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(26.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // --- Ingredientes ---
            Text(
                "Ingredientes",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(8.dp))

            ingredients.forEachIndexed { index, draft ->
                Column(Modifier.padding(bottom = 12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = draft.name,
                            onValueChange = { draft.name = it },
                            label = { Text("Ingrediente") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(onClick = { if (ingredients.size > 1) ingredients.removeAt(index) }) {
                            Icon(
                                painterResource(R.drawable.ic_delete),
                                contentDescription = "Remover ingrediente",
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = draft.quantity,
                            onValueChange = { draft.quantity = it },
                            label = { Text("Qtd") },
                            placeholder = { Text("500 g") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.width(120.dp),
                        )
                        Spacer(Modifier.width(10.dp))
                        CategoryDropdown(
                            selected = draft.category,
                            onSelect = { draft.category = it },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            OutlinedButton(
                onClick = { ingredients.add(IngredientDraft()) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
            ) {
                Icon(painterResource(R.drawable.ic_add), contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Adicionar ingrediente")
            }

            Spacer(Modifier.height(20.dp))

            // --- Modo de preparo (passos opcionais) ---
            Text(
                "Modo de preparo",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(8.dp))

            steps.forEachIndexed { index, draft ->
                Row(
                    modifier = Modifier.padding(bottom = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "${index + 1}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    OutlinedTextField(
                        value = draft.text,
                        onValueChange = { draft.text = it },
                        label = { Text("Passo ${index + 1}") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = { steps.removeAt(index) }) {
                        Icon(
                            painterResource(R.drawable.ic_delete),
                            contentDescription = "Remover passo",
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }

            OutlinedButton(
                onClick = { steps.add(StepDraft()) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
            ) {
                Icon(painterResource(R.drawable.ic_add), contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Adicionar passo")
            }

            Spacer(Modifier.height(24.dp))

            // --- Salvar ---
            Button(
                onClick = { montarReceita()?.let(onSave) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(15.dp),
            ) {
                Text(
                    if (initial == null) "Salvar receita" else "Salvar alterações",
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// Seletor de categoria compacto (menu suspenso) para cada ingrediente.
@Composable
private fun CategoryDropdown(
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = categoryLabel(selected),
            onValueChange = {},
            readOnly = true,
            label = { Text("Categoria") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.menuAnchor().fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            CATEGORIES.forEach { cat ->
                DropdownMenuItem(
                    text = { Text(cat.label) },
                    onClick = {
                        onSelect(cat.key)
                        expanded = false
                    },
                )
            }
        }
    }
}
