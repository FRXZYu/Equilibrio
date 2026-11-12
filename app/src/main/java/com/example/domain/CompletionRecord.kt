package com.example.domain

/**
 * Modelo de dominio puro para una completación de misión.
 * Sin dependencias del framework de Android.
 */
data class CompletionRecord(
  val id: Long = 0,
  val missionId: String,
  val dimensionIndex: Int,
  val expAwarded: Int,
  val date: String // "yyyy-MM-dd"
)
