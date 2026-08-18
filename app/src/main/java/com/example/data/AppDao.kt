package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfileFlow(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getUserProfile(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(userProfile: UserProfileEntity)

    @Query("SELECT * FROM stage_stars")
    fun getStageStarsFlow(): Flow<List<StageStarsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStageStars(stageStars: StageStarsEntity)

    @Query("SELECT * FROM table_stats")
    fun getTableStatsFlow(): Flow<List<TableStatEntity>>

    @Query("SELECT * FROM table_stats WHERE number = :number LIMIT 1")
    suspend fun getTableStat(number: Int): TableStatEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTableStat(tableStat: TableStatEntity)

    @Query("SELECT * FROM duel_prefs WHERE id = 1 LIMIT 1")
    fun getDuelPrefsFlow(): Flow<DuelPrefsEntity?>

    @Query("SELECT * FROM duel_prefs WHERE id = 1 LIMIT 1")
    suspend fun getDuelPrefs(): DuelPrefsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDuelPrefs(duelPrefs: DuelPrefsEntity)

    @Query("DELETE FROM user_profile")
    suspend fun clearProfile()

    @Query("DELETE FROM stage_stars")
    suspend fun clearStageStars()

    @Query("DELETE FROM table_stats")
    suspend fun clearTableStats()
}
