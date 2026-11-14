package com.example.domain

/**
 * Resumen de cómo se ha sentido la persona con las acciones completadas.
 */
data class HelpfulnessSummary(
  val helpfulCount: Int,
  val neutralCount: Int,
  val heavyCount: Int,
  val helpfulByDimension: Map<Int, Int>,
  val helpfulMissionIds: Set<String>,
  val heavyMissionIds: Set<String>
) {
  val totalRated: Int get() = helpfulCount + neutralCount + heavyCount
}

/**
 * Calculador de dominio puro para el hexágono de equilibrio basado en acciones reales de cuidado,
 * sin niveles, rangos ni métricas de EXP que confundan puntos con bienestar.
 * 100% testeable en JVM sin dependencias de Android.
 */
object HexagonCalculator {

  /**
   * Meta orientativa de acciones de cuidado realizadas en una dimensión durante 14 días
   * para reflejar consistencia plena (10/10) en el hexágono real.
   */
  const val DEFAULT_OBJETIVO_ACCIONES = 5
  const val DEFAULT_OBJETIVO_EXP = 140

  /**
   * Mapa de metas orientativas por dimensión (índices 0 a 5).
   */
  val DEFAULT_OBJETIVOS: Map<Int, Int> = (0..5).associateWith { DEFAULT_OBJETIVO_ACCIONES }
  val DEFAULT_EXP_OBJETIVOS: Map<Int, Int> = (0..5).associateWith { DEFAULT_OBJETIVO_EXP }

  /**
   * Calcula los 6 puntajes reales (0..10) a partir de las acciones registradas en los últimos 14 días.
   * Fórmula: real(dim) = min(10, 10 * acciones14d(dim) / OBJETIVO(dim)).
   * Evita duplicar la misma acción el mismo día si llegara repetida.
   */
  fun calculateRealScores(
    completions: List<CompletionRecord>,
    dayKey: DayKey = DayKey.DEFAULT,
    objetivos: Map<Int, Int> = DEFAULT_OBJETIVOS
  ): Map<Int, Int> {
    val actionsByDim = calculateActionsByDimension14d(completions, dayKey)
    return (0..5).associateWith { dim ->
      val count = actionsByDim[dim] ?: 0
      val objetivo = objetivos[dim] ?: DEFAULT_OBJETIVO_ACCIONES
      if (objetivo > 0) {
        minOf(10, (10 * count) / objetivo)
      } else {
        0
      }
    }
  }

  /**
   * Variante con precisión flotante (0.0f a 10.0f) para animaciones suaves en el hexágono.
   */
  fun calculateRealScoresFloat(
    completions: List<CompletionRecord>,
    dayKey: DayKey = DayKey.DEFAULT,
    objetivos: Map<Int, Int> = DEFAULT_OBJETIVOS
  ): Map<Int, Float> {
    val actionsByDim = calculateActionsByDimension14d(completions, dayKey)
    return (0..5).associateWith { dim ->
      val count = actionsByDim[dim] ?: 0
      val objetivo = objetivos[dim] ?: DEFAULT_OBJETIVO_ACCIONES
      if (objetivo > 0) {
        minOf(10f, (10f * count) / objetivo.toFloat())
      } else {
        0f
      }
    }
  }

  /**
   * Cuenta acciones únicas (missionId + date) por dimensión en los últimos 14 días.
   */
  fun calculateActionsByDimension14d(
    completions: List<CompletionRecord>,
    dayKey: DayKey = DayKey.DEFAULT
  ): Map<Int, Int> {
    val validDays = dayKey.getRangeLastDays(14).toSet()
    val uniqueRecent = completions
      .filter { it.date in validDays && it.dimensionIndex in 0..5 }
      .distinctBy { "${it.missionId}_${it.date}" }

    val counts = (0..5).associateWith { 0 }.toMutableMap()
    for (c in uniqueRecent) {
      counts[c.dimensionIndex] = (counts[c.dimensionIndex] ?: 0) + 1
    }
    return counts
  }

  /**
   * Suma la EXP acumulada por dimensión durante los últimos 14 días.
   */
  fun calculateExpByDimension14d(
    completions: List<CompletionRecord>,
    dayKey: DayKey = DayKey.DEFAULT
  ): Map<Int, Int> {
    val validDays = dayKey.getRangeLastDays(14).toSet()
    val uniqueRecent = completions
      .filter { it.date in validDays && it.dimensionIndex in 0..5 }
      .distinctBy { "${it.missionId}_${it.date}" }

    val totals = (0..5).associateWith { 0 }.toMutableMap()
    for (c in uniqueRecent) {
      totals[c.dimensionIndex] = (totals[c.dimensionIndex] ?: 0) + c.expAwarded.coerceAtLeast(0)
    }
    return totals
  }

  fun calculateTotalExp(completions: List<CompletionRecord>): Int =
    completions.sumOf { it.expAwarded.coerceAtLeast(0) }

  fun calculateExpOnDate(completions: List<CompletionRecord>, date: String): Int =
    completions.filter { it.date == date }.sumOf { it.expAwarded.coerceAtLeast(0) }

