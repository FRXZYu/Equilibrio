package com.example.model

/**
 * Perfil de usuario sereno y enfocado, con métricas suaves derivadas 100% de Room y preferencias locales.
 * Sin niveles artificiales, rangos ni puntos que confundan bienestar con gamificación.
 */
data class UserProfile(
  val name: String = "",
  val photoUri: String? = null,
  val monthlyGoal: String = "Cuidarme con pasos pequeños y realistas",
  val chosenFocusDimensions: Set<Int> = emptySet(),
  val completedActionsTotal: Int = 0,
  val activeDaysRecent: Int = 0,
  val activeStreakDays: Int = 0
)

/**
 * Resumen amable de seguimiento y utilidad real de las acciones realizadas.
 */
data class WellbeingStats(
  val activeDaysStreak: Int = 0,
  val activeDaysLast14: Int = 0,
  val completedActionsTotal: Int = 0,
  val completedTodayCount: Int = 0,
  val helpfulActionsCount: Int = 0,
  val savedMissionIds: Set<String> = emptySet()
)
