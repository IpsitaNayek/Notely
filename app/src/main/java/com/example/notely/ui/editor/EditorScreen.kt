package com.example.notely.ui.editor

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.notely.ui.theme.NotelyBackground
import com.example.notely.ui.theme.NotelyTheme

/**
 * Full-screen note editor — §8.
 *
 * Layout:
 *  1. Gradient background tinted by selected note colour (via [NotelyBackground])
 *  2. Top toolbar: Back ← | spacer | Pin | Colour | Trash
 *  3. Scrollable title + body text fields (BasicTextField, no decoration)
 *  4. Animated bottom colour picker sheet
 *
 * Auto-saves on back press if note has content.
 */
@Composable
fun EditorScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditorViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = NotelyTheme.colors
    val noteColor = colors.noteColors.getOrElse(uiState.colorId) { colors.noteColors[0] }

    // Navigate back after trash action
    LaunchedEffect(uiState.isTrashed) {
        if (uiState.isTrashed && !uiState.isLoading) onBack()
    }

    // Auto-save on system back
    BackHandler {
        viewModel.save()
        onBack()
    }

    NotelyBackground(overrideColor = noteColor) {
        Box(modifier = modifier.fillMaxSize()) {

            // ── Scrollable content ──
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(
                        horizontal = NotelyTheme.spacing.screenHorizontalPadding,
                        vertical = 8.dp,
                    ),
            ) {
                // Toolbar
                EditorToolbar(
                    isPinned = uiState.isPinned,
                    onBack = {
                        viewModel.save()
                        onBack()
                    },
                    onTogglePin = viewModel::onTogglePin,
                    onOpenColorPicker = viewModel::onToggleColorPicker,
                    onTrash = viewModel::onShowDeleteConfirm,
                    selectedColorId = uiState.colorId,
                    noteColors = colors.noteColors,
                )

                Spacer(Modifier.height(NotelyTheme.spacing.headerSpacing))

                // Title field
                BasicTextField(
                    value = uiState.title,
                    onValueChange = viewModel::onTitleChanged,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = NotelyTheme.typography.editorTitle.copy(
                        color = colors.textPrimary,
                    ),
                    cursorBrush = SolidColor(colors.accent),
                    decorationBox = { inner ->
                        Box {
                            if (uiState.title.isEmpty()) {
                                Text(
                                    text = "Title",
                                    style = NotelyTheme.typography.editorTitle,
                                    color = colors.textTertiary,
                                )
                            }
                            inner()
                        }
                    },
                )

                Spacer(Modifier.height(16.dp))

                // Body field — grows with content
                BasicTextField(
                    value = uiState.body,
                    onValueChange = viewModel::onBodyChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(bottom = 120.dp), // room for colour picker sheet
                    textStyle = NotelyTheme.typography.body.copy(
                        color = colors.textPrimary,
                    ),
                    cursorBrush = SolidColor(colors.accent),
                    decorationBox = { inner ->
                        Box {
                            if (uiState.body.isEmpty()) {
                                Text(
                                    text = "Start writing…",
                                    style = NotelyTheme.typography.body,
                                    color = colors.textTertiary,
                                )
                            }
                            inner()
                        }
                    },
                )
            }

            // ── Colour picker bottom sheet ──
            AnimatedVisibility(
                visible = uiState.showColorPicker,
                modifier = Modifier.align(Alignment.BottomCenter),
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it },
            ) {
                ColorPickerSheet(
                    noteColors = colors.noteColors,
                    selectedColorId = uiState.colorId,
                    onColorSelected = viewModel::onColorSelected,
                    onDismiss = viewModel::onDismissColorPicker,
                )
            }
        }
    }

    // ── Trash confirmation dialog ──
    if (uiState.showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = viewModel::onDismissDeleteConfirm,
            title = { Text("Move to trash?", color = colors.textPrimary) },
            text = {
                Text(
                    "This note will be moved to the trash.",
                    color = colors.textSecondary,
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::onMoveToTrash) {
                    Text("Move to trash", color = colors.danger)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onDismissDeleteConfirm) {
                    Text("Cancel", color = colors.textSecondary)
                }
            },
            containerColor = colors.bgMid,
            shape = NotelyTheme.shapes.menu,
        )
    }
}

// ── Toolbar ───────────────────────────────────────────────────────────────

@Composable
private fun EditorToolbar(
    isPinned: Boolean,
    onBack: () -> Unit,
    onTogglePin: () -> Unit,
    onOpenColorPicker: () -> Unit,
    onTrash: () -> Unit,
    selectedColorId: Int,
    noteColors: List<Color>,
    modifier: Modifier = Modifier,
) {
    val colors = NotelyTheme.colors

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Back
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = colors.textPrimary,
            )
        }

        Spacer(Modifier.weight(1f))

        // Pin
        IconButton(onClick = onTogglePin) {
            Icon(
                imageVector = Icons.Filled.PushPin,
                contentDescription = if (isPinned) "Unpin" else "Pin",
                tint = if (isPinned) colors.accent else colors.textSecondary,
            )
        }

        // Colour dot — opens picker
        Box(
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .size(28.dp)
                .clip(CircleShape)
                .background(noteColors.getOrElse(selectedColorId) { noteColors[0] })
                .border(2.dp, colors.glassBorder, CircleShape)
                .clickable(onClick = onOpenColorPicker),
        )

        // Trash
        IconButton(onClick = onTrash) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = "Move to trash",
                tint = colors.textSecondary,
            )
        }
    }
}

// ── Colour picker ─────────────────────────────────────────────────────────

@Composable
private fun ColorPickerSheet(
    noteColors: List<Color>,
    selectedColorId: Int,
    onColorSelected: (Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = NotelyTheme.colors
    val spacing = NotelyTheme.spacing

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        shape = NotelyTheme.shapes.bottomSheet,
        color = colors.bgMid.copy(alpha = 0.96f),
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = spacing.screenHorizontalPadding,
                vertical = 20.dp,
            ),
        ) {
            Text(
                text = "Note colour",
                style = NotelyTheme.typography.label,
                color = colors.textSecondary,
                modifier = Modifier.padding(bottom = 16.dp),
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                noteColors.forEachIndexed { index, color ->
                    val isSelected = index == selectedColorId
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(color)
                            .then(
                                if (isSelected) {
                                    Modifier.border(3.dp, colors.textPrimary, CircleShape)
                                } else {
                                    Modifier.border(1.5.dp, colors.glassBorder, CircleShape)
                                }
                            )
                            .clickable { onColorSelected(index) },
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            TextButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.End),
            ) {
                Text(
                    text = "Done",
                    style = NotelyTheme.typography.label,
                    color = colors.accent,
                )
            }
        }
    }
}
