package com.android.keyencryption.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "data_table")
data class DataTable(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    @ColumnInfo(name = "generated_key") var generatedKey: String? = null,
    @ColumnInfo(name = "text_to_encrypt") var textToEncrypt: String? = null,
)