package com.example.domain

/**
 * Representa una acción candidata del catálogo o personalizada dentro del dominio puro.
 */
data class MissionCandidate(
  val id: String,
  val dimensionIndex: Int,
  val durationMinutes: Int,
  val isCustom: Boolean = false,
  val isHabit: Boolean = false
)

/**
 * Registro histórico ligero de misiones ofrecidas en días anteriores para evitar repeticiones.
 */
data class DailyPlanHistoryItem(
  val date: String,
  val missionId: String,
  val slotIndex: Int
)

/**
 * Planificador diario de acciones concretas y realistas.
 * - Selecciona hasta [DAILY_ACTION_COUNT] acciones pequeñas para hoy.
 * - Prioriza las áreas elegidas por la persona (y necesidades reales si no hay selección).
 * - Evita repetir acciones ofrecidas o realizadas en los últimos días.
 * - Aprende de lo que resultó útil vs. pesado para no convertir la app en una carga.
 */
object DailyMissionPlanner {

  const val DAILY_ACTION_COUNT = 3
  const val OFFERED_COOLDOWN_DAYS = 3
  const val COMPLETED_COOLDOWN_DAYS = 4

  fun planDailyMissions(
    todayKey: String,
    catalog: List<MissionCandidate>,
    chosenFocusDimensions: Set<Int>,
    perceivedScores: Map<Int, Int>,
    realScores: Map<Int, Int>,
    recentCompletions: List<CompletionRecord>,
    recentPlanHistory: List<DailyPlanHistoryItem>,
    savedMissionIds: Set<String> = emptySet(),
    dayKey: DayKey = DayKey.DEFAULT
  ): List<MissionCandidate> {
    if (catalog.isEmpty()) return emptyList()

    val validFocus = chosenFocusDimensions.filter { it in 0..5 }.toSet()
    val fallbackPriorityDim = HexagonCalculator.findLowestRealDimension(realScores, perceivedScores)
    val primaryFocusDims = if (validFocus.isNotEmpty()) validFocus else setOf(fallbackPriorityDim)

    val recentOfferedWindow = dayKey.getRangeLastDays(OFFERED_COOLDOWN_DAYS + 1)
      .filter { it != todayKey }
      .toSet()
    val recentCompletedWindow = dayKey.getRangeLastDays(COMPLETED_COOLDOWN_DAYS + 1)
      .filter { it != todayKey }
      .toSet()

    val recentlyOfferedIds = recentPlanHistory
      .filter { it.date in recentOfferedWindow }
      .map { it.missionId }
      .toSet()

    val recentlyCompletedIds = recentCompletions
      .filter { it.date in recentCompletedWindow }
      .map { it.missionId }
      .toSet()

    val helpfulnessSummary = HexagonCalculator.calculateHelpfulnessSummary(recentCompletions)
    val seed = todayKey.hashCode()

    fun scoreCandidate(candidate: MissionCandidate, preferFocus: Boolean): Int {
      var score = 0

      // 1. Respeto al área elegida o necesidad actual
      val inChosenFocus = candidate.dimensionIndex in primaryFocusDims
      if (preferFocus && inChosenFocus) {
        score += 120
      } else if (!preferFocus && !inChosenFocus) {
        score += 45
      }

      // 2. Priorizar acciones pequeñas y realistas (<= 10 min / <= 15 min)
      score += when {
        candidate.durationMinutes <= 5 -> 35
        candidate.durationMinutes <= 10 -> 28
        candidate.durationMinutes <= 15 -> 18
        else -> 4
      }

      // 3. Hábitos propios o guardadas por la persona
      if (candidate.isHabit) score += 30
      if (candidate.id in savedMissionIds) score += 20

      // 4. Aprendizaje por experiencia real ("Me ayudó" vs "Se sintió pesado")
      if (candidate.id in helpfulnessSummary.helpfulMissionIds) {
        score += 24
      }
      if ((helpfulnessSummary.helpfulByDimension[candidate.dimensionIndex] ?: 0) > 0) {
        score += 10
      }
      if (candidate.id in helpfulnessSummary.heavyMissionIds) {
        score -= 140 // Evitar insistir con lo que se sintió pesado
      }

      // 5. Anti-repetición estricta (cooldown) salvo hábitos creados por la persona
      if (!candidate.isHabit) {
        if (candidate.id in recentlyOfferedIds) score -= 110
        if (candidate.id in recentlyCompletedIds) score -= 130
      } else if (candidate.id in recentlyCompletedIds) {
        // Incluso hábitos propios no se fuerzan si ya se hicieron ayer
        score -= 15
      }

      // 6. Rotación determinista por fecha para variedad diaria a las 00:00
      val dailyJitter = ((candidate.id.hashCode() xor seed) and 0x7FFFFFFF) % 19
      score += dailyJitter

      return score
    }

    val selected = mutableListOf<MissionCandidate>()
    val remaining = catalog.distinctBy { it.id }.toMutableList()

    // Ranura 1 y 2: priorizar el enfoque elegido / necesidad actual, evitando duplicar misma dimensión si hay variedad
    val focusSlots = minOf(2, DAILY_ACTION_COUNT)
    for (slot in 0 until focusSlots) {
      val usedDims = selected.map { it.dimensionIndex }.toSet()
      val best = remaining.maxByOrNull { candidate ->
        val sameDimPenalty = if (candidate.dimensionIndex in usedDims && primaryFocusDims.size > 1) -40 else 0
        scoreCandidate(candidate, preferFocus = true) + sameDimPenalty
      } ?: break
      selected.add(best)
      remaining.remove(best)
    }

    // Ranura 3: variedad amable en otra área para cuidar el equilibrio sin abrumar
    if (selected.size < DAILY_ACTION_COUNT && remaining.isNotEmpty()) {
      val usedDims = selected.map { it.dimensionIndex }.toSet()
      val bestThird = remaining.maxByOrNull { candidate ->
        val freshDimBonus = if (candidate.dimensionIndex !in usedDims) 50 else 0
        scoreCandidate(candidate, preferFocus = false) + freshDimBonus
      }
      if (bestThird != null) {
        selected.add(bestThird)
      }
    }

    return selected
  }

