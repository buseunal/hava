package com.kampplus.hava.ui.city

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kampplus.hava.data.TurkishProvinces
import com.kampplus.hava.model.City
import com.kampplus.hava.ui.theme.HavaTheme

@Composable
fun CityCard(city: City, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val plate = TurkishProvinces.plateOf(city)
    // Plaka yoksa (yurt dışı sonuç) addan türetilen sabit bir sayı kullanılır.
    val seed = plate?.toIntOrNull() ?: ((city.name.hashCode() and 0x7fffffff) % 81 + 1)
    val tint = Tints[seed % Tints.size]
    val texture = Texture.entries[seed % Texture.entries.size]

    val cardColor = tint.copy(alpha = 0.20f).compositeOver(scheme.surface.copy(alpha = 0.72f))

    // Dokunulunca kart hafifçe küçülür, bırakınca yay gibi geri döner.
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "press"
    )

    Card(
        onClick = onClick,
        interactionSource = interaction,
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, tint.copy(alpha = 0.45f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Türkiye illerinde plaka, diğerlerinde ülke kodu.
            PlateBadge(text = plate ?: city.countryCode ?: "?", tint = tint, texture = texture)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = city.name,
                    style = MaterialTheme.typography.titleMedium,
                    // Tek satıra sığmayan ad "..." ile kısaltılır, satır kaymaz.
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = city.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                // Ok yalnızca görseldir, "dokunulabilir" bilgisini Card zaten verir.
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Şehir başına dönüşümlü kullanılan soft pastel tonlar.
private val Tints = listOf(
    Color(0xFFFFB59E), // şeftali
    Color(0xFFB9A8F0), // lavanta
    Color(0xFF9FDCC0), // nane
    Color(0xFF9CC9F2), // gök mavisi
    Color(0xFFF2A7C3), // gül
    Color(0xFFF3D98B), // tereyağı
    Color(0xFFB5CFA0), // adaçayı
    Color(0xFFD7B5E8) // leylak
)

// Rozet üstündeki ince doku çeşitleri. 8 renk × 3 doku = 24 farklı görünüm.
private enum class Texture { DOTS, DIAGONAL, LINES }

@Composable
private fun PlateBadge(text: String, tint: Color, texture: Texture, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val isDarkSurface = scheme.surface.luminance() < 0.5f
    // Koyu temada pastel biraz kısılır ki açık renkli yazı okunaklı kalsın.
    val badgeColor = tint.copy(alpha = if (isDarkSurface) 0.38f else 0.65f).compositeOver(scheme.surface)
    val patternColor = scheme.onSurface.copy(alpha = 0.12f)

    Surface(
        modifier = modifier.size(44.dp),
        shape = RoundedCornerShape(12.dp),
        color = badgeColor,
        contentColor = scheme.onSurface
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    val step = 7.dp.toPx()
                    val stroke = 1.dp.toPx()
                    when (texture) {
                        Texture.DOTS -> {
                            var y = step / 2
                            while (y < size.height) {
                                var x = step / 2
                                while (x < size.width) {
                                    drawCircle(patternColor, 1.2.dp.toPx(), Offset(x, y))
                                    x += step
                                }
                                y += step
                            }
                        }
                        Texture.DIAGONAL -> {
                            var x = -size.height
                            while (x < size.width) {
                                drawLine(patternColor, Offset(x, size.height), Offset(x + size.height, 0f), stroke)
                                x += step
                            }
                        }
                        Texture.LINES -> {
                            var y = step / 2
                            while (y < size.height) {
                                drawLine(patternColor, Offset(0f, y), Offset(size.width, y), stroke)
                                y += step
                            }
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CityCardPreview() {
    HavaTheme {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(12.dp)) {
            listOf("Adana", "Adıyaman", "Afyonkarahisar", "Ağrı").forEach { name ->
                CityCard(
                    city = City(
                        id = 1,
                        name = name,
                        latitude = 0.0,
                        longitude = 0.0,
                        countryCode = "TR",
                        country = "Türkiye",
                        admin1 = name
                    ),
                    onClick = {}
                )
            }
        }
    }
}
