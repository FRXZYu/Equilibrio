package com.example.ui.screens

import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.FormatQuote
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Schedule
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.domain.HexagonCalculator
import com.example.model.AssessmentReport
import com.example.model.BalanceMission
import com.example.model.DimensionsProvider
import com.example.model.GamificationStats
import com.example.model.UserProfile
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

private data class CalmPhrase(
  val quote: String,
  val author: String
)

/**
 * Pantalla principal "Hoy" (tab central):
 * Diseño minimalista, sin scroll largo, guiado por frases inspiradoras,
 * hexágono dinámico de dos capas interactivo y mazo compacto de misión prioritaria.
 */
@Suppress("UNUSED_PARAMETER")
@Composable
fun HoySection(
  userProfile: UserProfile,
  report: AssessmentReport,
  realScores: Map<Int, Int>,
  realScoresFloat: Map<Int, Float>,
  todayCompletedIds: Set<String>,
  allMissions: List<BalanceMission>,
  selectedDimensionForDetail: Int?,
  onSelectDimension: (Int) -> Unit,
  onToggleCompleteMission: (BalanceMission) -> Unit,
  onViewMissionDetails: (BalanceMission) -> Unit,
  liveHarmonyPercentage: Int,
  onOpenFullReport: () -> Unit,
  onEditGoal: () -> Unit,
  modifier: Modifier = Modifier
) {
  val haptic = LocalHapticFeedback.current
  val dimensions = DimensionsProvider.dimensions
  val chosenFocus = userProfile.chosenFocusDimensions

  val priorityDimIndex = remember(realScores, report.scores, chosenFocus) {
    HexagonCalculator.findPriorityDimension(realScores, report.scores, chosenFocus)
  }
  val priorityDim = dimensions.getOrNull(priorityDimIndex) ?: dimensions[0]
  val hasRealData = remember(realScoresFloat) { realScoresFloat.values.any { it > 0.05f } }

  val animatedHarmony by animateIntAsState(
    targetValue = liveHarmonyPercentage,
    animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
    label = "hoy_animated_harmony"
  )

  // Mazo compacto de hasta 4 acciones sugeridas para hoy (evita scroll infinito)
  val spotlightMissions = remember(allMissions, priorityDimIndex, todayCompletedIds) {
    val prioritized = allMissions.sortedWith(
      compareBy<BalanceMission> { it.isCompleted }
        .thenByDescending { it.dimensionIndex == priorityDimIndex }
        .thenByDescending { it.hasMultiplier }
        .thenByDescending { it.isSavedForLater }
    )
    prioritized.take(4)
  }

  var deckIndex by remember { mutableIntStateOf(0) }
  var slideDirection by remember { mutableIntStateOf(1) }
  val safeDeckIndex = if (spotlightMissions.isNotEmpty()) {
    deckIndex.coerceIn(0, spotlightMissions.lastIndex)
  } else 0
  val activeMission = spotlightMissions.getOrNull(safeDeckIndex)

  val completedTodayCount = remember(allMissions, todayCompletedIds) {
    allMissions.count { todayCompletedIds.contains(it.id) }
  }

  var phraseIndex by remember { mutableIntStateOf(0) }
  var phraseRefreshCount by remember { mutableIntStateOf(0) }
  val phrases = remember(report.wisdomQuote, report.wisdomAuthor, priorityDim.title, userProfile.monthlyGoal) {
    listOf(
      CalmPhrase(
        quote = "Foco sugerido hoy: ${priorityDim.title}. Un paso breve suma a tu hexágono.",
        author = "Tu equilibrio hoy"
      ),
      CalmPhrase(
        quote = report.wisdomQuote,
        author = report.wisdomAuthor
      ),
      CalmPhrase(
        quote = userProfile.monthlyGoal.ifBlank { "Paso a paso se construye la armonía diaria." },
        author = "Tu meta del mes"
      ),
      CalmPhrase(
        quote = "La constancia tranquila vence a cualquier esfuerzo extremo de un solo día.",
        author = "Equilibrio"
      )
    )
  }
  val currentPhrase = phrases[phraseIndex % phrases.size]

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 20.dp)
      .padding(top = 10.dp, bottom = 104.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .widthIn(max = 600.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      // 1. Encabezado compacto con saludo, racha/EXP y avatar (tocar edita meta)
      HoyCompactHeader(
        userProfile = userProfile,
        completedTodayCount = completedTodayCount,
        onEditGoal = {
          haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
          onEditGoal()
        },
        onOpenReport = {
          haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
          onOpenFullReport()
        }
      )

      // 2. Frase guía interactiva (sin recargar la pantalla)
      CalmPhraseBanner(
        phrase = currentPhrase,
        accentColor = priorityDim.accentColor,
        refreshSpinCount = phraseRefreshCount,
        onNextPhrase = {
          haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
          phraseIndex = (phraseIndex + 1) % phrases.size
          phraseRefreshCount++
        }
      )

      // 3. Tarjeta del Hexágono Dinámico de 2 capas (vértices táctiles -> DimensionDetailDialog)
      Surface(
        shape = RoundedCornerShape(24.dp),
        color = SurfaceCardLight,
        border = BorderStroke(1.dp, BorderLight),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Tu Hexágono en Vivo",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = TextPrimaryLight
              )
              Text(
                text = if (hasRealData) {
                  "Toca un vértice para ver detalle real vs base"
                } else {
                  "Completa misiones para expandir tu huella real"
                },
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondaryLight
              )
            }

            val reportInteraction = rememberBounceInteractionSource()
            Surface(
              onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onOpenFullReport()
              },
              interactionSource = reportInteraction,
              shape = RoundedCornerShape(12.dp),
              color = SurfaceVariantLight,
              modifier = Modifier
                .minimumInteractiveComponentSize()
                .bouncyScale(reportInteraction, scaleDown = 0.93f)
                .testTag("btn_hoy_open_report")
            ) {
              Text(
                text = "$animatedHarmony% · Reporte",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  fontFeatureSettings = "tnum"
                ),
                color = AccentCharcoal,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(6.dp))

          DualLayerHexagonRadarChart(
            perceivedScores = report.scores,
            realScores = realScoresFloat,
            selectedDimensionIndex = selectedDimensionForDetail,
            onDimensionSelect = { idx ->
              haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              onSelectDimension(idx)
            },
            chartSize = 204.dp,
            showLegend = true,
            showChips = false
          )
        }
      }

      // 4. Mazo interactivo de Misión del Momento (1 tarjeta a la vez, cero scroll largo)
      if (activeMission != null) {
        val isCompleted = todayCompletedIds.contains(activeMission.id)
        HoySpotlightDeckCard(
          mission = activeMission,
          isCompletedToday = isCompleted,
          positionIndex = safeDeckIndex,
          totalCount = spotlightMissions.size,
          slideDirection = slideDirection,
          onPrevious = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            slideDirection = -1
            deckIndex = if (safeDeckIndex - 1 < 0) spotlightMissions.lastIndex else safeDeckIndex - 1
          },
          onNext = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            slideDirection = 1
            deckIndex = (safeDeckIndex + 1) % spotlightMissions.size
          },
          onViewDetails = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onViewMissionDetails(activeMission)
          }
        )
      }
    }
  }
}

