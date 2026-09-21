package com.listinha.app.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

// ============================================================
//  ImageUtils.kt  ===  tratar as fotos das receitas
// ------------------------------------------------------------
//  A gente NÃO usa servidor de imagens (Firebase Storage). Em vez
//  disso, a foto é REDUZIDA + COMPACTADA e guardada como texto
//  (base64) dentro da própria receita no Firestore. Fica leve,
//  grátis e sincroniza junto com a receita.
// ============================================================

// Lê a foto escolhida (uri), reduz o tamanho, gira se preciso e
// devolve como texto base64. Roda fora da thread principal (IO).
suspend fun compressImageToBase64(
    context: Context,
    uri: Uri,
    maxSize: Int = 800,   // maior lado da imagem, em pixels
    quality: Int = 78,    // qualidade do JPEG (0..100)
): String? = withContext(Dispatchers.IO) {
    try {
        // 1) Primeiro só MEDIMOS a foto, sem carregar os pixels na memória.
        //    inJustDecodeBounds = true = "me diga o tamanho e nada mais".
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, bounds)
        }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@withContext null

        // 2) Carrega a foto JÁ REDUZIDA. O inSampleSize faz o próprio decoder
        //    encolher na hora de ler — assim uma foto de 12MP nunca vira um
        //    bitmap gigante na memória (era isso que estourava o app / OOM).
        val opts = BitmapFactory.Options().apply {
            inSampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight, maxSize)
        }
        val original = context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, opts)
        } ?: return@withContext null

        // 3) Corrige a rotação (agora sobre a imagem JÁ pequena = barato).
        val rotated = applyExifRotation(context, uri, original)

        // 4) Ajuste fino: garante no máximo maxSize no maior lado.
        val scaled = downscale(rotated, maxSize)

        // 5) Compacta como JPEG e converte para base64.
        val baos = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, quality, baos)
        Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
    } catch (e: Throwable) {
        // Throwable (e não só Exception) para também pegar OutOfMemoryError:
        // no pior caso a receita fica sem foto, mas o app NÃO fecha.
        Log.e("ImageUtils", "Falha ao processar a foto", e)
        null
    }
}

// Descobre de quanto dá pra encolher a foto já na leitura. Devolve uma
// potência de 2 (1, 2, 4, 8...): inSampleSize = 2 significa "carregue com
// metade da largura e da altura". Paramos antes de ficar menor que maxSize,
// deixando o ajuste fino final para o downscale().
private fun calculateInSampleSize(width: Int, height: Int, maxSize: Int): Int {
    var inSampleSize = 1
    val bigger = maxOf(width, height)
    if (bigger > maxSize) {
        val half = bigger / 2
        while (half / inSampleSize >= maxSize) {
            inSampleSize *= 2
        }
    }
    return inSampleSize
}

// Volta o base64 a virar imagem para mostrar na tela (ou null se vazio/erro).
fun decodeBase64ToImageBitmap(base64: String): ImageBitmap? = try {
    if (base64.isBlank()) {
        null
    } else {
        val bytes = Base64.decode(base64, Base64.NO_WRAP)
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
    }
} catch (e: Exception) {
    null
}

// --- Ajudantes internos ---

// Encolhe a imagem mantendo a proporção.
private fun downscale(bmp: Bitmap, maxSize: Int): Bitmap {
    val bigger = maxOf(bmp.width, bmp.height)
    if (bigger <= maxSize) return bmp
    val scale = maxSize.toFloat() / bigger
    return Bitmap.createScaledBitmap(bmp, (bmp.width * scale).toInt(), (bmp.height * scale).toInt(), true)
}

// Gira a imagem conforme a orientação gravada na foto (EXIF).
private fun applyExifRotation(context: Context, uri: Uri, bmp: Bitmap): Bitmap {
    return try {
        val degrees = context.contentResolver.openInputStream(uri)?.use { input ->
            val exif = ExifInterface(input)
            when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
        } ?: 0f
        if (degrees == 0f) {
            bmp
        } else {
            val m = Matrix().apply { postRotate(degrees) }
            Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, m, true)
        }
    } catch (e: Exception) {
        bmp
    }
}
