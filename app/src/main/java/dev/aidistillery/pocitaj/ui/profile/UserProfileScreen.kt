package dev.aidistillery.pocitaj.ui.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.aidistillery.pocitaj.ui.components.PocitajScreen

@Composable
fun UserProfileScreen(
    onUserSelected: (Long) -> Unit,
    onAddUserClicked: () -> Unit
) {
    PocitajScreen {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("User Profiles")
        }
    }
}
