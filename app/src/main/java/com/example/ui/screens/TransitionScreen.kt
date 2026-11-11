package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ExpandedHexagonRadarChart
import com.example.ui.theme.CanvasBackgroundLight
import com.example.ui.theme.TextPrimaryLight
import com.example.ui.theme.TextSecondaryLight
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun TransitionScreen(
  scores: Map<Int, Int>,
  onComplete: () -> Unit,
  modifier: Modifier = Modifier
) {
  // Animatable values for translation and expansion
  val animProgress = remember { Animatable(0f) }
  val textAlpha = remember { Animatable(0f) }

  LaunchedEffect(Unit) {
    // Stage 1: Fade in text
    launch {
      textAlpha.animateTo(
        targetValue = 1f,
        animationSpec = tween(400, easing = FastOutSlowInEasing)
      )
    }
    // Stage 2: Move from top-right to center and expand with organic bounce
    launch {
      delay(200)
      animProgress.animateTo(
        targetValue = 1f,
        animationSpec = spring(
          dampingRatio = Spring.DampingRatioMediumBouncy,
          stiffness = Spring.StiffnessLow
        )
      )
    }
    // Stage 3: Wait a moment for visual payoff, then transition to full report
    delay(1700)
    onComplete()
  }

  BoxWithConstraints(
    modifier = modifier
      .fillMaxSize()
      .background(CanvasBackgroundLight)
      .testTag("transition_screen"),
    contentAlignment = Alignment.Center
  ) {
    val screenWidth = maxWidth
    val screenHeight = maxHeight

    // Compute interpolation:
    // At p=0: near top right (xOffset ~ 100.dp, yOffset ~ -240.dp, scale ~ 0.35f)
    // At p=1: center (xOffset = 0, yOffset = -20.dp, scale = 1f)
    val p = animProgress.value
    val startX = (screenWidth.value * 0.32f).dp
    val startY = (-screenHeight.value * 0.35f).dp
    val currentX = (startX.value * (1f - p)).dp
    val currentY = (startY.value * (1f - p) - (20f * p).dp.value).dp

    val currentScale = 0.32f + (0.68f * p)

    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 24.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Spacer(modifier = Modifier.weight(0.15f))

      // Center/Animated Hexagon
      Box(
        modifier = Modifier
          .offset {
            IntOffset(
              currentX.toPx().roundToInt(),
              currentY.toPx().roundToInt()
            )
          }
          .scale(currentScale),
        contentAlignment = Alignment.Center
      ) {
        ExpandedHexagonRadarChart(
          scores = scores,
          selectedDimensionIndex = null,
          onDimensionSelect = {},
          modifier = Modifier.size(280.dp)
        )
      }

      Spacer(modifier = Modifier.weight(0.1f))

      // Informative status text
      Column(
        modifier = Modifier
          .alpha(textAlpha.value)
          .padding(bottom = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = "Construyendo tu Hexágono Vital",
          style = MaterialTheme.typography.displayMedium.copy(
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
          ),
          color = TextPrimaryLight,
          textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = "Analizando armonía, debilidades prioritarias y plan de acción...",
          style = MaterialTheme.typography.bodyLarge,
          color = TextSecondaryLight,
          textAlign = TextAlign.Center
        )
      }

      Spacer(modifier = Modifier.weight(0.05f))
    }
  }
}
