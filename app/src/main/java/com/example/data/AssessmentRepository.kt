package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.analysis.LifeAnalysisEngine
import com.example.model.AssessmentReport
import com.example.model.GamificationStats
import com.example.model.MissionRewardResult
import com.example.model.UserProfile
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AssessmentRepository(context: Context) {
  private val prefs: SharedPreferences = context.getSharedPreferences("equilibrio_prefs", Context.MODE_PRIVATE)

  companion object {
    private const val KEY_IS_COMPLETED = "key_is_completed"
    private const val KEY_CURRENT_PREFIX = "key_curr_score_"
    private const val KEY_PREV_PREFIX = "key_prev_score_"
    private const val KEY_HAS_PREVIOUS = "key_has_previous"
    private const val KEY_LAST_UPDATED = "key_last_updated"

    // Gamification & Streak Keys
    private const val KEY_TOTAL_EXP = "key_total_exp"
    private const val KEY_EXP_TODAY = "key_exp_today"
    private const val KEY_COMPLETED_MISSIONS_COUNT = "key_completed_missions_count"
    private const val KEY_ACTIVE_STREAK = "key_active_streak"
    private const val KEY_LAST_ACTIVE_DATE = "key_last_active_date"
    private const val KEY_COMPLETED_MISSION_IDS = "key_completed_mission_ids"
    private const val KEY_SAVED_MISSION_IDS = "key_saved_mission_ids"
  }

  fun hasCompletedAssessment(): Boolean {
    return prefs.getBoolean(KEY_IS_COMPLETED, false)
  }

  fun hasPreviousAssessment(): Boolean {
    return prefs.getBoolean(KEY_HAS_PREVIOUS, false)
  }

  fun saveScores(newScores: Map<Int, Int>) {
    val existingCurrent = loadCurrentScores()
    val editor = prefs.edit()

    // Move existing current to previous
    if (existingCurrent != null && existingCurrent.size == 6) {
      existingCurrent.forEach { (index, score) ->
        editor.putInt("$KEY_PREV_PREFIX$index", score)
      }
      editor.putBoolean(KEY_HAS_PREVIOUS, true)
    } else if (!hasPreviousAssessment()) {
      // First run: save an initial baseline reference (5/10) so user immediately has a reference
      for (i in 0..5) {
        editor.putInt("$KEY_PREV_PREFIX$i", 5)
      }
      editor.putBoolean(KEY_HAS_PREVIOUS, true)
    }

    // Save new scores as current
    newScores.forEach { (index, score) ->
      editor.putInt("$KEY_CURRENT_PREFIX$index", score)
    }
    editor.putBoolean(KEY_IS_COMPLETED, true)
    editor.putLong(KEY_LAST_UPDATED, System.currentTimeMillis())
    editor.apply()
  }

  fun loadCurrentScores(): Map<Int, Int>? {
    if (!hasCompletedAssessment()) return null
    val result = mutableMapOf<Int, Int>()
    for (i in 0..5) {
      val score = prefs.getInt("$KEY_CURRENT_PREFIX$i", -1)
      if (score != -1) {
        result[i] = score
      }
    }
    return if (result.size == 6) result else null
  }

  fun loadPreviousScores(): Map<Int, Int>? {
    if (!hasPreviousAssessment()) return null
    val result = mutableMapOf<Int, Int>()
    for (i in 0..5) {
      val score = prefs.getInt("$KEY_PREV_PREFIX$i", -1)
      if (score != -1) {
        result[i] = score
      }
    }
    return if (result.size == 6) result else null
  }

  fun getSavedReport(): AssessmentReport? {
    val current = loadCurrentScores() ?: return null
    val previous = loadPreviousScores()
    return LifeAnalysisEngine.generateReport(current, previous)
  }

  fun resetAssessment() {
    prefs.edit().clear().apply()
  }

  // ==========================================
  // Gamificación, EXP, Racha y Misiones
  // ==========================================

  private fun getTodayDate(): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
  }

  private fun getYesterdayDate(): String {
    val cal = Calendar.getInstance()
    cal.add(Calendar.DAY_OF_YEAR, -1)
    return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
  }

  fun loadGamificationStats(): GamificationStats {
    val today = getTodayDate()
    val lastDate = prefs.getString(KEY_LAST_ACTIVE_DATE, "") ?: ""
    val totalExp = prefs.getInt(KEY_TOTAL_EXP, 0)
    var expToday = prefs.getInt(KEY_EXP_TODAY, 0)
    val completedCount = prefs.getInt(KEY_COMPLETED_MISSIONS_COUNT, 0)
    val streak = prefs.getInt(KEY_ACTIVE_STREAK, 0)
    val completedIds = prefs.getStringSet(KEY_COMPLETED_MISSION_IDS, emptySet())?.toSet() ?: emptySet()
    val savedIds = prefs.getStringSet(KEY_SAVED_MISSION_IDS, emptySet())?.toSet() ?: emptySet()

    // Si es un nuevo día, reiniciar el EXP ganado hoy para permitir nuevas ganancias hasta el tope
    if (lastDate.isNotEmpty() && lastDate != today) {
      expToday = 0
      prefs.edit().putInt(KEY_EXP_TODAY, 0).apply()
    }

    return GamificationStats(
      totalExp = totalExp,
      expToday = expToday,
      completedMissionsCount = completedCount,
      activeStreakDays = streak,
      lastActiveDate = if (lastDate.isEmpty()) today else lastDate,
      completedMissionIds = completedIds,
      savedMissionIds = savedIds
    )
  }

  fun completeMission(missionId: String, expReward: Int): MissionRewardResult {
    val stats = loadGamificationStats()
    val today = getTodayDate()
    val yesterday = getYesterdayDate()

    val oldLevel = stats.level
    val completedIds = stats.completedMissionIds.toMutableSet()
    val isAlreadyCompleted = completedIds.contains(missionId)
    completedIds.add(missionId)

    // Quitar de guardadas si estaba allí
    val savedIds = stats.savedMissionIds.toMutableSet()
    savedIds.remove(missionId)

    // Calcular EXP real considerando el tope diario de 150 EXP
    val remainingCap = (GamificationStats.DAILY_EXP_CAP - stats.expToday).coerceAtLeast(0)
    val capped = remainingCap <= 0
    val actualExpGained = if (!isAlreadyCompleted && remainingCap > 0) {
      minOf(expReward, remainingCap)
    } else {
      0
    }

    val newTotalExp = stats.totalExp + actualExpGained
    val newExpToday = stats.expToday + actualExpGained
    val newCompletedCount = if (!isAlreadyCompleted) stats.completedMissionsCount + 1 else stats.completedMissionsCount

    // Racha activa: si la última actividad registrada fue hoy, conserva la racha;
    // si fue ayer, avanza un día; de lo contrario inicia en 1 día.
    val newStreak = when (stats.lastActiveDate) {
      today -> stats.activeStreakDays
      yesterday -> stats.activeStreakDays + 1
      else -> if (stats.lastActiveDate.isEmpty()) stats.activeStreakDays else 1
    }

    val newLevel = (newTotalExp / GamificationStats.EXP_PER_LEVEL) + 1
    val leveledUp = newLevel > oldLevel

    prefs.edit()
      .putInt(KEY_TOTAL_EXP, newTotalExp)
      .putInt(KEY_EXP_TODAY, newExpToday)
      .putInt(KEY_COMPLETED_MISSIONS_COUNT, newCompletedCount)
      .putInt(KEY_ACTIVE_STREAK, newStreak)
      .putString(KEY_LAST_ACTIVE_DATE, today)
      .putStringSet(KEY_COMPLETED_MISSION_IDS, completedIds)
      .putStringSet(KEY_SAVED_MISSION_IDS, savedIds)
      .apply()

    val message = if (actualExpGained > 0) {
      if (leveledUp) "¡Subiste a Nivel $newLevel! +$actualExpGained EXP" else "+$actualExpGained EXP ganados"
    } else if (capped) {
      "Misión completada. Se alcanzó el límite de 150 EXP diarios para cuidar tu ritmo."
    } else {
      "Misión completada con éxito."
    }

    return MissionRewardResult(
      missionId = missionId,
      expEarned = actualExpGained,
      cappedByDailyLimit = capped,
      newTotalExp = newTotalExp,
      newLevel = newLevel,
      leveledUp = leveledUp,
      newStreak = newStreak,
      totalCompleted = newCompletedCount,
      message = message
    )
  }

  fun toggleSaveMissionForLater(missionId: String): Boolean {
    val stats = loadGamificationStats()
    val savedIds = stats.savedMissionIds.toMutableSet()
    val isNowSaved = if (savedIds.contains(missionId)) {
      savedIds.remove(missionId)
      false
    } else {
      savedIds.add(missionId)
      true
    }
    prefs.edit().putStringSet(KEY_SAVED_MISSION_IDS, savedIds).apply()
    return isNowSaved
  }

  // ==========================================
  // User Profile
  // ==========================================
  private val KEY_USER_NAME = "key_user_name"
  private val KEY_USER_PHOTO = "key_user_photo"
  private val KEY_USER_ZODIAC = "key_user_zodiac"
  private val KEY_USER_ZODIAC_SYM = "key_user_zodiac_sym"
  private val KEY_USER_GOAL = "key_user_goal"
  private val KEY_PROFILE_COMPLETED = "key_profile_completed"

  fun isProfileCompleted(): Boolean {
    return prefs.getBoolean(KEY_PROFILE_COMPLETED, false)
  }

  fun saveProfile(profile: UserProfile) {
    prefs.edit()
      .putString(KEY_USER_NAME, profile.name)
      .putString(KEY_USER_PHOTO, profile.photoUri)
      .putString(KEY_USER_ZODIAC, profile.zodiacSign)
      .putString(KEY_USER_ZODIAC_SYM, profile.zodiacSymbol)
      .putString(KEY_USER_GOAL, profile.monthlyGoal)
      .putBoolean(KEY_PROFILE_COMPLETED, true)
      .apply()
  }

  fun updateMonthlyGoal(goal: String) {
    prefs.edit().putString(KEY_USER_GOAL, goal).apply()
  }

  fun loadProfile(): UserProfile {
    val name = prefs.getString(KEY_USER_NAME, "") ?: ""
    val photo = prefs.getString(KEY_USER_PHOTO, null)
    val zodiac = prefs.getString(KEY_USER_ZODIAC, "Aries") ?: "Aries"
    val zodiacSym = prefs.getString(KEY_USER_ZODIAC_SYM, "♈") ?: "♈"
    val goal = prefs.getString(KEY_USER_GOAL, "Quiero ser mi mejor versión") ?: "Quiero ser mi mejor versión"

    val stats = loadGamificationStats()
    return UserProfile(
      name = name,
      photoUri = photo,
      zodiacSign = zodiac,
      zodiacSymbol = zodiacSym,
      monthlyGoal = goal,
      level = stats.level,
      rank = stats.rank,
      exp = stats.totalExp,
      expToday = stats.expToday,
      completedMissions = stats.completedMissionsCount,
      activeStreakDays = stats.activeStreakDays
    )
  }
}
