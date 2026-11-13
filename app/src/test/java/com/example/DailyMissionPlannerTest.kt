package com.example

import com.example.domain.CompletionRecord
import com.example.domain.DailyMissionPlanner
import com.example.domain.DailyPlanHistoryItem
import com.example.domain.DayKey
import com.example.domain.HelpfulnessRating
import com.example.model.MissionsProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class DailyMissionPlannerTest {

  private fun fixedDayKey(year: Int, month: Int, day: Int): DayKey {
    val cal = Calendar.getInstance().apply {
      set(Calendar.YEAR, year)
      set(Calendar.MONTH, month)
      set(Calendar.DAY_OF_MONTH, day)
      set(Calendar.HOUR_OF_DAY, 9)
      set(Calendar.MINUTE, 0)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
    }
    val millis = cal.timeInMillis
    return DayKey { millis }
  }

  @Test
  fun testPlansThreeActionsPrioritizingChosenFocusDimensions() {
    val dayKey = fixedDayKey(2026, Calendar.NOVEMBER, 12)
    val scores = (0..5).associateWith { 6 }
    val catalog = MissionsProvider.generateMissionsForUser(scores, setOf(4)).map { it.toCandidate() }

    val plan = DailyMissionPlanner.planDailyMissions(
      todayKey = dayKey.today(),
      catalog = catalog,
      chosenFocusDimensions = setOf(4),
      perceivedScores = scores,
      realScores = (0..5).associateWith { 0 },
      recentCompletions = emptyList(),
      recentPlanHistory = emptyList(),
      dayKey = dayKey
    )

    assertEquals(DailyMissionPlanner.DAILY_ACTION_COUNT, plan.size)
    assertEquals(plan.size, plan.map { it.id }.distinct().size)
    // Las primeras ranuras respetan el área elegida (Hobbies = 4)
    assertEquals(4, plan[0].dimensionIndex)
    assertEquals(4, plan[1].dimensionIndex)
    // La tercera ranura abre variedad amable en otra área
    assertNotEquals(4, plan[2].dimensionIndex)
  }

  @Test
  fun testCooldownAvoidsRepeatingRecentOfferedOrCompletedActions() {
    val dayKey = fixedDayKey(2026, Calendar.NOVEMBER, 12)
    val scores = (0..5).associateWith { 5 }
    val catalog = MissionsProvider.generateMissionsForUser(scores, setOf(2)).map { it.toCandidate() }

    val recentlyOffered = listOf("desc_1", "desc_2", "desc_3")
    val planHistory = recentlyOffered.mapIndexed { idx, id ->
      DailyPlanHistoryItem(date = dayKey.yesterday(), missionId = id, slotIndex = idx)
    }
    val recentCompletions = listOf(
      CompletionRecord(missionId = "desc_4", dimensionIndex = 2, date = dayKey.daysAgo(2))
    )

    val plan = DailyMissionPlanner.planDailyMissions(
      todayKey = dayKey.today(),
      catalog = catalog,
      chosenFocusDimensions = setOf(2),
      perceivedScores = scores,
      realScores = (0..5).associateWith { 0 },
      recentCompletions = recentCompletions,
      recentPlanHistory = planHistory,
      dayKey = dayKey
    )

    val plannedIds = plan.map { it.id }.toSet()
    assertTrue("No debe repetir las acciones ofrecidas ayer en Descanso", recentlyOffered.none { it in plannedIds })
    assertTrue("No debe repetir desc_4 completada hace 2 días", "desc_4" !in plannedIds)
  }

  @Test
  fun testDownRanksActionsRatedHeavyAndKeepsHelpfulOnesEligible() {
    val dayKey = fixedDayKey(2026, Calendar.NOVEMBER, 12)
    val scores = (0..5).associateWith { 5 }
    val catalog = MissionsProvider.generateMissionsForUser(scores, setOf(1)).map { it.toCandidate() }

    val completions = listOf(
      CompletionRecord(
        missionId = "ejer_1",
        dimensionIndex = 1,
        date = dayKey.daysAgo(10),
        helpfulness = HelpfulnessRating.HEAVY,
        completedAtMillis = 100L
      ),
      CompletionRecord(
        missionId = "ejer_2",
        dimensionIndex = 1,
        date = dayKey.daysAgo(10),
        helpfulness = HelpfulnessRating.HELPFUL,
        completedAtMillis = 200L
      )
    )

    val plan = DailyMissionPlanner.planDailyMissions(
      todayKey = dayKey.today(),
      catalog = catalog,
      chosenFocusDimensions = setOf(1),
      perceivedScores = scores,
      realScores = (0..5).associateWith { 0 },
      recentCompletions = completions,
      recentPlanHistory = emptyList(),
      dayKey = dayKey
    )

    val plannedIds = plan.map { it.id }.toSet()
    assertTrue("ejer_1 marcada como pesada no debe elegirse habiendo alternativas", "ejer_1" !in plannedIds)
    assertTrue("ejer_2 marcada como útil debe priorizarse", "ejer_2" in plannedIds)
  }

  @Test
  fun testPickReplacementMissionReturnsDistinctAlternative() {
    val dayKey = fixedDayKey(2026, Calendar.NOVEMBER, 12)
    val scores = (0..5).associateWith { 5 }
    val catalog = MissionsProvider.generateMissionsForUser(scores, setOf(0)).map { it.toCandidate() }

    val currentDayIds = setOf("auto_1", "auto_2", "ejer_1")
    val replacing = catalog.first { it.id == "auto_1" }

    val replacement = DailyMissionPlanner.pickReplacementMission(
      todayKey = dayKey.today(),
      currentDayMissionIds = currentDayIds,
      replacingMission = replacing,
      catalog = catalog,
      chosenFocusDimensions = setOf(0),
      recentCompletions = emptyList(),
      recentPlanHistory = emptyList(),
      dayKey = dayKey
    )

    assertNotNull(replacement)
    assertNotEquals("auto_1", replacement?.id)
    assertTrue(replacement?.id !in currentDayIds)
    assertEquals(0, replacement?.dimensionIndex)
  }
}
