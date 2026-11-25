package com.example.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object StudioIcons {

    val Remove: ImageVector by lazy {
        icon("Remove") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(19f, 13f)
                horizontalLineTo(5f)
                verticalLineToRelative(-2f)
                horizontalLineToRelative(14f)
                verticalLineToRelative(2f)
                close()
            }
        }
    }

    val Stop: ImageVector by lazy {
        icon("Stop") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(6f, 6f)
                horizontalLineToRelative(12f)
                verticalLineToRelative(12f)
                horizontalLineTo(6f)
                close()
            }
        }
    }

    val ContentCopy: ImageVector by lazy {
        icon("ContentCopy") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(16f, 1f)
                horizontalLineTo(4f)
                curveToRelative(-1.1f, 0f, -2f, 0.9f, -2f, 2f)
                verticalLineToRelative(14f)
                horizontalLineToRelative(2f)
                verticalLineTo(3f)
                horizontalLineToRelative(12f)
                verticalLineTo(1f)
                close()
                moveTo(19f, 5f)
                horizontalLineTo(8f)
                curveToRelative(-1.1f, 0f, -2f, 0.9f, -2f, 2f)
                verticalLineToRelative(14f)
                curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f)
                horizontalLineToRelative(11f)
                curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f)
                verticalLineTo(7f)
                curveToRelative(0f, -1.1f, -0.9f, -2f, -2f, -2f)
                close()
                moveTo(19f, 21f)
                horizontalLineTo(8f)
                verticalLineTo(7f)
                horizontalLineToRelative(11f)
                verticalLineToRelative(14f)
                close()
            }
        }
    }

    val Download: ImageVector by lazy {
        icon("Download") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(5f, 20f)
                horizontalLineToRelative(14f)
                verticalLineToRelative(-2f)
                horizontalLineTo(5f)
                verticalLineToRelative(2f)
                close()
                moveTo(19f, 9f)
                horizontalLineToRelative(-4f)
                verticalLineTo(3f)
                horizontalLineTo(9f)
                verticalLineToRelative(6f)
                horizontalLineTo(5f)
                lineToRelative(7f, 7f)
                lineToRelative(7f, -7f)
                close()
            }
        }
    }

    val Memory: ImageVector by lazy {
        icon("Memory") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(15f, 9f)
                horizontalLineTo(9f)
                verticalLineToRelative(6f)
                horizontalLineToRelative(6f)
                verticalLineTo(9f)
                close()
                moveTo(13f, 13f)
                horizontalLineToRelative(-2f)
                verticalLineToRelative(-2f)
                horizontalLineToRelative(2f)
                verticalLineToRelative(2f)
                close()
                moveTo(21f, 11f)
                verticalLineTo(9f)
                horizontalLineToRelative(-2f)
                verticalLineTo(7f)
                curveToRelative(0f, -1.1f, -0.9f, -2f, -2f, -2f)
                horizontalLineToRelative(-2f)
                verticalLineTo(3f)
                horizontalLineToRelative(-2f)
                verticalLineToRelative(2f)
                horizontalLineToRelative(-2f)
                verticalLineTo(3f)
                horizontalLineTo(9f)
                verticalLineToRelative(2f)
                horizontalLineTo(7f)
                curveToRelative(-1.1f, 0f, -2f, 0.9f, -2f, 2f)
                verticalLineToRelative(2f)
                horizontalLineTo(3f)
                verticalLineToRelative(2f)
                horizontalLineToRelative(2f)
                verticalLineToRelative(2f)
                horizontalLineTo(3f)
                verticalLineToRelative(2f)
                horizontalLineToRelative(2f)
                verticalLineToRelative(2f)
                curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f)
                horizontalLineToRelative(2f)
                verticalLineToRelative(2f)
                horizontalLineToRelative(2f)
                verticalLineToRelative(-2f)
                horizontalLineToRelative(2f)
                verticalLineToRelative(2f)
                horizontalLineToRelative(2f)
                verticalLineToRelative(-2f)
                horizontalLineToRelative(2f)
                curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f)
                verticalLineToRelative(-2f)
                horizontalLineToRelative(2f)
                verticalLineToRelative(-2f)
                horizontalLineToRelative(-2f)
                verticalLineToRelative(-2f)
                horizontalLineToRelative(2f)
                close()
                moveTo(17f, 17f)
                horizontalLineTo(7f)
                verticalLineTo(7f)
                horizontalLineToRelative(10f)
                verticalLineToRelative(10f)
                close()
            }
        }
    }

    val FolderOpen: ImageVector by lazy {
        icon("FolderOpen") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(20f, 6f)
                horizontalLineToRelative(-8f)
                lineToRelative(-2f, -2f)
                horizontalLineTo(4f)
                curveToRelative(-1.1f, 0f, -1.99f, 0.9f, -1.99f, 2f)
                lineTo(2f, 18f)
                curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f)
                horizontalLineToRelative(16f)
                curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f)
                verticalLineTo(8f)
                curveToRelative(0f, -1.1f, -0.9f, -2f, -2f, -2f)
                close()
                moveTo(20f, 18f)
                horizontalLineTo(4f)
                verticalLineTo(8f)
                horizontalLineToRelative(16f)
                verticalLineToRelative(10f)
                close()
            }
        }
    }

    val Description: ImageVector by lazy {
        icon("Description") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(14f, 2f)
                horizontalLineTo(6f)
                curveToRelative(-1.1f, 0f, -1.99f, 0.9f, -1.99f, 2f)
                lineTo(4f, 20f)
                curveToRelative(0f, 1.1f, 0.89f, 2f, 1.99f, 2f)
                horizontalLineTo(18f)
                curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f)
                verticalLineTo(8f)
                lineToRelative(-6f, -6f)
                close()
                moveTo(16f, 18f)
                horizontalLineTo(8f)
                verticalLineToRelative(-2f)
                horizontalLineToRelative(8f)
                verticalLineToRelative(2f)
                close()
                moveTo(16f, 14f)
                horizontalLineTo(8f)
                verticalLineToRelative(-2f)
                horizontalLineToRelative(8f)
                verticalLineToRelative(2f)
                close()
                moveTo(13f, 9f)
                verticalLineTo(3.5f)
                lineTo(18.5f, 9f)
                horizontalLineTo(13f)
                close()
            }
        }
    }

    val Visibility: ImageVector by lazy {
        icon("Visibility") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 4.5f)
                curveTo(7f, 4.5f, 2.73f, 7.61f, 1f, 12f)
                curveToRelative(1.73f, 4.39f, 6f, 7.5f, 11f, 7.5f)
                reflectiveCurveToRelative(9.27f, -3.11f, 11f, -7.5f)
                curveToRelative(-1.73f, -4.39f, -6f, -7.5f, -11f, -7.5f)
                close()
                moveTo(12f, 17f)
                curveToRelative(-2.76f, 0f, -5f, -2.24f, -5f, -5f)
                reflectiveCurveToRelative(2.24f, -5f, 5f, -5f)
                reflectiveCurveToRelative(5f, 2.24f, 5f, 5f)
                reflectiveCurveToRelative(-2.24f, 5f, -5f, 5f)
                close()
                moveTo(12f, 9f)
                curveToRelative(-1.66f, 0f, -3f, 1.34f, -3f, 3f)
                reflectiveCurveToRelative(1.34f, 3f, 3f, 3f)
                reflectiveCurveToRelative(3f, -1.34f, 3f, -3f)
                reflectiveCurveToRelative(-1.34f, -3f, -3f, -3f)
                close()
            }
        }
    }

    val AutoFixHigh: ImageVector by lazy {
        icon("AutoFixHigh") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(7.5f, 5.6f)
                lineTo(10f, 7f)
                lineTo(8.6f, 4.5f)
                lineTo(10f, 2f)
                lineTo(7.5f, 3.4f)
                lineTo(5f, 2f)
                lineTo(6.4f, 4.5f)
                lineTo(5f, 7f)
                close()
                moveTo(19.5f, 15.4f)
                lineTo(17f, 14f)
                lineTo(18.4f, 16.5f)
                lineTo(17f, 19f)
                lineTo(19.5f, 17.6f)
                lineTo(22f, 19f)
                lineTo(20.6f, 16.5f)
                lineTo(22f, 14f)
                close()
                moveTo(22f, 2f)
                lineTo(19.5f, 3.4f)
                lineTo(17f, 2f)
                lineTo(18.4f, 4.5f)
                lineTo(17f, 7f)
                lineTo(19.5f, 5.6f)
                lineTo(22f, 7f)
                lineTo(20.6f, 4.5f)
                close()
                moveTo(14.37f, 7.29f)
                curveToRelative(-0.39f, -0.39f, -1.02f, -0.39f, -1.41f, 0f)
                lineTo(1.29f, 18.96f)
                curveToRelative(-0.39f, 0.39f, -0.39f, 1.02f, 0f, 1.41f)
                lineToRelative(2.34f, 2.34f)
                curveToRelative(0.39f, 0.39f, 1.02f, 0.39f, 1.41f, 0f)
                lineTo(16.7f, 11.05f)
                curveToRelative(0.39f, -0.39f, 0.39f, -1.02f, 0f, -1.41f)
                lineToRelative(-2.33f, -2.35f)
                close()
            }
        }
    }

    val Tune: ImageVector by lazy {
        icon("Tune") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(3f, 17f)
                verticalLineToRelative(2f)
                horizontalLineToRelative(6f)
                verticalLineToRelative(-2f)
                horizontalLineTo(3f)
                close()
                moveTo(3f, 5f)
                verticalLineToRelative(2f)
                horizontalLineToRelative(10f)
                verticalLineTo(5f)
                horizontalLineTo(3f)
                close()
                moveTo(13f, 21f)
                verticalLineToRelative(-2f)
                horizontalLineToRelative(8f)
                verticalLineToRelative(-2f)
                horizontalLineToRelative(-8f)
                verticalLineToRelative(-2f)
                horizontalLineToRelative(-2f)
                verticalLineToRelative(6f)
                horizontalLineToRelative(2f)
                close()
                moveTo(7f, 9f)
                verticalLineToRelative(2f)
                horizontalLineTo(3f)
                verticalLineToRelative(2f)
                horizontalLineToRelative(4f)
                verticalLineToRelative(2f)
                horizontalLineToRelative(2f)
                verticalLineTo(9f)
                horizontalLineTo(7f)
                close()
                moveTo(21f, 13f)
                verticalLineToRelative(-2f)
                horizontalLineTo(11f)
                verticalLineToRelative(2f)
                horizontalLineToRelative(10f)
                close()
                moveTo(15f, 9f)
                horizontalLineToRelative(2f)
                verticalLineTo(7f)
                horizontalLineToRelative(4f)
                verticalLineTo(5f)
                horizontalLineToRelative(-4f)
                verticalLineTo(3f)
                horizontalLineToRelative(-2f)
                verticalLineToRelative(6f)
                close()
            }
        }
    }

    val AutoStories: ImageVector by lazy {
        icon("AutoStories") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(21f, 5f)
                curveToRelative(-1.11f, -0.35f, -2.33f, -0.5f, -3.5f, -0.5f)
                curveToRelative(-1.95f, 0f, -4.05f, 0.4f, -5.5f, 1.5f)
                curveToRelative(-1.45f, -1.1f, -3.55f, -1.5f, -5.5f, -1.5f)
                reflectiveCurveTo(2.45f, 4.9f, 1f, 6f)
                verticalLineToRelative(14.65f)
                curveToRelative(0f, 0.25f, 0.25f, 0.5f, 0.5f, 0.5f)
                curveToRelative(0.1f, 0f, 0.15f, -0.05f, 0.25f, -0.05f)
                curveTo(3.1f, 20.45f, 5.05f, 20f, 6.5f, 20f)
                curveToRelative(1.95f, 0f, 4.05f, 0.4f, 5.5f, 1.5f)
                curveToRelative(1.35f, -0.85f, 3.8f, -1.5f, 5.5f, -1.5f)
                curveToRelative(1.65f, 0f, 3.35f, 0.3f, 4.75f, 1.05f)
                curveToRelative(0.1f, 0.05f, 0.15f, 0.05f, 0.25f, 0.05f)
                curveToRelative(0.25f, 0f, 0.5f, -0.25f, 0.5f, -0.5f)
                verticalLineTo(6f)
                curveToRelative(-0.6f, -0.45f, -1.25f, -0.75f, -2f, -1f)
                close()
                moveTo(21f, 18.5f)
                curveToRelative(-1.1f, -0.35f, -2.3f, -0.5f, -3.5f, -0.5f)
                curveToRelative(-1.7f, 0f, -4.15f, 0.65f, -5.5f, 1.5f)
                verticalLineTo(8f)
                curveToRelative(1.35f, -0.85f, 3.8f, -1.5f, 5.5f, -1.5f)
                curveToRelative(1.2f, 0f, 2.4f, 0.15f, 3.5f, 0.5f)
                verticalLineToRelative(11.5f)
                close()
            }
        }
    }

    val EditNote: ImageVector by lazy {
        icon("EditNote") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(3f, 10f)
                horizontalLineToRelative(11f)
                verticalLineToRelative(2f)
                horizontalLineTo(3f)
                verticalLineToRelative(-2f)
                close()
                moveTo(3f, 8f)
                horizontalLineToRelative(11f)
                verticalLineTo(6f)
                horizontalLineTo(3f)
                verticalLineToRelative(2f)
                close()
                moveTo(3f, 16f)
                horizontalLineToRelative(7f)
                verticalLineToRelative(-2f)
                horizontalLineTo(3f)
                verticalLineToRelative(2f)
                close()
                moveTo(18.01f, 12.87f)
                lineToRelative(0.71f, -0.71f)
                curveToRelative(0.39f, -0.39f, 1.02f, -0.39f, 1.41f, 0f)
                lineToRelative(0.71f, 0.71f)
                curveToRelative(0.39f, 0.39f, 0.39f, 1.02f, 0f, 1.41f)
                lineToRelative(-0.71f, 0.71f)
                lineToRelative(-2.12f, -2.12f)
                close()
                moveTo(17.3f, 13.58f)
                lineToRelative(-5.3f, 5.3f)
                verticalLineTo(21f)
                horizontalLineToRelative(2.12f)
                lineToRelative(5.3f, -5.3f)
                lineToRelative(-2.12f, -2.12f)
                close()
            }
        }
    }

    private inline fun icon(
        name: String,
        block: ImageVector.Builder.() -> Unit
    ): ImageVector = ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply(block).build()
}
