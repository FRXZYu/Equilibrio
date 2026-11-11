package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DimensionsProvider
import com.example.ui.components.CompactHexagonProgress
import com.example.ui.components.RatingSelector
import com.example.ui.components.bouncyClick
import com.example.ui.theme.AccentCharcoal
import com.example.ui.theme.BorderLight
import com.example.ui.theme.CanvasBackgroundLight
import com.example.ui.theme.SurfaceCardLight
import com.example.ui.theme.SurfaceVariantLight
import com.example.ui.theme.TextPrimaryLight
import com.example.ui.theme.TextSecondaryLight
import com.example.ui.theme.TextTertiaryLight

@Composable
fun QuestionnaireScreen(
  currentStep: Int,
  scores: Map<Int, Int>,
  onScoreSelected: (Int, Int) -> Unit,
  onNext: () -> Unit,
  onPrevious: () -> Unit,
  onJumpToStep: (Int) -> Unit,
  modifier: Modifier = Modifier
) {
  val dimensions = DimensionsProvider.dimensions
  val dimension = dimensions[currentStep]
  val currentScore = scores[currentStep] ?: 7
  val totalSteps = dimensions.size
  val isLastStep = currentStep == totalSteps - 1

  val progress = (currentStep + 1f) / totalSteps
  val animatedProgress by animateFloatAsState(
    targetValue = progress,
    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
    label = "progress_bar"
  )

  val scrollState = rememberScrollState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(CanvasBackgroundLight)
      .statusBarsPadding()
      .navigationBarsPadding()
      .padding(horizontal = 20.dp, vertical = 12.dp)
  ) {
    // Top Bar: Title & Compact Hexagon on Top Right
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = "Equilibrio",
          style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
          color = TextPrimaryLight
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = "Pregunta ${currentStep + 1} de $totalSteps · ${dimension.title}",
          style = MaterialTheme.typography.titleMedium,
          color = TextSecondaryLight
        )
      }

      // Live Compact Hexagon in Top-Right Corner
      CompactHexagonProgress(
        scores = scores,
        currentQuestionIndex = currentStep,
        sizeDp = 68.dp,
        modifier = Modifier.testTag("compact_hexagon_preview")
      )
    }

    // Smooth Progress Bar
    LinearProgressIndicator(
      progress = { animatedProgress },
      modifier = Modifier
        .fillMaxWidth()
        .height(6.dp)
        .clip(RoundedCornerShape(3.dp)),
      color = AccentCharcoal,
      trackColor = BorderLight,
      strokeCap = StrokeCap.Round
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Main Question Card (Surface is stable, content transitions smoothly without flickering)
    Surface(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .verticalScroll(scrollState),
      shape = RoundedCornerShape(26.dp),
      color = SurfaceCardLight,
      border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
      shadowElevation = 2.dp
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
      ) {
        Column {
          // Dimension Category Pill
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .background(dimension.accentColor.copy(alpha = 0.12f))
              .padding(horizontal = 12.dp, vertical = 6.dp)
          ) {
            Icon(
              imageVector = dimension.icon,
              contentDescription = null,
              tint = dimension.accentColor,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = dimension.title.uppercase(),
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
              ),
              color = dimension.accentColor
            )
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Animated Question Text without layout blinking
          AnimatedContent(
            targetState = currentStep,
            transitionSpec = {
              fadeIn(androidx.compose.animation.core.tween(220)) togetherWith
                  fadeOut(androidx.compose.animation.core.tween(180))
            },
            label = "question_text_fade"
          ) { stepIndex ->
            val curDim = dimensions[stepIndex]
            Column {
              Text(
                text = curDim.question,
                style = MaterialTheme.typography.displayMedium.copy(
                  fontSize = 24.sp,
                  lineHeight = 32.sp
                ),
                color = TextPrimaryLight,
                modifier = Modifier.testTag("question_text")
              )

              Spacer(modifier = Modifier.height(10.dp))

              Text(
                text = curDim.description,
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondaryLight
              )
            }
          }

          Spacer(modifier = Modifier.height(24.dp))

          // Rating Selector (1 to 10)
          RatingSelector(
            currentScore = currentScore,
            accentColor = dimension.accentColor,
            onScoreSelected = { newScore ->
              onScoreSelected(currentStep, newScore)
            }
          )

          Spacer(modifier = Modifier.height(20.dp))

              // Extreme Reference Labels (1 vs 10)
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(14.dp))
                  .background(SurfaceVariantLight)
                  .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = "1 · Nivel Bajo",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextSecondaryLight
                  )
                  Text(
                    text = dimension.minLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextTertiaryLight
                  )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(
                  modifier = Modifier.weight(1f),
                  horizontalAlignment = Alignment.End
                ) {
                  Text(
                    text = "10 · Nivel Pleno",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = dimension.accentColor
                  )
                  Text(
                    text = dimension.maxLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextTertiaryLight
                  )
                }
              }
            }
          }
        }

    Spacer(modifier = Modifier.height(14.dp))

    // Step dots indicator
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically
    ) {
      dimensions.forEachIndexed { idx, dim ->
        val isCompleted = scores.containsKey(idx)
        val isCurrent = idx == currentStep

        Box(
          modifier = Modifier
            .padding(horizontal = 4.dp)
            .size(if (isCurrent) 10.dp else 7.dp)
            .clip(CircleShape)
            .background(
              when {
                isCurrent -> AccentCharcoal
                isCompleted -> dim.accentColor
                else -> BorderLight
              }
            )
            .bouncyClick { onJumpToStep(idx) }
        )
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Navigation Action Buttons
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      if (currentStep > 0) {
        OutlinedButton(
          onClick = onPrevious,
          shape = RoundedCornerShape(18.dp),
          colors = ButtonDefaults.outlinedButtonColors(
            contentColor = TextPrimaryLight
          ),
          border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
          modifier = Modifier
            .weight(0.35f)
            .height(54.dp)
            .bouncyClick(onClick = onPrevious)
            .testTag("btn_previous_question")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Anterior",
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text("Atrás", style = MaterialTheme.typography.labelLarge)
        }
      }

      Button(
        onClick = onNext,
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = AccentCharcoal,
          contentColor = Color.White
        ),
        modifier = Modifier
          .weight(if (currentStep > 0) 0.65f else 1f)
          .height(54.dp)
          .bouncyClick(onClick = onNext)
          .testTag("btn_next_question")
      ) {
        if (isLastStep) {
          Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Generar Estadística Hexagonal",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
          )
        } else {
          Text(
            text = "Siguiente",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = "Siguiente",
            modifier = Modifier.size(20.dp)
          )
        }
      }
    }
  }
}
