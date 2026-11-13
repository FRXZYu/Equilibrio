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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.example.domain.CompletionRecord
import com.example.domain.DayKey
import com.example.domain.HelpfulnessRating
import com.example.domain.HelpfulnessSummary
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Pantalla "Progreso": seguimiento amable y realista sin puntos ni rangos.
 * - Hexágono completo de 2 capas (Percibido vs Práctica real de 14 días).
 * - Selector libre de qué áreas elegir para cuidar ahora.
 * - Ritmo de los últimos 7 días + intención mensual editable.
 * - Lo que te ha ayudado + historial reciente con acceso a editar tu reflexión.
 */
@Composable
fun ProgresoSection(
  userProfile: UserProfile,
  report: AssessmentReport,
  realScoresFloat: Map<Int, Float>,
  actionsByDimension14d: Map<Int, Int>,
  allCompletions: List<CompletionRecord>,
  helpfulnessSummary: HelpfulnessSummary,
  allMissions: List<BalanceMission>,
  dayKey: DayKey,
  currentDayKey: String = dayKey.today(),
  selectedDimensionForDetail: Int?,
  onSelectDimension: (Int) -> Unit,
  onToggleFocusDimension: (Int) -> Unit,
  onEditGoal: () -> Unit,
  onOpenFullReport: () -> Unit,
  onReviewCompletion: (CompletionRecord) -> Unit,
  modifier: Modifier = Modifier
) {
  val haptic = LocalHapticFeedback.current
  val dimensions = DimensionsProvider.dimensions
  val chosenFocus = userProfile.chosenFocusDimensions
  val missionById = remember(allMissions) { allMissions.associateBy { it.id } }

  val last7Days = remember(dayKey, currentDayKey, allCompletions.size) { dayKey.getRangeLastDays(7) }
  val activeDatesSet = remember(allCompletions) { allCompletions.map { it.date }.toSet() }
  val todayStr = remember(currentDayKey) { currentDayKey }
  val yesterdayStr = remember(dayKey, currentDayKey) { dayKey.yesterday() }

  val topHelpfulDimTitle = remember(helpfulnessSummary) {
    helpfulnessSummary.helpfulByDimension.maxByOrNull { it.value }
      ?.takeIf { it.value > 0 }
      ?.let { dimensions.getOrNull(it.key)?.title }
  }

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
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // 1. Encabezado + acceso a lectura inicial
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Tu Progreso",
            style = MaterialTheme.typography.headlineMedium.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 24.sp
            ),
            color = TextPrimaryLight
          )
          Text(
            text = "Tu proceso real, sin prisas ni comparaciones",
            style = MaterialTheme.typography.bodySmall,
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
          color = SurfaceCardLight,
          border = BorderStroke(1.dp, BorderLight),
          modifier = Modifier
            .minimumInteractiveComponentSize()
            .bouncyScale(reportInteraction, scaleDown = 0.94f)
            .testTag("btn_progress_open_report")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Outlined.Insights,
              contentDescription = null,
              tint = AccentCharcoal,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Lectura inicial",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
              color = TextPrimaryLight
            )
          }
        }
      }

      // 2. Hexágono completo de dos capas
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
            selectedDimensionIndex = selectedDimensionForDetail,
            onDimensionSelect = { idx ->
              haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              onSelectDimension(idx)
            },
            chartSize = 240.dp,
            showLegend = true,
            showChips = true
          )
        }
      }

      // 3. Áreas que eliges cuidar + práctica real de los últimos 14 días
      Surface(
        shape = RoundedCornerShape(24.dp),
        color = SurfaceCardLight,
        border = BorderStroke(1.dp, BorderLight),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Outlined.Explore,
              contentDescription = null,
              tint = AccentCharcoal,
              modifier = Modifier.size(19.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                text = "Áreas que eliges cuidar",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimaryLight
              )
              Text(
                text = "Elige en cuáles quieres enfocarte estos días (referencia: ${HexagonCalculator.DEFAULT_OBJETIVO_ACCIONES} acciones en 14 días)",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondaryLight
              )
            }
          }

          dimensions.forEach { dim ->
            val actionsCount = actionsByDimension14d[dim.index] ?: 0
            val progress = (actionsCount.toFloat() / HexagonCalculator.DEFAULT_OBJETIVO_ACCIONES.toFloat()).coerceIn(0f, 1f)
            val animatedProgress by animateFloatAsState(
              targetValue = progress,
              animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
              label = "dim_progress_${dim.index}"
            )
            val isFocus = dim.index in chosenFocus
            val focusInteraction = rememberBounceInteractionSource()
            val chipBg by animateColorAsState(
              targetValue = if (isFocus) AccentCharcoal else SurfaceVariantLight,
              animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
              label = "progress_focus_bg_${dim.index}"
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
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
                      .size(10.dp)
                      .clip(CircleShape)
                      .background(dim.accentColor)
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Column {
                    Text(
                      text = dim.title,
                      style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                      color = TextPrimaryLight
                    )
                    Text(
                      text = "$actionsCount ${if (actionsCount == 1) "acción" else "acciones"} en 14 días",
                      style = MaterialTheme.typography.labelSmall,
                      color = TextSecondaryLight
                    )
                  }
                }

                Surface(
                  onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onToggleFocusDimension(dim.index)
                  },
                  interactionSource = focusInteraction,
                  shape = RoundedCornerShape(12.dp),
                  color = chipBg,
                  modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .bouncyScale(focusInteraction, scaleDown = 0.93f)
                    .testTag("toggle_focus_dim_${dim.index}")
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    if (isFocus) {
                      Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(13.dp)
                      )
                      Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                      text = if (isFocus) "Enfocada" else "Enfocar",
                      style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                      color = if (isFocus) Color.White else TextPrimaryLight
                    )
                  }
                }
              }

              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .height(6.dp)
                  .clip(RoundedCornerShape(3.dp))
                  .background(SurfaceVariantLight)
              ) {
                Box(
                  modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(dim.accentColor)
                )
              }
            }
          }
        }
      }

      // 4. Ritmo de los últimos 7 días + intención mensual
      Surface(
        shape = RoundedCornerShape(24.dp),
        color = SurfaceCardLight,
        border = BorderStroke(1.dp, BorderLight),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          Text(
            text = "Ritmo de esta semana",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = TextPrimaryLight
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            last7Days.forEach { dateKey ->
              val isActive = dateKey in activeDatesSet
              val isToday = dateKey == todayStr
              Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(5.dp)
              ) {
                Box(
                  modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(
                      when {
                        isActive -> Color(0xFFDCFCE7)
                        isToday -> SurfaceVariantLight
                        else -> Color(0xFFF3F4F6)
                      }
                    ),
                  contentAlignment = Alignment.Center
                ) {
                  if (isActive) {
                    Icon(
                      imageVector = Icons.Filled.Check,
                      contentDescription = "Día con cuidado",
                      tint = Color(0xFF15803D),
                      modifier = Modifier.size(16.dp)
                    )
                  } else {
                    Box(
                      modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (isToday) AccentCharcoal else TextTertiaryLight.copy(alpha = 0.5f))
                    )
                  }
                }
                Text(
                  text = shortDayLetter(dateKey),
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium
                  ),
                  color = if (isToday) TextPrimaryLight else TextSecondaryLight
                )
              }
            }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            CalmStatBox(
              label = "Días seguidos",
              value = "${userProfile.activeStreakDays}",
              modifier = Modifier.weight(1f)
            )
            CalmStatBox(
              label = "Días activos (14d)",
              value = "${userProfile.activeDaysRecent}",
              modifier = Modifier.weight(1f)
            )
            CalmStatBox(
              label = "Te ayudaron",
              value = "${helpfulnessSummary.helpfulCount}",
              modifier = Modifier.weight(1f)
            )
          }

          // Intención mensual editable
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = SurfaceVariantLight,
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
              ) {
                Icon(
                  imageVector = Icons.Outlined.Flag,
                  contentDescription = null,
                  tint = AccentCharcoal,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                  Text(
                    text = "TU INTENCIÓN DEL MES",
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontWeight = FontWeight.Bold,
                      letterSpacing = 0.5.sp
                    ),
                    color = TextSecondaryLight
                  )
                  Text(
                    text = userProfile.monthlyGoal,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = TextPrimaryLight
                  )
                }
              }

              val editInteraction = rememberBounceInteractionSource()
              Surface(
                onClick = {
                  haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                  onEditGoal()
                },
                interactionSource = editInteraction,
                shape = RoundedCornerShape(10.dp),
                color = Color.White,
                border = BorderStroke(1.dp, BorderLight),
                modifier = Modifier
                  .minimumInteractiveComponentSize()
                  .bouncyScale(editInteraction, scaleDown = 0.92f)
                  .testTag("btn_edit_monthly_goal")
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "Editar intención",
                    tint = AccentCharcoal,
                    modifier = Modifier.size(14.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "Editar",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = AccentCharcoal
                  )
                }
              }
            }
          }
        }
      }

      // 5. Lo que te ha ayudado + historial reciente con notas
      Surface(
        shape = RoundedCornerShape(24.dp),
        color = SurfaceCardLight,
        border = BorderStroke(1.dp, BorderLight),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Outlined.History,
              contentDescription = null,
              tint = AccentCharcoal,
              modifier = Modifier.size(19.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Lo que te ha servido e historial",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = TextPrimaryLight
            )
          }

          if (helpfulnessSummary.totalRated > 0) {
            Surface(
              shape = RoundedCornerShape(14.dp),
              color = Color(0xFFF0FDF4),
              border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Outlined.ThumbUp,
                  contentDescription = null,
                  tint = Color(0xFF15803D),
                  modifier = Modifier.size(17.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = buildString {
                    append("${helpfulnessSummary.helpfulCount} veces sentiste que te ayudó")
                    if (topHelpfulDimTitle != null) {
                      append(", especialmente en $topHelpfulDimTitle")
                    }
                    append(".")
                  },
                  style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                  color = Color(0xFF166534)
                )
              }
            }
          } else {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(SurfaceVariantLight)
                .padding(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Outlined.AutoAwesome,
                contentDescription = null,
                tint = TextSecondaryLight,
                modifier = Modifier.size(17.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Cuando completes una acción y anotes si te sirvió, aquí verás qué funciona mejor para ti.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondaryLight
              )
            }
          }

          val recentList = remember(allCompletions) { allCompletions.take(10) }
          if (recentList.isEmpty()) {
            Text(
              text = "Aún no hay registros guardados. Empieza con una acción breve en Hoy.",
              style = MaterialTheme.typography.bodySmall,
              color = TextSecondaryLight
            )
          } else {
            recentList.forEach { completion ->
              val mission = missionById[completion.missionId]
              val dim = dimensions.getOrNull(completion.dimensionIndex)
              val dateLabel = when (completion.date) {
                todayStr -> "Hoy"
                yesterdayStr -> "Ayer"
                else -> completion.date
              }
              val itemInteraction = rememberBounceInteractionSource()

              Surface(
                onClick = {
                  haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                  onReviewCompletion(completion)
                },
                interactionSource = itemInteraction,
                shape = RoundedCornerShape(16.dp),
                color = SurfaceVariantLight.copy(alpha = 0.65f),
                modifier = Modifier
                  .fillMaxWidth()
                  .bouncyScale(itemInteraction, scaleDown = 0.97f)
                  .testTag("history_item_${completion.missionId}_${completion.date}")
              ) {
                Column(modifier = Modifier.padding(12.dp)) {
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
                          .size(8.dp)
                          .clip(CircleShape)
                          .background(dim?.accentColor ?: AccentCharcoal)
                      )
                      Spacer(modifier = Modifier.width(6.dp))
                      Text(
                        text = mission?.title ?: (dim?.title ?: "Acción de cuidado"),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimaryLight,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                      )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                      text = dateLabel,
                      style = MaterialTheme.typography.labelSmall,
                      color = TextSecondaryLight
                    )
                  }

                  Spacer(modifier = Modifier.height(5.dp))

                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(
                      text = dim?.title ?: "",
                      style = MaterialTheme.typography.labelSmall,
                      color = TextSecondaryLight
                    )

                    val ratingColor = when (completion.helpfulness) {
                      HelpfulnessRating.HELPFUL -> Color(0xFF15803D)
                      HelpfulnessRating.NEUTRAL -> AccentCharcoal
                      HelpfulnessRating.HEAVY -> Color(0xFFB45309)
                      null -> TextTertiaryLight
                    }
                    Text(
                      text = completion.helpfulness?.label ?: "Tocar para valorar",
                      style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                      color = ratingColor
                    )
                  }

                  if (completion.reflectionNote.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                      text = "“${completion.reflectionNote}”",
                      style = MaterialTheme.typography.bodySmall,
                      color = TextPrimaryLight
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
}

@Composable
private fun CalmStatBox(
  label: String,
  value: String,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(14.dp),
    color = SurfaceVariantLight,
    modifier = modifier
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = value,
        style = MaterialTheme.typography.titleLarge.copy(
          fontWeight = FontWeight.Bold,
          fontFeatureSettings = "tnum"
        ),
        color = TextPrimaryLight
      )
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = TextSecondaryLight,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}

private fun shortDayLetter(dateKey: String): String {
  return try {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val date = sdf.parse(dateKey) ?: return ""
    val cal = Calendar.getInstance()
    cal.time = date
    when (cal.get(Calendar.DAY_OF_WEEK)) {
      Calendar.MONDAY -> "L"
      Calendar.TUESDAY -> "M"
      Calendar.WEDNESDAY -> "X"
      Calendar.THURSDAY -> "J"
      Calendar.FRIDAY -> "V"
      Calendar.SATURDAY -> "S"
      Calendar.SUNDAY -> "D"
      else -> ""
    }
  } catch (_: Exception) {
    ""
  }
}
