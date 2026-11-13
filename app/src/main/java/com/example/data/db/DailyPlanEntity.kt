package com.example.data.db

import androidx.room.Entity

/**
 * Plan diario personalizado para una fecha local ("yyyy-MM-dd").
 * Permite actualizar automáticamente las acciones cada día a las 00:00
 * y recordar qué se ofreció recientemente para no repetir sin sentido.
 */
@Entity(
  tableName = "daily_plan",
  primaryKeys = ["date", "slotIndex"]
)
data class DailyPlanEntity(
  val date: String,
  val slotIndex: Int,
  val missionId: String,
  val dimensionIndex: Int,
  val wasSwapped: Boolean = false
)
