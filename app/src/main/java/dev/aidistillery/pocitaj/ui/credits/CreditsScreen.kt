package dev.aidistillery.pocitaj.ui.credits

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.ui.compose.LibraryDefaults
import com.mikepenz.aboutlibraries.ui.compose.android.produceLibraries
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import com.mikepenz.aboutlibraries.ui.compose.m3.chipColors
import com.mikepenz.aboutlibraries.ui.compose.m3.libraryColors
import dev.aidistillery.pocitaj.ui.theme.AppFontFamily
import dev.aidistillery.pocitaj.ui.theme.AppTheme

@Composable
fun CreditsScreen(onNavigateUp: () -> Unit) {
    val libraries by produceLibraries()
    CreditsScreenContent(
        libraries = libraries,
        onNavigateUp = onNavigateUp
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditsScreenContent(
    libraries: Libs?,
    onNavigateUp: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Open Source Licenses") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LibrariesContainer(
            libraries = libraries,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            textStyles = LibraryDefaults.libraryTextStyles(
                nameTextStyle = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = AppFontFamily
                ),
                versionTextStyle = MaterialTheme.typography.titleSmall.copy(
                    fontFamily = AppFontFamily
                ),
                licensesTextStyle = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = AppFontFamily
                )
            ),
            colors = LibraryDefaults.libraryColors(
                licenseChipColors = LibraryDefaults.chipColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            ),
            padding = LibraryDefaults.libraryPadding(
                contentPadding = PaddingValues(8.dp),
                licensePadding = LibraryDefaults.chipPadding(containerPadding = PaddingValues(0.dp)),
                verticalPadding = 0.dp
            )

        )
    }
}

private const val previewJson = """
{
"licenses": {
  "Apache-2.0": {
    "name": "Apache License 2.0",
    "url": "http://www.apache.org/licenses/LICENSE-2.0"
  }
},
  "libraries": [
    {
      "uniqueId": "androidx.activity:activity",
      "name": "Activity",
      "description": "Provides the base Activity subclass and the relevant hooks to build a composable structure on top.",
      "artifactVersion": "1.10.1",
      "website": "https://developer.android.com/jetpack/androidx/releases/activity#1.10.1",
      "developers": [],
      "licenses": ["Apache-2.0"]
    },
    {
      "uniqueId": "androidx.activity:activity-compose",
      "name": "Activity Compose",
      "description": "Compose integration with Activity",
      "artifactVersion": "1.10.1",
      "website": "https://developer.android.com/jetpack/androidx/releases/activity#1.10.1",
      "developers": [],
      "licenses": ["Apache-2.0"]
    }
  ]
}
"""

@Preview
@Composable
fun CreditsScreenPreview() {
    val previewLibraries = Libs.Builder().withJson(previewJson).build()
    AppTheme {
        CreditsScreenContent(
            libraries = previewLibraries,
            onNavigateUp = {}
        )
    }
}