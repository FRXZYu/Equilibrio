package com.example.model

data class ZodiacSign(
  val name: String,
  val symbol: String,
  val dates: String,
  val element: String
)

val AllZodiacSigns = listOf(
  ZodiacSign("Aries", "♈", "21 mar - 19 abr", "Fuego"),
  ZodiacSign("Tauro", "♉", "20 abr - 20 may", "Tierra"),
  ZodiacSign("Géminis", "♊", "21 may - 20 jun", "Aire"),
  ZodiacSign("Cáncer", "♋", "21 jun - 22 jul", "Agua"),
  ZodiacSign("Leo", "♌", "23 jul - 22 ago", "Fuego"),
  ZodiacSign("Virgo", "♍", "23 ago - 22 sep", "Tierra"),
  ZodiacSign("Libra", "♎", "23 sep - 22 oct", "Aire"),
  ZodiacSign("Escorpio", "♏", "23 oct - 21 nov", "Agua"),
  ZodiacSign("Sagitario", "♐", "22 nov - 21 dic", "Fuego"),
  ZodiacSign("Capricornio", "♑", "22 dic - 19 ene", "Tierra"),
  ZodiacSign("Acuario", "♒", "20 ene - 18 feb", "Aire"),
  ZodiacSign("Piscis", "♓", "19 feb - 20 mar", "Agua")
)

/**
 * Perfil de usuario con valores derivados exclusivamente de los registros reales de Room.
 * Sin datos simulados ni valores falsos por defecto.
 */
data class UserProfile(
  val name: String = "",
  val photoUri: String? = null,
  val zodiacSign: String = "Aries",
  val zodiacSymbol: String = "♈",
  val monthlyGoal: String = "Quiero ser mi mejor versión",
  val level: Int = 1,
  val rank: String = "Principiante",
  val exp: Int = 0,
  val expToday: Int = 0,
  val completedMissions: Int = 0,
  val activeStreakDays: Int = 0
) {
  fun withStats(stats: GamificationStats): UserProfile = copy(
    level = stats.level,
    rank = stats.rank,
    exp = stats.totalExp,
    expToday = stats.expToday,
    completedMissions = stats.completedMissionsCount,
    activeStreakDays = stats.activeStreakDays
  )
}
