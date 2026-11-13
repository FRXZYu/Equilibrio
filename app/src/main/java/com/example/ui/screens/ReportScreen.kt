package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
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
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.TipsAndUpdates
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AssessmentReport
import com.example.model.DimensionDiagnostic
import com.example.model.DimensionsProvider
import com.example.model.PatternInsight
import com.example.model.ReportFilter
import com.example.ui.components.ExpandedHexagonRadarChart
import com.example.ui.components.bouncyScale
import com.example.ui.components.rememberBounceInteractionSource
import com.example.ui.theme.AccentCharcoal
import com.example.ui.theme.BorderLight
import com.example.ui.theme.SurfaceCardLight
import com.example.ui.theme.SurfaceVariantLight
import com.example.ui.theme.TextPrimaryLight
import com.example.ui.theme.TextSecondaryLight
import com.example.ui.theme.TextTertiaryLight

private enum class ReportDeckTab(val label: String) {
  PROFILE("Perfil"),
  AREAS("Áreas"),
  PATTERNS("Patrones")
}

private enum class DiagnosticDetailSubTab(val label: String) {
  SUMMARY("Diagnóstico"),
  BLIND_SPOTS("Puntos ciegos"),
  ACTIONS("Mejora")
}

@Composable
fun ReportScreen(
  report: AssessmentReport,
  selectedDimensionIndex: Int?,
  filter: ReportFilter,
  onDimensionSelect: (Int) -> Unit,
  onFilterChange: (ReportFilter) -> Unit,
  onRetake: () -> Unit,
  onContinue: () -> Unit,
  isProfileCompleted: Boolean = false,
  modifier: Modifier = Modifier
) {
  val haptic = LocalHapticFeedback.current
  val scrollState = rememberScrollState()

  var activeDeckTab by remember { mutableStateOf(ReportDeckTab.PROFILE) }
  var areaPagerIndex by remember { mutableIntStateOf(0) }
  var areaSlideDirection by remember { mutableIntStateOf(1) }
  var activeSubTab by remember { mutableStateOf(DiagnosticDetailSubTab.SUMMARY) }

  var patternPagerIndex by remember { mutableIntStateOf(0) }
  var patternSlideDirection by remember { mutableIntStateOf(1) }

  var entered by remember { mutableStateOf(false) }
  LaunchedEffect(Unit) { entered = true }

  val filteredDiagnostics = remember(report.diagnostics, filter) {
    when (filter) {
      ReportFilter.ALL -> report.diagnostics
      ReportFilter.WEAKNESSES -> report.diagnostics.filter { it.score <= 6 }
      ReportFilter.STRENGTHS -> report.diagnostics.filter { it.score >= 7 }
    }
  }

  // Sincroniza el paginador cuando cambia la dimensión seleccionada en el hexágono o el filtro
  LaunchedEffect(selectedDimensionIndex, filteredDiagnostics) {
    if (selectedDimensionIndex != null && filteredDiagnostics.isNotEmpty()) {
      val idx = filteredDiagnostics.indexOfFirst { it.dimension.index == selectedDimensionIndex }
      if (idx >= 0) {
        areaSlideDirection = if (idx >= areaPagerIndex) 1 else -1
        areaPagerIndex = idx
      } else {
        areaPagerIndex = 0
      }
    } else {
      areaPagerIndex = 0
    }
  }

  val activeDimIndex = selectedDimensionIndex ?: report.lowestDimension.index
  val activeDim = DimensionsProvider.dimensions.getOrNull(activeDimIndex) ?: report.lowestDimension
  val ambientAccent by animateColorAsState(
    targetValue = activeDim.accentColor,
    animationSpec = tween(durationMillis = 480, easing = FastOutSlowInEasing),
    label = "report_ambient_accent"
  )

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFFFAFAFC))
      .drawBehind {
        val glowCenter = Offset(size.width * 0.5f, size.height * 0.28f)
        drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(
              ambientAccent.copy(alpha = 0.09f),
              ambientAccent.copy(alpha = 0.02f),
              Color.Transparent
            ),
            center = glowCenter,
            radius = size.width * 0.65f
          ),
          radius = size.width * 0.65f,
          center = glowCenter
        )
      }
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding()
        .verticalScroll(scrollState)
        .padding(horizontal = 20.dp, vertical = 10.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .widthIn(max = 600.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // ==========================================
        // 1. ENCABEZADO MINIMALISTA
        // ==========================================
        ReportCompactHeader(
          report = report,
          isProfileCompleted = isProfileCompleted,
          onRetake = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onRetake()
          },
          onContinue = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onContinue()
          }
        )

        // ==========================================
        // 2. CUADRO GRANDE DEL HEXÁGONO (CON SUPERPONER)
        // ==========================================
        AnimatedVisibility(
          visible = entered,
          enter = fadeIn(tween(320)) + slideInVertically(
            tween(340, easing = FastOutSlowInEasing)
          ) { it / 12 }
        ) {
          Surface(
            shape = RoundedCornerShape(26.dp),
            color = SurfaceCardLight,
            border = BorderStroke(1.dp, BorderLight),
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 12.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Text(
                text = "GEOMETRÍA DE TU EQUILIBRIO",
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 1.sp
                ),
                color = TextSecondaryLight
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = if (report.previousScores != null) {
                  "Hexágono actual vs respuestas anteriores superpuestas"
                } else {
                  "Toca un vértice para explorar su diagnóstico abajo"
                },
                style = MaterialTheme.typography.bodySmall,
                color = TextTertiaryLight
              )

              Spacer(modifier = Modifier.height(10.dp))

              ExpandedHexagonRadarChart(
                scores = report.scores,
                previousScores = report.previousScores,
                selectedDimensionIndex = selectedDimensionIndex,
                onDimensionSelect = { dimIndex ->
                  haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                  onDimensionSelect(dimIndex)
                  // Si la dimensión elegida queda oculta por el filtro, vuelve a Todas
                  if (filteredDiagnostics.none { it.dimension.index == dimIndex }) {
                    onFilterChange(ReportFilter.ALL)
                  }
                  activeDeckTab = ReportDeckTab.AREAS
                }
              )
            }
          }
        }

        // ==========================================
        // 3. PENSAMIENTO GUÍA (CONSERVADO Y PULIDO)
        // ==========================================
        AnimatedVisibility(
          visible = entered,
          enter = fadeIn(tween(360, delayMillis = 60)) + slideInVertically(
            tween(360, delayMillis = 60, easing = FastOutSlowInEasing)
          ) { it / 10 }
        ) {
          WisdomQuoteCard(
            quote = report.wisdomQuote,
            author = report.wisdomAuthor
          )
        }

        // ==========================================
        // 4. PANEL INTERACTIVO POR PESTAÑAS (SIN SCROLL LARGO)
        // ==========================================
        ReportDeckSwitcher(
          activeTab = activeDeckTab,
          areasCount = report.diagnostics.size,
          patternsCount = report.patternInsights.size,
          onSelectTab = { tab ->
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            activeDeckTab = tab
          }
        )

        AnimatedContent(
          targetState = activeDeckTab,
          transitionSpec = {
            (fadeIn(tween(240, easing = FastOutSlowInEasing)) +
              slideInVertically(tween(240)) { it / 14 }) togetherWith
              (fadeOut(tween(160, easing = FastOutSlowInEasing)) +
                slideOutVertically(tween(160)) { -it / 16 })
          },
          label = "report_deck_tab_content"
        ) { tab ->
          when (tab) {
            ReportDeckTab.PROFILE -> {
              MinimalArchetypeCard(
                report = report,
                onInspectDimension = { dimIndex ->
                  haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                  onFilterChange(ReportFilter.ALL)
                  onDimensionSelect(dimIndex)
                  activeDeckTab = ReportDeckTab.AREAS
                }
              )
            }

            ReportDeckTab.AREAS -> {
              Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ReportAreaFilterRow(
                  filter = filter,
                  weaknessesCount = report.weaknessesCount,
                  strengthsCount = report.strengthsCount,
                  onFilterChange = { newFilter ->
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    areaPagerIndex = 0
                    onFilterChange(newFilter)
                  }
                )

                if (filteredDiagnostics.isNotEmpty()) {
                  val safeIdx = areaPagerIndex.coerceIn(0, filteredDiagnostics.lastIndex)
                  val currentDiagnostic = filteredDiagnostics[safeIdx]

                  InPlaceDiagnosticExplorerCard(
                    diagnostic = currentDiagnostic,
                    positionIndex = safeIdx,
                    totalCount = filteredDiagnostics.size,
                    slideDirection = areaSlideDirection,
                    activeSubTab = activeSubTab,
                    onSelectSubTab = { sub ->
                      haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                      activeSubTab = sub
                    },
                    onPrevious = {
                      haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                      areaSlideDirection = -1
                      val prevIdx = if (safeIdx - 1 < 0) filteredDiagnostics.lastIndex else safeIdx - 1
                      areaPagerIndex = prevIdx
                      onDimensionSelect(filteredDiagnostics[prevIdx].dimension.index)
                    },
                    onNext = {
                      haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                      areaSlideDirection = 1
                      val nextIdx = (safeIdx + 1) % filteredDiagnostics.size
                      areaPagerIndex = nextIdx
                      onDimensionSelect(filteredDiagnostics[nextIdx].dimension.index)
                    }
                  )
                } else {
                  EmptyFilterCard(
                    onResetFilter = {
                      haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                      onFilterChange(ReportFilter.ALL)
                    }
                  )
                }
              }
            }

            ReportDeckTab.PATTERNS -> {
              if (report.patternInsights.isNotEmpty()) {
                val safePatternIdx = patternPagerIndex.coerceIn(0, report.patternInsights.lastIndex)
                val insight = report.patternInsights[safePatternIdx]

                InPlacePatternPagerCard(
                  insight = insight,
                  positionIndex = safePatternIdx,
                  totalCount = report.patternInsights.size,
                  slideDirection = patternSlideDirection,
                  onPrevious = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    patternSlideDirection = -1
                    patternPagerIndex = if (safePatternIdx - 1 < 0) report.patternInsights.lastIndex else safePatternIdx - 1
                  },
                  onNext = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    patternSlideDirection = 1
                    patternPagerIndex = (safePatternIdx + 1) % report.patternInsights.size
                  }
                )
              } else {
                EmptyPatternsMinimalCard()
              }
            }
          }
        }

        // ==========================================
        // 5. ACCIÓN FINAL COMPACTA
        // ==========================================
        val continueFooterInteraction = rememberBounceInteractionSource()
        Button(
          onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onContinue()
          },
          interactionSource = continueFooterInteraction,
          shape = RoundedCornerShape(18.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = AccentCharcoal,
            contentColor = Color.White
          ),
          modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .bouncyScale(continueFooterInteraction, scaleDown = 0.96f)
            .testTag("btn_continue_footer")
        ) {
          Text(
            text = if (isProfileCompleted) "Volver al Panel Principal" else "Continuar al Perfil y Metas",
            style = MaterialTheme.typography.labelLarge.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp
            )
          )
          Spacer(modifier = Modifier.width(8.dp))
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
          )
        }

        Text(
          text = "Equilibrio · 6 Dimensiones Vitales",
          style = MaterialTheme.typography.labelSmall,
          color = TextTertiaryLight,
          modifier = Modifier
            .align(Alignment.CenterHorizontally)
            .padding(bottom = 8.dp)
        )
      }
    }
  }
}

