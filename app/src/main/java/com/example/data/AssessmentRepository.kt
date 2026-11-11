package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.analysis.LifeAnalysisEngine
import com.example.model.AssessmentReport

class AssessmentRepository(context: Context) {
  private val prefs: SharedPreferences = context.getSharedPreferences("equilibrio_prefs", Context.MODE_PRIVATE)

  companion object {
    private const val KEY_IS_COMPLETED = "key_is_completed"
    private const val KEY_CURRENT_PREFIX = "key_curr_score_"
    private const val KEY_PREV_PREFIX = "key_prev_score_"
    private const val KEY_HAS_PREVIOUS = "key_has_previous"
    private const val KEY_LAST_UPDATED = "key_last_updated"
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
      // First run: save an initial baseline reference (e.g. 5/10 across dimensions) so user immediately has a reference
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

  // User Profile
  private val KEY_USER_NAME = "key_user_name"
  private val KEY_USER_PHOTO = "key_user_photo"
  private val KEY_USER_ZODIAC = "key_user_zodiac"
  private val KEY_USER_ZODIAC_SYM = "key_user_zodiac_sym"
  private val KEY_USER_GOAL = "key_user_goal"
  private val KEY_PROFILE_COMPLETED = "key_profile_completed"

  fun isProfileCompleted(): Boolean {
    return prefs.getBoolean(KEY_PROFILE_COMPLETED, false)
  }

  fun saveProfile(profile: com.example.model.UserProfile) {
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

  fun loadProfile(): com.example.model.UserProfile {
    val name = prefs.getString(KEY_USER_NAME, "") ?: ""
    val photo = prefs.getString(KEY_USER_PHOTO, null)
    val zodiac = prefs.getString(KEY_USER_ZODIAC, "Aries") ?: "Aries"
    val zodiacSym = prefs.getString(KEY_USER_ZODIAC_SYM, "♈") ?: "♈"
    val goal = prefs.getString(KEY_USER_GOAL, "Quiero ser mi mejor versión") ?: "Quiero ser mi mejor versión"

    return com.example.model.UserProfile(
      name = name,
      photoUri = photo,
      zodiacSign = zodiac,
      zodiacSymbol = zodiacSym,
      monthlyGoal = goal
    )
  }
}
