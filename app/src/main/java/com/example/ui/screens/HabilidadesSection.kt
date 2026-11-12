package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Celebration
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Hexagon
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AssessmentReport
import com.example.model.BalanceMission
import com.example.model.DimensionDiagnostic
import com.example.model.DimensionsProvider
import com.example.model.LifeDimension
import com.example.model.ScoreTier
import com.example.ui.components.DualLayerHexagonRadarChart
import com.example.ui.components.bouncyClick
import com.example.ui.theme.AccentCharcoal
import com.example.ui.theme.BorderLight
import com.example.ui.theme.SurfaceCardLight
import com.example.ui.theme.SurfaceVariantLight
import com.example.ui.theme.TextPrimaryLight
import com.example.ui.theme.TextSecondaryLight

enum class HabilidadesFilterTab(val label: String, val icon: ImageVector) {
  RESUMEN("Resumen", Icons.Outlined.BarChart),
  FORTALEZAS("Fortalezas", Icons.Outlined.FitnessCenter),
  CRECIMIENTO("Espacios de Crecimiento", Icons.Outlined.Spa),
  NUEVOS_RETOS("Nuevos Retos", Icons.Outlined.AutoAwesome)
}

/**
 * Pestaña interactiva de "Habilidades" (Tab 1) totalmente independiente del informe inicial.
 * Proporciona un resumen dinámico con navegación por pestañas/botones, tarjetas interactivas expandibles,
 * diferenciación clara entre fortalezas y áreas en crecimiento, y acciones directas para mejorar.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HabilidadesSection(
  report: AssessmentReport,
  realScores: Map<Int, Int>,
  realScoresFloat: Map<Int, Float>,
  allMissions: List<BalanceMission>,
  onLaunchMission: (BalanceMission) -> Unit,
  onOpenCreateMission: () -> Unit,
  modifier: Modifier = Modifier
) {
  var activeTab by remember { mutableStateOf(HabilidadesFilterTab.RESUMEN) }
  var expandedDimensionIndex by remember { mutableStateOf<Int?>(null) }
  val haptic = LocalHapticFeedback.current
  val scrollState = rememberScrollState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(scrollState)
      .padding(horizontal = 20.dp, vertical = 14.dp)
  ) {
    // 1. Encabezado principal del Centro de Habilidades
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
            fontSize = 28.sp
          ),
          color = TextPrimaryLight
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = "Tu mapa interactivo de capacidades y crecimiento",
          style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
          color = TextSecondaryLight
        )
      }

      // Badge de Armonía de Vida
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = SurfaceCardLight,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
      ) {
        Column(
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = "${report.harmonyPercentage}%",
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

    // 2. Selector de pestañas interactivas con botones redondeados
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(20.dp))
        .background(SurfaceVariantLight)
        .padding(4.dp),
      horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      HabilidadesFilterTab.values().forEach { tab ->
        val isSelected = activeTab == tab
        val bgColor by animateColorAsState(
          targetValue = if (isSelected) AccentCharcoal else Color.Transparent,
          animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
          label = "tab_bg_${tab.name}"
        )
        val textColor by animateColorAsState(
          targetValue = if (isSelected) Color.White else TextSecondaryLight,
          label = "tab_text_${tab.name}"
        )

        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .clickable {
              activeTab = tab
              expandedDimensionIndex = null
            }
            .padding(vertical = 10.dp),
          contentAlignment = Alignment.Center
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = tab.icon,
              contentDescription = null,
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
              maxLines = 1
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // 3. Contenido animado según la pestaña activa
    AnimatedContent(
      targetState = activeTab,
      transitionSpec = {
        (fadeIn() + slideInHorizontally { width -> width / 4 }) togetherWith
            (fadeOut() + slideOutHorizontally { width -> -width / 4 })
      },
      label = "tab_content_transition"
    ) { currentTab ->
      when (currentTab) {
        HabilidadesFilterTab.RESUMEN -> {
          ResumenHabilidadesTab(
            report = report,
            realScores = realScores,
            realScoresFloat = realScoresFloat,
            allMissions = allMissions,
            expandedDimensionIndex = expandedDimensionIndex,
            onToggleExpand = { dimIdx ->
              expandedDimensionIndex = if (expandedDimensionIndex == dimIdx) null else dimIdx
            },
            onLaunchMission = onLaunchMission
          )
        }

        HabilidadesFilterTab.FORTALEZAS -> {
          FortalezasTab(
            report = report,
            realScores = realScores,
            allMissions = allMissions,
            onLaunchMission = onLaunchMission
          )
        }

        HabilidadesFilterTab.CRECIMIENTO -> {
          EspaciosCrecimientoTab(
            report = report,
            realScores = realScores,
            allMissions = allMissions,
            onLaunchMission = onLaunchMission,
            onOpenCreateMission = onOpenCreateMission
          )
        }

        HabilidadesFilterTab.NUEVOS_RETOS -> {
          NuevosRetosTab(
            report = report,
            allMissions = allMissions,
            onLaunchMission = onLaunchMission,
            onOpenCreateMission = onOpenCreateMission
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(100.dp))
  }
}

/**
 * Pestaña 1: Resumen General interactivo con Hexágono de 2 capas y tarjetas expandibles de las 6 dimensiones.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ResumenHabilidadesTab(
  report: AssessmentReport,
  realScores: Map<Int, Int>,
  realScoresFloat: Map<Int, Float>,
  allMissions: List<BalanceMission>,
  expandedDimensionIndex: Int?,
  onToggleExpand: (Int) -> Unit,
  onLaunchMission: (BalanceMission) -> Unit
) {
  Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
    // Tarjeta del Hexágono Comparativo
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
          Text(
            text = "Mapa de 6 Habilidades",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = TextPrimaryLight
          )
          Text(
            text = "Toca para explorar",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondaryLight
          )
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

    // Tarjeta de métricas rápidas (Fortalezas vs Crecimiento)
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      // Fortalezas Count Card
      Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFF0FDF4),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0)),
        modifier = Modifier.weight(1f)
      ) {
        Row(
          modifier = Modifier.padding(14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Outlined.FitnessCenter,
            contentDescription = null,
            tint = Color(0xFF15803D),
            modifier = Modifier.size(24.dp)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "${report.diagnostics.count { (realScores[it.dimension.index] ?: it.score) >= 7 }} Fortalezas",
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
              color = Color(0xFF15803D)
            )
            Text(
              text = "Puntos consolidados",
              style = MaterialTheme.typography.labelSmall,
              color = Color(0xFF166534)
            )
          }
        }
      }

      // Crecimiento Count Card
      Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFFFFBEB),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A)),
        modifier = Modifier.weight(1f)
      ) {
        Row(
          modifier = Modifier.padding(14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Outlined.Spa,
            contentDescription = null,
            tint = Color(0xFFB45309),
            modifier = Modifier.size(24.dp)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "${report.diagnostics.count { (realScores[it.dimension.index] ?: it.score) <= 6 }} En Crecimiento",
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

    // Lista interactiva expandible de las 6 dimensiones
    DimensionsProvider.dimensions.forEach { dim ->
      val diag = report.diagnostics.find { it.dimension.index == dim.index }
      val isExpanded = expandedDimensionIndex == dim.index
      val perceived = report.scores[dim.index] ?: 5
      val real = realScores[dim.index] ?: 0
      val isStrong = (if (real > 0) real else perceived) >= 7

      InteractiveSkillCard(
        dimension = dim,
        diagnostic = diag,
        perceivedScore = perceived,
        realScore = real,
        isStrong = isStrong,
        isExpanded = isExpanded,
        onToggle = { onToggleExpand(dim.index) },
        allMissions = allMissions,
        onLaunchMission = onLaunchMission
      )
    }
  }
}

/**
 * Pestaña 2: Partes Fortalecidas (Dimensiones con puntuación sólida o excepcional)
 */
