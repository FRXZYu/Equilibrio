package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.FormatListNumbered
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.domain.HelpfulnessRating
import com.example.model.BalanceMission
import com.example.model.DimensionsProvider
import com.example.ui.components.bouncyScale
import com.example.ui.components.rememberBounceInteractionSource
import com.example.ui.theme.AccentCharcoal
import com.example.ui.theme.BorderLight
import com.example.ui.theme.SurfaceVariantLight
import com.example.ui.theme.TextPrimaryLight
import com.example.ui.theme.TextSecondaryLight
import com.example.ui.theme.TextTertiaryLight

/**
 * Modal sereno para explorar una acción cotidiana, sus pasos prácticos y
 * revisar después si realmente te sirvió ("Me ayudó", "Fue neutro", "Se sintió pesado").
 */
@Composable
fun MissionDetailModal(
  mission: BalanceMission,
  initialDateLabel: String = "Hoy",
  onDismiss: () -> Unit,
  onMarkCompleted: () -> Unit,
  onUnmarkCompleted: () -> Unit,
  onSaveReflection: (HelpfulnessRating?, String) -> Unit,
  onSaveForLater: () -> Unit
) {
  val haptic = LocalHapticFeedback.current
  val dimension = DimensionsProvider.dimensions.getOrNull(mission.dimensionIndex)
  val accentColor = dimension?.accentColor ?: AccentCharcoal

  var isCompletedNow by remember(mission.id, mission.isCompleted) { mutableStateOf(mission.isCompleted) }
  var selectedRating by remember(mission.id, mission.helpfulRating) { mutableStateOf(mission.helpfulRating) }
  var reflectionNote by remember(mission.id, mission.reflectionNote) { mutableStateOf(mission.reflectionNote) }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(Color.Black.copy(alpha = 0.42f))
        .padding(16.dp),
      contentAlignment = Alignment.Center
    ) {
      Surface(
        shape = RoundedCornerShape(28.dp),
        color = Color(0xFFFAFAFC),
        border = BorderStroke(1.dp, BorderLight),
        shadowElevation = 18.dp,
        modifier = Modifier
          .fillMaxWidth()
          .heightIn(max = 740.dp)
          .testTag("mission_detail_modal")
      ) {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 22.dp, vertical = 20.dp)
        ) {
          // Encabezado: Área + Enfoque + Cerrar
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = accentColor.copy(alpha = 0.14f)
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Box(
                    modifier = Modifier
                      .size(8.dp)
                      .clip(CircleShape)
                      .background(accentColor)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = mission.dimensionTitle.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontWeight = FontWeight.Bold,
                      letterSpacing = 0.5.sp
                    ),
                    color = accentColor
                  )
                }
              }

              if (mission.isPriorityFocus) {
                Surface(
                  shape = RoundedCornerShape(10.dp),
                  color = SurfaceVariantLight,
                  border = BorderStroke(1.dp, BorderLight)
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(
                      imageVector = Icons.Outlined.Explore,
                      contentDescription = null,
                      tint = TextPrimaryLight,
                      modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = "En tu enfoque",
                      style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                      color = TextPrimaryLight
                    )
                  }
                }
              }
            }

            val closeInteraction = rememberBounceInteractionSource()
            IconButton(
              onClick = onDismiss,
              interactionSource = closeInteraction,
              modifier = Modifier
                .minimumInteractiveComponentSize()
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.05f))
                .bouncyScale(closeInteraction, scaleDown = 0.90f)
            ) {
              Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Cerrar",
                tint = TextPrimaryLight,
                modifier = Modifier.size(18.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Contenido con scroll
          Column(
            modifier = Modifier
              .weight(1f)
              .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
          ) {
            Text(
              text = mission.title,
              style = MaterialTheme.typography.headlineSmall.copy(
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 27.sp
              ),
              color = TextPrimaryLight
            )

            Text(
              text = mission.description,
              style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp, lineHeight = 22.sp),
              color = TextSecondaryLight
            )

            // Duración y fecha
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                border = BorderStroke(1.dp, BorderLight)
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    tint = TextTertiaryLight,
                    modifier = Modifier.size(15.dp)
                  )
                  Spacer(modifier = Modifier.width(5.dp))
                  Text(
                    text = "Tiempo estimado: ${mission.durationLabel}",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = TextPrimaryLight
                  )
                }
              }

              if (mission.isHabit) {
                Surface(
                  shape = RoundedCornerShape(12.dp),
                  color = Color.White,
                  border = BorderStroke(1.dp, BorderLight)
                ) {
                  Text(
                    text = "Hábito · ${mission.frequency}",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = TextSecondaryLight,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                  )
                }
              }
            }

            // ¿Cómo hacerlo?
            Surface(
              shape = RoundedCornerShape(20.dp),
              color = Color.White,
              border = BorderStroke(1.dp, BorderLight),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Box(
                    modifier = Modifier
                      .size(28.dp)
                      .clip(CircleShape)
                      .background(AccentCharcoal.copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = Icons.Outlined.FormatListNumbered,
                      contentDescription = null,
                      tint = AccentCharcoal,
                      modifier = Modifier.size(16.dp)
                    )
                  }
                  Spacer(modifier = Modifier.width(10.dp))
                  Text(
                    text = "¿Cómo hacerlo?",
                    style = MaterialTheme.typography.titleMedium.copy(
                      fontWeight = FontWeight.Bold,
                      fontSize = 16.sp
                    ),
                    color = TextPrimaryLight
                  )
                }

                Spacer(modifier = Modifier.height(12.dp))

                val stepsToRender = if (mission.steps.isNotEmpty()) {
                  mission.steps
                } else {
                  listOf(
                    "Reserva ${mission.durationLabel.lowercase()} con calma.",
                    mission.description,
                    "Al terminar, observa cómo se siente tu cuerpo y tu mente."
                  )
                }

                stepsToRender.forEachIndexed { idx, stepText ->
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top
                  ) {
                    Box(
                      modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.14f)),
                      contentAlignment = Alignment.Center
                    ) {
                      Text(
                        text = "${idx + 1}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = accentColor,
                        fontSize = 11.sp
                      )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                      text = stepText,
                      style = MaterialTheme.typography.bodyMedium.copy(
                        lineHeight = 20.sp,
                        fontSize = 14.sp
                      ),
                      color = TextPrimaryLight
                    )
                  }
                }
              }
            }

            // Por qué puede ayudarte
            Surface(
              shape = RoundedCornerShape(20.dp),
              color = Color(0xFFF0FDF4),
              border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(15.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Outlined.Spa,
                    contentDescription = null,
                    tint = Color(0xFF16A34A),
                    modifier = Modifier.size(18.dp)
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = "Por qué puede ayudarte",
                    style = MaterialTheme.typography.labelMedium.copy(
                      fontWeight = FontWeight.Bold,
                      letterSpacing = 0.4.sp
                    ),
                    color = Color(0xFF15803D)
                  )
                }
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                  text = mission.benefit,
                  style = MaterialTheme.typography.bodyMedium.copy(
                    lineHeight = 20.sp,
                    fontSize = 13.5.sp
                  ),
                  color = Color(0xFF166534)
                )
              }
            }

            // Revisión amable posterior: "¿Te sirvió esta acción?"
            AnimatedVisibility(
              visible = isCompletedNow,
              enter = fadeIn() + expandVertically(),
              exit = fadeOut() + shrinkVertically()
            ) {
              Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f)),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("mission_review_section")
              ) {
                Column(modifier = Modifier.padding(16.dp)) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      imageVector = Icons.Outlined.EditNote,
                      contentDescription = null,
                      tint = accentColor,
                      modifier = Modifier.size(19.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = "¿Te sirvió esta acción? ($initialDateLabel)",
                      style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                      color = TextPrimaryLight
                    )
                  }
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = "Tu respuesta ajusta las sugerencias futuras para que no se sientan como una carga.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondaryLight
                  )
                  Spacer(modifier = Modifier.height(10.dp))

                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                  ) {
                    HelpfulnessOptionChip(
                      label = HelpfulnessRating.HELPFUL.label,
                      icon = Icons.Outlined.ThumbUp,
                      selected = selectedRating == HelpfulnessRating.HELPFUL,
                      selectedColor = Color(0xFF15803D),
                      onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        selectedRating = HelpfulnessRating.HELPFUL
                      },
                      modifier = Modifier
                        .weight(1f)
                        .testTag("rating_helpful")
                    )
                    HelpfulnessOptionChip(
                      label = HelpfulnessRating.NEUTRAL.shortLabel,
                      icon = Icons.Outlined.Remove,
                      selected = selectedRating == HelpfulnessRating.NEUTRAL,
                      selectedColor = AccentCharcoal,
                      onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        selectedRating = HelpfulnessRating.NEUTRAL
                      },
                      modifier = Modifier
                        .weight(1f)
                        .testTag("rating_neutral")
                    )
                    HelpfulnessOptionChip(
                      label = HelpfulnessRating.HEAVY.shortLabel,
                      icon = Icons.Outlined.HourglassEmpty,
                      selected = selectedRating == HelpfulnessRating.HEAVY,
                      selectedColor = Color(0xFFB45309),
                      onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        selectedRating = HelpfulnessRating.HEAVY
                      },
                      modifier = Modifier
                        .weight(1f)
                        .testTag("rating_heavy")
                    )
                  }

                  Spacer(modifier = Modifier.height(10.dp))

                  OutlinedTextField(
                    value = reflectionNote,
                    onValueChange = { reflectionNote = it },
                    placeholder = {
                      Text(
                        text = "Nota breve opcional: ¿qué notaste?",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiaryLight
                      )
                    },
                    maxLines = 2,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                      focusedBorderColor = AccentCharcoal,
                      unfocusedBorderColor = BorderLight,
                      focusedContainerColor = Color(0xFFFAFAFC),
                      unfocusedContainerColor = Color(0xFFFAFAFC)
                    ),
                    modifier = Modifier
                      .fillMaxWidth()
                      .testTag("input_reflection_note")
                  )
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Botones finales
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val primaryInteraction = rememberBounceInteractionSource()
            if (!isCompletedNow) {
              Button(
                onClick = {
                  haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                  isCompletedNow = true
                  onMarkCompleted()
                },
                interactionSource = primaryInteraction,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                  containerColor = AccentCharcoal,
                  contentColor = Color.White
                ),
                modifier = Modifier
                  .fillMaxWidth()
                  .height(52.dp)
                  .bouncyScale(primaryInteraction)
                  .testTag("btn_complete_mission_detail")
              ) {
                Icon(
                  imageVector = Icons.Filled.CheckCircle,
                  contentDescription = null,
                  modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "Marcar como hecha",
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
              }

              val saveInteraction = rememberBounceInteractionSource()
              OutlinedButton(
                onClick = onSaveForLater,
                interactionSource = saveInteraction,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentCharcoal),
                border = BorderStroke(1.dp, BorderLight),
                modifier = Modifier
                  .fillMaxWidth()
                  .height(48.dp)
                  .bouncyScale(saveInteraction)
                  .testTag("btn_save_for_later_mission_detail")
              ) {
                Icon(
                  imageVector = if (mission.isSavedForLater) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                  contentDescription = null,
                  modifier = Modifier.size(18.dp),
                  tint = AccentCharcoal
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = if (mission.isSavedForLater) "Quitar de guardadas" else "Guardar para otro momento",
                  style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
              }
            } else {
              Button(
                onClick = {
                  haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                  onSaveReflection(selectedRating, reflectionNote)
                },
                interactionSource = primaryInteraction,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                  containerColor = AccentCharcoal,
                  contentColor = Color.White
                ),
                modifier = Modifier
                  .fillMaxWidth()
                  .height(50.dp)
                  .bouncyScale(primaryInteraction)
                  .testTag("btn_save_reflection_detail")
              ) {
                Icon(
                  imageVector = Icons.Filled.Check,
                  contentDescription = null,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "Guardar y cerrar",
                  style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
              }

              val undoInteraction = rememberBounceInteractionSource()
              TextButton(
                onClick = {
                  haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                  isCompletedNow = false
                  onUnmarkCompleted()
                },
                interactionSource = undoInteraction,
                modifier = Modifier
                  .align(Alignment.CenterHorizontally)
                  .minimumInteractiveComponentSize()
                  .bouncyScale(undoInteraction)
                  .testTag("btn_unmark_mission_detail")
              ) {
                Text(
                  text = "Desmarcar registro",
                  style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                  color = TextSecondaryLight
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun HelpfulnessOptionChip(
  label: String,
  icon: ImageVector,
  selected: Boolean,
  selectedColor: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val interaction = rememberBounceInteractionSource()
  val bg by animateColorAsState(
    targetValue = if (selected) selectedColor.copy(alpha = 0.14f) else SurfaceVariantLight,
    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
    label = "help_chip_bg_$label"
  )

  Surface(
    onClick = onClick,
    interactionSource = interaction,
    shape = RoundedCornerShape(14.dp),
    color = bg,
    border = BorderStroke(1.dp, if (selected) selectedColor else BorderLight),
    modifier = modifier
      .minimumInteractiveComponentSize()
      .height(48.dp)
      .bouncyScale(interaction, scaleDown = 0.93f)
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 6.dp),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = if (selected) selectedColor else TextSecondaryLight,
        modifier = Modifier.size(15.dp)
      )
      Spacer(modifier = Modifier.width(4.dp))
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall.copy(
          fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        ),
        color = if (selected) selectedColor else TextPrimaryLight,
        maxLines = 1
      )
    }
  }
}
