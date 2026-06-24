package com.castlefrog.shuffle

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.castlefrog.shuffle.ui.theme.ShuffleTheme
import com.castlefrog.shuffle.view.HomeView
import com.castlefrog.shuffle.view.ItemListView
import com.castlefrog.shuffle.viewmodel.MainViewModel
import com.castlefrog.shuffle.viewmodel.MainViewModelFactory

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(
            analyticsLogger = getAnalyticsLogger(),
            shuffleListRepository = getShuffleListRepository(),
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        splashScreen.setKeepOnScreenCondition {
            viewModel.uiState.value.mainView is MainViewModel.UiState.MainView.Loading
        }
        enableEdgeToEdge()
        setContent {
            val uiState by viewModel.uiState.collectAsState()

            ShuffleTheme {
                when (val mainView = uiState.mainView) {
                    is MainViewModel.UiState.MainView.ShuffleView -> {
                        HomeView(
                            listNames = mainView.allListNames,
                            selectedListName = mainView.selectedListName,
                            hasItems = mainView.selectedItems.isNotEmpty(),
                            onListSelected = { viewModel.handleUiEvent(MainViewModel.UiEvent.ChangeList(it)) },
                            onEditClick = { viewModel.handleUiEvent(MainViewModel.UiEvent.OpenEditList) },
                            onRefreshClick = { viewModel.handleUiEvent(MainViewModel.UiEvent.Refresh) },
                            onDeleteListClick = { viewModel.handleUiEvent(MainViewModel.UiEvent.RequestDeleteList(it)) },
                            onAddListClick = { viewModel.handleUiEvent(MainViewModel.UiEvent.OpenAddList) },
                        ) { innerPadding ->
                            ItemListView(
                                paddingValues = innerPadding,
                                itemData = mainView.selectedItems,
                                currentItemCount = mainView.selectedItems.size,
                                totalItemCount = mainView.totalItemCount,
                                onSelectItemListener = { index -> viewModel.handleUiEvent(MainViewModel.UiEvent.SelectItem(index)) },
                                onIncreaseSubsetSize = { viewModel.handleUiEvent(MainViewModel.UiEvent.IncreaseSubsetSize) },
                                onDecreaseSubsetSize = { viewModel.handleUiEvent(MainViewModel.UiEvent.DecreaseSubsetSize) },
                            )
                        }
                    }
                    is MainViewModel.UiState.MainView.Loading -> {}
                }

                val overlay = uiState.overlayView

                if (overlay is MainViewModel.UiState.OverlayView.EditListView) {
                    var showAddItemSheet by remember { mutableStateOf(false) }
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.handleUiEvent(MainViewModel.UiEvent.DismissBottomSheet) },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                    ) {
                        Column(modifier = Modifier.fillMaxHeight()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = overlay.list.name,
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                Button(onClick = { showAddItemSheet = true }) {
                                    Icon(Icons.Filled.Add, contentDescription = "add item")
                                }
                            }
                            HorizontalDivider()
                            LazyColumn(modifier = Modifier.weight(1f)) {
                                items(overlay.list.items) { item ->
                                    ListItem(
                                        headlineContent = { Text(item.text) },
                                        trailingContent = {
                                            IconButton(onClick = {
                                                viewModel.handleUiEvent(MainViewModel.UiEvent.DeleteItemFromList(item.text))
                                            }) {
                                                Icon(Icons.Filled.Delete, contentDescription = "Delete ${item.text}")
                                            }
                                        },
                                    )
                                    HorizontalDivider()
                                }
                            }
                        }
                    }

                    if (showAddItemSheet) {
                        var itemName by remember { mutableStateOf("") }
                        val focusRequester = remember { FocusRequester() }
                        ModalBottomSheet(
                            onDismissRequest = { showAddItemSheet = false },
                            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                                OutlinedTextField(
                                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                                    value = itemName,
                                    onValueChange = { itemName = it },
                                    singleLine = true,
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                Button(
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = itemName.isNotBlank(),
                                    onClick = {
                                        viewModel.handleUiEvent(MainViewModel.UiEvent.AddItemToList(itemName.trim()))
                                        showAddItemSheet = false
                                    },
                                ) {
                                    Icon(Icons.Filled.Add, contentDescription = "add item")
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                            }
                            LaunchedEffect(Unit) { focusRequester.requestFocus() }
                        }
                    }
                }

                if (overlay is MainViewModel.UiState.OverlayView.AddListView) {
                    var listName by remember { mutableStateOf("") }
                    val focusRequester = remember { FocusRequester() }
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.handleUiEvent(MainViewModel.UiEvent.DismissBottomSheet) },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                            OutlinedTextField(
                                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                                value = listName,
                                onValueChange = { listName = it },
                                singleLine = true,
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                modifier = Modifier.fillMaxWidth(),
                                enabled = listName.isNotBlank(),
                                onClick = { viewModel.handleUiEvent(MainViewModel.UiEvent.CreateNewList(listName.trim())) },
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = "add list")
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                        LaunchedEffect(Unit) {
                            focusRequester.requestFocus()
                        }
                    }
                }

                if (overlay is MainViewModel.UiState.OverlayView.ConfirmDeleteListView) {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.handleUiEvent(MainViewModel.UiEvent.DismissBottomSheet) },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                            Text(
                                text = "Delete \"${overlay.listName}\"",
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                OutlinedButton(
                                    modifier = Modifier.weight(1f),
                                    onClick = { viewModel.handleUiEvent(MainViewModel.UiEvent.DismissBottomSheet) },
                                ) {
                                    Text(stringResource(android.R.string.cancel))
                                }
                                Button(
                                    modifier = Modifier.weight(1f),
                                    onClick = { viewModel.handleUiEvent(MainViewModel.UiEvent.ConfirmDeleteList(overlay.listName)) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error,
                                    ),
                                ) {
                                    Text(stringResource(android.R.string.ok))
                                }
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }
        }
        viewModel.handleUiEvent(MainViewModel.UiEvent.Init)
    }
}
