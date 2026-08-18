package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "table_stats")
data class TableStatEntity(
    @PrimaryKey val number: Int, // 1 to 9
    val correctCount: Int = 0,
    val wrongCount: Int = 0
)
