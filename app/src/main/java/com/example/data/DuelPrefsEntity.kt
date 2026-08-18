package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "duel_prefs")
data class DuelPrefsEntity(
    @PrimaryKey val id: Int = 1,
    val player1Name: String = "",
    val player2Name: String = "",
    val player1Avatar: String = "👦",
    val player2Avatar: String = "👧",
    val matchDuration: Int = 60
)
