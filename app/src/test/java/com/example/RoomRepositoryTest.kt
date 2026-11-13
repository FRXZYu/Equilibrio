package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.BalanceRepository
import com.example.data.db.AppDatabase
import com.example.data.db.CompletionEntity
import com.example.data.db.MissionEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RoomRepositoryTest {

  private lateinit var database: AppDatabase
  private lateinit var repository: BalanceRepository

  @Before
  fun setUp() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    // Crear base de datos Room en memoria para pruebas aisladas
    database = Room.inMemoryDatabaseBuilder(
      context,
      AppDatabase::class.java
    ).allowMainThreadQueries().build()

    repository = BalanceRepository(
      context = context,
      database = database
    )
  }

  @After
  fun tearDown() {
    database.close()
  }

  @Test
  fun testInsertarReleerYBorrarAlDesmarcarCompletacion() = runBlocking {
    val missionId = "test_desc_1"
    val date = "2026-11-12"

    // 1. Estado inicial: ninguna completación
    val inicial = database.completionDao().findCompletion(missionId, date)
    assertNull("No debe existir completación al inicio", inicial)

    // 2. Insertar completación
    val insertId = repository.insertCompletion(
      missionId = missionId,
      dimensionIndex = 2,
      expAwarded = 40,
      date = date
    )
    assertTrue("ID insertado debe ser mayor a 0", insertId > 0)

    // 3. Releer y verificar que está guardada
    val guardada = database.completionDao().findCompletion(missionId, date)
    assertNotNull("La completación debe existir en la base", guardada)
    assertEquals(missionId, guardada?.missionId)
    assertEquals(2, guardada?.dimensionIndex)
    assertEquals(40, guardada?.expAwarded)
    assertEquals(date, guardada?.date)

    // Verificar lectura reactiva mediante Flow
    val todas = repository.allCompletions.first()
    assertEquals(1, todas.size)
    assertEquals(missionId, todas[0].missionId)

    // 4. Borrar al desmarcar
    val deletedCount = repository.deleteCompletion(missionId, date)
    assertEquals("Debe haberse eliminado exactamente 1 registro", 1, deletedCount)

    // 5. Verificar que ya no existe
    val trasBorrar = database.completionDao().findCompletion(missionId, date)
    assertNull("La completación debe haber sido eliminada tras desmarcarla", trasBorrar)

    val todasTrasBorrar = repository.allCompletions.first()
    assertTrue("La lista de completaciones debe estar vacía", todasTrasBorrar.isEmpty())
  }

  @Test
  fun testToggleCompletacionInsertarYDesmarcar() = runBlocking {
    val missionId = "custom_ejer_99"
    val date = "2026-11-12"

    // Primer toggle: marcar como completada
    val marcada = repository.toggleMissionCompletion(
      missionId = missionId,
      dimensionIndex = 1,
      expReward = 50,
      date = date
    )
    assertTrue("Primer toggle debe marcar como completada", marcada)

    val existente = database.completionDao().findCompletion(missionId, date)
    assertNotNull(existente)
    assertEquals(50, existente?.expAwarded)

    // Segundo toggle: desmarcar (borrar)
    val desmarcada = repository.toggleMissionCompletion(
      missionId = missionId,
      dimensionIndex = 1,
      expReward = 50,
      date = date
    )
    assertTrue("Segundo toggle debe retornar false (desmarcada)", !desmarcada)

    val borrado = database.completionDao().findCompletion(missionId, date)
    assertNull("Debe eliminarse de Room al desmarcar", borrado)
  }

  @Test
  fun testInsertarYReleerMisionPersonalizada() = runBlocking {
    val customMission = MissionEntity(
      id = "custom_yoga_1",
      dimensionIndex = 1,
      title = "Yoga Restaurativo Matutino",
      description = "Sesión de 20 minutos de asanas suaves.",
      benefit = "Puede ayudarte a relajar la musculatura y centrar la respiración.",
      durationLabel = "20 min",
      expReward = 35,
      difficulty = "Fácil",
      isHabit = true
    )

    repository.insertCustomMission(customMission)

    val misiones = repository.customMissions.first()
    assertEquals(1, misiones.size)
    assertEquals("custom_yoga_1", misiones[0].id)
    assertEquals("Yoga Restaurativo Matutino", misiones[0].title)
    assertTrue(misiones[0].isHabit)

    // Eliminar misión
    repository.deleteCustomMission("custom_yoga_1")
    val trasBorrar = repository.customMissions.first()
    assertTrue(trasBorrar.isEmpty())
  }

  @Test
  fun testTopeDiarioExp150YGuardadoReactivo() = runBlocking {
    val date = "2026-11-12"

    // Guardar misión para más tarde y verificar flujo reactivo
    val saved = repository.toggleSaveMissionForLater("desc_1")
    assertTrue(saved)
    assertTrue(repository.savedMissionIds.first().contains("desc_1"))

    // Completar misiones hasta alcanzar y superar el tope de 150 EXP
    repository.toggleMissionCompletion("m1", 0, 60, date)
    repository.toggleMissionCompletion("m2", 1, 60, date)
    repository.toggleMissionCompletion("m3", 2, 60, date) // Solo debe otorgar 30 EXP restantes

    val m3Record = database.completionDao().findCompletion("m3", date)
    assertNotNull(m3Record)
    assertEquals(30, m3Record?.expAwarded)

    // Una cuarta misión ya con tope completo registra 0 EXP pero sigue contando como completada
    repository.toggleMissionCompletion("desc_1", 2, 40, date)
    val desc1Record = database.completionDao().findCompletion("desc_1", date)
    assertNotNull(desc1Record)
    assertEquals(0, desc1Record?.expAwarded)

    // Al completarla se elimina automáticamente de guardadas
    assertTrue(!repository.savedMissionIds.first().contains("desc_1"))
  }
}
