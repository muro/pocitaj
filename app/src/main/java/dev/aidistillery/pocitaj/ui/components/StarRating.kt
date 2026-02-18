package dev.aidistillery.pocitaj.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun StarRatingDisplay(
    progress: Float,
    modifier: Modifier = Modifier,
    starColor: Color = Color(0xFFFFC107),
    emptyStarColor: Color = Color.Gray
) {
    val totalPoints = 15
    val filledPoints = (progress * totalPoints).roundToInt()

    Row(modifier = modifier) {
        for (i in 1..3) {
            val pointsInThisStar = (filledPoints - (i - 1) * 5).coerceIn(0, 5)
            val fillPercentage = pointsInThisStar / 5f

            Box(
                modifier = Modifier.size(24.dp),
                contentAlignment = Alignment.Center
            ) {
                PartiallyFilledStar(
                    fillPercentage = fillPercentage,
                    starColor = starColor,
                    emptyStarColor = emptyStarColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun PartiallyFilledStar(
    fillPercentage: Float,
    modifier: Modifier = Modifier,
    starColor: Color = Color(0xFFFFC107),
    emptyStarColor: Color = Color.Gray
) {
    val isFull = fillPercentage >= 1f
    val scale = remember { Animatable(1f) }

    // TODO: consider moving out
    LaunchedEffect(isFull) {
        if (isFull) {
            // Bounce: scale up significantly then settle at the "full" scale
            scale.animateTo(
                targetValue = 1.4f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
            scale.animateTo(
                targetValue = 1.15f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        } else {
            scale.snapTo(1f)
        }
    }

    val numFilledPoints = (fillPercentage * 5).toInt()

    Canvas(
        modifier = modifier.graphicsLayer {
            val s = scale.value
            scaleX = s
            scaleY = s
        }
    ) {
        val (starPath, pointPaths) = createStarWithPoints(size)

        // Draw the empty star background
        drawPath(path = starPath, color = emptyStarColor)

        if (isFull) {
            val shinyGradient = Brush.verticalGradient(
                colors = listOf(starColor.copy(alpha = 0.8f), starColor),
                startY = 0f,
                endY = size.height
            )
            drawPath(path = starPath, brush = shinyGradient)
        } else {
            // Draw filled points one by one
            for (i in 0 until numFilledPoints) {
                drawPath(path = pointPaths[i], color = starColor)
            }
        }
    }
}

private fun createStarWithPoints(size: Size): Pair<Path, List<Path>> {
    val scaleX = size.width / 24f
    val scaleY = size.height / 24f
    val centerX = 12f * scaleX
    val centerY = 12f * scaleY // Adjusted for better centering

    // Star outer points
    val p1 = Pair(12f * scaleX, 2f * scaleY)   // Top
    val p2 = Pair(18.18f * scaleX, 21f * scaleY) // Bottom-right
    val p3 = Pair(2f * scaleX, 9.24f * scaleY)  // Top-left (visual)
    val p4 = Pair(22f * scaleX, 9.24f * scaleY)  // Top-right (visual)
    val p5 = Pair(5.82f * scaleX, 21f * scaleY)  // Bottom-left

    // Star inner points
    val i1 = Pair(14.81f * scaleX, 8.63f * scaleY)
    val i2 = Pair(16.54f * scaleX, 13.97f * scaleY)
    val i3 = Pair(12f * scaleX, 17.27f * scaleY)
    val i4 = Pair(7.46f * scaleX, 13.97f * scaleY)
    val i5 = Pair(9.19f * scaleX, 8.63f * scaleY)

    val fullStarPath = Path().apply {
        moveTo(p1.first, p1.second)
        lineTo(i1.first, i1.second)
        lineTo(p4.first, p4.second)
        lineTo(i2.first, i2.second)
        lineTo(p2.first, p2.second)
        lineTo(i3.first, i3.second)
        lineTo(p5.first, p5.second)
        lineTo(i4.first, i4.second)
        lineTo(p3.first, p3.second)
        lineTo(i5.first, i5.second)
        close()
    }

    // Order: Top-left, Bottom-left, Bottom-right, Top-right, Top
    val pointTopLeft = createPointPath(centerX, centerY, p3, i5, i4)
    val pointBottomLeft = createPointPath(centerX, centerY, p5, i4, i3)
    val pointBottomRight = createPointPath(centerX, centerY, p2, i3, i2)
    val pointTopRight = createPointPath(centerX, centerY, p4, i2, i1)
    val pointTop = createPointPath(centerX, centerY, p1, i1, i5)

    val pointPaths =
        listOf(pointTopLeft, pointBottomLeft, pointBottomRight, pointTopRight, pointTop)

    return Pair(fullStarPath, pointPaths)
}

private fun createPointPath(
    cx: Float,
    cy: Float,
    p: Pair<Float, Float>,
    i1: Pair<Float, Float>,
    i2: Pair<Float, Float>
): Path {
    return Path().apply {
        moveTo(cx, cy)
        lineTo(i2.first, i2.second)
        lineTo(p.first, p.second)
        lineTo(i1.first, i1.second)
        close()
    }
}


@Preview(name = "0.0 Stars (0%)") @Composable fun P0() { StarRatingDisplay(progress = 0f) }
@Preview(name = "0.3 Stars (10%)") @Composable fun P10() { StarRatingDisplay(progress = 0.1f) }
@Preview(name = "0.6 Stars (20%)") @Composable fun P20() { StarRatingDisplay(progress = 0.2f) }
@Preview(name = "0.9 Stars (30%)") @Composable fun P30() { StarRatingDisplay(progress = 0.3f) }
@Preview(name = "1.2 Stars (40%)") @Composable fun P40() { StarRatingDisplay(progress = 0.4f) }
@Preview(name = "1.5 Stars (50%)") @Composable fun P50() { StarRatingDisplay(progress = 0.5f) }
@Preview(name = "1.8 Stars (60%)") @Composable fun P60() { StarRatingDisplay(progress = 0.6f) }
@Preview(name = "2.1 Stars (70%)") @Composable fun P70() { StarRatingDisplay(progress = 0.7f) }
@Preview(name = "2.4 Stars (80%)") @Composable fun P80() { StarRatingDisplay(progress = 0.8f) }
@Preview(name = "2.7 Stars (90%)") @Composable fun P90() { StarRatingDisplay(progress = 0.9f) }
@Preview(name = "3.0 Stars (100%)") @Composable fun P100() { StarRatingDisplay(progress = 1f) }