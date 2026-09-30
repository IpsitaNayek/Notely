package com.example.notely.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.notely.ui.notes.DockItem
import com.example.notely.ui.theme.NotelyTheme

/**
 * Bottom dock bar with 4 items:
 * - Home
 * - Search
 * - Audio Note (quick recording action)
 * - Settings
 *
 * (Trash icon has been removed per requirements; Bin is a category tab)
 */
@Composable
fun BottomDock(
    selectedItem: DockItem,
    onItemSelected: (DockItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = NotelyTheme.colors
    val shapes = NotelyTheme.shapes
    val spacing = NotelyTheme.spacing

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.screenHorizontalPadding)
            .height(spacing.dockHeight)
            .clip(shapes.pill)
            .background(colors.dock),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DockItem.entries.forEach { item ->
                val isSelected = item == selectedItem
                val icon = when (item) {
                    DockItem.HOME -> Icons.Filled.Home
                    DockItem.SEARCH -> Icons.Filled.Search
                    DockItem.AUDIO_NOTE -> Icons.Filled.Mic
                    DockItem.SETTINGS -> Icons.Filled.Settings
                }

                Box(
                    modifier = Modifier
                        .clip(shapes.dockSelectedItem)
                        .then(
                            if (isSelected) {
                                Modifier.background(colors.dockSelectedItem)
                            } else {
                                Modifier
                            }
                        )
                        .clickable { onItemSelected(item) }
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = item.label,
                        modifier = Modifier.size(24.dp),
                        tint = if (isSelected) {
                            if (colors.isDark) colors.textPrimary else colors.onDock
                        } else {
                            colors.onDock.copy(alpha = 0.5f)
                        },
                    )
                }
            }
        }
    }
}
