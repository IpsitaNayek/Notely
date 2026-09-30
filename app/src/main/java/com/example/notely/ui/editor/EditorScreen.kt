package com.example.notely.ui.editor

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.notely.data.local.ChecklistItem
import com.example.notely.data.media.AudioPlayerManager
import com.example.notely.data.model.NoteContentBlock
import com.example.notely.ui.components.AudioRecorderDialog
import com.example.notely.ui.theme.NotelyBackground
import com.example.notely.ui.theme.NotelyTheme
import java.util.Locale

@Composable
fun EditorScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditorViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = NotelyTheme.colors
    val shapes = NotelyTheme.shapes
    val noteColor = colors.noteColors.getOrElse(uiState.colorId) { colors.noteColors[0] }

    var showAudioRecorder by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                viewModel.onInsertImageAtCursor(uri.toString())
            }
        }
    )

    // Ensure audio playback stops when leaving editor
    DisposableEffect(Unit) {
        onDispose {
            AudioPlayerManager.stopActivePlayer()
        }
    }

    // Navigate back after trash action
    LaunchedEffect(uiState.isTrashed) {
        if (uiState.isTrashed && !uiState.isLoading) onBack()
    }

    // Auto-save on system back and close overlays first
    BackHandler {
        when {
            uiState.fullscreenImageUri != null -> viewModel.onViewImage(null)
            uiState.showColorPicker -> viewModel.onDismissColorPicker()
            uiState.showDeleteConfirm -> viewModel.onDismissDeleteConfirm()
            showAudioRecorder -> showAudioRecorder = false
            else -> {
                AudioPlayerManager.stopActivePlayer()
                viewModel.save()
                onBack()
            }
        }
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
                // Top Toolbar
                EditorToolbar(
                    isPinned = uiState.isPinned,
                    onBack = {
                        AudioPlayerManager.stopActivePlayer()
                        viewModel.save()
                        onBack()
                    },
                    onTogglePin = viewModel::onTogglePin,
                    onOpenColorPicker = viewModel::onToggleColorPicker,
                    onTrash = viewModel::onShowDeleteConfirm,
                    selectedColorId = uiState.colorId,
                    noteColors = colors.noteColors,
                )

                Spacer(Modifier.height(8.dp))

                // Rich Formatting / Attachment Toolbar
                FormattingBar(
                    onInsertBullet = viewModel::onInsertBullet,
                    onInsertNumbering = viewModel::onInsertNumbering,
                    onAddCheckbox = viewModel::onAddChecklistItem,
                    onAddPhoto = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onRecordAudio = { showAudioRecorder = true },
                )

                Spacer(Modifier.height(16.dp))

                // Title field: ALWAYS stays at the very top!
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

                // Interactive Checklist Items
                if (uiState.checklist.isNotEmpty()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        uiState.checklist.forEach { item ->
                            ChecklistItemRow(
                                item = item,
                                onCheckedChange = { viewModel.onToggleChecklistItem(item.id) },
                                onTextChanged = { viewModel.onUpdateChecklistItem(item.id, it) },
                                onDelete = { viewModel.onRemoveChecklistItem(item.id) },
                            )
                        }

                        TextButton(
                            onClick = viewModel::onAddChecklistItem,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Add checkbox item",
                                tint = colors.accent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Add checkbox item",
                                style = NotelyTheme.typography.label,
                                color = colors.accent,
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }

                // ── Note Body: Sequential text & media blocks at cursor positions ──
                uiState.contentBlocks.forEachIndexed { index, block ->
                    when (block) {
                        is NoteContentBlock.Text -> {
                            var tfv by remember(block.id, block.content) {
                                mutableStateOf(TextFieldValue(block.content, TextRange(block.content.length)))
                            }

                            BasicTextField(
                                value = tfv,
                                onValueChange = { newTfv ->
                                    tfv = newTfv
                                    viewModel.onUpdateTextBlock(block.id, newTfv.text, newTfv.selection.start)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                textStyle = NotelyTheme.typography.body.copy(
                                    color = colors.textPrimary,
                                ),
                                cursorBrush = SolidColor(colors.accent),
                                decorationBox = { inner ->
                                    Box {
                                        if (tfv.text.isEmpty() && uiState.contentBlocks.size == 1 && uiState.checklist.isEmpty()) {
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

                        is NoteContentBlock.Image -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .height(220.dp)
                                    .clip(shapes.card)
                                    .clickable { viewModel.onViewImage(block.uri) }
                            ) {
                                AsyncImage(
                                    model = block.uri,
                                    contentDescription = "Inserted photo (tap to view)",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                IconButton(
                                    onClick = { viewModel.onRemoveBlock(block.id) },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(colors.fabContainer.copy(alpha = 0.85f))
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Remove photo",
                                        tint = colors.fabIcon,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        is NoteContentBlock.Audio -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                AudioPlayerCard(
                                    audioPath = block.path,
                                    durationSec = block.durationSec,
                                    onRemove = { viewModel.onRemoveBlock(block.id) },
                                )
                            }
                        }
                    }
                }

                Spacer(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .height(120.dp)
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

    // ── Fullscreen Image Viewer Dialog ──
    uiState.fullscreenImageUri?.let { uri ->
        Dialog(
            onDismissRequest = { viewModel.onViewImage(null) },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.92f))
            ) {
                AsyncImage(
                    model = uri,
                    contentDescription = "Full view photo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                )
                IconButton(
                    onClick = { viewModel.onViewImage(null) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .statusBarsPadding()
                        .padding(16.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                    )
                }
            }
        }
    }

    // ── Audio recording dialog inside editor ──
    if (showAudioRecorder) {
        AudioRecorderDialog(
            onDismiss = { showAudioRecorder = false },
            onSave = { _, file, duration ->
                viewModel.onInsertAudioAtCursor(file, duration)
                showAudioRecorder = false
            }
        )
    }

    // ── Trash confirmation dialog ──
    if (uiState.showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = viewModel::onDismissDeleteConfirm,
            title = { Text("Move to Bin?", color = colors.textPrimary) },
            text = {
                Text(
                    "This note will be moved to the Bin.",
                    color = colors.textSecondary,
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::onMoveToTrash) {
                    Text("Move to Bin", color = colors.danger)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onDismissDeleteConfirm) {
                    Text("Cancel", color = colors.textSecondary)
                }
            },
            containerColor = colors.bgMid,
            shape = shapes.menu,
        )
    }
}

// ── Formatting Bar ─────────────────────────────────────────────────────────

@Composable
private fun FormattingBar(
    onInsertBullet: () -> Unit,
    onInsertNumbering: () -> Unit,
    onAddCheckbox: () -> Unit,
    onAddPhoto: () -> Unit,
    onRecordAudio: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FormatButton(
            icon = Icons.AutoMirrored.Filled.FormatListBulleted,
            label = "Bullet",
            onClick = onInsertBullet,
        )
        FormatButton(
            icon = Icons.Filled.FormatListNumbered,
            label = "Number",
            onClick = onInsertNumbering,
        )
        // Renamed from "To-do" to "Checkbox" per user instruction #1
        FormatButton(
            icon = Icons.Filled.CheckBox,
            label = "Checkbox",
            onClick = onAddCheckbox,
        )
        FormatButton(
            icon = Icons.Filled.Image,
            label = "Photo",
            onClick = onAddPhoto,
        )
        FormatButton(
            icon = Icons.Filled.Mic,
            label = "Audio",
            onClick = onRecordAudio,
        )
    }
}

@Composable
private fun FormatButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    val colors = NotelyTheme.colors
    val shapes = NotelyTheme.shapes

    Surface(
        onClick = onClick,
        shape = shapes.pill,
        color = colors.glassFill,
        modifier = Modifier.height(34.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = colors.textSecondary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = label,
                style = NotelyTheme.typography.meta,
                color = colors.textSecondary
            )
        }
    }
}

// ── Checklist Item Row ────────────────────────────────────────────────────

@Composable
private fun ChecklistItemRow(
    item: ChecklistItem,
    onCheckedChange: () -> Unit,
    onTextChanged: (String) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = NotelyTheme.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = item.isChecked,
            onCheckedChange = { onCheckedChange() },
            colors = CheckboxDefaults.colors(
                checkedColor = colors.accent,
                uncheckedColor = colors.textSecondary,
                checkmarkColor = colors.bgMid,
            )
        )

        BasicTextField(
            value = item.text,
            onValueChange = onTextChanged,
            modifier = Modifier.weight(1f),
            textStyle = NotelyTheme.typography.body.copy(
                color = if (item.isChecked) colors.textTertiary else colors.textPrimary,
            ),
            cursorBrush = SolidColor(colors.accent),
            decorationBox = { inner ->
                Box {
                    if (item.text.isEmpty()) {
                        Text(
                            text = "Checkbox item",
                            style = NotelyTheme.typography.body,
                            color = colors.textTertiary,
                        )
                    }
                    inner()
                }
            }
        )

        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Remove checkbox item",
                tint = colors.textTertiary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// ── Audio Player Card with Seek, Duration, and Error Feedback ─────────────

@Composable
private fun AudioPlayerCard(
    audioPath: String,
    durationSec: Int,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val colors = NotelyTheme.colors
    val shapes = NotelyTheme.shapes
    val player = remember { AudioPlayerManager(context) }
    val playInfo by player.playbackInfo.collectAsState()

    DisposableEffect(Unit) {
        onDispose { player.release() }
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = shapes.card,
        color = colors.glassFill,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = {
                        if (playInfo.isPlaying) {
                            player.pause()
                        } else {
                            player.play(audioPath)
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(colors.accent)
                ) {
                    Icon(
                        imageVector = if (playInfo.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = colors.bgMid,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    val currentSec = playInfo.currentPositionMs / 1000
                    val totalSec = if (playInfo.totalDurationMs > 0) playInfo.totalDurationMs / 1000 else durationSec
                    val timeLabel = String.format(
                        Locale.getDefault(),
                        "%02d:%02d / %02d:%02d",
                        currentSec / 60, currentSec % 60,
                        totalSec / 60, totalSec % 60
                    )
                    Text(
                        text = timeLabel,
                        style = NotelyTheme.typography.meta,
                        color = colors.textSecondary
                    )

                    val sliderValue = if (playInfo.totalDurationMs > 0) {
                        (playInfo.currentPositionMs.toFloat() / playInfo.totalDurationMs.toFloat()).coerceIn(0f, 1f)
                    } else 0f

                    Slider(
                        value = sliderValue,
                        onValueChange = { frac ->
                            if (playInfo.totalDurationMs > 0) {
                                player.seekTo((frac * playInfo.totalDurationMs).toInt())
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = colors.accent,
                            activeTrackColor = colors.accent,
                            inactiveTrackColor = colors.glassBorder
                        )
                    )
                }

                IconButton(onClick = {
                    player.release()
                    onRemove()
                }) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Remove audio",
                        tint = colors.danger,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (playInfo.errorMessage != null) {
                Text(
                    text = playInfo.errorMessage ?: "",
                    style = NotelyTheme.typography.meta,
                    color = colors.danger,
                    modifier = Modifier.padding(top = 4.dp, start = 8.dp)
                )
            }
        }
    }
}

// ── Top Toolbar ───────────────────────────────────────────────────────────

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

        // Move to Bin
        IconButton(onClick = onTrash) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = "Move to Bin",
                tint = colors.textSecondary,
            )
        }
    }
}

// ── Colour Picker Sheet ───────────────────────────────────────────────────

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
        color = colors.bgMid.copy(alpha = 0.98f),
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
