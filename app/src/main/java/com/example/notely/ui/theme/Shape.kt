package com.example.notely.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Notely shape tokens — §10.6.
 */
@Immutable
data class NotelyShapes(
    val card: Shape,             // Note cards, featured card — 20 dp
    val pill: Shape,             // Chips, dock, search, snackbar — fully rounded
    val circleButton: Shape,     // Glass icon buttons — circle
    val fab: Shape,              // FAB — 20 dp rounded square
    val dockSelectedItem: Shape, // Selected dock item — 16 dp
    val bottomSheet: Shape,      // Bottom sheet top corners — 28 dp
    val menu: Shape,             // Menus/dialogs — 16 dp
)

/**
 * Notely spacing tokens — §10.6.
 */
@Immutable
data class NotelySpacing(
    val screenHorizontalPadding: Dp,
    val gridGutter: Dp,
    val cardPadding: Dp,
    val chipHeight: Dp,
    val chipHorizontalPadding: Dp,
    val chipGap: Dp,
    val dockHeight: Dp,
    val dockBottomMargin: Dp,
    val fabSize: Dp,
    val fabDockGap: Dp,
    val headerSpacing: Dp,
    val gridBottomPadding: Dp,
)

val NotelyShapesDefaults = NotelyShapes(
    card = RoundedCornerShape(20.dp),
    pill = CircleShape,
    circleButton = CircleShape,
    fab = RoundedCornerShape(20.dp),
    dockSelectedItem = RoundedCornerShape(16.dp),
    bottomSheet = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    menu = RoundedCornerShape(16.dp),
)

val NotelySpacingDefaults = NotelySpacing(
    screenHorizontalPadding = 16.dp,
    gridGutter = 12.dp,
    cardPadding = 14.dp,
    chipHeight = 36.dp,
    chipHorizontalPadding = 16.dp,
    chipGap = 8.dp,
    dockHeight = 64.dp,
    dockBottomMargin = 16.dp,
    fabSize = 64.dp,
    fabDockGap = 12.dp,
    headerSpacing = 24.dp,
    gridBottomPadding = 104.dp,
)

val LocalNotelyShapes = staticCompositionLocalOf { NotelyShapesDefaults }
val LocalNotelySpacing = staticCompositionLocalOf { NotelySpacingDefaults }
