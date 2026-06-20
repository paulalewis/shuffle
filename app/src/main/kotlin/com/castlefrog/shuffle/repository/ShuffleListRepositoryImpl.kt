package com.castlefrog.shuffle.repository

import android.content.SharedPreferences
import com.castlefrog.shuffle.model.ShuffleColor
import com.castlefrog.shuffle.model.ShuffleGroup
import com.castlefrog.shuffle.model.ShuffleItem
import com.castlefrog.shuffle.model.ShuffleList
import com.castlefrog.shuffle.repository.room.ShuffleListDao
import com.castlefrog.shuffle.repository.room.ShuffleListEntity
import com.castlefrog.shuffle.repository.room.ShuffleListWithItems
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import androidx.core.content.edit

private const val SELECTED_LIST_KEY = "selected_list"

class ShuffleListRepositoryImpl(
    private val dao: ShuffleListDao,
    private val sharedPreferences: SharedPreferences,
) : ShuffleListRepository {

    override fun getAllShuffleListNames(): Flow<List<String>> = dao.getAllListNames()

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getCurrentSelectedList(): Flow<String?> = flow {
        emit(sharedPreferences.getString(SELECTED_LIST_KEY, null))
    }

    override fun setCurrentSelectedList(name: String?): Flow<Unit> = flow {
        sharedPreferences.edit (true) { putString(SELECTED_LIST_KEY, name) }
        emit(Unit)
    }

    override fun incListSubsetSize(name: String): Flow<Unit> = flow {
        dao.incrementSubsetSize(name)
        emit(Unit)
    }

    override fun decListSubsetSize(name: String): Flow<Unit> = flow {
        dao.decrementSubsetSize(name)
        emit(Unit)
    }

    override fun getShuffleListByName(name: String): Flow<ShuffleList> =
        dao.getListWithItems(name).map { it.toDomain() }

    override fun createShuffleList(name: String): Flow<Unit> = flow {
        dao.insertList(ShuffleListEntity(name = name, subsetSize = 1))
        emit(Unit)
    }

    override fun deleteShuffleList(name: String): Flow<Unit> = flow {
        dao.deleteList(name)
        emit(Unit)
    }

    override fun addItemToShuffleList(name: String, item: String): Flow<Unit> = flow {
        dao.addItem(name, item)
        emit(Unit)
    }

    override fun removeItemFromShuffleList(name: String, item: String): Flow<Unit> = flow {
        dao.removeItem(name, item)
        emit(Unit)
    }
}

private fun ShuffleListWithItems.toDomain(): ShuffleList =
    ShuffleList(
        name = list.name,
        subsetSize = list.subsetSize,
        items = items.sortedBy { it.sortOrder }.map { entity ->
            ShuffleItem(
                text = entity.text,
                group = ShuffleGroup(
                    label = entity.groupLabel,
                    color = ShuffleColor.valueOf(entity.groupColor),
                ),
            )
        },
    )
