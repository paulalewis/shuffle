package com.castlefrog.shuffle.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun LoadingView() {
    CircularProgressIndicator(
        modifier = Modifier.size(64.dp),
    )
}

@Composable
fun FullScreenLoadingView() {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LoadingView()
        }
    }
}

@Preview
@Composable
fun LoadingViewPreview() {
    LoadingView()
}

@Preview
@Composable
fun FullScreenLoadingViewPreview() {
    FullScreenLoadingView()
}
