package com.castlefrog.shuffle.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.castlefrog.shuffle.model.ShuffleItem

@Composable
fun ItemListView(
    paddingValues: PaddingValues,
    itemData: SnapshotStateList<ShuffleItem>,
    onSelectItemListener: (index: Int) -> Unit = {},
    shareClickListener: () -> Unit = {},
    deleteClickListener: () -> Unit = {},
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            IconButton(
                onClick = shareClickListener,
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    tint = Color.White
                )
            }
        }
        LazyColumn(
            modifier = Modifier.fillMaxHeight(),
            contentPadding = paddingValues,
        ) {
            itemsIndexed(items = itemData) { index, item ->
                Column {
                    CardView(
                        id = index,
                        onSelectItemListener = onSelectItemListener,
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                        ) {
                            ItemView(item)
                            IconButton(
                                onClick = deleteClickListener,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = Color.Black
                                )
                            }
                        }
                    }
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
