package com.castlefrog.shuffle

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.castlefrog.shuffle.ui.theme.ShuffleTheme
import com.castlefrog.shuffle.view.EmptyView
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
                        ) { innerPadding ->
                            ItemListView(
                                paddingValues = innerPadding,
                                itemData = mainView.selectedItems,
                            )
                        }
                    }
                    is MainViewModel.UiState.MainView.Loading -> FullScreenLoadingView()
                    is MainViewModel.UiState.MainView.Empty -> EmptyView()
                    is MainViewModel.UiState.MainView.EditListView -> {
                        TODO()
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
