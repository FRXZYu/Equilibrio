package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.data.AssessmentRepository
import com.example.model.UserProfile
import com.example.ui.AppScreenState
import com.example.ui.MainViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class NavigationFlowTest {

  private lateinit var app: Application
  private lateinit var repository: AssessmentRepository

  @Before
  fun setUp() {
    app = ApplicationProvider.getApplicationContext()
    repository = AssessmentRepository(app)
    repository.resetAssessment()
  }

  @Test
  fun `continueFromReport opens onboarding when profile is incomplete`() {
    repository.saveScores(mapOf(0 to 6, 1 to 7, 2 to 5, 3 to 8, 4 to 6, 5 to 7))

    val viewModel = MainViewModel(app)
    val initialState = viewModel.uiState.value
    assertTrue(initialState is AppScreenState.Report)
    assertEquals(false, (initialState as AppScreenState.Report).isProfileCompleted)

    viewModel.continueFromReport()
    val afterContinue = viewModel.uiState.value
    assertTrue(afterContinue is AppScreenState.Loading)
    assertTrue((afterContinue as AppScreenState.Loading).nextScreen is AppScreenState.NameInput)
  }

  @Test
  fun `continueFromReport returns directly to Home and preserves active tab when profile is completed`() {
    repository.saveScores(mapOf(0 to 6, 1 to 7, 2 to 5, 3 to 8, 4 to 6, 5 to 7))
    repository.saveProfile(
      UserProfile(
        name = "Alex",
        monthlyGoal = "Dormir 8 horas"
      )
    )

    val viewModel = MainViewModel(app)
    assertTrue(viewModel.uiState.value is AppScreenState.Home)

    // User switches to Habilidades (tab 1) or Misiones (tab 0) and opens Estadística Hexagonal
    viewModel.selectHomeTab(1)
    assertEquals(1, viewModel.selectedHomeTab.value)

    viewModel.openDetailedReportFromHome()
    val reportState = viewModel.uiState.value
    assertTrue(reportState is AppScreenState.Report)
    assertEquals(true, (reportState as AppScreenState.Report).isProfileCompleted)

    // Returning from Report goes back to Home on tab 1 without forcing profile setup
    viewModel.continueFromReport()
    assertTrue(viewModel.uiState.value is AppScreenState.Home)
    assertEquals(1, viewModel.selectedHomeTab.value)
  }
}
