package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.TipsAndUpdates
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AssessmentReport
import com.example.model.DimensionDiagnostic
import com.example.model.PatternInsight
import com.example.model.ReportFilter
import com.example.ui.components.ExpandedHexagonRadarChart
import com.example.ui.components.bouncyClick
import com.example.ui.theme.AccentCharcoal
import com.example.ui.theme.BorderLight
import com.example.ui.theme.CanvasBackgroundLight
import com.example.ui.theme.SurfaceCardLight
import com.example.ui.theme.SurfaceVariantLight
import com.example.ui.theme.TextPrimaryLight
import com.example.ui.theme.TextSecondaryLight
import com.example.ui.theme.TextTertiaryLight

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReportScreen(
  report: AssessmentReport,
  selectedDimensionIndex: Int?,
  filter: ReportFilter,
  onDimensionSelect: (Int) -> Unit,
  onFilterChange: (ReportFilter) -> Unit,
  onRetake: () -> Unit,
  onContinue: () -> Unit,
  modifier: Modifier = Modifier
) {
  val listState = rememberLazyListState()

  // Filter diagnostics based on mode
  val filteredDiagnostics = when (filter) {
    ReportFilter.ALL -> report.diagnostics
    ReportFilter.WEAKNESSES -> report.diagnostics.filter { it.score <= 6 }
    ReportFilter.STRENGTHS -> report.diagnostics.filter { it.score >= 7 }
  }

  // Scroll to selected dimension card when tapped in radar chart
  LaunchedEffect(selectedDimensionIndex) {
    if (selectedDimensionIndex != null) {
      val targetIndex = filteredDiagnostics.indexOfFirst { it.dimension.index == selectedDimensionIndex }
      if (targetIndex >= 0) {
        // Offset by items before diagnostics list (Header, Hexagon Chart, Wisdom card, Archetype card, Patterns)
        listState.animateScrollToItem((targetIndex + 4).coerceAtLeast(0))
      }
    }
  }

  LazyColumn(
    state = listState,
    modifier = modifier
      .fillMaxSize()
      .background(CanvasBackgroundLight)
      .statusBarsPadding()
      .navigationBarsPadding()
      .padding(horizontal = 20.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Top Bar Header
    item(key = "header") {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 12.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Estadística Hexagonal",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = TextPrimaryLight
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = if (report.previousScores != null) {
              "Comparativa de Respuestas Actuales vs Anteriores"
            } else {
              "Diagnóstico Integral de Balance y Debilidades"
            },
            style = MaterialTheme.typography.titleMedium,
            color = TextSecondaryLight
          )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedButton(
            onClick = onRetake,
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
            colors = ButtonDefaults.outlinedButtonColors(
              contentColor = TextPrimaryLight
            ),
            modifier = Modifier.bouncyClick(onClick = onRetake).testTag("btn_retake_header")
          ) {
            Icon(
              imageVector = Icons.Default.Refresh,
              contentDescription = null,
              modifier = Modifier.size(16.dp)
            )
          }

          Button(
            onClick = onContinue,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = AccentCharcoal,
              contentColor = Color.White
            ),
            modifier = Modifier.bouncyClick(onClick = onContinue).testTag("btn_continue_header")
          ) {
            Text("Continuar", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
          }
        }
      }
    }

    // Centered Interactive Hexagonal Chart (with Previous Responses Comparison)
    item(key = "hexagon_chart") {
      Surface(
        shape = RoundedCornerShape(26.dp),
        color = SurfaceCardLight,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 18.dp, horizontal = 12.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = "Geometría de tu Equilibrio",
            style = MaterialTheme.typography.labelMedium.copy(
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp
            ),
            color = TextSecondaryLight
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = if (report.previousScores != null) {
              "Hexágono actual vs respuestas anteriores superpuestas"
            } else {
              "Toca cada vértice para explorar sus debilidades y plan"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = TextTertiaryLight
          )

          Spacer(modifier = Modifier.height(14.dp))

          // The interactive radar displaying current and previous scores
          ExpandedHexagonRadarChart(
            scores = report.scores,
            previousScores = report.previousScores,
            selectedDimensionIndex = selectedDimensionIndex,
            onDimensionSelect = onDimensionSelect
          )
        }
      }
    }

    // Deep Wisdom Quote Card (Philosophical, Grounded Insight)
    item(key = "wisdom_card") {
      WisdomQuoteCard(
        quote = report.wisdomQuote,
        author = report.wisdomAuthor
      )
    }

    // Archetype and KPI Metrics Section
    item(key = "archetype_section") {
      ArchetypeCard(report = report)
    }

    // Cross-Domain Pattern Insights
    if (report.patternInsights.isNotEmpty()) {
      item(key = "cross_patterns_header") {
        Text(
          text = "Diagnóstico de Patrones Transversales",
          style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
          color = TextPrimaryLight,
          modifier = Modifier.padding(top = 8.dp)
        )
      }

      items(report.patternInsights, key = { it.title }) { insight ->
        PatternInsightCard(insight = insight)
      }
    }

    // Detailed Diagnostics & Improvement Section
    item(key = "diagnostics_header") {
      Column(modifier = Modifier.padding(top = 8.dp)) {
        Text(
          text = "Análisis por Categoría y Debilidades",
          style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
          color = TextPrimaryLight
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "Revisa qué áreas requieren atención prioritaria y qué acciones tomar.",
          style = MaterialTheme.typography.bodyMedium,
          color = TextSecondaryLight
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Filter chips: Todas, Debilidades, Fortalezas
        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          FilterChip(
            selected = filter == ReportFilter.ALL,
            onClick = { onFilterChange(ReportFilter.ALL) },
            label = { Text("Todas (6)") },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = AccentCharcoal,
              selectedLabelColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.bouncyClick { onFilterChange(ReportFilter.ALL) }
          )

          FilterChip(
            selected = filter == ReportFilter.WEAKNESSES,
            onClick = { onFilterChange(ReportFilter.WEAKNESSES) },
            label = { Text("Debilidades (${report.weaknessesCount})") },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = AccentCharcoal,
              selectedLabelColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.bouncyClick { onFilterChange(ReportFilter.WEAKNESSES) }
          )

          FilterChip(
            selected = filter == ReportFilter.STRENGTHS,
            onClick = { onFilterChange(ReportFilter.STRENGTHS) },
            label = { Text("Fortalezas (${report.strengthsCount})") },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = AccentCharcoal,
              selectedLabelColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.bouncyClick { onFilterChange(ReportFilter.STRENGTHS) }
          )
        }
      }
    }

    // List of Diagnostics Cards
    items(filteredDiagnostics, key = { it.dimension.index }) { diag ->
      val isSelected = selectedDimensionIndex == diag.dimension.index
      DimensionDiagnosticCard(
        diagnostic = diag,
        isSelected = isSelected,
        onClick = { onDimensionSelect(diag.dimension.index) }
      )
    }

    // Bottom Re-evaluation CTA
    item(key = "bottom_cta") {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Button(
          onClick = onContinue,
          shape = RoundedCornerShape(18.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = AccentCharcoal,
            contentColor = Color.White
          ),
          modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .bouncyClick(onClick = onContinue)
            .testTag("btn_continue_footer")
        ) {
          Text(
            text = "Continuar al Perfil y Metas",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
          onClick = onRetake,
          shape = RoundedCornerShape(18.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
          colors = ButtonDefaults.outlinedButtonColors(
            contentColor = TextPrimaryLight
          ),
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .bouncyClick(onClick = onRetake)
            .testTag("btn_retake_footer")
        ) {
          Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Realizar Nueva Evaluación de Progreso",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
          )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
          text = "Equilibrio · Diagnóstico de 6 Dimensiones Vitales",
          style = MaterialTheme.typography.labelMedium,
          color = TextTertiaryLight
        )
      }
    }
  }
}

