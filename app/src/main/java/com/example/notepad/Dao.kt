package com.example.notepad

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface Dao {
    @Insert
    fun insertItem(item: Item)
    @Update
    fun updateItem(item: Item)
    @Delete
    fun deleteItem(item: Item)
    @Query("SELECT * FROM items")
    fun getAllItem(): Flow<List<Item>>
    @Query("SELECT * FROM items WHERE id = :itemId")
    fun getById(itemId: Int): Item?
    @Query("DELETE FROM items WHERE id = :itemId")
    fun deleteItemById(itemId: Int)
}