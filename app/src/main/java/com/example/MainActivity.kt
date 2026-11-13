package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AppScreenState
import com.example.ui.MainViewModel
import com.example.ui.screens.LoadingScreen
import com.example.ui.screens.MainHomeScreen
import com.example.ui.screens.NameInputScreen
import com.example.ui.screens.ProfilePhotoScreen
import com.example.ui.screens.QuestionnaireScreen
import com.example.ui.screens.ReportScreen
import com.example.ui.screens.TransitionScreen
import com.example.ui.screens.ZodiacAndGoalScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  private val viewModel: MainViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      MyApplicationTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          EquilibrioApp(viewModel = viewModel)
        }
      }
    }
  }
}

@Composable
fun EquilibrioApp(viewModel: MainViewModel) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  Crossfade(
    targetState = uiState,
    animationSpec = tween(350),
    label = "screen_crossfade"
  ) { state ->
    when (state) {
      is AppScreenState.Questionnaire -> {
        BackHandler(enabled = state.currentStep > 0) {
          viewModel.previousQuestion()
        }

        QuestionnaireScreen(
          currentStep = state.currentStep,
          scores = state.scores,
          onScoreSelected = { dimIndex, score ->
            viewModel.onScoreChanged(dimIndex, score)
          },
          onNext = {
            viewModel.nextQuestion()
          },
          onPrevious = {
            viewModel.previousQuestion()
          },
          onJumpToStep = { step ->
            viewModel.jumpToQuestion(step)
          }
        )
      }

      is AppScreenState.TransitioningToReport -> {
        TransitionScreen(
          scores = state.scores,
          onComplete = {
            viewModel.onTransitionComplete(state.scores)
          }
        )
      }

      is AppScreenState.Report -> {
        BackHandler {
          viewModel.backToHomeFromReport()
        }

        ReportScreen(
          report = state.report,
          selectedDimensionIndex = state.selectedDimensionIndex,
          filter = state.filter,
          onDimensionSelect = { dimIndex ->
            viewModel.selectDimensionInReport(dimIndex)
          },
          onFilterChange = { filter ->
            viewModel.setReportFilter(filter)
          },
          onRetake = {
            viewModel.retakeAssessment()
          },
          onContinue = {
            viewModel.startProfileSetup()
          }
        )
      }

      is AppScreenState.Loading -> {
        LoadingScreen(
          message = state.quote,
          onFinished = {
            viewModel.onLoadingFinished()
          }
        )
      }

      is AppScreenState.NameInput -> {
        BackHandler {
          viewModel.openSavedReport()
        }

        NameInputScreen(
          initialName = state.currentName,
          onNameSubmitted = { name ->
            viewModel.onNameSubmitted(name)
          }
        )
      }

      is AppScreenState.ProfilePhoto -> {
        BackHandler {
          viewModel.backToNameInput()
        }

        ProfilePhotoScreen(
          currentPhotoUri = state.photoUri,
          onPhotoSelected = { uri ->
            viewModel.onPhotoSelected(uri)
          },
          onContinue = { uri ->
            viewModel.onPhotoConfirmed(uri)
          }
        )
      }

      is AppScreenState.ZodiacAndGoal -> {
        BackHandler {
          viewModel.backToProfilePhoto()
        }

        ZodiacAndGoalScreen(
          initialZodiac = state.zodiacSign,
          initialGoal = state.monthlyGoal,
          onFinished = { zodiacSign, zodiacSymbol, monthlyGoal ->
            viewModel.onZodiacAndGoalFinished(zodiacSign, zodiacSymbol, monthlyGoal)
          }
        )
      }

      is AppScreenState.Home -> {
        MainHomeScreen(
          userProfile = state.userProfile,
          report = state.report,
          onOpenReport = {
            viewModel.openDetailedReportFromHome()
          },
          onUpdateGoal = { newGoal ->
            viewModel.onUpdateGoal(newGoal)
          }
        )
      }
    }
  }
}
