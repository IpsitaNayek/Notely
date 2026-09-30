package com.example.notely.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.notely.ui.theme.NotelyTheme

/**
 * Glass-style search bar — §5.
 *
 * Pill-shaped text field with a search icon and optional clear button.
 */
@Composable
fun SearchBar(
    query: String,
    onQueryChanged: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = NotelyTheme.colors

    TextField(
        value = query,
        onValueChange = onQueryChanged,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        placeholder = {
            Text(
                text = "Search notes…",
                style = NotelyTheme.typography.label,
                color = colors.textTertiary,
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "Search",
                tint = colors.textTertiary,
                modifier = Modifier.size(20.dp),
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClear) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Clear search",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        },
        textStyle = NotelyTheme.typography.label.copy(color = colors.textPrimary),
        singleLine = true,
        shape = NotelyTheme.shapes.pill,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = colors.glassFill,
            unfocusedContainerColor = colors.glassFill,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = colors.accent,
        ),
    )
}
