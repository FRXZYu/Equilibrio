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

data class UserProfile(
  val name: String = "",
  val photoUri: String? = null,
  val zodiacSign: String = "Aries",
  val zodiacSymbol: String = "♈",
  val monthlyGoal: String = "Quiero ser mi mejor versión",
  val level: Int = 2,
  val rank: String = "Principiante",
  val exp: Int = 200,
  val expToday: Int = 20,
  val completedMissions: Int = 10,
  val activeStreakDays: Int = 3
)
