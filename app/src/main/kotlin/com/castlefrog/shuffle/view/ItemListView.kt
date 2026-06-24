package com.castlefrog.shuffle.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.castlefrog.shuffle.R
import com.castlefrog.shuffle.model.ShuffleItem

@Composable
fun ItemListView(
    paddingValues: PaddingValues,
    itemData: SnapshotStateList<ShuffleItem>,
    currentItemCount: Int,
    totalItemCount: Int,
    onSelectItemListener: (index: Int) -> Unit = {},
    onIncreaseSubsetSize: () -> Unit = {},
    onDecreaseSubsetSize: () -> Unit = {},
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = paddingValues,
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = onDecreaseSubsetSize,
                    enabled = itemData.size > 1,
                ) {
                    Icon(Icons.Filled.Remove, contentDescription = stringResource(R.string.cd_show_fewer_items))
                }
                Text(
                    text = "$currentItemCount",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 8.dp),
                )
                IconButton(
                    onClick = onIncreaseSubsetSize,
                    enabled = itemData.size < totalItemCount,
                ) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.cd_show_more_items))
                }
            }
        }
        itemsIndexed(items = itemData) { index, item ->
            CardView(
                id = index,
                onSelectItemListener = onSelectItemListener,
            ) {
                ItemView(item)
            }
        }
    }
}

@Preview
@Composable
fun ItemListViewLightPreview() {
    Box(
        modifier = Modifier.fillMaxWidth().background(color = MaterialTheme.colorScheme.background),
    ) {
        ItemListView(
            paddingValues = PaddingValues(top = 50.dp),
            currentItemCount = 4,
            totalItemCount = 6,
            itemData = SnapshotStateList<ShuffleItem>().apply {
                addAll(
                    listOf(
                        ShuffleItem(text = "Push Ups"),
                        ShuffleItem(text = "Sit Ups"),
                        ShuffleItem(text = "Planks"),
                        ShuffleItem(text = "Pull Ups"),
                    )
                )
            }
        )
    }
}
