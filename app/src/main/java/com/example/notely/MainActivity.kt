package com.example.notely

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.notely.data.preferences.NotelyPreferences
import com.example.notely.ui.navigation.NotelyNavHost
import com.example.notely.ui.theme.NotelyTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferences: NotelyPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val darkOverride by preferences.darkThemeOverride.collectAsStateWithLifecycle(initialValue = null)
            val isDark = when (darkOverride) {
                true -> true
                false -> false
                null -> isSystemInDarkTheme()
            }
            NotelyTheme(darkTheme = isDark) {
                NotelyNavHost()
            }
        }
    }
}