@Composable
private fun ReportCompactHeader(
  report: AssessmentReport,
  isProfileCompleted: Boolean,
  onRetake: () -> Unit,
  onContinue: () -> Unit
) {
  val retakeInteraction = rememberBounceInteractionSource()
  val continueInteraction = rememberBounceInteractionSource()

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(top = 4.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = "Estadística Hexagonal",
        style = MaterialTheme.typography.headlineSmall.copy(
          fontWeight = FontWeight.Bold,
          fontSize = 22.sp,
          letterSpacing = (-0.4).sp
        ),
        color = TextPrimaryLight,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = if (report.previousScores != null) {
          "Comparativa · ${report.harmonyPercentage}% armonía"
        } else {
          "${report.harmonyPercentage}% armonía · Promedio ${String.format("%.1f", report.averageScore)}/10"
        },
        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
        color = TextSecondaryLight,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }

    Row(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Surface(
        onClick = onRetake,
        interactionSource = retakeInteraction,
        shape = RoundedCornerShape(14.dp),
        color = SurfaceCardLight,
        border = BorderStroke(1.dp, BorderLight),
        modifier = Modifier
          .minimumInteractiveComponentSize()
          .size(42.dp)
          .bouncyScale(retakeInteraction, scaleDown = 0.90f)
          .testTag("btn_retake_header")
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = "Repetir evaluación",
            tint = TextPrimaryLight,
            modifier = Modifier.size(18.dp)
          )
        }
      }

      Button(
        onClick = onContinue,
        interactionSource = continueInteraction,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = AccentCharcoal,
          contentColor = Color.White
        ),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
        modifier = Modifier
          .height(42.dp)
          .bouncyScale(continueInteraction, scaleDown = 0.93f)
          .testTag("btn_continue_header")
      ) {
        Text(
          text = if (isProfileCompleted) "Panel" else "Continuar",
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
        )
      }
    }
  }
}

