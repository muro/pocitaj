package com.codinglikeapirate.pocitaj

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.junit4.ComposeTestRule
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

        val p1 = getDrawingOffset(canvasWidth, canvasHeight, 0.4f, 0.3f) // Top-center
        val p2 = getDrawingOffset(canvasWidth, canvasHeight, 0.5f, 0.0f) // Middle-right
        val p3 = getDrawingOffset(canvasWidth, canvasHeight, 0.5f, 1.0f) // Bottom-center

        return listOf(
            listOf(p1, p2, p3)
        )
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

    fun getPathForScribble(canvasWidth: Float, canvasHeight: Float): List<List<Offset>> {
        // This uses the getDrawingOffset helper to respect existing margins.
        // A horizontal line across the middle of the drawing area.
        val start = getDrawingOffset(canvasWidth, canvasHeight, 0.0f, 0.5f) // Left-middle of drawing area
        val end = getDrawingOffset(canvasWidth, canvasHeight, 1.0f, 0.5f)   // Right-middle of drawing area
        return listOf(listOf(start, end))
    }

    fun performStrokes(rule: ComposeTestRule, canvasNode: SemanticsNodeInteraction, strokes: List<List<Offset>>) {
        rule.waitForIdle()
        strokes.forEachIndexed { index, strokePoints ->
            if (strokePoints.size < 2) {
                if (strokePoints.isEmpty()) {
                    return@forEachIndexed
                }
                if (strokePoints.size == 1) {
                    return@forEachIndexed
                }
            }

            canvasNode.performTouchInput {
                down(strokePoints.first())
                for (i in 1 until strokePoints.size) {
                    moveTo(strokePoints[i])
                }
                up()
            }

            if (index < strokes.size - 1) {
                rule.waitForIdle()
            }
        }
    }
}
