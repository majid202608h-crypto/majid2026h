package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stage_stars")
data class StageStarsEntity(
    @PrimaryKey val stageId: Int,
    val stars: Int
)
