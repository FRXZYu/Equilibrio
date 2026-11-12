package com.example.model

/**
 * Modelo de estado para el sistema de Gamificación, EXP, Racha y Misiones.
 * Incluye cálculo dinámico de nivel, rango, límite diario y progreso.
 */
data class GamificationStats(
  val totalExp: Int = 200,
  val expToday: Int = 20,
  val completedMissionsCount: Int = 10,
  val activeStreakDays: Int = 3,
  val lastActiveDate: String = "",
  val completedMissionIds: Set<String> = emptySet(),
  val savedMissionIds: Set<String> = emptySet()
) {
  // Límite diario de EXP para evitar abusos o sobrecarga mental
  companion object {
    const val DAILY_EXP_CAP = 150
    const val EXP_PER_LEVEL = 150
  }

  // Nivel calculado a partir del EXP total
  val level: Int get() = (totalExp / EXP_PER_LEVEL) + 1

  // Rango correspondiente según el nivel alcanzado
  val rank: String get() = when (level) {
    1 -> "Iniciado"
    2 -> "Principiante"
    3 -> "Buscador de Armonía"
    4 -> "Equilibrado"
    5 -> "Enfoque Pleno"
    else -> "Maestro Zen"
  }

  // EXP acumulado dentro del nivel actual
  val expInCurrentLevel: Int get() = totalExp % EXP_PER_LEVEL

  // Progreso de 0.0f a 1.0f para la barra de nivel
  val levelProgress: Float get() = (expInCurrentLevel.toFloat() / EXP_PER_LEVEL.toFloat()).coerceIn(0f, 1f)

  // Verifica si se alcanzó el límite diario de EXP
  val isDailyCapReached: Boolean get() = expToday >= DAILY_EXP_CAP

  // Cuánto EXP queda disponible hoy antes de alcanzar el límite
  val remainingDailyExp: Int get() = (DAILY_EXP_CAP - expToday).coerceAtLeast(0)
}

/**
 * Resultado devuelto al completar una misión
 */
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
