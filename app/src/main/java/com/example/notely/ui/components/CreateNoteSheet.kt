package com.example.notely.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.notely.ui.theme.NotelyTheme

/**
 * Bottom creation sheet with close button, tap-outside-to-dismiss scrim, and back-press handling.
 */
@Composable
fun CreateNoteSheet(
    onNewNormalNote: () -> Unit,
    onNewTodoNote: () -> Unit,
    onNewAudioNote: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = NotelyTheme.colors
    val shapes = NotelyTheme.shapes
    val spacing = NotelyTheme.spacing

    // Intercept hardware/gesture Back button to dismiss this panel first
    BackHandler(onBack = onDismiss)

    // Full screen overlay with dim scrim
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss,
            ),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { /* absorb clicks */ },
                )
                .navigationBarsPadding(),
            shape = shapes.bottomSheet,
            color = colors.bgMid.copy(alpha = 0.98f),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(spacing.screenHorizontalPadding)
                    .padding(vertical = 20.dp),
            ) {
                // Header with title and visible close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Create",
                        style = NotelyTheme.typography.cardTitleFeatured,
                        color = colors.textPrimary,
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = colors.textSecondary,
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                CreateOptionItem(
                    icon = Icons.AutoMirrored.Filled.Notes,
                    title = "New Note",
                    subtitle = "Regular text note with formatting",
                    onClick = {
                        onDismiss()
                        onNewNormalNote()
                    }
                )

                Spacer(Modifier.height(8.dp))

                CreateOptionItem(
                    icon = Icons.Filled.CheckBox,
                    title = "New To-do",
                    subtitle = "Checklist and task items",
                    onClick = {
                        onDismiss()
                        onNewTodoNote()
                    }
                )

                Spacer(Modifier.height(8.dp))

                CreateOptionItem(
                    icon = Icons.Filled.Mic,
                    title = "Audio Note",
                    subtitle = "Record audio memo or voice note",
                    onClick = {
                        onDismiss()
                        onNewAudioNote()
                    }
                )
            }
        }
    }
}

@Composable
private fun CreateOptionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = NotelyTheme.colors
    val shapes = NotelyTheme.shapes

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.size(44.dp),
            shape = shapes.card,
            color = colors.glassFill,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = colors.accent,
                modifier = Modifier
                    .padding(10.dp)
                    .size(24.dp)
            )
        }

        Spacer(Modifier.width(16.dp))

        Column {
            Text(
                text = title,
                style = NotelyTheme.typography.cardTitle,
                color = colors.textPrimary,
            )
            Text(
                text = subtitle,
                style = NotelyTheme.typography.meta,
                color = colors.textSecondary,
            )
        }
    }
}
