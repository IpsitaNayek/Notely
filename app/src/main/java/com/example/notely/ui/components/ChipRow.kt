package com.example.notely.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.notely.ui.notes.NoteChip
import com.example.notely.ui.theme.NotelyTheme

/**
 * Horizontally scrollable row of filter chips — §5.
 *
 * "All" is always first. Color chips can optionally show a small color dot.
 */
@Composable
fun ChipRow(
    chips: List<NoteChip> = NoteChip.entries,
    selectedChip: NoteChip,
    onChipSelected: (NoteChip) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = NotelyTheme.colors
    val spacing = NotelyTheme.spacing

    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(spacing.chipGap),
    ) {
        chips.forEach { chip ->
            val isSelected = chip == selectedChip

            FilterChip(
                selected = isSelected,
                onClick = { onChipSelected(chip) },
                label = {
                    Text(
                        text = chip.label,
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
