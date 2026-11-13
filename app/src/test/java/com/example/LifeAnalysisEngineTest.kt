package com.example

import com.example.analysis.LifeAnalysisEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LifeAnalysisEngineTest {

  @Test
  fun `test balanced profile produces high harmony and gentle summary`() {
    val scores = mapOf(0 to 9, 1 to 8, 2 to 9, 3 to 9, 4 to 8, 5 to 9)
    val report = LifeAnalysisEngine.generateReport(scores)

    assertTrue("La armonía debe ser alta en perfiles equilibrados", report.harmonyPercentage > 80)
    assertTrue("El promedio debe ser >= 8.0", report.averageScore >= 8.0f)
    assertEquals(0, report.areasToNurtureCount)
    assertEquals(6, report.strengthsCount)
    assertEquals("Equilibrio Armónico", report.archetypeTitle)
  }

  @Test
  fun `test burnout pattern triggers gentle energy balance insight without harsh clinical wording`() {
    val scores = mapOf(0 to 7, 1 to 7, 2 to 3, 3 to 6, 4 to 5, 5 to 9)
    val report = LifeAnalysisEngine.generateReport(scores)

    assertTrue(
      "Debe detectar desbalance entre exigencia y descanso con tono amable",
      report.patternInsights.any { it.tag == "Balance de energía" && it.isAlert }
    )

    val combinedText = buildString {
      append(report.archetypeTitle).append(' ')
      append(report.archetypeSummary).append(' ')
      append(report.coreTakeaway).append(' ')
      report.patternInsights.forEach {
        append(it.title).append(' ').append(it.tag).append(' ').append(it.description).append(' ')
      }
      report.diagnostics.forEach {
        append(it.diagnosisSummary).append(' ').append(it.currentImpact).append(' ')
      }
    }.lowercase()

    val harshTerms = listOf("burnout", "crónico", "patológico", "debilidad", "fallo", "fracaso")
    for (term in harshTerms) {
      assertFalse("No debe contener lenguaje tajante o clínico: $term", combinedText.contains(term))
    }
  }

  @Test
  fun `test score deltas calculate correctly`() {
    val prev = mapOf(0 to 4, 1 to 5, 2 to 6, 3 to 5, 4 to 5, 5 to 7)
    val curr = mapOf(0 to 7, 1 to 5, 2 to 8, 3 to 4, 4 to 6, 5 to 9)

    val report = LifeAnalysisEngine.generateReport(curr, prev)

    assertEquals(3, report.deltas[0])
    assertEquals(0, report.deltas[1])
    assertEquals(-1, report.deltas[3])
    assertEquals(2, report.deltas[5])
  }
}
