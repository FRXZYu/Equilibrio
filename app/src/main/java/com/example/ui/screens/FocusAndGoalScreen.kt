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
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Info
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
import com.example.model.DimensionsProvider
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
 * Pantalla serena de cierre del registro inicial:
 * permite a cada persona elegir libremente qué área(s) quiere cuidar primero
 * y definir una intención sencilla para este mes.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FocusAndGoalScreen(
  initialFocusDimensions: Set<Int> = emptySet(),
  initialGoal: String = "Cuidarme con pasos pequeños y realistas",
  onFinished: (chosenFocusDimensions: Set<Int>, monthlyGoal: String) -> Unit,
  modifier: Modifier = Modifier
) {
  val haptic = LocalHapticFeedback.current
  var selectedFocus by remember(initialFocusDimensions) { mutableStateOf(initialFocusDimensions) }
  var goal by remember(initialGoal) { mutableStateOf(initialGoal) }
  val scrollState = rememberScrollState()

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
      Spacer(modifier = Modifier.height(12.dp))

      Text(
        text = "Tu Enfoque y Propósito",
        style = MaterialTheme.typography.displayMedium.copy(
          fontSize = 27.sp,
          fontWeight = FontWeight.Bold
        ),
        color = TextPrimaryLight
      )

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = "Elige libremente en qué áreas quieres avanzar hoy y escribe una intención amable.",
        style = MaterialTheme.typography.bodyLarge,
        color = TextSecondaryLight
      )

      Spacer(modifier = Modifier.height(26.dp))

      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Outlined.Explore,
          contentDescription = null,
          tint = AccentCharcoal,
          modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "¿Qué áreas quieres cuidar?",
          style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
          color = TextPrimaryLight
        )
      }

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = "Puedes seleccionar una o varias, o cambiarlas en cualquier momento desde Hoy y Progreso.",
        style = MaterialTheme.typography.bodySmall,
        color = TextSecondaryLight
      )

      Spacer(modifier = Modifier.height(14.dp))

      FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        DimensionsProvider.dimensions.forEach { dim ->
          val isSelected = dim.index in selectedFocus
          val interaction = rememberBounceInteractionSource()
          val bg by animateColorAsState(
            targetValue = if (isSelected) AccentCharcoal else SurfaceCardLight,
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
            label = "focus_chip_bg_${dim.index}"
          )

          Surface(
            onClick = {
              haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              selectedFocus = if (isSelected) {
                selectedFocus - dim.index
              } else {
                selectedFocus + dim.index
              }
            },
            interactionSource = interaction,
            shape = RoundedCornerShape(16.dp),
            color = bg,
            border = BorderStroke(1.dp, if (isSelected) AccentCharcoal else BorderLight),
            modifier = Modifier
              .minimumInteractiveComponentSize()
              .bouncyScale(interaction, scaleDown = 0.94f)
              .testTag("focus_dim_option_${dim.index}")
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(24.dp)
                  .clip(CircleShape)
                  .background(
                    if (isSelected) Color.White.copy(alpha = 0.16f)
                    else dim.accentColor.copy(alpha = 0.14f)
                  ),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = if (isSelected) Icons.Filled.Check else dim.icon,
                  contentDescription = null,
                  tint = if (isSelected) Color.White else dim.accentColor,
                  modifier = Modifier.size(14.dp)
                )
              }
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = dim.title,
                style = MaterialTheme.typography.labelLarge.copy(
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                ),
                color = if (isSelected) Color.White else TextPrimaryLight
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(28.dp))

      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Outlined.Flag,
          contentDescription = null,
          tint = AccentCharcoal,
          modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Tu intención para este mes",
          style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
          color = TextPrimaryLight
        )
      }

      Spacer(modifier = Modifier.height(6.dp))

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
          text = "Puedes cambiarla después sin presión",
          style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
          color = TextSecondaryLight
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      OutlinedTextField(
        value = goal,
        onValueChange = { goal = it },
        placeholder = { Text("Ej. Cuidar mi descanso y disfrutar mis tardes") },
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

    val finishInteraction = rememberBounceInteractionSource()
    Button(
      onClick = {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        val trimmedGoal = goal.trim().ifEmpty { "Cuidarme con pasos pequeños y realistas" }
        onFinished(selectedFocus, trimmedGoal)
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
        text = "Entrar a mi espacio",
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
