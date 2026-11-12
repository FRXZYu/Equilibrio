package com.example.domain

/**
 * Calculador de dominio puro para las estadísticas del hexágono y gamificación de la app.
 * Sin dependencias de Android (100% testeable en JVM).
 */
object HexagonCalculator {

  /**
   * Objetivo de puntos de EXP acumulados en una dimensión durante 14 días para alcanzar un puntaje real de 10.
   * Valor provisional: 140 EXP (equivale aprox. a 10 EXP diarios o 3-4 misiones completadas en esa dimensión).
   */
  const val DEFAULT_OBJETIVO_EXP = 140

  /**
   * Mapa de objetivos por dimensión (índices 0 a 5).
   * Centralizado en una sola constante para ajuste sencillo.
   */
  val DEFAULT_OBJETIVOS: Map<Int, Int> = (0..5).associateWith { DEFAULT_OBJETIVO_EXP }

  /**
   * EXP necesaria por nivel (provisional, cada 200 de EXP sube 1 nivel).
   */
  const val EXP_POR_NIVEL = 200

  /**
   * Calcula los 6 puntajes reales (0..5) a partir de las completaciones registradas en los últimos 14 días.
   * Fórmula: real(dim) = min(10, 10 * puntosExp14d(dim) / OBJETIVO(dim)).
   *
   * Las completaciones con fechas fuera de la ventana de 14 días son descartadas.
   */
  fun calculateRealScores(
    completions: List<CompletionRecord>,
    dayKey: DayKey = DayKey.DEFAULT,
    objetivos: Map<Int, Int> = DEFAULT_OBJETIVOS
  ): Map<Int, Int> {
    val validDays = dayKey.getRangeLastDays(14).toSet()
    val completions14d = completions.filter { it.date in validDays }

    val expByDimension = mutableMapOf<Int, Int>()
    for (i in 0..5) {
      expByDimension[i] = 0
    }

    for (c in completions14d) {
      if (c.dimensionIndex in 0..5) {
        val current = expByDimension[c.dimensionIndex] ?: 0
        expByDimension[c.dimensionIndex] = current + c.expAwarded
      }
    }

    val result = mutableMapOf<Int, Int>()
    for (i in 0..5) {
      val puntos = expByDimension[i] ?: 0
      val objetivo = objetivos[i] ?: DEFAULT_OBJETIVO_EXP
      val score = if (objetivo > 0) {
        minOf(10, (10 * puntos) / objetivo)
      } else {
        0
      }
      result[i] = score
    }
    return result
  }

  /**
   * Variante con precisión flotante (0.0f a 10.0f) para animaciones suaves en el hexágono.
   */
  fun calculateRealScoresFloat(
    completions: List<CompletionRecord>,
    dayKey: DayKey = DayKey.DEFAULT,
    objetivos: Map<Int, Int> = DEFAULT_OBJETIVOS
  ): Map<Int, Float> {
    val validDays = dayKey.getRangeLastDays(14).toSet()
    val completions14d = completions.filter { it.date in validDays }

    val expByDimension = (0..5).associateWith { 0 }.toMutableMap()
    for (c in completions14d) {
      if (c.dimensionIndex in 0..5) {
        val current = expByDimension[c.dimensionIndex] ?: 0
        expByDimension[c.dimensionIndex] = current + c.expAwarded
      }
    }

    return (0..5).associateWith { i ->
      val puntos = expByDimension[i] ?: 0
      val objetivo = objetivos[i] ?: DEFAULT_OBJETIVO_EXP
      if (objetivo > 0) {
        minOf(10f, (10f * puntos) / objetivo.toFloat())
      } else {
        0f
      }
    }
  }

  /**
   * Exp total = suma de expAwarded de todas las completaciones históricas.
   */
  fun calculateTotalExp(completions: List<CompletionRecord>): Int {
    return completions.sumOf { it.expAwarded }
  }

  /**
   * Nivel = exp total / 200 + 1 (provisional, cada 200 EXP es un nivel nuevo).
   */
  fun calculateLevel(totalExp: Int): Int {
    return (totalExp / EXP_POR_NIVEL) + 1
  }

  /**
   * Rango descriptivo basado en el nivel actual.
   */
  fun calculateRank(level: Int): String {
    return when {
      level <= 2 -> "Principiante"
      level <= 4 -> "Explorador"
      level <= 7 -> "Equilibrado"
      level <= 10 -> "Conquistador"
      else -> "Maestro"
    }
  }

  /**
   * Racha = días consecutivos con al menos una completación, terminando hoy o ayer.
   * Si hoy aún no hay completaciones, la racha NO se rompe hasta que termine el día.
   */
  fun calculateStreak(
    completions: List<CompletionRecord>,
    dayKey: DayKey = DayKey.DEFAULT
  ): Int {
    val activeDates = completions.map { it.date }.toSet()
    val today = dayKey.today()
    val yesterday = dayKey.yesterday()

    return if (today in activeDates) {
      // Hoy ya se completó al menos una misión: contamos hacia atrás empezando hoy
      var streak = 1
      var daysBack = 1
      while (dayKey.daysAgo(daysBack) in activeDates) {
        streak++
        daysBack++
      }
      streak
    } else if (yesterday in activeDates) {
      // Hoy aún no se completa ninguna, pero ayer sí: la racha sigue viva
      var streak = 1
      var daysBack = 2
      while (dayKey.daysAgo(daysBack) in activeDates) {
        streak++
        daysBack++
      }
      streak
    } else {
      // Ni hoy ni ayer hubo completaciones: racha es 0
      0
    }
  }

  /**
   * Determina la dimensión real más baja (0 a 5).
   * En caso de empate, desempata eligiendo la dimensión más baja del percibido.
   * Si persiste el empate, elige el menor índice.
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

      if (real < minReal) {
        minDimension = dim
        minReal = real
        minPerceived = perceived
      } else if (real == minReal) {
        // Desempate con la más baja del percibido
        if (perceived < minPerceived) {
          minDimension = dim
          minReal = real
          minPerceived = perceived
        }
      }
    }
    return minDimension
  }
}
