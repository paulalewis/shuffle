package com.castlefrog.shuffle.repository

import com.castlefrog.shuffle.model.ShuffleList
import kotlinx.coroutines.flow.Flow

interface ShuffleListRepository {
    fun getAllShuffleListNames(): Flow<List<String>>

    fun getCurrentSelectedList(): Flow<ShuffleList>

    fun setCurrentSelectedList(name: String): Flow<Unit>

    fun getShuffleListByName(name: String): Flow<ShuffleList>

    fun createShuffleList(name: String): Flow<Unit>

    fun deleteShuffleList(name: String): Flow<Unit>

    fun addItemToShuffleList(name: String, item: String) : Flow<Unit>

    fun removeItemFromShuffleList(name: String, item: String) : Flow<Unit>
}