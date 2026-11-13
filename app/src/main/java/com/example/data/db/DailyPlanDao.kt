package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyPlanDao {
  @Query("SELECT * FROM daily_plan WHERE date = :date ORDER BY slotIndex ASC")
  fun getPlanForDate(date: String): Flow<List<DailyPlanEntity>>

  @Query("SELECT * FROM daily_plan WHERE date = :date ORDER BY slotIndex ASC")
  suspend fun getPlanForDateOnce(date: String): List<DailyPlanEntity>

  @Query("SELECT * FROM daily_plan WHERE date IN (:dates) ORDER BY date DESC, slotIndex ASC")
  suspend fun getPlansInDatesOnce(dates: List<String>): List<DailyPlanEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertPlanEntries(entries: List<DailyPlanEntity>)

  @Query(
    "UPDATE daily_plan SET missionId = :newMissionId, dimensionIndex = :newDimensionIndex, wasSwapped = 1 " +
      "WHERE date = :date AND slotIndex = :slotIndex"
  )
  suspend fun updatePlanSlot(
    date: String,
    slotIndex: Int,
    newMissionId: String,
    newDimensionIndex: Int
  ): Int

  @Query("DELETE FROM daily_plan WHERE date = :date")
  suspend fun deletePlanForDate(date: String)
}
