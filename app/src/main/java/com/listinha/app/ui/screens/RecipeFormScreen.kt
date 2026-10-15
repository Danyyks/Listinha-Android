@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
)

package com.listinha.app.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.listinha.app.R
import com.listinha.app.data.CATEGORIES
import com.listinha.app.data.Recipe
import com.listinha.app.data.RecipeIngredient
import com.listinha.app.data.categoryColor
import com.listinha.app.data.categoryLabel
import com.listinha.app.ui.components.rememberRecipeBitmap
import com.listinha.app.ui.theme.FredokaFamily
import com.listinha.app.util.compressImageToBase64
import com.listinha.app.util.guessIconIndex
import com.listinha.app.util.ingredientsToText
import com.listinha.app.util.normalize
import com.listinha.app.util.parseIngredients
import com.listinha.app.util.parseRecipeText
import com.listinha.app.util.parseSteps
import com.listinha.app.util.stepsToText
import kotlinx.coroutines.launch

// ============================================================
//  RecipeFormScreen.kt  ===  criar / editar uma receita
// ------------------------------------------------------------
//  Feito para salvar SEM ESFORÇO:
//   • "Colar": cola a receita inteira e o app separa tudo;
//   • ingredientes num campo só (um por linha) — a quantidade e a
//     CATEGORIA são descobertas sozinhas ("Revisar" para corrigir);
//   • modo de preparo também num campo só (um passo por linha);
//   • o ícone da capa é escolhido pelo nome da receita.
//  Ao salvar, monta um Recipe e devolve via onSave — quem grava no
//  banco é o RecipeViewModel.
// ============================================================