@Composable
private fun WisdomQuoteCard(quote: String, author: String) {
  Surface(
    shape = RoundedCornerShape(20.dp),
    color = SurfaceCardLight,
    border = BorderStroke(1.dp, BorderLight),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.FormatQuote,
          contentDescription = null,
          tint = AccentCharcoal,
          modifier = Modifier.size(17.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "PENSAMIENTO GUÍA",
          style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
          ),
          color = TextSecondaryLight
        )
      }

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = "“$quote”",
        style = MaterialTheme.typography.bodyMedium.copy(
          fontStyle = FontStyle.Italic,
          fontWeight = FontWeight.Medium,
          lineHeight = 20.sp
        ),
        color = TextPrimaryLight
      )

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = "— $author",
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
        color = TextSecondaryLight,
        modifier = Modifier.align(Alignment.End)
      )
    }
  }
}

@Composable
private fun ReportDeckSwitcher(
  activeTab: ReportDeckTab,
  areasCount: Int,
  patternsCount: Int,
  onSelectTab: (ReportDeckTab) -> Unit
) {
  Surface(
    shape = RoundedCornerShape(16.dp),
    color = SurfaceVariantLight.copy(alpha = 0.9f),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(4.dp),
      horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      ReportDeckTab.entries.forEach { tab ->
        val selected = tab == activeTab
        val interaction = rememberBounceInteractionSource()
        val bg by animateColorAsState(
          targetValue = if (selected) SurfaceCardLight else Color.Transparent,
          animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
          label = "report_tab_bg_${tab.name}"
        )
        val label = when (tab) {
          ReportDeckTab.PROFILE -> "Perfil"
          ReportDeckTab.AREAS -> "Áreas ($areasCount)"
          ReportDeckTab.PATTERNS -> if (patternsCount > 0) "Patrones ($patternsCount)" else "Patrones"
        }

        Surface(
          onClick = { onSelectTab(tab) },
          interactionSource = interaction,
          shape = RoundedCornerShape(12.dp),
          color = bg,
          shadowElevation = if (selected) 1.dp else 0.dp,
          modifier = Modifier
            .weight(1f)
            .height(38.dp)
            .bouncyScale(interaction, scaleDown = 0.94f)
            .testTag("report_tab_${tab.name.lowercase()}")
        ) {
          Box(contentAlignment = Alignment.Center) {
            Text(
              text = label,
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
              ),
              color = if (selected) TextPrimaryLight else TextSecondaryLight,
              maxLines = 1
            )
          }
        }
      }
    }
  }
}

