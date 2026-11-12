package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad Room para misiones personalizadas creadas por el usuario con el botón "+".
 * Las misiones predeterminadas del sistema provienen de MissionsProvider sin duplicarse en base de datos.
 */
@Entity(tableName = "custom_missions")
data class MissionEntity(
  @PrimaryKey
  val id: String,
  val dimensionIndex: Int,
  val title: String,
  val description: String,
  val benefit: String,
  val durationLabel: String,
  val expReward: Int,
  val difficulty: String,
  val isHabit: Boolean
)
