package dev.aidistillery.pocitaj.ui.theme

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.glow(
    color: Color,
    borderRadius: Dp = 0.dp,
    radius: Dp = 20.dp,
    spread: Dp = 0.dp
) = this.drawBehind {
    this.drawIntoCanvas {
        val paint = Paint()
        val frameworkPaint = paint.asFrameworkPaint()
        frameworkPaint.color = Color.Transparent.toArgb()
        frameworkPaint.setShadowLayer(
            radius.toPx(),
            0f,
            0f,
            color.toArgb()
        )
        it.drawRoundRect(
            0f - spread.toPx(),
            0f - spread.toPx(),
            this.size.width + spread.toPx(),
            this.size.height + spread.toPx(),
            borderRadius.toPx(),
            borderRadius.toPx(),
            paint
        )
    }
}
