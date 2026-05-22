package com.castlefrog.shuffle.repository

import com.castlefrog.shuffle.model.ShuffleItem
import com.castlefrog.shuffle.model.ShuffleList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class InMemoryShuffleListRepository : ShuffleListRepository {
    private val lists = MutableStateFlow<Map<String, ShuffleList>>(
        listOf(
            ShuffleList(
                name = "Farm Animals",
                subsetSize = 1,
                items = listOf("Cow", "Pig", "Chicken", "Horse", "Sheep", "Goat", "Duck", "Turkey").map { ShuffleItem(text = it) },
            ),
            ShuffleList(
                name = "Car Models",
                subsetSize = 1,
                items = listOf("Toyota Camry", "Ford F-150", "Honda Civic", "Chevrolet Silverado", "Tesla Model 3", "BMW 3 Series", "Toyota Corolla", "Volkswagen Golf").map { ShuffleItem(text = it) },
            ),
        ).associateBy { it.name }
    )

    private var selectedListIndex = 0

    override fun getAllShuffleListNames(): Flow<List<String>> {
        return lists.map { it.keys.toList() }
    }

    override fun getCurrentSelectedList(): Flow<ShuffleList> {
        return lists.map { it.values.toList()[selectedListIndex] }
    }

    override fun setCurrentSelectedList(name: String): Flow<Unit> = flow {
        selectedListIndex = lists.value.keys.indexOf(name)
        emit(Unit)
    }

    override fun getShuffleListByName(name: String): Flow<ShuffleList> {
        return lists.map { it.getValue(name) }
    }

    override fun createShuffleList(name: String): Flow<Unit> = flow {
        lists.value += (name to ShuffleList(name = name, subsetSize = 1, items = emptyList()))
        emit(Unit)
    }

    override fun deleteShuffleList(name: String): Flow<Unit> = flow {
        lists.value -= name
        emit(Unit)
    }

    override fun addItemToShuffleList(name: String, item: String): Flow<Unit> = flow {
        val list = lists.value.getValue(name)
        lists.value += (name to list.copy(items = list.items + ShuffleItem(text = item)))
        emit(Unit)
    }

    override fun removeItemFromShuffleList(name: String, item: String): Flow<Unit> = flow {
        val list = lists.value.getValue(name)
        lists.value += (name to list.copy(items = list.items.filter { it.text != item }))
        emit(Unit)
    }
}
