package com.alarysai.alarysai.core.designsystem.theme

import androidx.compose.ui.graphics.Color

// Brand palette taken from the home mockup (dark navy with blue/violet glow and cyan accents).
val Navy950 = Color(0xFF060820)
val Navy900 = Color(0xFF0B0F2E)
val Navy800 = Color(0xFF141A45)
val Cyan400 = Color(0xFF38C8F8)
val Blue500 = Color(0xFF3B6CF6)
val Violet500 = Color(0xFF8B5CF6)
val Teal400 = Color(0xFF2DD4BF)
val TextPrimary = Color(0xFFF4F6FF)
val TextSecondary = Color(0xFFA9B3D6)

/** Accent per content card, cycled by position (blue, violet, teal, cyan, indigo). */
val CardAccents = listOf(
    Blue500,
    Violet500,
    Teal400,
    Cyan400,
    Color(0xFF6366F1),
)
