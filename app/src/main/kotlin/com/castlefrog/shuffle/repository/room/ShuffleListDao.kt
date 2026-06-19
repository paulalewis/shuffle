package com.castlefrog.shuffle.repository.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ShuffleListDao {
    @Query("SELECT name FROM shuffle_lists ORDER BY createdAt ASC")
    fun getAllListNames(): Flow<List<String>>

    @Transaction
    @Query("SELECT * FROM shuffle_lists WHERE name = :name")
    fun getListWithItems(name: String): Flow<ShuffleListWithItems>

    @Query("SELECT * FROM selected_list WHERE id = 1")
    fun getSelectedList(): Flow<SelectedListEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertList(list: ShuffleListEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<ShuffleItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSelectedList(selected: SelectedListEntity)

    @Query("DELETE FROM shuffle_lists WHERE name = :name")
    suspend fun deleteList(name: String)

    @Query(
        """
        INSERT INTO shuffle_items (listName, text, groupLabel, groupColor, sortOrder)
        VALUES (:listName, :text, '', 'WHITE',
            (SELECT COALESCE(MAX(sortOrder) + 1, 0) FROM shuffle_items WHERE listName = :listName))
        """
    )
    suspend fun addItem(listName: String, text: String)

    @Query("DELETE FROM shuffle_items WHERE listName = :listName AND text = :text")
    suspend fun removeItem(listName: String, text: String)

    @Query("UPDATE shuffle_lists SET subsetSize = subsetSize + 1 WHERE name = :name")
    suspend fun incrementSubsetSize(name: String)

    @Query("UPDATE shuffle_lists SET subsetSize = MAX(1, subsetSize - 1) WHERE name = :name")
    suspend fun decrementSubsetSize(name: String)
}
