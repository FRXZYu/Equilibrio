package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DimensionsProvider
import com.example.model.HexagonViewMode
import com.example.model.LifeDimension
import com.example.ui.theme.AccentCharcoal
import com.example.ui.theme.BorderLight
import com.example.ui.theme.SurfaceCardLight
import com.example.ui.theme.SurfaceVariantLight
import com.example.ui.theme.TextPrimaryLight
import com.example.ui.theme.TextSecondaryLight
import com.example.ui.theme.TextTertiaryLight
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

/**
 * Compact Hexagon Widget displayed in the top-right corner of the questionnaire.
 * Smoothly fills up wedge-by-wedge / axis-by-axis as questions are answered.
 */
@Composable
fun CompactHexagonProgress(
  scores: Map<Int, Int>,
  currentQuestionIndex: Int,
  modifier: Modifier = Modifier,
  sizeDp: Dp = 68.dp,
  onClick: (() -> Unit)? = null
) {
  val animatedValues = (0..5).map { index ->
    val rawScore = scores[index]
    val targetVal = if (rawScore != null) {
      (rawScore / 10f).coerceIn(0.12f, 1f)
    } else if (index == currentQuestionIndex) {
      0.15f
    } else {
      0.06f
    }
    animateFloatAsState(
      targetValue = targetVal,
      animationSpec = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMediumLow
      ),
      label = "compact_hex_$index"
    ).value
  }

  Box(
    modifier = modifier
      .size(sizeDp)
      .clip(RoundedCornerShape(16.dp))
      .background(SurfaceCardLight)
      .border(1.dp, BorderLight, RoundedCornerShape(16.dp))
      .clickable(enabled = onClick != null) { onClick?.invoke() }
      .padding(8.dp),
    contentAlignment = Alignment.Center
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val center = Offset(size.width / 2f, size.height / 2f)
      val maxRadius = (size.minDimension / 2f) * 0.90f

      // Background grid hexagon
      drawHexagonPolygon(
        center = center,
        radius = maxRadius,
        strokeColor = BorderLight.copy(alpha = 0.8f),
        strokeWidth = 1.2.dp.toPx()
      )

      // Radial spokes
      for (i in 0..5) {
        val angle = -PI / 2 + i * (PI / 3)
        val end = Offset(
          (center.x + maxRadius * cos(angle)).toFloat(),
          (center.y + maxRadius * sin(angle)).toFloat()
        )
        drawLine(
          color = BorderLight.copy(alpha = 0.6f),
          start = center,
          end = end,
          strokeWidth = 1.dp.toPx()
        )
      }

      // Filled active polygon
      val path = Path()
      val points = (0..5).map { i ->
        val r = maxRadius * animatedValues[i]
        val angle = -PI / 2 + i * (PI / 3)
        Offset(
          (center.x + r * cos(angle)).toFloat(),
          (center.y + r * sin(angle)).toFloat()
        )
      }

      path.moveTo(points[0].x, points[0].y)
      for (i in 1 until points.size) {
        path.lineTo(points[i].x, points[i].y)
      }
      path.close()

      // Gradient fill
      drawPath(
        path = path,
        brush = Brush.radialGradient(
          colors = listOf(
            AccentCharcoal.copy(alpha = 0.35f),
            AccentCharcoal.copy(alpha = 0.15f)
          ),
          center = center,
          radius = maxRadius
        ),
        style = Fill
      )

      // Border stroke
      drawPath(
        path = path,
        color = AccentCharcoal,
        style = Stroke(
          width = 2.dp.toPx(),
          join = StrokeJoin.Round,
          cap = StrokeCap.Round
        )
      )

      // Vertex dots for answered questions
      points.forEachIndexed { i, pt ->
        val isAnswered = scores.containsKey(i)
        val color = DimensionsProvider.dimensions[i].accentColor
        drawCircle(
          color = if (isAnswered) color else BorderLight,
          radius = if (isAnswered) 2.5.dp.toPx() else 1.5.dp.toPx(),
          center = pt
        )
      }
    }
  }
}

