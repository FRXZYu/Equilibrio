package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Hexagon
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.HexagonCalculator
import com.example.model.AssessmentReport
import com.example.model.BalanceMission
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
import com.example.ui.theme.TextTertiaryLight

private enum class HabilidadesFilter(val label: String) {
  ALL("Todas"),
  BOOST("A fortalecer (1.5x)"),
  STRONG("Fortalezas")
}

/**
 * Apartado de Habilidades sincronizado en vivo con Room y el Hexágono Dinámico.
 * Cada misión muestra únicamente "Ver detalles" y refleja estado completado en gris + Material 3.
 */
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
  val haptic = LocalHapticFeedback.current
  val dimensions = DimensionsProvider.dimensions

  var activeFilter by remember { mutableStateOf(HabilidadesFilter.ALL) }
  var selectedDimensionIndex by remember {
    mutableIntStateOf(HexagonCalculator.findLowestRealDimension(realScores, report.scores))
  }
  var expandedDimIndex by remember {
    mutableStateOf<Int?>(HexagonCalculator.findLowestRealDimension(realScores, report.scores))
  }

  val animatedHarmony by animateIntAsState(
    targetValue = liveHarmonyPercentage,
    animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing),
    label = "hab_harmony"
  )

  val displayedDimensions = remember(activeFilter, report.scores, realScores) {
    val withStatus = dimensions.map { dim ->
      val perceived = report.scores[dim.index] ?: 5
      val real = realScores[dim.index] ?: 0
      val isWeak = perceived <= 6 || real < 4
      dim to isWeak
    }
    when (activeFilter) {
      HabilidadesFilter.ALL -> withStatus.sortedBy { realScores[it.first.index] ?: 0 }
      HabilidadesFilter.BOOST -> withStatus.filter { it.second }.ifEmpty { withStatus.take(2) }
      HabilidadesFilter.STRONG -> withStatus.filter { !it.second }
    }.map { it.first }
  }

  val createInteraction = rememberBounceInteractionSource()
  val reportInteraction = rememberBounceInteractionSource()

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 20.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    // Encabezado
    item(key = "hab_header") {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Habilidades",
            style = MaterialTheme.typography.headlineMedium.copy(
              fontSize = 24.sp,
              fontWeight = FontWeight.Bold
            ),
            color = TextPrimaryLight
          )
          Text(
            text = "Toca una dimensión o vértice para entrenarla",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondaryLight
          )
        }

        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Surface(
            onClick = {
              haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              onOpenFullReport()
            },
            interactionSource = reportInteraction,
            shape = RoundedCornerShape(12.dp),
            color = SurfaceCardLight,
            border = BorderStroke(1.dp, BorderLight),
            modifier = Modifier
              .minimumInteractiveComponentSize()
              .bouncyScale(reportInteraction, scaleDown = 0.93f)
              .testTag("btn_habilidades_report")
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Outlined.Hexagon,
                contentDescription = "Ver estadística",
                tint = AccentCharcoal,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "$animatedHarmony%",
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = FontWeight.Bold,
                  fontFeatureSettings = "tnum"
                ),
                color = TextPrimaryLight
              )
            }
          }

          Surface(
            onClick = {
              haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              onOpenCreateMission()
            },
            interactionSource = createInteraction,
            shape = RoundedCornerShape(12.dp),
            color = AccentCharcoal,
            modifier = Modifier
              .minimumInteractiveComponentSize()
              .bouncyScale(createInteraction, scaleDown = 0.92f)
              .testTag("btn_habilidades_create")
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
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
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
              )
            }
          }
        }
      }
    }

    // Mini radar sincronizado en vivo
    item(key = "hab_radar") {
      Surface(
        shape = RoundedCornerShape(24.dp),
        color = SurfaceCardLight,
        border = BorderStroke(1.dp, BorderLight),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          DualLayerHexagonRadarChart(
            perceivedScores = report.scores,
            realScores = realScoresFloat,
            selectedDimensionIndex = selectedDimensionIndex,
            onDimensionSelect = { idx ->
              haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              selectedDimensionIndex = idx
              expandedDimIndex = idx
            },
            chartSize = 188.dp,
            showLegend = true,
            showChips = false
          )
        }
      }
    }

    // Filtros rápidos
    item(key = "hab_filters") {
      LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        items(HabilidadesFilter.entries, key = { it.name }) { filterOption ->
          val isSelected = activeFilter == filterOption
          val interaction = rememberBounceInteractionSource()
          val chipBg by animateColorAsState(
            targetValue = if (isSelected) AccentCharcoal else SurfaceCardLight,
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
            label = "hab_filter_bg_${filterOption.name}"
          )
          Surface(
            onClick = {
              haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              activeFilter = filterOption
            },
            interactionSource = interaction,
            shape = RoundedCornerShape(14.dp),
            color = chipBg,
            border = BorderStroke(1.dp, if (isSelected) AccentCharcoal else BorderLight),
            modifier = Modifier
              .minimumInteractiveComponentSize()
              .bouncyScale(interaction, scaleDown = 0.93f)
              .testTag("hab_filter_${filterOption.name.lowercase()}")
          ) {
            Text(
              text = filterOption.label,
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
              ),
              color = if (isSelected) Color.White else TextPrimaryLight,
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
            )
          }
        }
      }
    }

    if (displayedDimensions.isEmpty()) {
      item(key = "hab_empty") {
        Surface(
          shape = RoundedCornerShape(20.dp),
          color = SurfaceCardLight,
          border = BorderStroke(1.dp, BorderLight),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = "Todas tus dimensiones están listas para fortalecerse en la pestaña 'Todas'.",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondaryLight,
            modifier = Modifier.padding(16.dp)
          )
        }
      }
    } else {
      items(displayedDimensions, key = { "hab_dim_${it.index}" }) { dim ->
        val dimMissions = remember(allMissions, dim.index) {
          allMissions.filter { it.dimensionIndex == dim.index }
        }
        val perceived = report.scores[dim.index] ?: 5
        val realScore = realScores[dim.index] ?: 0
        val exp14d = expByDimension14d[dim.index] ?: 0
        val isWeak = perceived <= 6 || realScore < 4
        val isExpanded = expandedDimIndex == dim.index

        SkillDimensionCard(
          dimension = dim,
          perceivedScore = perceived,
          realScore = realScore,
          exp14d = exp14d,
          isWeaknessBoost = isWeak,
          isExpanded = isExpanded,
          missions = dimMissions,
          onToggleExpand = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            selectedDimensionIndex = dim.index
            expandedDimIndex = if (isExpanded) null else dim.index
          },
          onLaunchMission = onLaunchMission
        )
      }
    }

    item(key = "hab_bottom_space") {
      Spacer(modifier = Modifier.height(104.dp))
    }
  }
}

