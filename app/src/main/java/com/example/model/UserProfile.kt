package com.example.model

data class UserProfile(
  val name: String = "",
  val photoUri: String? = null,
  val zodiacSign: String = "Aries",
  val zodiacSymbol: String = "♈",
  val monthlyGoal: String = "Quiero ser mi mejor versión",
  val chosenFocusDimensions: Set<Int> = emptySet(),
  val level: Int = 1,
  val rank: String = "Iniciado",
  val exp: Int = 0,
  val expToday: Int = 0,
  val completedMissions: Int = 0,
  val activeStreakDays: Int = 0,
  val completedActionsTotal: Int = 0,
  val activeDaysRecent: Int = 0
)

data class ZodiacSign(
  val name: String,
  val symbol: String,
  val dates: String
)

val AllZodiacSigns = listOf(
  ZodiacSign("Aries", "♈", "Mar 21 - Abr 19"),
  ZodiacSign("Tauro", "♉", "Abr 20 - May 20"),
  ZodiacSign("Géminis", "♊", "May 21 - Jun 20"),
  ZodiacSign("Cáncer", "♋", "Jun 21 - Jul 22"),
  ZodiacSign("Leo", "♌", "Jul 23 - Ago 22"),
  ZodiacSign("Virgo", "♍", "Ago 23 - Sep 22"),
  ZodiacSign("Libra", "♎", "Sep 23 - Oct 22"),
  ZodiacSign("Escorpio", "♏", "Oct 23 - Nov 21"),
  ZodiacSign("Sagitario", "♐", "Nov 22 - Dic 21"),
  ZodiacSign("Capricornio", "♑", "Dic 22 - Ene 19"),
  ZodiacSign("Acuario", "♒", "Ene 20 - Feb 18"),
  ZodiacSign("Piscis", "♓", "Feb 19 - Mar 20")
)

data class WellbeingStats(
  val activeDaysStreak: Int = 0,
  val activeDaysLast14: Int = 0,
  val completedActionsTotal: Int = 0,
  val completedTodayCount: Int = 0,
  val helpfulActionsCount: Int = 0,
  val savedMissionIds: Set<String> = emptySet()
)
