package com.castlefrog.shuffle

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.castlefrog.shuffle.ui.theme.ShuffleTheme
import com.castlefrog.shuffle.view.HomeView
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
            ShuffleTheme {
                HomeView {

                }
            }
        }
    }
}