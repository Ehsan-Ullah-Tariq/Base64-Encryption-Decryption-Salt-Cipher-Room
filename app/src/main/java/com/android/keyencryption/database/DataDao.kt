package com.android.keyencryption.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DataDao {

    @Query("SELECT * from data_table")
    fun getAllData(): List<DataTable>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertData(dataTable: DataTable)


    @Query("DELETE from data_table WHERE id = :id")
    suspend fun deleteCard(id: Int?)

}