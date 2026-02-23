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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import dev.aidistillery.pocitaj.data.DailyActivityCount
import dev.aidistillery.pocitaj.data.ExerciseAttempt
import dev.aidistillery.pocitaj.data.ExerciseAttemptDao
import dev.aidistillery.pocitaj.data.User
import dev.aidistillery.pocitaj.data.UserAppearance
import dev.aidistillery.pocitaj.data.UserDao
import dev.aidistillery.pocitaj.ui.theme.AppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    onUserSelected: (Long) -> Unit,
    onBack: () -> Unit,
    initialShowAddUserDialog: Boolean = false,
    viewModel: UserProfileViewModel = viewModel(factory = UserProfileViewModelFactory)
) {
    val users by viewModel.users.collectAsState()
    val activeUser by viewModel.activeUser.collectAsState()
    var showAddUserDialog by remember { mutableStateOf(initialShowAddUserDialog) }
    var editingUser by remember { mutableStateOf<User?>(null) }
    var userToDelete by remember { mutableStateOf<User?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.user_profile)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            LazyColumn(modifier = Modifier.testTag("user_profile_list")) {
                items(users.filter { it.id != 1L }) { user ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 8.dp)
                            .clickable { onUserSelected(user.id) },
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
                        Text(
                            user.name,
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.primary
                        )
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
                            onClick = { userToDelete = user },
                            enabled = user.id != 1L && user.id != activeUser.id
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(R.string.delete, user.name),
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
                AddUserDialog(
                    onDismiss = { showAddUserDialog = false },
                    onAddUser = { name ->
                        viewModel.addUser(name)
                        showAddUserDialog = false
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

            userToDelete?.let { user ->
                DeleteUserDialog(
                    user = user,
                    onDismiss = { userToDelete = null },
                    onConfirmDelete = {
                        viewModel.deleteUser(user)
                        userToDelete = null
                    },
                    getAttemptCount = { viewModel.getAttemptCountForUser(user) }
                )
            }
        }
    }
}

@Suppress("AssignedValueIsNeverRead")
@Composable
fun AddUserDialog(
    onDismiss: () -> Unit,
    onAddUser: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var newUserName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier.testTag("add_user_dialog"),
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
                onClick = { onAddUser(newUserName) },
                enabled = newUserName.isNotBlank()
            ) {
                Text(stringResource(id = R.string.add))
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(stringResource(id = R.string.cancel))
            }
        }
    )
}

