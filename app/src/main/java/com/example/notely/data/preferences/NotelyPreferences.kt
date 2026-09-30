package com.example.notely.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wraps DataStore<Preferences> for Notely user preferences.
 *
 * Currently stores:
 *  - [useDarkTheme] — explicit dark mode override (null = follow system)
 */
@Singleton
class NotelyPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    companion object {
        private val KEY_USE_DARK_THEME = booleanPreferencesKey("use_dark_theme")
        private val KEY_DARK_THEME_OVERRIDE = booleanPreferencesKey("dark_theme_override")
    }

    /**
     * Emits `null` = follow system, `true` = always dark, `false` = always light.
     */
    val darkThemeOverride: Flow<Boolean?> = dataStore.data.map { prefs ->
        if (prefs[KEY_DARK_THEME_OVERRIDE] == true) prefs[KEY_USE_DARK_THEME] else null
    }

    suspend fun setDarkTheme(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_USE_DARK_THEME] = enabled
            prefs[KEY_DARK_THEME_OVERRIDE] = true
        }
    }

    suspend fun clearDarkThemeOverride() {
        dataStore.edit { prefs ->
            prefs.remove(KEY_USE_DARK_THEME)
            prefs.remove(KEY_DARK_THEME_OVERRIDE)
        }
    }
}
