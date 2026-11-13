package com.example.ui.screens

import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.FormatQuote
import androidx.compose.material.icons.outlined.Hexagon
import androidx.compose.material.icons.outlined.Info
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.domain.HexagonCalculator
import com.example.model.AssessmentReport
import com.example.model.BalanceMission
import com.example.model.DimensionsProvider
import com.example.model.GamificationStats
import com.example.model.LifeDimension
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

private enum class HoyDeckMode(val label: String) {
  FOCUS("Enfoque"),
  TODAY("Hoy"),
  PULSE("Pulso")
}

private data class BalancePhrase(
  val quote: String,
  val author: String
)

/**
 * Pantalla "Hoy" minimalista, con frases interactivas, sin scroll vertical largo
 * y con el hexágono flotando en composición asimétrica con aura ambiental animada.
 */
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
  liveHarmonyPercentage: Int = HexagonCalculator.calculateLiveHarmonyPercentage(realScores, report.scores),
  onOpenFullReport: () -> Unit = {},
  onEditGoal: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val haptic = LocalHapticFeedback.current
  val dimensions = DimensionsProvider.dimensions
  val lowestDimIndex = remember(realScores, report.scores) {
    HexagonCalculator.findLowestRealDimension(realScores, report.scores)
  }

  var focusedDimIndex by remember(lowestDimIndex) { mutableIntStateOf(lowestDimIndex) }
  var activeDeckMode by remember { mutableStateOf(HoyDeckMode.FOCUS) }
  var phraseIndex by remember { mutableIntStateOf(0) }
  var phraseRefreshCount by remember { mutableIntStateOf(0) }
  var missionCycleOffset by remember { mutableIntStateOf(0) }
  var cycleDirection by remember { mutableIntStateOf(1) }

  // Sincroniza foco si el detalle del hexágono cambia externamente
  LaunchedEffect(selectedDimensionForDetail) {
    if (selectedDimensionForDetail != null && selectedDimensionForDetail in 0..5) {
      focusedDimIndex = selectedDimensionForDetail
    }
  }

  // Al cambiar de dimensión enfocada, reinicia el índice del carrusel de enfoque suavemente
  LaunchedEffect(focusedDimIndex) {
    missionCycleOffset = 0
  }

  val focusedDim = dimensions.getOrNull(focusedDimIndex) ?: dimensions[0]
  val hasRealData = remember(realScoresFloat) { realScoresFloat.values.any { it > 0.05f } }

  val animatedHarmony by animateIntAsState(
    targetValue = liveHarmonyPercentage,
    animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
    label = "hoy_animated_harmony"
  )

  val ambientColor by animateColorAsState(
    targetValue = focusedDim.accentColor,
    animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
    label = "hoy_ambient_color"
  )

  // Frases minimalistas + cita del diagnóstico + sugerencia amable para la dimensión enfocada
  val phrases = remember(report.wisdomQuote, report.wisdomAuthor, focusedDimIndex) {
    listOf(
      BalancePhrase(
        quote = "Aquí hay espacio para crecer: ${focusedDim.title}. Un paso breve hoy cambia tu semana.",
        author = "Brújula diaria"
      ),
      BalancePhrase(
        quote = report.wisdomQuote,
        author = report.wisdomAuthor
      ),
      BalancePhrase(
        quote = "La constancia suave vence a la exigencia extrema. Haz una cosa con calma.",
        author = "Equilibrio"
      ),
      BalancePhrase(
        quote = "No busques perfección hoy; busca presencia en lo que elijas cuidar.",
        author = "Serenidad"
      ),
      BalancePhrase(
        quote = "Pequeñas pausas conscientes sostienen grandes metas sin agotar tu mente.",
        author = "Ritmo propio"
      )
    )
  }

  val currentPhrase = phrases[phraseIndex % phrases.size]

  // Misiones ordenadas priorizando la dimensión enfocada y las pendientes
  val orderedFocusMissions = remember(allMissions, focusedDimIndex, todayCompletedIds) {
    val uncompletedFocused = allMissions.filter { it.dimensionIndex == focusedDimIndex && it.id !in todayCompletedIds }
    val uncompletedOthers = allMissions.filter { it.dimensionIndex != focusedDimIndex && it.id !in todayCompletedIds }
    val completed = allMissions.filter { it.id in todayCompletedIds }
    uncompletedFocused + uncompletedOthers + completed
  }

  val pendingCount = remember(allMissions, todayCompletedIds) {
    allMissions.count { it.id !in todayCompletedIds }
  }

  val scrollFallback = rememberScrollState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(scrollFallback)
      .padding(horizontal = 20.dp)
      .padding(top = 10.dp, bottom = 94.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .widthIn(max = 600.dp)
    ) {
      // ==========================================
      // 1. ENCABEZADO COMPACTO Y SERENO
      // ==========================================
      HoyCompactHeader(
        userProfile = userProfile,
        harmonyPercentage = animatedHarmony,
        onAvatarClick = {
          haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
          activeDeckMode = HoyDeckMode.PULSE
        }
      )

      Spacer(modifier = Modifier.height(10.dp))

      // ==========================================
      // 2. TIRA DE FRASES INTERACTIVA (CON ANIMACIÓN)
      // ==========================================
      InteractivePhraseBanner(
        phrase = currentPhrase,
        accentColor = ambientColor,
        refreshSpinCount = phraseRefreshCount,
        onNextPhrase = {
          haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
          phraseIndex = (phraseIndex + 1) % phrases.size
          phraseRefreshCount++
        }
      )

      Spacer(modifier = Modifier.height(12.dp))

      // ==========================================
      // 3. ESCENARIO ASIMÉTRICO DEL HEXÁGONO (SIN CUADRO ENORME)
      // ==========================================
      FloatingHexagonStage(
        perceivedScores = report.scores,
        realScores = realScores,
        realScoresFloat = realScoresFloat,
        hasRealData = hasRealData,
        animatedHarmony = animatedHarmony,
        focusedDim = focusedDim,
        focusedDimIndex = focusedDimIndex,
        ambientColor = ambientColor,
        selectedDimensionForDetail = selectedDimensionForDetail,
        onFocusDimension = { idx ->
          haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
          focusedDimIndex = idx
        },
        onOpenDimensionDetail = { idx ->
          haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
          focusedDimIndex = idx
          onSelectDimension(idx)
        }
      )

      Spacer(modifier = Modifier.height(8.dp))

      // Tira horizontal de 6 vértices interactivos para explorar sin scroll
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 2.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        items(dimensions, key = { it.index }) { dim ->
          val isSelected = dim.index == focusedDimIndex
          val realVal = realScores[dim.index] ?: 0
          val perceivedVal = report.scores[dim.index] ?: 5
          DimensionOrbitPill(
            dimension = dim,
            isSelected = isSelected,
            scoreLabel = if (hasRealData) "$realVal/$perceivedVal" else "$perceivedVal",
            onClick = {
              haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              focusedDimIndex = dim.index
            }
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // ==========================================
      // 4. PANEL DE ACCIÓN INTERACTIVO (SIN SCROLL HACIA ABAJO)
      // ==========================================
      HoyDeckSwitcher(
        activeMode = activeDeckMode,
        pendingCount = pendingCount,
        onSelectMode = { mode ->
          haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
          activeDeckMode = mode
        }
      )

      Spacer(modifier = Modifier.height(10.dp))

      AnimatedContent(
        targetState = activeDeckMode,
        transitionSpec = {
          (fadeIn(tween(240, easing = FastOutSlowInEasing)) +
            slideInVertically(tween(240)) { it / 12 }) togetherWith
            (fadeOut(tween(170, easing = FastOutSlowInEasing)) +
              slideOutVertically(tween(170)) { -it / 16 })
        },
        label = "hoy_deck_mode_content"
      ) { mode ->
        when (mode) {
          HoyDeckMode.FOCUS -> {
            if (orderedFocusMissions.isNotEmpty()) {
              val safeIndex = ((missionCycleOffset % orderedFocusMissions.size) + orderedFocusMissions.size) % orderedFocusMissions.size
              val activeMission = orderedFocusMissions[safeIndex]
              val isCompleted = activeMission.id in todayCompletedIds || activeMission.isCompleted

              FocusMissionInteractiveDeckCard(
                mission = activeMission,
                isCompleted = isCompleted,
                positionIndex = safeIndex,
                totalCount = orderedFocusMissions.size,
                direction = cycleDirection,
                onPrevious = {
                  haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                  cycleDirection = -1
                  missionCycleOffset--
                },
                onNext = {
                  haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                  cycleDirection = 1
                  missionCycleOffset++
                },
                onViewDetails = { onViewMissionDetails(activeMission) }
              )
            } else {
              AllDoneMinimalBanner(onOpenFullReport = onOpenFullReport)
            }
          }

          HoyDeckMode.TODAY -> {
            if (orderedFocusMissions.isNotEmpty()) {
              LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(horizontal = 2.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                items(orderedFocusMissions, key = { it.id }) { mission ->
                  val isCompleted = mission.id in todayCompletedIds || mission.isCompleted
                  HoyCarouselMissionCard(
                    mission = mission,
                    isCompleted = isCompleted,
                    onViewDetails = { onViewMissionDetails(mission) }
                  )
                }
              }
            } else {
              AllDoneMinimalBanner(onOpenFullReport = onOpenFullReport)
            }
          }

          HoyDeckMode.PULSE -> {
            HoyPulseCard(
              userProfile = userProfile,
              onEditGoal = onEditGoal,
              onOpenFullReport = onOpenFullReport
            )
          }
        }
      }
    }
  }
}

