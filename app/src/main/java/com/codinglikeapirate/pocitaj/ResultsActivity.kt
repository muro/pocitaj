package com.codinglikeapirate.pocitaj

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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
                Results(results)
            }
        }
    }

    @Composable
    fun Results(results: List<ResultDescription>, modifier: Modifier = Modifier) {
        Surface(
            modifier = modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            ResultsList(results)
        }
    }

    @Composable
    fun ResultsList(results: List<ResultDescription>, modifier: Modifier = Modifier) {
        LazyColumn {
            items(results) { result ->
                ResultCard(result, modifier)
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun PreviewResultsList() {
        val results = ArrayList<ResultDescription>()
        results.add(ResultDescription("2 + 2 = 4", ResultStatus.CORRECT))
        results.add(ResultDescription("3 + 3 ≠ 5", ResultStatus.INCORRECT))
        results.add(ResultDescription("3 + 3 = ?", ResultStatus.NOT_RECOGNIZED))

        AppTheme {
            Surface {
                ResultsList(results)
            }
        }
    }

    @Composable
    fun ResultCard(result: ResultDescription, modifier: Modifier = Modifier) {
        Surface(color = MaterialTheme.colorScheme.primary) {
            Row(
                modifier = modifier.padding(8.dp)
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(when (result.status) {
                        ResultStatus.CORRECT -> R.drawable.cat_heart
                        ResultStatus.INCORRECT -> R.drawable.cat_cry
                        ResultStatus.NOT_RECOGNIZED -> R.drawable.cat_big_eyes
                    }),
                    contentDescription = "Heart",
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 0.dp)
                        .fillMaxHeight()
                        .aspectRatio(1f)
                        .align(Alignment.CenterVertically)
                )
                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = result.equation,
                    //color = MaterialTheme.colorScheme.primary,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Light,// , textAlign = TextAlign.Center,
                    fontFamily = FontFamily.SansSerif,
                    modifier = Modifier.padding(16.dp).fillMaxWidth()
                )
            }
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
                ResultCard(ResultDescription("2 + 2 = 4", ResultStatus.CORRECT))
            }
        }
    }

    @Preview(
        widthDp = 320,
        heightDp = 480,
        showSystemUi = true,
        name = "Small phone"
    )
    @Preview(
        widthDp = 320,
        heightDp = 480,
        showSystemUi = true,
        uiMode = Configuration.UI_MODE_NIGHT_YES,
        name = "Small phone horizontal"
    )
    @Composable
    fun ResultsPreview() {
        val results = ArrayList<ResultDescription>()
        results.add(ResultDescription("2 + 2 = 4", ResultStatus.CORRECT))
        results.add(ResultDescription("3 + 3 ≠ 5", ResultStatus.INCORRECT))
        AppTheme {
            Results(results)
        }
    }

}