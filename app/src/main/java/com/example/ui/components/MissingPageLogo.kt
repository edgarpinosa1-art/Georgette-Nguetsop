package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.BoneWhite
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.ObsidianBackground

/**
 * Custom vector trademark emblem for THE MISSING PAGE:
 * Minimalist open bone-white book silhouette on obsidian black with a sharp
 * negative-space missing/torn page gap on the right side marked by a vivid
 * crimson-red vertical page edge and bookmark ribbon.
 */
@Composable
fun MissingPageLogo(
    modifier: Modifier = Modifier,
    size: Dp = 36.dp
) {
    Canvas(
        modifier = modifier
            .size(size)
            .semantics { contentDescription = "The Missing Page Trademark Emblem" }
    ) {
        val w = this.size.width
        val h = this.size.height

        // Background obsidian plate
        drawRect(color = ObsidianBackground)

        // Outer spine cradle (subtle crisp frame along bottom)
        val cradlePath = Path().apply {
            moveTo(w * 0.10f, h * 0.25f)
            lineTo(w * 0.10f, h * 0.79f)
            lineTo(w * 0.43f, h * 0.79f)
            lineTo(w * 0.50f, h * 0.85f)
            lineTo(w * 0.57f, h * 0.79f)
            lineTo(w * 0.90f, h * 0.79f)
            lineTo(w * 0.90f, h * 0.25f)
        }
        drawPath(
            path = cradlePath,
            color = BoneWhite,
            style = Stroke(
                width = w * 0.055f,
                cap = StrokeCap.Square,
                join = StrokeJoin.Miter
            )
        )

        // Left open page (full solid bone-white silhouette curving into center spine)
        val leftPage = Path().apply {
            moveTo(w * 0.17f, h * 0.20f)
            lineTo(w * 0.41f, h * 0.20f)
            quadraticTo(w * 0.47f, h * 0.21f, w * 0.485f, h * 0.28f)
            lineTo(w * 0.485f, h * 0.74f)
            quadraticTo(w * 0.45f, h * 0.70f, w * 0.39f, h * 0.70f)
            lineTo(w * 0.17f, h * 0.70f)
            close()
        }
        drawPath(path = leftPage, color = BoneWhite, style = Fill)

        // Right open page with jagged negative-space missing/torn corner
        val rightPage = Path().apply {
            moveTo(w * 0.515f, h * 0.28f)
            quadraticTo(w * 0.53f, h * 0.21f, w * 0.59f, h * 0.20f)
            lineTo(w * 0.74f, h * 0.15f)
            // Torn jagged diagonal edge slicing down toward spine
            lineTo(w * 0.69f, h * 0.28f)
            lineTo(w * 0.73f, h * 0.34f)
            lineTo(w * 0.65f, h * 0.46f)
            lineTo(w * 0.68f, h * 0.53f)
            lineTo(w * 0.58f, h * 0.66f)
            lineTo(w * 0.58f, h * 0.70f)
            lineTo(w * 0.515f, h * 0.74f)
            close()
        }
        drawPath(path = rightPage, color = BoneWhite, style = Fill)

        // Remaining torn right-edge stub (subtle bone-white fragment to frame the negative gap)
        val rightStub = Path().apply {
            moveTo(w * 0.79f, h * 0.40f)
            lineTo(w * 0.83f, h * 0.36f)
            lineTo(w * 0.83f, h * 0.70f)
            lineTo(w * 0.77f, h * 0.70f)
            lineTo(w * 0.77f, h * 0.50f)
            close()
        }
        drawPath(path = rightStub, color = BoneWhite.copy(alpha = 0.90f), style = Fill)

        // Vivid crimson vertical page edge / bookmark ribbon marking the missing page
        val bookmarkRibbon = Path().apply {
            moveTo(w * 0.665f, h * 0.54f)
            lineTo(w * 0.745f, h * 0.45f)
            lineTo(w * 0.745f, h * 0.92f)
            lineTo(w * 0.705f, h * 0.86f)
            lineTo(w * 0.665f, h * 0.92f)
            close()
        }
        drawPath(path = bookmarkRibbon, color = CrimsonPrimary, style = Fill)

        // Crimson vertical page-edge highlight along right outer edge
        drawLine(
            color = CrimsonPrimary,
            start = Offset(w * 0.835f, h * 0.22f),
            end = Offset(w * 0.835f, h * 0.70f),
            strokeWidth = w * 0.042f,
            cap = StrokeCap.Butt
        )
    }
}
