package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CompletionDao {
  @Query("SELECT * FROM completions WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC, completedAtMillis DESC")
  fun getCompletionsByDateRange(startDate: String, endDate: String): Flow<List<CompletionEntity>>

  @Query("SELECT * FROM completions WHERE date IN (:dates) ORDER BY date ASC")
  fun getCompletionsForDates(dates: List<String>): Flow<List<CompletionEntity>>

  @Query("SELECT * FROM completions WHERE date = :date ORDER BY completedAtMillis DESC")
  fun getCompletionsForDate(date: String): Flow<List<CompletionEntity>>

  @Query("SELECT * FROM completions ORDER BY completedAtMillis DESC, id DESC")
  fun getAllCompletions(): Flow<List<CompletionEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCompletion(completion: CompletionEntity): Long

  @Query(
    "UPDATE completions SET helpfulRating = :helpfulRating, reflectionNote = :reflectionNote " +
      "WHERE missionId = :missionId AND date = :date"
  )
  suspend fun updateCompletionFeedback(
    missionId: String,
    date: String,
    helpfulRating: String?,
    reflectionNote: String
  ): Int

  @Query("DELETE FROM completions WHERE missionId = :missionId AND date = :date")
  suspend fun deleteCompletion(missionId: String, date: String): Int

  @Query("SELECT * FROM completions WHERE missionId = :missionId AND date = :date LIMIT 1")
  suspend fun findCompletion(missionId: String, date: String): CompletionEntity?

  @Query("SELECT DISTINCT date FROM completions ORDER BY date ASC")
  fun getAllActiveDates(): Flow<List<String>>
}
