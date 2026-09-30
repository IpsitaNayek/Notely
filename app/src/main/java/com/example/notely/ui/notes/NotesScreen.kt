package com.example.notely.ui.notes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.notely.data.local.NoteEntity
import com.example.notely.data.local.NoteType
import com.example.notely.ui.components.AudioRecorderDialog
import com.example.notely.ui.components.BottomDock
import com.example.notely.ui.components.ChipRow
import com.example.notely.ui.components.CreateNoteSheet
import com.example.notely.ui.components.FeaturedNoteCard
import com.example.notely.ui.components.NoteActionSheet
import com.example.notely.ui.components.NoteCard
import com.example.notely.ui.components.NotelyFab
import com.example.notely.ui.components.SearchBar
import com.example.notely.ui.theme.NotelyBackground
import com.example.notely.ui.theme.NotelyTheme
import kotlinx.coroutines.launch

@Composable
fun NotesScreen(
    onOpenNote: (String) -> Unit,
    onNewNote: (type: String?) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = NotelyTheme.colors
    val spacing = NotelyTheme.spacing
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    NotelyBackground {
        Box(modifier = modifier.fillMaxSize()) {
            if (uiState.selectedDockItem == DockItem.SETTINGS) {
                com.example.notely.ui.settings.SettingsContent(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .padding(bottom = spacing.dockBottomMargin + spacing.dockHeight + 24.dp),
                )
            } else {
                // ── Main scrollable grid ──
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding(),
                    contentPadding = PaddingValues(
                        start = spacing.screenHorizontalPadding,
                        end = spacing.screenHorizontalPadding,
                        bottom = spacing.gridBottomPadding + 20.dp,
                    ),
                    horizontalArrangement = Arrangement.spacedBy(spacing.gridGutter),
                    verticalItemSpacing = spacing.gridGutter,
                ) {
                    // ── Header ──
                    item(span = StaggeredGridItemSpan.FullLine) {
                        NotesHeader(
                            uiState = uiState,
                            onQueryChanged = viewModel::onSearchQueryChanged,
                            onClearSearch = { viewModel.onSearchQueryChanged("") },
                            modifier = Modifier.padding(bottom = 4.dp),
                        )
                    }

                    // ── Meaningful Category Chip Row ──
                    item(span = StaggeredGridItemSpan.FullLine) {
                        ChipRow(
                            selectedCategory = uiState.selectedCategory,
                            onCategorySelected = viewModel::onCategorySelected,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(modifier = Modifier.height(spacing.gridGutter))
                    }

                    // ── Bin Action Header ──
                    if (uiState.selectedCategory == NoteCategory.BIN && uiState.notes.isNotEmpty()) {
                        item(span = StaggeredGridItemSpan.FullLine) {
                            BinTopBar(
                                count = uiState.notes.size,
                                onEmptyBin = { viewModel.onShowEmptyBinDialog(true) },
                            )
                        }
                    }

                    // ── Empty State ──
                    if (uiState.notes.isEmpty() && !uiState.isLoading) {
                        item(span = StaggeredGridItemSpan.FullLine) {
                            CategoryEmptyState(
                                category = uiState.selectedCategory,
                                isSearching = uiState.isSearchActive && uiState.searchQuery.isNotBlank()
                            )
                        }
                    } else {
                        // ── Featured card (first note in All or Pinned) ──
                        val featured = uiState.notes.firstOrNull()
                        val showFeatured = featured != null &&
                                uiState.selectedCategory != NoteCategory.BIN &&
                                !uiState.isSearchActive

                        if (showFeatured && featured != null) {
                            item(span = StaggeredGridItemSpan.FullLine) {
                                FeaturedNoteCard(
                                    note = featured,
                                    onClick = { onOpenNote(featured.id) },
                                    onLongClick = { viewModel.onShowNoteActions(featured) },
                                )
                            }
                        }

                        // ── Staggered grid cards ──
                        val gridNotes = if (showFeatured) uiState.notes.drop(1) else uiState.notes
                        items(gridNotes, key = { it.id }) { note ->
                            NoteCard(
                                note = note,
                                onClick = {
                                    if (uiState.selectedCategory == NoteCategory.BIN) {
                                        viewModel.onShowNoteActions(note)
                                    } else {
                                        onOpenNote(note.id)
                                    }
                                },
                                onLongClick = {
                                    viewModel.onShowNoteActions(note)
                                },
                            )
                        }
                    }
                }
            }

            // ── Floating Action Button & Bottom Dock ──
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = spacing.dockBottomMargin),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // FAB — hidden in Bin / Settings
                AnimatedVisibility(
                    visible = uiState.selectedDockItem == DockItem.HOME &&
                            uiState.selectedCategory != NoteCategory.BIN,
                    enter = fadeIn() + slideInVertically { it },
                    exit = fadeOut() + slideOutVertically { it },
                ) {
                    NotelyFab(
                        onClick = { viewModel.onShowCreateChoice(true) },
                        modifier = Modifier.padding(bottom = spacing.fabDockGap),
                    )
                }

                BottomDock(
                    selectedItem = uiState.selectedDockItem,
                    onItemSelected = viewModel::onDockItemSelected,
                )
            }

            // Snackbar Host for Undo Delete
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = spacing.dockHeight + spacing.dockBottomMargin + 16.dp)
            )
        }
    }

    // ── Create Note Choice Bottom Sheet ──
    if (uiState.showCreateChoiceDialog) {
        CreateNoteSheet(
            onNewNormalNote = { onNewNote(NoteType.NORMAL.name) },
            onNewTodoNote = { onNewNote(NoteType.TODO.name) },
            onNewAudioNote = { viewModel.onShowAudioRecorder(true) },
            onDismiss = { viewModel.onShowCreateChoice(false) }
        )
    }

    // ── Dedicated Audio Recorder Dialog ──
    if (uiState.showAudioRecorderDialog) {
        AudioRecorderDialog(
            onDismiss = { viewModel.onShowAudioRecorder(false) },
            onSave = { title, file, duration ->
                viewModel.onSaveAudioNote(title, file, duration)
                viewModel.onShowAudioRecorder(false)
            }
        )
    }

    // ── Long-Press / Bin Note Contextual Action Sheet ──
    uiState.noteForActions?.let { note ->
        NoteActionSheet(
            note = note,
            isInBin = note.isTrashed || uiState.selectedCategory == NoteCategory.BIN,
            onTogglePin = { viewModel.onTogglePin(note.id) },
            onMoveToBin = {
                viewModel.onMoveToTrash(note.id)
                scope.launch {
                    val result = snackbarHostState.showSnackbar(
                        message = "Note moved to Bin",
                        actionLabel = "Undo",
                        duration = SnackbarDuration.Short,
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.onRestoreFromTrash(note.id)
                    }
                }
            },
            onRestore = {
                viewModel.onRestoreFromTrash(note.id)
            },
            onDeletePermanently = {
                viewModel.onDeletePermanently(note.id)
            },
            onDismiss = { viewModel.onShowNoteActions(null) }
        )
    }

    // ── Empty Bin Confirmation Dialog ──
    if (uiState.showEmptyBinDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onShowEmptyBinDialog(false) },
            title = { Text("Empty Bin?", color = colors.textPrimary) },
            text = {
                Text(
                    "This will permanently delete all notes in the Bin. This action cannot be undone.",
                    color = colors.textSecondary,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.onEmptyTrash() },
                ) {
                    Text("Delete Permanently", color = colors.danger)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onShowEmptyBinDialog(false) }) {
                    Text("Cancel", color = colors.textSecondary)
                }
            },
            containerColor = colors.bgMid,
            shape = NotelyTheme.shapes.menu,
        )
    }
}