@Composable
private fun HoyCompactHeader(
  userProfile: UserProfile,
  completedTodayCount: Int,
  onEditGoal: () -> Unit,
  onOpenReport: () -> Unit
) {
  val avatarInteraction = rememberBounceInteractionSource()
  val statsInteraction = rememberBounceInteractionSource()

  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = if (userProfile.name.isNotBlank()) "Hola, ${userProfile.name}" else "Hola",
        style = MaterialTheme.typography.headlineMedium.copy(
          fontWeight = FontWeight.Bold,
          fontSize = 24.sp,
          letterSpacing = (-0.5).sp
        ),
        color = TextPrimaryLight,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = if (completedTodayCount > 0) {
          "$completedTodayCount ${if (completedTodayCount == 1) "misión" else "misiones"} completadas hoy · Nv. ${userProfile.level}"
        } else {
          "Nivel ${userProfile.level} (${userProfile.rank}) · Un paso a la vez"
        },
        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
        color = TextSecondaryLight,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }

    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Surface(
        onClick = onOpenReport,
        interactionSource = statsInteraction,
        shape = RoundedCornerShape(14.dp),
        color = SurfaceCardLight,
        border = BorderStroke(1.dp, BorderLight),
        modifier = Modifier
          .minimumInteractiveComponentSize()
          .bouncyScale(statsInteraction, scaleDown = 0.93f)
          .testTag("btn_hoy_streak_exp")
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Icon(
            imageVector = Icons.Outlined.LocalFireDepartment,
            contentDescription = "Racha",
            tint = if (userProfile.activeStreakDays > 0) Color(0xFFD97706) else TextTertiaryLight,
            modifier = Modifier.size(16.dp)
          )
          Text(
            text = "${userProfile.activeStreakDays}d",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = TextPrimaryLight
          )
          Box(
            modifier = Modifier
              .width(1.dp)
              .height(12.dp)
              .background(BorderLight)
          )
          Icon(
            imageVector = Icons.Default.Bolt,
            contentDescription = "EXP de hoy",
            tint = AccentCharcoal,
            modifier = Modifier.size(15.dp)
          )
          Text(
            text = "${userProfile.expToday}/${GamificationStats.DAILY_EXP_CAP}",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              fontFeatureSettings = "tnum"
            ),
            color = TextSecondaryLight
          )
        }
      }

      Surface(
        onClick = onEditGoal,
        interactionSource = avatarInteraction,
        shape = CircleShape,
        color = SurfaceVariantLight,
        border = BorderStroke(1.5.dp, BorderLight),
        modifier = Modifier
          .size(48.dp)
          .bouncyScale(avatarInteraction, scaleDown = 0.90f)
          .testTag("btn_hoy_avatar_goal")
      ) {
        Box(contentAlignment = Alignment.Center) {
          if (userProfile.photoUri != null) {
            AsyncImage(
              model = Uri.parse(userProfile.photoUri),
              contentDescription = "Editar meta mensual",
              contentScale = ContentScale.Crop,
              modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
            )
          } else {
            Icon(
              imageVector = Icons.Filled.Person,
              contentDescription = "Editar meta mensual",
              tint = AccentCharcoal,
              modifier = Modifier.size(22.dp)
            )
          }
        }
      }
    }
  }
}

