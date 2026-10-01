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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.RestoreFromTrash
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.notely.data.local.NoteEntity
import com.example.notely.ui.theme.NotelyTheme

@Composable
fun NoteActionSheet(
    note: NoteEntity,
    isInBin: Boolean,
    onTogglePin: () -> Unit,
    onMoveToBin: () -> Unit,
    onRestore: () -> Unit,
    onDeletePermanently: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = NotelyTheme.colors
    val shapes = NotelyTheme.shapes
    val spacing = NotelyTheme.spacing

    BackHandler(onBack = onDismiss)

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
                    onClick = { /* absorb */ },
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
                // Note preview header with close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = note.title.ifBlank { "Untitled Note" },
                            style = NotelyTheme.typography.cardTitle,
                            color = colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (note.body.isNotBlank()) {
                            Text(
                                text = note.body,
                                style = NotelyTheme.typography.meta,
                                color = colors.textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(top = 2.dp),
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = colors.textSecondary,
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                if (!isInBin) {
                    // Active note actions
                    ActionItem(
                        icon = Icons.Filled.PushPin,
                        title = if (note.isPinned) "Unpin note" else "Pin to top",
                        tint = if (note.isPinned) colors.accent else colors.textPrimary,
                        onClick = {
                            onTogglePin()
                            onDismiss()
                        }
                    )

                    ActionItem(
                        icon = Icons.Filled.Delete,
                        title = "Move to Bin",
                        tint = colors.danger,
                        onClick = {
                            onMoveToBin()
                            onDismiss()
                        }
                    )
                } else {
                    // Bin actions
                    ActionItem(
                        icon = Icons.Filled.RestoreFromTrash,
                        title = "Restore note",
                        tint = colors.textPrimary,
                        onClick = {
                            onRestore()
                            onDismiss()
                        }
                    )

                    ActionItem(
                        icon = Icons.Filled.DeleteForever,
                        title = "Delete permanently",
                        tint = colors.danger,
                        onClick = {
                            onDeletePermanently()
                            onDismiss()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionItem(
    icon: ImageVector,
    title: String,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shapes = NotelyTheme.shapes

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = shapes.card,
            color = tint.copy(alpha = 0.12f),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = tint,
                modifier = Modifier
                    .padding(8.dp)
                    .size(24.dp)
            )
        }

        Spacer(Modifier.width(16.dp))

        Text(
            text = title,
            style = NotelyTheme.typography.cardTitle,
            color = tint,
        )
    }
}
