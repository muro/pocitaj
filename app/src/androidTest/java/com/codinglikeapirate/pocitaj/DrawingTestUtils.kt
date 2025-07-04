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

    fun getDefaultDrawingPath(canvasWidth: Float, canvasHeight: Float): List<List<Offset>> {

        val p1 = getDrawingOffset(canvasWidth, canvasHeight, 0.4f, 0.3f) // Top-center
        val p2 = getDrawingOffset(canvasWidth, canvasHeight, 0.5f, 0.0f) // Middle-right
        val p3 = getDrawingOffset(canvasWidth, canvasHeight, 0.5f, 1.0f) // Bottom-center

        return listOf(
            listOf(p1, p2, p3)
        )
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
