package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Celebration
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Hexagon
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.HexagonCalculator
import com.example.model.AssessmentReport
import com.example.model.BalanceMission
import com.example.model.DimensionDiagnostic
import com.example.model.DimensionsProvider
import com.example.model.LifeDimension
import com.example.ui.components.DualLayerHexagonRadarChart
import com.example.ui.components.bouncyScale
import com.example.ui.components.rememberBounceInteractionSource
import com.example.ui.theme.AccentCharcoal
import com.example.ui.theme.BorderLight
import com.example.ui.theme.SurfaceCardLight
import com.example.ui.theme.SurfaceVariantLight
import com.example.ui.theme.TextPrimaryLight
import com.example.ui.theme.TextSecondaryLight

private val CompletedGrayContainer = Color(0xFFE5E7EB)
private val CompletedGrayContent = Color(0xFF4B5563)
private val CompletedGrayCardBg = Color(0xFFF3F4F6)

enum class HabilidadesFilterTab(val label: String, val icon: ImageVector) {
  RESUMEN("Resumen", Icons.Outlined.BarChart),
  FORTALEZAS("Fortalezas", Icons.Outlined.FitnessCenter),
  CRECIMIENTO("Crecimiento", Icons.Outlined.Spa),
  NUEVOS_RETOS("Retos", Icons.Outlined.AutoAwesome)
}

/**
 * Puntaje efectivo unificado entre todas las pestañas para evitar inconsistencias:
 * si el puntaje real (últimos 14 días) alcanza 7+ se consolida como fortaleza;
 * mientras tanto usa el puntaje inicial percibido hasta superarlo.
 */
private fun effectiveSkillScore(perceivedScore: Int, realScore: Int): Int {
  return if (realScore >= 7) realScore else perceivedScore
}

private fun isSkillStrength(perceivedScore: Int, realScore: Int): Boolean {
  return effectiveSkillScore(perceivedScore, realScore) >= 7
}

