package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.FormatQuote
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentCharcoal
import com.example.ui.theme.CanvasBackgroundLight
import com.example.ui.theme.SurfaceCardLight
import com.example.ui.theme.TextPrimaryLight
import com.example.ui.theme.TextSecondaryLight
import kotlinx.coroutines.delay

object MotivationalQuotes {
  val onboarding = listOf(
    "Cada gran transformación comienza con un nombre y una decisión clara.",
    "Reconocer tu identidad es el primer paso hacia una autoestima inquebrantable.",
    "La autenticidad es tu mayor superpoder: muéstrate al mundo sin máscaras.",
    "Tus cimientos están listos. Es hora de conquistar tu mejor versión."
  )
}

@Composable
fun LoadingScreen(
  message: String,
  onFinished: () -> Unit,
  modifier: Modifier = Modifier
) {
  val entranceScale = remember { Animatable(0.9f) }
  val entranceAlpha = remember { Animatable(0f) }

  val infiniteTransition = rememberInfiniteTransition(label = "loading_pulse")
  val iconPulse by infiniteTransition.animateFloat(
    initialValue = 0.94f,
    targetValue = 1.06f,
    animationSpec = infiniteRepeatable(
      animation = tween(900, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "icon_pulse"
  )

  val progressAnim = remember { Animatable(0f) }

  LaunchedEffect(message) {
    entranceScale.snapTo(0.9f)
    entranceAlpha.snapTo(0f)
    progressAnim.snapTo(0f)

    entranceAlpha.animateTo(1f, tween(320, easing = FastOutSlowInEasing))
    entranceScale.animateTo(
      1f,
      spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)
    )
    progressAnim.animateTo(1f, tween(1150, easing = LinearEasing))
    delay(120)
    onFinished()
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(CanvasBackgroundLight)
      .statusBarsPadding()
      .navigationBarsPadding()
      .padding(horizontal = 28.dp)
      .testTag("loading_screen"),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
      modifier = Modifier
        .fillMaxWidth()
        .scale(entranceScale.value)
        .alpha(entranceAlpha.value)
    ) {
      Surface(
        shape = CircleShape,
        color = AccentCharcoal.copy(alpha = 0.08f),
        modifier = Modifier
          .size(72.dp)
          .scale(iconPulse)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = Icons.Outlined.AutoAwesome,
            contentDescription = null,
            tint = AccentCharcoal,
            modifier = Modifier.size(34.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(28.dp))

      Surface(
        shape = RoundedCornerShape(22.dp),
        color = SurfaceCardLight,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.padding(horizontal = 24.dp, vertical = 22.dp)
        ) {
          Icon(
            imageVector = Icons.Outlined.FormatQuote,
            contentDescription = null,
            tint = TextSecondaryLight.copy(alpha = 0.65f),
            modifier = Modifier.size(26.dp)
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = message,
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.SemiBold,
              lineHeight = 24.sp
            ),
            color = TextPrimaryLight,
            textAlign = TextAlign.Center
          )
        }
      }

      Spacer(modifier = Modifier.height(28.dp))

      LinearProgressIndicator(
        progress = { progressAnim.value },
        color = AccentCharcoal,
        trackColor = AccentCharcoal.copy(alpha = 0.14f),
        strokeCap = StrokeCap.Round,
        modifier = Modifier
          .width(160.dp)
          .height(5.dp)
      )
    }
  }
}