@Composable
fun RecipeFormScreen(
    initial: Recipe?,          // null = nova receita; senão = editando
    prefill: Recipe?,          // rascunho vindo de texto compartilhado (ou null)
    openPaste: Boolean,        // já abrir a caixa "Colar receita"
    onCancel: () -> Unit,
    onSave: (Recipe) -> Unit,
) {
    val context = LocalContext.current
    val isNew = initial == null
    val base = initial ?: prefill

    // --- Estado do formulário (começa vazio, com a receita ou com o rascunho) ---
    var name by rememberSaveable { mutableStateOf(base?.name ?: "") }
    var minutes by rememberSaveable { mutableStateOf(base?.minutes?.takeIf { it > 0 }?.toString() ?: "") }
    var servings by rememberSaveable { mutableStateOf(base?.servings?.takeIf { base.id.isNotBlank() || it > 1 }?.toString() ?: "") }
    var ingredientsText by rememberSaveable { mutableStateOf(base?.ingredients?.let(::ingredientsToText) ?: "") }
    var stepsText by rememberSaveable { mutableStateOf(base?.steps?.let(::stepsToText) ?: "") }
    var sourceUrl by rememberSaveable { mutableStateOf(base?.sourceUrl ?: "") }
    // Foto (base64) — "" = sem foto (aí aparece a capa automática).
    var photo by remember { mutableStateOf(initial?.photo ?: "") }
    var loadingPhoto by remember { mutableStateOf(false) }

    // Categorias escolhidas à mão (nome normalizado -> categoria). Na edição,
    // começa com as categorias já salvas, para nada mudar sem querer.
    val overrides = remember {
        mutableStateMapOf<String, String>().apply {
            base?.ingredients?.forEach { put(normalize(it.name), it.category) }
        }
    }

    var showPaste by rememberSaveable { mutableStateOf(openPaste) }
    var showReview by remember { mutableStateOf(false) }

    // Os ingredientes como o app está entendendo AGORA (com as correções).
    val parsed: List<RecipeIngredient> = parseIngredients(ingredientsText).map { ing ->
        overrides[normalize(ing.name)]?.let { ing.copy(category = it) } ?: ing
    }

    // Abre a galeria de fotos (sem precisar de permissão no Android moderno).
    val scope = rememberCoroutineScope()
    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            loadingPhoto = true
            scope.launch {
                val b64 = compressImageToBase64(context, uri)
                if (b64 != null) photo = b64
                else Toast.makeText(context, R.string.form_photo_error, Toast.LENGTH_SHORT).show()
                loadingPhoto = false
            }
        }
    }
    fun openGallery() = pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))

    // Monta a receita a partir do formulário (ou avisa se faltar o nome).
    fun buildRecipe(): Recipe? {
        val nome = name.trim()
        if (nome.isEmpty()) {
            Toast.makeText(context, R.string.form_need_name, Toast.LENGTH_SHORT).show()
            return null
        }
        // Ícone: o que a pessoa já tinha; se era o "coringa" (panela), tenta adivinhar.
        val icon = initial?.iconIndex?.takeIf { it != 0 } ?: guessIconIndex(nome)
        return Recipe(
            id = initial?.id ?: "",
            name = nome,
            servings = servings.toIntOrNull()?.coerceIn(1, 99) ?: 1,
            minutes = minutes.toIntOrNull()?.coerceIn(0, 6000) ?: 0,
            iconIndex = icon,
            photo = photo,
            ingredients = parsed,
            steps = parseSteps(stepsText),
            sourceUrl = sourceUrl.trim(),
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(
                            painterResource(R.drawable.ic_close),
                            contentDescription = stringResource(R.string.action_close),
                            tint = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                },
                title = {
                    Text(
                        stringResource(if (isNew) R.string.form_new else R.string.form_edit),
                        fontFamily = FredokaFamily,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                },
                actions = {
                    Button(
                        onClick = { buildRecipe()?.let(onSave) },
                        modifier = Modifier.padding(end = 8.dp).height(40.dp),
                    ) {
                        Text(stringResource(R.string.action_save), fontWeight = FontWeight.ExtraBold)
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .imePadding() // o teclado não cobre o campo que está sendo digitado
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            Spacer(Modifier.height(8.dp))

            // --- "Copiou uma receita? Colar" (só na receita nova) ---
            if (isNew) {
                PasteCard(onClick = { showPaste = true })
                Spacer(Modifier.height(16.dp))
            }

            // --- Foto + nome + tempo + porções ---
            Row(verticalAlignment = Alignment.Top) {
                PhotoBox(
                    photo = photo,
                    loading = loadingPhoto,
                    onPick = { openGallery() },
                    onRemove = { photo = "" },
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    FormField(
                        value = name,
                        onValueChange = { name = it },
                        label = stringResource(R.string.form_name),
                        placeholder = stringResource(R.string.form_name_hint),
                        singleLine = true,
                        capitalization = KeyboardCapitalization.Sentences,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FormField(
                            value = minutes,
                            onValueChange = { v -> minutes = v.filter(Char::isDigit).take(4) },
                            label = stringResource(R.string.form_time),
                            suffix = stringResource(R.string.form_minutes_suffix),
                            singleLine = true,
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.weight(1f),
                        )
                        FormField(
                            value = servings,
                            onValueChange = { v -> servings = v.filter(Char::isDigit).take(2) },
                            label = stringResource(R.string.form_servings),
                            singleLine = true,
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            // --- Ingredientes (um por linha) ---
            Spacer(Modifier.height(22.dp))
            SectionHeader(stringResource(R.string.form_ingredients), stringResource(R.string.form_one_per_line))
            Spacer(Modifier.height(8.dp))
            FormField(
                value = ingredientsText,
                onValueChange = { ingredientsText = it },
                placeholder = stringResource(R.string.form_ingredients_hint),
                minLines = 4,
                capitalization = KeyboardCapitalization.Sentences,
            )
            // Como o app entendeu: "Doces 2 · Laticínios 2 · Revisar".
            if (parsed.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                CategorySummary(parsed, onReview = { showReview = true })
            }

            // --- Modo de preparo (um passo por linha) ---
            Spacer(Modifier.height(20.dp))
            SectionHeader(stringResource(R.string.form_steps), stringResource(R.string.form_step_per_line))
            Spacer(Modifier.height(8.dp))
            FormField(
                value = stepsText,
                onValueChange = { stepsText = it },
                placeholder = stringResource(R.string.form_steps_hint),
                minLines = 3,
                capitalization = KeyboardCapitalization.Sentences,
            )

            // --- Link de onde veio (opcional) ---
            Spacer(Modifier.height(20.dp))
            FormField(
                value = sourceUrl,
                onValueChange = { sourceUrl = it.trim() },
                label = stringResource(R.string.form_link),
                singleLine = true,
                keyboardType = KeyboardType.Uri,
            )

            Spacer(Modifier.height(32.dp))
        }
    }

    // Caixa "Colar receita": lê a área de transferência e organiza.
    if (showPaste) {
        PasteDialog(
            onDismiss = { showPaste = false },
            onOrganize = { text ->
                val draft = parseRecipeText(text)
                if (draft.name.isBlank() && draft.ingredients.isEmpty() && draft.steps.isEmpty()) {
                    Toast.makeText(context, R.string.paste_nothing, Toast.LENGTH_SHORT).show()
                } else {
                    if (draft.name.isNotBlank()) name = draft.name
                    if (draft.ingredients.isNotEmpty()) ingredientsText = ingredientsToText(draft.ingredients)
                    if (draft.steps.isNotEmpty()) stepsText = stepsToText(draft.steps)
                    if (draft.minutes > 0) minutes = draft.minutes.toString()
                    if (draft.servings > 1) servings = draft.servings.toString()
                    if (draft.sourceUrl.isNotBlank()) sourceUrl = draft.sourceUrl
                    showPaste = false
                    Toast.makeText(context, R.string.paste_done, Toast.LENGTH_SHORT).show()
                }
            },
        )
    }

    // Caixa "Revisar categorias".
    if (showReview) {
        ReviewCategoriesDialog(
            ingredients = parsed,
            onChange = { ing, category -> overrides[normalize(ing.name)] = category },
            onDismiss = { showReview = false },
        )
    }
}

// Cartão roxo "Copiou uma receita? Cole e a gente organiza tudo. [Colar]".
@Composable
private fun PasteCard(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.primary)
            .clickable(onClick = onClick)
            .padding(start = 16.dp, end = 14.dp, top = 14.dp, bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painterResource(R.drawable.ic_clipboard),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                stringResource(R.string.form_paste_title),
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onPrimary,
            )
            Text(
                stringResource(R.string.form_paste_text),
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(
            stringResource(R.string.form_paste_button),
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .clip(RoundedCornerShape(19.dp))
                .background(MaterialTheme.colorScheme.onPrimary)
                .padding(horizontal = 16.dp, vertical = 9.dp),
        )
    }
}

// Quadradinho da foto: tracejado vazio, foto escolhida ou carregando.
@Composable
private fun PhotoBox(photo: String, loading: Boolean, onPick: () -> Unit, onRemove: () -> Unit) {
    val bitmap = rememberRecipeBitmap(photo)
    Box(Modifier.size(112.dp)) {
        Box(
            modifier = Modifier
                .size(112.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surface)
                .then(
                    if (bitmap == null) Modifier.border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
                    else Modifier
                )
                .clickable(onClick = onPick),
            contentAlignment = Alignment.Center,
        ) {
            when {
                loading -> CircularProgressIndicator(Modifier.size(28.dp), strokeWidth = 3.dp)
                bitmap != null -> Image(
                    bitmap = bitmap,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                else -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painterResource(R.drawable.ic_camera),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp),
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        stringResource(R.string.form_photo),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
        // "X" para tirar a foto.
        if (bitmap != null && !loading) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.55f))
                    .clickable(onClick = onRemove),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painterResource(R.drawable.ic_close),
                    contentDescription = stringResource(R.string.form_photo_remove),
                    tint = Color.White,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
    }
}

// Campo de texto branco, sem borda, cantos arredondados (padrão do formulário).
@Composable
private fun FormField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    suffix: String? = null,
    singleLine: Boolean = false,
    minLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.None,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = label?.let { { Text(it, fontWeight = FontWeight.Bold) } },
        placeholder = placeholder?.let { { Text(it, lineHeight = 24.sp) } },
        suffix = suffix?.let { { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) } },
        singleLine = singleLine,
        minLines = minLines,
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, capitalization = capitalization),
        textStyle = MaterialTheme.typography.bodyLarge.copy(lineHeight = 26.sp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        ),
        modifier = modifier.fillMaxWidth(),
    )
}

// Título da seção + dica à direita ("Ingredientes ........ Um por linha").
@Composable
private fun SectionHeader(title: String, hint: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onBackground)
        Text(hint, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// Resumo das categorias que o app achou + o link "Revisar".
@Composable
private fun CategorySummary(ingredients: List<RecipeIngredient>, onReview: () -> Unit) {
    val groups = ingredients.groupingBy { it.category }.eachCount()
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        groups.forEach { (category, count) ->
            Row(
                modifier = Modifier
                    .height(30.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(8.dp).clip(CircleShape).background(categoryColor(category)))
                Spacer(Modifier.width(6.dp))
                Text(
                    "${categoryLabel(category)} $count",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        Text(
            stringResource(R.string.form_review),
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .clip(RoundedCornerShape(15.dp))
                .clickable(onClick = onReview)
                .padding(horizontal = 8.dp, vertical = 6.dp),
        )
    }
}

// Caixa "Colar receita": já vem com o que estiver copiado no celular.
@Composable
private fun PasteDialog(onDismiss: () -> Unit, onOrganize: (String) -> Unit) {
    val clipboard = LocalClipboardManager.current
    var text by remember { mutableStateOf(clipboard.getText()?.text ?: "") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.paste_title)) },
        text = {
            Column {
                Text(
                    stringResource(R.string.paste_text),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text(stringResource(R.string.paste_field)) },
                    minLines = 6,
                    maxLines = 10,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(onClick = { onOrganize(text) }, enabled = text.isNotBlank()) {
                Text(stringResource(R.string.paste_organize), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
    )
}

// Caixa "Categorias": cada ingrediente com a categoria que o app escolheu
// (tocar na categoria abre a lista para trocar).
@Composable
private fun ReviewCategoriesDialog(
    ingredients: List<RecipeIngredient>,
    onChange: (RecipeIngredient, String) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.form_review_title)) },
        text = {
            Column(Modifier.heightIn(max = 440.dp).verticalScroll(rememberScrollState())) {
                Text(
                    stringResource(R.string.form_review_text),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
                ingredients.forEach { ing ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            ing.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f),
                        )
                        CategoryPicker(selected = ing.category, onSelect = { onChange(ing, it) })
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.form_review_done)) }
        },
    )
}

// Pílula com a categoria atual; ao tocar, abre o menu com todas.
@Composable
private fun CategoryPicker(selected: String, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Row(
            modifier = Modifier
                .height(34.dp)
                .clip(RoundedCornerShape(17.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { expanded = true }
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(categoryColor(selected)))
            Spacer(Modifier.width(6.dp))
            Text(categoryLabel(selected), fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            CATEGORIES.forEach { cat ->
                DropdownMenuItem(
                    leadingIcon = { Box(Modifier.size(10.dp).clip(CircleShape).background(cat.color)) },
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