@Composable
private fun HoyCompactHeader(
  userProfile: UserProfile,
  harmonyPercentage: Int,
  onAvatarClick: () -> Unit
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
        text = "Armonía $harmonyPercentage% · Nivel ${userProfile.level}",
        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
        color = TextSecondaryLight
      )
    }

    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      // Píldora minimalista de racha
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = SurfaceCardLight,
        border = BorderStroke(1.dp, BorderLight)
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Outlined.LocalFireDepartment,
            contentDescription = "Racha activa",
            tint = if (userProfile.activeStreakDays > 0) Color(0xFFD97706) else TextTertiaryLight,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "${userProfile.activeStreakDays}d",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = TextPrimaryLight
          )
        }
      }

      // Avatar táctil con rebote
      Surface(
        onClick = onAvatarClick,
        interactionSource = avatarInteraction,
        shape = CircleShape,
        color = SurfaceVariantLight,
        border = BorderStroke(1.5.dp, BorderLight),
        modifier = Modifier
          .size(48.dp)
          .bouncyScale(avatarInteraction, scaleDown = 0.90f)
          .testTag("btn_hoy_avatar_pulse")
      ) {
        Box(contentAlignment = Alignment.Center) {
          if (userProfile.photoUri != null) {
            AsyncImage(
              model = Uri.parse(userProfile.photoUri),
              contentDescription = "Ver pulso de perfil",
              contentScale = ContentScale.Crop,
              modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
            )
          } else {
            Icon(
              imageVector = Icons.Filled.Person,
              contentDescription = "Ver pulso de perfil",
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
private fun InteractivePhraseBanner(
  phrase: BalancePhrase,
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
    color = SurfaceCardLight.copy(alpha = 0.92f),
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
          (fadeIn(tween(280, easing = FastOutSlowInEasing)) +
            slideInVertically(tween(280)) { it / 3 }) togetherWith
            (fadeOut(tween(180, easing = FastOutSlowInEasing)) +
              slideOutVertically(tween(180)) { -it / 3 })
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
              lineHeight = 17.sp
            ),
            color = TextPrimaryLight,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "${item.author} · Toca para cambiar frase",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = TextTertiaryLight,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }

      Box(
        modifier = Modifier
          .minimumInteractiveComponentSize()
          .size(32.dp),
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

@Composable
private fun FloatingHexagonStage(
  perceivedScores: Map<Int, Int>,
  realScores: Map<Int, Int>,
  realScoresFloat: Map<Int, Float>,
  hasRealData: Boolean,
  animatedHarmony: Int,
  focusedDim: LifeDimension,
  focusedDimIndex: Int,
  ambientColor: Color,
  selectedDimensionForDetail: Int?,
  onFocusDimension: (Int) -> Unit,
  onOpenDimensionDetail: (Int) -> Unit
) {
  // Respiración orgánica sutil para que el hexágono flotante se sienta vivo
  val infiniteTransition = rememberInfiniteTransition(label = "hex_breathe")
  val breatheScale by infiniteTransition.animateFloat(
    initialValue = 0.988f,
    targetValue = 1.016f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 3600, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "hex_breathe_scale"
  )

  val inspectInteraction = rememberBounceInteractionSource()
  val perceivedVal = perceivedScores[focusedDimIndex] ?: 5
  val realVal = realScores[focusedDimIndex] ?: 0

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .drawBehind {
        // Aura radial ambiental suave detrás del hexágono en el lado derecho
        val auraCenter = Offset(size.width * 0.72f, size.height * 0.48f)
        drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(
              ambientColor.copy(alpha = 0.14f),
              ambientColor.copy(alpha = 0.04f),
              Color.Transparent
            ),
            center = auraCenter,
            radius = size.width * 0.46f
          ),
          radius = size.width * 0.46f,
          center = auraCenter
        )
      }
      .padding(vertical = 4.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Lado izquierdo: Armonía, leyenda minimalista e inspector vivo de la dimensión enfocada
      Column(
        modifier = Modifier
          .weight(0.43f)
          .padding(end = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Column {
          Text(
            text = "$animatedHarmony%",
            style = MaterialTheme.typography.displaySmall.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 32.sp,
              letterSpacing = (-1).sp,
              fontFeatureSettings = "tnum"
            ),
            color = TextPrimaryLight
          )
          Text(
            text = "Armonía vital",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = TextSecondaryLight
          )
        }

        // Leyenda compacta de capas del hexágono
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(if (hasRealData) AccentCharcoal else TextTertiaryLight.copy(alpha = 0.4f))
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (hasRealData) "Real (14d)" else "Real (sin datos)",
              style = MaterialTheme.typography.labelSmall,
              color = if (hasRealData) TextPrimaryLight else TextTertiaryLight,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .width(12.dp)
                .height(2.5.dp)
                .background(Color(0xFF8E95A2), shape = RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Percibido",
              style = MaterialTheme.typography.labelSmall,
              color = TextSecondaryLight,
              maxLines = 1
            )
          }
        }

        // Tarjeta flotante minimalista del vértice seleccionado (actualiza con transición fluida)
        AnimatedContent(
          targetState = focusedDim,
          transitionSpec = {
            (fadeIn(tween(220, easing = FastOutSlowInEasing)) +
              slideInVertically(tween(220)) { it / 4 }) togetherWith
              (fadeOut(tween(150)) + slideOutVertically(tween(150)) { -it / 4 })
          },
          label = "hoy_focus_inspector"
        ) { dim ->
          Surface(
            onClick = { onOpenDimensionDetail(dim.index) },
            interactionSource = inspectInteraction,
            shape = RoundedCornerShape(16.dp),
            color = SurfaceCardLight.copy(alpha = 0.94f),
            border = BorderStroke(1.dp, dim.accentColor.copy(alpha = 0.28f)),
            modifier = Modifier
              .fillMaxWidth()
              .bouncyScale(inspectInteraction, scaleDown = 0.94f)
              .testTag("btn_hoy_inspect_dimension_${dim.index}")
          ) {
            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(dim.accentColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = dim.title,
                  style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                  color = TextPrimaryLight,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }
              Spacer(modifier = Modifier.height(3.dp))
              Text(
                text = if (hasRealData) "Real $realVal · Base $perceivedVal" else "Base $perceivedVal/10 · Día 1",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = TextSecondaryLight,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              Spacer(modifier = Modifier.height(5.dp))
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = "Analizar vértice",
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                  color = dim.accentColor
                )
                Spacer(modifier = Modifier.width(2.dp))
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                  contentDescription = null,
                  tint = dim.accentColor,
                  modifier = Modifier.size(14.dp)
                )
              }
            }
          }
        }
      }

      // Lado derecho: Hexágono flotante de dos capas sin caja pesada
      Box(
        modifier = Modifier
          .weight(0.57f)
          .graphicsLayer {
            scaleX = breatheScale
            scaleY = breatheScale
          },
        contentAlignment = Alignment.Center
      ) {
        DualLayerHexagonRadarChart(
          perceivedScores = perceivedScores,
          realScores = realScoresFloat,
          selectedDimensionIndex = selectedDimensionForDetail ?: focusedDimIndex,
          onDimensionSelect = { idx ->
            onFocusDimension(idx)
            onOpenDimensionDetail(idx)
          },
          chartSize = 192.dp,
          showLegend = false,
          showChips = false
        )
      }
    }
  }

  if (!hasRealData) {
    Text(
      text = "Tu hexágono real se dibuja con lo que hagas esta semana",
      style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
      color = TextTertiaryLight,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      modifier = Modifier.padding(top = 2.dp)
    )
  }
}