/**
 * Centro de Habilidades rediseñado con Material Design 3, animaciones spring,
 * sincronización total con Room y el hexágono dinámico de dos capas, y estado
 * completado en gris para todas las misiones.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HabilidadesSection(
  report: AssessmentReport,
  realScores: Map<Int, Int>,
  realScoresFloat: Map<Int, Float>,
  expByDimension14d: Map<Int, Int>,
  liveHarmonyPercentage: Int,
  allMissions: List<BalanceMission>,
  onLaunchMission: (BalanceMission) -> Unit,
  onOpenCreateMission: () -> Unit,
  onOpenFullReport: () -> Unit,
  modifier: Modifier = Modifier
) {
  var activeTab by remember { mutableStateOf(HabilidadesFilterTab.RESUMEN) }
  var expandedDimensionIndex by remember { mutableStateOf<Int?>(null) }
  val haptic = LocalHapticFeedback.current
  val scrollState = rememberScrollState()

  val animatedHarmony by animateIntAsState(
    targetValue = liveHarmonyPercentage,
    animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing),
    label = "harmony_counter"
  )

  val strengthsCount = remember(report, realScores) {
    report.diagnostics.count { diag ->
      val real = realScores[diag.dimension.index] ?: 0
      isSkillStrength(diag.score, real)
    }
  }
  val growthCount = 6 - strengthsCount

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(scrollState)
      .padding(horizontal = 20.dp, vertical = 14.dp)
  ) {
    // 1. Encabezado principal sincronizado con el progreso real del panel
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = "Centro de Habilidades",
          style = MaterialTheme.typography.displaySmall.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp
          ),
          color = TextPrimaryLight
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = "Sincronizado en tiempo real con tu hexágono",
          style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
          color = TextSecondaryLight
        )
      }

      val harmonyInteraction = rememberBounceInteractionSource()
      Surface(
        onClick = {
          haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
          onOpenFullReport()
        },
        interactionSource = harmonyInteraction,
        shape = RoundedCornerShape(16.dp),
        color = SurfaceCardLight,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
        shadowElevation = 1.dp,
        modifier = Modifier
          .bouncyScale(harmonyInteraction, scaleDown = 0.93f)
          .testTag("btn_skills_harmony_report")
      ) {
        Column(
          modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = "$animatedHarmony%",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = AccentCharcoal
          )
          Text(
            text = "Armonía",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondaryLight
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // 2. Barra segmentada Material 3 con rebote táctil y transición animada
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = SurfaceVariantLight,
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        HabilidadesFilterTab.entries.forEach { tab ->
          val isSelected = activeTab == tab
          val tabInteraction = rememberBounceInteractionSource()
          val bgColor by animateColorAsState(
            targetValue = if (isSelected) AccentCharcoal else Color.Transparent,
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
            label = "tab_bg_${tab.name}"
          )
          val textColor by animateColorAsState(
            targetValue = if (isSelected) Color.White else TextSecondaryLight,
            label = "tab_text_${tab.name}"
          )

          Surface(
            onClick = {
              haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              activeTab = tab
              expandedDimensionIndex = null
            },
            interactionSource = tabInteraction,
            shape = RoundedCornerShape(16.dp),
            color = bgColor,
            modifier = Modifier
              .weight(1f)
              .bouncyScale(tabInteraction, scaleDown = 0.93f)
              .testTag("skills_tab_${tab.name.lowercase()}")
          ) {
            Row(
              modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center
            ) {
              Icon(
                imageVector = tab.icon,
                contentDescription = tab.label,
                tint = textColor,
                modifier = Modifier.size(15.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = tab.label,
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                  fontSize = 11.5.sp
                ),
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(18.dp))

    // 3. Contenido animado según la pestaña activa
    AnimatedContent(
      targetState = activeTab,
      transitionSpec = {
        val direction = if (targetState.ordinal >= initialState.ordinal) 1 else -1
        (fadeIn(tween(220)) + slideInHorizontally(
          animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
        ) { width -> direction * (width / 5) }) togetherWith
            (fadeOut(tween(160)) + slideOutHorizontally { width -> -direction * (width / 5) })
      },
      label = "skills_tab_content"
    ) { currentTab ->
      when (currentTab) {
        HabilidadesFilterTab.RESUMEN -> {
          ResumenHabilidadesTab(
            report = report,
            realScores = realScores,
            realScoresFloat = realScoresFloat,
            expByDimension14d = expByDimension14d,
            strengthsCount = strengthsCount,
            growthCount = growthCount,
            allMissions = allMissions,
            expandedDimensionIndex = expandedDimensionIndex,
            onToggleExpand = { dimIdx ->
              haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              expandedDimensionIndex = if (expandedDimensionIndex == dimIdx) null else dimIdx
            },
            onNavigateToTab = { targetTab ->
              haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              activeTab = targetTab
            },
            onLaunchMission = onLaunchMission,
            onOpenFullReport = onOpenFullReport
          )
        }

        HabilidadesFilterTab.FORTALEZAS -> {
          FortalezasTab(
            report = report,
            realScores = realScores,
            expByDimension14d = expByDimension14d,
            allMissions = allMissions,
            onLaunchMission = onLaunchMission
          )
        }

        HabilidadesFilterTab.CRECIMIENTO -> {
          EspaciosCrecimientoTab(
            report = report,
            realScores = realScores,
            expByDimension14d = expByDimension14d,
            allMissions = allMissions,
            onLaunchMission = onLaunchMission
          )
        }

        HabilidadesFilterTab.NUEVOS_RETOS -> {
          NuevosRetosTab(
            allMissions = allMissions,
            onLaunchMission = onLaunchMission,
            onOpenCreateMission = onOpenCreateMission
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(104.dp))
  }
}

/**
 * Pestaña 1: Mapa Hexagonal dinámico + métricas sincronizadas + explorador expandible de las 6 dimensiones.
 */