@Composable
private fun MinimalArchetypeCard(
  report: AssessmentReport,
  onInspectDimension: (Int) -> Unit
) {
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
      verticalArrangement = Arrangement.spacedBy(10.dp)
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
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "PERFIL GENERAL",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              letterSpacing = 0.9.sp
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
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = TextPrimaryLight,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
          )
        }
      }

      Text(
        text = report.archetypeTitle,
        style = MaterialTheme.typography.titleLarge.copy(
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold
        ),
        color = TextPrimaryLight
      )

      Text(
        text = report.archetypeSummary,
        style = MaterialTheme.typography.bodyMedium,
        color = TextSecondaryLight,
        lineHeight = 20.sp
      )

      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(14.dp))
          .background(SurfaceVariantLight)
          .padding(12.dp)
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
            style = MaterialTheme.typography.bodySmall.copy(
              fontWeight = FontWeight.Medium,
              lineHeight = 18.sp
            ),
            color = TextPrimaryLight
          )
        }
      }

      // 3 KPIs táctiles: tocar Pilar Fuerte o Foco Débil abre directamente su detalle en Áreas
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
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

        KpiMiniCard(
          title = "Pilar Fuerte",
          value = report.highestDimension.title,
          sub = "${report.scores[report.highestDimension.index]}/10 · Ver",
          accentColor = report.highestDimension.accentColor,
          onClick = { onInspectDimension(report.highestDimension.index) },
          modifier = Modifier.weight(1f)
        )

        KpiMiniCard(
          title = "Foco Débil",
          value = report.lowestDimension.title,
          sub = "${report.scores[report.lowestDimension.index]}/10 · Ver",
          accentColor = Color(0xFFEF4444),
          onClick = { onInspectDimension(report.lowestDimension.index) },
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
  onClick: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val interaction = rememberBounceInteractionSource()
  Surface(
    onClick = { onClick?.invoke() },
    enabled = onClick != null,
    interactionSource = interaction,
    shape = RoundedCornerShape(14.dp),
    color = SurfaceVariantLight,
    modifier = modifier.then(
      if (onClick != null) Modifier.bouncyScale(interaction, scaleDown = 0.94f) else Modifier
    )
  ) {
    Column(modifier = Modifier.padding(10.dp)) {
      Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        color = TextTertiaryLight,
        maxLines = 1
      )
      Spacer(modifier = Modifier.height(3.dp))
      Text(
        text = value,
        style = MaterialTheme.typography.titleSmall.copy(
          fontWeight = FontWeight.Bold,
          fontFeatureSettings = "tnum"
        ),
        color = accentColor ?: TextPrimaryLight,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
      Text(
        text = sub,
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
        color = subColor,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}

@Composable
private fun ReportAreaFilterRow(
  filter: ReportFilter,
  weaknessesCount: Int,
  strengthsCount: Int,
  onFilterChange: (ReportFilter) -> Unit
) {
  Row(
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    modifier = Modifier.fillMaxWidth()
  ) {
    ReportFilterPill(
      label = "Todas (6)",
      selected = filter == ReportFilter.ALL,
      onClick = { onFilterChange(ReportFilter.ALL) },
      modifier = Modifier.weight(1f)
    )
    ReportFilterPill(
      label = "Debilidades ($weaknessesCount)",
      selected = filter == ReportFilter.WEAKNESSES,
      onClick = { onFilterChange(ReportFilter.WEAKNESSES) },
      modifier = Modifier.weight(1f)
    )
    ReportFilterPill(
      label = "Fortalezas ($strengthsCount)",
      selected = filter == ReportFilter.STRENGTHS,
      onClick = { onFilterChange(ReportFilter.STRENGTHS) },
      modifier = Modifier.weight(1f)
    )
  }
}

@Composable
private fun ReportFilterPill(
  label: String,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val interaction = rememberBounceInteractionSource()
  val bg by animateColorAsState(
    targetValue = if (selected) AccentCharcoal else SurfaceCardLight,
    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
    label = "report_filter_bg_$label"
  )

  Surface(
    onClick = onClick,
    interactionSource = interaction,
    shape = RoundedCornerShape(12.dp),
    color = bg,
    border = BorderStroke(1.dp, if (selected) AccentCharcoal else BorderLight),
    modifier = modifier
      .height(36.dp)
      .bouncyScale(interaction, scaleDown = 0.94f)
  ) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 6.dp)) {
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall.copy(
          fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold
        ),
        color = if (selected) Color.White else TextPrimaryLight,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}

@Composable
private fun InPlaceDiagnosticExplorerCard(
  diagnostic: DimensionDiagnostic,
  positionIndex: Int,
  totalCount: Int,
  slideDirection: Int,
  activeSubTab: DiagnosticDetailSubTab,
  onSelectSubTab: (DiagnosticDetailSubTab) -> Unit,
  onPrevious: () -> Unit,
  onNext: () -> Unit
) {
  val prevInteraction = rememberBounceInteractionSource()
  val nextInteraction = rememberBounceInteractionSource()

  val accentBorder by animateColorAsState(
    targetValue = diagnostic.dimension.accentColor.copy(alpha = 0.35f),
    animationSpec = tween(320),
    label = "diag_card_border"
  )

  Surface(
    shape = RoundedCornerShape(24.dp),
    color = SurfaceCardLight,
    border = BorderStroke(1.dp, accentBorder),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("diagnostic_card_${diagnostic.dimension.index}")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      // Cabecera con navegación in-place (< 1 / 6 >)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        AnimatedContent(
          targetState = diagnostic,
          transitionSpec = {
            val offset = if (slideDirection >= 0) 40 else -40
            (fadeIn(tween(220)) + slideInHorizontally(tween(220)) { offset }) togetherWith
              (fadeOut(tween(150)) + slideOutHorizontally(tween(150)) { -offset / 2 })
          },
          modifier = Modifier.weight(1f),
          label = "diag_header_slide"
        ) { diag ->
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(diag.dimension.accentColor.copy(alpha = 0.12f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = diag.dimension.icon,
                contentDescription = null,
                tint = diag.dimension.accentColor,
                modifier = Modifier.size(20.dp)
              )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = diag.dimension.title,
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                  color = TextPrimaryLight,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )

                if (diag.previousScore != null) {
                  val delta = diag.score - diag.previousScore
                  if (delta != 0) {
                    Spacer(modifier = Modifier.width(6.dp))
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
                          modifier = Modifier.size(11.dp)
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
                text = "${diag.score}/10 · ${diag.tier.label}",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = Color(diag.tier.badgeColorHex),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          }
        }

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
                .testTag("btn_diag_prev")
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                  contentDescription = "Área anterior",
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
              modifier = Modifier.padding(horizontal = 4.dp)
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
                .testTag("btn_diag_next")
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                  contentDescription = "Siguiente área",
                  tint = TextPrimaryLight,
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          }
        }
      }

      // Selector interno compacto para evitar apilar bloques largos
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(SurfaceVariantLight.copy(alpha = 0.7f))
          .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        DiagnosticDetailSubTab.entries.forEach { subTab ->
          val selected = subTab == activeSubTab
          val subInteraction = rememberBounceInteractionSource()
          val subBg by animateColorAsState(
            targetValue = if (selected) SurfaceCardLight else Color.Transparent,
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
            label = "diag_subtab_bg_${subTab.name}"
          )
          Surface(
            onClick = { onSelectSubTab(subTab) },
            interactionSource = subInteraction,
            shape = RoundedCornerShape(9.dp),
            color = subBg,
            shadowElevation = if (selected) 1.dp else 0.dp,
            modifier = Modifier
              .weight(1f)
              .height(32.dp)
              .bouncyScale(subInteraction, scaleDown = 0.94f)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Text(
                text = subTab.label,
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                ),
                color = if (selected) TextPrimaryLight else TextSecondaryLight,
                maxLines = 1
              )
            }
          }
        }
      }

      // Contenido animado del sub-apartado seleccionado
      AnimatedContent(
        targetState = Pair(diagnostic, activeSubTab),
        transitionSpec = {
          (fadeIn(tween(220, easing = FastOutSlowInEasing)) +
            slideInVertically(tween(220)) { it / 12 }) togetherWith
            (fadeOut(tween(150)) + slideOutVertically(tween(150)) { -it / 12 })
        },
        label = "diag_sub_content"
      ) { (diag, sub) ->
        when (sub) {
          DiagnosticDetailSubTab.SUMMARY -> {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
              Text(
                text = diag.diagnosisSummary,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = TextPrimaryLight,
                lineHeight = 20.sp
              )

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
                  Spacer(modifier = Modifier.height(3.dp))
                  Text(
                    text = diag.corePhilosophy,
                    style = MaterialTheme.typography.bodySmall.copy(
                      fontStyle = FontStyle.Italic,
                      fontWeight = FontWeight.Medium,
                      lineHeight = 18.sp
                    ),
                    color = TextPrimaryLight
                  )
                }
              }

              Text(
                text = "Impacto diario: ${diag.currentImpact}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondaryLight,
                lineHeight = 18.sp
              )
            }
          }

          DiagnosticDetailSubTab.BLIND_SPOTS -> {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceVariantLight)
                .padding(14.dp)
            ) {
              Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                  text = "PUNTOS CIEGOS DETECTADOS",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                  ),
                  color = if (diag.score <= 5) Color(0xFFDC2626) else TextSecondaryLight
                )
                diag.blindSpots.forEach { spot ->
                  Row(verticalAlignment = Alignment.Top) {
                    Box(
                      modifier = Modifier
                        .padding(top = 6.dp, end = 8.dp)
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(diag.dimension.accentColor)
                    )
                    Text(
                      text = spot,
                      style = MaterialTheme.typography.bodySmall,
                      color = TextPrimaryLight,
                      lineHeight = 18.sp
                    )
                  }
                }
              }
            }
          }

          DiagnosticDetailSubTab.ACTIONS -> {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(diag.dimension.accentColor.copy(alpha = 0.08f))
                .border(
                  1.dp,
                  diag.dimension.accentColor.copy(alpha = 0.25f),
                  RoundedCornerShape(16.dp)
                )
                .padding(14.dp)
            ) {
              Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                  text = "PASOS PRÁCTICOS DE MEJORA",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                  ),
                  color = diag.dimension.accentColor
                )
                diag.actionableImprovements.forEach { action ->
                  Row(verticalAlignment = Alignment.Top) {
                    Icon(
                      imageVector = Icons.Outlined.Check,
                      contentDescription = null,
                      tint = diag.dimension.accentColor,
                      modifier = Modifier
                        .padding(top = 2.dp, end = 6.dp)
                        .size(15.dp)
                    )
                    Text(
                      text = action,
                      style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                      color = TextPrimaryLight,
                      lineHeight = 18.sp
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
private fun InPlacePatternPagerCard(
  insight: PatternInsight,
  positionIndex: Int,
  totalCount: Int,
  slideDirection: Int,
  onPrevious: () -> Unit,
  onNext: () -> Unit
) {
  val prevInteraction = rememberBounceInteractionSource()
  val nextInteraction = rememberBounceInteractionSource()

  val borderColor = if (insight.isAlert) Color(0xFFFCA5A5) else BorderLight
  val bgTint = if (insight.isAlert) Color(0xFFFEF2F2) else SurfaceCardLight

  Surface(
    shape = RoundedCornerShape(24.dp),
    color = bgTint,
    border = BorderStroke(1.dp, borderColor),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
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
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = insight.tag.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              letterSpacing = 0.8.sp
            ),
            color = if (insight.isAlert) Color(0xFFDC2626) else TextSecondaryLight
          )
        }

        if (totalCount > 1) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
              onClick = onPrevious,
              interactionSource = prevInteraction,
              shape = CircleShape,
              color = if (insight.isAlert) Color.White else SurfaceVariantLight,
              modifier = Modifier
                .minimumInteractiveComponentSize()
                .size(34.dp)
                .bouncyScale(prevInteraction, scaleDown = 0.88f)
                .testTag("btn_pattern_prev")
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                  contentDescription = "Patrón anterior",
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
              modifier = Modifier.padding(horizontal = 4.dp)
            )

            Surface(
              onClick = onNext,
              interactionSource = nextInteraction,
              shape = CircleShape,
              color = if (insight.isAlert) Color.White else SurfaceVariantLight,
              modifier = Modifier
                .minimumInteractiveComponentSize()
                .size(34.dp)
                .bouncyScale(nextInteraction, scaleDown = 0.88f)
                .testTag("btn_pattern_next")
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                  contentDescription = "Siguiente patrón",
                  tint = TextPrimaryLight,
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          }
        }
      }

      AnimatedContent(
        targetState = insight,
        transitionSpec = {
          val offset = if (slideDirection >= 0) 50 else -50
          (fadeIn(tween(220)) + slideInHorizontally(tween(220)) { offset }) togetherWith
            (fadeOut(tween(150)) + slideOutHorizontally(tween(150)) { -offset / 2 })
        },
        label = "report_pattern_slide"
      ) { item ->
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = item.title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = TextPrimaryLight
          )
          Text(
            text = item.subtitle,
            style = MaterialTheme.typography.labelMedium,
            color = if (item.isAlert) Color(0xFFB91C1C) else TextSecondaryLight
          )
          Text(
            text = item.description,
            style = MaterialTheme.typography.bodySmall,
            color = TextPrimaryLight,
            lineHeight = 19.sp
          )
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(14.dp))
              .background(if (item.isAlert) Color.White else SurfaceVariantLight)
              .padding(12.dp)
          ) {
            Row(verticalAlignment = Alignment.Top) {
              Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = if (item.isAlert) Color(0xFFDC2626) else AccentCharcoal,
                modifier = Modifier
                  .size(16.dp)
                  .padding(top = 2.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = item.recommendation,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = TextPrimaryLight,
                lineHeight = 18.sp
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun EmptyFilterCard(onResetFilter: () -> Unit) {
  val interaction = rememberBounceInteractionSource()
  Surface(
    shape = RoundedCornerShape(22.dp),
    color = SurfaceCardLight,
    border = BorderStroke(1.dp, BorderLight),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier.padding(16.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
        Icon(
          imageVector = Icons.Outlined.AutoAwesome,
          contentDescription = null,
          tint = AccentCharcoal,
          modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
          text = "No hay áreas en este filtro.",
          style = MaterialTheme.typography.bodyMedium,
          color = TextSecondaryLight
        )
      }
      Surface(
        onClick = onResetFilter,
        interactionSource = interaction,
        shape = RoundedCornerShape(12.dp),
        color = SurfaceVariantLight,
        modifier = Modifier.bouncyScale(interaction)
      ) {
        Text(
          text = "Ver todas",
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
          color = AccentCharcoal,
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
      }
    }
  }
}

@Composable
private fun EmptyPatternsMinimalCard() {
  Surface(
    shape = RoundedCornerShape(22.dp),
    color = SurfaceCardLight,
    border = BorderStroke(1.dp, BorderLight),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier.padding(16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = Icons.Outlined.AutoAwesome,
        contentDescription = null,
        tint = AccentCharcoal,
        modifier = Modifier.size(22.dp)
      )
      Spacer(modifier = Modifier.width(10.dp))
      Text(
        text = "Tus seis áreas mantienen un balance estable sin fricciones críticas cruzadas.",
        style = MaterialTheme.typography.bodySmall,
        color = TextSecondaryLight,
        lineHeight = 18.sp
      )
    }
  }
}
