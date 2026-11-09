package com.listinha.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.listinha.app.R
import com.listinha.app.ui.components.BrandTitle
import com.listinha.app.ui.components.PaperTicket
import com.listinha.app.ui.components.capFontScale
import com.listinha.app.ui.theme.Acai
import com.listinha.app.ui.theme.AcaiMuted
import com.listinha.app.ui.theme.AcaiRose
import com.listinha.app.ui.theme.Area
import com.listinha.app.ui.theme.AreaTheme
import com.listinha.app.ui.theme.Creme
import com.listinha.app.ui.theme.FredokaFamily
import com.listinha.app.ui.theme.LocalPaper
import com.listinha.app.ui.theme.LightPaper
import com.listinha.app.ui.theme.Uva
import androidx.compose.runtime.CompositionLocalProvider

// ============================================================
//  LoginScreen.kt  —  a tela de boas-vindas
// ------------------------------------------------------------
//  Foto de smash burger, a frase da marca e uma notinha com os
//  ingredientes. As cores são fixas: a primeira impressão é igual
//  em qualquer tema. O botão do Google segue o padrão oficial.
// ============================================================

@Composable
fun LoginScreen(
    isLoading: Boolean,
    errorMessage: String?,
    onSignIn: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AreaTheme(Area.ACAI, lightStatusIcons = false) {
        // A notinha é sempre creme aqui, mesmo com o aparelho no tema escuro.
        CompositionLocalProvider(LocalPaper provides LightPaper) {
            var alturaPainel by remember { mutableIntStateOf(0) }
            var alturaTela by remember { mutableIntStateOf(0) }
            var fimTitulo by remember { mutableIntStateOf(0) }
            var alturaNota by remember { mutableIntStateOf(0) }
            val density = LocalDensity.current
            Box(modifier = modifier.fillMaxSize().background(Acai).onSizeChanged { alturaTela = it.height }) {
                Image(
                    painter = painterResource(R.drawable.welcome_burger),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.TopCenter,
                    modifier = Modifier.fillMaxSize(),
                )
                BrandTitle(
                    "Deu vontade?\nVirou *lista.*",
                    fontSize = 52.sp,
                    color = Acai,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onSizeChanged { fimTitulo = it.height }
                        .statusBarsPadding()
                        .padding(start = 16.dp, end = 16.dp, top = 22.dp),
                )
                // A notinha fica logo acima do painel. Em tela pequena ela cobriria o título
                // (a altura medida do título já inclui a barra de status): aí ela não aparece.
                val folga = with(density) { 56.dp.toPx() }
                val cabe = alturaTela - alturaPainel - fimTitulo - alturaNota > folga
                PaperTicket(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .alpha(if (cabe) 1f else 0f)
                        .padding(end = 20.dp, bottom = with(density) { alturaPainel.toDp() } + 28.dp)
                        .onSizeChanged { alturaNota = it.height }
                        .width(180.dp)
                        .rotate(5f)
                        .clearAndSetSemantics { },
                    contentPadding = 14.dp,
                ) {
                    BrandTitle("*Smash burger*", fontSize = 21.sp, color = Uva)
                    Spacer(Modifier.height(8.dp))
                    TicketLine("Pão brioche", true)
                    TicketLine("Carne moída", true)
                    TicketLine("Cheddar", true)
                    TicketLine("Bacon", false)
                }

                // O painel açaí fica sobre a foto, com os cantos arredondados.
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .onSizeChanged { alturaPainel = it.height }
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 34.dp, topEnd = 34.dp))
                        .background(Acai)
                        .navigationBarsPadding()
                        .padding(start = 24.dp, end = 24.dp, top = 26.dp, bottom = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        text = stringResource(R.string.app_name),
                        fontFamily = FredokaFamily,
                        // A marca é um desenho: não cresce com a fonte do sistema.
                        fontSize = capFontScale(76.sp, 1f),
                        lineHeight = capFontScale(76.sp, 1f),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-1.5).sp,
                        color = AcaiRose,
                    )

                    // Botão oficial do Google: fundo claro, logo colorido e texto padrão.
                    Button(
                        onClick = onSignIn,
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth().widthIn(max = 480.dp).heightIn(min = 58.dp),
                        shape = RoundedCornerShape(999.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Creme,
                            contentColor = LightPaper.ink,
                            disabledContainerColor = Creme,
                            disabledContentColor = LightPaper.ink,
                        ),
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Uva,
                                strokeWidth = 2.dp,
                            )
                        } else {
                            // Image, não Icon, para manter as 4 cores do logo do Google.
                            Image(
                                painter = painterResource(R.drawable.ic_google),
                                contentDescription = null,
                                modifier = Modifier.size(22.dp),
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(text = "Continuar com Google", fontSize = capFontScale(17.sp, 1.2f), fontWeight = FontWeight.ExtraBold, maxLines = 2)
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.ic_lock),
                            contentDescription = null,
                            tint = AcaiMuted,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Entrar é rápido e seguro",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AcaiMuted,
                        )
                    }

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = AcaiRose,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

// Uma linha da notinha decorativa: ingrediente marcado ou ainda em aberto.
@Composable
private fun TicketLine(label: String, checked: Boolean) {
    Row(
        modifier = Modifier.padding(vertical = 3.5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(if (checked) Uva else Color.Transparent),
            contentAlignment = Alignment.Center,
        ) {
            if (checked) {
                Icon(painterResource(R.drawable.ic_check), contentDescription = null, tint = Creme, modifier = Modifier.size(12.dp))
            } else {
                Box(
                    Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(LightPaper.muted),
                ) {
                    Box(Modifier.align(Alignment.Center).size(14.dp).clip(CircleShape).background(Creme))
                }
            }
        }
        Spacer(Modifier.width(8.dp))
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = LightPaper.ink)
    }
}