@Composable
private fun ResumenHabilidadesTab(
  report: AssessmentReport,
  realScores: Map<Int, Int>,
  realScoresFloat: Map<Int, Float>,
  expByDimension14d: Map<Int, Int>,
  strengthsCount: Int,
  growthCount: Int,
  allMissions: List<BalanceMission>,
  expandedDimensionIndex: Int?,
  onToggleExpand: (Int) -> Unit,
  onNavigateToTab: (HabilidadesFilterTab) -> Unit,
  onLaunchMission: (BalanceMission) -> Unit,
  onOpenFullReport: () -> Unit
) {
  val totalExp14d = expByDimension14d.values.sum()
  val completedTodayCount = allMissions.count { it.isCompleted }

  Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
    // Hexágono dinámico de 2 capas
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = SurfaceCardLight,
      border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
      shadowElevation = 2.dp,
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "Mapa Dinámico (14 días)",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = TextPrimaryLight
            )
            Text(
              text = if (totalExp14d > 0) {
                "$totalExp14d EXP sumados al hexágono • $completedTodayCount hechas hoy"
              } else {
                "Toca un vértice para inspeccionar y sumar EXP"
              },
              style = MaterialTheme.typography.labelSmall,
              color = TextSecondaryLight
            )
          }

          val reportBtnInteraction = rememberBounceInteractionSource()
          Surface(
            onClick = onOpenFullReport,
            interactionSource = reportBtnInteraction,
            shape = RoundedCornerShape(12.dp),
            color = SurfaceVariantLight,
            modifier = Modifier.bouncyScale(reportBtnInteraction, scaleDown = 0.92f)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Outlined.Hexagon,
                contentDescription = null,
                tint = AccentCharcoal,
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "Informe",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = AccentCharcoal
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        DualLayerHexagonRadarChart(
          perceivedScores = report.scores,
          realScores = realScoresFloat,
          selectedDimensionIndex = expandedDimensionIndex,
          onDimensionSelect = onToggleExpand
        )
      }
    }

    // Tarjetas rápidas sincronizadas (tocarlas navega directamente a Fortalezas / Crecimiento)
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      val strengthsCardInteraction = rememberBounceInteractionSource()
      Surface(
        onClick = { onNavigateToTab(HabilidadesFilterTab.FORTALEZAS) },
        interactionSource = strengthsCardInteraction,
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFF0FDF4),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0)),
        modifier = Modifier
          .weight(1f)
          .bouncyScale(strengthsCardInteraction, scaleDown = 0.94f)
      ) {
        Row(
          modifier = Modifier.padding(14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Outlined.FitnessCenter,
            contentDescription = null,
            tint = Color(0xFF15803D),
            modifier = Modifier.size(22.dp)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "$strengthsCount Fortalezas",
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
              color = Color(0xFF15803D)
            )
            Text(
              text = "Puntos sólidos",
              style = MaterialTheme.typography.labelSmall,
              color = Color(0xFF166534)
            )
          }
        }
      }

      val growthCardInteraction = rememberBounceInteractionSource()
      Surface(
        onClick = { onNavigateToTab(HabilidadesFilterTab.CRECIMIENTO) },
        interactionSource = growthCardInteraction,
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFFFFBEB),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A)),
        modifier = Modifier
          .weight(1f)
          .bouncyScale(growthCardInteraction, scaleDown = 0.94f)
      ) {
        Row(
          modifier = Modifier.padding(14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Outlined.Spa,
            contentDescription = null,
            tint = Color(0xFFB45309),
            modifier = Modifier.size(22.dp)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "$growthCount En Crecimiento",
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
              color = Color(0xFFB45309)
            )
            Text(
              text = "Espacios de mejora",
              style = MaterialTheme.typography.labelSmall,
              color = Color(0xFF92400E)
            )
          }
        }
      }
    }

    Text(
      text = "Explorador de Habilidades",
      style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
      color = TextPrimaryLight
    )

    // 6 tarjetas interactivas expandibles sincronizadas con Room
    DimensionsProvider.dimensions.forEach { dim ->
      val diag = report.diagnostics.find { it.dimension.index == dim.index }
      val isExpanded = expandedDimensionIndex == dim.index
      val perceived = report.scores[dim.index] ?: 5
      val real = realScores[dim.index] ?: 0
      val exp14d = expByDimension14d[dim.index] ?: 0
      val isStrong = isSkillStrength(perceived, real)
      val dimMissions = allMissions.filter { it.dimensionIndex == dim.index }

      InteractiveSkillCard(
        dimension = dim,
        diagnostic = diag,
        perceivedScore = perceived,
        realScore = real,
        exp14d = exp14d,
        isStrong = isStrong,
        isExpanded = isExpanded,
        onToggle = { onToggleExpand(dim.index) },
        dimensionMissions = dimMissions,
        onLaunchMission = onLaunchMission
      )
    }
  }
}

