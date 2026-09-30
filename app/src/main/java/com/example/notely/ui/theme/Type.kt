package com.example.notely.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Notely typography tokens — §10.5.
 */
@Immutable
data class NotelyTypography(
    val display: TextStyle,
    val editorTitle: TextStyle,
    val cardTitle: TextStyle,
    val cardTitleFeatured: TextStyle,
    val body: TextStyle,
    val preview: TextStyle,
    val label: TextStyle,
    val meta: TextStyle,
)

val NotelyTypographyDefaults = NotelyTypography(
    display = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Light,
        fontSize = 56.sp,
        lineHeight = 56.sp,
        letterSpacing = (-0.5).sp,
    ),
    editorTitle = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Light,
        fontSize = 32.sp,
        lineHeight = 37.sp,
        letterSpacing = (-0.2).sp,
    ),
    cardTitle = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp,
    ),
    cardTitleFeatured = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp,
    ),
    body = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        lineHeight = 25.5.sp,
        letterSpacing = 0.sp,
    ),
    preview = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 17.5.sp,
        letterSpacing = 0.sp,
    ),
    label = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        letterSpacing = 0.1.sp,
    ),
    meta = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        letterSpacing = 0.2.sp,
    ),
)

val LocalNotelyTypography = staticCompositionLocalOf { NotelyTypographyDefaults }