package com.listinha.app.ui.screens

import android.media.RingtoneManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.listinha.app.R
import com.listinha.app.data.RecipeIngredient
import com.listinha.app.ui.components.BrandTitle
import com.listinha.app.ui.components.FilledPill
import com.listinha.app.ui.components.OutlinePill
import com.listinha.app.ui.components.RoundIconButton
import com.listinha.app.ui.components.SectionLabel
import com.listinha.app.ui.theme.Area
import com.listinha.app.ui.theme.AreaTheme
import com.listinha.app.ui.theme.FredokaFamily
import com.listinha.app.ui.theme.FrauncesFamily
import com.listinha.app.ui.theme.LocalFlavor
import com.listinha.app.util.detectTimerSeconds
import com.listinha.app.util.formatClock
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ============================================================
//  CookModeScreen.kt  ===  o "modo cozinhar"
// ------------------------------------------------------------
//  Tela cheia pensada para a cozinha: mão suja, celular longe.
//   • Página 1: separar os ingredientes (já nas porções escolhidas).
//   • Depois: UM passo por página, letra grande, arrastar ou tocar
//     em Próximo.
//   • Se o passo fala de tempo ("asse por 25 minutos"), aparece o
//     TIMER num toque — ele toca um som ao terminar.
//   • A tela fica SEMPRE ACESA enquanto o modo estiver aberto.
// ============================================================

// Ocupa a tela toda (o detalhe da receita mostra ESTA tela no lugar dele).
// O "voltar" do celular fecha o modo cozinhar.
@Composable
fun CookModeScreen(
    recipeName: String,
    ingredients: List<RecipeIngredient>,
    steps: List<String>,
    onClose: () -> Unit,
) {
    BackHandler(onBack = onClose)
    // Sempre no sabor noite: tela escura cansa menos no fogão e gasta menos bateria.
    AreaTheme(Area.COZINHAR) {
        Surface(modifier = Modifier.fillMaxSize(), color = LocalFlavor.current.bg) {
            CookModeContent(recipeName, ingredients, steps, onClose)
        }
    }
}

@Composable
private fun CookModeContent(
    recipeName: String,
    ingredients: List<RecipeIngredient>,
    steps: List<String>,
    onClose: () -> Unit,
) {
    val context = LocalContext.current

    // Tela sempre acesa enquanto este modo existir.
    val view = LocalView.current
    DisposableEffect(view) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }

    // Página 0 = ingredientes; páginas 1..N = passos.
    val pageCount = steps.size + 1
    val pager = rememberPagerState(pageCount = { pageCount })
    val scope = rememberCoroutineScope()

    // --- O timer (um por vez) ---
    var timerPage by rememberSaveable { mutableIntStateOf(-1) }   // de qual passo é o timer (-1 = nenhum)
    var remaining by rememberSaveable { mutableIntStateOf(0) }    // segundos que faltam
    var running by rememberSaveable { mutableStateOf(false) }
    var finished by rememberSaveable { mutableStateOf(false) }

    // Conta 1 segundo de cada vez enquanto estiver rodando.
    LaunchedEffect(running) {
        while (running && remaining > 0) {
            delay(1000)
            remaining--
        }
        if (running && remaining == 0) {
            running = false
            finished = true
            // Toca o som de notificação do celular (não precisa de permissão).
            try {
                val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                RingtoneManager.getRingtone(context, uri)?.play()
            } catch (_: Exception) {
                // Sem som disponível: o aviso visual já resolve.
            }
        }
    }

    val flavor = LocalFlavor.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        // --- Topo: fechar, nome, "3 de 6" e a barra de progresso ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 8.dp, end = 20.dp, top = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onClose, modifier = Modifier.size(48.dp)) {
                Icon(
                    painterResource(R.drawable.ic_close),
                    contentDescription = stringResource(R.string.cook_exit),
                    tint = flavor.text,
                )
            }
            Text(
                recipeName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = flavor.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            // Timer rodando em OUTRO passo: aparece aqui em cima (toque = ir até ele).
            if (timerPage >= 0 && timerPage != pager.currentPage && (running || finished || remaining > 0)) {
                Text(
                    formatClock(remaining),
                    fontFamily = FredokaFamily,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = flavor.text,
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(flavor.soft)
                        .clickable { scope.launch { pager.animateScrollToPage(timerPage) } }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                )
                Spacer(Modifier.width(10.dp))
            }
            if (pager.currentPage > 0) {
                Text(
                    stringResource(R.string.cook_progress, pager.currentPage, steps.size),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = flavor.title,
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        ProgressSegments(total = pageCount, current = pager.currentPage)

        // --- As páginas ---
        HorizontalPager(state = pager, modifier = Modifier.weight(1f)) { page ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 34.dp),
            ) {
                if (page == 0) {
                    IngredientsPage(ingredients)
                } else {
                    val step = steps[page - 1]
                    SectionLabel(stringResource(R.string.cook_step, page), color = flavor.title)
                    Spacer(Modifier.height(14.dp))
                    Text(
                        step,
                        fontFamily = FrauncesFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 33.sp,
                        lineHeight = 40.sp,
                        letterSpacing = (-0.015).em,
                        color = flavor.text,
                    )

                    // Timer, se o passo falar de tempo.
                    val seconds = remember(step) { detectTimerSeconds(step) }
                    if (seconds != null) {
                        Spacer(Modifier.height(28.dp))
                        if (timerPage == page && (running || finished || remaining > 0)) {
                            TimerCard(
                                remaining = remaining,
                                running = running,
                                finished = finished,
                                onToggle = { running = !running },
                                onCancel = {
                                    running = false
                                    finished = false
                                    timerPage = -1
                                },
                            )
                        } else {
                            OutlinePill(
                                text = stringResource(R.string.cook_start_timer, formatClock(seconds)),
                                height = 56.dp,
                                fontSize = 17.sp,
                                onClick = {
                                    timerPage = page
                                    remaining = seconds
                                    finished = false
                                    running = true
                                },
                            )
                        }
                    }
                }
            }
        }

        // --- Rodapé: "tela acesa" + Anterior / Próximo ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(painterResource(R.drawable.ic_sun), contentDescription = null, tint = flavor.secondary, modifier = Modifier.size(17.dp))
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.cook_screen_on), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = flavor.secondary)
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            val last = pager.currentPage == pageCount - 1
            OutlinePill(
                text = stringResource(R.string.cook_previous),
                onClick = { scope.launch { pager.animateScrollToPage(pager.currentPage - 1) } },
                enabled = pager.currentPage > 0,
                color = flavor.text,
                borderColor = flavor.soft,
                height = 58.dp,
                fontSize = 17.sp,
                horizontalPadding = 8.dp,
                modifier = Modifier.weight(1f),
            )
            FilledPill(
                text = stringResource(if (last) R.string.cook_finish else R.string.cook_next),
                onClick = {
                    if (last) onClose()
                    else scope.launch { pager.animateScrollToPage(pager.currentPage + 1) }
                },
                height = 58.dp,
                fontSize = 17.sp,
                modifier = Modifier.weight(1.4f),
            )
        }
    }
}

