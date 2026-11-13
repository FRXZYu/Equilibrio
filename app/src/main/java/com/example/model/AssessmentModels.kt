package com.example.model

enum class ReportFilter {
  ALL,
  TO_NURTURE,
  STRENGTHS
}

enum class HexagonViewMode {
  OVERLAY,   // Current + Previous overlaid
  CURRENT,   // Only Current
  PREVIOUS   // Only Previous
}

enum class ScoreTier(val label: String, val badgeColorHex: Long) {
  CRITICAL("Espacio para cuidar", 0xFFD97706),
  DEVELOPING("En construcción", 0xFF0284C7),
  SOLID("Base estable", 0xFF059669),
  EXCEPTIONAL("Fortaleza serena", 0xFF4F46E5);

  companion object {
    fun fromScore(score: Int): ScoreTier = when (score) {
      in 1..3 -> CRITICAL
      in 4..6 -> DEVELOPING
      in 7..8 -> SOLID
      else -> EXCEPTIONAL
    }
  }
}

data class DimensionDiagnostic(
  val dimension: LifeDimension,
  val score: Int,
  val previousScore: Int?,
  val tier: ScoreTier,
  val corePhilosophy: String,
  val diagnosisSummary: String,
  val currentImpact: String,
  val blindSpots: List<String>,
  val actionableImprovements: List<String>
)

data class PatternInsight(
  val title: String,
  val subtitle: String,
  val tag: String,
  val description: String,
  val recommendation: String,
  val isAlert: Boolean
)

data class AssessmentReport(
  val scores: Map<Int, Int>, // current dimension index -> score 1..10
  val previousScores: Map<Int, Int>?, // previous answers if available
  val averageScore: Float,
  val previousAverageScore: Float?,
  val deltas: Map<Int, Int>, // score - previousScore
  val harmonyPercentage: Int, // 0 to 100 based on standard deviation
  val highestDimension: LifeDimension,
  val lowestDimension: LifeDimension,
  val archetypeTitle: String,
  val archetypeSummary: String,
  val coreTakeaway: String,
  val wisdomQuote: String,
  val wisdomAuthor: String,
  val patternInsights: List<PatternInsight>,
  val diagnostics: List<DimensionDiagnostic>,
  val areasToNurtureCount: Int,
  val strengthsCount: Int
)
