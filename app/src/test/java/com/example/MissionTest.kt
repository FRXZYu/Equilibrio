package com.example

import com.example.model.BalanceMission
import com.example.model.DimensionsProvider
import com.example.model.GamificationStats
import com.example.model.MissionsProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MissionTest {

  @Test
  fun testGenerateMissionsPrioritizesWeaknesses() {
    // Descanso (2) and Autoestima (3) are lowest
    val scores = mapOf(
      0 to 3, // Autoestima (weak)
      1 to 8, // Ejercicio (strong)
      2 to 2, // Descanso (weak)
      3 to 7, // Relaciones (strong)
      4 to 8, // Hobbies (strong)
      5 to 9  // Excelencia (strong)
    )
    val missions = MissionsProvider.generateMissionsForUser(scores)
    assertTrue("Should generate missions", missions.isNotEmpty())

    val weakMissions = missions.filter { it.isWeaknessFocus }
    assertTrue("Should flag weakness focus missions", weakMissions.isNotEmpty())
    val descansoMissions = weakMissions.filter { it.dimensionIndex == 2 }
    assertTrue("Should prioritize Descanso", descansoMissions.isNotEmpty())
  }

  @Test
  fun testWeaknessMissionsAward1Point5xExpMultiplier() {
    val scores = mapOf(
      0 to 3, // Autoestima (weak <= 6 -> 1.5x)
      1 to 9, // Ejercicio (strong > 6 -> 1.0x)
      2 to 2, // Descanso (weak <= 6 -> 1.5x)
      3 to 8, // Relaciones
      4 to 8, // Hobbies
      5 to 9  // Excelencia
    )
    val missions = MissionsProvider.generateMissionsForUser(scores)

    // Weak mission (e.g. Descanso)
    val descansoMission = missions.first { it.dimensionIndex == 2 }
    assertTrue("Weak mission must have multiplier", descansoMission.hasMultiplier)
    assertEquals(
      "Weak mission must award 1.5x base EXP",
      (descansoMission.baseExpReward * 1.5).toInt(),
      descansoMission.expReward
    )

    // Strong mission (e.g. Ejercicio)
    val ejercicioMission = missions.first { it.dimensionIndex == 1 }
    assertFalse("Strong mission must not have 1.5x multiplier", ejercicioMission.hasMultiplier)
    assertEquals(
      "Strong mission must award standard normal EXP",
      ejercicioMission.baseExpReward,
      ejercicioMission.expReward
    )
  }

  @Test
  fun testDailyExpCapAndLevelCalculation() {
    val statsNormal = GamificationStats(
      totalExp = 200,
      expToday = 80,
      completedMissionsCount = 10,
      activeStreakDays = 3
    )

    assertFalse("80 EXP is under 150 daily cap", statsNormal.isDailyCapReached)
    assertEquals(70, statsNormal.remainingDailyExp)
    assertEquals(2, statsNormal.level)
    assertEquals("Principiante", statsNormal.rank)

    val statsCapped = GamificationStats(
      totalExp = 350,
      expToday = 150,
      completedMissionsCount = 14,
      activeStreakDays = 4
    )

    assertTrue("150 EXP reaches daily cap", statsCapped.isDailyCapReached)
    assertEquals(0, statsCapped.remainingDailyExp)
    assertEquals(3, statsCapped.level)
    assertEquals("Buscador de Armonía", statsCapped.rank)
  }

  @Test
  fun testCustomMissionCreationWithHabit() {
    val habitDays = listOf("Lun", "Mié", "Vie")
    val customMission = BalanceMission(
      id = "custom_1",
      dimensionIndex = 0,
      dimensionTitle = "Autoestima",
      title = "Práctica diaria de gratitud y autoreflexión",
      summary = "Escribir 3 cosas por las que me siento orgulloso hoy.",
      description = "Escribir 3 cosas por las que me siento orgulloso hoy.",
      steps = listOf("Paso 1: Preparación", "Paso 2: Ejecución", "Paso 3: Reflexión"),
      benefit = "Eleva el autoconcepto positivo.",
      durationLabel = "Diario",
      baseExpReward = 35,
      expReward = 35,
      isWeaknessFocus = false,
      isCompleted = false,
      isCustom = true,
      difficulty = "Media",
      frequency = "Diariamente",
      isHabit = true,
      habitRepeatWeeks = 2,
      habitDays = habitDays,
      habitEndRule = "Nunca"
    )

    assertTrue(customMission.isCustom)
    assertTrue(customMission.isHabit)
    assertEquals(2, customMission.habitRepeatWeeks)
    assertEquals(3, customMission.habitDays.size)
    assertEquals("Nunca", customMission.habitEndRule)
    assertFalse(customMission.isCompleted)
  }
}
