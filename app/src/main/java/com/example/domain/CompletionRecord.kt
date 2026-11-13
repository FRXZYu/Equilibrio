package com.example.domain

/**
 * Valoración amable posterior a realizar una acción cotidiana.
 * Ayuda a revisar qué sirve realmente y ajustar futuras sugerencias.
 */
enum class HelpfulnessRating(val label: String, val shortLabel: String) {
  HELPFUL("Me ayudó", "Ayudó"),
  NEUTRAL("Fue neutro", "Neutro"),
  HEAVY("Se sintió pesado", "Pesado");

  companion object {
    fun fromStorage(raw: String?): HelpfulnessRating? {
      if (raw.isNullOrBlank()) return null
      return entries.firstOrNull { it.name.equals(raw, ignoreCase = true) }
    }
  }
}

/**
 * Modelo de dominio puro para una acción de cuidado registrada.
 * Sin dependencias del framework de Android ni puntajes de EXP.
 */
data class CompletionRecord(
  val id: Long = 0,
  val missionId: String,
  val dimensionIndex: Int,
  val date: String, // "yyyy-MM-dd" en hora local
  val helpfulness: HelpfulnessRating? = null,
  val reflectionNote: String = "",
  val completedAtMillis: Long = 0L
)
