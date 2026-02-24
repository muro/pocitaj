package dev.aidistillery.pocitaj.ui.history.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.aidistillery.pocitaj.logic.SmartHighlight
import dev.aidistillery.pocitaj.ui.theme.AppTheme

@Composable
fun SmartHighlightCard(
    highlight: SmartHighlight,
    modifier: Modifier = Modifier
) {
    // Map highlight type to colors and icons
    val (icon, backgroundColor, iconColor) = when (highlight) {
        is SmartHighlight.SpeedyPaws -> Triple(
            Icons.Filled.ElectricBolt,
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer
        )

        is SmartHighlight.LaserFocus -> Triple(
            Icons.Rounded.AutoAwesome,
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer
        )

        is SmartHighlight.PerfectPrecision -> Triple(
            Icons.Filled.WorkspacePremium,
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer
        )

        is SmartHighlight.Unstoppable -> Triple(
            Icons.Filled.Star,
            MaterialTheme.colorScheme.errorContainer, // Using error palette for orange/red
            MaterialTheme.colorScheme.onErrorContainer
        )
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor.copy(alpha = 0.6f)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(id = highlight.titleResId),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(
                        id = highlight.messageResId,
                        *highlight.formatArgs.toTypedArray()
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true, name = "Speedy")
@Composable
fun SmartHighlightCardSpeedyPreview() {
    AppTheme { Surface { SmartHighlightCard(highlight = SmartHighlight.SpeedyPaws(3)) } }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true, name = "Focus")
@Composable
fun SmartHighlightCardFocusPreview() {
    AppTheme { Surface { SmartHighlightCard(highlight = SmartHighlight.LaserFocus(42)) } }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true, name = "Perfect")
@Composable
fun SmartHighlightCardPerfectPreview() {
    AppTheme { Surface { SmartHighlightCard(highlight = SmartHighlight.PerfectPrecision()) } }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true, name = "Unstoppable")
@Composable
fun SmartHighlightCardUnstoppablePreview() {
    AppTheme { Surface { SmartHighlightCard(highlight = SmartHighlight.Unstoppable(100)) } }
}
