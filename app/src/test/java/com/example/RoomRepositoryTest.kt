package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AssessmentRepository
import com.example.data.BalanceRepository
import com.example.data.db.AppDatabase
import com.example.data.db.MissionEntity
import com.example.domain.DayKey
import com.example.domain.HelpfulnessRating
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Calendar
import java.util.concurrent.atomic.AtomicLong

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RoomRepositoryTest {

  private lateinit var context: Context
  private lateinit var database: AppDatabase
  private lateinit var clockMillis: AtomicLong
  private lateinit var dayKey: DayKey
  private lateinit var repository: BalanceRepository

  @Before
  fun setUp() {
    context = ApplicationProvider.getApplicationContext()
    val startCal = Calendar.getInstance().apply {
      set(Calendar.YEAR, 2026)
      set(Calendar.MONTH, Calendar.NOVEMBER)
      set(Calendar.DAY_OF_MONTH, 12)
      set(Calendar.HOUR_OF_DAY, 23)
      set(Calendar.MINUTE, 55)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
    }
    clockMillis = AtomicLong(startCal.timeInMillis)
    dayKey = DayKey { clockMillis.get() }

    database = Room.inMemoryDatabaseBuilder(
      context,
      AppDatabase::class.java
    ).allowMainThreadQueries().build()

    val assessmentRepo = AssessmentRepository(context)
    assessmentRepo.resetAssessment()

    repository = BalanceRepository(
      context = context,
      database = database,
      assessmentRepository = assessmentRepo,
      dayKey = dayKey
    )
  }

  @After
  fun tearDown() {
    database.close()
  }

  @Test
  fun testInsertarActualizarReflexionYBorrarCompletacion() = runBlocking {
    val missionId = "desc_1"
    val date = dayKey.today()

    assertNull(database.completionDao().findCompletion(missionId, date))

    val insertId = repository.insertCompletion(
      missionId = missionId,
      dimensionIndex = 2,
      date = date,
      rating = HelpfulnessRating.HELPFUL,
      reflectionNote = "Me costó soltar el móvil al inicio pero dormí mejor."
    )
    assertTrue(insertId > 0)

    val saved = database.completionDao().findCompletion(missionId, date)
    assertNotNull(saved)
    assertEquals(HelpfulnessRating.HELPFUL.name, saved?.helpfulRating)
    assertEquals("Me costó soltar el móvil al inicio pero dormí mejor.", saved?.reflectionNote)

    // Actualizar reflexión posterior sin perder el registro
    repository.saveReflectionFeedback(
      missionId = missionId,
      dimensionIndex = 2,
      rating = HelpfulnessRating.NEUTRAL,
      reflectionNote = "Nota editada",
      date = date
    )

    val updated = database.completionDao().findCompletion(missionId, date)
    assertEquals(HelpfulnessRating.NEUTRAL.name, updated?.helpfulRating)
    assertEquals("Nota editada", updated?.reflectionNote)

    val deletedCount = repository.deleteCompletion(missionId, date)
    assertEquals(1, deletedCount)
    assertNull(database.completionDao().findCompletion(missionId, date))
  }

  @Test
  fun testToggleCompletacionInsertarYDesmarcar() = runBlocking {
    val missionId = "custom_ejer_99"
    val date = dayKey.today()

    val completed = repository.toggleMissionCompletion(
      missionId = missionId,
      dimensionIndex = 1,
      date = date
    )
    assertTrue(completed)
    assertNotNull(database.completionDao().findCompletion(missionId, date))

    val unmarked = repository.toggleMissionCompletion(
      missionId = missionId,
      dimensionIndex = 1,
      date = date
    )
    assertFalse(unmarked)
    assertNull(database.completionDao().findCompletion(missionId, date))
  }

  @Test
  fun testInsertarYReleerMisionPersonalizada() = runBlocking {
    val customMission = MissionEntity(
      id = "custom_yoga_1",
      dimensionIndex = 1,
      title = "Estiramiento tranquilo",
      description = "10 minutos suaves al despertar.",
      benefit = "Puede ayudarte a soltar tensión matutina.",
      durationLabel = "10 min",
      durationMinutes = 10,
      frequency = "Diariamente",
      isHabit = true
    )

    repository.insertCustomMission(customMission)

    val missions = repository.customMissions.first()
    assertEquals(1, missions.size)
    assertEquals("custom_yoga_1", missions[0].id)
    assertEquals(10, missions[0].durationMinutes)
    assertTrue(missions[0].isHabit)

    repository.deleteCustomMission("custom_yoga_1")
    assertTrue(repository.customMissions.first().isEmpty())
  }

  @Test
  fun testMidnightRolloverRefreshesDailyPlanWithoutLosingHistory() = runBlocking {
    val perceivedScores = (0..5).associateWith { 5 }
    repository.saveFocusDimensions(setOf(2)) // Descanso

    val day1Key = dayKey.today()
    val day1Plan = repository.ensureDailyPlanForToday(perceivedScores)
    assertEquals(3, day1Plan.size)
    assertEquals(day1Key, day1Plan.first().date)

    // Completar y valorar la primera acción del día 1
    val firstAction = day1Plan.first()
    repository.saveReflectionFeedback(
      missionId = firstAction.missionId,
      dimensionIndex = firstAction.dimensionIndex,
      rating = HelpfulnessRating.HELPFUL,
      reflectionNote = "Ayudó bastante",
      date = day1Key
    )
    assertTrue(repository.todayCompletedMissionIds.first().contains(firstAction.missionId))

    // Avanzar el reloj local 10 minutos para cruzar las 00:00 (2026-11-12 23:55 -> 2026-11-13 00:05)
    clockMillis.addAndGet(10 * 60 * 1000L)
    val changed = repository.refreshDayIfNeeded()
    assertTrue("Cruzar las 00:00 locales debe actualizar currentDayKey", changed)

    val day2Key = dayKey.today()
    assertNotEquals(day1Key, day2Key)
    assertEquals(day2Key, repository.currentDayKey.value)

    // El nuevo día empieza limpio en hoy, pero el historial completo conserva lo de ayer
    assertTrue(repository.todayCompletedMissionIds.first().isEmpty())
    val history = repository.allCompletions.first()
    assertEquals(1, history.size)
    assertEquals("Ayudó bastante", history.first().reflectionNote)

    // El nuevo plan diario a las 00:00 evita repetir las acciones ofrecidas/completadas ayer
    val day2Plan = repository.ensureDailyPlanForToday(perceivedScores)
    assertEquals(3, day2Plan.size)
    assertEquals(day2Key, day2Plan.first().date)
    val day1Ids = day1Plan.map { it.missionId }.toSet()
    val day2Ids = day2Plan.map { it.missionId }.toSet()
    assertTrue("La acción completada ayer no debe repetirse hoy", firstAction.missionId !in day2Ids)
    assertFalse("El plan de hoy debe traer acciones nuevas frente al de ayer", day1Ids == day2Ids)
  }

  @Test
  fun testSwapDailyMissionAndReplanForFocusChange() = runBlocking {
    val perceivedScores = (0..5).associateWith { 5 }
    repository.saveFocusDimensions(setOf(0))

    val initialPlan = repository.ensureDailyPlanForToday(perceivedScores)
    assertEquals(3, initialPlan.size)

    val swapped = repository.swapDailyMission(slotIndex = 0, perceivedScores = perceivedScores)
    assertNotNull(swapped)
    assertNotEquals(initialPlan[0].missionId, swapped?.missionId)
    assertTrue(swapped?.wasSwapped == true)

    // Cambiar enfoque a Hobbies (4) actualiza las ranuras pendientes
    repository.saveFocusDimensions(setOf(4))
    val replanned = repository.replanTodayForFocusChange(perceivedScores)
    assertEquals(3, replanned.size)
    assertTrue(replanned.any { it.dimensionIndex == 4 })
  }

  @Test
  fun testDailyExpCapAndHexagonExpSync() = runBlocking {
    val date = dayKey.today()
    // 5 completions at 35 EXP = 175 -> should cap at 150 EXP today
    for (i in 1..5) {
      repository.toggleMissionCompletion(
        missionId = "exp_test_$i",
        dimensionIndex = 0,
        expAwarded = 35,
        date = date
      )
    }

    val profile = repository.userProfileFlow.first()
    assertEquals(150, profile.expToday)
    assertEquals(150, profile.exp)
    assertEquals(1, profile.level)
    assertEquals(5, profile.completedMissions)

    val expByDim = repository.expByDimension14d.first()
    assertEquals(150, expByDim[0])
  }
}
