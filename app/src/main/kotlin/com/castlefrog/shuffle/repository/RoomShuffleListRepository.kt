package com.castlefrog.shuffle.repository

import com.castlefrog.shuffle.model.ShuffleColor
import com.castlefrog.shuffle.model.ShuffleGroup
import com.castlefrog.shuffle.model.ShuffleItem
import com.castlefrog.shuffle.model.ShuffleList
import com.castlefrog.shuffle.repository.room.SelectedListEntity
import com.castlefrog.shuffle.repository.room.ShuffleListDao
import com.castlefrog.shuffle.repository.room.ShuffleListEntity
import com.castlefrog.shuffle.repository.room.ShuffleListWithItems
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single

class RoomShuffleListRepository(private val dao: ShuffleListDao) : ShuffleListRepository {

    override fun getAllShuffleListNames(): Flow<List<String>> = dao.getAllListNames()

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getCurrentSelectedList(): Flow<ShuffleList> =
        dao.getSelectedList()
            .filterNotNull()
            .flatMapLatest { dao.getListWithItems(it.listName) }
            .map { it.toDomain() }

    override fun setCurrentSelectedList(name: String): Flow<Unit> = flow {
        dao.upsertSelectedList(SelectedListEntity(listName = name))
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
