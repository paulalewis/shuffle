package com.castlefrog.shuffle.repository.room

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "shuffle_items",
    foreignKeys = [
        ForeignKey(
            entity = ShuffleListEntity::class,
            parentColumns = ["name"],
            childColumns = ["listName"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("listName")],
)
data class ShuffleItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val listName: String,
    val text: String,
    val groupLabel: String = "",
    val groupColor: String = "WHITE",
    val sortOrder: Int = 0,
)