@Composable
private fun DimensionOrbitPill(
  dimension: LifeDimension,
  isSelected: Boolean,
  scoreLabel: String,
  onClick: () -> Unit
) {
  val interaction = rememberBounceInteractionSource()
  val bg by animateColorAsState(
    targetValue = if (isSelected) AccentCharcoal else SurfaceCardLight,
    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
    label = "orbit_pill_bg_${dimension.index}"
  )
  val border by animateColorAsState(
    targetValue = if (isSelected) AccentCharcoal else BorderLight,
    label = "orbit_pill_border_${dimension.index}"
  )

  Surface(
    onClick = onClick,
    interactionSource = interaction,
    shape = RoundedCornerShape(14.dp),
    color = bg,
    border = BorderStroke(1.dp, border),
    modifier = Modifier
      .height(36.dp)
      .bouncyScale(interaction, scaleDown = 0.93f)
      .testTag("hoy_dim_pill_${dimension.index}")
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 10.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(7.dp)
          .clip(CircleShape)
          .background(dimension.accentColor)
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = dimension.title,
        style = MaterialTheme.typography.labelSmall.copy(
          fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
        ),
        color = if (isSelected) Color.White else TextPrimaryLight
      )
      Spacer(modifier = Modifier.width(5.dp))
      Text(
        text = scoreLabel,
        style = MaterialTheme.typography.labelSmall.copy(
          fontWeight = FontWeight.Bold,
          fontFeatureSettings = "tnum"
        ),
        color = if (isSelected) dimension.accentColor else TextSecondaryLight
      )
    }
  }
}

