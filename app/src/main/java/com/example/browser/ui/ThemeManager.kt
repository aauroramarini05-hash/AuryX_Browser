package com.example.browser.ui

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

enum class AuryxTheme(val displayName: String, val isDark: Boolean) {
    LIGHT("Classic Light", false),
    DARK("Auryx Dark", true),
    COSMIC("Cosmic Purple", true),
    CYBERPUNK("Cyberpunk Neon", true),
    FOREST("Forest Mint", true)
}

object ThemeManager {

    val LightScheme = lightColorScheme(
        primary = Color(0xFF0D6EFD),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFE7F1FF),
        onPrimaryContainer = Color(0xFF001D47),
        secondary = Color(0xFF6C757D),
        onSecondary = Color.White,
        background = Color(0xFFF8F9FA),
        onBackground = Color(0xFF212529),
        surface = Color.White,
        onSurface = Color(0xFF212529),
        surfaceVariant = Color(0xFFE9ECEF),
        onSurfaceVariant = Color(0xFF495057),
        outline = Color(0xFFCED4DA)
    )

    val DarkScheme = darkColorScheme(
        primary = Color(0xFF4285F4),
        onPrimary = Color.White,
        primaryContainer = Color(0xFF1A3B8B),
        onPrimaryContainer = Color(0xFFD6E2FF),
        secondary = Color(0xFF8AB4F8),
        onSecondary = Color(0xFF003063),
        background = Color(0xFF131314),
        onBackground = Color(0xFFE3E3E3),
        surface = Color(0xFF1F1F20),
        onSurface = Color(0xFFE3E3E3),
        surfaceVariant = Color(0xFF2D2E30),
        onSurfaceVariant = Color(0xFFC4C7C5),
        outline = Color(0xFF8E918F)
    )

    val CosmicScheme = darkColorScheme(
        primary = Color(0xFFB388FF),
        onPrimary = Color(0xFF2A0066),
        primaryContainer = Color(0xFF3F0099),
        onPrimaryContainer = Color(0xFFEADEFF),
        secondary = Color(0xFFFF80AB),
        onSecondary = Color(0xFF4F0025),
        background = Color(0xFF0F0C1B),
        onBackground = Color(0xFFEDE8F5),
        surface = Color(0xFF1B142E),
        onSurface = Color(0xFFEDE8F5),
        surfaceVariant = Color(0xFF2C2248),
        onSurfaceVariant = Color(0xFFDBD1EA),
        outline = Color(0xFF7B5C97)
    )

    val CyberpunkScheme = darkColorScheme(
        primary = Color(0xFF00E5FF),
        onPrimary = Color(0xFF00363D),
        primaryContainer = Color(0xFF004D56),
        onPrimaryContainer = Color(0xFFB2F5FF),
        secondary = Color(0xFFFF007F),
        onSecondary = Color(0xFF45001E),
        background = Color(0xFF0A0012),
        onBackground = Color(0xFFE5FFFA),
        surface = Color(0xFF120224),
        onSurface = Color(0xFFE5FFFA),
        surfaceVariant = Color(0xFF29054A),
        onSurfaceVariant = Color(0xFFD3FFFA),
        outline = Color(0xFF0091A8)
    )

    val ForestScheme = darkColorScheme(
        primary = Color(0xFF00E676),
        onPrimary = Color(0xFF00391A),
        primaryContainer = Color(0xFF005228),
        onPrimaryContainer = Color(0xFFB9FFC9),
        secondary = Color(0xFF81C784),
        onSecondary = Color(0xFF1B4D24),
        background = Color(0xFF0D1B13),
        onBackground = Color(0xFFE2EFE5),
        surface = Color(0xFF16291E),
        onSurface = Color(0xFFE2EFE5),
        surfaceVariant = Color(0xFF233B2C),
        onSurfaceVariant = Color(0xFFC7DBD0),
        outline = Color(0xFF3E5A47)
    )

    fun getColorScheme(theme: AuryxTheme): ColorScheme {
        return when (theme) {
            AuryxTheme.LIGHT -> LightScheme
            AuryxTheme.DARK -> DarkScheme
            AuryxTheme.COSMIC -> CosmicScheme
            AuryxTheme.CYBERPUNK -> CyberpunkScheme
            AuryxTheme.FOREST -> ForestScheme
        }
    }
}
