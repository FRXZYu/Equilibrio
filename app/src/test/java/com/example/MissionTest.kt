package com.example

import com.example.model.BalanceMission
import com.example.model.DimensionsProvider
import com.example.model.MissionsProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MissionTest {

  @Test
  fun testGenerateMissionsPrioritizesWeaknesses() {
    // Descanso and Autoestima are lowest
    val scores = mapOf(
      0 to 3, // Autoestima
      1 to 8, // Ejercicio
      2 to 2, // Descanso
      3 to 7, // Relaciones
      4 to 8, // Hobbies
      5 to 9  // Excelencia
    )
    val missions = MissionsProvider.generateMissionsForUser(scores)
    assertTrue("Should generate missions", missions.isNotEmpty())

    val weakMissions = missions.filter { it.isWeaknessFocus }
    assertTrue("Should flag weakness focus missions", weakMissions.isNotEmpty())
    val descansoMissions = weakMissions.filter { it.dimensionIndex == 2 }
    assertTrue("Should prioritize Descanso", descansoMissions.isNotEmpty())
  }

  @Test
  fun testCustomMissionCreationWithHabit() {
    val habitDays = listOf("Lun", "Mié", "Vie")
    val customMission = BalanceMission(
      id = "custom_1",
      dimensionIndex = 0,
      dimensionTitle = "Autoestima",
      title = "Práctica diaria de gratitud y autoreflexión",
      description = "Escribir 3 cosas por las que me siento orgulloso hoy.",
      benefit = "Eleva el autoconcepto positivo.",
      durationLabel = "Diario",
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
