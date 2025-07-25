package com.codinglikeapirate.pocitaj.ui.components

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign

/**
 * A Composable that adjusts its font size to fit its content within the available width.
 *
 * @param text The text to display.
 * @param modifier The modifier to be applied to this layout node.
 * @param style The text style to be applied to the text.
 */
@Composable
fun AutoSizeText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    style: TextStyle = LocalTextStyle.current,
    textAlign: TextAlign = TextAlign.Start
) {
    var scaledTextStyle by remember { mutableStateOf(style) }
    var readyToDraw by remember { mutableStateOf(false) }

    Text(
        text,
        modifier = modifier.drawWithContent {
            if (readyToDraw) {
                drawContent()
            }
        },
        color = color,
        style = scaledTextStyle,
        textAlign = textAlign,
        softWrap = false, // This is crucial to prevent wrapping and trigger overflow
        onTextLayout = { textLayoutResult ->
            // When the text overflows, scale it down and redraw
            if (textLayoutResult.didOverflowWidth) {
                scaledTextStyle = scaledTextStyle.copy(
                    fontSize = scaledTextStyle.fontSize * 0.9
                )
            } else {
                // If it fits, we are ready to draw
                readyToDraw = true
            }
        }
    )
}
