package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentCharcoal
import com.example.ui.theme.BorderLight
import com.example.ui.theme.CanvasBackgroundLight
import com.example.ui.theme.TextPrimaryLight
import com.example.ui.theme.TextSecondaryLight
import kotlinx.coroutines.delay

val MotivationalQuotes = listOf(
  "Pequeños pasos sostenidos construyen grandes destinos.",
  "La constancia vence al talento cuando el talento no es constante.",
  "Tu energía de hoy define tu claridad de mañana.",
  "Honra tu propio ritmo: avanzar despacio sigue siendo avanzar.",
  "La mejor versión de ti misma empieza con una sola decisión consciente."
)

@Composable
fun LoadingScreen(
  message: String? = null,
  durationMillis: Long = 1300L,
  onFinished: () -> Unit,
  modifier: Modifier = Modifier
) {
  val quote = remember { message ?: MotivationalQuotes.random() }
  val rotation = remember { Animatable(0f) }

  LaunchedEffect(Unit) {
    rotation.animateTo(
      targetValue = 360f,
      animationSpec = infiniteRepeatable(
        animation = tween(1200, easing = LinearEasing),
        repeatMode = RepeatMode.Restart
      )
    )
  }

  LaunchedEffect(Unit) {
    delay(durationMillis)
    onFinished()
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(CanvasBackgroundLight)
      .padding(32.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      // Beautiful minimal loading dial in the center
      Box(
        modifier = Modifier.size(64.dp),
        contentAlignment = Alignment.Center
      ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
          // Track
          drawCircle(
            color = BorderLight,
            style = Stroke(width = 3.dp.toPx())
          )
          // Animated Arc
          drawArc(
            color = AccentCharcoal,
            startAngle = rotation.value,
            sweepAngle = 90f,
            useCenter = false,
            style = Stroke(
              width = 3.5.dp.toPx(),
              cap = StrokeCap.Round
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(28.dp))

      Text(
        text = quote,
        style = MaterialTheme.typography.titleMedium.copy(
          fontSize = 16.sp,
          lineHeight = 24.sp,
          fontWeight = FontWeight.Medium
        ),
        color = TextPrimaryLight,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 16.dp)
      )

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = "Sincronizando...",
        style = MaterialTheme.typography.labelSmall,
        color = TextSecondaryLight
      )
    }
  }
}
