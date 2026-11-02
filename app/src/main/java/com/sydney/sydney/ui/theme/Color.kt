package com.sydney.sydney.ui.theme

import androidx.compose.ui.graphics.Color

/*
 * Three colour sets, split 60 / 30 / 10.
 *
 *  60  Canvas  The warm neutral every screen sits on.
 *  30  Ink     Text, cards, hairlines and the primary button. Ink is charcoal in light mode
 *              and bone in dark mode, so buttons always contrast with the canvas.
 *  10  Accent  Emerald, kept from the original ColFind green. Used only for the things that
 *              deserve attention: saved hearts, prices, links, icons and focus.
 */

// 60: canvas
val Bone = Color(0xFFF7F6F3)
val Night = Color(0xFF111412)

// 30: ink and surfaces
val Ink = Color(0xFF2B2F31)
val InkMuted = Color(0xFF787774)
val Paper = Color(0xFFFFFFFF)
val Hairline = Color(0xFFE6E3DD)
val InputLine = Color(0xFFCFCBC3)
val Wash = Color(0xFFEFEDE8)

val Stone = Color(0xFF1A1F1C)
val StoneText = Color(0xFFECEAE4)
val StoneMuted = Color(0xFF9A9A94)
val StoneHairline = Color(0xFF2A312D)
val StoneInput = Color(0xFF454B47)
val StoneWash = Color(0xFF222823)

// 10: accent
val Emerald = Color(0xFF1F7A55)
val EmeraldTint = Color(0xFFE3F0E9)
val Mint = Color(0xFF6FE0A8)
val MintTint = Color(0xFF1E3A2E)

val ErrorLight = Color(0xFFB3261E)
val ErrorDark = Color(0xFFF2B8B5)
