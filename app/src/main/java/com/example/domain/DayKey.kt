package com.example.domain

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Helper para manejo de fechas como claves de día ("yyyy-MM-dd") en hora local.
 * Compatible con minSdk 24 (sin java.time).
 * Permite inyectar un reloj (relojMillis) para pruebas unitarias deterministas.
 */
class DayKey(
  private val relojMillis: () -> Long = { System.currentTimeMillis() }
) {

  private val dateFormat: SimpleDateFormat
    get() = SimpleDateFormat("yyyy-MM-dd", Locale.US)

  /**
   * Clave del día actual ("yyyy-MM-dd").
   */
  fun today(): String {
    return format(relojMillis())
  }

  /**
   * Clave del día de ayer ("yyyy-MM-dd").
   */
  fun yesterday(): String {
    return daysAgo(1)
  }

  /**
   * Clave de N días atrás ("yyyy-MM-dd").
   */
  fun daysAgo(days: Int): String {
    val cal = Calendar.getInstance()
    cal.timeInMillis = relojMillis()
    cal.add(Calendar.DAY_OF_YEAR, -days)
    return dateFormat.format(cal.time)
  }

  /**
   * Formatea un timestamp en milisegundos a "yyyy-MM-dd".
   */
  fun format(millis: Long): String {
    return dateFormat.format(Date(millis))
  }

  /**
   * Lista de claves de los últimos [count] días, desde el más antiguo hasta hoy.
   * Por ejemplo, para count = 14: [hoy - 13, ..., ayer, hoy].
   */
  fun getRangeLastDays(count: Int = 14): List<String> {
    val list = mutableListOf<String>()
    for (i in (count - 1) downTo 0) {
      list.add(daysAgo(i))
    }
    return list
  }

  /**
   * Verifica si una fecha "yyyy-MM-dd" cae dentro de los últimos [daysCount] días (incluyendo hoy).
   */
  fun isWithinLastDays(dateStr: String, daysCount: Int = 14): Boolean {
    val range = getRangeLastDays(daysCount).toSet()
    return range.contains(dateStr)
  }

  companion object {
    val DEFAULT = DayKey()

    fun defaultToday(): String = DEFAULT.today()
    fun defaultYesterday(): String = DEFAULT.yesterday()
  }
}