@Composable
private fun HoySpotlightDeckCard(
  mission: BalanceMission,
  isCompletedToday: Boolean,
  positionIndex: Int,
  totalCount: Int,
  slideDirection: Int,
  onPrevious: () -> Unit,
  onNext: () -> Unit,
  onViewDetails: () -> Unit
) {
  val dim = DimensionsProvider.dimensions.getOrNull(mission.dimensionIndex)
  val accentColor = dim?.accentColor ?: AccentCharcoal
  val cardInteraction = rememberBounceInteractionSource()
  val btnInteraction = rememberBounceInteractionSource()
  val prevInteraction = rememberBounceInteractionSource()
  val nextInteraction = rememberBounceInteractionSource()

  val cardBg by animateColorAsState(
    targetValue = if (isCompletedToday) Color(0xFFF3F4F6) else SurfaceCardLight,
    animationSpec = tween(250),
    label = "hoy_spotlight_bg"
  )
  val borderColor by animateColorAsState(
    targetValue = if (isCompletedToday) Color(0xFFD1D5DB) else BorderLight,
    animationSpec = tween(250),
    label = "hoy_spotlight_border"
  )

  Surface(
    onClick = onViewDetails,
    interactionSource = cardInteraction,
    shape = RoundedCornerShape(22.dp),
    color = cardBg,
    border = BorderStroke(1.dp, borderColor),
    shadowElevation = if (isCompletedToday) 0.dp else 1.dp,
    modifier = Modifier
      .fillMaxWidth()
      .bouncyScale(cardInteraction, scaleDown = 0.98f)
      .testTag("hoy_spotlight_card_${mission.id}")
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
        Text(
          text = "FOCO SUGERIDO",
          style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp
          ),
          color = TextSecondaryLight
        )

        if (totalCount > 1) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
              onClick = onPrevious,
              interactionSource = prevInteraction,
              shape = CircleShape,
              color = SurfaceVariantLight,
              modifier = Modifier
                .minimumInteractiveComponentSize()
                .size(34.dp)
                .bouncyScale(prevInteraction, scaleDown = 0.88f)
                .testTag("btn_hoy_mission_prev")
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                  contentDescription = "Misión anterior",
                  tint = TextPrimaryLight,
                  modifier = Modifier.size(18.dp)
                )
              }
            }

            Text(
              text = "${positionIndex + 1}/$totalCount",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontFeatureSettings = "tnum"
              ),
              color = TextSecondaryLight,
              modifier = Modifier.padding(horizontal = 6.dp)
            )

            Surface(
              onClick = onNext,
              interactionSource = nextInteraction,
              shape = CircleShape,
              color = SurfaceVariantLight,
              modifier = Modifier
                .minimumInteractiveComponentSize()
                .size(34.dp)
                .bouncyScale(nextInteraction, scaleDown = 0.88f)
                .testTag("btn_hoy_mission_next")
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                  contentDescription = "Siguiente misión",
                  tint = TextPrimaryLight,
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      AnimatedContent(
        targetState = mission,
        transitionSpec = {
          val offset = if (slideDirection >= 0) 40 else -40
          (fadeIn(tween(220)) + slideInHorizontally(tween(220)) { offset }) togetherWith
            (fadeOut(tween(140)) + slideOutHorizontally(tween(140)) { -offset / 2 })
        },
        label = "hoy_spotlight_mission_slide"
      ) { item ->
        Column {
          Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (isCompletedToday) Color(0xFFE5E7EB) else accentColor.copy(alpha = 0.12f)
            ) {
              Text(
                text = item.dimensionTitle.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = if (isCompletedToday) Color(0xFF6B7280) else accentColor,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
              )
            }

            if (isCompletedToday) {
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
                  Spacer(modifier = Modifier.width(3.dp))
                  Text(
                    text = "Completada",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF4B5563)
                  )
                }
              }
            } else {
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (item.hasMultiplier) Color(0xFFFEF3C7) else SurfaceVariantLight
              ) {
                Text(
                  text = if (item.hasMultiplier) "+${item.expReward} EXP · 1.5x" else "+${item.expReward} EXP",
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                  color = if (item.hasMultiplier) Color(0xFF92400E) else TextPrimaryLight,
                  modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = item.title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = if (isCompletedToday) Color(0xFF6B7280) else TextPrimaryLight
          )

          Spacer(modifier = Modifier.height(3.dp))

          Text(
            text = item.summary.ifEmpty { item.description },
            style = MaterialTheme.typography.bodySmall,
            color = if (isCompletedToday) Color(0xFF9CA3AF) else TextSecondaryLight,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 18.sp
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Outlined.Schedule,
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
          onClick = onViewDetails,
          interactionSource = btnInteraction,
          colors = ButtonDefaults.buttonColors(
            containerColor = if (isCompletedToday) Color(0xFFE5E7EB) else AccentCharcoal,
            contentColor = if (isCompletedToday) Color(0xFF4B5563) else Color.White
          ),
          shape = RoundedCornerShape(12.dp),
          contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
          modifier = Modifier
            .height(40.dp)
            .bouncyScale(btnInteraction, scaleDown = 0.94f)
            .testTag("btn_hoy_details_${mission.id}")
        ) {
          Text(
            text = "Ver detalles",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
          )
          Spacer(modifier = Modifier.width(5.dp))
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            modifier = Modifier.size(15.dp)
          )
        }
      }
    }
  }
}