@Composable
private fun WisdomQuoteCard(quote: String, author: String) {
  Surface(
    shape = RoundedCornerShape(22.dp),
    color = SurfaceCardLight,
    border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
    shadowElevation = 1.dp,
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(18.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.FormatQuote,
            contentDescription = null,
            tint = AccentCharcoal,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "PENSAMIENTO GUÍA",
            style = MaterialTheme.typography.labelMedium.copy(
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp
            ),
            color = TextSecondaryLight
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = "“$quote”",
        style = MaterialTheme.typography.titleMedium.copy(
          fontStyle = FontStyle.Italic,
          fontSize = 15.sp,
          lineHeight = 22.sp
        ),
        color = TextPrimaryLight
      )

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = "— $author",
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
        color = TextSecondaryLight,
        modifier = Modifier.align(Alignment.End)
      )
    }
  }
}

@Composable
private fun ArchetypeCard(report: AssessmentReport) {
  Surface(
    shape = RoundedCornerShape(26.dp),
    color = SurfaceCardLight,
    border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
    shadowElevation = 2.dp,
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(20.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Outlined.TipsAndUpdates,
            contentDescription = null,
            tint = AccentCharcoal,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "PERFIL GENERAL",
            style = MaterialTheme.typography.labelMedium.copy(
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp
            ),
            color = TextSecondaryLight
          )
        }

        Surface(
          color = SurfaceVariantLight,
          shape = RoundedCornerShape(10.dp)
        ) {
          Text(
            text = "${report.harmonyPercentage}% Armonía",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = TextPrimaryLight,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      Text(
        text = report.archetypeTitle,
        style = MaterialTheme.typography.displayMedium.copy(
          fontSize = 22.sp,
          fontWeight = FontWeight.Bold
        ),
        color = TextPrimaryLight
      )

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = report.archetypeSummary,
        style = MaterialTheme.typography.bodyLarge,
        color = TextSecondaryLight
      )

      Spacer(modifier = Modifier.height(14.dp))

      // Core takeaway callout box
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(14.dp))
          .background(SurfaceVariantLight)
          .padding(14.dp)
      ) {
        Row(verticalAlignment = Alignment.Top) {
          Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = AccentCharcoal,
            modifier = Modifier
              .size(16.dp)
              .padding(top = 2.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = report.coreTakeaway,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = TextPrimaryLight
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // 3 KPI Badges Row (with delta comparison)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Average Score & Delta
        val prevAvg = report.previousAverageScore
        val avgSub = if (prevAvg != null) {
          val diff = report.averageScore - prevAvg
          if (diff > 0) "+${String.format("%.1f", diff)} vs ant." else if (diff < 0) "${String.format("%.1f", diff)} vs ant." else "Sin variación"
        } else {
          "de 10"
        }

        KpiMiniCard(
          title = "Promedio",
          value = String.format("%.1f", report.averageScore),
          sub = avgSub,
          subColor = if (prevAvg != null && (report.averageScore - prevAvg) > 0) Color(0xFF10B981) else TextSecondaryLight,
          modifier = Modifier.weight(1f)
        )

        // Highest Dimension
        KpiMiniCard(
          title = "Pilar Fuerte",
          value = report.highestDimension.title,
          sub = "${report.scores[report.highestDimension.index]}/10",
          accentColor = report.highestDimension.accentColor,
          modifier = Modifier.weight(1f)
        )

        // Lowest Dimension / Critical
        KpiMiniCard(
          title = "Foco Débil",
          value = report.lowestDimension.title,
          sub = "${report.scores[report.lowestDimension.index]}/10",
          accentColor = Color(0xFFEF4444),
          modifier = Modifier.weight(1f)
        )
      }
    }
  }
}