  /**
   * Días consecutivos con al menos una acción amable de cuidado, terminando hoy o ayer.
   * Si hoy aún no hay registros, no se pierde hasta que termine el día.
   */
  fun calculateStreak(
    completions: List<CompletionRecord>,
    dayKey: DayKey = DayKey.DEFAULT
  ): Int {
    val activeDates = completions.map { it.date }.toSet()
    val today = dayKey.today()
    val yesterday = dayKey.yesterday()

    return if (today in activeDates) {
      var streak = 1
      var daysBack = 1
      while (dayKey.daysAgo(daysBack) in activeDates) {
        streak++
        daysBack++
      }
      streak
    } else if (yesterday in activeDates) {
      var streak = 1
      var daysBack = 2
      while (dayKey.daysAgo(daysBack) in activeDates) {
        streak++
        daysBack++
      }
      streak
    } else {
      0
    }
  }

  /**
   * Cantidad de días distintos de los últimos 14 en los que hubo al menos una acción registrada.
   */
  fun calculateActiveDaysLast14(
    completions: List<CompletionRecord>,
    dayKey: DayKey = DayKey.DEFAULT
  ): Int {
    val validDays = dayKey.getRangeLastDays(14).toSet()
    return completions.map { it.date }.filter { it in validDays }.toSet().size
  }

  /**
   * Resume qué acciones y áreas han resultado realmente útiles para la persona.
   */
  fun calculateHelpfulnessSummary(completions: List<CompletionRecord>): HelpfulnessSummary {
    var helpful = 0
    var neutral = 0
    var heavy = 0
    val helpfulByDim = (0..5).associateWith { 0 }.toMutableMap()
    val helpfulIds = mutableSetOf<String>()
    val heavyIds = mutableSetOf<String>()

    // Usamos el registro más reciente de cada misión para identificar si hoy resulta pesada o útil
    val latestByMission = completions.groupBy { it.missionId }
      .mapValues { (_, list) -> list.maxByOrNull { it.completedAtMillis } ?: list.last() }

    for (c in completions) {
      when (c.helpfulness) {
        HelpfulnessRating.HELPFUL -> {
          helpful++
          if (c.dimensionIndex in 0..5) {
            helpfulByDim[c.dimensionIndex] = (helpfulByDim[c.dimensionIndex] ?: 0) + 1
          }
        }
        HelpfulnessRating.NEUTRAL -> neutral++
        HelpfulnessRating.HEAVY -> heavy++
        null -> Unit
      }
    }

    for ((missionId, latest) in latestByMission) {
      when (latest.helpfulness) {
        HelpfulnessRating.HELPFUL -> helpfulIds.add(missionId)
        HelpfulnessRating.HEAVY -> heavyIds.add(missionId)
        else -> Unit
      }
    }

    return HelpfulnessSummary(
      helpfulCount = helpful,
      neutralCount = neutral,
      heavyCount = heavy,
      helpfulByDimension = helpfulByDim,
      helpfulMissionIds = helpfulIds,
      heavyMissionIds = heavyIds
    )
  }

  /**
   * Elige el área prioritaria respetando primero la elección explícita de la persona;
   * si aún no ha elegido, sugiere suavemente el área con más espacio para cuidar.
   */
  fun findPriorityDimension(
    realScores: Map<Int, Int>,
    perceivedScores: Map<Int, Int>,
    chosenFocusDimensions: Set<Int> = emptySet()
  ): Int {
    val validChosen = chosenFocusDimensions.filter { it in 0..5 }
    if (validChosen.isNotEmpty()) {
      return validChosen.minByOrNull { dim ->
        val real = realScores[dim] ?: 0
        val perceived = perceivedScores[dim] ?: 5
        real * 100 + perceived
      } ?: validChosen.first()
    }
    return findLowestRealDimension(realScores, perceivedScores)
  }

  /**
   * Determina la dimensión real más baja (0 a 5).
   * En caso de empate, desempata eligiendo la más baja del percibido.
   */
  fun findLowestRealDimension(
    realScores: Map<Int, Int>,
    perceivedScores: Map<Int, Int>
  ): Int {
    var minDimension = 0
    var minReal = realScores[0] ?: 0
    var minPerceived = perceivedScores[0] ?: 5

    for (dim in 1..5) {
      val real = realScores[dim] ?: 0
      val perceived = perceivedScores[dim] ?: 5

      if (real < minReal || (real == minReal && perceived < minPerceived)) {
        minDimension = dim
        minReal = real
        minPerceived = perceived
      }
    }
    return minDimension
  }

  /**
   * Calcula un indicador suave de equilibrio vivo combinando percepción inicial y acciones reales.
   */
  fun calculateLiveHarmonyPercentage(
    realScores: Map<Int, Int>,
    perceivedScores: Map<Int, Int>
  ): Int {
    val hasReal = realScores.values.any { it > 0 }
    val effectiveScores = (0..5).map { idx ->
      val perceived = perceivedScores[idx] ?: 5
      val real = realScores[idx] ?: 0
      if (!hasReal) {
        perceived
      } else if (real >= perceived) {
        real
      } else {
        ((perceived * 0.65f) + (real * 0.35f)).toInt().coerceIn(1, 10)
      }
    }
    return ((effectiveScores.sum() * 100) / 60).coerceIn(0, 100)
  }
}
