package com.kampplus.hava.ui.lofi

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.tooling.preview.Preview
import kotlin.random.Random

/**
 * Uygulamanın arka planı: yumuşak lofi gökyüzü + katmanlı tepeler + hafif tane dokusu.
 *
 * Tamamen kodla çizilir (görsel dosyası yok) ve animasyonsuzdur, yani pil tüketmez.
 * [dark] gece/koyu temada lacivert tonlar + ay ve yıldızlar, aksi halde şeftali-lavanta tonlar çizer.
 */

@Composable
fun LofiBackground(dark: Boolean, modifier: Modifier = Modifier) {
    // Rastgele noktalar her çizimde değişmesin diye bir kez üretilir.
    val grain = remember { Random(11).let { r -> List(260) { Offset(r.nextFloat(), r.nextFloat()) } } }
    val stars = remember { Random(5).let { r -> List(45) { Offset(r.nextFloat(), r.nextFloat() * 0.5f) } } }

    Canvas(modifier) {
        val w = size.width
        val h = size.height

        // Gökyüzü
        val sky = if (dark) {
            listOf(Color(0xFF141737), Color(0xFF2A2556), Color(0xFF3A2F63))
        } else {
            listOf(Color(0xFFFBE3D6), Color(0xFFF3D5E3), Color(0xFFD9D4F2))
        }
        drawRect(Brush.verticalGradient(sky))

        // Güneş / ay parıltısı
        val glowCenter = Offset(w * 0.78f, h * 0.16f)
        val glowColor = if (dark) Color(0xFFF3EBD0) else Color(0xFFFFD6A5)
        drawCircle(
            Brush.radialGradient(
                listOf(glowColor.copy(alpha = if (dark) 0.22f else 0.65f), Color.Transparent),
                glowCenter,
                w * 0.55f
            ),
            w * 0.55f,
            glowCenter
        )
        drawCircle(glowColor.copy(alpha = if (dark) 0.9f else 0.55f), w * 0.07f, glowCenter)

        if (dark) {
            stars.forEach { s -> drawCircle(Color.White.copy(alpha = 0.55f), 1.6f, Offset(s.x * w, s.y * h)) }
        }

        // Katmanlı tepeler (uzaktan yakına, giderek koyulaşır)
        val hills = if (dark) {
            listOf(Color(0xFF3B3470), Color(0xFF2D2760), Color(0xFF211C4A))
        } else {
            listOf(Color(0xFFE4C9E3), Color(0xFFCFB8DD), Color(0xFFB8A5CF))
        }
        hills.forEachIndexed { i, color ->
            val base = h * (0.72f + i * 0.08f)
            val path = Path().apply {
                moveTo(0f, base)
                cubicTo(w * 0.25f, base - h * 0.07f, w * 0.5f, base + h * 0.05f, w * 0.75f, base - h * 0.04f)
                cubicTo(w * 0.88f, base - h * 0.07f, w * 0.95f, base - h * 0.02f, w, base - h * 0.03f)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(path, color.copy(alpha = 0.55f + i * 0.15f))
        }

        // Plak/film tanesi hissi veren çok hafif noktalar
        val grainColor = if (dark) Color.White else Color(0xFF5B3A55)
        grain.forEach { g -> drawCircle(grainColor.copy(alpha = 0.06f), 1.2f, Offset(g.x * w, g.y * h)) }
    }
}

@Preview(widthDp = 360, heightDp = 640)
@Composable
private fun LofiBackgroundPreview() {
    LofiBackground(dark = false, modifier = Modifier.fillMaxSize())
}
