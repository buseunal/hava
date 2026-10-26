package com.kampplus.hava.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun Modifier.softAppear(index: Int): Modifier {
    var played by rememberSaveable { mutableStateOf(index >= ANIMATED_COUNT) }
    val progress = remember { Animatable(if (played) 1f else 0f) }

    LaunchedEffect(Unit) {
        if (!played) {
            delay(index * STAGGER_MS)
            progress.animateTo(1f, tween(durationMillis = 380, easing = FastOutSlowInEasing))
            played = true
        }
    }

    return graphicsLayer {
        alpha = progress.value
        translationY = (1f - progress.value) * 24.dp.toPx()
    }
}

private const val ANIMATED_COUNT = 10
private const val STAGGER_MS = 45L
