package com.sydney.sydney.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.sydney.sydney.R

/*
 * Geist for interface text and Newsreader for display headings (both SIL Open Font License).
 * They are variable fonts, so the weight axis works on Android 8 and up. Older phones fall back
 * to the regular weight, which still reads fine.
 */
@OptIn(ExperimentalTextApi::class)
private fun variable(resId: Int, weight: Int) =
    Font(resId, FontWeight(weight), variationSettings = FontVariation.Settings(FontVariation.weight(weight)))

val Geist = FontFamily(
    variable(R.font.geist, 400),
    variable(R.font.geist, 500),
    variable(R.font.geist, 600),
)

val Newsreader = FontFamily(
    variable(R.font.newsreader, 400),
    variable(R.font.newsreader, 500),
)

private val Base = Typography()

private fun TextStyle.sans() = copy(fontFamily = Geist, letterSpacing = 0.sp)

private fun TextStyle.serif(tracking: Double) = copy(
    fontFamily = Newsreader,
    fontWeight = FontWeight.Medium,
    letterSpacing = tracking.sp,
    lineHeight = fontSize * 1.15f,
)

val Typography = Typography(
    displayLarge = Base.displayLarge.serif(-1.0),
    displayMedium = Base.displayMedium.serif(-0.8),
    displaySmall = Base.displaySmall.serif(-0.6),
    headlineLarge = Base.headlineLarge.serif(-0.6),
    headlineMedium = Base.headlineMedium.serif(-0.5),
    headlineSmall = Base.headlineSmall.serif(-0.3),
    titleLarge = Base.titleLarge.serif(-0.2),
    titleMedium = Base.titleMedium.sans().copy(fontWeight = FontWeight.Medium),
    titleSmall = Base.titleSmall.sans().copy(fontWeight = FontWeight.Medium),
    bodyLarge = Base.bodyLarge.sans().copy(lineHeight = 24.sp),
    bodyMedium = Base.bodyMedium.sans().copy(lineHeight = 21.sp),
    bodySmall = Base.bodySmall.sans(),
    labelLarge = Base.labelLarge.sans().copy(fontWeight = FontWeight.Medium),
    labelMedium = Base.labelMedium.sans().copy(fontWeight = FontWeight.Medium),
    labelSmall = Base.labelSmall.sans().copy(fontWeight = FontWeight.Medium),
)
