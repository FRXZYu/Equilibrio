package com.example

import com.example.domain.CompletionRecord
import com.example.domain.DayKey
import com.example.domain.HelpfulnessRating
import com.example.domain.HexagonCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class HexagonCalculatorTest {

  private fun fixedClock(year: Int, month: Int, day: Int): DayKey {
    val cal = Calendar.getInstance().apply {
      set(Calendar.YEAR, year)
      set(Calendar.MONTH, month)
      set(Calendar.DAY_OF_MONTH, day)
      set(Calendar.HOUR_OF_DAY, 12)
      set(Calendar.MINUTE, 0)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
    }
    val fixedMillis = cal.timeInMillis
    return DayKey { fixedMillis }
  }

  @Test
  fun testRealScoresByUniqueActionCountLast14Days() {
    val dayKey = fixedClock(2026, Calendar.NOVEMBER, 12)
    val completions = listOf(
      // Dimensión 0: 1 acción -> (10 * 1) / 5 = 2
      CompletionRecord(missionId = "auto_1", dimensionIndex = 0, date = dayKey.today()),
      // Dimensión 1: 3 acciones en distintos días -> (10 * 3) / 5 = 6
      CompletionRecord(missionId = "ejer_1", dimensionIndex = 1, date = dayKey.today()),
      CompletionRecord(missionId = "ejer_2", dimensionIndex = 1, date = dayKey.daysAgo(2)),
      CompletionRecord(missionId = "ejer_3", dimensionIndex = 1, date = dayKey.daysAgo(13)),
      // Dimensión 2: 5 acciones -> 10 (tope)
      CompletionRecord(missionId = "desc_1", dimensionIndex = 2, date = dayKey.today()),
      CompletionRecord(missionId = "desc_2", dimensionIndex = 2, date = dayKey.daysAgo(1)),
      CompletionRecord(missionId = "desc_3", dimensionIndex = 2, date = dayKey.daysAgo(2)),
      CompletionRecord(missionId = "desc_4", dimensionIndex = 2, date = dayKey.daysAgo(3)),
      CompletionRecord(missionId = "desc_5", dimensionIndex = 2, date = dayKey.daysAgo(4)),
      // Misma acción duplicada el mismo día no debe inflar el puntaje
      CompletionRecord(missionId = "auto_1", dimensionIndex = 0, date = dayKey.today()),
      // Acción de hace 14 días (fuera de ventana de últimos 14 días) debe ignorarse
      CompletionRecord(missionId = "rela_1", dimensionIndex = 3, date = dayKey.daysAgo(14))
    )

    val realScores = HexagonCalculator.calculateRealScores(completions, dayKey)
    assertEquals(2, realScores[0])
    assertEquals(6, realScores[1])
    assertEquals(10, realScores[2])
    assertEquals(0, realScores[3])
    assertEquals(0, realScores[4])
    assertEquals(0, realScores[5])

    val counts = HexagonCalculator.calculateActionsByDimension14d(completions, dayKey)
    assertEquals(1, counts[0])
    assertEquals(3, counts[1])
    assertEquals(5, counts[2])
    assertEquals(0, counts[3])
  }

  @Test
  fun testRealScoresFloatAndLiveHarmony() {
    val dayKey = fixedClock(2026, Calendar.NOVEMBER, 12)
    val completions = listOf(
      CompletionRecord(missionId = "auto_1", dimensionIndex = 0, date = dayKey.today()),
      CompletionRecord(missionId = "auto_2", dimensionIndex = 0, date = dayKey.daysAgo(1)),
      CompletionRecord(missionId = "auto_3", dimensionIndex = 0, date = dayKey.daysAgo(2))
    )

    val floatScores = HexagonCalculator.calculateRealScoresFloat(completions, dayKey)
    assertEquals(6.0f, floatScores[0] ?: 0f, 0.01f)
    assertEquals(0.0f, floatScores[1] ?: 0f, 0.01f)

    val perceived = (0..5).associateWith { 6 }
    val harmony = HexagonCalculator.calculateLiveHarmonyPercentage(
      realScores = HexagonCalculator.calculateRealScores(completions, dayKey),
      perceivedScores = perceived
    )
    assertTrue("La armonía viva debe estar entre 0 y 100", harmony in 1..100)
  }

  @Test
  fun testFindPriorityDimensionRespectsUserChoiceBeforeFallback() {
    val realScores = mapOf(0 to 8, 1 to 2, 2 to 0, 3 to 6, 4 to 6, 5 to 6)
    val perceivedScores = mapOf(0 to 7, 1 to 5, 2 to 4, 3 to 6, 4 to 6, 5 to 6)

    // Si la persona eligió avanzar en Autoestima (0) y Hobbies (4), se respeta su elección
    val chosenPriority = HexagonCalculator.findPriorityDimension(
      realScores = realScores,
      perceivedScores = perceivedScores,
      chosenFocusDimensions = setOf(0, 4)
    )
    assertEquals(4, chosenPriority)

    // Sin elección explícita, sugiere la más baja real (con desempate por percibido)
    val fallbackPriority = HexagonCalculator.findPriorityDimension(
      realScores = realScores,
      perceivedScores = perceivedScores,
      chosenFocusDimensions = emptySet()
    )
    assertEquals(2, fallbackPriority)
  }

  @Test
  fun testFindLowestRealDimensionTieBreaksByPerceived() {
    val realScores = mapOf(0 to 0, 1 to 0, 2 to 4, 3 to 4, 4 to 2, 5 to 6)
    val perceivedScores = mapOf(0 to 7, 1 to 3, 2 to 8, 3 to 5, 4 to 6, 5 to 9)

    val lowest = HexagonCalculator.findLowestRealDimension(realScores, perceivedScores)
    assertEquals(1, lowest)
  }

  @Test
  fun testHelpfulnessSummaryUsesLatestRatingPerMission() {
    val completions = listOf(
      CompletionRecord(
        missionId = "desc_1",
        dimensionIndex = 2,
        date = "2026-11-10",
        helpfulness = HelpfulnessRating.HEAVY,
        completedAtMillis = 100L
      ),
      CompletionRecord(
        missionId = "desc_1",
        dimensionIndex = 2,
        date = "2026-11-12",
        helpfulness = HelpfulnessRating.HELPFUL,
        completedAtMillis = 200L
      ),
      CompletionRecord(
        missionId = "ejer_1",
        dimensionIndex = 1,
        date = "2026-11-11",
        helpfulness = HelpfulnessRating.HEAVY,
        completedAtMillis = 150L
      ),
      CompletionRecord(
        missionId = "rela_1",
        dimensionIndex = 3,
        date = "2026-11-12",
        helpfulness = HelpfulnessRating.NEUTRAL,
        completedAtMillis = 180L
      )
    )

    val summary = HexagonCalculator.calculateHelpfulnessSummary(completions)
    assertEquals(1, summary.helpfulCount)
    assertEquals(1, summary.neutralCount)
    assertEquals(2, summary.heavyCount)
    assertTrue("desc_1 debe quedar como útil según su último registro", "desc_1" in summary.helpfulMissionIds)
    assertTrue("ejer_1 debe quedar marcada como pesada", "ejer_1" in summary.heavyMissionIds)
  }
}
