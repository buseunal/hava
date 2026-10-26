package com.kampplus.hava.ui.lofi

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kampplus.hava.model.WeatherCondition
import kotlin.math.sin
import kotlin.random.Random

/**
 * Hava durumuna göre değişen lofi sahne (pencereden görünen gökyüzü + masa,
 * bitki, kupa) ve altında lofi müziği çalan/durduran bir düğme.
 */

@Composable
fun LofiWeatherPanel(condition: WeatherCondition, modifier: Modifier = Modifier, isDay: Boolean = true) {
    val player = remember { LofiPlayer() }
    var playing by rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(condition, isDay, playing) {
        if (playing) player.play(condition, isDay) else player.stop()
    }
    DisposableEffect(Unit) { onDispose { player.release() } }

    Box(
        modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(24.dp))
    ) {
        LofiScene(condition, Modifier.fillMaxSize(), isDay)
        FilledTonalButton(
            onClick = { playing = !playing },
            modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp)
        ) {
            Text(if (playing) "❚❚  Lofi" else "▶  Lofi")
        }
    }
}

private data class Palette(val skyTop: Color, val skyBottom: Color, val room: Color, val accent: Color)

// Gece için paleti lacivert tona doğru kaydırır; hava durumunun karakteri korunur.
private fun Palette.atNight() = Palette(
    skyTop = lerp(skyTop, Color(0xFF070B24), 0.85f),
    skyBottom = lerp(skyBottom, Color(0xFF26305E), 0.8f),
    room = lerp(room, Color(0xFF14101F), 0.55f),
    accent = lerp(accent, Color(0xFFFFC27A), 0.4f)
)

private fun paletteOf(c: WeatherCondition) = when (c) {
    WeatherCondition.CLEAR, WeatherCondition.MAINLY_CLEAR ->
        Palette(Color(0xFFFFB38A), Color(0xFFFFE0B5), Color(0xFF5B3A55), Color(0xFFFFD66B))
    WeatherCondition.PARTLY_CLOUDY ->
        Palette(Color(0xFF8EC5E8), Color(0xFFF6D7C3), Color(0xFF4A3D5C), Color(0xFFFFE08A))
    WeatherCondition.OVERCAST, WeatherCondition.FOG ->
        Palette(Color(0xFF9AA3B5), Color(0xFFC9CCD6), Color(0xFF3E3D4F), Color(0xFFE8D9B0))
    WeatherCondition.DRIZZLE, WeatherCondition.RAIN, WeatherCondition.RAIN_SHOWERS ->
        Palette(Color(0xFF3B4A6B), Color(0xFF6D7FA3), Color(0xFF2A2438), Color(0xFFFFB86B))
    WeatherCondition.SNOW, WeatherCondition.SNOW_SHOWERS ->
        Palette(Color(0xFFA9B8D9), Color(0xFFE9EEF7), Color(0xFF3C3A55), Color(0xFFFFC48A))
    WeatherCondition.THUNDERSTORM ->
        Palette(Color(0xFF1C1B33), Color(0xFF43406B), Color(0xFF16131F), Color(0xFFFF9E5E))
    WeatherCondition.UNKNOWN ->
        Palette(Color(0xFF7F7FB5), Color(0xFFD9C3E8), Color(0xFF3B3350), Color(0xFFFFD08A))
}

