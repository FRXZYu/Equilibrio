package com.example.data

import android.content.Context
import com.example.data.db.AppDatabase
import com.example.data.db.CompletionEntity
import com.example.data.db.MissionEntity
import com.example.domain.CompletionRecord
import com.example.domain.DayKey
import com.example.domain.HexagonCalculator
import com.example.model.AssessmentReport
import com.example.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

fun CompletionEntity.toDomain(): CompletionRecord {
  return CompletionRecord(
    id = id,
    missionId = missionId,
    dimensionIndex = dimensionIndex,
    expAwarded = expAwarded,
    date = date
  )
}

/**
 * Repositorio unificado que combina la base de datos Room (misiones creadas y completaciones históricas)
 * con el AssessmentRepository existente basado en SharedPreferences (cuestionario y perfil).
 */
class BalanceRepository(
  context: Context,
  val database: AppDatabase = AppDatabase.getInstance(context),
  val assessmentRepository: AssessmentRepository = AssessmentRepository(context),
  val dayKey: DayKey = DayKey.DEFAULT
) {

  val missionDao = database.missionDao()
  val completionDao = database.completionDao()

  // ==========================================
  // Misiones personalizadas (Room)
  // ==========================================
  val customMissions: Flow<List<MissionEntity>> = missionDao.getAllCustomMissions()

  suspend fun insertCustomMission(mission: MissionEntity) {
    missionDao.insertCustomMission(mission)
  }

  suspend fun deleteCustomMission(id: String) {
    missionDao.deleteCustomMissionById(id)
  }

  // ==========================================
  // Completaciones de Misiones (Room)
  // ==========================================
  val allCompletions: Flow<List<CompletionRecord>> = completionDao.getAllCompletions().map { list ->
    list.map { it.toDomain() }
  }

  /**
   * Flow de completaciones de los últimos 14 días (incluyendo hoy).
   */
  val completionsLast14Days: Flow<List<CompletionRecord>> = allCompletions.map { list ->
    val validDays = dayKey.getRangeLastDays(14).toSet()
    list.filter { it.date in validDays }
  }

  /**
   * Flow de los 6 puntajes reales calculados reactivamente con los últimos 14 días.
   */
  val realScores: Flow<Map<Int, Int>> = completionsLast14Days.map { completions14d ->
    HexagonCalculator.calculateRealScores(completions14d, dayKey)
  }

  /**
   * Flow de los 6 puntajes reales en precisión flotante para animaciones suaves.
   */
  val realScoresFloat: Flow<Map<Int, Float>> = completionsLast14Days.map { completions14d ->
    HexagonCalculator.calculateRealScoresFloat(completions14d, dayKey)
  }

  /**
   * Flow de los IDs de misiones completadas en el día de hoy.
   */
  val todayCompletedMissionIds: Flow<Set<String>> = completionDao.getCompletionsForDate(dayKey.today()).map { list ->
    list.map { it.missionId }.toSet()
  }

  /**
   * Flow reactivo de UserProfile con nivel, EXP, misiones y racha derivados 100% de Room.
   */
  val userProfileFlow: Flow<UserProfile> = allCompletions.map { completions ->
    val baseProfile = assessmentRepository.loadProfile()
    val today = dayKey.today()

    val totalExp = HexagonCalculator.calculateTotalExp(completions)
    val level = HexagonCalculator.calculateLevel(totalExp)
    val rank = HexagonCalculator.calculateRank(level)
    val expToday = completions.filter { it.date == today }.sumOf { it.expAwarded }
    val completedCount = completions.size
    val streak = HexagonCalculator.calculateStreak(completions, dayKey)

    baseProfile.copy(
      level = level,
      rank = rank,
      exp = totalExp,
      expToday = expToday,
      completedMissions = completedCount,
      activeStreakDays = streak
    )
  }

  /**
   * Marca o desmarca una misión para la fecha indicada (por defecto hoy).
   * Al desmarcarla, se elimina la fila en Room.
   * Retorna true si fue marcada como completada, o false si fue desmarcada.
   */
  suspend fun toggleMissionCompletion(
    missionId: String,
    dimensionIndex: Int,
    expReward: Int,
    date: String = dayKey.today()
  ): Boolean {
    val existing = completionDao.findCompletion(missionId, date)
    return if (existing != null) {
      completionDao.deleteCompletion(missionId, date)
      false
    } else {
      completionDao.insertCompletion(
        CompletionEntity(
          missionId = missionId,
          dimensionIndex = dimensionIndex,
          expAwarded = expReward,
          date = date
        )
      )
      true
    }
  }

  suspend fun deleteCompletion(missionId: String, date: String = dayKey.today()): Int {
    return completionDao.deleteCompletion(missionId, date)
  }

  suspend fun insertCompletion(
    missionId: String,
    dimensionIndex: Int,
    expAwarded: Int,
    date: String = dayKey.today()
  ): Long {
    return completionDao.insertCompletion(
      CompletionEntity(
        missionId = missionId,
        dimensionIndex = dimensionIndex,
        expAwarded = expAwarded,
        date = date
      )
    )
  }

  // ==========================================
  // Métodos heredados del AssessmentRepository
  // ==========================================
  fun hasCompletedAssessment(): Boolean = assessmentRepository.hasCompletedAssessment()
  fun hasPreviousAssessment(): Boolean = assessmentRepository.hasPreviousAssessment()
  fun saveScores(newScores: Map<Int, Int>) = assessmentRepository.saveScores(newScores)
  fun loadCurrentScores(): Map<Int, Int>? = assessmentRepository.loadCurrentScores()
  fun loadPreviousScores(): Map<Int, Int>? = assessmentRepository.loadPreviousScores()
  fun getSavedReport(): AssessmentReport? = assessmentRepository.getSavedReport()
  fun resetAssessment() = assessmentRepository.resetAssessment()

  fun isProfileCompleted(): Boolean = assessmentRepository.isProfileCompleted()
  fun saveProfile(profile: UserProfile) = assessmentRepository.saveProfile(profile)
  fun updateMonthlyGoal(goal: String) = assessmentRepository.updateMonthlyGoal(goal)
  fun loadProfile(): UserProfile = assessmentRepository.loadProfile()

  fun toggleSaveMissionForLater(missionId: String): Boolean =
    assessmentRepository.toggleSaveMissionForLater(missionId)
}
