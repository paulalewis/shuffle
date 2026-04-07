package com.castlefrog.shuffle.repository

import com.castlefrog.shuffle.model.ShuffleList
import kotlinx.coroutines.flow.Flow

interface ShuffleListRepository {
    fun getAllShuffleListNames(): Flow<List<String>>

    fun getShuffleListByName(name: String): Flow<ShuffleList>

    fun createShuffleList(name: String): Flow<Unit>

    fun deleteShuffleList(name: String): Flow<Unit>

    fun addItemToShuffleList(list: ShuffleList, item: String) : Flow<Unit>

    fun removeItemFromShuffleList(list: ShuffleList, item: String) : Flow<Unit>
}