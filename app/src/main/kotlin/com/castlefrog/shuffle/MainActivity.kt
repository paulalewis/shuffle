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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import com.castlefrog.shuffle.ui.theme.ShuffleTheme
import com.castlefrog.shuffle.view.FullScreenLoadingView
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
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val uiState by viewModel.uiState.collectAsState()

            ShuffleTheme {
                when (val mainView = uiState.mainView) {
                    is MainViewModel.UiState.MainView.ShuffleView -> {
                        HomeView(
                            listNames = mainView.allListNames,
                            selectedListName = mainView.selectedListName,
                            onListSelected = { viewModel.handleUiEvent(MainViewModel.UiEvent.ChangeList(it)) },
                            onRefreshClick = { viewModel.handleUiEvent(MainViewModel.UiEvent.Refresh) },
                            onDeleteListClick = { viewModel.handleUiEvent(MainViewModel.UiEvent.RequestDeleteList(it)) },
                            onAddListClick = { viewModel.handleUiEvent(MainViewModel.UiEvent.OpenAddList) },
                        ) { innerPadding ->
                            ItemListView(
                                paddingValues = innerPadding,
                                itemData = mainView.selectedItems,
                            )
                        }
                    }
                    is MainViewModel.UiState.MainView.Loading -> FullScreenLoadingView()
                    is MainViewModel.UiState.MainView.EditListView -> {
                        TODO()
                    }
                }

                val overlay = uiState.overlayView

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
                                label = { Text("List name") },
                                singleLine = true,
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                modifier = Modifier.fillMaxWidth(),
                                enabled = listName.isNotBlank(),
                                onClick = { viewModel.handleUiEvent(MainViewModel.UiEvent.CreateNewList(listName.trim())) },
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = "Add list")
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
                        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                            Text(
                                text = "Delete \"${overlay.listName}\"?",
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "This will permanently delete the list and all its items.",
                                style = MaterialTheme.typography.bodyMedium,
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
                                    Text("Cancel")
                                }
                                Button(
                                    modifier = Modifier.weight(1f),
                                    onClick = { viewModel.handleUiEvent(MainViewModel.UiEvent.ConfirmDeleteList) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error,
                                    ),
                                ) {
                                    Text("Delete")
                                }
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.handleUiEvent(MainViewModel.UiEvent.Init)
    }
}
