package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad Room para acciones o hábitos personalizados creados por la persona con el botón "+".
 * Las acciones base provienen de MissionsProvider sin duplicarse en base de datos.
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
  val durationMinutes: Int = 10,
  val frequency: String = "Diariamente",
  val isHabit: Boolean
)
