package com.example.notely.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.notely.data.local.NoteEntity
import com.example.notely.ui.theme.NotelyTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Standard note card for the staggered grid.
 *
 * Shows:
 * - Pin badge (if pinned)
 * - Title (max 2 lines)
 * - Image thumbnail (if attached)
 * - Audio badge / Checklist progress indicator
 * - Body preview (max 4 lines)
 * - Timestamp
 */
@Composable
fun NoteCard(
    note: NoteEntity,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val colors = NotelyTheme.colors
    val noteColor = colors.noteColors.getOrElse(note.colorId) { colors.noteColors[0] }

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        fillColor = noteColor,
        borderColor = noteColor.copy(alpha = 0.3f),
        onClick = onClick,
        onLongClick = onLongClick,
    ) {
        Column {
            // Pin indicator / Badges Row
            if (note.isPinned || note.isAudioNote() || note.isTodoNote()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val audioInfo = remember(note.audioPath, note.body) { note.getFirstAudio() }
                    if (audioInfo != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(colors.accent.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Mic,
                                contentDescription = "Audio",
                                tint = colors.accent,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            val durationSec = audioInfo.second
                            val mins = durationSec / 60
                            val secs = durationSec % 60
                            Text(
                                text = String.format(Locale.getDefault(), "%d:%02d", mins, secs),
                                style = NotelyTheme.typography.meta,
                                color = colors.accent,
                            )
                        }
                    } else if (note.isTodoNote()) {
                        val checklist = remember(note.checklistJson) { note.getChecklist() }
                        val checkedCount = checklist.count { it.isChecked }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(colors.textSecondary.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckBox,
                                contentDescription = "To-do",
                                tint = colors.textPrimary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "$checkedCount/${checklist.size}",
                                style = NotelyTheme.typography.meta,
                                color = colors.textPrimary,
                            )
                        }
                    } else {
                        Spacer(Modifier.width(1.dp))
                    }

                    if (note.isPinned) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(colors.accent.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PushPin,
                                contentDescription = "Pinned",
                                tint = colors.accent,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }

            // Attached image thumbnail
            val firstImage = remember(note.imageUri, note.body) { note.getFirstImageUri() }
            if (firstImage.isNotBlank()) {
                AsyncImage(
                    model = firstImage,
                    contentDescription = "Attached photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .padding(bottom = 6.dp)
                )
            }

            if (note.title.isNotBlank()) {
                Text(
                    text = note.title,
                    style = NotelyTheme.typography.cardTitle,
                    color = colors.textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Checklist preview or Body preview
            val cleanBody = remember(note.body) { note.getCleanBodyPreview() }
            if (note.isTodoNote() && note.checklistJson.isNotBlank()) {
                val items = remember(note.checklistJson) { note.getChecklist().take(3) }
                Column(modifier = Modifier.padding(bottom = 4.dp)) {
                    items.forEach { item ->
                        Text(
                            text = "${if (item.isChecked) "☑" else "☐"} ${item.text}",
                            style = NotelyTheme.typography.preview,
                            color = if (item.isChecked) colors.textTertiary else colors.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            } else if (cleanBody.isNotBlank()) {
                Text(
                    text = cleanBody,
                    style = NotelyTheme.typography.preview,
                    color = colors.textSecondary,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(6.dp))
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
 */
@Composable
fun FeaturedNoteCard(
    note: NoteEntity,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val colors = NotelyTheme.colors

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        fillColor = colors.featuredCard,
        borderColor = colors.glassBorder,
        onClick = onClick,
        onLongClick = onLongClick,
    ) {
        Column {
            // Badges Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val audioInfo = remember(note.audioPath, note.body) { note.getFirstAudio() }
                if (audioInfo != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(colors.accent.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Mic,
                            contentDescription = "Audio",
                            tint = colors.accent,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        val durationSec = audioInfo.second
                        val mins = durationSec / 60
                        val secs = durationSec % 60
                        Text(
                            text = String.format(Locale.getDefault(), "%d:%02d", mins, secs),
                            style = NotelyTheme.typography.meta,
                            color = colors.accent,
                        )
                    }
                } else if (note.isTodoNote()) {
                    val checklist = remember(note.checklistJson) { note.getChecklist() }
                    val checkedCount = checklist.count { it.isChecked }
                    Text(
                        text = "☑ $checkedCount of ${checklist.size} completed",
                        style = NotelyTheme.typography.meta,
                        color = colors.onFeaturedCardSecondary,
                    )
                } else {
                    Spacer(Modifier.width(1.dp))
                }

                if (note.isPinned) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(colors.accent.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PushPin,
                            contentDescription = "Pinned",
                            tint = colors.accent,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            val firstImage = remember(note.imageUri, note.body) { note.getFirstImageUri() }
            if (firstImage.isNotBlank()) {
                AsyncImage(
                    model = firstImage,
                    contentDescription = "Attached photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .padding(bottom = 8.dp)
                )
            }

            if (note.title.isNotBlank()) {
                Text(
                    text = note.title,
                    style = NotelyTheme.typography.cardTitleFeatured,
                    color = colors.onFeaturedCard,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            val cleanBody = remember(note.body) { note.getCleanBodyPreview() }
            if (cleanBody.isNotBlank()) {
                Text(
                    text = cleanBody,
                    style = NotelyTheme.typography.preview,
                    color = colors.onFeaturedCardSecondary,
                    maxLines = 5,
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