@Composable
private fun FortalezasTab(
  report: AssessmentReport,
  realScores: Map<Int, Int>,
  allMissions: List<BalanceMission>,
  onLaunchMission: (BalanceMission) -> Unit
) {
  val strongDiagnostics = remember(report, realScores) {
    report.diagnostics.filter { diag ->
      val real = realScores[diag.dimension.index] ?: 0
      val effectiveScore = if (real > 0) real else diag.score
      effectiveScore >= 7
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
          modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
          Text(
            text = "Tus Cimientos Sólidos",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFF15803D)
          )
          Text(
            text = "Estas son las áreas donde demuestras consistencia y energía positiva. Mantén el ritmo sin sobrecargarte.",
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
            modifier = Modifier.size(36.dp)
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "Tus fortalezas están en construcción",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = TextPrimaryLight
          )
          Text(
            text = "Completa tus misiones diarias para consolidar tus primeras dimensiones sobre 7 puntos.",
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
        val effectiveScore = if (real > 0) real else diag.score

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
                    text = "$effectiveScore / 10",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF15803D)
                  )
                }
              }
            }

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

            Spacer(modifier = Modifier.height(14.dp))

            // Misión sugerida para mantener la fortaleza
            val relatedMission = allMissions.firstOrNull { it.dimensionIndex == dim.index }
            if (relatedMission != null) {
              Button(
                onClick = { onLaunchMission(relatedMission) },
                colors = ButtonDefaults.buttonColors(containerColor = dim.accentColor),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                Icon(
                  imageVector = Icons.Default.ElectricBolt,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "Mantener fortaleza: ${relatedMission.title}",
                  color = Color.White,
                  style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
              }
            }
          }
        }
      }
    }
  }
}

