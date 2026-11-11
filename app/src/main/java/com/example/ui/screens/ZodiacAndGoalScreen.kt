package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.NightlightRound
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AllZodiacSigns
import com.example.model.ZodiacSign
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
fun ZodiacAndGoalScreen(
  initialZodiac: String = "Aries",
  initialGoal: String = "Quiero ser mi mejor versión",
  onFinished: (zodiacSign: String, zodiacSymbol: String, monthlyGoal: String) -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedZodiac by remember {
    mutableStateOf(AllZodiacSigns.firstOrNull { it.name == initialZodiac } ?: AllZodiacSigns[0])
  }
  var goal by remember { mutableStateOf(initialGoal) }

  val verticalScrollState = rememberScrollState()
  val horizontalZodiacScroll = rememberScrollState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(CanvasBackgroundLight)
      .statusBarsPadding()
      .navigationBarsPadding()
      .padding(horizontal = 24.dp, vertical = 20.dp),
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    Column(
      modifier = Modifier
        .weight(1f)
        .verticalScroll(verticalScrollState)
    ) {
      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = "Tu Esencia y Tu Meta",
        style = MaterialTheme.typography.displayMedium.copy(
          fontSize = 28.sp,
          fontWeight = FontWeight.Bold
        ),
        color = TextPrimaryLight
      )

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = "Conecta tu signo astrológico y define tu norte para este mes.",
        style = MaterialTheme.typography.bodyLarge,
        color = TextSecondaryLight
      )

      Spacer(modifier = Modifier.height(28.dp))

      // Section 1: Signo Zodiacal
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Outlined.NightlightRound,
          contentDescription = null,
          tint = AccentCharcoal,
          modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "¿Cuál es tu signo zodiacal?",
          style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
          color = TextPrimaryLight
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Horizontal scrollable zodiac selector pills
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(horizontalZodiacScroll),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        AllZodiacSigns.forEach { sign ->
          val isSelected = sign.name == selectedZodiac.name

          Surface(
            onClick = { selectedZodiac = sign },
            shape = RoundedCornerShape(18.dp),
            color = if (isSelected) AccentCharcoal else SurfaceCardLight,
            border = androidx.compose.foundation.BorderStroke(
              1.dp,
              if (isSelected) AccentCharcoal else BorderLight
            ),
            modifier = Modifier.bouncyClick { selectedZodiac = sign }
          ) {
            Column(
              modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Text(
                text = sign.symbol,
                style = MaterialTheme.typography.displayMedium.copy(fontSize = 26.sp),
                color = if (isSelected) Color.White else TextPrimaryLight
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = sign.name,
                style = MaterialTheme.typography.labelLarge.copy(
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                ),
                color = if (isSelected) Color.White else TextPrimaryLight
              )
              Text(
                text = sign.dates,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) Color(0xFFD1D5DB) else TextTertiaryLight
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(32.dp))

      // Section 2: Meta para el mes
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Outlined.Flag,
          contentDescription = null,
          tint = AccentCharcoal,
          modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Tu meta para este mes",
          style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
          color = TextPrimaryLight
        )
      }

      Spacer(modifier = Modifier.height(6.dp))

      // Mandatory user requirement message: "puedes cambiarlo despues"
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
          .clip(RoundedCornerShape(10.dp))
          .background(SurfaceVariantLight)
          .padding(horizontal = 10.dp, vertical = 6.dp)
      ) {
        Icon(
          imageVector = Icons.Outlined.Info,
          contentDescription = null,
          tint = TextSecondaryLight,
          modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "Puedes cambiarlo después",
          style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
          color = TextSecondaryLight
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      OutlinedTextField(
        value = goal,
        onValueChange = { goal = it },
        placeholder = { Text("Ej. Quiero ser mi mejor versión") },
        minLines = 2,
        maxLines = 4,
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = SurfaceCardLight,
          unfocusedContainerColor = SurfaceCardLight,
          focusedBorderColor = AccentCharcoal,
          unfocusedBorderColor = BorderLight,
          focusedTextColor = TextPrimaryLight,
          unfocusedTextColor = TextPrimaryLight
        ),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("input_monthly_goal")
      )

      Spacer(modifier = Modifier.height(24.dp))
    }

    Button(
      onClick = {
        val trimmedGoal = goal.trim().ifEmpty { "Quiero ser mi mejor versión" }
        onFinished(selectedZodiac.name, selectedZodiac.symbol, trimmedGoal)
      },
      shape = RoundedCornerShape(18.dp),
      colors = ButtonDefaults.buttonColors(
        containerColor = AccentCharcoal,
        contentColor = Color.White
      ),
      modifier = Modifier
        .fillMaxWidth()
        .height(54.dp)
        .bouncyClick {
          val trimmedGoal = goal.trim().ifEmpty { "Quiero ser mi mejor versión" }
          onFinished(selectedZodiac.name, selectedZodiac.symbol, trimmedGoal)
        }
        .testTag("btn_finish_onboarding")
    ) {
      Text(
        text = "Entrar a mi Panel Principal",
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
      )
      Spacer(modifier = Modifier.width(8.dp))
      Icon(
        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
        contentDescription = null,
        modifier = Modifier.size(20.dp)
      )
    }
  }
}
