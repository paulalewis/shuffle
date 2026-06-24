package com.castlefrog.shuffle.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.TextButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.castlefrog.shuffle.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeView(
    listNames: List<String> = emptyList(),
    selectedListName: String = "",
    hasItems: Boolean = false,
    onListSelected: (String) -> Unit = {},
    onEditClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    onRefreshClick: () -> Unit = {},
    onDeleteListClick: (String) -> Unit = {},
    onAddListClick: () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                listNames.forEach { name ->
                    NavigationDrawerItem(
                        label = { Text(name) },
                        selected = name == selectedListName,
                        onClick = {
                            onListSelected(name)
                            scope.launch { drawerState.close() }
                        },
                        shape = androidx.compose.material3.MaterialTheme.shapes.small,
                        badge = {
                            IconButton(onClick = { onDeleteListClick(name) }) {
                                Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.cd_delete, name))
                            }
                        },
                    )
                }
                TextButton(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    onClick = onAddListClick,
                ) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.cd_new_list))
                }
            }
        },
    ) {
        if (listNames.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxSize(),
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                ) {
                    FloatingActionButton(onClick = onAddListClick) {
                        Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.cd_add_list))
                    }
                }
            }
        } else {
            Scaffold(
                floatingActionButton = {
                    if (hasItems) {
                        FloatingActionButton(onClick = onRefreshClick) {
                            Icon(Icons.Filled.Refresh, contentDescription = stringResource(R.string.cd_refresh))
                        }
                    }
                },
                topBar = {
                    TopAppBar(
                        title = { Text(selectedListName) },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Filled.Menu, contentDescription = stringResource(R.string.cd_menu))
                            }
                        },
                        actions = {
                            IconButton(onClick = onEditClick) {
                                Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.cd_edit))
                            }
                            if (hasItems) {
                                IconButton(onClick = onShareClick) {
                                    Icon(Icons.Filled.Share, contentDescription = stringResource(R.string.cd_share))
                                }
                            }
                        },
                    )
                },
            ) { innerPadding ->
                if (hasItems) {
                    content(innerPadding)
                }
            }
        }
    }
}

@Preview
@Composable
fun HomeViewPreview() {
    HomeView(
        listNames = listOf("Morning Routine", "Workout", "Shopping"),
        selectedListName = "Workout",
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.Blue)
        ) {
            Text(
                modifier = Modifier.padding(it),
                text = "Content",
            )
        }
    }
}

@Preview
@Composable
fun EmptyHomeViewPreview() {
    HomeView(
        listNames = listOf(),
        selectedListName = "",
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.Blue)
        ) {
            Text(
                modifier = Modifier.padding(it),
                text = "Content",
            )
        }
    }
}
