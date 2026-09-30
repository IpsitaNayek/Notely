package com.example.notely.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notely.data.preferences.NotelyPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val darkThemeOverride: Boolean? = null, // null = system
    val isLoading: Boolean = true,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferences: NotelyPreferences,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = preferences.darkThemeOverride
        .map { override ->
            SettingsUiState(darkThemeOverride = override, isLoading = false)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsUiState(),
        )

    fun onThemeModeChanged(useDark: Boolean?) {
        viewModelScope.launch {
            if (useDark == null) {
                preferences.clearDarkThemeOverride()
            } else {
                preferences.setDarkTheme(useDark)
            }
        }
    }
}
