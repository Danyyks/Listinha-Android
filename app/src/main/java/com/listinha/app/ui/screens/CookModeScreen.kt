package com.listinha.app.ui.screens

import android.media.RingtoneManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.sp
import com.listinha.app.R
import com.listinha.app.data.RecipeIngredient
import com.listinha.app.data.categoryColor
import com.listinha.app.ui.theme.FredokaFamily
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
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        CookModeContent(recipeName, ingredients, steps, onClose)
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
    var timerPage by remember { mutableIntStateOf(-1) }   // de qual passo é o timer (-1 = nenhum)
    var remaining by remember { mutableIntStateOf(0) }    // segundos que faltam
    var running by remember { mutableStateOf(false) }
    var finished by remember { mutableStateOf(false) }

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        // --- Topo: fechar, nome, "3 de 6" e a barra de progresso ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 4.dp, end = 20.dp, top = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onClose, modifier = Modifier.size(48.dp)) {
                Icon(
                    painterResource(R.drawable.ic_close),
                    contentDescription = stringResource(R.string.cook_exit),
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
            Text(
                recipeName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .clickable { scope.launch { pager.animateScrollToPage(timerPage) } }
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                )
                Spacer(Modifier.width(10.dp))
            }
            if (pager.currentPage > 0) {
                Text(
                    stringResource(R.string.cook_progress, pager.currentPage, steps.size),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
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
                    .padding(horizontal = 28.dp, vertical = 36.dp),
            ) {
                if (page == 0) {
                    IngredientsPage(ingredients)
                } else {
                    val step = steps[page - 1]
                    Text(
                        stringResource(R.string.cook_step, page),
                        fontFamily = FredokaFamily,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        step,
                        fontSize = 27.sp,
                        lineHeight = 37.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                    )

                    // Timer, se o passo falar de tempo.
                    val seconds = remember(step) { detectTimerSeconds(step) }
                    if (seconds != null) {
                        Spacer(Modifier.height(32.dp))
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
                            StartTimerButton(seconds) {
                                timerPage = page
                                remaining = seconds
                                finished = false
                                running = true
                            }
                        }
                    }
                }
            }
        }

        // --- Rodapé: "tela acesa" + Anterior / Próximo ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painterResource(R.drawable.ic_sun),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                stringResource(R.string.cook_screen_on),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val last = pager.currentPage == pageCount - 1
            OutlinedButton(
                onClick = { scope.launch { pager.animateScrollToPage(pager.currentPage - 1) } },
                enabled = pager.currentPage > 0,
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.weight(1f).height(60.dp),
            ) {
                Text(stringResource(R.string.cook_previous), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
            }
            Button(
                onClick = {
                    if (last) onClose()
                    else scope.launch { pager.animateScrollToPage(pager.currentPage + 1) }
                },
                modifier = Modifier.weight(1.4f).height(60.dp),
            ) {
                Text(
                    stringResource(if (last) R.string.cook_finish else R.string.cook_next),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
        }
    }
}

// A barrinha de progresso em segmentos (um por página).
@Composable
private fun ProgressSegments(total: Int, current: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        repeat(total) { i ->
            val color by animateColorAsState(
                if (i <= current) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                label = "segment",
            )
            Box(
                Modifier
                    .weight(1f)
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(color),
            )
        }
    }
}

// Página 1: os ingredientes, para separar tudo antes de começar.
@Composable
private fun IngredientsPage(ingredients: List<RecipeIngredient>) {
    Text(
        stringResource(R.string.cook_prepare),
        fontFamily = FredokaFamily,
        fontSize = 26.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onBackground,
    )
    Spacer(Modifier.height(18.dp))
    ingredients.forEach { ing ->
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(categoryColor(ing.category)),
            )
            Spacer(Modifier.width(14.dp))
            Text(
                ing.name,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f),
            )
            if (ing.quantity.isNotBlank()) {
                Spacer(Modifier.width(10.dp))
                Text(ing.quantity, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// Pílula "Timer 25:00" — toque e começa a contar.
@Composable
private fun StartTimerButton(seconds: Int, onStart: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(26.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .clickable(onClick = onStart)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painterResource(R.drawable.ic_clock),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.width(10.dp))
        Text(
            stringResource(R.string.cook_start_timer, formatClock(seconds)),
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
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
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(26.dp))
            .background(
                if (finished) MaterialTheme.colorScheme.errorContainer
                else MaterialTheme.colorScheme.primaryContainer
            )
            .padding(start = 20.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painterResource(R.drawable.ic_clock),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(26.dp),
        )
        Spacer(Modifier.width(14.dp))
        Text(
            if (finished) stringResource(R.string.cook_timer_done) else formatClock(remaining),
            fontFamily = FredokaFamily,
            fontSize = if (finished) 22.sp else 34.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (finished) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer,
        )
        Spacer(Modifier.width(14.dp))
        if (!finished) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable(onClick = onToggle),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painterResource(if (running) R.drawable.ic_pause else R.drawable.ic_play),
                    contentDescription = stringResource(
                        if (running) R.string.cook_pause_timer else R.string.cook_resume_timer
                    ),
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(Modifier.width(8.dp))
        }
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
                .clickable(onClick = onCancel),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painterResource(R.drawable.ic_close),
                contentDescription = stringResource(R.string.cook_cancel_timer),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}