// A barrinha de progresso em segmentos (um por página).
@Composable
private fun ProgressSegments(total: Int, current: Int) {
    val flavor = LocalFlavor.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        repeat(total) { i ->
            val color by animateColorAsState(
                if (i <= current) flavor.title else flavor.soft,
                label = "segment",
            )
            Box(
                Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(color),
            )
        }
    }
}

// Página 1: os ingredientes, para separar tudo antes de começar.
@Composable
private fun IngredientsPage(ingredients: List<RecipeIngredient>) {
    val flavor = LocalFlavor.current
    BrandTitle(stringResource(R.string.cook_prepare), fontSize = 38.sp)
    Spacer(Modifier.height(18.dp))
    ingredients.forEach { ing ->
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                ing.name,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = flavor.text,
                modifier = Modifier.weight(1f),
            )
            if (ing.quantity.isNotBlank()) {
                Spacer(Modifier.width(12.dp))
                Text(ing.quantity, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = flavor.secondary)
            }
        }
        HorizontalDivider(color = flavor.soft)
    }
}

// O timer contando: "18:42" + pausar/continuar + cancelar.
@Composable
private fun TimerCard(
    remaining: Int,
    running: Boolean,
    finished: Boolean,
    onToggle: () -> Unit,
    onCancel: () -> Unit,
) {
    val flavor = LocalFlavor.current
    if (finished) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BrandTitle(stringResource(R.string.cook_timer_done), fontSize = 34.sp, modifier = Modifier.weight(1f, fill = false))
            Spacer(Modifier.width(12.dp))
            RoundIconButton(
                icon = R.drawable.ic_close,
                description = stringResource(R.string.cook_cancel_timer),
                onClick = onCancel,
                iconSize = 20.dp,
            )
        }
        return
    }
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(flavor.title)
            .padding(start = 22.dp, end = 10.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            formatClock(remaining),
            fontFamily = FredokaFamily,
            fontSize = 42.sp,
            fontWeight = FontWeight.Bold,
            color = flavor.on,
        )
        Spacer(Modifier.width(14.dp))
        RoundIconButton(
            icon = if (running) R.drawable.ic_pause else R.drawable.ic_play,
            description = stringResource(if (running) R.string.cook_pause_timer else R.string.cook_resume_timer),
            onClick = onToggle,
            size = 56.dp,
            iconSize = 22.dp,
            container = flavor.on,
            contentColor = flavor.title,
        )
        Spacer(Modifier.width(8.dp))
        RoundIconButton(
            icon = R.drawable.ic_close,
            description = stringResource(R.string.cook_cancel_timer),
            onClick = onCancel,
            size = 48.dp,
            iconSize = 20.dp,
            container = flavor.title,
            contentColor = flavor.on,
        )
    }
}
