package com.example.notely.ui.theme

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Shader
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import kotlin.random.Random

/**
 * Cached grain noise bitmap (128×128, mid-gray pixels with random alpha).
 * Created once per process lifetime, tiled via [BitmapShader].
 * Deterministic seed (42) ensures visual consistency across sessions.
 */
private object GrainTexture {
    val bitmap: Bitmap by lazy {
        val size = 128
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val random = Random(42)
        val pixels = IntArray(size * size) {
            val alpha = random.nextInt(256)
            (alpha shl 24) or 0x00808080 // mid-gray with random alpha
        }
        bmp.setPixels(pixels, 0, size, 0, 0, size, size)
        bmp
    }
}

/**
 * Full-screen background with a 3-stop vertical gradient and subtle film grain overlay.
 *
 * Used by both screens:
 * - Notes screen: uses the default gradient ([NotelyColors.bgTop] → [NotelyColors.bgMid] → [NotelyColors.bgBottom])
 * - Editor screen: passes [overrideColor] to fill with the note's own color
 *
 * The grain is drawn once via [drawWithCache] and never regenerated per frame.
 * See §10.2 for the visual spec.
 */
@Composable
fun NotelyBackground(
    modifier: Modifier = Modifier,
    overrideColor: Color? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val colors = NotelyTheme.colors
    val grainAlpha = if (colors.isDark) 0.06f else 0.04f

    val gradientColors = if (overrideColor != null) {
        listOf(overrideColor, overrideColor)
    } else {
        listOf(colors.bgTop, colors.bgMid, colors.bgBottom)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .drawWithCache {
                val gradient = Brush.verticalGradient(
                    colors = gradientColors,
                    startY = 0f,
                    endY = size.height,
                )
                val grainShader = BitmapShader(
                    GrainTexture.bitmap,
                    Shader.TileMode.REPEAT,
                    Shader.TileMode.REPEAT,
                )
                val grainPaint = android.graphics.Paint().apply {
                    shader = grainShader
                    alpha = (grainAlpha * 255).toInt()
                }

                onDrawBehind {
                    drawRect(brush = gradient)
                    drawIntoCanvas { canvas ->
                        canvas.nativeCanvas.drawPaint(grainPaint)
                    }
                }
            },
        content = content,
    )
}