/**
 * Pestaña 3: Espacios de Crecimiento (Dimensiones que requieren impulso y atención sin culpa)
 */
@Composable
private fun EspaciosCrecimientoTab(
  report: AssessmentReport,
  realScores: Map<Int, Int>,
  allMissions: List<BalanceMission>,
  onLaunchMission: (BalanceMission) -> Unit,
  onOpenCreateMission: () -> Unit
) {
  val growthDiagnostics = remember(report, realScores) {
    report.diagnostics.filter { diag ->
      val real = realScores[diag.dimension.index] ?: 0
      val effectiveScore = if (real > 0) real else diag.score
      effectiveScore <= 6
    }.sortedBy { realScores[it.dimension.index] ?: it.score }
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
          modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
          Text(
            text = "Tus Áreas con Espacio para Crecer",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFF92400E)
          )
          Text(
            text = "Aquí es donde una pequeña acción de 10 o 15 minutos genera el mayor impacto en tu bienestar.",
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
            text = "¡Excelente equilibrio alcanzado!",
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
        val effectiveScore = if (real > 0) real else diag.score
        val relatedMission = allMissions.firstOrNull { it.dimensionIndex == dim.index && !it.isCompleted }
          ?: allMissions.firstOrNull { it.dimensionIndex == dim.index }

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
                    text = "1.5x EXP  •  $effectiveScore / 10",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFFB45309)
                  )
                }
              }
            }

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

            // Acción clave
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
                  text = diag.actionableImprovements.firstOrNull() ?: "Dedica 10 min a avanzar sin presiones.",
                  style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                  color = TextPrimaryLight,
                  lineHeight = 18.sp
                )
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (relatedMission != null) {
              Button(
                onClick = { onLaunchMission(relatedMission) },
                colors = ButtonDefaults.buttonColors(containerColor = dim.accentColor),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                Icon(
                  imageVector = Icons.Default.ElectricBolt,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "Impulsar ahora: ${relatedMission.title}",
                  color = Color.White,
                  style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
              }
            }
          }
        }
      }
    }
  }
}

/**
 * Pestaña 4: Nuevos Retos y Desafíos
 */
