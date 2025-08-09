package dev.aidistillery.pocitaj.ui.profile

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.aidistillery.pocitaj.R
import dev.aidistillery.pocitaj.data.User
import dev.aidistillery.pocitaj.data.UserAppearance
import dev.aidistillery.pocitaj.data.UserDao
import dev.aidistillery.pocitaj.ui.components.PocitajScreen
import dev.aidistillery.pocitaj.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Composable
fun UserProfileScreen(
    users: List<User>,
    onUserSelected: (Long) -> Unit,
    onAddUserClicked: (String) -> Unit,
    onDeleteUserClicked: (User) -> Unit,
    initialShowAddUserDialog: Boolean = false,
    viewModel: UserProfileViewModel = viewModel(factory = UserProfileViewModelFactory)
) {
    var showAddUserDialog by remember { mutableStateOf(initialShowAddUserDialog) }
    var newUserName by remember { mutableStateOf("") }
    var editingUser by remember { mutableStateOf<User?>(null) }

    PocitajScreen {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(stringResource(id = R.string.user_profile), color = MaterialTheme.colorScheme.primary)
            LazyColumn(modifier = Modifier.testTag("user_profile_list")) {
                // TODO: ignore the default user, it should be invisible
                items(users) { user ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(top=8.dp, bottom = 8.dp)
                            .clickable {
                                onUserSelected(user.id)
                            },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val iconRes = UserAppearance.icons[user.iconId]
                        if (iconRes != null) {
                            Icon(
                                painter = painterResource(id = iconRes),
                                contentDescription = "${user.iconId} Icon",
                                tint = Color(user.color),
                                modifier = Modifier
                                    .size(30.dp)
                                    .semantics(mergeDescendants = false) {}
                                    .testTag("UserIcon_${user.name}_${user.iconId}")
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(user.name, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.primary)
                        IconButton(
                            onClick = { editingUser = user },
                            enabled = user.id != 1L
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit ${user.name}",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                        IconButton(
                            onClick = { onDeleteUserClicked(user) },
                            enabled = user.id != 1L
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "${stringResource(id = R.string.delete)} ${user.name}",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }

                    }

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
                                viewModel.addUser(newUserName)
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

            editingUser?.let { user ->
                EditUserAppearanceDialog(
                    user = user,
                    onDismiss = { editingUser = null },
                    onSave = { updatedUser ->
                        viewModel.updateUser(updatedUser)
                        editingUser = null
                    }
                )
            }
        }
    }
}

@Composable
fun EditUserAppearanceDialog(
    user: User,
    onDismiss: () -> Unit,
    onSave: (User) -> Unit
) {
    var selectedIconId by remember { mutableStateOf(user.iconId) }
    var selectedColor by remember { mutableStateOf(Color(user.color)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Appearance") },
        text = {
            Column {
                Text("Icon")
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 48.dp),
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    items(UserAppearance.icons.entries.toList()) { (iconId, iconRes) ->
                        IconButton(
                            onClick = { selectedIconId = iconId },
                            modifier = Modifier.testTag("icon_select_$iconId")
                        ) {
                            Icon(
                                painter = painterResource(id = iconRes),
                                contentDescription = "$iconId icon",
                                tint = if (selectedIconId == iconId) selectedColor else Color.Gray,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Color")
                LazyRow(modifier = Modifier.padding(top = 16.dp)) {
                    itemsIndexed(UserAppearance.colors) { index, color ->
                        IconButton(
                            onClick = { selectedColor = color },
                            modifier = Modifier.testTag("color_select_$index")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(color, CircleShape)
                                    .border(
                                        width = 2.dp,
                                        color = if (selectedColor == color) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                        shape = CircleShape
                                    )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(user.copy(iconId = selectedIconId, color = selectedColor.toArgb()))
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

class FakeUserDao : UserDao {
    private val users = mutableMapOf<Long, User>()
    private var nextId = 1L
    override suspend fun insert(user: User): Long {
        val idToInsert = user.id.takeIf { it != 0L } ?: nextId++
        users[idToInsert] = user.copy(id = idToInsert)
        return idToInsert
    }

    override suspend fun update(user: User) {
        users[user.id] = user.copy()
    }

    override suspend fun getUser(id: Long): User? = users[id]
    override suspend fun delete(user: User) {
        users.remove(user.id)
    }

    override suspend fun getUserByName(name: String): User? =
        users.values.find { it.name == name }

    override fun getAllUsers() = MutableStateFlow(users.values.toList()).asStateFlow()
}


class FakeUserProfileViewModel : UserProfileViewModel(FakeUserDao()) {
    override val users: StateFlow<List<User>> = MutableStateFlow(
        listOf(
            User(id = 1, name = "Alice"),
            User(id = 2, name = "Bob")
        )
    )
}

@SuppressLint("ViewModelConstructorInComposable")
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
fun UserProfileScreenPreview() {
    AppTheme {
        UserProfileScreen(
            users = listOf(
                User(id = 2, name = "Caleb", iconId = "bull", color = UserAppearance.colors[2].toArgb()),
                User(id = 3, name = "Dora", iconId = "owl", color = UserAppearance.colors[4].toArgb())),
            onUserSelected = {},
            onAddUserClicked = {},
            viewModel = FakeUserProfileViewModel(),
            onDeleteUserClicked = { },
            initialShowAddUserDialog = false
        )
    }
}

@SuppressLint("ViewModelConstructorInComposable")
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
fun UserProfileScreenAddUserDialogPreview() {
    AppTheme {
        UserProfileScreen(
            users = listOf(
                User(id = 2, name = "Alice", iconId = "jellyfish", color = UserAppearance.colors[5].toArgb()),
                User(id = 3, name = "Bob", iconId = "starfish", color = UserAppearance.colors[7].toArgb())),
            onUserSelected = {},
            onAddUserClicked = {},
            initialShowAddUserDialog = true,
            viewModel = FakeUserProfileViewModel(),
            onDeleteUserClicked = { }
        )
    }
}