package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.analysis.LifeAnalysisEngine
import com.example.data.AssessmentRepository
import com.example.model.AssessmentReport
import com.example.model.DimensionsProvider
import com.example.model.ReportFilter
import com.example.model.UserProfile
import com.example.ui.screens.MotivationalQuotes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

sealed interface AppScreenState {
  data class Questionnaire(
    val currentStep: Int = 0,
    val scores: Map<Int, Int> = emptyMap()
  ) : AppScreenState

  data class TransitioningToReport(
    val scores: Map<Int, Int>,
    val previousScores: Map<Int, Int>? = null
  ) : AppScreenState

  data class Report(
    val report: AssessmentReport,
    val selectedDimensionIndex: Int? = null,
    val filter: ReportFilter = ReportFilter.ALL,
    val isProfileCompleted: Boolean = false
  ) : AppScreenState

  data class Loading(
    val quote: String,
    val nextScreen: AppScreenState
  ) : AppScreenState

  data class NameInput(
    val currentName: String = ""
  ) : AppScreenState

  data class ProfilePhoto(
    val name: String,
    val photoUri: String? = null
  ) : AppScreenState

  data class ZodiacAndGoal(
    val name: String,
    val photoUri: String?,
    val zodiacSign: String = "Aries",
    val monthlyGoal: String = "Quiero ser mi mejor versión"
  ) : AppScreenState

  data class Home(
    val userProfile: UserProfile,
    val report: AssessmentReport
  ) : AppScreenState
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
  private val repository = AssessmentRepository(application)

  private val _uiState = MutableStateFlow<AppScreenState>(
    AppScreenState.Questionnaire(currentStep = 0, scores = emptyMap())
  )
  val uiState: StateFlow<AppScreenState> = _uiState.asStateFlow()

  private val _selectedHomeTab = MutableStateFlow(2)
  val selectedHomeTab: StateFlow<Int> = _selectedHomeTab.asStateFlow()

  // Track profile in progress
  private var pendingName: String = ""
  private var pendingPhotoUri: String? = null

  init {
    loadInitialState()
  }

  private fun loadInitialState() {
    val currentScores = repository.loadCurrentScores()
    val previousScores = repository.loadPreviousScores()

    if (currentScores != null && currentScores.size == 6) {
      val report = LifeAnalysisEngine.generateReport(currentScores, previousScores)
      if (repository.isProfileCompleted()) {
        val profile = repository.loadProfile()
        _uiState.value = AppScreenState.Home(userProfile = profile, report = report)
      } else {
        _uiState.value = AppScreenState.Report(
          report = report,
          selectedDimensionIndex = report.lowestDimension.index,
          isProfileCompleted = false
        )
      }
    } else {
      _uiState.value = AppScreenState.Questionnaire(
        currentStep = 0,
        scores = emptyMap()
      )
    }
  }

  fun onScoreChanged(dimensionIndex: Int, score: Int) {
    val current = _uiState.value
    if (current is AppScreenState.Questionnaire) {
      val updated = current.scores.toMutableMap()
      updated[dimensionIndex] = score
      _uiState.update { current.copy(scores = updated) }
    }
  }

  fun nextQuestion() {
    val current = _uiState.value
    if (current is AppScreenState.Questionnaire) {
      val maxSteps = DimensionsProvider.dimensions.size
      val currentScores = current.scores.toMutableMap()
      if (!currentScores.containsKey(current.currentStep)) {
        currentScores[current.currentStep] = 7
      }

      if (current.currentStep < maxSteps - 1) {
        _uiState.update {
          current.copy(
            currentStep = current.currentStep + 1,
            scores = currentScores
          )
        }
      } else {
        finishAssessment(currentScores)
      }
    }
  }

  fun previousQuestion() {
    val current = _uiState.value
    if (current is AppScreenState.Questionnaire && current.currentStep > 0) {
      _uiState.update { current.copy(currentStep = current.currentStep - 1) }
    }
  }

  fun jumpToQuestion(step: Int) {
    val current = _uiState.value
    if (current is AppScreenState.Questionnaire && step in 0 until DimensionsProvider.dimensions.size) {
      _uiState.update { current.copy(currentStep = step) }
    }
  }

  private fun finishAssessment(scores: Map<Int, Int>) {
    repository.saveScores(scores)
    val previousScores = repository.loadPreviousScores()
    _uiState.value = AppScreenState.TransitioningToReport(
      scores = scores,
      previousScores = previousScores
    )
  }

  fun onTransitionComplete(scores: Map<Int, Int>) {
    val previousScores = repository.loadPreviousScores()
    val report = LifeAnalysisEngine.generateReport(scores, previousScores)
    _uiState.value = AppScreenState.Report(
      report = report,
      selectedDimensionIndex = report.lowestDimension.index,
      isProfileCompleted = repository.isProfileCompleted()
    )
  }

  fun selectDimensionInReport(index: Int) {
    val current = _uiState.value
    if (current is AppScreenState.Report) {
      _uiState.update { current.copy(selectedDimensionIndex = index) }
    }
  }