@Composable
private fun NuevosRetosTab(
  report: AssessmentReport,
  allMissions: List<BalanceMission>,
  onLaunchMission: (BalanceMission) -> Unit,
  onOpenCreateMission: () -> Unit
) {
  val customMissions = allMissions.filter { it.isCustom }
  val suggestedMissions = allMissions.filter { !it.isCompleted }

  Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
    // Tarjeta de invitación a crear reto personalizado
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
          text = "Crea misiones personalizadas adaptadas a tus metas específicas con frecuencia diaria, semanal o como hábito recurrente.",
          style = MaterialTheme.typography.bodySmall,
          color = Color.White.copy(alpha = 0.85f),
          lineHeight = 19.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
          onClick = onOpenCreateMission,
          colors = ButtonDefaults.buttonColors(containerColor = Color.White),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier.fillMaxWidth()
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

    Text(
      text = "Nuevos Desafíos Disponibles",
      style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
      color = TextPrimaryLight
    )

    suggestedMissions.take(4).forEach { mission ->
      val dim = DimensionsProvider.dimensions.getOrNull(mission.dimensionIndex)
      val accentColor = dim?.accentColor ?: AccentCharcoal

      Surface(
        shape = RoundedCornerShape(18.dp),
        color = SurfaceCardLight,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = mission.dimensionTitle.uppercase(),
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = accentColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = mission.title,
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
              color = TextPrimaryLight
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = mission.durationLabel + " • +" + mission.expReward + " EXP",
              style = MaterialTheme.typography.labelSmall,
              color = TextSecondaryLight
            )
          }

          Spacer(modifier = Modifier.width(10.dp))

          Button(
            onClick = { onLaunchMission(mission) },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = Color.White)
          ) {
            Text("Probar", fontWeight = FontWeight.Bold, color = Color.White)
          }
        }
      }
    }
  }
}

/**
 * Tarjeta interactiva expandible para una habilidad individual
 */
@Composable
private fun InteractiveSkillCard(
  dimension: LifeDimension,
  diagnostic: DimensionDiagnostic?,
  perceivedScore: Int,
  realScore: Int,
  isStrong: Boolean,
  isExpanded: Boolean,
  onToggle: () -> Unit,
  allMissions: List<BalanceMission>,
  onLaunchMission: (BalanceMission) -> Unit
) {
  val accentColor = dimension.accentColor

  Surface(
    shape = RoundedCornerShape(20.dp),
    color = SurfaceCardLight,
    border = androidx.compose.foundation.BorderStroke(
      1.dp,
      if (isExpanded) accentColor else BorderLight
    ),
    shadowElevation = if (isExpanded) 3.dp else 1.dp,
    modifier = Modifier
      .fillMaxWidth()
      .bouncyClick(scaleDown = 0.98f) { onToggle() }
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
          Text(
            text = dimension.title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = TextPrimaryLight
          )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (isStrong) Color(0xFFDCFCE7) else Color(0xFFFEF3C7)
          ) {
            Text(
              text = if (isStrong) "Fortaleza ($perceivedScore/10)" else "Crecimiento ($perceivedScore/10)",
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

      // Detalle desplegable animado
      AnimatedVisibility(visible = isExpanded) {
        Column(modifier = Modifier.padding(top = 14.dp)) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(1.dp)
              .background(BorderLight)
          )

          Spacer(modifier = Modifier.height(12.dp))

          // Comparativa Percibido vs Real
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
              text = "Real (14 días): ${if (realScore > 0) "$realScore / 10" else "Sin datos"}",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = AccentCharcoal
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

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

          Spacer(modifier = Modifier.height(14.dp))

          val mission = allMissions.firstOrNull { it.dimensionIndex == dimension.index }
          if (mission != null) {
            Button(
              onClick = { onLaunchMission(mission) },
              colors = ButtonDefaults.buttonColors(containerColor = dimension.accentColor),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Icon(
                imageVector = Icons.Default.ElectricBolt,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Realizar Misión de ${dimension.title}",
                color = Color.White,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
              )
            }
          }
        }
      }
    }
  }
}
