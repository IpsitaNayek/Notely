package com.example.notely.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.notely.data.local.NoteEntity
import com.example.notely.ui.theme.NotelyTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Standard note card for the staggered grid.
 *
 * Shows title (max 2 lines), body preview (max 4 lines), and an "Edited" timestamp.
 * Background is the note's assigned color from [NotelyColors.noteColors].
 */
@Composable
fun NoteCard(
    note: NoteEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = NotelyTheme.colors
    val noteColor = colors.noteColors.getOrElse(note.colorId) { colors.noteColors[0] }

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        fillColor = noteColor,
        borderColor = noteColor.copy(alpha = 0.3f),
        onClick = onClick,
    ) {
        Column {
            if (note.title.isNotBlank()) {
                Text(
                    text = note.title,
                    style = NotelyTheme.typography.cardTitle,
                    color = colors.textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            if (note.body.isNotBlank()) {
                Text(
                    text = note.body,
                    style = NotelyTheme.typography.preview,
                    color = colors.textSecondary,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            val formattedDate = remember(note.updatedAt) {
                formatTimestamp(note.updatedAt)
            }
            Text(
                text = "Edited $formattedDate",
                style = NotelyTheme.typography.meta,
                color = colors.textTertiary,
            )
        }
    }
}

/**
 * Featured note card — larger, uses [NotelyColors.featuredCard] background.
 *
 * Spans full width. Shows title (max 2 lines) and a longer body preview (max 6 lines).
 */
@Composable
fun FeaturedNoteCard(
    note: NoteEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = NotelyTheme.colors

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        fillColor = colors.featuredCard,
        borderColor = colors.glassBorder,
        onClick = onClick,
    ) {
        Column {
            if (note.title.isNotBlank()) {
                Text(
                    text = note.title,
                    style = NotelyTheme.typography.cardTitleFeatured,
                    color = colors.onFeaturedCard,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (note.body.isNotBlank()) {
                Text(
                    text = note.body,
                    style = NotelyTheme.typography.preview,
                    color = colors.onFeaturedCardSecondary,
                    maxLines = 6,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            val formattedDate = remember(note.updatedAt) {
                formatTimestamp(note.updatedAt)
            }
            Text(
                text = "Edited $formattedDate",
                style = NotelyTheme.typography.meta,
                color = colors.onFeaturedCardSecondary.copy(alpha = 0.6f),
            )
        }
    }
}

/** Format epoch millis to a user-friendly relative or absolute time string. */
private fun formatTimestamp(millis: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - millis

    return when {
        diff < 60_000 -> "just now"
        diff < 3_600_000 -> "${diff / 60_000}m ago"
        diff < 86_400_000 -> "${diff / 3_600_000}h ago"
        diff < 172_800_000 -> "yesterday"
        else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(millis))
    }
}
