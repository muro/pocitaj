package dev.aidistillery.pocitaj.ui.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.aidistillery.pocitaj.data.User
import dev.aidistillery.pocitaj.ui.components.PocitajScreen
import dev.aidistillery.pocitaj.ui.theme.AppTheme

@Composable
fun UserProfileScreen(
    users: List<User>,
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
            LazyColumn {
                items(users) { user ->
                    Text(user.name)
                }
            }
        }
    }
}

@Preview
@Composable
fun UserProfileScreenPreview() {
    AppTheme {
        UserProfileScreen(
            users = listOf(User(id = 1, name = "Alice"), User(id = 2, name = "Bob")),
            onUserSelected = {},
            onAddUserClicked = {}
        )
    }
}
