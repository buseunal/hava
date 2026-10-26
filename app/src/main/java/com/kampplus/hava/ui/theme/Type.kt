package com.kampplus.hava.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.kampplus.hava.R

@OptIn(ExperimentalTextApi::class)
private fun nunito(weight: FontWeight) = Font(
    resId = R.font.nunito,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight))
)

private val HavaFont = FontFamily(
    nunito(FontWeight.Normal),
    nunito(FontWeight.Medium),
    nunito(FontWeight.SemiBold),
    nunito(FontWeight.Bold),
    nunito(FontWeight.ExtraBold)
)

private val base = Typography()

private fun TextStyle.rounded(weight: FontWeight? = null) = copy(fontFamily = HavaFont, fontWeight = weight ?: fontWeight)

internal val HavaTypography = Typography(
    displayLarge = base.displayLarge.rounded(),
    displayMedium = base.displayMedium.rounded(),
    displaySmall = base.displaySmall.rounded(FontWeight.SemiBold),
    headlineLarge = base.headlineLarge.rounded(FontWeight.Bold),
    headlineMedium = base.headlineMedium.rounded(FontWeight.Bold),
    headlineSmall = base.headlineSmall.rounded(FontWeight.SemiBold),
    titleLarge = base.titleLarge.rounded(FontWeight.SemiBold),
    titleMedium = base.titleMedium.rounded(FontWeight.SemiBold),
    titleSmall = base.titleSmall.rounded(FontWeight.SemiBold),
    bodyLarge = base.bodyLarge.rounded(),
    bodyMedium = base.bodyMedium.rounded(),
    bodySmall = base.bodySmall.rounded(),
    labelLarge = base.labelLarge.rounded(FontWeight.Bold),
    labelMedium = base.labelMedium.rounded(FontWeight.Medium),
    labelSmall = base.labelSmall.rounded(FontWeight.Medium)
)
