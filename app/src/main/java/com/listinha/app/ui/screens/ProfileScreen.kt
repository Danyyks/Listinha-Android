package com.listinha.app.ui.screens

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.listinha.app.R
import com.listinha.app.ui.theme.FredokaFamily
import com.listinha.app.util.formatCurrency
import java.util.Calendar

// ============================================================
//  ProfileScreen.kt  —  a tela de Perfil (redesenhada)
// ------------------------------------------------------------
//  Foto de capa "limpa" (sem filtro roxo) com o avatar atravessando
//  a borda de baixo — estilo dos apps modernos. Nome, saudação por
//  horário, estatísticas, tema com ícones, e opções de avaliar/
//  compartilhar/limpar/sair. Sem emojis — só ícones de linha.
// ============================================================

@Composable
fun ProfileScreen(
    userName: String?,
    userEmail: String?,
    photoUrl: String?,
    itemCount: Int,
    budget: Double?,
    theme: String,
    onThemeChange: (String) -> Unit,
    onClearPriceHistory: () -> Unit,
    onSignOut: () -> Unit,
    onDeleteAccount: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var showClearDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val firstName = userName?.split(" ")?.firstOrNull() ?: "você"
    val greeting = remember { greetingForHour() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // ---------- FOTO DE CAPA (sem filtro roxo) ----------
        Box(modifier = Modifier.fillMaxWidth().height(150.dp)) {
            Image(
                painter = painterResource(R.drawable.supermarket_cover),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(bottomStart = 26.dp, bottomEnd = 26.dp)),
            )
            // Leve escurecido só no topo, para os ícones da barra de status ficarem visíveis.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.28f), Color.Transparent))
                    ),
            )
            // Avatar sobreposto: atravessa a borda de baixo da foto.
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = 50.dp)
                    .size(104.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.background) // "anel" da cor do fundo
                    .padding(4.dp),
            ) {
                if (photoUrl != null) {
                    AsyncImage(
                        model = photoUrl,
                        contentDescription = "Foto de perfil",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = (userName?.firstOrNull() ?: '?').uppercaseChar().toString(),
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                }
            }
        }

        // Espaço para a metade do avatar que "sobra" abaixo da foto.
        Spacer(Modifier.height(60.dp))

        // Nome, e-mail e saudação (agora sobre o fundo claro).
        Text(
            text = userName ?: "Usuário",
            fontFamily = FredokaFamily,
            fontSize = 23.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        if (userEmail != null) {
            Text(
                text = userEmail,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "$greeting, $firstName!",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }

        // ---------- CORPO ----------
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatTile("$itemCount", "itens na lista", Modifier.weight(1f))
                StatTile(
                    if (budget != null) formatCurrency(budget) else "—",
                    "orçamento",
                    Modifier.weight(1f),
                )
            }

            Column {
                Text(
                    "APARÊNCIA",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemePill("Claro", R.drawable.ic_sun, theme == "light", Modifier.weight(1f)) { onThemeChange("light") }
                    ThemePill("Escuro", R.drawable.ic_moon, theme == "dark", Modifier.weight(1f)) { onThemeChange("dark") }
                    ThemePill("Auto", R.drawable.ic_auto, theme == "auto", Modifier.weight(1f)) { onThemeChange("auto") }
                }
            }

            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                SettingRow(R.drawable.ic_delete, "Limpar histórico de preços") { showClearDialog = true }
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                SettingRow(R.drawable.ic_star, "Avaliar o Listinha") { openPlayStore(context) }
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                SettingRow(R.drawable.ic_share, "Compartilhar o app") { shareApp(context) }
            }

            OutlinedButton(
                onClick = onSignOut,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.error),
            ) {
                Text("Sair do app", color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(6.dp))
            TextButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    "Excluir conta",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                )
            }

            Text(
                text = "Listinha v1.0",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                textAlign = TextAlign.Center,
            )
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Limpar histórico de preços?") },
            text = { Text("As sugestões de preço serão esquecidas. Essa ação não pode ser desfeita.") },
            confirmButton = {
                TextButton(onClick = {
                    onClearPriceHistory()
                    showClearDialog = false
                }) { Text("Limpar") }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text("Cancelar") }
            },
        )
    }

    // Confirmação FORTE para excluir a conta (ação irreversível).
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Excluir sua conta?") },
            text = { Text("Isso apaga PERMANENTEMENTE sua conta e todos os seus dados — lista, receitas, orçamento e histórico. Essa ação não pode ser desfeita.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDeleteAccount()
                }) { Text("Excluir tudo", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") }
            },
        )
    }
}

@Composable
private fun StatTile(value: String, label: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(value, fontFamily = FredokaFamily, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(label, fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ThemePill(
    label: String,
    iconRes: Int,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val bg = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    val fg = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(painterResource(iconRes), contentDescription = null, tint = fg, modifier = Modifier.size(18.dp))
        Text(label, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = fg)
    }
}

@Composable
private fun SettingRow(iconRes: Int, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painterResource(iconRes),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp),
            )
        }
        Spacer(Modifier.width(14.dp))
        Text(label, fontSize = 14.5.sp, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface)
        Icon(
            painterResource(R.drawable.ic_chevron_right),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(18.dp),
        )
    }
}

private fun greetingForHour(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 5..11 -> "Bom dia"
        in 12..17 -> "Boa tarde"
        else -> "Boa noite"
    }
}

private fun openPlayStore(context: Context) {
    val pkg = context.packageName
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$pkg")))
    } catch (e: ActivityNotFoundException) {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$pkg"))
        )
    }
}

private fun shareApp(context: Context) {
    val pkg = context.packageName
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(
            Intent.EXTRA_TEXT,
            "Baixe o Listinha, o app pra organizar suas compras do mercado! " +
                "https://play.google.com/store/apps/details?id=$pkg",
        )
    }
    context.startActivity(Intent.createChooser(intent, "Compartilhar o Listinha"))
}
