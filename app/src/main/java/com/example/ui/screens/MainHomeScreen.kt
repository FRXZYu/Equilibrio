package com.example.ui.screens

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.AssignmentTurnedIn
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.EventRepeat
import androidx.compose.material.icons.outlined.Hexagon
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.AssessmentRepository
import com.example.data.BalanceRepository
import com.example.data.db.MissionEntity
import com.example.domain.HexagonCalculator
import com.example.model.GamificationStats
import com.example.ui.components.DualLayerHexagonRadarChart
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.AssessmentReport
import com.example.model.BalanceMission
import com.example.model.DimensionsProvider
import com.example.model.MissionsProvider
import com.example.model.UserProfile
import com.example.ui.components.bouncyClick
import com.example.ui.theme.AccentCharcoal
import com.example.ui.theme.BorderLight
import com.example.ui.theme.SurfaceCardLight
import com.example.ui.theme.SurfaceVariantLight
import com.example.ui.theme.TextPrimaryLight
import com.example.ui.theme.TextSecondaryLight
import com.example.ui.theme.TextTertiaryLight
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MainHomeScreen(
  userProfile: UserProfile,
  report: AssessmentReport,
  onOpenReport: () -> Unit,
  onUpdateGoal: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedTab by remember { mutableIntStateOf(2) } // 0: Misiones, 1: Habilidades, 2: Inicio, 3: Plan, 4: Logros
  var showEditGoalDialog by remember { mutableStateOf(false) }
  var editedGoal by remember { mutableStateOf(userProfile.monthlyGoal) }

  // "+" FAB Expansion state
  var isFabExpanded by remember { mutableStateOf(false) }
  var showCreateMissionDialog by remember { mutableStateOf(false) }

  val context = LocalContext.current
  val haptic = LocalHapticFeedback.current
  val scope = rememberCoroutineScope()
  val balanceRepository = remember { BalanceRepository(context) }

  // Estados reactivos derivados 100% de Room (sin datos simulados)
  val liveProfile by balanceRepository.userProfileFlow.collectAsStateWithLifecycle(initialValue = userProfile)
  val realScoresFloat by balanceRepository.realScoresFloat.collectAsStateWithLifecycle(initialValue = emptyMap())
  val realScores by balanceRepository.realScores.collectAsStateWithLifecycle(initialValue = emptyMap())
  val todayCompletedIds by balanceRepository.todayCompletedMissionIds.collectAsStateWithLifecycle(initialValue = emptySet())
  val customMissionsEntities by balanceRepository.customMissions.collectAsStateWithLifecycle(initialValue = emptyList())

  // Diálogos de detalle y estado de misiones expandidas
  var selectedDimensionForDetail by remember { mutableStateOf<Int?>(null) }
  var activeDetailMission by remember { mutableStateOf<BalanceMission?>(null) }
  var isOtherMissionsExpanded by remember { mutableStateOf(false) }

  // Misiones combinadas (sugeridas por MissionsProvider + personalizadas de Room)
  val allMissions = remember(report.scores, customMissionsEntities, todayCompletedIds) {
    val base = MissionsProvider.generateMissionsForUser(report.scores)
    val custom = customMissionsEntities.map { entity ->
      val dim = DimensionsProvider.dimensions.getOrNull(entity.dimensionIndex) ?: DimensionsProvider.dimensions[0]
      BalanceMission(
        id = entity.id,
        dimensionIndex = entity.dimensionIndex,
        dimensionTitle = dim.title,
        title = entity.title,
        summary = entity.description.take(70),
        description = entity.description,
        steps = listOf(
          "Paso 1 (Preparación): Define tu espacio y propósito para ${entity.title}.",
          "Paso 2 (Ejecución): Realiza la actividad con presencia durante ${entity.durationLabel}.",
          "Paso 3 (Reflexión): Agradece tu compromiso con tu bienestar integral."
        ),
        benefit = entity.benefit,
        durationLabel = entity.durationLabel,
        baseExpReward = entity.expReward,
        isWeaknessFocus = false,
        hasMultiplier = false,
        expReward = entity.expReward,
        isCompleted = false,
        isSavedForLater = false,
        isCustom = true,
        difficulty = entity.difficulty,
        isHabit = entity.isHabit
      )
    }
    (custom + base).map { m ->
      m.copy(
        isCompleted = todayCompletedIds.contains(m.id)
      )
    }
  }

  val homeScrollState = rememberScrollState()

  // Clean, radiant, minimalist white canvas
  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFFFAFAFC))
  ) {
    // Main Content
    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
    ) {
      Box(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
      ) {
        if (selectedTab == 0) {
          // ==========================================
          // APARTADO DE MISIONES (CATEGORÍAS & CATÁLOGO)
          // ==========================================
          val gamificationStats = GamificationStats(
            totalExp = liveProfile.exp,
            expToday = liveProfile.expToday,
            completedMissionsCount = liveProfile.completedMissions,
            activeStreakDays = liveProfile.activeStreakDays,
            completedMissionIds = todayCompletedIds
          )
          MissionsSection(
            report = report,
            missions = allMissions,
            gamificationStats = gamificationStats,
            onCompleteMission = { mission ->
              scope.launch {
                balanceRepository.toggleMissionCompletion(
                  mission.id,
                  mission.dimensionIndex,
                  mission.expReward
                )
              }
              haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            },
            onSaveMissionForLater = { mission ->
              balanceRepository.toggleSaveMissionForLater(mission.id)
            },
            onOpenReport = onOpenReport,
            onOpenCreateMission = { showCreateMissionDialog = true }
          )
        } else if (selectedTab == 1) {
          // ==========================================
          // APARTADO DE HABILIDADES (RESUMEN INTERACTIVO Y DETALLES)
          // ==========================================
          HabilidadesSection(
            report = report,
            realScores = realScores,
            realScoresFloat = realScoresFloat,
            allMissions = allMissions,
            onLaunchMission = { mission ->
              activeDetailMission = mission
            },
            onOpenCreateMission = {
              showCreateMissionDialog = true
            }
          )
        } else {
          HoySection(
            userProfile = liveProfile,
            report = report,
            realScores = realScores,
            realScoresFloat = realScoresFloat,
            todayCompletedIds = todayCompletedIds,
            allMissions = allMissions,
            selectedDimensionForDetail = selectedDimensionForDetail,
            onSelectDimension = { selectedDimensionForDetail = it },
            onToggleCompleteMission = { mission ->
              scope.launch {
                balanceRepository.toggleMissionCompletion(
                  mission.id,
                  mission.dimensionIndex,
                  mission.expReward
                )
              }
              haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            },
            onViewMissionDetails = { mission ->
              activeDetailMission = mission
            }
          )
        }
      }

      // ==========================================
      // CENTRED, FLOATING NAVIGATION BAR WITH EQUAL BALANCED ICONS
      // ==========================================
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .navigationBarsPadding()
          .padding(horizontal = 18.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
      ) {
        Surface(
          shape = RoundedCornerShape(32.dp),
          color = AccentCharcoal,
          shadowElevation = 8.dp,
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Tab 0: Misiones
            NavBottomItem(
              icon = Icons.Outlined.TaskAlt,
              label = "Misiones",
              isSelected = selectedTab == 0,
              modifier = Modifier.weight(1f),
              onClick = { selectedTab = 0 }
            )

            // Tab 1: Habilidades
            NavBottomItem(
              icon = Icons.Outlined.Hexagon,
              label = "Habilidades",
              isSelected = selectedTab == 1,
              modifier = Modifier.weight(1f),
              onClick = {
                selectedTab = 1
              }
            )

            // Tab 2: Hoy
            NavBottomItem(
              icon = Icons.Outlined.Person,
              label = "Hoy",
              isSelected = selectedTab == 2,
              modifier = Modifier.weight(1f),
              onClick = { selectedTab = 2 }
            )

            // Tab 3: Plan
            NavBottomItem(
              icon = Icons.Outlined.CalendarMonth,
              label = "Plan",
              isSelected = selectedTab == 3,
              modifier = Modifier.weight(1f),
              onClick = { selectedTab = 3 }
            )

            // Tab 4: Logros
            NavBottomItem(
              icon = Icons.Outlined.EmojiEvents,
              label = "Logros",
              isSelected = selectedTab == 4,
              modifier = Modifier.weight(1f),
              onClick = { selectedTab = 4 }
            )
          }
        }
      }
    }

    // ==========================================
    // FLOATING ACTION BUTTON (+) WITH PILL POPUP
    // Elevated safely above the action bar with ample clearance
    // ==========================================
    Column(
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .navigationBarsPadding()
        .padding(bottom = 104.dp, end = 20.dp),
      horizontalAlignment = Alignment.End
    ) {
      // Deployed Material Design Pill: "Misión"
      AnimatedVisibility(
        visible = isFabExpanded,
        enter = fadeIn() + scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + slideInVertically { it / 2 },
        exit = fadeOut() + scaleOut() + slideOutVertically { it / 2 }
      ) {
        Surface(
          shape = RoundedCornerShape(26.dp),
          color = AccentCharcoal,
          shadowElevation = 8.dp,
          border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
          modifier = Modifier
            .padding(bottom = 12.dp)
            .bouncyClick {
              isFabExpanded = false
              showCreateMissionDialog = true
            }
            .testTag("fab_mission_pill")
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp)
          ) {
            Icon(
              imageVector = Icons.Outlined.Checklist,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Misión",
              style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
              ),
              color = Color.White
            )
          }
        }
      }

      // Main FAB (+)
      val fabInteractionSource = remember { MutableInteractionSource() }
      val isFabPressed by fabInteractionSource.collectIsPressedAsState()
      val fabScale by animateFloatAsState(
        targetValue = if (isFabPressed) 0.88f else 1f,
        animationSpec = spring(
          dampingRatio = Spring.DampingRatioMediumBouncy,
          stiffness = Spring.StiffnessLow
        ),
        label = "fab_bounce"
      )

      val rotationDegree by animateFloatAsState(
        targetValue = if (isFabExpanded) 45f else 0f,
        animationSpec = spring(
          dampingRatio = Spring.DampingRatioMediumBouncy,
          stiffness = Spring.StiffnessLow
        ),
        label = "fab_rotation"
      )

      FloatingActionButton(
        onClick = { isFabExpanded = !isFabExpanded },
        interactionSource = fabInteractionSource,
        containerColor = AccentCharcoal,
        contentColor = Color.White,
        shape = CircleShape,
        modifier = Modifier
          .size(54.dp)
          .graphicsLayer {
            scaleX = fabScale
            scaleY = fabScale
          }
          .shadow(6.dp, CircleShape)
          .testTag("fab_add_mission")
      ) {
        Icon(
          imageVector = Icons.Default.Add,
          contentDescription = if (isFabExpanded) "Cerrar" else "Añadir misión",
          modifier = Modifier
            .size(26.dp)
            .rotate(rotationDegree)
        )
      }
    }
  }

  // Dialog to Create Custom Mission & Habit (Room persistence)
  if (showCreateMissionDialog) {
    CreateMissionDialog(
      onDismiss = { showCreateMissionDialog = false },
      onMissionCreated = { newMission ->
        scope.launch {
          balanceRepository.insertCustomMission(
            MissionEntity(
              id = newMission.id,
              dimensionIndex = newMission.dimensionIndex,
              title = newMission.title,
              description = newMission.description,
              benefit = newMission.benefit,
              durationLabel = newMission.durationLabel,
              expReward = newMission.expReward,
              difficulty = newMission.difficulty,
              isHabit = newMission.isHabit
            )
          )
        }
        showCreateMissionDialog = false
        selectedTab = 0
      }
    )
  }

  // Dialog to View Dimension Diagnostic from Hexagon
  if (selectedDimensionForDetail != null) {
    DimensionDetailDialog(
      dimensionIndex = selectedDimensionForDetail!!,
      report = report,
      realScore = realScores[selectedDimensionForDetail!!] ?: 0,
      onDismiss = { selectedDimensionForDetail = null }
    )
  }

  // Dialog to View Mission Detailed Steps
  val currentDetailMission = activeDetailMission
  if (currentDetailMission != null) {
    MissionDetailModal(
      mission = currentDetailMission,
      stats = GamificationStats(
        totalExp = liveProfile.exp,
        expToday = liveProfile.expToday,
        completedMissionsCount = liveProfile.completedMissions,
        activeStreakDays = liveProfile.activeStreakDays,
        completedMissionIds = todayCompletedIds
      ),
      onDismiss = { activeDetailMission = null },
      onComplete = {
        val missionId = currentDetailMission.id
        val dimIndex = currentDetailMission.dimensionIndex
        val exp = currentDetailMission.expReward
        activeDetailMission = null
        scope.launch {
          balanceRepository.toggleMissionCompletion(
            missionId,
            dimIndex,
            exp
          )
        }
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
      },
      onSaveForLater = {
        val missionId = currentDetailMission.id
        activeDetailMission = null
        scope.launch {
          balanceRepository.toggleSaveMissionForLater(missionId)
        }
      }
    )
  }

  // Edit Goal Dialog ("Puedes cambiarlo después")
  if (showEditGoalDialog) {
    AlertDialog(
      onDismissRequest = { showEditGoalDialog = false },
      title = {
        Text("Tu Meta para el Mes", style = MaterialTheme.typography.titleLarge)
      },
      text = {
        Column {
          Text(
            text = "Puedes cambiarlo después cuando tus prioridades evolucionen.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondaryLight
          )
          Spacer(modifier = Modifier.height(12.dp))
          OutlinedTextField(
            value = editedGoal,
            onValueChange = { editedGoal = it },
            placeholder = { Text("¿Cuál es tu propósito?") },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        TextButton(
          onClick = {
            if (editedGoal.isNotBlank()) {
              onUpdateGoal(editedGoal.trim())
            }
            showEditGoalDialog = false
          }
        ) {
          Text("Guardar", fontWeight = FontWeight.Bold, color = AccentCharcoal)
        }
      },
      dismissButton = {
        TextButton(onClick = { showEditGoalDialog = false }) {
          Text("Cancelar", color = TextSecondaryLight)
        }
      }
    )
  }
}

/**
 * Misiones de Equilibrio: Focused directly on the user's weakest dimensions!
 * Provides productive, wellbeing-enhancing daily activities without loading screens.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MissionsSection(
  report: AssessmentReport,
  missions: List<BalanceMission>,
  gamificationStats: GamificationStats,
  onCompleteMission: (BalanceMission) -> Unit,
  onSaveMissionForLater: (BalanceMission) -> Unit,
  onOpenReport: () -> Unit,
  onOpenCreateMission: () -> Unit
) {
  // Categorías de las 6 habilidades, con ⭐ Recomendadas (debilidades) de primero y 📌 Guardadas al final
  var selectedCategoryIndex by remember { mutableIntStateOf(0) }
  var activeDetailMission by remember { mutableStateOf<BalanceMission?>(null) }

  val categories = remember {
    listOf(
      "Recomendadas",
      "Autoestima",
      "Ejercicio",
      "Descanso",
      "Relaciones",
      "Hobbies",
      "Excelencia",
      "Guardadas"
    )
  }

  val filteredMissions = remember(selectedCategoryIndex, missions) {
    when (selectedCategoryIndex) {
      0 -> missions.filter { it.isWeaknessFocus }
      in 1..6 -> missions.filter { it.dimensionIndex == selectedCategoryIndex - 1 }
      7 -> missions.filter { it.isSavedForLater }
      else -> missions
    }
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 22.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Encabezado principal: Título y Acciones
    item(key = "missions_header") {
      Column(modifier = Modifier.padding(top = 16.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Misiones de Equilibrio",
            style = MaterialTheme.typography.displayMedium.copy(
              fontSize = 26.sp,
              fontWeight = FontWeight.Bold
            ),
            color = TextPrimaryLight
          )

          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Surface(
              color = AccentCharcoal,
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.bouncyClick { onOpenCreateMission() }
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.Add,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "Crear",
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                  color = Color.White
                )
              }
            }

            Surface(
              color = AccentCharcoal.copy(alpha = 0.08f),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.bouncyClick { onOpenReport() }
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Outlined.Hexagon,
                  contentDescription = null,
                  tint = AccentCharcoal,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "Hexágono",
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                  color = AccentCharcoal
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Tarjeta interactiva del Sistema de EXP Diario y Racha (con Límite Anti-Abuso)
        Surface(
          shape = RoundedCornerShape(20.dp),
          color = Color.White,
          border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
          shadowElevation = 2.dp,
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFEF3C7)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.ElectricBolt,
                    contentDescription = null,
                    tint = Color(0xFFD97706),
                    modifier = Modifier.size(16.dp)
                  )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                  Text(
                    text = "EXP Hoy: ${gamificationStats.expToday} / ${GamificationStats.DAILY_EXP_CAP} EXP",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimaryLight
                  )
                  Text(
                    text = if (gamificationStats.isDailyCapReached) "Tope diario alcanzado" else "Quedan ${gamificationStats.remainingDailyExp} EXP hoy",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (gamificationStats.isDailyCapReached) Color(0xFFD97706) else TextSecondaryLight
                  )
                }
              }

              Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFFEF3C7)
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(
                    imageVector = Icons.Outlined.LocalFireDepartment,
                    contentDescription = null,
                    tint = Color(0xFFD97706),
                    modifier = Modifier.size(14.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "${gamificationStats.activeStreakDays} días racha",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF92400E)
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Barra de progreso del límite diario de EXP
            val dailyProgress = (gamificationStats.expToday.toFloat() / GamificationStats.DAILY_EXP_CAP).coerceIn(0f, 1f)
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(SurfaceVariantLight)
            ) {
              Box(
                modifier = Modifier
                  .fillMaxWidth(dailyProgress)
                  .height(8.dp)
                  .clip(RoundedCornerShape(4.dp))
                  .background(if (gamificationStats.isDailyCapReached) Color(0xFFF59E0B) else AccentCharcoal)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Selector de Categorías (Recomendadas de primero, luego las 6 dimensiones, y Guardadas)
        LazyRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          itemsIndexed(categories) { index, catName ->
            val isSelected = selectedCategoryIndex == index
            Surface(
              shape = RoundedCornerShape(14.dp),
              color = if (isSelected) AccentCharcoal else Color.White,
              border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isSelected) AccentCharcoal else BorderLight
              ),
              modifier = Modifier.bouncyClick { selectedCategoryIndex = index }
            ) {
              Text(
                text = catName,
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                ),
                color = if (isSelected) Color.White else TextPrimaryLight,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4. Banner contextual explicativo
        if (selectedCategoryIndex == 0) {
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFEFF6FF),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Outlined.Lightbulb,
                  contentDescription = null,
                  tint = Color(0xFF1D4ED8),
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "ÁREAS PRIORITARIAS • 1.5x EXP",
                  style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                  ),
                  color = Color(0xFF1D4ED8)
                )
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Tus dimensiones con menor puntaje actual otorgan +50% de EXP. Complétalas para fortalecer tu armonía vital.",
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                color = Color(0xFF1E3A8A)
              )
            }
          }
        } else if (selectedCategoryIndex == 7) {
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFF9FAFB),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = if (filteredMissions.isEmpty()) "Aún no tienes misiones guardadas. Abre cualquier misión y pulsa 'Guardar para más tarde'." else "Misiones que guardaste para realizar en otro momento.",
              style = MaterialTheme.typography.bodySmall,
              color = TextSecondaryLight,
              modifier = Modifier.padding(14.dp)
            )
          }
        }
      }
    }

    // 5. Lista de tarjetas de misión compactas con botón "Continuar"
    items(filteredMissions, key = { it.id }) { mission ->
      CompactMissionCard(
        mission = mission,
        onContinue = { activeDetailMission = mission }
      )
    }

    item(key = "bottom_space") {
      Spacer(modifier = Modifier.height(120.dp))
    }
  }
}

/**
 * Tarjeta de misión compacta y minimalista.
 * Muestra solo un resumen básico con botón "Continuar" para desplegar la información completa.
 */
@Composable
internal fun CompactMissionCard(
  mission: BalanceMission,
  onContinue: () -> Unit
) {
  val dimension = DimensionsProvider.dimensions.getOrNull(mission.dimensionIndex)
  val accentColor = dimension?.accentColor ?: AccentCharcoal

  Surface(
    shape = RoundedCornerShape(22.dp),
    color = SurfaceCardLight,
    border = androidx.compose.foundation.BorderStroke(
      1.dp,
      if (mission.isCompleted) Color(0xFF10B981).copy(alpha = 0.5f) else BorderLight
    ),
    shadowElevation = if (mission.isCompleted) 1.dp else 2.dp,
    modifier = Modifier
      .fillMaxWidth()
      .bouncyClick { onContinue() }
      .testTag("mission_card_${mission.id}")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(18.dp)
    ) {
      // Fila superior: Dimensión + Estados (1.5x / Completada / Guardada)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = accentColor.copy(alpha = 0.12f)
        ) {
          Text(
            text = mission.dimensionTitle.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = accentColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
          )
        }

        Row(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          if (mission.isCompleted) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = Color(0xFFDCFCE7)
            ) {
              Text(
                text = "✓ Completada",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF15803D),
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
              )
            }
          } else if (mission.isSavedForLater) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = Color(0xFFEFF6FF)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.Bookmark,
                  contentDescription = null,
                  tint = Color(0xFF1D4ED8),
                  modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                  text = "Guardada",
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                  color = Color(0xFF1D4ED8)
                )
              }
            }
          }

          if (mission.hasMultiplier) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = Color(0xFFFEF3C7)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.ElectricBolt,
                  contentDescription = null,
                  tint = Color(0xFFD97706),
                  modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                  text = "1.5x EXP",
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                  color = Color(0xFFB45309)
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Título de la misión
      Text(
        text = mission.title,
        style = MaterialTheme.typography.titleMedium.copy(
          fontWeight = FontWeight.Bold,
          fontSize = 17.sp
        ),
        color = if (mission.isCompleted) TextSecondaryLight else TextPrimaryLight
      )

      Spacer(modifier = Modifier.height(4.dp))

      // Resumen básico conciso
      Text(
        text = mission.summary.ifEmpty { mission.description.take(85) + "..." },
        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, lineHeight = 20.sp),
        color = TextSecondaryLight
      )

      Spacer(modifier = Modifier.height(12.dp))

      // Fila inferior: Tiempo + EXP + Botón "Continuar"
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Timer,
            contentDescription = null,
            tint = TextTertiaryLight,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = mission.durationLabel,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondaryLight
          )
          Spacer(modifier = Modifier.width(8.dp))
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (mission.hasMultiplier) Color(0xFFFEF3C7) else SurfaceVariantLight
          ) {
            Text(
              text = "+${mission.expReward} EXP",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = if (mission.hasMultiplier) Color(0xFF92400E) else TextPrimaryLight,
              modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
            )
          }
        }

        // Botón Continuar
        Button(
          onClick = onContinue,
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = AccentCharcoal,
            contentColor = Color.White
          ),
          contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 6.dp),
          modifier = Modifier.testTag("btn_continue_mission_${mission.id}")
        ) {
          Text(
            text = "Ver Detalles",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            modifier = Modifier.size(14.dp)
          )
        }
      }
    }
  }
}

