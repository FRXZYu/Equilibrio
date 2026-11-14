package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AllZodiacSigns
import com.example.ui.components.bouncyScale
import com.example.ui.components.rememberBounceInteractionSource
import com.example.ui.theme.AccentCharcoal
import com.example.ui.theme.BorderLight
import com.example.ui.theme.CanvasBackgroundLight
import com.example.ui.theme.SurfaceCardLight
import com.example.ui.theme.SurfaceVariantLight
import com.example.ui.theme.TextPrimaryLight
import com.example.ui.theme.TextSecondaryLight

/**
 * Pantalla final del registro inicial:
 * selección de signo y meta del mes con Material Design 3 puro y transiciones suaves.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ZodiacAndGoalScreen(
  initialZodiac: String = "Aries",
  initialGoal: String = "Quiero ser mi mejor versión",
  onFinished: (zodiacSign: String, zodiacSymbol: String, monthlyGoal: String) -> Unit,
  modifier: Modifier = Modifier
) {
  val haptic = LocalHapticFeedback.current
  var selectedSignName by remember(initialZodiac) {
    mutableStateOf(
      AllZodiacSigns.firstOrNull { it.name.equals(initialZodiac, ignoreCase = true) }?.name
        ?: AllZodiacSigns.first().name
    )
  }
  var monthlyGoal by remember(initialGoal) { mutableStateOf(initialGoal) }
  val scrollState = rememberScrollState()

  val selectedSign = AllZodiacSigns.firstOrNull { it.name == selectedSignName } ?: AllZodiacSigns.first()

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
        .verticalScroll(scrollState)
    ) {
      Spacer(modifier = Modifier.height(10.dp))

      Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = AccentCharcoal.copy(alpha = 0.08f),
          modifier = Modifier.size(46.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.Outlined.AutoAwesome,
              contentDescription = null,
              tint = AccentCharcoal,
              modifier = Modifier.size(24.dp)
            )
          }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
          Text(
            text = "Últimos detalles",
            style = MaterialTheme.typography.headlineSmall.copy(
              fontSize = 25.sp,
              fontWeight = FontWeight.Bold
            ),
            color = TextPrimaryLight
          )
          Text(
            text = "Personaliza tu perfil y propósito mensual",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondaryLight
          )
        }
      }

      Spacer(modifier = Modifier.height(22.dp))

      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Outlined.NightsStay,
          contentDescription = null,
          tint = AccentCharcoal,
          modifier = Modifier.size(19.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Tu signo",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = TextPrimaryLight
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        AllZodiacSigns.forEach { sign ->
          val isSelected = sign.name == selectedSignName
          val interaction = rememberBounceInteractionSource()
          val bg by animateColorAsState(
            targetValue = if (isSelected) AccentCharcoal else SurfaceCardLight,
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
            label = "zodiac_bg_${sign.name}"
          )
          Surface(
            onClick = {
              haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              selectedSignName = sign.name
            },
            interactionSource = interaction,
            shape = RoundedCornerShape(14.dp),
            color = bg,
            border = BorderStroke(1.dp, if (isSelected) AccentCharcoal else BorderLight),
            modifier = Modifier
              .minimumInteractiveComponentSize()
              .bouncyScale(interaction, scaleDown = 0.93f)
              .testTag("zodiac_chip_${sign.name.lowercase()}")
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(22.dp)
                  .clip(CircleShape)
                  .background(
                    if (isSelected) Color.White.copy(alpha = 0.16f)
                    else SurfaceVariantLight
                  ),
                contentAlignment = Alignment.Center
              ) {
                if (isSelected) {
                  Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(13.dp)
                  )
                } else {
                  Text(
                    text = sign.name.take(2).uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontSize = 10.sp,
                      fontWeight = FontWeight.Bold
                    ),
                    color = AccentCharcoal
                  )
                }
              }
              Spacer(modifier = Modifier.width(6.dp))
              Column {
                Text(
                  text = sign.name,
                  style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                  ),
                  color = if (isSelected) Color.White else TextPrimaryLight
                )
                Text(
                  text = sign.dates,
                  style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                  color = if (isSelected) Color.White.copy(alpha = 0.72f) else TextSecondaryLight
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Outlined.Flag,
          contentDescription = null,
          tint = AccentCharcoal,
          modifier = Modifier.size(19.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Tu meta para el mes",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = TextPrimaryLight
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

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
          text = "Puedes cambiarlo después cuando quieras desde tu panel",
          style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
          color = TextSecondaryLight
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      OutlinedTextField(
        value = monthlyGoal,
        onValueChange = { monthlyGoal = it },
        placeholder = { Text("Ej. Dormir mejor y moverme 15 minutos al día") },
        minLines = 2,
        maxLines = 4,
        shape = RoundedCornerShape(16.dp),
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

      Spacer(modifier = Modifier.height(16.dp))
    }

    val finishInteraction = rememberBounceInteractionSource()
    Button(
      onClick = {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        val trimmedGoal = monthlyGoal.trim().ifEmpty { "Quiero ser mi mejor versión" }
        onFinished(selectedSign.name, selectedSign.symbol, trimmedGoal)
      },
      interactionSource = finishInteraction,
      shape = RoundedCornerShape(18.dp),
      colors = ButtonDefaults.buttonColors(
        containerColor = AccentCharcoal,
        contentColor = Color.White
      ),
      modifier = Modifier
        .fillMaxWidth()
        .height(54.dp)
        .bouncyScale(finishInteraction)
        .testTag("btn_finish_onboarding")
    ) {
      Text(
        text = "Entrar a mi panel",
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