@Composable
fun DeleteUserDialog(
    user: User,
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit,
    getAttemptCount: suspend () -> Int
) {
    var attemptCount by remember { mutableStateOf<Int?>(null) }
    var confirmText by remember { mutableStateOf("") }

    LaunchedEffect(user) {
        attemptCount = getAttemptCount()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete '${user.name}'?") },
        text = {
            Column {
                val count = attemptCount
                if (count == null) {
                    Text("Loading...")
                } else if (count <= 10) {
                    Text("This profile has no significant progress. Are you sure you want to permanently delete it?")
                } else {
                    Text("This profile has a lot of progress! Deleting it will erase all their data permanently.")
                    Spacer(modifier = Modifier.height(16.dp))
                    TextField(
                        value = confirmText,
                        onValueChange = { confirmText = it },
                        label = { Text("Type '${user.name}' to confirm") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmDelete,
                enabled = attemptCount?.let { count ->
                    if (count <= 10) true else confirmText == user.name
                } ?: false
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EditUserAppearanceDialog(
    user: User,
    onDismiss: () -> Unit,
    onSave: (User) -> Unit
) {
    var selectedIconId by remember { mutableStateOf(user.iconId) }
    var selectedColor by remember { mutableStateOf(Color(user.color)) }
    var newName by remember { mutableStateOf(user.name) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Appearance") },
        text = {
            Column {
                TextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("User Name") },
                    modifier = Modifier.testTag("edit_user_name_field")
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Icon")
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 48.dp),
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    items(
                        UserAppearance.icons.entries.toList()
                            .filter { it.key != "robot" }) { (iconId, iconRes) ->
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
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 48.dp),
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    items(UserAppearance.colors.size - 1) { index ->
                        val color = UserAppearance.colors[index]
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
                onSave(
                    user.copy(
                        name = newName,
                        iconId = selectedIconId,
                        color = selectedColor.toArgb()
                    )
                )
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

    override fun getAllUsersFlow() = MutableStateFlow(users.values.toList()).asStateFlow()

    override suspend fun getUser(id: Long): User? = users[id]
    override suspend fun delete(user: User) {
        users.remove(user.id)
    }

    override suspend fun getUserByName(name: String): User? =
        users.values.find { it.name == name }

    override suspend fun getUserFlow(id: Long): User? = users[id]

    override fun getAllUsers(): StateFlow<List<User>> = getAllUsersFlow()
}

// TODO: there are multiple FakeExerciseAttemptDao classes now - unify them.
class FakeExerciseAttemptDao : ExerciseAttemptDao {
    private val attempts = mutableListOf<ExerciseAttempt>()
    override suspend fun insert(attempt: ExerciseAttempt) {
        attempts.add(attempt)
    }

    override fun getAttemptsForDate(userId: Long, dateString: String): Flow<List<ExerciseAttempt>> {
        return MutableStateFlow(attempts.filter {
            it.userId == userId &&
                    Instant.ofEpochMilli(it.timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
                        .toString() == dateString
        }).asStateFlow()
    }

    override fun getDailyActivityCounts(userId: Long): Flow<List<DailyActivityCount>> {
        val counts = attempts.filter { it.userId == userId }
            .groupBy {
                Instant.ofEpochMilli(it.timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
                    .toString()
            }
            .map { DailyActivityCount(it.key, it.value.size) }
        return MutableStateFlow(counts).asStateFlow()
    }

    override suspend fun getAttemptCountForUser(userId: Long): Int {
        return attempts.count { it.userId == userId }
    }
}


class FakeUserProfileViewModel(
    userDao: FakeUserDao,
    exerciseAttemptDao: ExerciseAttemptDao,
    activeUserManager: dev.aidistillery.pocitaj.data.ActiveUserManager
) : UserProfileViewModel(userDao, exerciseAttemptDao, activeUserManager) {
    override val users: StateFlow<List<User>> = userDao.getAllUsers()
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
    val caleb = User(
        id = 2,
        name = "Caleb",
        iconId = "bull",
        color = UserAppearance.colors[2].toArgb()
    )
    val dora = User(
        id = 3,
        name = "Dora",
        iconId = "owl",
        color = UserAppearance.colors[4].toArgb()
    )
    val userDao = FakeUserDao()
    runBlocking {
        userDao.insert(caleb)
        userDao.insert(dora)
    }
    val activeUserManager = dev.aidistillery.pocitaj.data.FakeActiveUserManager(caleb)
    AppTheme {
        UserProfileScreen(
            onUserSelected = {},
            onBack = {},
            viewModel = FakeUserProfileViewModel(
                userDao,
                FakeExerciseAttemptDao(),
                activeUserManager
            )
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
    val alice = User(
        id = 2,
        name = "Alice",
        iconId = "jellyfish",
        color = UserAppearance.colors[5].toArgb()
    )
    val bob = User(
        id = 3,
        name = "Bob",
        iconId = "starfish",
        color = UserAppearance.colors[7].toArgb()
    )
    val userDao = FakeUserDao()
    runBlocking {
        userDao.insert(alice)
        userDao.insert(bob)
    }
    val activeUserManager = dev.aidistillery.pocitaj.data.FakeActiveUserManager(alice)
    AppTheme {
        UserProfileScreen(
            onUserSelected = {},
            onBack = {},
            initialShowAddUserDialog = true,
            viewModel = FakeUserProfileViewModel(
                userDao,
                FakeExerciseAttemptDao(),
                activeUserManager
            )
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
fun AddUserDialogWithArchivedPreview() {
    AppTheme {
        AddUserDialog(
            onDismiss = {},
            onAddUser = {}
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
fun EditUserAppearanceDialogPreview() {
    AppTheme {
        EditUserAppearanceDialog(
            user = User(
                id = 2,
                name = "Caleb",
                iconId = "bull",
                color = UserAppearance.colors[2].toArgb()
            ),
            onDismiss = {},
            onSave = {}
        )
    }
}