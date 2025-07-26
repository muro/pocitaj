package com.codinglikeapirate.pocitaj.ui.exercise

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.dp
import com.codinglikeapirate.pocitaj.R

@Composable
fun ResultDisplay(
    answerResult: AnswerResult,
    showResultImage: Boolean,
    correctAnswer: String,
    isTrainingMode: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = showResultImage,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        val resultImageRes = when (answerResult) {
            is AnswerResult.Correct -> R.drawable.cat_heart
            is AnswerResult.Incorrect -> R.drawable.cat_cry
            is AnswerResult.Unrecognized -> R.drawable.cat_big_eyes
            else -> null
        }

        val contentDesc = when (answerResult) {
            is AnswerResult.Correct -> "Correct Answer Image"
            is AnswerResult.Incorrect -> "Incorrect Answer Image"
            is AnswerResult.Unrecognized -> "Unrecognized Answer Image"
            else -> null
        }

        if (resultImageRes != null && contentDesc != null) {
            val imageScale by animateFloatAsState(
                targetValue = if (showResultImage) 1.2f else 1f,
                label = "imageScale"
            )
            val alphaScale by animateFloatAsState(
                targetValue = if (showResultImage) 1f else 0f,
                label = "alphaScale"
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    bitmap = ImageBitmap.imageResource(id = resultImageRes),
                    contentDescription = contentDesc,
                    modifier = Modifier
                        .size(200.dp)
                        .graphicsLayer {
                            scaleX = imageScale
                            scaleY = imageScale
                            alpha = alphaScale
                        }
                )
                if (isTrainingMode && answerResult is AnswerResult.Incorrect) {
                    Text(
                        text = "Correct answer: $correctAnswer",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
