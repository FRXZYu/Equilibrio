package com.example.model

/**
 * Modelo de estado para el sistema de Gamificación, EXP, Racha y Misiones.
 */
data class GamificationStats(
  val totalExp: Int = 0,
  val expToday: Int = 0,
  val completedMissionsCount: Int = 0,
  val activeStreakDays: Int = 0,
  val lastActiveDate: String = "",
  val completedMissionIds: Set<String> = emptySet(),
  val savedMissionIds: Set<String> = emptySet()
) {
  companion object {
    const val DAILY_EXP_CAP = 150
    const val EXP_PER_LEVEL = 200
  }

  val level: Int get() = (totalExp / EXP_PER_LEVEL) + 1

  val rank: String get() = when (level) {
    1 -> "Iniciado"
    2 -> "Principiante"
    3 -> "Buscador de Armonía"
    4 -> "Equilibrado"
    5 -> "Enfoque Pleno"
    else -> "Maestro Zen"
  }

  val expInCurrentLevel: Int get() = totalExp % EXP_PER_LEVEL
  val levelProgress: Float get() = (expInCurrentLevel.toFloat() / EXP_PER_LEVEL.toFloat()).coerceIn(0f, 1f)
  val isDailyCapReached: Boolean get() = expToday >= DAILY_EXP_CAP
  val remainingDailyExp: Int get() = (DAILY_EXP_CAP - expToday).coerceAtLeast(0)
}

data class MissionRewardResult(
  val missionId: String,
  val expEarned: Int,
  val cappedByDailyLimit: Boolean,
  val newTotalExp: Int,
  val newLevel: Int,
  val leveledUp: Boolean,
  val newStreak: Int,
  val totalCompleted: Int,
  val message: String
)
