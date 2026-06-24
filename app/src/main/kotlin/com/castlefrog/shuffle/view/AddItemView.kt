package com.castlefrog.shuffle.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemView(
    onDismissRequested: () -> Unit,
    onAddItemClicked: (text: String) -> Unit,
) {
    var itemName by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    ModalBottomSheet(
        onDismissRequest = { onDismissRequested() },
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
                onClick = { onAddItemClicked(itemName.trim()) },
            ) {
                Icon(Icons.Filled.Add, contentDescription = "add item")
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
        LaunchedEffect(Unit) { focusRequester.requestFocus() }
    }
}