/**
 * Pestaña 2: Fortalezas (con progreso real, misiones sincronizadas y estado completado en gris).
 */
@Composable
private fun FortalezasTab(
  report: AssessmentReport,
  realScores: Map<Int, Int>,
  expByDimension14d: Map<Int, Int>,
  allMissions: List<BalanceMission>,
  onLaunchMission: (BalanceMission) -> Unit
) {
  val strongDiagnostics = remember(report, realScores) {
    report.diagnostics.filter { diag ->
      val real = realScores[diag.dimension.index] ?: 0
      isSkillStrength(diag.score, real)
    }
  }

  Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = Color(0xFFF0FDF4),
      border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0)),
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Default.Star,
          contentDescription = null,
          tint = Color(0xFF16A34A),
          modifier = Modifier.size(26.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
          Text(
            text = "Tus Cimientos Sólidos",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFF15803D)
          )
          Text(
            text = "Cada misión completada suma puntos reales al hexágono y mantiene estas áreas fuertes.",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF166534),
            lineHeight = 18.sp
          )
        }
      }
    }

    if (strongDiagnostics.isEmpty()) {
      Surface(
        shape = RoundedCornerShape(20.dp),
        color = SurfaceVariantLight,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Icon(
            imageVector = Icons.Outlined.Spa,
            contentDescription = null,
            tint = TextSecondaryLight,
            modifier = Modifier.size(34.dp)
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "Tus fortalezas están en construcción",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = TextPrimaryLight
          )
          Text(
            text = "Completa misiones diarias en Espacios de Crecimiento para consolidar dimensiones sobre 7 puntos.",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondaryLight,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
          )
        }
      }
    } else {
      strongDiagnostics.forEach { diag ->
        val dim = diag.dimension
        val real = realScores[dim.index] ?: 0
        val exp14d = expByDimension14d[dim.index] ?: 0
        val effective = effectiveSkillScore(diag.score, real)
        val dimMissions = allMissions.filter { it.dimensionIndex == dim.index }

        Surface(
          shape = RoundedCornerShape(22.dp),
          color = SurfaceCardLight,
          border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
          shadowElevation = 2.dp,
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(dim.accentColor)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = dim.title,
                  style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                  color = TextPrimaryLight
                )
              }

              Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFDCFCE7)
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFF15803D),
                    modifier = Modifier.size(14.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = if (real > 0) "Real $real/10 • Inicial ${diag.score}" else "$effective / 10",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF15803D)
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            DimensionExpProgressBar(
              exp14d = exp14d,
              accentColor = dim.accentColor
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
              text = diag.diagnosisSummary,
              style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
              color = TextPrimaryLight
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
              text = diag.currentImpact,
              style = MaterialTheme.typography.bodySmall,
              color = TextSecondaryLight,
              lineHeight = 19.sp
            )

            if (dimMissions.isNotEmpty()) {
              Spacer(modifier = Modifier.height(14.dp))
              Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                dimMissions.forEach { mission ->
                  SyncedSkillMissionActionButton(
                    mission = mission,
                    activeColor = dim.accentColor,
                    actionPrefix = "Mantener",
                    onClick = { onLaunchMission(mission) }
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}

/**
 * Pestaña 3: Espacios de Crecimiento (prioriza las áreas con mayor oportunidad y suma al hexágono).
 */
@Composable
private fun EspaciosCrecimientoTab(
  report: AssessmentReport,
  realScores: Map<Int, Int>,
  expByDimension14d: Map<Int, Int>,
  allMissions: List<BalanceMission>,
  onLaunchMission: (BalanceMission) -> Unit
) {
  val growthDiagnostics = remember(report, realScores) {
    report.diagnostics.filter { diag ->
      val real = realScores[diag.dimension.index] ?: 0
      !isSkillStrength(diag.score, real)
    }.sortedBy { diag ->
      val real = realScores[diag.dimension.index] ?: 0
      if (real > 0) real else diag.score
    }
  }

  Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = Color(0xFFFFFBEB),
      border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A)),
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Outlined.Lightbulb,
          contentDescription = null,
          tint = Color(0xFFB45309),
          modifier = Modifier.size(26.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
          Text(
            text = "Tus Áreas con Espacio para Crecer",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFF92400E)
          )
          Text(
            text = "Otorgan 1.5x EXP. Al superar 7/10 en acciones reales se convierten en fortalezas.",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF78350F),
            lineHeight = 18.sp
          )
        }
      }
    }

    if (growthDiagnostics.isEmpty()) {
      Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFF0FDF4),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Icon(
            imageVector = Icons.Outlined.Celebration,
            contentDescription = null,
            tint = Color(0xFF15803D),
            modifier = Modifier.size(36.dp)
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "¡Equilibrio integral alcanzado!",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFF15803D)
          )
          Text(
            text = "Todas tus dimensiones superan los 6 puntos. Sigue manteniendo tus hábitos diarios.",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF166534),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
          )
        }
      }
    } else {
      growthDiagnostics.forEach { diag ->
        val dim = diag.dimension
        val real = realScores[dim.index] ?: 0
        val exp14d = expByDimension14d[dim.index] ?: 0
        val dimMissions = allMissions.filter { it.dimensionIndex == dim.index }

        Surface(
          shape = RoundedCornerShape(22.dp),
          color = SurfaceCardLight,
          border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
          shadowElevation = 2.dp,
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(dim.accentColor)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = dim.title,
                  style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                  color = TextPrimaryLight
                )
              }

              Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFFEF3C7)
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(
                    imageVector = Icons.Default.ElectricBolt,
                    contentDescription = null,
                    tint = Color(0xFFB45309),
                    modifier = Modifier.size(14.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "1.5x EXP • ${if (real > 0) "Real $real" else diag.score}/10",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFFB45309)
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            DimensionExpProgressBar(
              exp14d = exp14d,
              accentColor = dim.accentColor
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
              text = diag.diagnosisSummary,
              style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
              color = TextPrimaryLight
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
              text = diag.currentImpact,
              style = MaterialTheme.typography.bodySmall,
              color = TextSecondaryLight,
              lineHeight = 19.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
              shape = RoundedCornerShape(14.dp),
              color = SurfaceVariantLight
            ) {
              Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Outlined.Lightbulb,
                  contentDescription = null,
                  tint = Color(0xFFB45309),
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = diag.actionableImprovements.firstOrNull()
                    ?: "Dedica 10 min a avanzar sin presiones.",
                  style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                  color = TextPrimaryLight,
                  lineHeight = 18.sp
                )
              }
            }

            if (dimMissions.isNotEmpty()) {
              Spacer(modifier = Modifier.height(14.dp))
              Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                dimMissions.forEach { mission ->
                  SyncedSkillMissionActionButton(
                    mission = mission,
                    activeColor = dim.accentColor,
                    actionPrefix = "Impulsar",
                    onClick = { onLaunchMission(mission) }
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}

/**
 * Pestaña 4: Nuevos Retos y Misiones Personalizadas (100% sincronizada con completados en gris).
 */
@Composable
private fun NuevosRetosTab(
  allMissions: List<BalanceMission>,
  onLaunchMission: (BalanceMission) -> Unit,
  onOpenCreateMission: () -> Unit
) {
  val customMissions = remember(allMissions) { allMissions.filter { it.isCustom } }
  val featuredChallenges = remember(allMissions) {
    allMissions.filter { !it.isCustom }.sortedBy { it.isCompleted }.take(6)
  }

  Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
    Surface(
      shape = RoundedCornerShape(22.dp),
      color = AccentCharcoal,
      shadowElevation = 4.dp,
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = Color(0xFFF59E0B),
            modifier = Modifier.size(24.dp)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = "Diseña Tu Propio Desafío",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = Color.White
          )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
          text = "Crea misiones y hábitos a medida para sumar puntos directamente al hexágono dinámico.",
          style = MaterialTheme.typography.bodySmall,
          color = Color.White.copy(alpha = 0.85f),
          lineHeight = 19.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        val createBtnInteraction = rememberBounceInteractionSource()
        Button(
          onClick = onOpenCreateMission,
          interactionSource = createBtnInteraction,
          colors = ButtonDefaults.buttonColors(containerColor = Color.White),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .fillMaxWidth()
            .bouncyScale(createBtnInteraction)
        ) {
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            tint = AccentCharcoal,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Crear Mi Misión",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = AccentCharcoal
          )
        }
      }
    }

    if (customMissions.isNotEmpty()) {
      Text(
        text = "Tus Misiones Creadas (${customMissions.size})",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = TextPrimaryLight
      )
      customMissions.forEach { mission ->
        RetoMissionCard(
          mission = mission,
          onLaunchMission = { onLaunchMission(mission) }
        )
      }
    }

    Text(
      text = "Desafíos para el Hexágono",
      style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
      color = TextPrimaryLight
    )

    featuredChallenges.forEach { mission ->
      RetoMissionCard(
        mission = mission,
        onLaunchMission = { onLaunchMission(mission) }
      )
    }
  }
}

