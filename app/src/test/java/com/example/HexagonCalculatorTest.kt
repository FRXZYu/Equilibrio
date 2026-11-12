package com.example

import com.example.domain.CompletionRecord
import com.example.domain.DayKey
import com.example.domain.HexagonCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HexagonCalculatorTest {

  // Reloj fijo para pruebas: 2026-11-12 12:00:00 UTC (1762948800000L)
  private val testMillis = 1762948800000L
  private val testDayKey = DayKey { testMillis }

  @Test
  fun testSinDatos() {
    val completions = emptyList<CompletionRecord>()
    val scores = HexagonCalculator.calculateRealScores(completions, testDayKey)

    assertEquals(6, scores.size)
    for (i in 0..5) {
      assertEquals("Dimensión $i debe ser 0 sin datos", 0, scores[i])
    }

    assertEquals("EXP total sin datos debe ser 0", 0, HexagonCalculator.calculateTotalExp(completions))
    assertEquals("Nivel sin datos debe ser 1", 1, HexagonCalculator.calculateLevel(0))
  }

  @Test
  fun testUnaSolaDimension() {
    val today = testDayKey.today()
    // 70 puntos de EXP en la dimensión 1 (Ejercicio), con objetivo de 140
    // 10 * 70 / 140 = 5
    val completions = listOf(
      CompletionRecord(missionId = "m1", dimensionIndex = 1, expAwarded = 40, date = today),
      CompletionRecord(missionId = "m2", dimensionIndex = 1, expAwarded = 30, date = today)
    )

    val scores = HexagonCalculator.calculateRealScores(completions, testDayKey)

    assertEquals("Dimensión 1 debe ser 5 (70/140)", 5, scores[1])
    assertEquals("Dimensión 0 debe ser 0", 0, scores[0])
    assertEquals("Dimensión 2 debe ser 0", 0, scores[2])
    assertEquals("Dimensión 3 debe ser 0", 0, scores[3])
    assertEquals("Dimensión 4 debe ser 0", 0, scores[4])
    assertEquals("Dimensión 5 debe ser 0", 0, scores[5])
  }

  @Test
  fun testTodasParejas() {
    val today = testDayKey.today()
    // 140 puntos en cada una de las 6 dimensiones
    val completions = (0..5).map { dim ->
      CompletionRecord(missionId = "m_$dim", dimensionIndex = dim, expAwarded = 140, date = today)
    }

    val scores = HexagonCalculator.calculateRealScores(completions, testDayKey)

    for (i in 0..5) {
      assertEquals("Todas las dimensiones deben alcanzar 10", 10, scores[i])
    }
  }

  @Test
  fun testTopeEn10() {
    val today = testDayKey.today()
    // 280 puntos en dimensión 0 (doble del objetivo de 140)
    val completions = listOf(
      CompletionRecord(missionId = "m_super", dimensionIndex = 0, expAwarded = 280, date = today)
    )

    val scores = HexagonCalculator.calculateRealScores(completions, testDayKey)

    assertEquals("El puntaje real no debe superar 10", 10, scores[0])
  }

  @Test
  fun testCompletacionesFueraDeLos14DiasIgnoradas() {
    val today = testDayKey.today()
    val hace13Dias = testDayKey.daysAgo(13) // Dentro de la ventana de 14 días
    val hace14Dias = testDayKey.daysAgo(14) // Fuera de la ventana (día 15 hacia atrás)
    val hace20Dias = testDayKey.daysAgo(20) // Muy anterior

    val completions = listOf(
      CompletionRecord(missionId = "c_hoy", dimensionIndex = 2, expAwarded = 35, date = today),
      CompletionRecord(missionId = "c_hace13", dimensionIndex = 2, expAwarded = 35, date = hace13Dias),
      CompletionRecord(missionId = "c_hace14", dimensionIndex = 2, expAwarded = 70, date = hace14Dias), // ignorado
      CompletionRecord(missionId = "c_hace20", dimensionIndex = 2, expAwarded = 140, date = hace20Dias) // ignorado
    )

    val scores = HexagonCalculator.calculateRealScores(completions, testDayKey)

    // Solo se suman 35 + 35 = 70 EXP -> (10 * 70) / 140 = 5
    assertEquals("Solo deben computarse completaciones de los últimos 14 días", 5, scores[2])
  }

  @Test
  fun testDimensionMasBajaRealConDesempate() {
    val realScores = mapOf(
      0 to 5,
      1 to 2, // Empate en real más bajo
      2 to 2, // Empate en real más bajo
      3 to 6,
      4 to 8,
      5 to 7
    )
    val perceivedScores = mapOf(
      0 to 8,
      1 to 6, // Percibido de dimensión 1 es 6
      2 to 3, // Percibido de dimensión 2 es 3 (más bajo que 1)
      3 to 7,
      4 to 8,
      5 to 9
    )

    val lowest = HexagonCalculator.findLowestRealDimension(realScores, perceivedScores)
    assertEquals("Debe desempatar eligiendo la dimensión 2 por tener menor percibido", 2, lowest)
  }
}
