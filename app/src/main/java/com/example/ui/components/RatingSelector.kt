package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ScoreTier
import com.example.ui.theme.AccentCharcoal
import com.example.ui.theme.BorderLight
import com.example.ui.theme.SurfaceCardLight
import com.example.ui.theme.TextPrimaryLight
import com.example.ui.theme.TextSecondaryLight

@Composable
fun RatingSelector(
  currentScore: Int,
  accentColor: Color,
  onScoreSelected: (Int) -> Unit,
  modifier: Modifier = Modifier
) {
  val tier = ScoreTier.fromScore(currentScore)
  val scrollState = rememberScrollState()

  Column(
    modifier = modifier.fillMaxWidth(),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // Large Animated Score Header with Tier Badge
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 4.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.Bottom) {
        Text(
          text = "$currentScore",
          style = MaterialTheme.typography.displayLarge.copy(
            fontSize = 44.sp,
            fontWeight = FontWeight.Bold
          ),
          color = TextPrimaryLight,
          modifier = Modifier.testTag("score_display_value")
        )
        Text(
          text = "/10",
          style = MaterialTheme.typography.titleMedium,
          color = TextSecondaryLight,
          modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
        )
      }

      // Qualitative Badge
      Surface(
        color = Color(tier.badgeColorHex).copy(alpha = 0.12f),
        shape = RoundedCornerShape(12.dp)
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(8.dp)
              .clip(CircleShape)
              .background(Color(tier.badgeColorHex))
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = tier.label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = Color(tier.badgeColorHex)
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Interactive Slider
    Slider(
      value = currentScore.toFloat(),
      onValueChange = { onScoreSelected(it.toInt().coerceIn(1, 10)) },
      valueRange = 1f..10f,
      steps = 8, // 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 (8 interior steps)
      colors = SliderDefaults.colors(
        thumbColor = accentColor,
        activeTrackColor = accentColor,
        inactiveTrackColor = BorderLight,
        activeTickColor = Color.Transparent,
        inactiveTickColor = Color.Transparent
      ),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("score_slider")
    )

    Spacer(modifier = Modifier.height(12.dp))

    // 1 to 10 Tactile Pill Row with Bounce Feedback
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(scrollState),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      for (score in 1..10) {
        val isSelected = score == currentScore
        val scale by animateFloatAsState(
          targetValue = if (isSelected) 1.08f else 1.0f,
          animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
          ),
          label = "pill_scale_$score"
        )

        Box(
          modifier = Modifier
            .scale(scale)
            .bouncyClick(scaleDown = 0.90f) {
              onScoreSelected(score)
            }
            .clip(RoundedCornerShape(14.dp))
            .background(
              if (isSelected) AccentCharcoal else SurfaceCardLight
            )
            .border(
              width = if (isSelected) 0.dp else 1.dp,
              color = BorderLight,
              shape = RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .testTag("score_pill_$score"),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "$score",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            ),
            color = if (isSelected) Color.White else TextPrimaryLight
          )
        }
      }
    }
  }
}
