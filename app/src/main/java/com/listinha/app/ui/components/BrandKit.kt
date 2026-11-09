package com.listinha.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.listinha.app.ui.theme.FrauncesFamily
import com.listinha.app.ui.theme.LocalFlavor
import com.listinha.app.ui.theme.LocalPaper
import com.listinha.app.ui.theme.NunitoFamily

// ============================================================
//  BrandKit.kt  —  as peças da identidade do Listinha
// ------------------------------------------------------------
//  O título em serifa com uma palavra em itálico e a notinha de
//  papel picotada, que é a assinatura visual do app.
// ============================================================

/**
 * Transforma "O que *falta*" em texto com a palavra entre asteriscos em itálico.
 * Fica fora do composable para poder ser testado sem interface.
 */
fun brandText(raw: String): AnnotatedString = buildAnnotatedString {
    val partes = raw.split("*")
    partes.forEachIndexed { i, parte ->
        // Os trechos ímpares ficaram entre um par de asteriscos.
        if (i % 2 == 1 && i < partes.lastIndex) {
            pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
            append(parte)
            pop()
        } else {
            append(parte)
        }
    }
}

/**
 * Limita o quanto um texto cresce com a fonte do sistema. Usado em títulos e rótulos
 * de espaço fixo, onde 200% quebraria palavras no meio.
 *
 * @param max maior aumento aceito; 1f faz o texto ignorar a fonte do sistema
 */
@Composable
@ReadOnlyComposable
fun capFontScale(size: TextUnit, max: Float): TextUnit {
    val escala = LocalDensity.current.fontScale
    return if (escala > max) size * (max / escala) else size
}

/**
 * Título da marca: Fraunces 700 com uma palavra em itálico.
 *
 * @param text texto com a palavra de ênfase entre asteriscos
 * @param fontSize tamanho; o padrão é o dos títulos de tela
 * @param color cor; por padrão, a do título do sabor atual
 */
@Composable
fun BrandTitle(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 40.sp,
    color: Color = LocalFlavor.current.title,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
) {
    val annotated = remember(text) { brandText(text) }
    Text(
        text = annotated,
        modifier = modifier,
        color = color,
        textAlign = textAlign,
        maxLines = maxLines,
        style = TextStyle(
            fontFamily = FrauncesFamily,
            fontWeight = FontWeight.Bold,
            fontSize = capFontScale(fontSize, 1.2f),
            letterSpacing = (-0.03).em,
            lineHeight = 1.04.em,
            // Sem folga extra em cima e embaixo, para o título alinhar com os botões.
            lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.Both),
        ),
    )
}

/**
 * Formato da notinha: cantos quase retos em cima e picote em zigue-zague embaixo.
 *
 * @param tooth largura de cada dente do picote
 * @param depth profundidade do dente
 */
class TicketShape(
    private val tooth: Dp = 16.dp,
    private val depth: Dp = 8.dp,
    private val corner: Dp = 6.dp,
) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val toothPx = with(density) { tooth.toPx() }
        val depthPx = with(density) { depth.toPx() }
        val r = with(density) { corner.toPx() }
        // Ajusta a largura do dente para o picote fechar certinho nas duas pontas.
        val teeth = (size.width / toothPx).toInt().coerceAtLeast(1)
        val step = size.width / teeth
        val bottom = size.height
        val path = Path().apply {
            moveTo(0f, r)
            arcTo(Rect(Offset.Zero, Size(r * 2, r * 2)), 180f, 90f, false)
            lineTo(size.width - r, 0f)
            arcTo(Rect(Offset(size.width - r * 2, 0f), Size(r * 2, r * 2)), 270f, 90f, false)
            lineTo(size.width, bottom - depthPx)
            for (i in teeth downTo 1) {
                lineTo(step * i - step / 2, bottom)
                lineTo(step * (i - 1), bottom - depthPx)
            }
            close()
        }
        return Outline.Generic(path)
    }
}

/** Espaço que o picote ocupa embaixo; o conteúdo precisa terminar acima dele. */
val TicketToothSpace = 8.dp

/**
 * A notinha: papel creme com picote embaixo e sombra suave.
 * O conteúdo recebe a margem interna, já descontando o picote.
 */
