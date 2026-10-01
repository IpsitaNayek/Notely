package com.example.notely.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
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
 * Frosted-glass card supporting both click and long-click gestures.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    fillColor: Color = NotelyTheme.colors.glassFill,
    borderColor: Color = NotelyTheme.colors.glassBorder,
    shape: Shape = NotelyTheme.shapes.card,
    contentPadding: Dp = NotelyTheme.spacing.cardPadding,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = fillColor,
        border = BorderStroke(1.dp, borderColor),
    ) {
        val clickModifier = if (onClick != null || onLongClick != null) {
            Modifier.combinedClickable(
                onClick = { onClick?.invoke() },
                onLongClick = { onLongClick?.invoke() }
            )
        } else {
            Modifier
        }

        Box(
            modifier = Modifier
                .then(clickModifier)
                .padding(contentPadding)
        ) {
            content()
        }
    }
}