@Composable
private fun HoyDeckSwitcher(
  activeMode: HoyDeckMode,
  pendingCount: Int,
  onSelectMode: (HoyDeckMode) -> Unit
) {
  Surface(
    shape = RoundedCornerShape(16.dp),
    color = SurfaceVariantLight.copy(alpha = 0.85f),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(4.dp),
      horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      HoyDeckMode.entries.forEach { mode ->
        val selected = mode == activeMode
        val interaction = rememberBounceInteractionSource()
        val bgColor by animateColorAsState(
          targetValue = if (selected) SurfaceCardLight else Color.Transparent,
          animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
          label = "deck_tab_bg_${mode.name}"
        )

        val label = when (mode) {
          HoyDeckMode.FOCUS -> "Enfoque"
          HoyDeckMode.TODAY -> "Hoy ($pendingCount)"
          HoyDeckMode.PULSE -> "Pulso"
        }

        Surface(
          onClick = { onSelectMode(mode) },
          interactionSource = interaction,
          shape = RoundedCornerShape(12.dp),
          color = bgColor,
          shadowElevation = if (selected) 1.dp else 0.dp,
          modifier = Modifier
            .weight(1f)
            .height(36.dp)
            .bouncyScale(interaction, scaleDown = 0.95f)
            .testTag("hoy_deck_tab_${mode.name.lowercase()}")
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
private fun FocusMissionInteractiveDeckCard(
  mission: BalanceMission,
  isCompleted: Boolean,
  positionIndex: Int,
  totalCount: Int,
  direction: Int,
  onPrevious: () -> Unit,
  onNext: () -> Unit,
  onViewDetails: () -> Unit
) {
  val dim = DimensionsProvider.dimensions.getOrNull(mission.dimensionIndex)
  val accentColor = dim?.accentColor ?: AccentCharcoal

  val prevInteraction = rememberBounceInteractionSource()
  val nextInteraction = rememberBounceInteractionSource()
  val ctaInteraction = rememberBounceInteractionSource()

  val cardBg by animateColorAsState(
    targetValue = if (isCompleted) Color(0xFFF3F4F6) else SurfaceCardLight,
    animationSpec = tween(260),
    label = "focus_deck_bg"
  )
  val buttonBg by animateColorAsState(
    targetValue = if (isCompleted) Color(0xFFE5E7EB) else AccentCharcoal,
    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
    label = "focus_deck_cta_bg"
  )
  val buttonFg by animateColorAsState(
    targetValue = if (isCompleted) Color(0xFF4B5563) else Color.White,
    label = "focus_deck_cta_fg"
  )

  Surface(
    shape = RoundedCornerShape(22.dp),
    color = cardBg,
    border = BorderStroke(1.dp, if (isCompleted) Color(0xFFD1D5DB) else BorderLight),
    shadowElevation = if (isCompleted) 0.dp else 1.dp,
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
      // Cabecera con badges + selector discreto (< 1/N >) sin scroll vertical
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
            shape = RoundedCornerShape(9.dp),
            color = if (isCompleted) Color(0xFFE5E7EB) else accentColor.copy(alpha = 0.12f)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(6.dp)
                  .clip(CircleShape)
                  .background(if (isCompleted) Color(0xFF6B7280) else accentColor)
              )
              Spacer(modifier = Modifier.width(5.dp))
              Text(
                text = mission.dimensionTitle,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = if (isCompleted) Color(0xFF4B5563) else accentColor
              )
            }
          }

          Surface(
            shape = RoundedCornerShape(9.dp),
            color = if (isCompleted) Color(0xFFE5E7EB) else SurfaceVariantLight
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Filled.ElectricBolt,
                contentDescription = null,
                tint = if (isCompleted) Color(0xFF4B5563) else accentColor,
                modifier = Modifier.size(12.dp)
              )
              Spacer(modifier = Modifier.width(3.dp))
              Text(
                text = if (mission.hasMultiplier) "+${mission.expReward} · 1.5x" else "+${mission.expReward}",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = if (isCompleted) Color(0xFF4B5563) else TextPrimaryLight
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
                .size(32.dp)
                .bouncyScale(prevInteraction, scaleDown = 0.88f)
                .testTag("btn_hoy_prev_mission")
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
              modifier = Modifier.padding(horizontal = 4.dp)
            )

            Surface(
              onClick = onNext,
              interactionSource = nextInteraction,
              shape = CircleShape,
              color = SurfaceVariantLight,
              modifier = Modifier
                .minimumInteractiveComponentSize()
                .size(32.dp)
                .bouncyScale(nextInteraction, scaleDown = 0.88f)
                .testTag("btn_hoy_next_mission")
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
          val enterOffset = if (direction >= 0) 60 else -60
          (fadeIn(tween(220)) + slideInHorizontally(tween(220)) { enterOffset }) togetherWith
            (fadeOut(tween(150)) + slideOutHorizontally(tween(150)) { -enterOffset / 2 })
        },
        label = "hoy_focus_mission_slide"
      ) { currentMission ->
        val done = currentMission.isCompleted || isCompleted
        Column {
          Text(
            text = currentMission.title,
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              textDecoration = if (done) TextDecoration.LineThrough else TextDecoration.None
            ),
            color = if (done) Color(0xFF4B5563) else TextPrimaryLight,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          Spacer(modifier = Modifier.height(3.dp))
          Text(
            text = currentMission.summary.ifEmpty { currentMission.description },
            style = MaterialTheme.typography.bodySmall,
            color = if (done) Color(0xFF6B7280) else TextSecondaryLight,
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
            tint = TextSecondaryLight,
            modifier = Modifier.size(15.dp)
          )
          Spacer(modifier = Modifier.width(5.dp))
          Text(
            text = mission.durationLabel,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
            color = TextSecondaryLight
          )
        }

        Button(
          onClick = onViewDetails,
          interactionSource = ctaInteraction,
          colors = ButtonDefaults.buttonColors(
            containerColor = buttonBg,
            contentColor = buttonFg
          ),
          shape = RoundedCornerShape(14.dp),
          contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
          modifier = Modifier
            .height(44.dp)
            .bouncyScale(ctaInteraction, scaleDown = 0.94f)
            .testTag("btn_featured_mission_details")
        ) {
          Icon(
            imageVector = if (isCompleted) Icons.Filled.CheckCircle else Icons.Outlined.Info,
            contentDescription = null,
            tint = buttonFg,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = if (isCompleted) "Completada" else "Ver Detalles",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = buttonFg
          )
        }
      }
    }
  }
}

