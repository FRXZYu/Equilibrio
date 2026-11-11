package com.example

import com.example.analysis.LifeAnalysisEngine
import com.example.model.DimensionsProvider
import com.example.model.ScoreTier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LifeAnalysisEngineTest {

  @Test
  fun testAllDimensionsHandled() {
    val scores = mapOf(
      0 to 8, // Autoestima
      1 to 6, // Ejercicio
      2 to 4, // Descanso
      3 to 7, // Relaciones
      4 to 5, // Hobbies
      5 to 9  // Excelencia
    )

    val report = LifeAnalysisEngine.generateReport(scores)

    assertEquals(6, report.diagnostics.size)
    assertEquals(DimensionsProvider.dimensions.size, report.diagnostics.size)
    assertEquals(9, report.scores[5])
    assertEquals(4, report.scores[2])
    assertEquals("Excelencia", report.highestDimension.title)
    assertEquals("Descanso", report.lowestDimension.title)
  }

  @Test
  fun testBurnoutCrossPatternDetected() {
    // Excelencia 9, Descanso 3
    val scores = mapOf(
      0 to 7,
      1 to 7,
      2 to 3, // Low descanso
      3 to 7,
      4 to 7,
      5 to 10 // High excelencia
    )

    val report = LifeAnalysisEngine.generateReport(scores)
    val hasBurnoutAlert = report.patternInsights.any { 
      it.tag.contains("Burnout") || (it.subtitle.contains("Excelencia") && it.subtitle.contains("Descanso"))
    }

    assertTrue("Burnout cross-pattern should be detected", hasBurnoutAlert)
    assertEquals(1, report.weaknessesCount) // Only Descanso is <= 6
  }

  @Test
  fun testComparisonWithPreviousScores() {
    val currentScores = mapOf(0 to 9, 1 to 7, 2 to 6, 3 to 8, 4 to 6, 5 to 9)
    val prevScores = mapOf(0 to 6, 1 to 5, 2 to 7, 3 to 7, 4 to 4, 5 to 9)

    val report = LifeAnalysisEngine.generateReport(currentScores, prevScores)

    assertNotNull(report.previousScores)
    assertEquals(3, report.deltas[0]) // Autoestima: 9 - 6 = +3
    assertEquals(2, report.deltas[1]) // Ejercicio: 7 - 5 = +2
    assertEquals(-1, report.deltas[2]) // Descanso: 6 - 7 = -1
    assertEquals(0, report.deltas[5]) // Excelencia: 9 - 9 = 0
    assertNotNull(report.wisdomQuote)
    assertNotNull(report.coreTakeaway)
  }
}