@Composable
private fun SkillDimensionCard(
  dimension: LifeDimension,
  perceivedScore: Int,
  realScore: Int,
  exp14d: Int,
  isWeaknessBoost: Boolean,
  isExpanded: Boolean,
  missions: List<BalanceMission>,
  onToggleExpand: () -> Unit,
  onLaunchMission: (BalanceMission) -> Unit
) {
  val cardInteraction = rememberBounceInteractionSource()
  val targetExp = HexagonCalculator.DEFAULT_EXP_OBJETIVOS[dimension.index]
    ?: HexagonCalculator.DEFAULT_OBJETIVO_EXP
  val progress by animateFloatAsState(
    targetValue = (exp14d.toFloat() / targetExp.toFloat()).coerceIn(0f, 1f),
    animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
    label = "skill_progress_${dimension.index}"
  )
  val borderColor by animateColorAsState(
    targetValue = if (isExpanded) dimension.accentColor else BorderLight,
    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
    label = "skill_border_${dimension.index}"
  )

  val completedCount = remember(missions) { missions.count { it.isCompleted } }

  Surface(
    shape = RoundedCornerShape(22.dp),
    color = SurfaceCardLight,
    border = BorderStroke(if (isExpanded) 1.5.dp else 1.dp, borderColor),
    shadowElevation = if (isExpanded) 2.dp else 0.dp,
    modifier = Modifier
      .fillMaxWidth()
      .testTag("skill_dim_card_${dimension.index}")
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      Surface(
        onClick = onToggleExpand,
        interactionSource = cardInteraction,
        color = Color.Transparent,
        modifier = Modifier
          .fillMaxWidth()
          .bouncyScale(cardInteraction, scaleDown = 0.98f)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.weight(1f)
            ) {
              Box(
                modifier = Modifier
                  .size(40.dp)
                  .clip(CircleShape)
                  .background(dimension.accentColor.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = dimension.icon,
                  contentDescription = null,
                  tint = dimension.accentColor,
                  modifier = Modifier.size(20.dp)
                )
              }

              Spacer(modifier = Modifier.width(12.dp))

              Column {
                Text(
                  text = dimension.title,
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                  color = TextPrimaryLight
                )
                Text(
                  text = "Real $realScore/10 · Base $perceivedScore/10 · $completedCount/${missions.size} hoy",
                  style = MaterialTheme.typography.labelSmall,
                  color = TextSecondaryLight
                )
              }
            }

            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isWeaknessBoost) Color(0xFFFEF3C7) else Color(0xFFDCFCE7)
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(
                    imageVector = if (isWeaknessBoost) Icons.Default.Bolt else Icons.Outlined.TrendingUp,
                    contentDescription = null,
                    tint = if (isWeaknessBoost) Color(0xFFB45309) else Color(0xFF15803D),
                    modifier = Modifier.size(13.dp)
                  )
                  Spacer(modifier = Modifier.width(3.dp))
                  Text(
                    text = if (isWeaknessBoost) "1.5x EXP" else "Fuerte",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (isWeaknessBoost) Color(0xFF92400E) else Color(0xFF166534)
                  )
                }
              }

              Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = if (isExpanded) "Contraer" else "Expandir",
                tint = TextSecondaryLight,
                modifier = Modifier.size(20.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(SurfaceVariantLight)
            ) {
              Box(
                modifier = Modifier
                  .fillMaxWidth(progress)
                  .height(6.dp)
                  .clip(RoundedCornerShape(3.dp))
                  .background(dimension.accentColor)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = "$exp14d/$targetExp EXP",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontFeatureSettings = "tnum"
              ),
              color = TextSecondaryLight
            )
          }
        }
      }

      AnimatedVisibility(
        visible = isExpanded,
        enter = fadeIn(tween(200)) + expandVertically(),
        exit = fadeOut(tween(150)) + shrinkVertically()
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          missions.forEach { mission ->
            SkillMissionRow(
              mission = mission,
              accentColor = dimension.accentColor,
              onDetails = { onLaunchMission(mission) }
            )
          }
        }
      }
    }
  }
}

