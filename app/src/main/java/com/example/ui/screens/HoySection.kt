package com.example.ui.screens

import android.net.Uri
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
import androidx.compose.animation.slideInVertically
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.FormatQuote
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.SwapHoriz
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
import com.example.domain.CompletionRecord
import com.example.domain.HelpfulnessRating
import com.example.domain.HexagonCalculator
import com.example.model.AssessmentReport
import com.example.model.BalanceMission
import com.example.model.DimensionsProvider
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
 * Pantalla principal "Hoy": sencilla, serena y enfocada.
 * - Respeta las áreas que la persona eligió cuidar.
 * - Presenta hasta 3 acciones diarias concretas actualizadas cada día a las 00:00 hora local.
 * - Incluye cambio amable sin culpa y revisión rápida "¿Te sirvió esta acción?".
 * - Muestra el hexágono vivo de 2 capas sin métricas punitivas ni puntos EXP.
 */
@Composable
fun HoySection(
  userProfile: UserProfile,
  report: AssessmentReport,
  realScores: Map<Int, Int>,
  realScoresFloat: Map<Int, Float>,
  dailyMissions: List<BalanceMission>,
  todayCompletions: List<CompletionRecord>,
  selectedDimensionForDetail: Int?,
  onSelectDimension: (Int) -> Unit,
  onOpenFocusPicker: () -> Unit,
  onViewMissionDetails: (BalanceMission) -> Unit,
  onSwapDailyMission: (slotIndex: Int) -> Unit,
  onQuickRateCompletion: (mission: BalanceMission, rating: HelpfulnessRating) -> Unit,
  onNavigateToProgress: () -> Unit,
  liveHarmonyPercentage: Int = HexagonCalculator.calculateLiveHarmonyPercentage(realScores, report.scores),
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

  val todayCompletionMap = remember(todayCompletions) {
    todayCompletions.associateBy { it.missionId }
  }
  val completedTodayCount = remember(dailyMissions, todayCompletionMap) {
    dailyMissions.count { it.id in todayCompletionMap }
  }

  var phraseIndex by remember { mutableIntStateOf(0) }
  var phraseRefreshCount by remember { mutableIntStateOf(0) }
  val phrases = remember(report.wisdomQuote, report.wisdomAuthor, priorityDim.title) {
    listOf(
      CalmPhrase(
        quote = "Tu foco actual: ${priorityDim.title}. Un gesto breve hoy basta para empezar.",
        author = "Hoy"
      ),
      CalmPhrase(
        quote = report.wisdomQuote,
        author = report.wisdomAuthor
      ),
      CalmPhrase(
        quote = "Si una acción se siente pesada hoy, puedes cambiarla por otra más amable.",
        author = "Ritmo propio"
      ),
      CalmPhrase(
        quote = "Cuidarte no es acumular tareas; es elegir lo que de verdad te hace bien.",
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
      .padding(top = 12.dp, bottom = 104.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .widthIn(max = 600.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // 1. Encabezado sereno con saludo, racha amable y avatar
      HoySereneHeader(
        userProfile = userProfile,
        completedTodayCount = completedTodayCount,
        totalDailyCount = dailyMissions.size,
        onOpenProgress = {
          haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
          onNavigateToProgress()
        }
      )

      // 2. Selector rápido del enfoque elegido por la persona
      FocusSelectionBanner(
        chosenFocusDimensions = chosenFocus,
        suggestedDimensionTitle = priorityDim.title,
        accentColor = priorityDim.accentColor,
        onClick = {
          haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
          onOpenFocusPicker()
        }
      )

      // 3. Tus acciones personalizadas para hoy (refrescadas a las 00:00)
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "Tus acciones de hoy",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = TextPrimaryLight
            )
            Text(
              text = "Se renuevan cada día a las 00:00 · Sin presión",
              style = MaterialTheme.typography.labelSmall,
              color = TextSecondaryLight
            )
          }

          Surface(
            shape = RoundedCornerShape(12.dp),
            color = SurfaceVariantLight
          ) {
            Text(
              text = "$completedTodayCount/${dailyMissions.size}",
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                fontFeatureSettings = "tnum"
              ),
              color = TextPrimaryLight,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
            )
          }
        }

        dailyMissions.forEachIndexed { slotIndex, mission ->
          val completion = todayCompletionMap[mission.id]
          DailyActionCard(
            mission = mission,
            isCompletedToday = completion != null,
            savedRating = completion?.helpfulness,
            onViewDetails = {
              haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              onViewMissionDetails(mission)
            },
            onSwap = {
              haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              onSwapDailyMission(slotIndex)
            },
            onQuickRate = { rating ->
              haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              onQuickRateCompletion(mission, rating)
            }
          )
        }
      }

      // 4. Vista viva del hexágono de dos capas (tocar un vértice abre el detalle)
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
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "Tu mapa de equilibrio",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = TextPrimaryLight
              )
              Text(
                text = if (hasRealData) {
                  "Toca un vértice para ver cómo vas"
                } else {
                  "Tu hexágono real crece con tus pasos de esta semana"
                },
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondaryLight
              )
            }

            val progressInteraction = rememberBounceInteractionSource()
            Surface(
              onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onNavigateToProgress()
              },
              interactionSource = progressInteraction,
              shape = RoundedCornerShape(12.dp),
              color = SurfaceVariantLight,
              modifier = Modifier.bouncyScale(progressInteraction, scaleDown = 0.93f)
            ) {
              Text(
                text = "$animatedHarmony% · Ver más",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = AccentCharcoal,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          DualLayerHexagonRadarChart(
            perceivedScores = report.scores,
            realScores = realScoresFloat,
            selectedDimensionIndex = selectedDimensionForDetail,
            onDimensionSelect = { idx ->
              haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              onSelectDimension(idx)
            },
            chartSize = 200.dp,
            showLegend = true,
            showChips = false
          )
        }
      }

      // 5. Tira serena de pensamiento guía
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
    }
  }
}