// ── Sub-composables ────────────────────────────────────────────────────────

@Composable
private fun NotesHeader(
    uiState: NotesUiState,
    onQueryChanged: (String) -> Unit,
    onClearSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = NotelyTheme.colors
    val spacing = NotelyTheme.spacing
    val isBin = uiState.selectedCategory == NoteCategory.BIN

    Column(modifier = modifier) {
        Spacer(modifier = Modifier.height(spacing.headerSpacing))

        AnimatedVisibility(visible = !uiState.isSearchActive) {
            Column {
                Text(
                    text = if (isBin) "Bin" else "Your\nNotes",
                    style = NotelyTheme.typography.display,
                    color = colors.textPrimary,
                )
                val countText = if (isBin) {
                    "${uiState.trashedNotes.size} note${if (uiState.trashedNotes.size != 1) "s" else ""}"
                } else {
                    "${uiState.activeNoteCount} note${if (uiState.activeNoteCount != 1) "s" else ""}"
                }
                Text(
                    text = countText,
                    style = NotelyTheme.typography.preview,
                    color = colors.textTertiary,
                    modifier = Modifier.padding(top = 4.dp),
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Search Bar when SEARCH dock item is active
        AnimatedVisibility(visible = uiState.isSearchActive) {
            Column {
                Spacer(Modifier.height(8.dp))
                SearchBar(
                    query = uiState.searchQuery,
                    onQueryChanged = onQueryChanged,
                    onClear = onClearSearch,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun BinTopBar(
    count: Int,
    onEmptyBin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = NotelyTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "$count item${if (count != 1) "s" else ""} in Bin",
            style = NotelyTheme.typography.meta,
            color = colors.textSecondary,
        )
        TextButton(onClick = onEmptyBin) {
            Text(
                text = "Empty Bin",
                style = NotelyTheme.typography.label,
                color = colors.danger,
            )
        }
    }
}

@Composable
private fun CategoryEmptyState(
    category: NoteCategory,
    isSearching: Boolean,
) {
    val colors = NotelyTheme.colors
    val (title, subtitle) = if (isSearching) {
        "No results" to "Try a different search query."
    } else {
        when (category) {
            NoteCategory.ALL -> "No notes yet" to "Tap ＋ to write your first note."
            NoteCategory.PINNED -> "No pinned notes yet" to "Long-press any note to pin it here."
            NoteCategory.TODO -> "No to-do notes yet" to "Tap ＋ and choose New To-do."
            NoteCategory.NOTES -> "No notes yet" to "Tap ＋ to write your first note."
            NoteCategory.AUDIO -> "No audio notes yet" to "Tap the microphone icon in the dock to record."
            NoteCategory.BIN -> "Bin is empty" to "Deleted notes will appear here."
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = title,
            style = NotelyTheme.typography.cardTitleFeatured,
            color = colors.textPrimary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = subtitle,
            style = NotelyTheme.typography.preview,
            color = colors.textTertiary,
            textAlign = TextAlign.Center,
        )
    }
}
