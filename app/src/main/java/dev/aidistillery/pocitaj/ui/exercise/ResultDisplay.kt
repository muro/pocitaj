package dev.aidistillery.pocitaj.ui.exercise

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.aidistillery.pocitaj.R

@Composable
fun ResultDisplay(
    answerResult: AnswerResult,
    showResultImage: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = showResultImage,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        val (resultImageRes, contentDesc) = when (answerResult) {
            is AnswerResult.Correct -> R.drawable.excited to R.string.correct_answer_image
            is AnswerResult.Incorrect -> R.drawable.sad to R.string.incorrect_answer_image
            is AnswerResult.Unrecognized -> R.drawable.confused to R.string.unrecognized_answer_image
            is AnswerResult.ShowCorrection -> R.drawable.teacher to R.string.teacher_image

            else -> null to null
        }

        if (resultImageRes != null && contentDesc != null) {
            val imageScale by animateFloatAsState(
                targetValue = if (showResultImage) 1.2f else 1f,
                label = "imageScale"
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val baseSize = 200.dp
                Image(
                    painter = painterResource(id = resultImageRes),
                    contentDescription = stringResource(contentDesc),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(baseSize * imageScale)
                )
                if (answerResult is AnswerResult.ShowCorrection) {
                    Text(
                        text = answerResult.equation,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                } else {
                    Spacer(modifier = Modifier.height(MaterialTheme.typography.headlineMedium.fontSize.value.dp))
                }
            }
        }
    }
}
