package com.codinglikeapirate.pocitaj

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.performTouchInput

object DrawingTestUtils {

    // Margin from canvas edges (e.g., 20% from each side)
    private const val MARGIN_PERCENT = 0.20f
    private const val DRAW_AREA_PERCENT = 1.0f - 2 * MARGIN_PERCENT // Area where drawing will occur

    // Helper to calculate actual drawing coordinates
    private fun getDrawingOffset(canvasWidth: Float, canvasHeight: Float, xPercent: Float, yPercent: Float): Offset {
        val effectiveCanvasWidth = canvasWidth * DRAW_AREA_PERCENT
        val effectiveCanvasHeight = canvasHeight * DRAW_AREA_PERCENT
        val offsetX = canvasWidth * MARGIN_PERCENT
        val offsetY = canvasHeight * MARGIN_PERCENT
        return Offset(offsetX + effectiveCanvasWidth * xPercent, offsetY + effectiveCanvasHeight * yPercent)
    }

    fun getPathForDigitOne(canvasWidth: Float, canvasHeight: Float): List<List<Offset>> {
        val start = getDrawingOffset(canvasWidth, canvasHeight, 0.5f, 0.1f) // Top-middle
        val end = getDrawingOffset(canvasWidth, canvasHeight, 0.5f, 0.9f)   // Bottom-middle
        return listOf(listOf(start, end))
    }

    fun getPathForDigitZero(canvasWidth: Float, canvasHeight: Float): List<List<Offset>> {
        // Approximate a circle with 4 swipes (a square/diamond shape)
        // Points are percentages of the drawing area
        val p1 = getDrawingOffset(canvasWidth, canvasHeight, 0.5f, 0.0f) // Top-center
        val p2 = getDrawingOffset(canvasWidth, canvasHeight, 1.0f, 0.5f) // Middle-right
        val p3 = getDrawingOffset(canvasWidth, canvasHeight, 0.5f, 1.0f) // Bottom-center
        val p4 = getDrawingOffset(canvasWidth, canvasHeight, 0.0f, 0.5f) // Middle-left
        
        return listOf(
            listOf(p1, p2),
            listOf(p2, p3),
            listOf(p3, p4),
            listOf(p4, p1)
        )
    }

    fun getPathForDigitTwo(canvasWidth: Float, canvasHeight: Float): List<List<Offset>> {
        // Approximate '2' with three strokes
        val p1 = getDrawingOffset(canvasWidth, canvasHeight, 0.1f, 0.2f) // Start of the curve
        val p2 = getDrawingOffset(canvasWidth, canvasHeight, 0.5f, 0.0f) // Top-middle (curve peak)
        val p3 = getDrawingOffset(canvasWidth, canvasHeight, 0.9f, 0.2f) // End of curve, start of diagonal
        val p4 = getDrawingOffset(canvasWidth, canvasHeight, 0.1f, 0.9f) // Bottom-left (end of diagonal)
        val p5 = getDrawingOffset(canvasWidth, canvasHeight, 0.9f, 0.9f) // Bottom-right (end of horizontal base)

        return listOf(
            listOf(p1, p2, p3), // Curve part
            listOf(p3, p4),     // Diagonal
            listOf(p4, p5)      // Horizontal base
        )
    }

    fun getPathForDigitFive(canvasWidth: Float, canvasHeight: Float): List<List<Offset>> {
        // Approximate '5' with three strokes
        val p1 = getDrawingOffset(canvasWidth, canvasHeight, 0.9f, 0.1f) // Top-right
        val p2 = getDrawingOffset(canvasWidth, canvasHeight, 0.1f, 0.1f) // Top-left (start of horizontal top bar)
        val p3 = getDrawingOffset(canvasWidth, canvasHeight, 0.1f, 0.5f) // Mid-left (end of vertical bar)
        val p4 = getDrawingOffset(canvasWidth, canvasHeight, 0.2f, 0.4f) // Start of curve
        val p5 = getDrawingOffset(canvasWidth, canvasHeight, 0.8f, 0.6f) // Mid-point of curve
        val p6 = getDrawingOffset(canvasWidth, canvasHeight, 0.9f, 0.9f) // End of curve (bottom-right-ish)
        val p7 = getDrawingOffset(canvasWidth, canvasHeight, 0.1f, 0.9f) // Bottom-left part of curve end

        return listOf(
            listOf(p1, p2),      // Top horizontal bar (right to left)
            listOf(p2, p3),      // Vertical bar
            listOf(p3, p4, p5, p6, p7) // Curve part
        )
    }
}

fun performStrokes(canvasNode: SemanticsNodeInteraction, strokes: List<List<Offset>>) {
    strokes.forEachIndexed { index, strokePoints ->
        if (strokePoints.size < 2) {
            // Log or handle cases with less than 2 points, as a swipe needs at least a start and end.
            // For now, we'll just skip if it's less than 1, the performTouchInput handles 1 point correctly (tap)
            // but for drawing, we usually expect at least 2 points.
            // The prompt says "If the list of points is empty or has only one point, log a warning or skip"
            // An empty list will cause a crash on .first(). A list with one point will perform a tap.
            // Let's stick to the prompt and skip if less than 2 points.
            if (strokePoints.isEmpty()) {
                // Optionally log: println("Skipping empty stroke.")
                return@forEachIndexed
            }
            if (strokePoints.size == 1) {
                // Optionally log: println("Skipping stroke with only one point (would be a tap): ${strokePoints.first()}")
                // To perform a tap for a single point, it would be:
                // canvasNode.performTouchInput { down(strokePoints.first()); up() }
                // But for "drawing" a stroke, we need at least two points.
                return@forEachIndexed
            }
        }

        canvasNode.performTouchInput {
            // Ensure there are points before proceeding
            // This check is now more robust due to the size check above
            down(strokePoints.first()) // Press down at the start of the stroke
            for (i in 1 until strokePoints.size) {
                moveTo(strokePoints[i]) // Move to subsequent points
            }
            up() // Lift up at the end of the stroke
        }

        if (index < strokes.size - 1) {
            Thread.sleep(150) // Delay between strokes
        }
    }
}
