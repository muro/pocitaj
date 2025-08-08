package dev.aidistillery.pocitaj.ui.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.aidistillery.pocitaj.R
import dev.aidistillery.pocitaj.data.User
import dev.aidistillery.pocitaj.ui.components.PocitajScreen
import dev.aidistillery.pocitaj.ui.theme.AppTheme

@Composable
fun UserProfileScreen(
    users: List<User>,
    onUserSelected: (Long) -> Unit,
    onAddUserClicked: (String) -> Unit
) {
    var showAddUserDialog by remember { mutableStateOf(false) }
    var newUserName by remember { mutableStateOf("") }

    PocitajScreen {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(stringResource(id = R.string.user_profile))
            LazyColumn {
                items(users) { user ->
                    Text(user.name)
                }
            }
            Button(onClick = { showAddUserDialog = true }) {
                Text(stringResource(id = R.string.add_user))
            }

            if (showAddUserDialog) {
                AlertDialog(
                    onDismissRequest = { showAddUserDialog = false },
                    title = { Text(stringResource(id = R.string.create_a_new_profile)) },
                    text = {
                        TextField(
                            value = newUserName,
                            onValueChange = { newUserName = it },
                            label = { Text(stringResource(id = R.string.user_name)) }
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                onAddUserClicked(newUserName)
                                showAddUserDialog = false
                            }
                        ) {
                            Text(stringResource(id = R.string.add))
                        }
                    },
                    dismissButton = {
                        Button(onClick = { showAddUserDialog = false }) {
                            Text(stringResource(id = R.string.cancel))
                        }
                    }
                )
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