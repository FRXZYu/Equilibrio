package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Hexagon
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.BalanceRepository
import com.example.data.db.MissionEntity
import com.example.domain.HexagonCalculator
import com.example.model.AssessmentReport
import com.example.model.BalanceMission
import com.example.model.DimensionsProvider
import com.example.model.GamificationStats
import com.example.model.MissionsProvider
import com.example.model.UserProfile
import com.example.ui.components.bouncyScale
import com.example.ui.components.rememberBounceInteractionSource
import com.example.ui.theme.AccentCharcoal
import com.example.ui.theme.BorderLight
import com.example.ui.theme.TextSecondaryLight
import kotlinx.coroutines.launch

@Composable
fun MainHomeScreen(
  userProfile: UserProfile,
  report: AssessmentReport,
  onOpenReport: () -> Unit,
  onUpdateGoal: (String) -> Unit,
  selectedTab: Int = 2,
  onSelectTab: ((Int) -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  var fallbackTab by remember { mutableIntStateOf(selectedTab) } // 0: Misiones, 1: Habilidades, 2: Hoy, 3: Plan, 4: Logros
  val currentTab = if (onSelectTab != null) selectedTab else fallbackTab
  val setTab: (Int) -> Unit = { tab ->
    if (onSelectTab != null) onSelectTab(tab) else fallbackTab = tab
  }
  var showEditGoalDialog by remember { mutableStateOf(false) }
  var editedGoal by remember(userProfile.monthlyGoal) { mutableStateOf(userProfile.monthlyGoal) }

  // "+" FAB Expansion state
  var isFabExpanded by remember { mutableStateOf(false) }
  var showCreateMissionDialog by remember { mutableStateOf(false) }

  val context = LocalContext.current
  val haptic = LocalHapticFeedback.current
  val scope = rememberCoroutineScope()
  val balanceRepository = remember { BalanceRepository(context) }

  // Estados reactivos derivados 100% de Room + SharedPreferences
  val liveProfile by balanceRepository.userProfileFlow.collectAsStateWithLifecycle(initialValue = userProfile)
  val syncedProfile = remember(liveProfile, userProfile) {
    liveProfile.copy(
      name = userProfile.name,
      photoUri = userProfile.photoUri,
      zodiacSign = userProfile.zodiacSign,
      zodiacSymbol = userProfile.zodiacSymbol,
      monthlyGoal = userProfile.monthlyGoal
    )
  }

  val realScoresFloat by balanceRepository.realScoresFloat.collectAsStateWithLifecycle(initialValue = emptyMap())
  val realScores by balanceRepository.realScores.collectAsStateWithLifecycle(initialValue = emptyMap())
  val expByDimension14d by balanceRepository.expByDimension14d.collectAsStateWithLifecycle(initialValue = emptyMap())
  val completionsLast14Days by balanceRepository.completionsLast14Days.collectAsStateWithLifecycle(initialValue = emptyList())
  val actionsByDimension14d by balanceRepository.actionsByDimension14d.collectAsStateWithLifecycle(initialValue = emptyMap())
  val todayCompletions by balanceRepository.todayCompletions.collectAsStateWithLifecycle(initialValue = emptyList())
  val todayCompletedIds by balanceRepository.todayCompletedMissionIds.collectAsStateWithLifecycle(initialValue = emptySet())
  val savedMissionIds by balanceRepository.savedMissionIds.collectAsStateWithLifecycle(initialValue = emptySet())
  val customMissionsEntities by balanceRepository.customMissions.collectAsStateWithLifecycle(initialValue = emptyList())

  androidx.compose.runtime.LaunchedEffect(Unit) {
    balanceRepository.refreshDayIfNeeded()
  }

  val liveHarmonyPercentage = remember(realScores, report.scores) {
    HexagonCalculator.calculateLiveHarmonyPercentage(realScores, report.scores)
  }

  // Diálogos de detalle resueltos reactivamente por ID para evitar estado obsoleto
  var selectedDimensionForDetail by remember { mutableStateOf<Int?>(null) }
  var activeDetailMissionId by remember { mutableStateOf<String?>(null) }

  // Al pulsar atrás en pestañas secundarias, regresa a "Hoy"
  BackHandler(enabled = currentTab != 2 && !isFabExpanded) {
    setTab(2)
  }
  BackHandler(enabled = isFabExpanded) {
    isFabExpanded = false
  }

  // Misiones combinadas (sugeridas por MissionsProvider + personalizadas de Room),
  // sincronizadas con completadas hoy, guardadas y multiplicador 1.5x para áreas débiles.
  val allMissions = remember(report.scores, realScores, customMissionsEntities, todayCompletedIds, todayCompletions, savedMissionIds) {
    val weakDimensionIndices = DimensionsProvider.dimensions
      .sortedBy { report.scores[it.index] ?: 5 }
      .mapIndexedNotNull { rank, dim ->
        val score = report.scores[dim.index] ?: 5
        val realScore = realScores[dim.index] ?: 0
        if ((score <= 6 || rank < 2) && realScore < 8) dim.index else null
      }
      .toSet()

    val base = MissionsProvider.generateMissionsForUser(report.scores)
    val custom = customMissionsEntities.map { entity ->
      val dim = DimensionsProvider.dimensions.getOrNull(entity.dimensionIndex) ?: DimensionsProvider.dimensions[0]
      val isWeak = weakDimensionIndices.contains(entity.dimensionIndex)
      val boostedExp = if (isWeak) (entity.expReward * 1.5).toInt() else entity.expReward

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
        durationMinutes = entity.durationMinutes,
        baseExpReward = entity.expReward,
        isWeaknessFocus = isWeak,
        hasMultiplier = isWeak,
        expReward = boostedExp,
        isCompleted = false,
        isSavedForLater = false,
        isCustom = true,
        difficulty = entity.difficulty,
        frequency = entity.frequency,
        isHabit = entity.isHabit
      )
    }

    val todayCompletionMap = todayCompletions.associateBy { it.missionId }
    (custom + base).map { m ->
      val todayRecord = todayCompletionMap[m.id]
      m.copy(
        isCompleted = todayCompletedIds.contains(m.id),
        isSavedForLater = savedMissionIds.contains(m.id),
        helpfulRating = todayRecord?.helpfulness,
        reflectionNote = todayRecord?.reflectionNote ?: ""
      )
    }
  }

  val currentDetailMission = remember(activeDetailMissionId, allMissions) {
    allMissions.find { it.id == activeDetailMissionId }
  }

  val fabPillInteraction = rememberBounceInteractionSource()

  // Lienzo blanco minimalista
  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFFFAFAFC))
  ) {
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
        AnimatedContent(
          targetState = currentTab,
          transitionSpec = {
            fadeIn(tween(220, easing = FastOutSlowInEasing)) togetherWith
              fadeOut(tween(150, easing = FastOutSlowInEasing))
          },
          label = "home_tab_transition"
        ) { tab ->
          when (tab) {
            0 -> {
              // ==========================================
              // APARTADO DE MISIONES (CATEGORÍAS Y DETALLES)
              // ==========================================
              val gamificationStats = GamificationStats(
                totalExp = syncedProfile.exp,
                expToday = syncedProfile.expToday,
                completedMissionsCount = syncedProfile.completedMissions,
                activeStreakDays = syncedProfile.activeStreakDays,
                completedMissionIds = todayCompletedIds,
                savedMissionIds = savedMissionIds
              )
              MissionsSection(
                report = report,
                missions = allMissions,
                gamificationStats = gamificationStats,
                onViewMissionDetails = { mission ->
                  activeDetailMissionId = mission.id
                },
                onOpenReport = onOpenReport,
                onOpenCreateMission = { showCreateMissionDialog = true }
              )
            }

            1 -> {
              // ==========================================
              // APARTADO DE HABILIDADES (REDISEÑADO Y SINCRONIZADO)
              // ==========================================
              HabilidadesSection(
                report = report,
                realScores = realScores,
                realScoresFloat = realScoresFloat,
                expByDimension14d = expByDimension14d,
                liveHarmonyPercentage = liveHarmonyPercentage,
                allMissions = allMissions,
                onLaunchMission = { mission ->
                  activeDetailMissionId = mission.id
                },
                onOpenCreateMission = {
                  showCreateMissionDialog = true
                },
                onOpenFullReport = onOpenReport
              )
            }

            2 -> {
              // ==========================================
              // APARTADO HOY (HEXÁGONO DINÁMICO DE 2 CAPAS)
              // ==========================================
              HoySection(
                userProfile = syncedProfile,
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
                  activeDetailMissionId = mission.id
                },
                liveHarmonyPercentage = liveHarmonyPercentage,
                onOpenFullReport = onOpenReport,
                onEditGoal = {
                  editedGoal = syncedProfile.monthlyGoal
                  showEditGoalDialog = true
                }
              )
            }

            3 -> {
              // ==========================================
              // APARTADO PLAN (SEMANA ACTIVA, META Y HÁBITOS)
              // ==========================================
              PlanSection(
                userProfile = syncedProfile,
                report = report,
                realScores = realScores,
                completionsLast14Days = completionsLast14Days,
                allMissions = allMissions,
                onViewMissionDetails = { mission ->
                  activeDetailMissionId = mission.id
                },
                onOpenCreateMission = {
                  showCreateMissionDialog = true
                },
                onEditGoal = {
                  editedGoal = syncedProfile.monthlyGoal
                  showEditGoalDialog = true
                }
              )
            }

            else -> {
              // ==========================================
              // APARTADO LOGROS (NIVEL, DOMINIO 14D E INSIGNIAS)
              // ==========================================
              LogrosSection(
                userProfile = syncedProfile,
                report = report,
                realScores = realScores,
                expByDimension14d = expByDimension14d,
                liveHarmonyPercentage = liveHarmonyPercentage,
                completionsCount = syncedProfile.completedMissions,
                customMissionsCount = customMissionsEntities.size,
                onOpenFullReport = onOpenReport,
                onNavigateToMissions = { setTab(0) }
              )
            }
          }
        }
      }

      // ==========================================
      // BARRA DE NAVEGACIÓN CENTRADA CON REBOTE TÁCTIL
      // ==========================================
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .navigationBarsPadding()
          .padding(horizontal = 12.dp, vertical = 8.dp),
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
            NavBottomItem(
              icon = Icons.Outlined.TaskAlt,
              label = "Misiones",
              isSelected = currentTab == 0,
              modifier = Modifier.weight(1f),
              onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                isFabExpanded = false
                setTab(0)
              }
            )

            NavBottomItem(
              icon = Icons.Outlined.Hexagon,
              label = "Habilidades",
              isSelected = currentTab == 1,
              modifier = Modifier.weight(1f),
              onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                isFabExpanded = false
                setTab(1)
              }
            )

            NavBottomItem(
              icon = Icons.Outlined.Person,
              label = "Hoy",
              isSelected = currentTab == 2,
              modifier = Modifier.weight(1f),
              onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                isFabExpanded = false
                setTab(2)
              }
            )

            NavBottomItem(
              icon = Icons.Outlined.CalendarMonth,
              label = "Plan",
              isSelected = currentTab == 3,
              modifier = Modifier.weight(1f),
              onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                isFabExpanded = false
                setTab(3)
              }
            )

            NavBottomItem(
              icon = Icons.Outlined.EmojiEvents,
              label = "Logros",
              isSelected = currentTab == 4,
              modifier = Modifier.weight(1f),
              onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                isFabExpanded = false
                setTab(4)
              }
            )
          }
        }
      }
    }

    // ==========================================
    // FLOATING ACTION BUTTON (+) CON PÍLDORA DESPLEGABLE
    // ==========================================
    Column(
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .navigationBarsPadding()
        .padding(bottom = 104.dp, end = 20.dp),
      horizontalAlignment = Alignment.End
    ) {
      AnimatedVisibility(
        visible = isFabExpanded,
        enter = fadeIn() + scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + slideInVertically { it / 2 },
        exit = fadeOut() + scaleOut() + slideOutVertically { it / 2 }
      ) {
        Surface(
          onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            isFabExpanded = false
            showCreateMissionDialog = true
          },
          interactionSource = fabPillInteraction,
          shape = RoundedCornerShape(26.dp),
          color = AccentCharcoal,
          shadowElevation = 8.dp,
          border = BorderStroke(1.dp, BorderLight),
          modifier = Modifier
            .padding(bottom = 12.dp)
            .bouncyScale(fabPillInteraction, scaleDown = 0.92f)
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
        onClick = {
          haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
          isFabExpanded = !isFabExpanded
        },
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

  // Diálogo para Crear Misión y Hábito (persistido en Room)
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
              durationMinutes = newMission.durationMinutes,
              frequency = newMission.frequency,
              isHabit = newMission.isHabit,
              expReward = newMission.expReward,
              difficulty = newMission.difficulty
            )
          )
        }
        showCreateMissionDialog = false
        setTab(if (newMission.isHabit) 3 else 0)
      }
    )
  }

  // Diálogo de detalle de dimensión al tocar el hexágono
  if (selectedDimensionForDetail != null) {
    val dimIdx = selectedDimensionForDetail!!
    DimensionDetailDialog(
      dimensionIndex = dimIdx,
      report = report,
      realScore = realScores[dimIdx] ?: 0,
      actionsLast14d = actionsByDimension14d[dimIdx] ?: 0,
      isChosenFocus = dimIdx in syncedProfile.chosenFocusDimensions,
      onToggleFocus = {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        balanceRepository.toggleFocusDimension(dimIdx)
      },
      onDismiss = { selectedDimensionForDetail = null }
    )
  }

  // Modal de detalles de la misión (completar, valorar, desmarcar o guardar)
  if (currentDetailMission != null) {
    MissionDetailModal(
      mission = currentDetailMission,
      stats = GamificationStats(
        totalExp = syncedProfile.exp,
        expToday = syncedProfile.expToday,
        completedMissionsCount = syncedProfile.completedMissions,
        activeStreakDays = syncedProfile.activeStreakDays,
        completedMissionIds = todayCompletedIds,
        savedMissionIds = savedMissionIds
      ),
      onDismiss = { activeDetailMissionId = null },
      onMarkCompleted = {
        val missionId = currentDetailMission.id
        val dimIndex = currentDetailMission.dimensionIndex
        val exp = currentDetailMission.expReward
        scope.launch {
          balanceRepository.toggleMissionCompletion(
            missionId = missionId,
            dimensionIndex = dimIndex,
            expAwarded = exp
          )
        }
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
      },
      onUnmarkCompleted = {
        val missionId = currentDetailMission.id
        activeDetailMissionId = null
        scope.launch {
          balanceRepository.deleteCompletion(missionId)
        }
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
      },
      onSaveReflection = { rating, note ->
        val missionId = currentDetailMission.id
        val dimIndex = currentDetailMission.dimensionIndex
        val exp = currentDetailMission.expReward
        activeDetailMissionId = null
        scope.launch {
          balanceRepository.saveReflectionFeedback(
            missionId = missionId,
            dimensionIndex = dimIndex,
            rating = rating,
            reflectionNote = note,
            expAwarded = exp
          )
        }
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
      },
      onSaveForLater = {
        val missionId = currentDetailMission.id
        activeDetailMissionId = null
        balanceRepository.toggleSaveMissionForLater(missionId)
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
      }
    )
  }

  // Diálogo para editar meta mensual ("Puedes cambiarlo después")
  if (showEditGoalDialog) {
    val saveGoalInteraction = rememberBounceInteractionSource()
    val cancelGoalInteraction = rememberBounceInteractionSource()
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
          },
          interactionSource = saveGoalInteraction,
          modifier = Modifier.bouncyScale(saveGoalInteraction)
        ) {
          Text("Guardar", fontWeight = FontWeight.Bold, color = AccentCharcoal)
        }
      },
      dismissButton = {
        TextButton(
          onClick = { showEditGoalDialog = false },
          interactionSource = cancelGoalInteraction,
          modifier = Modifier.bouncyScale(cancelGoalInteraction)
        ) {
          Text("Cancelar", color = TextSecondaryLight)
        }
      }
    )
  }
}

@Composable
private fun NavBottomItem(
  icon: ImageVector,
  label: String,
  isSelected: Boolean,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  val interactionSource = rememberBounceInteractionSource()
  val contentColor by animateColorAsState(
    targetValue = if (isSelected) Color.White else Color.White.copy(alpha = 0.50f),
    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
    label = "nav_color_$label"
  )

  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
    modifier = modifier
      .height(52.dp)
      .bouncyScale(interactionSource, scaleDown = 0.90f)
      .clickable(
        interactionSource = interactionSource,
        indication = null,
        onClick = onClick
      )
      .padding(horizontal = 2.dp, vertical = 2.dp)
      .testTag("nav_tab_${label.lowercase()}")
  ) {
    Box(
      modifier = Modifier.size(24.dp),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = label,
        tint = contentColor,
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
      color = contentColor,
      maxLines = 1,
      softWrap = false,
      overflow = TextOverflow.Ellipsis,
      textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(3.dp))
    Box(
      modifier = Modifier
        .size(3.5.dp)
        .clip(CircleShape)
        .background(if (isSelected) Color.White else Color.Transparent)
    )
  }
}
