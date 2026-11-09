package com.listinha.app.ui.screens

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.listinha.app.R
import com.listinha.app.ui.components.BrandAlert
import com.listinha.app.ui.components.BrandTitle
import com.listinha.app.ui.components.OutlinePill
import com.listinha.app.ui.components.SectionLabel
import com.listinha.app.ui.components.capFontScale
import com.listinha.app.ui.theme.Acai
import com.listinha.app.ui.theme.AcaiRose
import com.listinha.app.ui.theme.Area
import com.listinha.app.ui.theme.AreaTheme
import com.listinha.app.ui.theme.FredokaFamily
import com.listinha.app.ui.theme.LocalFlavor
import com.listinha.app.ui.theme.LocalPaper
import com.listinha.app.util.formatCurrency
import java.util.Calendar

// ============================================================
//  ProfileScreen.kt  —  a tela de Perfil (redesenhada)
// ------------------------------------------------------------
//  Sabor açaí em qualquer tema, foto de toranjas no topo e o avatar
//  atravessando a borda dela. Saudação por horário, estatísticas,
//  aparência e as opções de avaliar, compartilhar, limpar e sair.
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

    val firstName = userName?.split(" ")?.firstOrNull()?.replace("*", "")?.takeIf { it.isNotBlank() } ?: "você"
    val greeting = remember { greetingForHour() }
    val versao = remember {
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }.getOrNull().orEmpty()
    }

    AreaTheme(Area.ACAI) {
        val flavor = LocalFlavor.current
        val paper = LocalPaper.current
        val rolagem = rememberScrollState()
        val limite = with(LocalDensity.current) { 150.dp.toPx() }
        val fotoSaiu by remember { derivedStateOf { rolagem.value > limite } }
        Box(modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(flavor.bg)
                .verticalScroll(rolagem),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                Image(
                    painter = painterResource(R.drawable.profile_cover),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize(),
                )
                // Sombra só no topo, para a hora e a bateria ficarem visíveis sobre a foto.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp)
                        .align(Alignment.TopCenter)
                        .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.28f), Color.Transparent))),
                )
                // O avatar atravessa a borda de baixo da foto.
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = 50.dp)
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(flavor.bg)
                        .padding(5.dp),
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
                            modifier = Modifier.fillMaxSize().clip(CircleShape).background(AcaiRose),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = (userName?.firstOrNull { it.isLetterOrDigit() } ?: '?').uppercaseChar().toString(),
                                fontFamily = FredokaFamily,
                                fontSize = 42.sp,
                                fontWeight = FontWeight.Bold,
                                color = Acai,
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 66.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    BrandTitle("$greeting, *$firstName.*", fontSize = 38.sp, textAlign = TextAlign.Center)
                    if (userEmail != null) {
                        Text(userEmail, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = flavor.secondary)
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatTile("$itemCount", "itens na lista", Modifier.weight(1f))
                    StatTile(
                        if (budget != null) formatCurrency(budget).replace(",00", "") else "—",
                        "orçamento",
                        Modifier.weight(1f),
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionLabel("Aparência", Modifier.padding(start = 4.dp), color = flavor.secondary)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        ThemePill("Claro", R.drawable.ic_sun, theme == "light", Modifier.weight(1f)) { onThemeChange("light") }
                        ThemePill("Escuro", R.drawable.ic_moon, theme == "dark", Modifier.weight(1f)) { onThemeChange("dark") }
                        ThemePill("Auto", R.drawable.ic_auto, theme == "auto", Modifier.weight(1f)) { onThemeChange("auto") }
                    }
                }

                Column(Modifier.clip(RoundedCornerShape(22.dp)).background(paper.paper)) {
                    SettingRow(R.drawable.ic_delete, "Limpar histórico de preços") { showClearDialog = true }
                    HorizontalDivider(color = paper.line)
                    SettingRow(R.drawable.ic_star, "Avaliar o Listinha") { openPlayStore(context) }
                    HorizontalDivider(color = paper.line)
                    SettingRow(R.drawable.ic_share, "Compartilhar o app") { shareApp(context) }
                }

                OutlinePill("Sair do app", onSignOut, Modifier.fillMaxWidth(), height = 54.dp)

                TextButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.align(Alignment.CenterHorizontally).heightIn(min = 48.dp),
                ) {
                    Text(
                        "Excluir conta",
                        color = flavor.secondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textDecoration = TextDecoration.Underline,
                    )
                }

                Text(
                    text = "Listinha v$versao",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = flavor.secondary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
            }
        }

        if (fotoSaiu) {
            Box(Modifier.fillMaxWidth().windowInsetsTopHeight(WindowInsets.statusBars).background(flavor.bg))
        }
        }

        if (showClearDialog) {
            BrandAlert(
                title = "Limpar *histórico?*",
                text = "As sugestões de preço serão esquecidas. Essa ação não pode ser desfeita.",
                confirmLabel = "Limpar",
                onConfirm = {
                    onClearPriceHistory()
                    showClearDialog = false
                },
                onDismiss = { showClearDialog = false },
            )
        }

        // Confirmação forte para excluir a conta, que é irreversível.
        if (showDeleteDialog) {
            BrandAlert(
                title = "Excluir sua *conta?*",
                text = "Isso apaga para sempre sua conta, lista, receitas, orçamento e histórico.\n\nNa próxima tela, o Google vai pedir para você tocar na sua conta. É só a confirmação de que é você.",
                confirmLabel = "Continuar",
                danger = true,
                onConfirm = {
                    showDeleteDialog = false
                    onDeleteAccount()
                },
                onDismiss = { showDeleteDialog = false },
            )
        }
    }
}

@Composable
private fun StatTile(value: String, label: String, modifier: Modifier = Modifier) {
    val paper = LocalPaper.current
    Column(
        modifier
            .clip(RoundedCornerShape(22.dp))
            .background(paper.paper)
            .padding(horizontal = 18.dp, vertical = 16.dp),
    ) {
        Text(value, fontFamily = FredokaFamily, fontSize = capFontScale(28.sp, 1.15f), fontWeight = FontWeight.Bold, color = paper.brand, maxLines = 1)
        Text(label, fontSize = capFontScale(13.sp, 1.3f), fontWeight = FontWeight.Bold, color = paper.muted, maxLines = 1)
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
    val flavor = LocalFlavor.current
    val bg = if (selected) flavor.title else flavor.soft
    val fg = if (selected) flavor.on else flavor.secondary
    Column(
        modifier = modifier
            .height(68.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterVertically),
    ) {
        Icon(painterResource(iconRes), contentDescription = null, tint = fg, modifier = Modifier.size(20.dp))
        Text(label, fontSize = capFontScale(14.sp, 1.3f), fontWeight = FontWeight.ExtraBold, color = fg, maxLines = 1)
    }
}

@Composable
private fun SettingRow(iconRes: Int, label: String, onClick: () -> Unit) {
    val paper = LocalPaper.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp)
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(paper.field),
            contentAlignment = Alignment.Center,
        ) {
            Icon(painterResource(iconRes), contentDescription = null, tint = paper.brand, modifier = Modifier.size(19.dp))
        }
        Spacer(Modifier.width(14.dp))
        Text(label, fontSize = 15.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), color = paper.ink)
        Icon(painterResource(R.drawable.ic_chevron_right), contentDescription = null, tint = paper.muted, modifier = Modifier.size(18.dp))
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
