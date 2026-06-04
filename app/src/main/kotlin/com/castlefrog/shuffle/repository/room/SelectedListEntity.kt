package com.castlefrog.shuffle.repository.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "selected_list")
data class SelectedListEntity(
    @PrimaryKey val id: Int = 1,
    val listName: String,
)
