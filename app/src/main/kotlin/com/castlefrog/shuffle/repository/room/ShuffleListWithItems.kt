package com.castlefrog.shuffle.repository.room

import androidx.room.Embedded
import androidx.room.Relation

data class ShuffleListWithItems(
    @Embedded val list: ShuffleListEntity,
    @Relation(
        parentColumn = "name",
        entityColumn = "listName",
    )
    val items: List<ShuffleItemEntity>,
)