@Composable
private fun HoyCarouselMissionCard(
  mission: BalanceMission,
  isCompleted: Boolean,
  onViewDetails: () -> Unit
) {
  val dim = DimensionsProvider.dimensions.getOrNull(mission.dimensionIndex)
  val accentColor = dim?.accentColor ?: AccentCharcoal
  val btnInteraction = rememberBounceInteractionSource()

  val cardBg by animateColorAsState(
    targetValue = if (isCompleted) Color(0xFFF3F4F6) else SurfaceCardLight,
    animationSpec = tween(250),
    label = "carousel_card_bg_${mission.id}"
  )
  val buttonBg by animateColorAsState(
    targetValue = if (isCompleted) Color(0xFFE5E7EB) else accentColor,
    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
    label = "carousel_btn_bg_${mission.id}"
  )
  val buttonFg by animateColorAsState(
    targetValue = if (isCompleted) Color(0xFF4B5563) else Color.White,
    label = "carousel_btn_fg_${mission.id}"
  )

  Surface(
    shape = RoundedCornerShape(20.dp),
    color = cardBg,
    border = BorderStroke(1.dp, if (isCompleted) Color(0xFFD1D5DB) else BorderLight),
    modifier = Modifier.width(248.dp)
  ) {
    Column(
      modifier = Modifier.padding(14.dp),
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = mission.dimensionTitle,
          style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
          color = if (isCompleted) Color(0xFF4B5563) else accentColor
        )
        Text(
          text = "+${mission.expReward} EXP · ${mission.durationLabel}",
          style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
          color = if (isCompleted) Color(0xFF6B7280) else TextSecondaryLight
        )
      }

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = mission.title,
        style = MaterialTheme.typography.titleSmall.copy(
          fontWeight = FontWeight.Bold,
          textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None
        ),
        color = if (isCompleted) Color(0xFF4B5563) else TextPrimaryLight,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = mission.summary.ifEmpty { mission.description },
        style = MaterialTheme.typography.bodySmall,
        color = if (isCompleted) Color(0xFF6B7280) else TextSecondaryLight,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        lineHeight = 17.sp
      )

      Spacer(modifier = Modifier.height(10.dp))

      Button(
        onClick = onViewDetails,
        interactionSource = btnInteraction,
        colors = ButtonDefaults.buttonColors(
          containerColor = buttonBg,
          contentColor = buttonFg
        ),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(40.dp)
          .bouncyScale(btnInteraction, scaleDown = 0.94f)
      ) {
        Icon(
          imageVector = if (isCompleted) Icons.Filled.CheckCircle else Icons.Outlined.Info,
          contentDescription = null,
          tint = buttonFg,
          modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = if (isCompleted) "Completada" else "Ver Detalles",
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
          color = buttonFg
        )
      }
    }
  }
}

