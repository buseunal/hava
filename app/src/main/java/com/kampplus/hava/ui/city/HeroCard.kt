package com.kampplus.hava.ui.city

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kampplus.hava.ui.theme.HavaTheme
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

// Günün hangi diliminde olduğumuz; selamı, emojiyi ve renkleri belirler.
private enum class DayPart(
    val greeting: String,
    val emoji: String,
    val from: Color,
    val to: Color,
    val text: Color
) {
    MORNING("Günaydın", "☀️", Color(0xFFFFE0B8), Color(0xFFFFB59E), Color(0xFF4A2E3F)),
    DAY("İyi günler", "🌤️", Color(0xFFCDE9F8), Color(0xFFD9D4F2), Color(0xFF2F3552)),
    EVENING("İyi akşamlar", "🌆", Color(0xFFFFB59E), Color(0xFFB9A8F0), Color(0xFF3B2A4A)),
    NIGHT("İyi geceler", "🌙", Color(0xFF2A2556), Color(0xFF4A3F85), Color(0xFFF1ECFF));

    companion object {
        fun of(time: LocalTime): DayPart = when (time.hour) {
            in 5..10 -> MORNING
            in 11..16 -> DAY
            in 17..20 -> EVENING
            else -> NIGHT
        }
    }
}

/**
 * Listenin en üstündeki karşılama kartı: saate göre değişen selam, tarih ve
 * hafifçe süzülen bir emoji. Ağ gerektirmez, telefonun saatini kullanır.
 */
@Composable
fun HeroCard(modifier: Modifier = Modifier, now: LocalTime = remember { LocalTime.now() }) {
    val part = DayPart.of(now)
    val date = remember { LocalDate.now().format(DATE_FORMAT) }

    // Emoji yukarı-aşağı çok hafif salınır; "canlı" hissi verir.
    val float by rememberInfiniteTransition(label = "hero").animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(tween(2400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "float"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.linearGradient(listOf(part.from, part.to)))
            .padding(horizontal = 20.dp, vertical = 22.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = date,
                style = MaterialTheme.typography.labelLarge,
                color = part.text.copy(alpha = 0.7f)
            )
            Text(
                text = part.greeting,
                style = MaterialTheme.typography.headlineMedium,
                color = part.text
            )
            Text(
                text = "Bugün hangi şehrin havasına bakalım?",
                style = MaterialTheme.typography.bodyMedium,
                color = part.text.copy(alpha = 0.85f)
            )
        }
        Text(
            text = part.emoji,
            style = MaterialTheme.typography.displayMedium,
            modifier = Modifier.graphicsLayer { translationY = float.dp.toPx() }
        )
    }
}

private val DATE_FORMAT: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMMM, EEEE", Locale.forLanguageTag("tr"))

@Preview(showBackground = true)
@Composable
private fun HeroCardPreview() {
    HavaTheme {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            HeroCard(now = LocalTime.of(8, 0))
            HeroCard(now = LocalTime.of(14, 0))
            HeroCard(now = LocalTime.of(18, 30))
            HeroCard(now = LocalTime.of(23, 0))
        }
    }
}
