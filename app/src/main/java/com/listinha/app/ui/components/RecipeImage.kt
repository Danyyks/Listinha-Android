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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import com.listinha.app.ui.theme.FrauncesFamily
import com.listinha.app.ui.theme.LocalFlavor
import com.listinha.app.data.Recipe
import com.listinha.app.util.decodeBase64ToImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.abs

// ============================================================
//  RecipeImage.kt  ===  a "cara" de uma receita
// ------------------------------------------------------------
//  Mostra a FOTO da receita ou, se não tiver, uma CAPA automática:
//  o tom suave da área com a inicial da receita em serifa itálica.
//  Assim a grade nunca fica com "buracos".
//
//  A foto (base64) é decodificada FORA da thread principal e fica
//  num cache pequeno — rolar a grade não engasga.
// ============================================================

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
    val flavor = LocalFlavor.current
    val bitmap = rememberRecipeBitmap(recipe.photo)
    Box(
        modifier = modifier.background(flavor.soft),
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
            Text(
                text = recipe.name.trim().take(1).uppercase(),
                fontFamily = FrauncesFamily,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic,
                fontSize = iconSize.value.sp,
                color = flavor.title,
            )
        }
    }
}