@Composable
fun LofiScene(condition: WeatherCondition, modifier: Modifier = Modifier, isDay: Boolean = true) {
    val transition = rememberInfiniteTransition(label = "lofi")
    val time by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(6000, easing = LinearEasing), RepeatMode.Restart),
        label = "time"
    )
    val pal = if (isDay) paletteOf(condition) else paletteOf(condition).atNight()
    val particles = remember(condition) { Random(condition.ordinal).let { r -> List(70) { Offset(r.nextFloat(), r.nextFloat()) } } }

    Canvas(modifier) {
        // Oda duvarı
        drawRect(pal.room)

        // Pencere
        val winL = size.width * 0.12f
        val winT = size.height * 0.08f
        val winW = size.width * 0.76f
        val winH = size.height * 0.62f
        val winOff = Offset(winL, winT)
        val winSize = Size(winW, winH)
        drawRect(Brush.verticalGradient(listOf(pal.skyTop, pal.skyBottom), startY = winT, endY = winT + winH), winOff, winSize)

        clipWindow(winOff, winSize) {
            drawSky(condition, pal, time, particles, winOff, winSize, isDay)
            // Uzak şehir silueti
            val r = Random(3)
            var x = winL
            while (x < winL + winW) {
                val bw = winW * (0.06f + r.nextFloat() * 0.06f)
                val bh = winH * (0.12f + r.nextFloat() * 0.22f)
                drawRect(pal.room.copy(alpha = 0.55f), Offset(x, winT + winH - bh), Size(bw, bh))
                if (r.nextBoolean()) {
                    drawRect(
                        pal.accent.copy(alpha = 0.7f),
                        Offset(x + bw * 0.3f, winT + winH - bh * 0.7f),
                        Size(
                            bw * 0.18f,
                            bh * 0.1f
                        )
                    )
                }
                x += bw + 2f
            }
        }

        // Pencere çerçevesi
        drawRect(Color(0xFF2B2233), winOff, winSize, style = Stroke(10f))
        drawLine(Color(0xFF2B2233), Offset(winL + winW / 2, winT), Offset(winL + winW / 2, winT + winH), 6f)
        drawLine(Color(0xFF2B2233), Offset(winL, winT + winH / 2), Offset(winL + winW, winT + winH / 2), 6f)

        // Masa
        val deskY = size.height * 0.76f
        drawRect(Color(0xFF6B4636), Offset(0f, deskY), Size(size.width, size.height - deskY))
        drawRect(Color(0xFF85573F), Offset(0f, deskY), Size(size.width, 8f))

        // Saksı bitkisi
        val potX = size.width * 0.16f
        drawRoundRect(Color(0xFFC0694E), Offset(potX, deskY - 34f), Size(46f, 34f), CornerRadius(6f))
        val sway = sin(time * 6.283f) * 4f
        for (i in -2..2) {
            drawLine(
                Color(0xFF5E9E6E),
                Offset(potX + 23f, deskY - 34f),
                Offset(potX + 23f + i * 16f + sway, deskY - 80f + kotlin.math.abs(i) * 12f),
                9f
            )
        }

        // Kupa + buhar
        val cupX = size.width * 0.62f
        drawRoundRect(Color(0xFFEFE6DD), Offset(cupX, deskY - 30f), Size(34f, 30f), CornerRadius(5f))
        drawCircle(Color(0xFFEFE6DD), 9f, Offset(cupX + 38f, deskY - 16f), style = Stroke(5f))
        for (k in 0..1) {
            val p = (time + k * 0.5f) % 1f
            drawCircle(Color.White.copy(alpha = (1 - p) * 0.35f), 6f + p * 6f, Offset(cupX + 17f + sin(p * 9f) * 5f, deskY - 36f - p * 40f))
        }

        // Lamba ışığı
        drawCircle(
            Brush.radialGradient(
                listOf(pal.accent.copy(alpha = 0.28f), Color.Transparent),
                Offset(size.width * 0.9f, deskY),
                size.height * 0.5f
            ),
            size.height * 0.5f,
            Offset(size.width * 0.9f, deskY)
        )

        // Fırtınada şimşek parlaması
        if (condition == WeatherCondition.THUNDERSTORM && time in 0.50f..0.53f) {
            drawRect(Color.White.copy(alpha = 0.45f))
        }
    }
}

private inline fun DrawScope.clipWindow(o: Offset, s: Size, block: DrawScope.() -> Unit) {
    drawContext.canvas.save()
    drawContext.canvas.clipRect(o.x, o.y, o.x + s.width, o.y + s.height)
    block()
    drawContext.canvas.restore()
}

private fun DrawScope.drawCloud(c: Offset, w: Float, color: Color) {
    drawCircle(color, w * 0.22f, c)
    drawCircle(color, w * 0.28f, c + Offset(w * 0.25f, -w * 0.08f))
    drawCircle(color, w * 0.2f, c + Offset(w * 0.5f, 0f))
    drawRoundRect(color, c + Offset(-w * 0.1f, 0f), Size(w * 0.7f, w * 0.2f), CornerRadius(w * 0.1f))
}

