package com.example

import com.example.domain.CompletionRecord
import com.example.domain.DayKey
import com.example.domain.HexagonCalculator
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class StreakTest {

  private var currentTestMillis = 1762948800000L // Jueves 12 de Noviembre 2026
  private val testDayKey = DayKey { currentTestMillis }

  @Test
  fun testStreakSinDatos() {
    val completions = emptyList<CompletionRecord>()
    val streak = HexagonCalculator.calculateStreak(completions, testDayKey)
    assertEquals("Sin datos la racha debe ser 0", 0, streak)
  }

  @Test
  fun testRachaDe3DiasTerminandoHoy() {
    val today = testDayKey.today()
    val yesterday = testDayKey.yesterday()
    val twoDaysAgo = testDayKey.daysAgo(2)

    val completions = listOf(
      CompletionRecord(missionId = "m1", dimensionIndex = 0, expAwarded = 30, date = twoDaysAgo),
      CompletionRecord(missionId = "m2", dimensionIndex = 1, expAwarded = 40, date = yesterday),
      CompletionRecord(missionId = "m3", dimensionIndex = 2, expAwarded = 35, date = today)
    )

    val streak = HexagonCalculator.calculateStreak(completions, testDayKey)
    assertEquals("3 días consecutivos completados deben dar racha de 3", 3, streak)
  }

  @Test
  fun testHoySinCompletarConAyerCompletado() {
    // Si hoy aún no se ha completado nada, la racha NO se rompe: se mantiene la racha activa de ayer
    val yesterday = testDayKey.yesterday()
    val twoDaysAgo = testDayKey.daysAgo(2)

    val completions = listOf(
      CompletionRecord(missionId = "m1", dimensionIndex = 0, expAwarded = 30, date = twoDaysAgo),
      CompletionRecord(missionId = "m2", dimensionIndex = 1, expAwarded = 40, date = yesterday)
      // Ninguna misión completada hoy
    )

    val streak = HexagonCalculator.calculateStreak(completions, testDayKey)
    assertEquals("Racha debe mantenerse en 2 mientras el día de hoy siga en curso", 2, streak)
  }

  @Test
  fun testHuecoDeUnDiaRompeRacha() {
    val today = testDayKey.today()
    val twoDaysAgo = testDayKey.daysAgo(2)
    // Ayer NO hubo ninguna completación (hueco)

    val completions = listOf(
      CompletionRecord(missionId = "m1", dimensionIndex = 0, expAwarded = 30, date = twoDaysAgo),
      CompletionRecord(missionId = "m3", dimensionIndex = 2, expAwarded = 35, date = today)
    )

    val streak = HexagonCalculator.calculateStreak(completions, testDayKey)
    assertEquals("Al haber un hueco ayer, la racha se reinicia en 1 hoy", 1, streak)

    // Si además hoy tampoco se completó, la racha es 0
    val completionsSinHoy = listOf(
      CompletionRecord(missionId = "m1", dimensionIndex = 0, expAwarded = 30, date = twoDaysAgo)
    )
    val streakSinHoy = HexagonCalculator.calculateStreak(completionsSinHoy, testDayKey)
    assertEquals("Sin completaciones hoy ni ayer, la racha es 0", 0, streakSinHoy)
  }

  @Test
  fun testMedianocheYCambioDeDia() {
    var simTime = 1762948800000L // Día D
    val dynamicClock = DayKey { simTime }

    val dia1 = dynamicClock.today()
    val completions = mutableListOf(
      CompletionRecord(missionId = "m1", dimensionIndex = 0, expAwarded = 30, date = dia1)
    )

    // Al final del Día D, racha es 1
    assertEquals(1, HexagonCalculator.calculateStreak(completions, dynamicClock))

    // Pasa la medianoche: avanzamos el reloj exactamente 24 horas (Día D + 1)
    simTime += 24 * 60 * 60 * 1000L

    // En el nuevo día (mañana), el usuario aún no completa misiones, pero la racha de ayer se mantiene viva
    assertEquals("Al pasar medianoche, la racha de ayer no se rompe durante el nuevo día", 1, HexagonCalculator.calculateStreak(completions, dynamicClock))

    // El usuario completa una misión en el nuevo día
    val dia2 = dynamicClock.today()
    completions.add(
      CompletionRecord(missionId = "m2", dimensionIndex = 1, expAwarded = 40, date = dia2)
    )

    // La racha sube a 2
    assertEquals(2, HexagonCalculator.calculateStreak(completions, dynamicClock))
  }
}