@Composable
private fun KpiMiniCard(
  title: String,
  value: String,
  sub: String,
  accentColor: Color? = null,
  subColor: Color = TextSecondaryLight,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(16.dp))
      .background(SurfaceVariantLight)
      .padding(10.dp)
  ) {
    Column {
      Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = TextTertiaryLight
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = value,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = accentColor ?: TextPrimaryLight,
        maxLines = 1
      )
      Text(
        text = sub,
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
        color = subColor,
        maxLines = 1
      )
    }
  }
}

@Composable
private fun PatternInsightCard(insight: PatternInsight) {
  val borderColor = if (insight.isAlert) Color(0xFFFCA5A5) else BorderLight
  val bgTint = if (insight.isAlert) Color(0xFFFEF2F2) else SurfaceCardLight

  Surface(
    shape = RoundedCornerShape(22.dp),
    color = bgTint,
    border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
    shadowElevation = 1.dp,
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(18.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = if (insight.isAlert) Icons.Default.WarningAmber else Icons.Outlined.Lightbulb,
            contentDescription = null,
            tint = if (insight.isAlert) Color(0xFFDC2626) else AccentCharcoal,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = insight.tag.uppercase(),
            style = MaterialTheme.typography.labelMedium.copy(
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp
            ),
            color = if (insight.isAlert) Color(0xFFDC2626) else TextSecondaryLight
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = insight.title,
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        color = TextPrimaryLight
      )

      Text(
        text = insight.subtitle,
        style = MaterialTheme.typography.labelLarge,
        color = if (insight.isAlert) Color(0xFFB91C1C) else TextSecondaryLight
      )

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = insight.description,
        style = MaterialTheme.typography.bodyLarge,
        color = TextPrimaryLight
      )

      Spacer(modifier = Modifier.height(12.dp))

      // Recommendation box
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(14.dp))
          .background(if (insight.isAlert) Color.White else SurfaceVariantLight)
          .padding(12.dp)
      ) {
        Row(verticalAlignment = Alignment.Top) {
          Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = if (insight.isAlert) Color(0xFFDC2626) else AccentCharcoal,
            modifier = Modifier
              .size(16.dp)
              .padding(top = 2.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = insight.recommendation,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = TextPrimaryLight
          )
        }
      }
    }
  }
}

