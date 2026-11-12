package com.example.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad Room para el registro histórico de completaciones de misiones.
 * Cada fila representa una misión completada en un día local específico con su EXP otorgada.
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
  val expAwarded: Int,
  val date: String // Formato local "yyyy-MM-dd"
)
