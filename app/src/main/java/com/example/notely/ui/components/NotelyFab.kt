package com.example.notely.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.notely.ui.theme.NotelyTheme

/**
 * Custom FAB — §6.
 *
 * 64 dp rounded-square with dark container and coral "+" icon.
 * Positioned above the dock with a 12 dp gap (handled by the parent layout).
 */
@Composable
fun NotelyFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = NotelyTheme.colors

    Surface(
        onClick = onClick,
        modifier = modifier.size(NotelyTheme.spacing.fabSize),
        shape = RoundedCornerShape(20.dp),
        color = colors.fabContainer,
        shadowElevation = 8.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "New note",
                tint = colors.fabIcon,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}