/**
 * Full Expanded Hexagon Radar Chart for statistical diagnosis and reporting.
 * Allows comparing current answers against previous answers directly in the center!
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExpandedHexagonRadarChart(
  scores: Map<Int, Int>,
  previousScores: Map<Int, Int>? = null,
  selectedDimensionIndex: Int?,
  onDimensionSelect: (Int) -> Unit,
  modifier: Modifier = Modifier
) {
  val dimensions = DimensionsProvider.dimensions
  var viewMode by remember {
    mutableStateOf(if (previousScores != null) HexagonViewMode.OVERLAY else HexagonViewMode.CURRENT)
  }

  // Animate current scores
  val animatedCurrentScores = (0..5).map { index ->
    val score = scores[index] ?: 5
    animateFloatAsState(
      targetValue = (score / 10f).coerceIn(0.12f, 1f),
      animationSpec = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMediumLow
      ),
      label = "current_score_$index"
    ).value
  }

  // Animate previous scores
  val animatedPreviousScores = (0..5).map { index ->
    val score = previousScores?.get(index) ?: 5
    animateFloatAsState(
      targetValue = (score / 10f).coerceIn(0.12f, 1f),
      animationSpec = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMediumLow
      ),
      label = "prev_score_$index"
    ).value
  }

  Column(
    modifier = modifier.fillMaxWidth(),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // Mode Switcher if previous scores exist
    if (previousScores != null) {
      Row(
        modifier = Modifier
          .clip(RoundedCornerShape(14.dp))
          .background(SurfaceVariantLight)
          .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        ViewModeButton(
          title = "Superponer",
          isSelected = viewMode == HexagonViewMode.OVERLAY,
          onClick = { viewMode = HexagonViewMode.OVERLAY }
        )
        ViewModeButton(
          title = "Actual",
          isSelected = viewMode == HexagonViewMode.CURRENT,
          onClick = { viewMode = HexagonViewMode.CURRENT }
        )
        ViewModeButton(
          title = "Anterior",
          isSelected = viewMode == HexagonViewMode.PREVIOUS,
          onClick = { viewMode = HexagonViewMode.PREVIOUS }
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Visual Legend
      Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        if (viewMode == HexagonViewMode.OVERLAY || viewMode == HexagonViewMode.CURRENT) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(AccentCharcoal)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Respuestas Actuales",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
              color = TextPrimaryLight
            )
          }
        }

        if (viewMode == HexagonViewMode.OVERLAY || viewMode == HexagonViewMode.PREVIOUS) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .width(16.dp)
                .height(3.dp)
                .background(Color(0xFF8E95A2), shape = RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Respuestas Anteriores",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
              color = TextSecondaryLight
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))
    }

    // Hexagonal Canvas Box
    BoxWithConstraints(
      modifier = Modifier
        .size(280.dp)
        .testTag("expanded_hexagon_chart"),
      contentAlignment = Alignment.Center
    ) {
      Canvas(
        modifier = Modifier
          .fillMaxSize()
          .pointerInput(scores, previousScores, viewMode) {
            detectTapGestures { tapOffset ->
              val center = Offset(size.width / 2f, size.height / 2f)
              val minDim = kotlin.math.min(size.width, size.height)
              val maxRadius = (minDim / 2f) * 0.82f

              var closestIndex = 0
              var minDistance = Float.MAX_VALUE

              for (i in 0..5) {
                val r = maxRadius * animatedCurrentScores[i]
                val angle = -PI / 2 + i * (PI / 3)
                val vx = (center.x + r * cos(angle)).toFloat()
                val vy = (center.y + r * sin(angle)).toFloat()
                val dist = hypot(tapOffset.x - vx, tapOffset.y - vy)
                if (dist < minDistance) {
                  minDistance = dist
                  closestIndex = i
                }
              }

              val angleFromCenter = Math.atan2(
                (tapOffset.y - center.y).toDouble(),
                (tapOffset.x - center.x).toDouble()
              )
              var normalized = angleFromCenter + PI / 2
              if (normalized < 0) normalized += 2 * PI
              val sectorIndex = ((normalized + PI / 6) / (PI / 3)).toInt() % 6

              val targetIndex = if (minDistance < 60f) closestIndex else sectorIndex
              onDimensionSelect(targetIndex)
            }
          }
      ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxRadius = (size.minDimension / 2f) * 0.82f

        // Concentric level rings at 2.5, 5.0, 7.5, 10.0
        val levelSteps = listOf(0.25f, 0.50f, 0.75f, 1.0f)
        levelSteps.forEach { levelFrac ->
          drawHexagonPolygon(
            center = center,
            radius = maxRadius * levelFrac,
            strokeColor = if (levelFrac == 1.0f) BorderLight else BorderLight.copy(alpha = 0.5f),
            strokeWidth = if (levelFrac == 1.0f) 1.5.dp.toPx() else 1.dp.toPx()
          )
        }

        // Radial axis spokes
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
            strokeWidth = 1.dp.toPx()
          )
        }

        // 1. Draw PREVIOUS Hexagon if enabled
        val showPrevious = (viewMode == HexagonViewMode.OVERLAY || viewMode == HexagonViewMode.PREVIOUS) && previousScores != null
        if (showPrevious) {
          val prevPath = Path()
          val prevPoints = (0..5).map { i ->
            val r = maxRadius * animatedPreviousScores[i]
            val angle = -PI / 2 + i * (PI / 3)
            Offset(
              (center.x + r * cos(angle)).toFloat(),
              (center.y + r * sin(angle)).toFloat()
            )
          }

          prevPath.moveTo(prevPoints[0].x, prevPoints[0].y)
          for (i in 1 until prevPoints.size) {
            prevPath.lineTo(prevPoints[i].x, prevPoints[i].y)
          }
          prevPath.close()

          // Subtle translucent fill for previous
          drawPath(
            path = prevPath,
            color = Color(0xFF64748B).copy(alpha = if (viewMode == HexagonViewMode.PREVIOUS) 0.22f else 0.10f),
            style = Fill
          )

          // Dashed stroke for previous
          drawPath(
            path = prevPath,
            color = Color(0xFF64748B),
            style = Stroke(
              width = if (viewMode == HexagonViewMode.PREVIOUS) 2.5.dp.toPx() else 1.8.dp.toPx(),
              pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f),
              join = StrokeJoin.Round,
              cap = StrokeCap.Round
            )
          )

          // Open circle dots on previous vertices
          prevPoints.forEach { pt ->
            drawCircle(
              color = Color.White,
              radius = 4.dp.toPx(),
              center = pt
            )
            drawCircle(
              color = Color(0xFF64748B),
              radius = 3.dp.toPx(),
              center = pt,
              style = Stroke(width = 1.5.dp.toPx())
            )
          }
        }

        // 2. Draw CURRENT Hexagon if enabled
        val showCurrent = viewMode == HexagonViewMode.OVERLAY || viewMode == HexagonViewMode.CURRENT
        if (showCurrent) {
          val currentPath = Path()
          val currentPoints = (0..5).map { i ->
            val r = maxRadius * animatedCurrentScores[i]
            val angle = -PI / 2 + i * (PI / 3)
            Offset(
              (center.x + r * cos(angle)).toFloat(),
              (center.y + r * sin(angle)).toFloat()
            )
          }

          currentPath.moveTo(currentPoints[0].x, currentPoints[0].y)
          for (i in 1 until currentPoints.size) {
            currentPath.lineTo(currentPoints[i].x, currentPoints[i].y)
          }
          currentPath.close()

          // Gradient fill
          drawPath(
            path = currentPath,
            brush = Brush.radialGradient(
              colors = listOf(
                AccentCharcoal.copy(alpha = 0.30f),
                AccentCharcoal.copy(alpha = 0.08f)
              ),
              center = center,
              radius = maxRadius
            ),
            style = Fill
          )

          // Border stroke
          drawPath(
            path = currentPath,
            color = AccentCharcoal,
            style = Stroke(
              width = 2.8.dp.toPx(),
              join = StrokeJoin.Round,
              cap = StrokeCap.Round
            )
          )

          // Vertex Points & Glowing Highlights
          currentPoints.forEachIndexed { i, point ->
            val isSelected = (selectedDimensionIndex == i)
            val accentColor = dimensions[i].accentColor

            if (isSelected) {
              drawCircle(
                color = accentColor.copy(alpha = 0.25f),
                radius = 16.dp.toPx(),
                center = point
              )
              drawCircle(
                color = accentColor.copy(alpha = 0.40f),
                radius = 10.dp.toPx(),
                center = point
              )
            }

            drawCircle(
              color = Color.White,
              radius = 6.dp.toPx(),
              center = point
            )
            drawCircle(
              color = accentColor,
              radius = 4.5.dp.toPx(),
              center = point
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Interactive category chips around the statistics with comparison deltas
    FlowRow(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 4.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      dimensions.forEach { dim ->
        val isSelected = selectedDimensionIndex == dim.index
        val score = scores[dim.index] ?: 5
        val prevScore = previousScores?.get(dim.index)
        val delta = if (prevScore != null) score - prevScore else null

        val chipInteraction = rememberBounceInteractionSource()
        Surface(
          onClick = { onDimensionSelect(dim.index) },
          interactionSource = chipInteraction,
          shape = RoundedCornerShape(16.dp),
          color = if (isSelected) AccentCharcoal else SurfaceCardLight,
          border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) AccentCharcoal else BorderLight
          ),
          modifier = Modifier.bouncyScale(chipInteraction, scaleDown = 0.95f)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(dim.accentColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = dim.title,
              style = MaterialTheme.typography.labelLarge,
              color = if (isSelected) Color.White else TextPrimaryLight
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "$score",
              style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
              color = if (isSelected) dim.accentColor else TextSecondaryLight
            )

            // Delta badge (vs previous response)
            if (delta != null && delta != 0) {
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = if (delta > 0) "+$delta" else "$delta",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = when {
                  delta > 0 -> Color(0xFF10B981)
                  else -> Color(0xFFEF4444)
                }
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun ViewModeButton(
  title: String,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  val interaction = rememberBounceInteractionSource()
  val bg by androidx.compose.animation.animateColorAsState(
    targetValue = if (isSelected) AccentCharcoal else Color.Transparent,
    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
    label = "view_mode_bg_$title"
  )
  Surface(
    onClick = onClick,
    interactionSource = interaction,
    shape = RoundedCornerShape(10.dp),
    color = bg,
    modifier = Modifier.bouncyScale(interaction, scaleDown = 0.93f)
  ) {
    Box(
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = title,
        style = MaterialTheme.typography.labelMedium.copy(
          fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        ),
        color = if (isSelected) Color.White else TextSecondaryLight
      )
    }
  }
}

private fun DrawScope.drawHexagonPolygon(
  center: Offset,
  radius: Float,
  strokeColor: Color,
  strokeWidth: Float
) {
  val path = Path()
  for (i in 0..5) {
    val angle = -PI / 2 + i * (PI / 3)
    val x = (center.x + radius * cos(angle)).toFloat()
    val y = (center.y + radius * sin(angle)).toFloat()
    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
  }
  path.close()
  drawPath(
    path = path,
    color = strokeColor,
    style = Stroke(width = strokeWidth)
  )
}

/**
 * Hexágono de dos capas para el tablero principal "Hoy":
 * - Capa PERCIBIDA (el cuestionario): contorno punteado (dashed stroke).
 * - Capa REAL (lo hecho en los últimos 14 días): relleno con transparencia y borde sólido.
 * - Día 1 sin datos reales: muestra SOLO el contorno percibido y el texto "Tu hexágono real se dibuja con lo que hagas esta semana".
 * - Animación: cuando cambia un puntaje real, el vértice crece o decrece con animateFloatAsState (~600 ms, easing suave).
 * - Tocar un vértice abre el detalle de esa dimensión.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DualLayerHexagonRadarChart(
  perceivedScores: Map<Int, Int>,
  realScores: Map<Int, Float>,
  selectedDimensionIndex: Int? = null,
  onDimensionSelect: (Int) -> Unit,
  modifier: Modifier = Modifier,
  chartSize: Dp = 280.dp,
  showLegend: Boolean = true,
  showChips: Boolean = true
) {
  val dimensions = DimensionsProvider.dimensions
  val hasRealData = realScores.values.any { it > 0.05f }

  // Animación del puntaje percibido (600 ms con easing suave)
  val animatedPerceived = (0..5).map { index ->
    val score = perceivedScores[index] ?: 5
    animateFloatAsState(
      targetValue = (score / 10f).coerceIn(0.12f, 1f),
      animationSpec = androidx.compose.animation.core.tween(
        durationMillis = 600,
        easing = androidx.compose.animation.core.FastOutSlowInEasing
      ),
      label = "dual_perceived_$index"
    ).value
  }

  // Animación de los puntajes reales (600 ms con easing suave)
  val animatedReal = (0..5).map { index ->
    val score = realScores[index] ?: 0f
    animateFloatAsState(
      targetValue = (score / 10f).coerceIn(0.06f, 1f),
      animationSpec = androidx.compose.animation.core.tween(
        durationMillis = 600,
        easing = androidx.compose.animation.core.FastOutSlowInEasing
      ),
      label = "dual_real_$index"
    ).value
  }

  Column(
    modifier = modifier.fillMaxWidth(),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    if (showLegend) {
      // Leyenda de las dos capas
      Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Capa real
        if (hasRealData) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(AccentCharcoal)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Real (14 días)",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = TextPrimaryLight
            )
          }
        }

        // Capa percibida
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .width(16.dp)
              .height(3.dp)
              .background(Color(0xFF8E95A2), shape = RoundedCornerShape(2.dp))
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Percibido (Cuestionario)",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
            color = TextSecondaryLight
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))
    }

    // Lienzo del Hexágono
    BoxWithConstraints(
      modifier = Modifier
        .size(chartSize)
        .testTag("dual_layer_hexagon_chart"),
      contentAlignment = Alignment.Center
    ) {
      Canvas(
        modifier = Modifier
          .fillMaxSize()
          .pointerInput(perceivedScores, realScores) {
            detectTapGestures { tapOffset ->
              val center = Offset(size.width / 2f, size.height / 2f)
              val minDim = kotlin.math.min(size.width, size.height)
              val maxRadius = (minDim / 2f) * 0.82f

              var closestIndex = 0
              var minDistance = Float.MAX_VALUE

              for (i in 0..5) {
                val r = maxRadius * if (hasRealData) animatedReal[i] else animatedPerceived[i]
                val angle = -PI / 2 + i * (PI / 3)
                val vx = (center.x + r * cos(angle)).toFloat()
                val vy = (center.y + r * sin(angle)).toFloat()
                val dist = hypot(tapOffset.x - vx, tapOffset.y - vy)
                if (dist < minDistance) {
                  minDistance = dist
                  closestIndex = i
                }
              }

              val angleFromCenter = Math.atan2(
                (tapOffset.y - center.y).toDouble(),
                (tapOffset.x - center.x).toDouble()
              )
              var normalized = angleFromCenter + PI / 2
              if (normalized < 0) normalized += 2 * PI
              val sectorIndex = ((normalized + PI / 6) / (PI / 3)).toInt() % 6

              val targetIndex = if (minDistance < 70f) closestIndex else sectorIndex
              onDimensionSelect(targetIndex)
            }
          }
      ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxRadius = (size.minDimension / 2f) * 0.82f

        // Anillos concéntricos de referencia (25%, 50%, 75%, 100%)
        val levelSteps = listOf(0.25f, 0.50f, 0.75f, 1.0f)
        levelSteps.forEach { levelFrac ->
          drawHexagonPolygon(
            center = center,
            radius = maxRadius * levelFrac,
            strokeColor = if (levelFrac == 1.0f) BorderLight else BorderLight.copy(alpha = 0.5f),
            strokeWidth = if (levelFrac == 1.0f) 1.5.dp.toPx() else 1.dp.toPx()
          )
        }

        // Radios de los 6 ejes
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
            strokeWidth = 1.dp.toPx()
          )
        }

        // ==========================================
        // CAPA 1: PERCIBIDO (Contorno punteado)
        // ==========================================
        val perceivedPath = Path()
        val perceivedPoints = (0..5).map { i ->
          val r = maxRadius * animatedPerceived[i]
          val angle = -PI / 2 + i * (PI / 3)
          Offset(
            (center.x + r * cos(angle)).toFloat(),
            (center.y + r * sin(angle)).toFloat()
          )
        }

        perceivedPath.moveTo(perceivedPoints[0].x, perceivedPoints[0].y)
        for (i in 1 until perceivedPoints.size) {
          perceivedPath.lineTo(perceivedPoints[i].x, perceivedPoints[i].y)
        }
        perceivedPath.close()

        // Trazo punteado para el percibido
        drawPath(
          path = perceivedPath,
          color = Color(0xFF64748B),
          style = Stroke(
            width = 2.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f),
            join = StrokeJoin.Round,
            cap = StrokeCap.Round
          )
        )

        // Puntos en los vértices del percibido (resaltado en color si está seleccionado sin datos reales)
        perceivedPoints.forEachIndexed { i, pt ->
          val isSelected = !hasRealData && selectedDimensionIndex == i
          val dimColor = dimensions[i].accentColor
          if (isSelected) {
            drawCircle(
              color = dimColor.copy(alpha = 0.28f),
              radius = 12.dp.toPx(),
              center = pt
            )
          }
          drawCircle(
            color = Color.White,
            radius = if (isSelected) 5.dp.toPx() else 3.5.dp.toPx(),
            center = pt
          )
          drawCircle(
            color = if (isSelected) dimColor else Color(0xFF64748B),
            radius = if (isSelected) 3.8.dp.toPx() else 2.5.dp.toPx(),
            center = pt,
            style = if (isSelected) Fill else Stroke(width = 1.2.dp.toPx())
          )
        }

        // ==========================================
        // CAPA 2: REAL (Relleno translúcido + contorno)
        // Solo si hay datos reales registrados; Día 1 sin datos NO muestra relleno vacío.
        // ==========================================
        if (hasRealData) {
          val realPath = Path()
          val realPoints = (0..5).map { i ->
            val r = maxRadius * animatedReal[i]
            val angle = -PI / 2 + i * (PI / 3)
            Offset(
              (center.x + r * cos(angle)).toFloat(),
              (center.y + r * sin(angle)).toFloat()
            )
          }

          realPath.moveTo(realPoints[0].x, realPoints[0].y)
          for (i in 1 until realPoints.size) {
            realPath.lineTo(realPoints[i].x, realPoints[i].y)
          }
          realPath.close()

          // Relleno degradado con transparencia
          drawPath(
            path = realPath,
            brush = Brush.radialGradient(
              colors = listOf(
                AccentCharcoal.copy(alpha = 0.32f),
                AccentCharcoal.copy(alpha = 0.08f)
              ),
              center = center,
              radius = maxRadius
            ),
            style = Fill
          )

          // Borde sólido
          drawPath(
            path = realPath,
            color = AccentCharcoal,
            style = Stroke(
              width = 2.6.dp.toPx(),
              join = StrokeJoin.Round,
              cap = StrokeCap.Round
            )
          )

          // Vértices y resplandor interactivo
          realPoints.forEachIndexed { i, pt ->
            val isSelected = selectedDimensionIndex == i
            val dimColor = dimensions[i].accentColor

            if (isSelected) {
              drawCircle(
                color = dimColor.copy(alpha = 0.30f),
                radius = 14.dp.toPx(),
                center = pt
              )
            }
            drawCircle(
              color = Color.White,
              radius = 5.dp.toPx(),
              center = pt
            )
            drawCircle(
              color = dimColor,
              radius = 4.dp.toPx(),
              center = pt
            )
          }
        }
      }
    }

    // Mensaje para Día 1 sin datos reales (solo en vista completa con chips)
    if (!hasRealData && showChips) {
      Spacer(modifier = Modifier.height(10.dp))
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = SurfaceVariantLight,
        modifier = Modifier.padding(horizontal = 20.dp)
      ) {
        Text(
          text = "Tu hexágono real se dibuja con lo que hagas esta semana",
          style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
          color = TextSecondaryLight,
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
      }
    }

    if (showChips) {
      Spacer(modifier = Modifier.height(12.dp))

      // Chips interactivos de dimensiones con su puntaje real y percibido
      FlowRow(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        dimensions.forEach { dim ->
          val isSelected = selectedDimensionIndex == dim.index
          val perceived = perceivedScores[dim.index] ?: 5
          val real = (realScores[dim.index] ?: 0f).toInt()

          val dualChipInteraction = rememberBounceInteractionSource()
          Surface(
            onClick = { onDimensionSelect(dim.index) },
            interactionSource = dualChipInteraction,
            shape = RoundedCornerShape(14.dp),
            color = if (isSelected) AccentCharcoal else SurfaceCardLight,
            border = androidx.compose.foundation.BorderStroke(
              1.dp,
              if (isSelected) AccentCharcoal else BorderLight
            ),
            modifier = Modifier.bouncyScale(dualChipInteraction, scaleDown = 0.95f)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(8.dp)
                  .clip(CircleShape)
                  .background(dim.accentColor)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = dim.title,
                style = MaterialTheme.typography.labelMedium,
                color = if (isSelected) Color.White else TextPrimaryLight
              )
              Spacer(modifier = Modifier.width(6.dp))
              if (hasRealData) {
                Text(
                  text = "$real",
                  style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                  color = if (isSelected) dim.accentColor else AccentCharcoal
                )
                Text(
                  text = " / $perceived",
                  style = MaterialTheme.typography.labelSmall,
                  color = if (isSelected) Color.White.copy(alpha = 0.7f) else TextSecondaryLight
                )
              } else {
                Text(
                  text = "$perceived",
                  style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                  color = if (isSelected) dim.accentColor else TextSecondaryLight
                )
              }
            }
          }
        }
      }
    }
  }
}

