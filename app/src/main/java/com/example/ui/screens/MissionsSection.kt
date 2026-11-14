package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.Hexagon
import androidx.compose.material.icons.outlined.LocalFireDepartment
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
import com.example.model.AssessmentReport
import com.example.model.BalanceMission
import com.example.model.DimensionsProvider
import com.example.model.GamificationStats
import com.example.ui.components.bouncyScale
import com.example.ui.components.rememberBounceInteractionSource
import com.example.ui.theme.AccentCharcoal
import com.example.ui.theme.BorderLight
import com.example.ui.theme.SurfaceCardLight
import com.example.ui.theme.SurfaceVariantLight
import com.example.ui.theme.TextPrimaryLight
import com.example.ui.theme.TextSecondaryLight
import com.example.ui.theme.TextTertiaryLight

/**
 * Apartado 0: Misiones organizadas por categorías, con meta diaria de EXP, racha,
 * enfoque 1.5x en áreas a fortalecer y tarjetas con botón único de "Ver detalles".
 */
@Composable
fun MissionsSection(
  report: AssessmentReport,
  missions: List<BalanceMission>,
  gamificationStats: GamificationStats,
  onViewMissionDetails: (BalanceMission) -> Unit,
  onOpenReport: () -> Unit,
  onOpenCreateMission: () -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedCategoryIndex by remember { mutableIntStateOf(0) }
  val haptic = LocalHapticFeedback.current

  val categories = remember {
    listOf(
      "Recomendado",
      "Autoestima",
      "Ejercicio",
      "Descanso",
      "Relaciones",
      "Hobbies",
      "Excelencia",
      "Guardadas",
      "Tus creaciones"
    )
  }

  val weakDimensions = remember(report.scores) {
    report.scores.entries
      .sortedBy { it.value }
      .take(2)
      .map { it.key }
      .toSet()
  }

  val filteredMissions = remember(selectedCategoryIndex, missions, weakDimensions) {
    val baseList = when (selectedCategoryIndex) {
      0 -> missions.filter { it.isWeaknessFocus || it.isPriorityFocus || it.dimensionIndex in weakDimensions }
        .ifEmpty { missions }
      in 1..6 -> missions.filter { it.dimensionIndex == selectedCategoryIndex - 1 }
      7 -> missions.filter { it.isSavedForLater }
      8 -> missions.filter { it.isCustom || it.isHabit }
      else -> missions
    }
    baseList.sortedWith(
      compareBy<BalanceMission> { it.isCompleted }
        .thenByDescending { it.hasMultiplier }
    )
  }

  val dailyCapProgress by animateFloatAsState(
    targetValue = (gamificationStats.expToday.toFloat() / GamificationStats.DAILY_EXP_CAP.toFloat()).coerceIn(0f, 1f),
    animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
    label = "missions_daily_cap"
  )

  val createBtnInteraction = rememberBounceInteractionSource()
  val summaryCardInteraction = rememberBounceInteractionSource()

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 20.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    item(key = "missions_header") {
      Column(modifier = Modifier.padding(top = 12.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Misiones",
              style = MaterialTheme.typography.headlineMedium.copy(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
              ),
              color = TextPrimaryLight
            )
            Text(
              text = "Abre cualquier misión para ver pasos y completarla",
              style = MaterialTheme.typography.bodySmall,
              color = TextSecondaryLight
            )
          }

          Surface(
            onClick = {
              haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              onOpenCreateMission()
            },
            interactionSource = createBtnInteraction,
            color = AccentCharcoal,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .minimumInteractiveComponentSize()
              .bouncyScale(createBtnInteraction, scaleDown = 0.92f)
              .testTag("btn_missions_create")
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
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

        Spacer(modifier = Modifier.height(12.dp))

        // Tarjeta compacta de EXP diaria + Racha + Acceso a Diagnóstico
        Surface(
          onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onOpenReport()
          },
          interactionSource = summaryCardInteraction,
          shape = RoundedCornerShape(18.dp),
          color = SurfaceCardLight,
          border = BorderStroke(1.dp, BorderLight),
          modifier = Modifier
            .fillMaxWidth()
            .bouncyScale(summaryCardInteraction, scaleDown = 0.98f)
            .testTag("btn_missions_open_report")
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(AccentCharcoal.copy(alpha = 0.08f)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = AccentCharcoal,
                    modifier = Modifier.size(18.dp)
                  )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text(
                    text = "${gamificationStats.expToday} / ${GamificationStats.DAILY_EXP_CAP} EXP hoy",
                    style = MaterialTheme.typography.labelLarge.copy(
                      fontWeight = FontWeight.Bold,
                      fontFeatureSettings = "tnum"
                    ),
                    color = TextPrimaryLight
                  )
                  Text(
                    text = if (gamificationStats.isDailyCapReached) {
                      "Tope diario alcanzado · Sigue sumando al hexágono"
                    } else {
                      "Nivel ${gamificationStats.level} (${gamificationStats.rank}) · 1.5x en áreas débiles"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondaryLight
                  )
                }
              }

              Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Surface(
                  shape = RoundedCornerShape(10.dp),
                  color = SurfaceVariantLight
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(
                      imageVector = Icons.Outlined.LocalFireDepartment,
                      contentDescription = "Racha",
                      tint = if (gamificationStats.activeStreakDays > 0) Color(0xFFD97706) else TextTertiaryLight,
                      modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                      text = "${gamificationStats.activeStreakDays}d",
                      style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                      color = TextPrimaryLight
                    )
                  }
                }
                Icon(
                  imageVector = Icons.Outlined.Hexagon,
                  contentDescription = "Ver estadística hexagonal",
                  tint = TextSecondaryLight,
                  modifier = Modifier.size(18.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(SurfaceVariantLight)
            ) {
              Box(
                modifier = Modifier
                  .fillMaxWidth(dailyCapProgress)
                  .height(6.dp)
                  .clip(RoundedCornerShape(3.dp))
                  .background(if (gamificationStats.isDailyCapReached) Color(0xFF15803D) else AccentCharcoal)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          itemsIndexed(categories) { index, catName ->
            val isSelected = selectedCategoryIndex == index
            val chipInteraction = rememberBounceInteractionSource()
            val chipBg by animateColorAsState(
              targetValue = if (isSelected) AccentCharcoal else Color.White,
              animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
              label = "cat_bg_$index"
            )
            val chipTextColor by animateColorAsState(
              targetValue = if (isSelected) Color.White else TextPrimaryLight,
              label = "cat_text_$index"
            )

            Surface(
              onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                selectedCategoryIndex = index
              },
              interactionSource = chipInteraction,
              shape = RoundedCornerShape(14.dp),
              color = chipBg,
              border = BorderStroke(1.dp, if (isSelected) AccentCharcoal else BorderLight),
              modifier = Modifier
                .minimumInteractiveComponentSize()
                .bouncyScale(chipInteraction, scaleDown = 0.93f)
                .testTag("mission_category_$index")
            ) {
              Text(
                text = catName,
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                ),
                color = chipTextColor,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        when (selectedCategoryIndex) {
          7 -> if (filteredMissions.isEmpty()) {
            EmptyExploreNote("Aún no tienes misiones guardadas. Abre cualquier detalle y toca 'Guardar para más tarde'.")
          }
          8 -> if (filteredMissions.isEmpty()) {
            EmptyExploreNote("Crea tus propios pasos o hábitos con el botón 'Crear'.")
          }
        }
      }
    }

    items(filteredMissions, key = { it.id }) { mission ->
      CompactMissionCard(
        mission = mission,
        onContinue = { onViewMissionDetails(mission) }
      )
    }

    item(key = "bottom_space") {
      Spacer(modifier = Modifier.height(104.dp))
    }
  }
}

@Composable
private fun EmptyExploreNote(message: String) {
  Surface(
    shape = RoundedCornerShape(16.dp),
    color = SurfaceCardLight,
    border = BorderStroke(1.dp, BorderLight),
    modifier = Modifier.fillMaxWidth()
  ) {
    Text(
      text = message,
      style = MaterialTheme.typography.bodySmall,
      color = TextSecondaryLight,
      modifier = Modifier.padding(14.dp)
    )
  }
}

@Composable
internal fun CompactMissionCard(
  mission: BalanceMission,
  onContinue: () -> Unit
) {
  val dimension = DimensionsProvider.dimensions.getOrNull(mission.dimensionIndex)
  val accentColor = dimension?.accentColor ?: AccentCharcoal
  val cardInteraction = rememberBounceInteractionSource()
  val btnInteraction = rememberBounceInteractionSource()

  val cardBg by animateColorAsState(
    targetValue = if (mission.isCompleted) Color(0xFFF3F4F6) else SurfaceCardLight,
    animationSpec = tween(durationMillis = 260),
    label = "card_bg_${mission.id}"
  )
  val borderColor by animateColorAsState(
    targetValue = if (mission.isCompleted) Color(0xFFD1D5DB) else BorderLight,
    label = "card_border_${mission.id}"
  )

  Surface(
    onClick = onContinue,
    interactionSource = cardInteraction,
    shape = RoundedCornerShape(22.dp),
    color = cardBg,
    border = BorderStroke(1.dp, borderColor),
    shadowElevation = if (mission.isCompleted) 0.dp else 1.dp,
    modifier = Modifier
      .fillMaxWidth()
      .bouncyScale(cardInteraction, scaleDown = 0.97f)
      .testTag("mission_card_${mission.id}")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = if (mission.isCompleted) Color(0xFFE5E7EB) else accentColor.copy(alpha = 0.12f)
        ) {
          Text(
            text = mission.dimensionTitle.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = if (mission.isCompleted) Color(0xFF6B7280) else accentColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
          )
        }

        Row(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          if (mission.isHabit || mission.isCustom) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = SurfaceVariantLight
            ) {
              Text(
                text = if (mission.isHabit) "Hábito" else "Propia",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = TextSecondaryLight,
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
              )
            }
          }

          if (mission.isCompleted) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = Color(0xFFE5E7EB)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Filled.CheckCircle,
                  contentDescription = null,
                  tint = Color(0xFF4B5563),
                  modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "Completada",
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                  color = Color(0xFF4B5563)
                )
              }
            }
          } else {
            if (mission.isSavedForLater) {
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = SurfaceVariantLight
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(
                    imageVector = Icons.Default.Bookmark,
                    contentDescription = null,
                    tint = AccentCharcoal,
                    modifier = Modifier.size(12.dp)
                  )
                  Spacer(modifier = Modifier.width(3.dp))
                  Text(
                    text = "Guardada",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = AccentCharcoal
                  )
                }
              }
            }

            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (mission.hasMultiplier) Color(0xFFFEF3C7) else SurfaceVariantLight
            ) {
              Text(
                text = if (mission.hasMultiplier) "+${mission.expReward} EXP · 1.5x" else "+${mission.expReward} EXP",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = if (mission.hasMultiplier) Color(0xFF92400E) else TextPrimaryLight,
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = mission.title,
        style = MaterialTheme.typography.titleMedium.copy(
          fontWeight = FontWeight.Bold,
          fontSize = 16.5.sp
        ),
        color = if (mission.isCompleted) Color(0xFF6B7280) else TextPrimaryLight
      )

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = mission.summary.ifEmpty { mission.description },
        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 19.sp),
        color = if (mission.isCompleted) Color(0xFF9CA3AF) else TextSecondaryLight,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
      )

      Spacer(modifier = Modifier.height(10.dp))

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
            text = "${mission.durationLabel} · ${mission.difficulty}",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondaryLight
          )
        }

        Button(
          onClick = onContinue,
          interactionSource = btnInteraction,
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = if (mission.isCompleted) Color(0xFFE5E7EB) else AccentCharcoal,
            contentColor = if (mission.isCompleted) Color(0xFF4B5563) else Color.White
          ),
          contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
          modifier = Modifier
            .height(40.dp)
            .bouncyScale(btnInteraction, scaleDown = 0.92f)
            .testTag("btn_continue_mission_${mission.id}")
        ) {
          Text(
            text = "Ver detalles",
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
