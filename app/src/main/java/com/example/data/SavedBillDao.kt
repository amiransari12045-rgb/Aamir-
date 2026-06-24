package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedBillDao {
    @Query("SELECT * FROM saved_bills ORDER BY createdAt DESC")
    fun getAllBills(): Flow<List<SavedBill>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBill(bill: SavedBill): Long

    @Query("DELETE FROM saved_bills WHERE id = :id")
    suspend fun deleteBillById(id: Long)
}
