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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.notely.data.local.NoteEntity
import com.example.notely.ui.components.BottomDock
import com.example.notely.ui.components.ChipRow
import com.example.notely.ui.components.FeaturedNoteCard
import com.example.notely.ui.components.NoteCard
import com.example.notely.ui.components.NotelyFab
import com.example.notely.ui.components.SearchBar
import com.example.notely.ui.theme.NotelyBackground
import com.example.notely.ui.theme.NotelyTheme

/**
 * Notes screen — §3–§7.
 *
 * Layout (top → bottom):
 *  1. Status bar spacer
 *  2. "Your Notes" display heading + note count (collapsed when searching)
 *  3. Search bar (visible when [DockItem.SEARCH] selected)
 *  4. Horizontal chip row
 *  5. Staggered grid (featured card first, then 2-col grid)
 *  6. FAB centred above dock
 *  7. Bottom dock + nav bar spacer
 *
 * @param onOpenNote  Navigate to editor with the given note ID.
 * @param onNewNote   Navigate to editor with no note ID (new note).
 */
@Composable
fun NotesScreen(
    onOpenNote: (String) -> Unit,
    onNewNote: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = NotelyTheme.colors
    val spacing = NotelyTheme.spacing

    var showEmptyTrashDialog by remember { mutableStateOf(false) }

    NotelyBackground {
        Box(
            modifier = modifier.fillMaxSize(),
        ) {
            if (uiState.selectedDockItem == DockItem.SETTINGS) {
                com.example.notely.ui.settings.SettingsContent(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .padding(bottom = spacing.dockBottomMargin + spacing.dockHeight + 24.dp),
                )
            } else {
                // ── Main scrollable content column ──
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding(),
                contentPadding = PaddingValues(
                    start = spacing.screenHorizontalPadding,
                    end = spacing.screenHorizontalPadding,
                    bottom = spacing.gridBottomPadding,
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

                // ── Chip row ──
                item(span = StaggeredGridItemSpan.FullLine) {
                    ChipRow(
                        selectedChip = uiState.selectedChip,
                        onChipSelected = viewModel::onChipSelected,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(spacing.gridGutter))
                }

                // ── Empty state ──
                if (uiState.notes.isEmpty() && !uiState.isLoading) {
                    item(span = StaggeredGridItemSpan.FullLine) {
                        EmptyState(dock = uiState.selectedDockItem)
                    }
                } else {
                    // ── Featured card (first note, full width) ──
                    val featured = uiState.notes.firstOrNull()
                    if (featured != null && uiState.selectedDockItem != DockItem.TRASH) {
                        item(span = StaggeredGridItemSpan.FullLine) {
                            FeaturedNoteCard(
                                note = featured,
                                onClick = { onOpenNote(featured.id) },
                            )
                        }
                    }

                    // ── 2-col staggered grid (rest of notes) ──
                    val gridNotes = if (uiState.selectedDockItem == DockItem.TRASH) {
                        uiState.notes
                    } else {
                        uiState.notes.drop(1)
                    }
                    items(gridNotes, key = { it.id }) { note ->
                        NoteCard(
                            note = note,
                            onClick = {
                                if (uiState.selectedDockItem != DockItem.TRASH) {
                                    onOpenNote(note.id)
                                }
                            },
                        )
                    }

                    // ── Trash footer ──
                    if (uiState.selectedDockItem == DockItem.TRASH && uiState.notes.isNotEmpty()) {
                        item(span = StaggeredGridItemSpan.FullLine) {
                            TrashFooter(
                                count = uiState.notes.size,
                                onEmptyTrash = { showEmptyTrashDialog = true },
                            )
                        }
                    }
                }
            }
        }

            // ── FAB + Dock (pinned to bottom) ──
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = spacing.dockBottomMargin),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // FAB — hidden in Trash / Settings tabs
                AnimatedVisibility(
                    visible = uiState.selectedDockItem == DockItem.HOME ||
                            uiState.selectedDockItem == DockItem.SEARCH,
                    enter = fadeIn() + slideInVertically { it },
                    exit = fadeOut() + slideOutVertically { it },
                ) {
                    NotelyFab(
                        onClick = onNewNote,
                        modifier = Modifier.padding(bottom = spacing.fabDockGap),
                    )
                }

                BottomDock(
                    selectedItem = uiState.selectedDockItem,
                    onItemSelected = viewModel::onDockItemSelected,
                )
            }
        }
    }

    // ── Empty Trash confirmation dialog ──
    if (showEmptyTrashDialog) {
        AlertDialog(
            onDismissRequest = { showEmptyTrashDialog = false },
            title = { Text("Empty trash?", color = colors.textPrimary) },
            text = {
                Text(
                    "This will permanently delete ${uiState.notes.size} note(s). This action cannot be undone.",
                    color = colors.textSecondary,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onEmptyTrash()
                        showEmptyTrashDialog = false
                    },
                ) {
                    Text("Delete all", color = colors.danger)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmptyTrashDialog = false }) {
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
    val isTrash = uiState.selectedDockItem == DockItem.TRASH

    Column(modifier = modifier) {
        Spacer(modifier = Modifier.height(spacing.headerSpacing))

        // Display heading — collapses to single line when searching
        AnimatedVisibility(visible = !uiState.isSearchActive) {
            Column {
                Text(
                    text = if (isTrash) "Trash" else "Your\nNotes",
                    style = NotelyTheme.typography.display,
                    color = colors.textPrimary,
                )
                if (!isTrash) {
                    Text(
                        text = "${uiState.activeNoteCount} note${if (uiState.activeNoteCount != 1) "s" else ""}",
                        style = NotelyTheme.typography.preview,
                        color = colors.textTertiary,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Search bar — shown when SEARCH dock item is selected
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
private fun EmptyState(dock: DockItem) {
    val colors = NotelyTheme.colors
    val (title, subtitle) = when (dock) {
        DockItem.TRASH -> "Trash is empty" to "Deleted notes will appear here."
        DockItem.SEARCH -> "No results" to "Try a different search term."
        else -> "Nothing here yet" to "Tap ＋ to write your first note."
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

@Composable
private fun TrashFooter(
    count: Int,
    onEmptyTrash: () -> Unit,
) {
    val colors = NotelyTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "$count item${if (count != 1) "s" else ""} in trash",
            style = NotelyTheme.typography.meta,
            color = colors.textTertiary,
        )
        TextButton(onClick = onEmptyTrash) {
            Text(
                text = "Empty trash",
                style = NotelyTheme.typography.label,
                color = colors.danger,
            )
        }
    }
}
