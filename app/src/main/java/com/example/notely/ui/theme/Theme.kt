package com.example.notely.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

object NotelyTheme {
    val colors: NotelyColors
        @Composable
        get() = LocalNotelyColors.current

    val typography: NotelyTypography
        @Composable
        get() = LocalNotelyTypography.current

    val shapes: NotelyShapes
        @Composable
        get() = LocalNotelyShapes.current

    val spacing: NotelySpacing
        @Composable
        get() = LocalNotelySpacing.current
}

@Composable
fun NotelyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkNotelyColors else LightNotelyColors
    val shapes = NotelyShapesDefaults
    val spacing = NotelySpacingDefaults
    val typography = NotelyTypographyDefaults

    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = colors.accent,
            onPrimary = colors.onAccent,
            background = colors.bgMid,
            surface = colors.dock,
            onSurface = colors.onDock,
        )
    } else {
        lightColorScheme(
            primary = colors.accent,
            onPrimary = colors.onAccent,
            background = colors.bgMid,
            surface = colors.dock,
            onSurface = colors.onDock,
        )
    }

    CompositionLocalProvider(
        LocalNotelyColors provides colors,
        LocalNotelyShapes provides shapes,
        LocalNotelySpacing provides spacing,
        LocalNotelyTypography provides typography,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content,
        )
    }
}