package com.example.notely.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.notely.ui.editor.EditorScreen
import com.example.notely.ui.notes.NotesScreen

/**
 * Navigation routes for Notely.
 */
object NotelyRoutes {
    const val NOTES = "notes"
    const val EDITOR_ROUTE = "editor?noteId={noteId}&type={type}"

    fun editorRoute(noteId: String? = null, type: String? = null): String {
        val params = mutableListOf<String>()
        if (noteId != null) params.add("noteId=$noteId")
        if (type != null) params.add("type=$type")
        return if (params.isNotEmpty()) "editor?${params.joinToString("&")}" else "editor"
    }
}

@Composable
fun NotelyNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = NotelyRoutes.NOTES,
        modifier = modifier,
    ) {
        composable(NotelyRoutes.NOTES) {
            NotesScreen(
                onOpenNote = { noteId ->
                    navController.navigate(NotelyRoutes.editorRoute(noteId = noteId))
                },
                onNewNote = { type ->
                    navController.navigate(NotelyRoutes.editorRoute(type = type))
                },
            )
        }

        composable(
            route = NotelyRoutes.EDITOR_ROUTE,
            arguments = listOf(
                navArgument("noteId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("type") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) {
            EditorScreen(
                onBack = { navController.popBackStack() },
            )
        }
    }
}