private fun DrawScope.drawSky(c: WeatherCondition, pal: Palette, time: Float, particles: List<Offset>, o: Offset, s: Size, isDay: Boolean) {
    val cloudCount = when (c) {
        WeatherCondition.CLEAR -> 0
        WeatherCondition.MAINLY_CLEAR -> 1
        WeatherCondition.PARTLY_CLOUDY -> 2
        else -> 4
    }
    val cloudColor = when (c) {
        WeatherCondition.THUNDERSTORM -> Color(0xFF2E2B4A)
        WeatherCondition.RAIN, WeatherCondition.RAIN_SHOWERS, WeatherCondition.DRIZZLE -> Color(0xFF55617F)
        else -> Color.White.copy(alpha = 0.85f)
    }

    // Güneş (gündüz) ya da ay + yıldızlar (gece) — açık/az bulutlu/parçalı havada
    if (cloudCount <= 2) {
        val body = o + Offset(s.width * 0.72f, s.height * 0.3f)
        if (isDay) {
            drawCircle(pal.accent.copy(alpha = 0.35f), s.height * 0.22f, body)
            drawCircle(pal.accent, s.height * 0.14f, body)
        } else {
            particles.take(28).forEachIndexed { i, p ->
                val twinkle = 0.45f + 0.4f * sin((time + i * 0.13f) * 6.283f)
                drawCircle(Color.White.copy(alpha = twinkle), 1.5f + p.x * 1.5f, o + Offset(p.x * s.width, p.y * s.height * 0.6f))
            }
            drawCircle(Color(0xFFF3EBD0).copy(alpha = 0.18f), s.height * 0.2f, body)
            drawCircle(Color(0xFFF3EBD0), s.height * 0.11f, body)
        }
    }

    // Kayan bulutlar
    repeat(cloudCount) { i ->
        val speed = 0.5f + i * 0.2f
        val x = ((time * speed + i * 0.3f) % 1.3f - 0.3f) * s.width
        drawCloud(o + Offset(x, s.height * (0.18f + i * 0.1f)), s.width * 0.3f, cloudColor)
    }

    when (c) {
        WeatherCondition.DRIZZLE, WeatherCondition.RAIN, WeatherCondition.RAIN_SHOWERS, WeatherCondition.THUNDERSTORM -> {
            val len = if (c == WeatherCondition.DRIZZLE) 8f else 16f
            val drops = if (c == WeatherCondition.DRIZZLE) particles.take(30) else particles
            drops.forEach { p ->
                val y = ((p.y + time * 4f) % 1f) * s.height
                val x = p.x * s.width
                drawLine(Color(0xFFBFD3F2).copy(alpha = 0.7f), o + Offset(x, y), o + Offset(x - 3f, y + len), 2.5f)
            }
            if (c == WeatherCondition.THUNDERSTORM && time in 0.48f..0.56f) {
                val bx = o.x + s.width * 0.4f
                val pts = listOf(0f to 0f, 20f to 40f, 5f to 45f, 30f to 95f)
                for (k in 0 until pts.size - 1) {
                    drawLine(
                        Color(0xFFFFF4A3),
                        Offset(bx + pts[k].first, o.y + pts[k].second),
                        Offset(
                            bx + pts[k + 1].first,
                            o.y + pts[k + 1].second
                        ),
                        5f
                    )
                }
            }
        }
        WeatherCondition.SNOW, WeatherCondition.SNOW_SHOWERS -> particles.forEach { p ->
            val y = ((p.y + time) % 1f) * s.height
            val x = p.x * s.width + sin((time + p.y) * 12f) * 8f
            drawCircle(Color.White, 3f + p.x * 3f, o + Offset(x, y))
        }
        WeatherCondition.FOG -> repeat(4) { i ->
            val shift = sin((time + i * 0.25f) * 6.283f) * 30f
            drawRoundRect(
                Color.White.copy(alpha = 0.35f),
                o + Offset(-40f + shift, s.height * (0.3f + i * 0.17f)),
                Size(s.width + 80f, s.height * 0.08f),
                CornerRadius(30f)
            )
        }
        else -> Unit
    }
}

@Preview(widthDp = 360, heightDp = 220)
@Composable
private fun LofiScenePreview() {
    LofiScene(WeatherCondition.RAIN, Modifier.fillMaxSize())
}
