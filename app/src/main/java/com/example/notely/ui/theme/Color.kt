package com.example.notely.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Notely color tokens — §10.3 and §10.4.
 */
@Immutable
data class NotelyColors(
    val isDark: Boolean,
    val bgTop: Color,
    val bgMid: Color,
    val bgBottom: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val glassFill: Color,
    val glassBorder: Color,
    val accent: Color,
    val onAccent: Color,
    val featuredCard: Color,
    val onFeaturedCard: Color,
    val onFeaturedCardSecondary: Color,
    val dock: Color,
    val onDock: Color,
    val dockSelectedItem: Color,
    val fabContainer: Color,
    val fabIcon: Color,
    val danger: Color,
    val noteColors: List<Color>,
)

val DarkNotelyColors = NotelyColors(
    isDark = true,
    bgTop = Color(0xFF6B5B54),
    bgMid = Color(0xFF5A5654),
    bgBottom = Color(0xFF4E5A5E),
    textPrimary = Color(0xFFFFFFFF),
    textSecondary = Color(0xB8FFFFFF), // white 72%
    textTertiary = Color(0x80FFFFFF),  // white 50%
    glassFill = Color(0x1AFFFFFF),     // white 10%
    glassBorder = Color(0x29FFFFFF),   // white 16%
    accent = Color(0xFFD9513A),        // coral
    onAccent = Color(0xFFFFFFFF),
    featuredCard = Color(0xFFF6F3EF),
    onFeaturedCard = Color(0xFF1B1917),
    onFeaturedCardSecondary = Color(0xFF6F6A66),
    dock = Color(0xFFF6F3EF),
    onDock = Color(0xFF1B1917),
    dockSelectedItem = Color(0xFF26221F),
    fabContainer = Color(0xFF26221F),
    fabIcon = Color(0xFFD9513A),
    danger = Color(0xFFF0776B),
    noteColors = listOf(
        Color(0xFFA5684F), // 0: Terracotta
        Color(0xFF6F7C5F), // 1: Sage
        Color(0xFF5F7A86), // 2: Dusty blue
        Color(0xFF8A7654), // 3: Sand
        Color(0xFFA06A6A), // 4: Clay rose
        Color(0xFF7D6577), // 5: Plum taupe
    ),
)

val LightNotelyColors = NotelyColors(
    isDark = false,
    bgTop = Color(0xFFF1E9E3),
    bgMid = Color(0xFFE9E5E1),
    bgBottom = Color(0xFFDDE4E6),
    textPrimary = Color(0xFF1F1B19),
    textSecondary = Color(0xA81F1B19), // 66%
    textTertiary = Color(0x731F1B19),  // 45%
    glassFill = Color(0x0D000000),     // black 5%
    glassBorder = Color(0x14000000),   // black 8%
    accent = Color(0xFFD9513A),
    onAccent = Color(0xFFFFFFFF),
    featuredCard = Color(0xFFFFFFFF),
    onFeaturedCard = Color(0xFF1B1917),
    onFeaturedCardSecondary = Color(0xFF6F6A66),
    dock = Color(0xFF26221F),
    onDock = Color(0xFFFFFFFF),
    dockSelectedItem = Color(0x29FFFFFF), // white 16%
    fabContainer = Color(0xFF26221F),
    fabIcon = Color(0xFFD9513A),
    danger = Color(0xFFC8402F),
    noteColors = listOf(
        Color(0xFFA5684F), // 0: Terracotta
        Color(0xFF6F7C5F), // 1: Sage
        Color(0xFF5F7A86), // 2: Dusty blue
        Color(0xFF8A7654), // 3: Sand
        Color(0xFFA06A6A), // 4: Clay rose
        Color(0xFF7D6577), // 5: Plum taupe
    ),
)

val LocalNotelyColors = staticCompositionLocalOf { DarkNotelyColors }