@Composable
private fun RetoMissionCard(
  mission: BalanceMission,
  onLaunchMission: () -> Unit
) {
  val dim = DimensionsProvider.dimensions.getOrNull(mission.dimensionIndex)
  val accentColor = dim?.accentColor ?: AccentCharcoal
  val cardInteraction = rememberBounceInteractionSource()
  val btnInteraction = rememberBounceInteractionSource()

  val cardBg by animateColorAsState(
    targetValue = if (mission.isCompleted) CompletedGrayCardBg else SurfaceCardLight,
    label = "reto_card_bg_${mission.id}"
  )
  val buttonBg by animateColorAsState(
    targetValue = if (mission.isCompleted) CompletedGrayContainer else accentColor,
    label = "reto_btn_bg_${mission.id}"
  )
  val buttonContent by animateColorAsState(
    targetValue = if (mission.isCompleted) CompletedGrayContent else Color.White,
    label = "reto_btn_fg_${mission.id}"
  )

  Surface(
    onClick = onLaunchMission,
    interactionSource = cardInteraction,
    shape = RoundedCornerShape(18.dp),
    color = cardBg,
    border = androidx.compose.foundation.BorderStroke(
      1.dp,
      if (mission.isCompleted) CompletedGrayContainer else BorderLight
    ),
    modifier = Modifier
      .fillMaxWidth()
      .bouncyScale(cardInteraction, scaleDown = 0.97f)
  ) {
    Row(
      modifier = Modifier.padding(16.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = mission.dimensionTitle.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = if (mission.isCompleted) CompletedGrayContent else accentColor
          )
          if (mission.isCompleted) {
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "• Completada",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = CompletedGrayContent
            )
          }
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
          text = mission.title,
          style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.Bold,
            textDecoration = if (mission.isCompleted) TextDecoration.LineThrough else TextDecoration.None
          ),
          color = if (mission.isCompleted) CompletedGrayContent else TextPrimaryLight
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = "${mission.durationLabel} • +${mission.expReward} EXP",
          style = MaterialTheme.typography.labelSmall,
          color = TextSecondaryLight
        )
      }

      Spacer(modifier = Modifier.width(10.dp))

      Button(
        onClick = onLaunchMission,
        interactionSource = btnInteraction,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = buttonBg,
          contentColor = buttonContent
        ),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        modifier = Modifier.bouncyScale(btnInteraction, scaleDown = 0.92f)
      ) {
        if (mission.isCompleted) {
          Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = buttonContent,
            modifier = Modifier.size(15.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text("Hecha", fontWeight = FontWeight.Bold, color = buttonContent)
        } else {
          Text("Ver Detalles", fontWeight = FontWeight.Bold, color = buttonContent)
        }
      }
    }
  }
}

