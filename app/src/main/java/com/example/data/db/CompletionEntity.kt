package com.example.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad Room para el registro histórico de acciones realizadas.
 * Conserva la fecha local, valoración de utilidad ("Me ayudó", "Neutro", "Pesado") y reflexión breve.
 */
@Entity(
  tableName = "completions",
  indices = [
    Index(value = ["missionId", "date"]),
    Index(value = ["date"]),
    Index(value = ["dimensionIndex"])
  ]
)
data class CompletionEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val missionId: String,
  val dimensionIndex: Int,
  val date: String, // Formato local "yyyy-MM-dd"
  val helpfulRating: String? = null,
  val reflectionNote: String = "",
  val completedAtMillis: Long = System.currentTimeMillis(),
  val expAwarded: Int = 0
)
