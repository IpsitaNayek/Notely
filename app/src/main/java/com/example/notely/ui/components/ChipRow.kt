package com.example.notely.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.notely.ui.notes.NoteCategory
import com.example.notely.ui.theme.NotelyTheme

/**
 * Horizontally scrollable row of meaningful category chips:
 * All | Pinned | To-do | Notes | Audio Notes | Bin
 */
@Composable
fun ChipRow(
    categories: List<NoteCategory> = NoteCategory.entries,
    selectedCategory: NoteCategory,
    onCategorySelected: (NoteCategory) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = NotelyTheme.colors
    val spacing = NotelyTheme.spacing

    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(spacing.chipGap),
    ) {
        categories.forEach { category ->
            val isSelected = category == selectedCategory

            FilterChip(
                selected = isSelected,
                onClick = { onCategorySelected(category) },
                label = {
                    Text(
                        text = category.label,
                        style = NotelyTheme.typography.label,
                    )
                },
                modifier = Modifier.height(spacing.chipHeight),
                shape = NotelyTheme.shapes.pill,
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = colors.glassFill,
                    labelColor = colors.textSecondary,
                    selectedContainerColor = colors.textPrimary.copy(alpha = 0.15f),
                    selectedLabelColor = colors.textPrimary,
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = colors.glassBorder,
                    selectedBorderColor = colors.textPrimary.copy(alpha = 0.3f),
                    enabled = true,
                    selected = isSelected,
                ),
            )
        }
    }
}
