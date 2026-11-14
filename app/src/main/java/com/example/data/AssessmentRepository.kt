package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.analysis.LifeAnalysisEngine
import com.example.model.AssessmentReport
import com.example.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AssessmentRepository(context: Context) {
  private val prefs: SharedPreferences = context.getSharedPreferences("equilibrio_prefs", Context.MODE_PRIVATE)

  private val _savedMissionIdsFlow = MutableStateFlow(
    prefs.getStringSet(KEY_SAVED_MISSION_IDS, emptySet())?.toSet() ?: emptySet()
  )
  val savedMissionIdsFlow: StateFlow<Set<String>> = _savedMissionIdsFlow.asStateFlow()

  private val _chosenFocusDimensionsFlow = MutableStateFlow(readFocusDimensions())
  val chosenFocusDimensionsFlow: StateFlow<Set<Int>> = _chosenFocusDimensionsFlow.asStateFlow()

  private val _baseProfileFlow = MutableStateFlow(readBaseProfile())
  val baseProfileFlow: StateFlow<UserProfile> = _baseProfileFlow.asStateFlow()

  companion object {
    private const val KEY_IS_COMPLETED = "key_is_completed"
    private const val KEY_CURRENT_PREFIX = "key_curr_score_"
    private const val KEY_PREV_PREFIX = "key_prev_score_"
    private const val KEY_HAS_PREVIOUS = "key_has_previous"
    private const val KEY_LAST_UPDATED = "key_last_updated"
    private const val KEY_SAVED_MISSION_IDS = "key_saved_mission_ids"
    private const val KEY_FOCUS_DIMENSIONS = "key_focus_dimensions"

    private const val KEY_USER_NAME = "key_user_name"
    private const val KEY_USER_PHOTO = "key_user_photo"
    private const val KEY_USER_ZODIAC = "key_user_zodiac"
    private const val KEY_USER_ZODIAC_SYMBOL = "key_user_zodiac_symbol"
    private const val KEY_USER_GOAL = "key_user_goal"
    private const val KEY_PROFILE_COMPLETED = "key_profile_completed"
    private const val DEFAULT_GOAL = "Cuidarme con pasos pequeños y realistas"
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

    if (existingCurrent != null && existingCurrent.size == 6) {
      existingCurrent.forEach { (index, score) ->
        editor.putInt("$KEY_PREV_PREFIX$index", score)
      }
      editor.putBoolean(KEY_HAS_PREVIOUS, true)
    } else if (!hasPreviousAssessment()) {
      for (i in 0..5) {
        editor.putInt("$KEY_PREV_PREFIX$i", 5)
      }
      editor.putBoolean(KEY_HAS_PREVIOUS, true)
    }

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
    _savedMissionIdsFlow.value = emptySet()
    _chosenFocusDimensionsFlow.value = emptySet()
    _baseProfileFlow.value = readBaseProfile()
  }

  // ==========================================
  // Áreas de enfoque elegidas por la persona
  // ==========================================

  private fun readFocusDimensions(): Set<Int> {
    val raw = prefs.getString(KEY_FOCUS_DIMENSIONS, "") ?: ""
    if (raw.isBlank()) return emptySet()
    return raw.split(",")
      .mapNotNull { it.trim().toIntOrNull() }
      .filter { it in 0..5 }
      .toSet()
  }

  fun saveFocusDimensions(dimensions: Set<Int>) {
    val clean = dimensions.filter { it in 0..5 }.toSet()
    val serialized = clean.sorted().joinToString(",")
    prefs.edit().putString(KEY_FOCUS_DIMENSIONS, serialized).apply()
    _chosenFocusDimensionsFlow.value = clean
    _baseProfileFlow.value = _baseProfileFlow.value.copy(chosenFocusDimensions = clean)
  }

  fun toggleFocusDimension(dimensionIndex: Int): Set<Int> {
    if (dimensionIndex !in 0..5) return _chosenFocusDimensionsFlow.value
    val updated = _chosenFocusDimensionsFlow.value.toMutableSet()
    if (updated.contains(dimensionIndex)) {
      updated.remove(dimensionIndex)
    } else {
      updated.add(dimensionIndex)
    }
    saveFocusDimensions(updated)
    return updated
  }

  // ==========================================
  // Acciones guardadas para más tarde
  // ==========================================

  fun toggleSaveMissionForLater(missionId: String): Boolean {
    val savedIds = (prefs.getStringSet(KEY_SAVED_MISSION_IDS, emptySet()) ?: emptySet()).toMutableSet()
    val isNowSaved = if (savedIds.contains(missionId)) {
      savedIds.remove(missionId)
      false
    } else {
      savedIds.add(missionId)
      true
    }
    prefs.edit().putStringSet(KEY_SAVED_MISSION_IDS, savedIds).apply()
    _savedMissionIdsFlow.value = savedIds.toSet()
    return isNowSaved
  }

  fun removeSavedMission(missionId: String) {
    val savedIds = (prefs.getStringSet(KEY_SAVED_MISSION_IDS, emptySet()) ?: emptySet()).toMutableSet()
    if (savedIds.remove(missionId)) {
      prefs.edit().putStringSet(KEY_SAVED_MISSION_IDS, savedIds).apply()
      _savedMissionIdsFlow.value = savedIds.toSet()
    }
  }

  // ==========================================
  // Perfil sereno del usuario
  // ==========================================

  fun isProfileCompleted(): Boolean {
    return prefs.getBoolean(KEY_PROFILE_COMPLETED, false)
  }

  fun saveProfile(profile: UserProfile) {
    val cleanFocus = profile.chosenFocusDimensions.filter { it in 0..5 }.toSet()
    prefs.edit()
      .putString(KEY_USER_NAME, profile.name)
      .putString(KEY_USER_PHOTO, profile.photoUri)
      .putString(KEY_USER_ZODIAC, profile.zodiacSign.ifBlank { "Aries" })
      .putString(KEY_USER_ZODIAC_SYMBOL, profile.zodiacSymbol.ifBlank { "♈" })
      .putString(KEY_USER_GOAL, profile.monthlyGoal.ifBlank { DEFAULT_GOAL })
      .putString(KEY_FOCUS_DIMENSIONS, cleanFocus.sorted().joinToString(","))
      .putBoolean(KEY_PROFILE_COMPLETED, true)
      .apply()
    _chosenFocusDimensionsFlow.value = cleanFocus
    _baseProfileFlow.value = readBaseProfile()
  }

  fun updateMonthlyGoal(goal: String) {
    val sanitized = goal.trim().ifBlank { DEFAULT_GOAL }
    prefs.edit().putString(KEY_USER_GOAL, sanitized).apply()
    _baseProfileFlow.value = _baseProfileFlow.value.copy(monthlyGoal = sanitized)
  }

  private fun readBaseProfile(): UserProfile {
    val name = prefs.getString(KEY_USER_NAME, "") ?: ""
    val photo = prefs.getString(KEY_USER_PHOTO, null)
    val zodiac = prefs.getString(KEY_USER_ZODIAC, "Aries") ?: "Aries"
    val zodiacSymbol = prefs.getString(KEY_USER_ZODIAC_SYMBOL, "♈") ?: "♈"
    val goal = prefs.getString(KEY_USER_GOAL, DEFAULT_GOAL) ?: DEFAULT_GOAL
    val focus = readFocusDimensions()
    return UserProfile(
      name = name,
      photoUri = photo,
      zodiacSign = zodiac,
      zodiacSymbol = zodiacSymbol,
      monthlyGoal = goal,
      chosenFocusDimensions = focus
    )
  }

  fun loadProfile(): UserProfile = readBaseProfile()
}
