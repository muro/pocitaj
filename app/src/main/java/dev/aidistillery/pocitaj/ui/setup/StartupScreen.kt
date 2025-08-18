package dev.aidistillery.pocitaj.ui.setup

import android.content.res.Configuration
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.aidistillery.pocitaj.R
import dev.aidistillery.pocitaj.ui.components.PocitajScreen
import dev.aidistillery.pocitaj.ui.theme.AppTheme

@Composable
fun StartupScreen(error: String?, onRetry: () -> Unit) {
    PocitajScreen {
        LaunchedEffect(Unit) {
            onRetry()
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (error == null) {
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val scale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulse"
                )
                Image(
                    painter = painterResource(id = R.drawable.smiling),
                    contentDescription = null,
                    modifier = Modifier
                        .size(128.dp)
                        .scale(scale)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    stringResource(id = R.string.loading),
                    color = MaterialTheme.colorScheme.onBackground
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.worrying),
                    contentDescription = null,
                    modifier = Modifier.size(128.dp)
                )
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                IconButton(onClick = onRetry) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = stringResource(id = R.string.retry),
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
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
fun PreviewStartupScreen() {
    AppTheme {
        StartupScreen(error = null) {}
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
fun PreviewStartupScreenWithError() {
    AppTheme {
        StartupScreen(error = "Something went wrong. This is a longer error message to see how it wraps.") {}
    }
}