  fun setReportFilter(filter: ReportFilter) {
    val current = _uiState.value
    if (current is AppScreenState.Report) {
      _uiState.update { current.copy(filter = filter) }
    }
  }

  fun continueFromReport() {
    if (repository.isProfileCompleted()) {
      backToHomeFromReport()
    } else {
      startProfileSetup()
    }
  }

  fun selectHomeTab(tabIndex: Int) {
    _selectedHomeTab.value = tabIndex.coerceIn(0, 4)
  }

  fun startProfileSetup() {
    val existingProfile = repository.loadProfile()
    pendingName = existingProfile.name
    pendingPhotoUri = existingProfile.photoUri

    val nextScreen = AppScreenState.NameInput(currentName = existingProfile.name)
    showLoadingTransition(
      quote = "Cada gran transformación comienza con un nombre y una decisión clara.",
      nextScreen = nextScreen
    )
  }

  fun onNameSubmitted(name: String) {
    pendingName = name
    val nextScreen = AppScreenState.ProfilePhoto(name = name, photoUri = pendingPhotoUri)
    showLoadingTransition(
      quote = "Reconocer tu identidad es el primer paso hacia una autoestima inquebrantable.",
      nextScreen = nextScreen
    )
  }

  fun onPhotoSelected(photoUri: String?) {
    pendingPhotoUri = photoUri
  }

  fun onPhotoConfirmed(photoUri: String?) {
    pendingPhotoUri = photoUri
    val nextScreen = AppScreenState.ZodiacAndGoal(
      name = pendingName,
      photoUri = pendingPhotoUri
    )
    showLoadingTransition(
      quote = "La autenticidad es tu mayor superpoder: muéstrate al mundo sin máscaras.",
      nextScreen = nextScreen
    )
  }

  fun onZodiacAndGoalFinished(zodiacSign: String, zodiacSymbol: String, monthlyGoal: String) {
    val updatedProfile = UserProfile(
      name = pendingName,
      photoUri = pendingPhotoUri,
      zodiacSign = zodiacSign,
      zodiacSymbol = zodiacSymbol,
      monthlyGoal = monthlyGoal
    )
    repository.saveProfile(updatedProfile)

    val currentScores = repository.loadCurrentScores() ?: mapOf(0 to 8, 1 to 7, 2 to 6, 3 to 7, 4 to 6, 5 to 9)
    val previousScores = repository.loadPreviousScores()
    val report = LifeAnalysisEngine.generateReport(currentScores, previousScores)

    val homeScreen = AppScreenState.Home(userProfile = updatedProfile, report = report)
    showLoadingTransition(
      quote = "Tus cimientos están listos. Es hora de conquistar tu mejor versión.",
      nextScreen = homeScreen
    )
  }

  fun onUpdateGoal(newGoal: String) {
    repository.updateMonthlyGoal(newGoal)
    val current = _uiState.value
    if (current is AppScreenState.Home) {
      val updatedProfile = current.userProfile.copy(monthlyGoal = newGoal)
      _uiState.value = current.copy(userProfile = updatedProfile)
    }
  }

  fun openDetailedReportFromHome() {
    val current = _uiState.value
    if (current is AppScreenState.Home) {
      _uiState.value = AppScreenState.Report(
        report = current.report,
        selectedDimensionIndex = current.report.lowestDimension.index,
        isProfileCompleted = repository.isProfileCompleted()
      )
    }
  }

  fun openSavedReport() {
    val currentScores = repository.loadCurrentScores() ?: return
    val previousScores = repository.loadPreviousScores()
    val report = LifeAnalysisEngine.generateReport(currentScores, previousScores)
    _uiState.value = AppScreenState.Report(
      report = report,
      selectedDimensionIndex = report.lowestDimension.index,
      isProfileCompleted = repository.isProfileCompleted()
    )
  }

  fun backToNameInput() {
    _uiState.value = AppScreenState.NameInput(currentName = pendingName)
  }

  fun backToProfilePhoto() {
    _uiState.value = AppScreenState.ProfilePhoto(name = pendingName, photoUri = pendingPhotoUri)
  }

  fun backToHomeFromReport() {
    if (repository.isProfileCompleted()) {
      val profile = repository.loadProfile()
      val currentScores = repository.loadCurrentScores() ?: return
      val previousScores = repository.loadPreviousScores()
      val report = LifeAnalysisEngine.generateReport(currentScores, previousScores)
      _uiState.value = AppScreenState.Home(userProfile = profile, report = report)
    }
  }

  private fun showLoadingTransition(quote: String, nextScreen: AppScreenState) {
    _uiState.value = AppScreenState.Loading(
      quote = quote,
      nextScreen = nextScreen
    )
  }

  fun onLoadingFinished() {
    val current = _uiState.value
    if (current is AppScreenState.Loading) {
      _uiState.value = current.nextScreen
    }
  }

  fun retakeAssessment() {
    _uiState.value = AppScreenState.Questionnaire(
      currentStep = 0,
      scores = emptyMap()
    )
  }
}