@Composable
private fun HoySereneHeader(
  userProfile: UserProfile,
  completedTodayCount: Int,
  totalDailyCount: Int,
  onOpenProgress: () -> Unit
) {
  val avatarInteraction = rememberBounceInteractionSource()

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
        text = when {
          totalDailyCount > 0 && completedTodayCount == totalDailyCount ->
            "Completaste tus pasos de hoy. Descansa tranquilo."
          completedTodayCount > 0 ->
            "$completedTodayCount de $totalDailyCount acciones hechas hoy"
          else ->
            "Un paso pequeño y realista a la vez"
        },
        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
        color = TextSecondaryLight
      )
    }

    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = SurfaceCardLight,
        border = BorderStroke(1.dp, BorderLight)
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Outlined.Spa,
            contentDescription = "Días cuidándote",
            tint = if (userProfile.activeStreakDays > 0) Color(0xFF15803D) else TextTertiaryLight,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = if (userProfile.activeStreakDays > 0) "${userProfile.activeStreakDays}d" else "Hoy",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = TextPrimaryLight
          )
        }
      }

      Surface(
        onClick = onOpenProgress,
        interactionSource = avatarInteraction,
        shape = CircleShape,
        color = SurfaceVariantLight,
        border = BorderStroke(1.5.dp, BorderLight),
        modifier = Modifier
          .size(48.dp)
          .bouncyScale(avatarInteraction, scaleDown = 0.90f)
          .testTag("btn_hoy_avatar_progress")
      ) {
        Box(contentAlignment = Alignment.Center) {
          if (userProfile.photoUri != null) {
            AsyncImage(
              model = Uri.parse(userProfile.photoUri),
              contentDescription = "Ver tu progreso",
              contentScale = ContentScale.Crop,
              modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
            )
          } else {
            Icon(
              imageVector = Icons.Filled.Person,
              contentDescription = "Ver tu progreso",
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
private fun FocusSelectionBanner(
  chosenFocusDimensions: Set<Int>,
  suggestedDimensionTitle: String,
  accentColor: Color,
  onClick: () -> Unit
) {
  val interaction = rememberBounceInteractionSource()
  val focusLabel = if (chosenFocusDimensions.isNotEmpty()) {
    chosenFocusDimensions.sorted()
      .mapNotNull { DimensionsProvider.dimensions.getOrNull(it)?.title }
      .joinToString(" · ")
  } else {
    "Sugerido: $suggestedDimensionTitle"
  }

  Surface(
    onClick = onClick,
    interactionSource = interaction,
    shape = RoundedCornerShape(16.dp),
    color = SurfaceCardLight,
    border = BorderStroke(1.dp, accentColor.copy(alpha = 0.30f)),
    modifier = Modifier
      .fillMaxWidth()
      .minimumInteractiveComponentSize()
      .bouncyScale(interaction, scaleDown = 0.97f)
      .testTag("btn_hoy_choose_focus")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 11.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.weight(1f)
      ) {
        Box(
          modifier = Modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(accentColor.copy(alpha = 0.14f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Outlined.Explore,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(16.dp)
          )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text(
            text = "En qué área quieres avanzar",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondaryLight
          )
          Text(
            text = focusLabel,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = TextPrimaryLight,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }

      Spacer(modifier = Modifier.width(8.dp))

      Surface(
        shape = RoundedCornerShape(10.dp),
        color = SurfaceVariantLight
      ) {
        Text(
          text = "Elegir",
          style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
          color = AccentCharcoal,
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
      }
    }
  }
}

@Composable
private fun DailyActionCard(
  mission: BalanceMission,
  isCompletedToday: Boolean,
  savedRating: HelpfulnessRating?,
  onViewDetails: () -> Unit,
  onSwap: () -> Unit,
  onQuickRate: (HelpfulnessRating) -> Unit
) {
  val dim = DimensionsProvider.dimensions.getOrNull(mission.dimensionIndex)
  val accentColor = dim?.accentColor ?: AccentCharcoal
  val cardInteraction = rememberBounceInteractionSource()
  val ctaInteraction = rememberBounceInteractionSource()
  val swapInteraction = rememberBounceInteractionSource()

  val cardBg by animateColorAsState(
    targetValue = if (isCompletedToday) Color(0xFFF0FDF4) else SurfaceCardLight,
    animationSpec = tween(260),
    label = "daily_card_bg_${mission.id}"
  )
  val borderColor by animateColorAsState(
    targetValue = if (isCompletedToday) Color(0xFFBBF7D0) else BorderLight,
    animationSpec = tween(260),
    label = "daily_card_border_${mission.id}"
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
      .bouncyScale(cardInteraction, scaleDown = 0.97f)
      .testTag("daily_action_card_${mission.id}")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (isCompletedToday) Color(0xFFDCFCE7) else accentColor.copy(alpha = 0.12f)
          ) {
            Text(
              text = mission.dimensionTitle.uppercase(),
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = if (isCompletedToday) Color(0xFF166534) else accentColor,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Outlined.Schedule,
              contentDescription = null,
              tint = TextSecondaryLight,
              modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
              text = mission.durationLabel,
              style = MaterialTheme.typography.labelSmall,
              color = TextSecondaryLight
            )
          }
        }

        if (!isCompletedToday) {
          Surface(
            onClick = onSwap,
            interactionSource = swapInteraction,
            shape = RoundedCornerShape(10.dp),
            color = SurfaceVariantLight,
            modifier = Modifier
              .minimumInteractiveComponentSize()
              .bouncyScale(swapInteraction, scaleDown = 0.90f)
              .testTag("btn_swap_daily_${mission.id}")
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Outlined.SwapHoriz,
                contentDescription = "Cambiar acción",
                tint = TextSecondaryLight,
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "Cambiar",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                color = TextSecondaryLight
              )
            }
          }
        } else {
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFFDCFCE7)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF15803D),
                modifier = Modifier.size(13.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = savedRating?.shortLabel ?: "Hecha hoy",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF166534)
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = mission.title,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = TextPrimaryLight
      )
      Spacer(modifier = Modifier.height(3.dp))
      Text(
        text = mission.summary.ifEmpty { mission.description },
        style = MaterialTheme.typography.bodySmall,
        color = TextSecondaryLight,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        lineHeight = 18.sp
      )

      // Revisión rápida en tarjeta tras completar sin haber valorado todavía
      AnimatedVisibility(
        visible = isCompletedToday && savedRating == null,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
      ) {
        Column(modifier = Modifier.padding(top = 10.dp)) {
          Text(
            text = "¿Cómo te hizo sentir?",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFF166534)
          )
          Spacer(modifier = Modifier.height(6.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            HelpfulnessRating.entries.forEach { rating ->
              val rateInteraction = rememberBounceInteractionSource()
              Surface(
                onClick = { onQuickRate(rating) },
                interactionSource = rateInteraction,
                shape = RoundedCornerShape(10.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                modifier = Modifier
                  .weight(1f)
                  .minimumInteractiveComponentSize()
                  .bouncyScale(rateInteraction, scaleDown = 0.93f)
                  .testTag("quick_rate_${mission.id}_${rating.name.lowercase()}")
              ) {
                Box(
                  modifier = Modifier.padding(vertical = 8.dp),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = rating.shortLabel,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = TextPrimaryLight
                  )
                }
              }
            }
          }
        }
      }

      if (!isCompletedToday || savedRating != null) {
        Spacer(modifier = Modifier.height(10.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End
        ) {
          Button(
            onClick = onViewDetails,
            interactionSource = ctaInteraction,
            colors = ButtonDefaults.buttonColors(
              containerColor = if (isCompletedToday) Color(0xFFDCFCE7) else AccentCharcoal,
              contentColor = if (isCompletedToday) Color(0xFF166534) else Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
            modifier = Modifier
              .height(40.dp)
              .bouncyScale(ctaInteraction, scaleDown = 0.94f)
              .testTag("btn_daily_details_${mission.id}")
          ) {
            Text(
              text = if (isCompletedToday) "Revisar nota" else "Ver pasos",
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
