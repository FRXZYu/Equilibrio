package com.example

import com.example.model.BalanceMission
import com.example.model.MissionsProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MissionTest {

  @Test
  fun testCatalogContains36ConcreteActionsAcrossAll6Dimensions() {
    val scores = mapOf(0 to 5, 1 to 6, 2 to 4, 3 to 7, 4 to 8, 5 to 6)
    val missions = MissionsProvider.generateMissionsForUser(scores)

    assertEquals(36, missions.size)
    assertEquals(36, missions.map { it.id }.distinct().size)
    for (dimIndex in 0..5) {
      assertEquals(6, missions.count { it.dimensionIndex == dimIndex })
    }
    assertTrue(missions.all { it.steps.size >= 3 && it.durationMinutes in 3..15 })
  }

  @Test
  fun testChosenFocusDimensionsArePrioritizedInCatalogOrdering() {
    val scores = mapOf(0 to 9, 1 to 2, 2 to 9, 3 to 8, 4 to 9, 5 to 8)
    // Aunque Ejercicio (1) tiene puntaje bajo, la persona eligió Hobbies (4) como enfoque
    val missions = MissionsProvider.generateMissionsForUser(
      scores = scores,
      chosenFocusDimensions = setOf(4)
    )

    assertEquals(4, missions.first().dimensionIndex)
    assertTrue(missions.filter { it.dimensionIndex == 4 }.all { it.isPriorityFocus })
    assertFalse(missions.filter { it.dimensionIndex == 1 }.any { it.isPriorityFocus })
  }

  @Test
  fun testCustomHabitAndCandidateConversion() {
    val customMission = BalanceMission(
      id = "custom_1",
      dimensionIndex = 2,
      dimensionTitle = "Descanso",
      title = "Lectura breve sin móvil",
      summary = "Leer 10 minutos antes de apagar la luz.",
      description = "Cerrar la jornada con calma lejos de pantallas.",
      steps = listOf("Dejar el móvil fuera", "Leer 10 min", "Apagar la luz"),
      benefit = "Facilita bajar revoluciones al final del día.",
      durationLabel = "10 min",
      durationMinutes = 10,
      isCustom = true,
      frequency = "Diariamente",
      isHabit = true,
      habitRepeatWeeks = 2,
      habitDays = listOf("L", "M", "V"),
      habitEndRule = "Después de 4 semanas"
    )

    val candidate = customMission.toCandidate()
    assertEquals("custom_1", candidate.id)
    assertEquals(2, candidate.dimensionIndex)
    assertEquals(10, candidate.durationMinutes)
    assertTrue(candidate.isCustom)
    assertTrue(candidate.isHabit)
  }
}
