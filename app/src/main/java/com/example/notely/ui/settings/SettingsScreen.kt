package com.example.notely.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.notely.BuildConfig
import com.example.notely.ui.theme.NotelyBackground
import com.example.notely.ui.theme.NotelyTheme

/**
 * Settings screen — navigated to from the dock Settings icon or route.
 */
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    NotelyBackground {
        SettingsContent(
            modifier = modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            viewModel = viewModel,
        )
    }
}

/**
 * Reusable settings content for embed in NotesScreen or standalone.
 */
@Composable
fun SettingsContent(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = NotelyTheme.colors
    val spacing = NotelyTheme.spacing

    Column(
        modifier = modifier.padding(horizontal = spacing.screenHorizontalPadding),
    ) {
        Spacer(Modifier.height(spacing.headerSpacing))

        Text(
            text = "Settings",
            style = NotelyTheme.typography.display,
            color = colors.textPrimary,
        )

        Spacer(Modifier.height(32.dp))

        // ── Appearance ──
        SectionHeader(title = "Appearance")
        Spacer(Modifier.height(12.dp))

        val options = listOf(
            null to "System default",
            false to "Light",
            true to "Dark",
        )
        Column(modifier = Modifier.selectableGroup()) {
            options.forEach { (value, label) ->
                ThemeOption(
                    label = label,
                    selected = uiState.darkThemeOverride == value,
                    onClick = { viewModel.onThemeModeChanged(value) },
                )
            }
        }

        Spacer(Modifier.height(24.dp))
        HorizontalDivider(color = colors.glassBorder)
        Spacer(Modifier.height(24.dp))

        // ── About ──
        SectionHeader(title = "About")
        Spacer(Modifier.height(12.dp))

        LabelValueRow(label = "Version", value = BuildConfig.VERSION_NAME)
        LabelValueRow(label = "Build", value = BuildConfig.VERSION_CODE.toString())
    }
}

// ── Sub-composables ──────────────────────────────────────────────────────

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = NotelyTheme.typography.label,
        color = NotelyTheme.colors.textTertiary,
    )
}

@Composable
private fun ThemeOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = NotelyTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
            colors = RadioButtonDefaults.colors(
                selectedColor = colors.accent,
                unselectedColor = colors.textTertiary,
            ),
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = label,
            style = NotelyTheme.typography.body,
            color = colors.textPrimary,
        )
    }
}

@Composable
private fun LabelValueRow(label: String, value: String) {
    val colors = NotelyTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = NotelyTheme.typography.body,
            color = colors.textPrimary,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = NotelyTheme.typography.body,
            color = colors.textSecondary,
        )
    }
}