@Composable
private fun NavBottomItem(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  label: String,
  isSelected: Boolean,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
    modifier = modifier
      .height(52.dp)
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick
      )
      .padding(horizontal = 2.dp, vertical = 2.dp)
  ) {
    Box(
      modifier = Modifier.size(24.dp),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = label,
        tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.50f),
        modifier = Modifier.size(22.dp)
      )
    }
    Spacer(modifier = Modifier.height(2.dp))
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall.copy(
        fontSize = 10.5.sp,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
      ),
      color = if (isSelected) Color.White else Color.White.copy(alpha = 0.50f),
      maxLines = 1,
      textAlign = androidx.compose.ui.text.style.TextAlign.Center
    )
    Spacer(modifier = Modifier.height(3.dp))
    // Symmetrical indicator dot slot for ALL tabs, avoiding vertical wobble
    Box(
      modifier = Modifier
        .size(3.5.dp)
        .clip(CircleShape)
        .background(if (isSelected) Color.White else Color.Transparent)
    )
  }
}

/**
 * Spiderweb Hexagonal Radar Canvas matching image.png on white background,
 * and dynamically filled with the user's 6 assessment scores!
 */
@Composable
private fun SpiderwebHexagonCanvas(
  scores: Map<Int, Int>,
  modifier: Modifier = Modifier
) {
  Canvas(modifier = modifier) {
    val center = Offset(size.width / 2f, size.height / 2f)
    val maxRadius = (size.minDimension / 2f) * 0.90f

    // 4 Concentric hexagon rings (spiderweb lines)
    val ringLevels = listOf(0.25f, 0.50f, 0.75f, 1.0f)
    ringLevels.forEach { frac ->
      val path = Path()
      for (i in 0..5) {
        val angle = -PI / 2 + i * (PI / 3)
        val x = (center.x + maxRadius * frac * cos(angle)).toFloat()
        val y = (center.y + maxRadius * frac * sin(angle)).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
      }
      path.close()
      drawPath(
        path = path,
        color = BorderLight,
        style = Stroke(width = 1.2.dp.toPx())
      )
    }

    // 6 Radial spoke lines from center to outer vertices
    for (i in 0..5) {
      val angle = -PI / 2 + i * (PI / 3)
      val end = Offset(
        (center.x + maxRadius * cos(angle)).toFloat(),
        (center.y + maxRadius * sin(angle)).toFloat()
      )
      drawLine(
        color = BorderLight,
        start = center,
        end = end,
        strokeWidth = 1.2.dp.toPx()
      )
    }

    // Filled radar polygon based on user's assessment
    val radarPath = Path()
    val radarPoints = (0..5).map { i ->
      val score = scores[i] ?: 6
      val r = maxRadius * (score / 10f).coerceIn(0.18f, 1f)
      val angle = -PI / 2 + i * (PI / 3)
      Offset(
        (center.x + r * cos(angle)).toFloat(),
        (center.y + r * sin(angle)).toFloat()
      )
    }

    radarPath.moveTo(radarPoints[0].x, radarPoints[0].y)
    for (i in 1 until radarPoints.size) {
      radarPath.lineTo(radarPoints[i].x, radarPoints[i].y)
    }
    radarPath.close()

    // Soft charcoal gradient fill
    drawPath(
      path = radarPath,
      brush = Brush.radialGradient(
        colors = listOf(
          AccentCharcoal.copy(alpha = 0.25f),
          AccentCharcoal.copy(alpha = 0.08f)
        ),
        center = center,
        radius = maxRadius
      ),
      style = Fill
    )

    // Crisp border
    drawPath(
      path = radarPath,
      color = AccentCharcoal,
      style = Stroke(
        width = 2.dp.toPx(),
        join = StrokeJoin.Round,
        cap = StrokeCap.Round
      )
    )

    // Glowing vertex dots
    radarPoints.forEachIndexed { i, pt ->
      val accent = DimensionsProvider.dimensions[i].accentColor
      drawCircle(
        color = Color.White,
        radius = 4.dp.toPx(),
        center = pt
      )
      drawCircle(
        color = accent,
        radius = 2.5.dp.toPx(),
        center = pt
      )
    }
  }
}

