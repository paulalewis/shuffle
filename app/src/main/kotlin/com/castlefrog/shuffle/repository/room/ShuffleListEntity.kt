package com.castlefrog.shuffle.repository.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shuffle_lists")
data class ShuffleListEntity(
    @PrimaryKey val name: String,
    val subsetSize: Int,
    val createdAt: Long = System.currentTimeMillis(),
)