/**
 * Barra animada de progreso real (últimos 14 días) que alimenta el vértice del hexágono.
 */
@Composable
private fun DimensionExpProgressBar(
  exp14d: Int,
  accentColor: Color
) {
  val target = HexagonCalculator.DEFAULT_OBJETIVO_EXP
  val progress by animateFloatAsState(
    targetValue = (exp14d.toFloat() / target.toFloat()).coerceIn(0f, 1f),
    animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow),
    label = "dim_exp_progress"
  )

  Column(modifier = Modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "Progreso al hexágono (14 días)",
        style = MaterialTheme.typography.labelSmall,
        color = TextSecondaryLight
      )
      Text(
        text = "$exp14d / $target EXP",
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
        color = accentColor
      )
    }
    Spacer(modifier = Modifier.height(6.dp))
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(7.dp)
        .clip(RoundedCornerShape(4.dp))
        .background(SurfaceVariantLight)
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth(progress)
          .height(7.dp)
          .clip(RoundedCornerShape(4.dp))
          .background(accentColor)
      )
    }
  }
}

/**
 * Botón interactivo con animación de rebote para lanzar una misión a detalles y que se sincroniza
 * en gris cuando la misión está completada.
 */
@Composable
private fun SyncedSkillMissionActionButton(
  mission: BalanceMission,
  activeColor: Color,
  actionPrefix: String,
  onClick: () -> Unit
) {
  val interactionSource = rememberBounceInteractionSource()
  val isCompleted = mission.isCompleted

  val containerColor by animateColorAsState(
    targetValue = if (isCompleted) CompletedGrayContainer else activeColor,
    animationSpec = tween(250),
    label = "skill_mission_btn_bg_${mission.id}"
  )
  val contentColor by animateColorAsState(
    targetValue = if (isCompleted) CompletedGrayContent else Color.White,
    animationSpec = tween(250),
    label = "skill_mission_btn_fg_${mission.id}"
  )

  Button(
    onClick = onClick,
    interactionSource = interactionSource,
    colors = ButtonDefaults.buttonColors(
      containerColor = containerColor,
      contentColor = contentColor
    ),
    shape = RoundedCornerShape(14.dp),
    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
    modifier = Modifier
      .fillMaxWidth()
      .bouncyScale(interactionSource, scaleDown = 0.94f)
      .testTag("btn_skill_mission_${mission.id}")
  ) {
    Icon(
      imageVector = if (isCompleted) Icons.Filled.CheckCircle else Icons.Default.ElectricBolt,
      contentDescription = null,
      tint = contentColor,
      modifier = Modifier.size(16.dp)
    )
    Spacer(modifier = Modifier.width(6.dp))
    Text(
      text = if (isCompleted) {
        "Completada: ${mission.title}"
      } else {
        "$actionPrefix: ${mission.title}"
      },
      color = contentColor,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      style = MaterialTheme.typography.labelLarge.copy(
        fontWeight = FontWeight.Bold,
        textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None
      ),
      modifier = Modifier.weight(1f, fill = false)
    )
    Spacer(modifier = Modifier.width(6.dp))
    Icon(
      imageVector = if (isCompleted) Icons.Outlined.Info else Icons.AutoMirrored.Filled.ArrowForward,
      contentDescription = null,
      tint = contentColor,
      modifier = Modifier.size(15.dp)
    )
  }
}

