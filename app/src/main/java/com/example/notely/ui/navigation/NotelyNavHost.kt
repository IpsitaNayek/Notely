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
 * Navigation routes — exactly two destinations per §2.
 */
object NotelyRoutes {
    const val NOTES = "notes"
    const val EDITOR_WITH_ARG = "editor?noteId={noteId}"

    fun editorRoute(noteId: String? = null): String =
        if (noteId != null) "editor?noteId=$noteId" else "editor"
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
                    navController.navigate(NotelyRoutes.editorRoute(noteId))
                },
                onNewNote = {
                    navController.navigate(NotelyRoutes.editorRoute())
                },
            )
        }

        composable(
            route = NotelyRoutes.EDITOR_WITH_ARG,
            arguments = listOf(
                navArgument("noteId") {
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