@Composable
fun PaperTicket(
    modifier: Modifier = Modifier,
    contentPadding: Dp = 18.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val paper = LocalPaper.current
    val shape = remember { TicketShape() }
    Box(
        modifier = modifier
            .shadow(10.dp, shape, ambientColor = paper.shadow, spotColor = paper.shadow)
            .clip(shape)
            .background(paper.paper),
    ) {
        Column(
            modifier = Modifier.padding(
                start = contentPadding,
                end = contentPadding,
                top = contentPadding,
                bottom = contentPadding + TicketToothSpace,
            ),
            content = content,
        )
    }
}

/** Faixa com fundo chapado do sabor atual, usada como raiz das telas. */
@Composable
fun FlavorBackground(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(modifier = modifier.background(LocalFlavor.current.bg), content = content)
}

/** Botão preenchido em pílula, na cor do título do sabor. */
@Composable
fun FilledPill(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int? = null,
    height: Dp = 48.dp,
    enabled: Boolean = true,
    container: Color = LocalFlavor.current.title,
    contentColor: Color = LocalFlavor.current.on,
    fontSize: TextUnit = 15.sp,
    horizontalPadding: Dp = 20.dp,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = height).alpha(if (enabled) 1f else 0.45f),
        shape = RoundedCornerShape(999.dp),
        color = container,
        contentColor = contentColor,
    ) {
        Row(
            modifier = Modifier.heightIn(min = height).padding(horizontal = horizontalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            if (icon != null) {
                Icon(painterResource(icon), contentDescription = null, modifier = Modifier.size(19.dp))
                Spacer(Modifier.width(8.dp))
            }
            // Duas linhas: com fonte grande o rótulo quebra entre palavras em vez de sumir.
            Text(
                text,
                fontSize = capFontScale(fontSize, 1.3f),
                lineHeight = capFontScale(fontSize, 1.3f) * 1.15f,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** Botão só com contorno, em pílula. */
@Composable
fun OutlinePill(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 48.dp,
    color: Color = LocalFlavor.current.title,
    borderColor: Color = color,
    enabled: Boolean = true,
    fontSize: TextUnit = 15.sp,
    horizontalPadding: Dp = 18.dp,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = height).alpha(if (enabled) 1f else 0.4f),
        shape = RoundedCornerShape(999.dp),
        color = Color.Transparent,
        contentColor = color,
        border = BorderStroke(2.dp, borderColor),
    ) {
        Box(Modifier.heightIn(min = height).padding(horizontal = horizontalPadding), contentAlignment = Alignment.Center) {
            // Duas linhas: com fonte grande o rótulo quebra entre palavras em vez de sumir.
            Text(
                text,
                fontSize = capFontScale(fontSize, 1.3f),
                lineHeight = capFontScale(fontSize, 1.3f) * 1.15f,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** Borda tracejada arredondada, para áreas de "adicione aqui" como a foto da receita. */
fun Modifier.dashedBorder(color: Color, radius: Dp, width: Dp = 2.dp): Modifier = drawBehind {
    val px = width.toPx()
    drawRoundRect(
        color = color,
        topLeft = Offset(px / 2, px / 2),
        size = Size(size.width - px, size.height - px),
        cornerRadius = CornerRadius(radius.toPx()),
        style = Stroke(px, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8.dp.toPx(), 6.dp.toPx()))),
    )
}

/**
 * Composição dos estados vazios: foto e notinha inclinadas, com linhas em branco.
 *
 * @param label texto da notinha, com a palavra de ênfase entre asteriscos
 * @param photo foto que aparece inclinada atrás da notinha
 */
@Composable
fun EmptyArt(label: String, @DrawableRes photo: Int, modifier: Modifier = Modifier) {
    val paper = LocalPaper.current
    val flavor = LocalFlavor.current
    Box(modifier.fillMaxWidth().height(262.dp).clearAndSetSemantics { }) {
        val fotoForma = RoundedCornerShape(24.dp)
        Image(
            painter = painterResource(photo),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-8).dp, y = 6.dp)
                .size(width = 156.dp, height = 196.dp)
                .rotate(6f)
                .shadow(16.dp, fotoForma, ambientColor = paper.shadow, spotColor = paper.shadow)
                .clip(fotoForma),
        )
        PaperTicket(
            modifier = Modifier.offset(x = 4.dp, y = 56.dp).width(228.dp).rotate(-4f),
            contentPadding = 16.dp,
        ) {
            BrandTitle(label, fontSize = 22.sp, color = flavor.titleOnPaper)
            Spacer(Modifier.height(12.dp))
            repeat(3) { i ->
                if (i > 0) Spacer(Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(20.dp).border(2.dp, paper.line, RoundedCornerShape(6.dp)))
                    Spacer(Modifier.width(10.dp))
                    DashedDivider(Modifier.weight(1f))
                }
            }
        }
    }
}

/** Botão redondo só com ícone. */
@Composable
fun RoundIconButton(
    @DrawableRes icon: Int,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    iconSize: Dp = 24.dp,
    enabled: Boolean = true,
    container: Color = LocalFlavor.current.title,
    contentColor: Color = LocalFlavor.current.on,
    shape: Shape = CircleShape,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.size(size).alpha(if (enabled) 1f else 0.45f),
        shape = shape,
        color = container,
        contentColor = contentColor,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(painterResource(icon), contentDescription = description, modifier = Modifier.size(iconSize))
        }
    }
}

