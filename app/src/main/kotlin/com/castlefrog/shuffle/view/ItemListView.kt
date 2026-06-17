package com.castlefrog.shuffle.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.tooling.preview.Preview
import com.castlefrog.shuffle.model.ShuffleItem

@Composable
fun ItemListView(
    paddingValues: PaddingValues,
    itemData: SnapshotStateList<ShuffleItem>,
    onSelectItemListener: (index: Int) -> Unit = {},
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = paddingValues,
    ) {
        itemsIndexed(items = itemData) { index, item ->
            Column {
                CardView(
                    id = index,
                    onSelectItemListener = onSelectItemListener,
                ) {
                    ItemView(item)
                }
            }
        }
    }
}

@Preview
@Composable
fun ItemListViewLightPreview() {
    ItemListView(
        paddingValues = PaddingValues(top = 50.dp),
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