  /**
   * Elige una alternativa distinta y fresca para cambiar una acción del día sin culpa.
   */
  fun pickReplacementMission(
    todayKey: String,
    currentDayMissionIds: Set<String>,
    replacingMission: MissionCandidate,
    catalog: List<MissionCandidate>,
    chosenFocusDimensions: Set<Int>,
    recentCompletions: List<CompletionRecord>,
    recentPlanHistory: List<DailyPlanHistoryItem>,
    dayKey: DayKey = DayKey.DEFAULT
  ): MissionCandidate? {
    val helpfulness = HexagonCalculator.calculateHelpfulnessSummary(recentCompletions)
    val recentOffered = recentPlanHistory
      .filter { it.date in dayKey.getRangeLastDays(OFFERED_COOLDOWN_DAYS + 1) }
      .map { it.missionId }
      .toSet()
    val recentCompleted = recentCompletions
      .filter { it.date in dayKey.getRangeLastDays(COMPLETED_COOLDOWN_DAYS + 1) }
      .map { it.missionId }
      .toSet()

    val pool = catalog
      .distinctBy { it.id }
      .filter { it.id !in currentDayMissionIds && it.id != replacingMission.id }

    if (pool.isEmpty()) return null

    return pool.maxByOrNull { candidate ->
      var s = 0
      if (candidate.dimensionIndex == replacingMission.dimensionIndex) s += 60
      if (candidate.dimensionIndex in chosenFocusDimensions) s += 40
      if (candidate.durationMinutes <= 10) s += 25
      if (candidate.id in helpfulness.heavyMissionIds) s -= 150
      if (candidate.id in recentOffered) s -= 90
      if (candidate.id in recentCompleted) s -= 100
      s += ((candidate.id.hashCode() xor todayKey.hashCode()) and 0x7FFFFFFF) % 13
      s
    }
  }
}