/**
 * Campo de texto da marca: sem borda, sobre o papel, com placeholder.
 *
 * @param description nome do campo para o leitor de tela
 * @param shape forma do campo; o padrão é uma pílula
 */
@Composable
fun BrandField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    description: String,
    modifier: Modifier = Modifier,
    height: Dp = 56.dp,
    fontSize: TextUnit = 17.sp,
    background: Color = LocalPaper.current.paper,
    shape: Shape = RoundedCornerShape(999.dp),
    shadow: Boolean = false,
    prefix: String? = null,
    @DrawableRes leadingIcon: Int? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    val paper = LocalPaper.current
    // Guarda cursor e seleção; se o valor mudar por fora, o cursor vai para o fim.
    var campo by remember { mutableStateOf(TextFieldValue(value, TextRange(value.length))) }
    val atual = if (campo.text == value) campo else TextFieldValue(value, TextRange(value.length))
    val base = if (shadow) {
        modifier.shadow(6.dp, shape, ambientColor = paper.shadow, spotColor = paper.shadow)
    } else {
        modifier
    }
    Row(
        modifier = base
            .clip(shape)
            .background(background)
            .heightIn(min = height)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leadingIcon != null) {
            Icon(painterResource(leadingIcon), contentDescription = null, tint = paper.muted, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
        }
        if (prefix != null) {
            Text(prefix, fontSize = fontSize, fontWeight = FontWeight.ExtraBold, color = paper.muted)
            Spacer(Modifier.width(6.dp))
        }
        BasicTextField(
            value = atual,
            onValueChange = { novo ->
                campo = novo
                if (novo.text != value) onValueChange(novo.text)
            },
            singleLine = true,
            modifier = Modifier.weight(1f).semantics { contentDescription = description },
            textStyle = TextStyle(
                fontFamily = NunitoFamily,
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                color = paper.ink,
            ),
            cursorBrush = SolidColor(paper.ink),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            decorationBox = { inner ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty()) {
                        Text(placeholder, fontSize = fontSize, fontWeight = FontWeight.Bold, color = paper.muted, maxLines = 1)
                    }
                    inner()
                }
            },
        )
    }
}

/** Linha tracejada do cupom. */
@Composable
fun DashedDivider(modifier: Modifier = Modifier, color: Color = LocalPaper.current.line) {
    Canvas(modifier.fillMaxWidth().height(2.dp)) {
        val dash = 7.dp.toPx()
        drawLine(
            color = color,
            start = Offset(0f, size.height / 2),
            end = Offset(size.width, size.height / 2),
            strokeWidth = size.height,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(dash, dash)),
        )
    }
}

/** Rótulo pequeno em caixa alta, como os títulos de seção da notinha. */
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier, color: Color = LocalPaper.current.muted) {
    Text(
        text = text.uppercase(),
        modifier = modifier,
        fontSize = 12.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 0.09.em,
        color = color,
    )
}

/**
 * Caixa de confirmação da marca, com título em serifa.
 *
 * @param title título com a palavra de ênfase entre asteriscos
 * @param danger pinta o botão de confirmar com a cor de perigo
 */
@Composable
fun BrandAlert(
    title: String,
    text: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    danger: Boolean = false,
) {
    val paper = LocalPaper.current
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        containerColor = paper.paper,
        title = { BrandTitle(title, fontSize = 30.sp, color = paper.ink) },
        text = { Text(text, fontSize = 15.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold, color = paper.muted) },
        confirmButton = {
            FilledPill(
                text = confirmLabel,
                onClick = onConfirm,
                container = if (danger) paper.danger else LocalFlavor.current.titleOnPaper,
                contentColor = if (danger) paper.onDanger else MaterialTheme.colorScheme.onPrimary,
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.heightIn(min = 48.dp)) {
                Text("Cancelar", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = paper.ink)
            }
        },
    )
}
