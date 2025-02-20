package com.codinglikeapirate.pocitaj

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class ResultsActivity : ComponentActivity() {

    companion object {
        const val EXERCISES_KEY = "exercises"
        const val RECOGNIZED_KEY = "recognized"
        const val CORRECTS_KEY = "corrects"
    }

    enum class ResultStatus {
        CORRECT, INCORRECT, NOT_RECOGNIZED;

        companion object {
            fun fromBooleanPair(recognized: Boolean, correct: Boolean): ResultStatus {
                return if (!recognized) {
                    NOT_RECOGNIZED
                } else {
                    if (correct) CORRECT else INCORRECT
                }
            }
        }
    }

    data class ResultDescription(val equation: String, val status: ResultStatus)

    private val results = ArrayList<ResultDescription>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val extras = intent.extras
        val exercises = extras?.getStringArray(EXERCISES_KEY) ?: emptyArray()
        val recognized = extras?.getBooleanArray(RECOGNIZED_KEY) ?: booleanArrayOf()
        val corrects = extras?.getBooleanArray(CORRECTS_KEY) ?: booleanArrayOf()

        results.clear()
        for (i in exercises.indices) {
            results.add(ResultDescription(exercises[i], ResultStatus.fromBooleanPair(recognized.getOrElse(i) { false }, corrects.getOrElse(i) { false })))
        }

        setContent {
            AppTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ResultsScreen(results)
                }
            }
        }
    }

    @Composable
    fun ResultsScreen(results: List<ResultDescription>) {
        LazyColumn {
            items(results) { result ->
                ResultCard(result)
            }
        }
    }

    @Preview
    @Composable
    fun PreviewResultsScreen() {
        val results = ArrayList<ResultDescription>()
        results.add(ResultDescription("2+2=4", ResultStatus.CORRECT))
        results.add(ResultDescription("3+3!=5", ResultStatus.INCORRECT))

        AppTheme {
            Surface(modifier=Modifier.background(MaterialTheme.colorScheme.onTertiaryContainer)) {
                ResultsScreen(results)
            }
        }
    }

    @Composable
    fun ResultCard(result: ResultDescription) {
        Row(modifier = Modifier.padding(all = 8.dp)) {
            Image(
                painter = painterResource(R.drawable.cat_heart),
                contentDescription = "Heart",
                modifier = Modifier
                    // Set image size to 40 dp
                    .size(20.dp)
                    // Clip image to be shaped as a circle
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))

            Text(text = result.equation,
                color = MaterialTheme.colorScheme.tertiary,
                fontSize = 20.sp, fontWeight = FontWeight.Light, textAlign = TextAlign.Center,
                fontFamily = FontFamily.SansSerif,
            )
        }
    }

    @Preview(
        uiMode = Configuration.UI_MODE_NIGHT_NO,
        showBackground = true,
        name = "Light Mode"
    )
    @Preview(
        uiMode = Configuration.UI_MODE_NIGHT_YES,
        showBackground = true,
        name = "Dark Mode"
    )
    @Composable
    fun PreviewResultCard() {
        AppTheme {
            Surface {
                ResultCard(ResultDescription("2+2=4", ResultStatus.CORRECT))
            }
        }
    }
}