@Composable
private fun SkillMissionRow(
  mission: BalanceMission,
  accentColor: Color,
  onDetails: () -> Unit
) {
  val rowInteraction = rememberBounceInteractionSource()
  val btnInteraction = rememberBounceInteractionSource()

  val bg by animateColorAsState(
    targetValue = if (mission.isCompleted) Color(0xFFF3F4F6) else SurfaceVariantLight.copy(alpha = 0.7f),
    animationSpec = tween(220),
    label = "skill_mission_bg_${mission.id}"
  )

  Surface(
    onClick = onDetails,
    interactionSource = rowInteraction,
    shape = RoundedCornerShape(16.dp),
    color = bg,
    border = BorderStroke(
      1.dp,
      if (mission.isCompleted) Color(0xFFE5E7EB) else BorderLight
    ),
    modifier = Modifier
      .fillMaxWidth()
      .bouncyScale(rowInteraction, scaleDown = 0.98f)
      .testTag("skill_mission_${mission.id}")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 10.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          if (mission.isCompleted) {
            Icon(
              imageVector = Icons.Filled.CheckCircle,
              contentDescription = "Completada",
              tint = Color(0xFF6B7280),
              modifier = Modifier.size(15.dp)
            )
          }
          Text(
            text = mission.title,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = if (mission.isCompleted) Color(0xFF6B7280) else TextPrimaryLight,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }

        Spacer(modifier = Modifier.height(3.dp))

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Outlined.Timer,
              contentDescription = null,
              tint = TextTertiaryLight,
              modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
              text = mission.durationLabel,
              style = MaterialTheme.typography.labelSmall,
              color = TextSecondaryLight
            )
          }

          AnimatedContent(
            targetState = mission.isCompleted,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "skill_mission_badge_${mission.id}"
          ) { done ->
            if (done) {
              Text(
                text = "Completada hoy",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = Color(0xFF6B7280)
              )
            } else {
              Text(
                text = "+${mission.expReward} EXP" + if (mission.hasMultiplier) " (1.5x)" else "",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = accentColor
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.width(10.dp))

      Button(
        onClick = onDetails,
        interactionSource = btnInteraction,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = if (mission.isCompleted) Color(0xFFE5E7EB) else AccentCharcoal,
          contentColor = if (mission.isCompleted) Color(0xFF4B5563) else Color.White
        ),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        modifier = Modifier
          .height(38.dp)
          .bouncyScale(btnInteraction, scaleDown = 0.91f)
          .testTag("btn_skill_details_${mission.id}")
      ) {
        Text(
          text = "Ver detalles",
          style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowForward,
          contentDescription = null,
          modifier = Modifier.size(13.dp)
        )
      }
    }
  }
}
