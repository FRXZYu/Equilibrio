package com.example.data

import android.content.Context
import com.example.data.db.AppDatabase
import com.example.data.db.CompletionEntity
import com.example.data.db.DailyPlanEntity
import com.example.data.db.MissionEntity
import com.example.domain.CompletionRecord
import com.example.domain.DailyMissionPlanner
import com.example.domain.DailyPlanHistoryItem
import com.example.domain.DayKey
import com.example.domain.HelpfulnessRating
import com.example.domain.HelpfulnessSummary
import com.example.domain.HexagonCalculator
import com.example.model.AssessmentReport
import com.example.model.BalanceMission
import com.example.model.DimensionsProvider
import com.example.model.GamificationStats
import com.example.model.MissionsProvider
import com.example.model.UserProfile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

fun CompletionEntity.toDomain(): CompletionRecord {
  return CompletionRecord(
    id = id,
    missionId = missionId,
    dimensionIndex = dimensionIndex,
    date = date,
    helpfulness = HelpfulnessRating.fromStorage(helpfulRating),
    reflectionNote = reflectionNote,
    completedAtMillis = completedAtMillis,
    expAwarded = expAwarded
  )
}

/**
 * Repositorio unificado que combina Room (acciones personalizadas, historial con valoración
 * de utilidad y plan diario a las 00:00) con AssessmentRepository (cuestionario, enfoque y perfil).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BalanceRepository(
  context: Context,
  val database: AppDatabase = AppDatabase.getInstance(context),
  val assessmentRepository: AssessmentRepository = AssessmentRepository(context),
  val dayKey: DayKey = DayKey.DEFAULT
) {

  val missionDao = database.missionDao()
  val completionDao = database.completionDao()
  val dailyPlanDao = database.dailyPlanDao()

  private val planMutex = Mutex()
  private val _currentDayKey = MutableStateFlow(dayKey.today())
  val currentDayKey: StateFlow<String> = _currentDayKey.asStateFlow()

  /**
   * Comprueba si el reloj local cruzó las 00:00 y actualiza el flujo reactivo del día actual.
   * Retorna true si cambió de día.
   */
  fun refreshDayIfNeeded(): Boolean {
    val today = dayKey.today()
    return if (_currentDayKey.value != today) {
      _currentDayKey.value = today
      true
    } else {
      false
    }
  }

  // ==========================================
  // Misiones y hábitos personalizados (Room)
  // ==========================================
  val customMissions: Flow<List<MissionEntity>> = missionDao.getAllCustomMissions()

  suspend fun insertCustomMission(mission: MissionEntity) {
    missionDao.insertCustomMission(mission)
  }

  suspend fun deleteCustomMission(id: String) {
    missionDao.deleteCustomMissionById(id)
  }

  // ==========================================
  // Áreas de enfoque elegidas y guardadas
  // ==========================================
  val chosenFocusDimensions: Flow<Set<Int>> = assessmentRepository.chosenFocusDimensionsFlow
  val savedMissionIds: Flow<Set<String>> = assessmentRepository.savedMissionIdsFlow

  fun saveFocusDimensions(dimensions: Set<Int>) {
    assessmentRepository.saveFocusDimensions(dimensions)
  }

  fun toggleFocusDimension(dimensionIndex: Int): Set<Int> {
    return assessmentRepository.toggleFocusDimension(dimensionIndex)
  }

  // ==========================================
  // Historial completo, últimos 14 días y hoy
  // ==========================================
  val allCompletions: Flow<List<CompletionRecord>> = completionDao.getAllCompletions().map { list ->
    list.map { it.toDomain() }
  }

  val completionsLast14Days: Flow<List<CompletionRecord>> = combine(allCompletions, currentDayKey) { list, _ ->
    val validDays = dayKey.getRangeLastDays(14).toSet()
    list.filter { it.date in validDays }
  }

  val realScores: Flow<Map<Int, Int>> = combine(completionsLast14Days, currentDayKey) { completions14d, _ ->
    HexagonCalculator.calculateRealScores(completions14d, dayKey)
  }

  val realScoresFloat: Flow<Map<Int, Float>> = combine(completionsLast14Days, currentDayKey) { completions14d, _ ->
    HexagonCalculator.calculateRealScoresFloat(completions14d, dayKey)
  }

  val actionsByDimension14d: Flow<Map<Int, Int>> = combine(completionsLast14Days, currentDayKey) { completions14d, _ ->
    HexagonCalculator.calculateActionsByDimension14d(completions14d, dayKey)
  }

  val expByDimension14d: Flow<Map<Int, Int>> = combine(completionsLast14Days, currentDayKey) { completions14d, _ ->
    HexagonCalculator.calculateExpByDimension14d(completions14d, dayKey)
  }

  val helpfulnessSummary: Flow<HelpfulnessSummary> = allCompletions.map { completions ->
    HexagonCalculator.calculateHelpfulnessSummary(completions)
  }

  val todayCompletions: Flow<List<CompletionRecord>> = currentDayKey.flatMapLatest { date ->
    completionDao.getCompletionsForDate(date).map { list -> list.map { it.toDomain() } }
  }

  val todayCompletedMissionIds: Flow<Set<String>> = todayCompletions.map { list ->
    list.map { it.missionId }.toSet()
  }

  val todayDailyPlan: Flow<List<DailyPlanEntity>> = currentDayKey.flatMapLatest { date ->
    dailyPlanDao.getPlanForDate(date)
  }

  val userProfileFlow: Flow<UserProfile> = combine(
    allCompletions,
    assessmentRepository.baseProfileFlow,
    currentDayKey
  ) { completions, baseProfile, todayDate ->
    val activeDays = HexagonCalculator.calculateActiveDaysLast14(completions, dayKey)
    val streak = HexagonCalculator.calculateStreak(completions, dayKey)
    val totalExp = HexagonCalculator.calculateTotalExp(completions)
    val expToday = HexagonCalculator.calculateExpOnDate(completions, todayDate)
    val stats = GamificationStats(totalExp = totalExp, expToday = expToday)
    baseProfile.copy(
      level = stats.level,
      rank = stats.rank,
      exp = totalExp,
      expToday = expToday,
      completedMissions = completions.size,
      activeStreakDays = streak,
      completedActionsTotal = completions.size,
      activeDaysRecent = activeDays
    )
  }

  // ==========================================
  // Plan diario automático a las 00:00
  // ==========================================

  private suspend fun buildFullCatalog(perceivedScores: Map<Int, Int>): List<BalanceMission> {
    val focus = assessmentRepository.chosenFocusDimensionsFlow.value
    val base = MissionsProvider.generateMissionsForUser(perceivedScores, focus)
    val custom = missionDao.getAllCustomMissions().first().map { it.toBalanceMission() }
    return (custom + base).distinctBy { it.id }
  }

  /**
   * Garantiza que exista un plan personalizado de hasta 3 acciones concretas para la fecha actual.
   * Si cambió de día (00:00 hora local) o aún no hay plan hoy, genera uno evitando repeticiones.
   */
  suspend fun ensureDailyPlanForToday(perceivedScores: Map<Int, Int>): List<DailyPlanEntity> {
    refreshDayIfNeeded()
    val today = dayKey.today()
    return planMutex.withLock {
      val existing = dailyPlanDao.getPlanForDateOnce(today)
      val catalog = buildFullCatalog(perceivedScores)
      val catalogIds = catalog.map { it.id }.toSet()
      val validExisting = existing.filter { it.missionId in catalogIds }

      if (validExisting.isNotEmpty() && validExisting.size == existing.size) {
        return@withLock validExisting
      }

      val focusDims = assessmentRepository.chosenFocusDimensionsFlow.value
      val savedIds = assessmentRepository.savedMissionIdsFlow.value
      val recentDays = dayKey.getRangeLastDays(14)
      val recentCompletions = completionDao.getCompletionsForDates(recentDays).first().map { it.toDomain() }
      val real = HexagonCalculator.calculateRealScores(recentCompletions, dayKey)

      val recentPlanDays = dayKey.getRangeLastDays(DailyMissionPlanner.OFFERED_COOLDOWN_DAYS + 1)
      val planHistory = dailyPlanDao.getPlansInDatesOnce(recentPlanDays).map {
        DailyPlanHistoryItem(date = it.date, missionId = it.missionId, slotIndex = it.slotIndex)
      }

      val planned = DailyMissionPlanner.planDailyMissions(
        todayKey = today,
        catalog = catalog.map { it.toCandidate() },
        chosenFocusDimensions = focusDims,
        perceivedScores = perceivedScores,
        realScores = real,
        recentCompletions = recentCompletions,
        recentPlanHistory = planHistory,
        savedMissionIds = savedIds,
        dayKey = dayKey
      )

      val entities = planned.mapIndexed { idx, candidate ->
        DailyPlanEntity(
          date = today,
          slotIndex = idx,
          missionId = candidate.id,
          dimensionIndex = candidate.dimensionIndex,
          wasSwapped = false
        )
      }
      dailyPlanDao.deletePlanForDate(today)
      if (entities.isNotEmpty()) {
        dailyPlanDao.insertPlanEntries(entities)
      }
      entities
    }
  }

  /**
   * Regenera las acciones no completadas de hoy cuando la persona cambia sus áreas de enfoque.
   */
  suspend fun replanTodayForFocusChange(perceivedScores: Map<Int, Int>): List<DailyPlanEntity> {
    refreshDayIfNeeded()
    val today = dayKey.today()
    return planMutex.withLock {
      val existing = dailyPlanDao.getPlanForDateOnce(today)
      val todayCompleted = completionDao.getCompletionsForDate(today).first().map { it.missionId }.toSet()
      val keptCompleted = existing.filter { it.missionId in todayCompleted }

      val catalog = buildFullCatalog(perceivedScores)
      val focusDims = assessmentRepository.chosenFocusDimensionsFlow.value
      val savedIds = assessmentRepository.savedMissionIdsFlow.value
      val recentDays = dayKey.getRangeLastDays(14)
      val recentCompletions = completionDao.getCompletionsForDates(recentDays).first().map { it.toDomain() }
      val real = HexagonCalculator.calculateRealScores(recentCompletions, dayKey)
      val recentPlanDays = dayKey.getRangeLastDays(DailyMissionPlanner.OFFERED_COOLDOWN_DAYS + 1)
      val planHistory = dailyPlanDao.getPlansInDatesOnce(recentPlanDays).map {
        DailyPlanHistoryItem(date = it.date, missionId = it.missionId, slotIndex = it.slotIndex)
      }

      val availableCatalog = catalog.filter { it.id !in todayCompleted }.map { it.toCandidate() }
      val fresh = DailyMissionPlanner.planDailyMissions(
        todayKey = today,
        catalog = availableCatalog,
        chosenFocusDimensions = focusDims,
        perceivedScores = perceivedScores,
        realScores = real,
        recentCompletions = recentCompletions,
        recentPlanHistory = planHistory,
        savedMissionIds = savedIds,
        dayKey = dayKey
      )

      val neededSlots = (0 until DailyMissionPlanner.DAILY_ACTION_COUNT).filter { slot ->
        keptCompleted.none { it.slotIndex == slot }
      }
      val filled = neededSlots.zip(fresh).map { (slot, candidate) ->
        DailyPlanEntity(
          date = today,
          slotIndex = slot,
          missionId = candidate.id,
          dimensionIndex = candidate.dimensionIndex,
          wasSwapped = false
        )
      }
      val merged = (keptCompleted + filled).sortedBy { it.slotIndex }
      dailyPlanDao.deletePlanForDate(today)
      if (merged.isNotEmpty()) {
        dailyPlanDao.insertPlanEntries(merged)
      }
      merged
    }
  }

  /**
   * Permite cambiar suavemente una acción del plan de hoy por otra alternativa realista sin penalización.
   */
  suspend fun swapDailyMission(
    slotIndex: Int,
    perceivedScores: Map<Int, Int>
  ): DailyPlanEntity? {
    refreshDayIfNeeded()
    val today = dayKey.today()
    return planMutex.withLock {
      val currentPlan = dailyPlanDao.getPlanForDateOnce(today)
      val target = currentPlan.firstOrNull { it.slotIndex == slotIndex } ?: return@withLock null
      val catalog = buildFullCatalog(perceivedScores)
      val currentCandidate = catalog.firstOrNull { it.id == target.missionId }?.toCandidate()
        ?: com.example.domain.MissionCandidate(
          id = target.missionId,
          dimensionIndex = target.dimensionIndex,
          durationMinutes = 10
        )

      val recentDays = dayKey.getRangeLastDays(14)
      val recentCompletions = completionDao.getCompletionsForDates(recentDays).first().map { it.toDomain() }
      val recentPlanDays = dayKey.getRangeLastDays(DailyMissionPlanner.OFFERED_COOLDOWN_DAYS + 1)
      val planHistory = dailyPlanDao.getPlansInDatesOnce(recentPlanDays).map {
        DailyPlanHistoryItem(date = it.date, missionId = it.missionId, slotIndex = it.slotIndex)
      }

      val replacement = DailyMissionPlanner.pickReplacementMission(
        todayKey = today,
        currentDayMissionIds = currentPlan.map { it.missionId }.toSet(),
        replacingMission = currentCandidate,
        catalog = catalog.map { it.toCandidate() },
        chosenFocusDimensions = assessmentRepository.chosenFocusDimensionsFlow.value,
        recentCompletions = recentCompletions,
        recentPlanHistory = planHistory,
        dayKey = dayKey
      ) ?: return@withLock null

      dailyPlanDao.updatePlanSlot(
        date = today,
        slotIndex = slotIndex,
        newMissionId = replacement.id,
        newDimensionIndex = replacement.dimensionIndex
      )
      DailyPlanEntity(
        date = today,
        slotIndex = slotIndex,
        missionId = replacement.id,
        dimensionIndex = replacement.dimensionIndex,
        wasSwapped = true
      )
    }
  }

  // ==========================================
  // Registro y revisión de utilidad en Room
  // ==========================================

  suspend fun toggleMissionCompletion(
    missionId: String,
    dimensionIndex: Int,
    expAwarded: Int = 20,
    date: String = dayKey.today()
  ): Boolean {
    refreshDayIfNeeded()
    val existing = completionDao.findCompletion(missionId, date)
    return if (existing != null) {
      completionDao.deleteCompletion(missionId, date)
      false
    } else {
      val expToday = completionDao.getCompletionsForDate(date).first().sumOf { it.expAwarded }
      val remainingCap = (GamificationStats.DAILY_EXP_CAP - expToday).coerceAtLeast(0)
      val cappedExp = expAwarded.coerceIn(0, remainingCap)
      completionDao.insertCompletion(
        CompletionEntity(
          missionId = missionId,
          dimensionIndex = dimensionIndex,
          date = date,
          helpfulRating = null,
          reflectionNote = "",
          completedAtMillis = dayKey.currentMillis(),
          expAwarded = cappedExp
        )
      )
      assessmentRepository.removeSavedMission(missionId)
      true
    }
  }

  suspend fun saveReflectionFeedback(
    missionId: String,
    dimensionIndex: Int,
    rating: HelpfulnessRating?,
    reflectionNote: String,
    date: String = dayKey.today(),
    expAwarded: Int = 20
  ) {
    refreshDayIfNeeded()
    val cleanNote = reflectionNote.trim()
    val existing = completionDao.findCompletion(missionId, date)
    if (existing != null) {
      completionDao.updateCompletionFeedback(
        missionId = missionId,
        date = date,
        helpfulRating = rating?.name,
        reflectionNote = cleanNote
      )
    } else {
      val expToday = completionDao.getCompletionsForDate(date).first().sumOf { it.expAwarded }
      val remainingCap = (GamificationStats.DAILY_EXP_CAP - expToday).coerceAtLeast(0)
      val cappedExp = expAwarded.coerceIn(0, remainingCap)
      completionDao.insertCompletion(
        CompletionEntity(
          missionId = missionId,
          dimensionIndex = dimensionIndex,
          date = date,
          helpfulRating = rating?.name,
          reflectionNote = cleanNote,
          completedAtMillis = dayKey.currentMillis(),
          expAwarded = cappedExp
        )
      )
      assessmentRepository.removeSavedMission(missionId)
    }
  }

  suspend fun deleteCompletion(missionId: String, date: String = dayKey.today()): Int {
    return completionDao.deleteCompletion(missionId, date)
  }

  suspend fun insertCompletion(
    missionId: String,
    dimensionIndex: Int,
    date: String = dayKey.today(),
    rating: HelpfulnessRating? = null,
    reflectionNote: String = "",
    expAwarded: Int = 20
  ): Long {
    return completionDao.insertCompletion(
      CompletionEntity(
        missionId = missionId,
        dimensionIndex = dimensionIndex,
        date = date,
        helpfulRating = rating?.name,
        reflectionNote = reflectionNote.trim(),
        completedAtMillis = dayKey.currentMillis(),
        expAwarded = expAwarded
      )
    )
  }

  // ==========================================
  // Métodos heredados de AssessmentRepository
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
