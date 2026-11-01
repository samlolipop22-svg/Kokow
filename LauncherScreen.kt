package com.glass.launcher

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val Glow = Color(0xFF9DB2FF)

/** Liquid-glass surface: frosted fill, bright rim light, specular highlight, optional light sweep. */
fun Modifier.glass(shape: Shape, focus: Float = 0f, sweep: Float = 0f): Modifier = this
    .clip(shape)
    .background(
        Brush.linearGradient(
            listOf(Color.White.copy(.15f + .12f * focus), Color.White.copy(.05f + .05f * focus))
        )
    )
    .border(
        1.dp,
        Brush.linearGradient(listOf(Color.White.copy(.8f), Color.White.copy(.06f), Color.White.copy(.4f))),
        shape
    )
    .drawWithContent {
        drawContent()
        drawRect(
            Brush.radialGradient(
                listOf(Color.White.copy(.24f), Color.Transparent),
                Offset(size.width * .15f, 0f),
                size.width * .9f
            )
        )
        if (sweep > 0f && sweep < 1f) {
            val cx = size.width * (-.3f + 1.6f * sweep)
            drawRect(
                Brush.linearGradient(
                    listOf(Color.Transparent, Color.White.copy(.4f), Color.Transparent),
                    Offset(cx - size.width * .2f, 0f),
                    Offset(cx + size.width * .2f, size.height * .4f)
                )
            )
        }
    }

@Composable
fun LauncherScreen(apps: List<AppEntry>, onOpen: (AppEntry) -> Unit, onInfo: (AppEntry) -> Unit) {
    var focused by remember { mutableIntStateOf(0) }
    val first = remember { FocusRequester() }
    LaunchedEffect(apps.isNotEmpty()) {
        if (apps.isNotEmpty()) { delay(50); runCatching { first.requestFocus() } }
    }

    Box(Modifier.fillMaxSize().background(Color(0xFF05070F))) {
        Aurora(focused)
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(.65f)))
            )
        )
        Column(Modifier.fillMaxSize().padding(top = 36.dp, bottom = 24.dp)) {
            TopBar(Modifier.padding(horizontal = 64.dp))
            Spacer(Modifier.weight(1f))
            Hero(apps.getOrNull(focused)?.label ?: "", Modifier.padding(horizontal = 64.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 64.dp, vertical = 40.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                itemsIndexed(apps, key = { _, a -> a.component.flattenToString() }) { i, app ->
                    AppTile(
                        app, i,
                        if (i == 0) Modifier.focusRequester(first) else Modifier,
                        onFocus = { focused = i },
                        onOpen = { onOpen(app) },
                        onInfo = { onInfo(app) }
                    )
                }
            }
        }
    }
}

/** Slow drifting colour blobs; they parallax-shift as focus moves along the row. */
@Composable
private fun Aurora(focused: Int) {
    val t = rememberInfiniteTransition(label = "aurora")
    val p by t.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(18000, easing = LinearEasing), RepeatMode.Reverse),
        label = "p"
    )
    val shift by animateFloatAsState(
        focused.coerceAtMost(12) * 50f, spring(dampingRatio = 0.8f, stiffness = 20f), label = "shift"
    )
    Canvas(Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        fun blob(c: Color, cx: Float, cy: Float, r: Float) = drawCircle(
            Brush.radialGradient(listOf(c.copy(alpha = .55f), Color.Transparent), Offset(cx, cy), r),
            r, Offset(cx, cy)
        )
        blob(Color(0xFF5B6CFF), w * (.2f + .15f * p) - shift, h * (.3f + .1f * p), w * .45f)
        blob(Color(0xFFFF4FA3), w * (.8f - .12f * p) - shift * .6f, h * (.25f + .15f * p), w * .4f)
        blob(Color(0xFF22D3EE), w * (.5f + .2f * p) - shift * .3f, h * (.85f - .1f * p), w * .5f)
    }
}

@Composable
private fun Pill(content: @Composable () -> Unit) {
    Box(Modifier.glass(RoundedCornerShape(50)).padding(horizontal = 28.dp, vertical = 14.dp)) { content() }
}

@Composable
private fun TopBar(modifier: Modifier = Modifier) {
    var now by remember { mutableStateOf(Date()) }
    LaunchedEffect(Unit) { while (true) { now = Date(); delay(15_000) } }
    val hour = Calendar.getInstance().apply { time = now }.get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        hour < 5 -> "Good night"
        hour < 12 -> "Good morning"
        hour < 18 -> "Good afternoon"
        else -> "Good evening"
    }
    val style = TextStyle(color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Medium)
    Row(modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
        Pill { BasicText(greeting, style = style) }
        Pill {
            BasicText(
                DateFormat.getTimeInstance(DateFormat.SHORT).format(now) + "  ·  " +
                    SimpleDateFormat("EEE d MMM", Locale.getDefault()).format(now),
                style = style
            )
        }
    }
}

@Composable
private fun Hero(title: String, modifier: Modifier = Modifier) {
    AnimatedContent(
        targetState = title,
        modifier = modifier.height(84.dp),
        contentAlignment = Alignment.CenterStart,
        transitionSpec = {
            (fadeIn(tween(350)) + slideInVertically(tween(350)) { it / 4 }) togetherWith
                (fadeOut(tween(200)) + slideOutVertically(tween(200)) { -it / 4 })
        },
        label = "hero"
    ) { text ->
        BasicText(
            text, maxLines = 1, overflow = TextOverflow.Ellipsis,
            style = TextStyle(color = Color.White, fontSize = 56.sp, fontWeight = FontWeight.Bold, letterSpacing = (-1).sp)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppTile(
    app: AppEntry,
    index: Int,
    modifier: Modifier,
    onFocus: () -> Unit,
    onOpen: () -> Unit,
    onInfo: () -> Unit,
) {
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        if (focused) 1.14f else 1f, spring(dampingRatio = .55f, stiffness = 380f), label = "scale"
    )
    val f by animateFloatAsState(if (focused) 1f else 0f, tween(260), label = "f")
    val sweep = remember { Animatable(0f) }
    val enter = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        delay(if (index < 8) index * 60L else 0L)
        enter.animateTo(1f, tween(600, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(focused) {
        sweep.snapTo(0f)
        if (focused) sweep.animateTo(1f, tween(750, easing = FastOutSlowInEasing))
    }

    val shape = RoundedCornerShape(26.dp)
    Column(
        Modifier
            .width(196.dp)
            .graphicsLayer {
                val s = scale
                scaleX = s; scaleY = s
                alpha = enter.value
                translationY = (1f - enter.value) * 80.dp.toPx() - 14.dp.toPx() * f
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier
                .onFocusChanged { focused = it.isFocused; if (it.isFocused) onFocus() }
                .combinedClickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onLongClick = onInfo,
                    onClick = onOpen
                )
                .size(196.dp, 110.dp)
                .shadow((30 * f).dp, shape, clip = false, ambientColor = Glow, spotColor = Glow)
                .glass(shape, f, sweep.value),
            contentAlignment = Alignment.Center
        ) {
            if (app.isBanner) {
                Image(app.image, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else {
                Image(app.image, null, Modifier.size(72.dp))
            }
        }
        BasicText(
            app.label, maxLines = 1, overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 14.dp).graphicsLayer { alpha = .55f + .45f * f },
            style = TextStyle(color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
        )
    }
}