/**
 * Tarjeta interactiva expandible para una habilidad individual en el Explorador.
 */
@Composable
private fun InteractiveSkillCard(
  dimension: LifeDimension,
  diagnostic: DimensionDiagnostic?,
  perceivedScore: Int,
  realScore: Int,
  exp14d: Int,
  isStrong: Boolean,
  isExpanded: Boolean,
  onToggle: () -> Unit,
  dimensionMissions: List<BalanceMission>,
  onLaunchMission: (BalanceMission) -> Unit
) {
  val accentColor = dimension.accentColor
  val cardInteraction = rememberBounceInteractionSource()
  val effective = effectiveSkillScore(perceivedScore, realScore)
  val completedInDim = dimensionMissions.count { it.isCompleted }

  Surface(
    onClick = onToggle,
    interactionSource = cardInteraction,
    shape = RoundedCornerShape(20.dp),
    color = SurfaceCardLight,
    border = androidx.compose.foundation.BorderStroke(
      1.dp,
      if (isExpanded) accentColor else BorderLight
    ),
    shadowElevation = if (isExpanded) 3.dp else 1.dp,
    modifier = Modifier
      .fillMaxWidth()
      .bouncyScale(cardInteraction, scaleDown = 0.98f)
      .animateContentSize(
        animationSpec = spring(
          dampingRatio = Spring.DampingRatioLowBouncy,
          stiffness = Spring.StiffnessMediumLow
        )
      )
      .testTag("skill_card_${dimension.index}")
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
              .size(10.dp)
              .clip(CircleShape)
              .background(accentColor)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = dimension.title,
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = TextPrimaryLight
            )
            if (dimensionMissions.isNotEmpty()) {
              Text(
                text = "$completedInDim/${dimensionMissions.size} misiones hoy • $exp14d EXP",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondaryLight
              )
            }
          }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (isStrong) Color(0xFFDCFCE7) else Color(0xFFFEF3C7)
          ) {
            Text(
              text = if (isStrong) "Fortaleza ($effective/10)" else "Crecimiento ($effective/10)",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = if (isStrong) Color(0xFF15803D) else Color(0xFFB45309),
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }

          Spacer(modifier = Modifier.width(6.dp))

          Icon(
            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
            contentDescription = null,
            tint = TextSecondaryLight
          )
        }
      }

      AnimatedVisibility(
        visible = isExpanded,
        enter = fadeIn(tween(200)) + expandVertically(),
        exit = fadeOut(tween(150)) + shrinkVertically()
      ) {
        Column(modifier = Modifier.padding(top = 14.dp)) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(1.dp)
              .background(BorderLight)
          )

          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = "Percibido (Cuestionario): $perceivedScore / 10",
              style = MaterialTheme.typography.labelSmall,
              color = TextSecondaryLight
            )
            Text(
              text = "Real (14 días): $realScore / 10",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = if (realScore > 0) accentColor else TextSecondaryLight
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          DimensionExpProgressBar(
            exp14d = exp14d,
            accentColor = accentColor
          )

          Spacer(modifier = Modifier.height(10.dp))

          if (diagnostic != null) {
            Text(
              text = diagnostic.diagnosisSummary,
              style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
              color = TextPrimaryLight
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
              text = diagnostic.currentImpact,
              style = MaterialTheme.typography.bodySmall,
              color = TextSecondaryLight,
              lineHeight = 18.sp
            )
          }

          if (dimensionMissions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(14.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              dimensionMissions.forEach { mission ->
                SyncedSkillMissionActionButton(
                  mission = mission,
                  activeColor = dimension.accentColor,
                  actionPrefix = "Realizar Misión",
                  onClick = { onLaunchMission(mission) }
                )
              }
            }
          }
        }
      }
    }
  }
}