@Composable
private fun HoyPulseCard(
  userProfile: UserProfile,
  onEditGoal: () -> Unit,
  onOpenFullReport: () -> Unit
) {
  val editInteraction = rememberBounceInteractionSource()
  val reportInteraction = rememberBounceInteractionSource()

  val expCapProgressTarget = (userProfile.expToday.toFloat() / GamificationStats.DAILY_EXP_CAP.toFloat()).coerceIn(0f, 1f)
  val animatedExpProgress by animateFloatAsState(
    targetValue = expCapProgressTarget,
    animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
    label = "hoy_pulse_exp_bar"
  )

  Surface(
    shape = RoundedCornerShape(22.dp),
    color = SurfaceCardLight,
    border = BorderStroke(1.dp, BorderLight),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      // Meta del mes editable con toque suave
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
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
              text = "Propósito actual",
              style = MaterialTheme.typography.labelSmall,
              color = TextTertiaryLight
            )
            Text(
              text = userProfile.monthlyGoal.ifBlank { "Construir mi mejor equilibrio" },
              style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
              color = TextPrimaryLight,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }

        Surface(
          onClick = onEditGoal,
          interactionSource = editInteraction,
          shape = CircleShape,
          color = SurfaceVariantLight,
          modifier = Modifier
            .minimumInteractiveComponentSize()
            .size(36.dp)
            .bouncyScale(editInteraction, scaleDown = 0.90f)
            .testTag("btn_hoy_edit_goal")
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.Filled.Edit,
              contentDescription = "Editar propósito",
              tint = AccentCharcoal,
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }

      // Barra animada de EXP diario
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Filled.ElectricBolt,
              contentDescription = null,
              tint = Color(0xFF0D9488),
              modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Energía de hoy: ${userProfile.expToday}/${GamificationStats.DAILY_EXP_CAP} EXP",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
              color = TextPrimaryLight
            )
          }
          Text(
            text = "${userProfile.exp} EXP total",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondaryLight
          )
        }

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(7.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(SurfaceVariantLight)
        ) {
          Box(
            modifier = Modifier
              .fillMaxWidth(animatedExpProgress)
              .height(7.dp)
              .clip(RoundedCornerShape(4.dp))
              .background(AccentCharcoal)
          )
        }
      }

      // Fila inferior: nivel, racha y botón al diagnóstico completo
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = null,
            tint = Color(0xFFD97706),
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(5.dp))
          Text(
            text = "Nivel ${userProfile.level} · ${userProfile.rank}",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = TextPrimaryLight
          )
        }

        Surface(
          onClick = onOpenFullReport,
          interactionSource = reportInteraction,
          shape = RoundedCornerShape(12.dp),
          color = SurfaceVariantLight,
          modifier = Modifier
            .bouncyScale(reportInteraction, scaleDown = 0.94f)
            .testTag("btn_hoy_open_full_report")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Outlined.Hexagon,
              contentDescription = null,
              tint = AccentCharcoal,
              modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Hexágono completo",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = AccentCharcoal
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowForward,
              contentDescription = null,
              tint = AccentCharcoal,
              modifier = Modifier.size(13.dp)
            )
          }
        }
      }
    }
  }
}

@Composable
private fun AllDoneMinimalBanner(
  onOpenFullReport: () -> Unit
) {
  val interaction = rememberBounceInteractionSource()
  Surface(
    shape = RoundedCornerShape(22.dp),
    color = Color(0xFFF0FDF4),
    border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier.padding(16.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.weight(1f)
      ) {
        Icon(
          imageVector = Icons.Outlined.AutoAwesome,
          contentDescription = null,
          tint = Color(0xFF16A34A),
          modifier = Modifier.size(26.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
          Text(
            text = "Día en equilibrio cumplido",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFF15803D)
          )
          Text(
            text = "Tu hexágono real ya refleja tu constancia.",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF166534)
          )
        }
      }
      Surface(
        onClick = onOpenFullReport,
        interactionSource = interaction,
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF16A34A).copy(alpha = 0.14f),
        modifier = Modifier.bouncyScale(interaction, scaleDown = 0.94f)
      ) {
        Text(
          text = "Ver informe",
          style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
          color = Color(0xFF15803D),
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
        )
      }
    }
  }
}
