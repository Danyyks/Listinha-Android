package com.listinha.app.ui.components

import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.listinha.app.data.Recipe
import com.listinha.app.ui.screens.iconForRecipe
import com.listinha.app.util.decodeBase64ToImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.abs

// ============================================================
//  RecipeImage.kt  ===  a "cara" de uma receita
// ------------------------------------------------------------
//  Mostra a FOTO da receita ou, se não tiver, uma CAPA automática:
//  um fundo pastel com o ícone grande no meio. Assim a grade nunca
//  fica com "buracos" e toda receita parece caprichada.
//
//  A foto (base64) é decodificada FORA da thread principal e fica
//  num cache pequeno — rolar a grade não engasga.
// ============================================================

// Pares (fundo, ícone) das capas automáticas: claro e escuro.
private val lightCovers = listOf(
    Color(0xFFE7E0F2) to Color(0xFF6E5A94), // lavanda
    Color(0xFFF6E2D3) to Color(0xFFB0704A), // pêssego
    Color(0xFFDCEEE4) to Color(0xFF4A8A69), // menta
    Color(0xFFF4EBCB) to Color(0xFF9A7B2C), // manteiga
    Color(0xFFF5DDE6) to Color(0xFFB0577F), // rosa
    Color(0xFFDCE8F4) to Color(0xFF4F7AA6), // céu
)
private val darkCovers = listOf(
    Color(0xFF3A3150) to Color(0xFFC6B7E6),
    Color(0xFF4A3426) to Color(0xFFF0BE9A),
    Color(0xFF253D31) to Color(0xFF9ED6B8),
    Color(0xFF423A1E) to Color(0xFFE6CF85),
    Color(0xFF45283A) to Color(0xFFEBA8C6),
    Color(0xFF24364A) to Color(0xFFA8C8EA),
)

// Escolhe sempre a MESMA cor para a mesma receita (pelo id, ou nome se nova).
@Composable
fun recipeCoverColors(recipe: Recipe): Pair<Color, Color> {
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val palette = if (dark) darkCovers else lightCovers
    val key = recipe.id.ifBlank { recipe.name }
    return palette[abs(key.hashCode() % palette.size)]
}

// Cache das fotos já decodificadas (as ~24 mais recentes).
private val bitmapCache = LruCache<String, ImageBitmap>(24)

private fun cacheKey(photo: String) = "${photo.length}:${photo.hashCode()}"

// Decodifica a foto em segundo plano e devolve null enquanto carrega
// (ou se a receita não tiver foto).
@Composable
fun rememberRecipeBitmap(photo: String): ImageBitmap? {
    val bitmap by produceState(initialValue = bitmapCache.get(cacheKey(photo)), photo) {
        if (photo.isBlank()) {
            value = null
            return@produceState
        }
        val key = cacheKey(photo)
        value = bitmapCache.get(key) ?: withContext(Dispatchers.Default) {
            decodeBase64ToImageBitmap(photo)
        }?.also { bitmapCache.put(key, it) }
    }
    return bitmap
}

// A imagem da receita, preenchendo o espaço que receber (o tamanho e os
// cantos arredondados vêm de quem chama, pelo modifier).
@Composable
fun RecipeImage(
    recipe: Recipe,
    modifier: Modifier = Modifier,
    iconSize: Dp = 56.dp,
) {
    val (coverBg, coverFg) = recipeCoverColors(recipe)
    val bitmap = rememberRecipeBitmap(recipe.photo)
    Box(
        modifier = modifier.background(coverBg),
        contentAlignment = Alignment.Center,
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = recipe.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else if (recipe.photo.isBlank()) {
            Icon(
                painterResource(iconForRecipe(recipe)),
                contentDescription = null,
                tint = coverFg,
                modifier = Modifier.size(iconSize),
            )
        }
    }
}
