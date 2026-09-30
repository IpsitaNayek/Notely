package com.example.notely.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.notely.ui.theme.NotelyTheme

/**
 * Frosted-glass card — §10.7.
 *
 * A semi-transparent surface with a thin border, matching the Notely glass aesthetic.
 * Used as the base for note cards, search bar, and chips.
 *
 * @param fillColor Override the glass fill (e.g. note color). Defaults to [NotelyColors.glassFill].
 * @param borderColor Override the border color. Defaults to [NotelyColors.glassBorder].
 * @param shape Card shape. Defaults to [NotelyShapes.card].
 * @param contentPadding Inner padding. Defaults to [NotelySpacing.cardPadding].
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    fillColor: Color = NotelyTheme.colors.glassFill,
    borderColor: Color = NotelyTheme.colors.glassBorder,
    shape: Shape = NotelyTheme.shapes.card,
    contentPadding: Dp = NotelyTheme.spacing.cardPadding,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = fillColor,
        border = BorderStroke(1.dp, borderColor),
        onClick = onClick ?: {},
        enabled = onClick != null,
    ) {
        Box(modifier = Modifier.padding(contentPadding)) {
            content()
        }
    }
}
