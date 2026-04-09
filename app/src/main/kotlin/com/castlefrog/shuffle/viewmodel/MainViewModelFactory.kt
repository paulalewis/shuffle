package com.castlefrog.shuffle.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.castlefrog.shuffle.analytics.AnalyticsLogger
import com.castlefrog.shuffle.repository.ShuffleListRepository

class MainViewModelFactory(
    private val analyticsLogger: AnalyticsLogger,
    private val shuffleListRepository: ShuffleListRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(analyticsLogger, shuffleListRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}