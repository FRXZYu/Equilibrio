package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MissionDao {
  @Query("SELECT * FROM custom_missions ORDER BY title ASC")
  fun getAllCustomMissions(): Flow<List<MissionEntity>>

  @Query("SELECT * FROM custom_missions WHERE id = :id LIMIT 1")
  suspend fun getCustomMissionById(id: String): MissionEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCustomMission(mission: MissionEntity)

  @Query("DELETE FROM custom_missions WHERE id = :id")
  suspend fun deleteCustomMissionById(id: String)
}