/**
 * 8-pointed golden star badge matching image.png level 2
 */
private fun DrawScope.drawEightPointStar(
  center: Offset,
  outerRadius: Float,
  innerRadius: Float,
  color: Color
) {
  val path = Path()
  val numPoints = 8
  val totalPoints = numPoints * 2

  for (i in 0 until totalPoints) {
    val r = if (i % 2 == 0) outerRadius else innerRadius
    val angle = -PI / 2 + i * (2 * PI / totalPoints)
    val x = (center.x + r * cos(angle)).toFloat()
    val y = (center.y + r * sin(angle)).toFloat()
    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
  }
  path.close()
  drawPath(path = path, color = color, style = Fill)
}

/**
 * Warm illustrated woman avatar placeholder matching image.png
 */
@Composable
private fun IllustratedAvatar() {
  Canvas(modifier = Modifier.fillMaxSize()) {
    val center = Offset(size.width / 2f, size.height / 2f)

    // Peach background circle
    drawCircle(
      color = Color(0xFFFFDFBA),
      radius = size.minDimension / 2f,
      center = center
    )

    // Dark hair silhouette
    val hairPath = Path()
    hairPath.moveTo(size.width * 0.22f, size.height * 0.95f)
    hairPath.cubicTo(
      size.width * 0.15f, size.height * 0.45f,
      size.width * 0.35f, size.height * 0.15f,
      size.width * 0.50f, size.height * 0.15f
    )
    hairPath.cubicTo(
      size.width * 0.65f, size.height * 0.15f,
      size.width * 0.85f, size.height * 0.45f,
      size.width * 0.78f, size.height * 0.95f
    )
    hairPath.close()
    drawPath(path = hairPath, color = Color(0xFF374151), style = Fill)

    // Face / Neck oval
    drawOval(
      color = Color(0xFFFBBF8C),
      topLeft = Offset(size.width * 0.34f, size.height * 0.28f),
      size = androidx.compose.ui.geometry.Size(size.width * 0.32f, size.height * 0.42f)
    )

    // Orange/coral top
    val shirtPath = Path()
    shirtPath.moveTo(size.width * 0.24f, size.height)
    shirtPath.cubicTo(
      size.width * 0.32f, size.height * 0.70f,
      size.width * 0.68f, size.height * 0.70f,
      size.width * 0.76f, size.height
    )
    shirtPath.close()
    drawPath(path = shirtPath, color = Color(0xFFFB923C), style = Fill)
  }
}