@Composable
private fun CalmPhraseBanner(
  phrase: CalmPhrase,
  accentColor: Color,
  refreshSpinCount: Int,
  onNextPhrase: () -> Unit
) {
  val bannerInteraction = rememberBounceInteractionSource()
  val rotation by animateFloatAsState(
    targetValue = refreshSpinCount * 180f,
    animationSpec = spring(
      dampingRatio = Spring.DampingRatioMediumBouncy,
      stiffness = Spring.StiffnessLow
    ),
    label = "phrase_refresh_spin"
  )

  Surface(
    onClick = onNextPhrase,
    interactionSource = bannerInteraction,
    shape = RoundedCornerShape(18.dp),
    color = SurfaceCardLight,
    border = BorderStroke(1.dp, BorderLight),
    modifier = Modifier
      .fillMaxWidth()
      .bouncyScale(bannerInteraction, scaleDown = 0.97f)
      .testTag("btn_hoy_next_phrase")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(start = 14.dp, end = 8.dp, top = 10.dp, bottom = 10.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Surface(
        shape = CircleShape,
        color = accentColor.copy(alpha = 0.12f),
        modifier = Modifier.size(30.dp)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = Icons.Outlined.FormatQuote,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(16.dp)
          )
        }
      }

      Spacer(modifier = Modifier.width(10.dp))

      AnimatedContent(
        targetState = phrase,
        transitionSpec = {
          (fadeIn(tween(260, easing = FastOutSlowInEasing)) +
            slideInVertically(tween(260)) { it / 3 }) togetherWith
            (fadeOut(tween(170, easing = FastOutSlowInEasing)) +
              slideOutVertically(tween(170)) { -it / 3 })
        },
        modifier = Modifier.weight(1f),
        label = "hoy_phrase_cycle"
      ) { item ->
        Column {
          Text(
            text = item.quote,
            style = MaterialTheme.typography.bodySmall.copy(
              fontWeight = FontWeight.Medium,
              fontStyle = FontStyle.Italic,
              lineHeight = 18.sp
            ),
            color = TextPrimaryLight
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "${item.author} · Toca para leer otra",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = TextTertiaryLight
          )
        }
      }

      Box(
        modifier = Modifier
          .minimumInteractiveComponentSize()
          .size(36.dp),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Filled.Refresh,
          contentDescription = "Siguiente frase",
          tint = TextSecondaryLight,
          modifier = Modifier
            .size(16.dp)
            .graphicsLayer { rotationZ = rotation }
        )
      }
    }
  }
}