@Composable
private fun DimensionDiagnosticCard(
  diagnostic: DimensionDiagnostic,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  var isExpanded by remember { mutableStateOf(isSelected || diagnostic.score <= 6) }

  val targetBorderColor = if (isSelected) diagnostic.dimension.accentColor else BorderLight
  val animatedBorderColor by animateColorAsState(
    targetValue = targetBorderColor,
    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
    label = "border_color"
  )

  Surface(
    shape = RoundedCornerShape(22.dp),
    color = SurfaceCardLight,
    border = androidx.compose.foundation.BorderStroke(
      width = if (isSelected) 2.dp else 1.dp,
      color = animatedBorderColor
    ),
    shadowElevation = if (isSelected) 4.dp else 1.dp,
    modifier = Modifier
      .fillMaxWidth()
      .bouncyClick(scaleDown = 0.98f) {
        isExpanded = !isExpanded
        onClick()
      }
      .testTag("diagnostic_card_${diagnostic.dimension.index}")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(18.dp)
    ) {
      // Header: Dimension Icon, Title, Score and Tier Badge + Delta
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
              .background(diagnostic.dimension.accentColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = diagnostic.dimension.icon,
              contentDescription = null,
              tint = diagnostic.dimension.accentColor,
              modifier = Modifier.size(22.dp)
            )
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = diagnostic.dimension.title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = TextPrimaryLight
              )

              // Delta pill compared to previous answers
              if (diagnostic.previousScore != null) {
                val delta = diagnostic.score - diagnostic.previousScore
                if (delta != 0) {
                  Spacer(modifier = Modifier.width(8.dp))
                  Surface(
                    color = if (delta > 0) Color(0xFFD1FAE5) else Color(0xFFFEE2E2),
                    shape = RoundedCornerShape(8.dp)
                  ) {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                      Icon(
                        imageVector = if (delta > 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                        contentDescription = null,
                        tint = if (delta > 0) Color(0xFF065F46) else Color(0xFF991B1B),
                        modifier = Modifier.size(12.dp)
                      )
                      Spacer(modifier = Modifier.width(2.dp))
                      Text(
                        text = if (delta > 0) "+$delta" else "$delta",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (delta > 0) Color(0xFF065F46) else Color(0xFF991B1B)
                      )
                    }
                  }
                }
              }
            }

            Text(
              text = "${diagnostic.score} / 10 · ${diagnostic.tier.label}",
              style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
              color = Color(diagnostic.tier.badgeColorHex)
            )
          }
        }

        IconButton(
          onClick = { isExpanded = !isExpanded },
          modifier = Modifier.size(32.dp)
        ) {
          Icon(
            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
            contentDescription = if (isExpanded) "Colapsar" else "Expandir",
            tint = TextSecondaryLight
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Diagnosis summary
      Text(
        text = diagnostic.diagnosisSummary,
        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
        color = TextPrimaryLight
      )

      AnimatedVisibility(
        visible = isExpanded,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
      ) {
        Column(modifier = Modifier.padding(top = 14.dp)) {
          // Core Philosophy Quote Callout (Calidad de la buena)
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(14.dp))
              .background(SurfaceVariantLight)
              .padding(12.dp)
          ) {
            Column {
              Text(
                text = "PRINCIPIO FUNDAMENTAL",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 0.5.sp
                ),
                color = TextSecondaryLight
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = diagnostic.corePhilosophy,
                style = MaterialTheme.typography.bodyMedium.copy(
                  fontStyle = FontStyle.Italic,
                  fontWeight = FontWeight.Medium
                ),
                color = TextPrimaryLight
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Current daily impact
          Text(
            text = "Impacto en tu día a día:",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = TextSecondaryLight
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = diagnostic.currentImpact,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondaryLight
          )

          Spacer(modifier = Modifier.height(14.dp))

          // Debilidades y puntos ciegos
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(16.dp))
              .background(SurfaceVariantLight)
              .padding(14.dp)
          ) {
            Column {
              Text(
                text = "DEBILIDADES Y PUNTOS CIEGOS DETECTADOS",
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 0.5.sp
                ),
                color = if (diagnostic.score <= 5) Color(0xFFDC2626) else TextSecondaryLight
              )
              Spacer(modifier = Modifier.height(8.dp))
              diagnostic.blindSpots.forEach { spot ->
                Row(
                  modifier = Modifier.padding(vertical = 3.dp),
                  verticalAlignment = Alignment.Top
                ) {
                  Text(
                    text = "•",
                    style = MaterialTheme.typography.titleMedium,
                    color = diagnostic.dimension.accentColor,
                    modifier = Modifier.padding(end = 6.dp)
                  )
                  Text(
                    text = spot,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimaryLight
                  )
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Qué debe mejorar (Acción concreta y medible)
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(16.dp))
              .background(diagnostic.dimension.accentColor.copy(alpha = 0.08f))
              .border(
                1.dp,
                diagnostic.dimension.accentColor.copy(alpha = 0.25f),
                RoundedCornerShape(16.dp)
              )
              .padding(14.dp)
          ) {
            Column {
              Text(
                text = "QUÉ DEBES MEJORAR (PASOS PRÁCTICOS)",
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 0.5.sp
                ),
                color = diagnostic.dimension.accentColor
              )
              Spacer(modifier = Modifier.height(8.dp))
              diagnostic.actionableImprovements.forEach { action ->
                Row(
                  modifier = Modifier.padding(vertical = 3.dp),
                  verticalAlignment = Alignment.Top
                ) {
                  Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = null,
                    tint = diagnostic.dimension.accentColor,
                    modifier = Modifier
                      .size(16.dp)
                      .padding(top = 2.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = action,
                    style = MaterialTheme.typography.bodyMedium.copy(
                      fontWeight = FontWeight.Medium
                    ),
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
