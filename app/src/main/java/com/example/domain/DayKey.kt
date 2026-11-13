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

  fun currentMillis(): Long = relojMillis()

  /**
   * Clave del día actual ("yyyy-MM-dd") en hora local.
   */
  fun today(): String {
    return format(relojMillis())
  }

  /**
   * Clave del día de ayer ("yyyy-MM-dd") en hora local.
   */
  fun yesterday(): String {
    return daysAgo(1)
  }

  /**
   * Clave de N días atrás ("yyyy-MM-dd") en hora local.
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

  /**
   * Milisegundos restantes hasta las próximas 00:00:00.000 según la hora local del dispositivo.
   */
  fun millisUntilNextMidnight(): Long {
    val now = relojMillis()
    val nextMidnight = Calendar.getInstance().apply {
      timeInMillis = now
      add(Calendar.DAY_OF_YEAR, 1)
      set(Calendar.HOUR_OF_DAY, 0)
      set(Calendar.MINUTE, 0)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
    }
    return (nextMidnight.timeInMillis - now).coerceAtLeast(1_000L)
  }

  companion object {
    val DEFAULT = DayKey()

    fun defaultToday(): String = DEFAULT.today()
    fun defaultYesterday(): String = DEFAULT.yesterday()
  }
}
