package com.ncs.kootopia.ui.theme

import androidx.compose.ui.graphics.Color

// Legacy colors (can remove these if not needed)
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// Your custom Kootopia colors
object KootopiaColors {
    val primaryDark = Color(0xFF1C1C1C)        // Main background
    val surfaceDark = Color(0xFF2D2D2D)        // Cards, elevated surfaces
    val textPrimary = Color(0xFFFFFFFF)        // Primary text on dark
    val textSecondary = Color(0xFF888888)      // Line numbers, secondary text
    val accentBlue = Color(0xFF007ACC)         // Buttons, highlights, active states
    val successGreen = Color(0xFF00CC66)       // Compilation success
    val errorRed = Color(0xFFFF4444)           // Error highlights, failures
}