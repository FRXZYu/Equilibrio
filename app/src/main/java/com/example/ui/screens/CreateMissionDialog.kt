package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.EventRepeat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.BalanceMission
import com.example.model.DimensionsProvider
import com.example.ui.components.bouncyClick
import com.example.ui.components.bouncyScale
import com.example.ui.components.rememberBounceInteractionSource
import com.example.ui.theme.AccentCharcoal
import com.example.ui.theme.BorderLight
import com.example.ui.theme.TextPrimaryLight
import com.example.ui.theme.TextSecondaryLight
import com.example.ui.theme.TextTertiaryLight
import java.util.UUID

/**
 * Diálogo limpio y sin presión para crear una acción o hábito propio,
 * eligiendo el área, duración realista (5-30 min) y repetición opcional.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateMissionDialog(
  initialDimensionIndex: Int = 0,
  onDismiss: () -> Unit,
  onMissionCreated: (BalanceMission) -> Unit
) {
  var title by remember { mutableStateOf("") }
  var description by remember { mutableStateOf("") }
  var selectedDimIndex by remember { mutableIntStateOf(initialDimensionIndex.coerceIn(0, 5)) }
  var selectedMinutes by remember { mutableIntStateOf(10) }
  var selectedFrequency by remember { mutableStateOf("Diariamente") }
  var isHabit by remember { mutableStateOf(false) }

  var showHabitCartel by remember { mutableStateOf(false) }
  var habitRepeatWeeks by remember { mutableIntStateOf(1) }
  val habitDays = remember { mutableStateListOf("Lun", "Mié", "Vie") }
  var habitEndRule by remember { mutableStateOf("Nunca") }

  val allDimensions = DimensionsProvider.dimensions
  val selectedDimension = allDimensions.getOrElse(selectedDimIndex) { allDimensions.first() }

  val weekDays = listOf(
    "Lun" to "L", "Mar" to "M", "Mié" to "X", "Jue" to "J", "Vie" to "V", "Sáb" to "S", "Dom" to "D"
  )
  val durationOptions = listOf(5, 10, 15, 30)

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(Color.Black.copy(alpha = 0.40f))
        .padding(16.dp),
      contentAlignment = Alignment.Center
    ) {
      Surface(
        shape = RoundedCornerShape(28.dp),
        color = Color(0xFFFAFAFC),
        border = BorderStroke(1.dp, BorderLight),
        shadowElevation = 16.dp,
        modifier = Modifier
          .fillMaxWidth()
          .heightIn(max = 680.dp)
          .then(if (showHabitCartel) Modifier.blur(8.dp) else Modifier)
      ) {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 22.dp, vertical = 20.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Nueva acción",
                style = MaterialTheme.typography.headlineSmall.copy(
                  fontSize = 23.sp,
                  fontWeight = FontWeight.Bold
                ),
                color = TextPrimaryLight
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = "Define algo concreto y alcanzable para cuidarte",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondaryLight
              )
            }

            IconButton(
              onClick = onDismiss,
              modifier = Modifier
                .minimumInteractiveComponentSize()
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.05f))
            ) {
              Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Cerrar",
                tint = TextPrimaryLight,
                modifier = Modifier.size(18.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          Column(
            modifier = Modifier
              .weight(1f)
              .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
          ) {
            // Nombre
            Column {
              Text(
                text = "Acción concreta",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = TextPrimaryLight
              )
              Spacer(modifier = Modifier.height(6.dp))
              OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = {
                  Text(
                    "Ej: Caminar 10 min sin teléfono",
                    color = TextTertiaryLight,
                    fontSize = 14.sp
                  )
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = AccentCharcoal,
                  unfocusedBorderColor = BorderLight,
                  focusedContainerColor = Color.White,
                  unfocusedContainerColor = Color.White
                ),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("input_mission_title")
              )
            }

            // Detalles opcionales
            Column {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(
                  text = "Nota o contexto",
                  style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                  color = TextPrimaryLight
                )
                Text(
                  text = "Opcional",
                  style = MaterialTheme.typography.labelSmall,
                  color = TextTertiaryLight
                )
              }
              Spacer(modifier = Modifier.height(6.dp))
              OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                placeholder = {
                  Text(
                    "¿Cuándo o cómo prefieres hacerlo?",
                    color = TextTertiaryLight,
                    fontSize = 14.sp
                  )
                },
                maxLines = 3,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = AccentCharcoal,
                  unfocusedBorderColor = BorderLight,
                  focusedContainerColor = Color.White,
                  unfocusedContainerColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
              )
            }

            // Área
            Column {
              Text(
                text = "Área que cuida",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = TextPrimaryLight
              )
              Spacer(modifier = Modifier.height(8.dp))

              FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                allDimensions.forEach { dim ->
                  val isSelected = selectedDimIndex == dim.index
                  Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) dim.accentColor.copy(alpha = 0.15f) else Color.White,
                    border = BorderStroke(
                      width = if (isSelected) 1.5.dp else 1.dp,
                      color = if (isSelected) dim.accentColor else BorderLight
                    ),
                    modifier = Modifier
                      .bouncyClick { selectedDimIndex = dim.index }
                      .testTag("dim_chip_${dim.index}")
                  ) {
                    Row(
                      modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Box(
                        modifier = Modifier
                          .size(8.dp)
                          .clip(CircleShape)
                          .background(dim.accentColor)
                      )
                      Spacer(modifier = Modifier.width(6.dp))
                      Text(
                        text = dim.title,
                        style = MaterialTheme.typography.labelMedium.copy(
                          fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (isSelected) TextPrimaryLight else TextSecondaryLight
                      )
                    }
                  }
                }
              }
            }

            // Duración estimada
            Column {
              Text(
                text = "Tiempo realista",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = TextPrimaryLight
              )
              Spacer(modifier = Modifier.height(8.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                durationOptions.forEach { mins ->
                  val isSelected = selectedMinutes == mins
                  Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSelected) AccentCharcoal else Color.White,
                    border = BorderStroke(1.dp, if (isSelected) AccentCharcoal else BorderLight),
                    modifier = Modifier
                      .weight(1f)
                      .bouncyClick { selectedMinutes = mins }
                  ) {
                    Box(
                      modifier = Modifier.padding(vertical = 11.dp),
                      contentAlignment = Alignment.Center
                    ) {
                      Text(
                        text = "$mins min",
                        style = MaterialTheme.typography.labelMedium.copy(
                          fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (isSelected) Color.White else TextPrimaryLight
                      )
                    }
                  }
                }
              }
            }

            // Frecuencia
            Column {
              Text(
                text = "Ritmo deseado",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = TextPrimaryLight
              )
              Spacer(modifier = Modifier.height(8.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                listOf("Diariamente", "Semanalmente").forEach { freq ->
                  val isSelected = selectedFrequency == freq
                  Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSelected) AccentCharcoal else Color.White,
                    border = BorderStroke(1.dp, if (isSelected) AccentCharcoal else BorderLight),
                    modifier = Modifier
                      .weight(1f)
                      .bouncyClick { selectedFrequency = freq }
                  ) {
                    Box(
                      modifier = Modifier.padding(vertical = 11.dp),
                      contentAlignment = Alignment.Center
                    ) {
                      Text(
                        text = freq,
                        style = MaterialTheme.typography.labelMedium.copy(
                          fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (isSelected) Color.White else TextPrimaryLight
                      )
                    }
                  }
                }
              }
            }

            // ¿Hábito?
            Column {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text(
                    text = "¿Convertir en hábito?",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = TextPrimaryLight
                  )
                  Text(
                    text = "Prioriza esta acción en tus días elegidos",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiaryLight
                  )
                }

                Row(
                  modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(1.dp, BorderLight, RoundedCornerShape(16.dp))
                    .padding(3.dp),
                  horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                  Surface(
                    shape = RoundedCornerShape(13.dp),
                    color = if (!isHabit) AccentCharcoal else Color.Transparent,
                    modifier = Modifier
                      .bouncyClick {
                        isHabit = false
                        showHabitCartel = false
                      }
                      .testTag("habit_no_chip")
                  ) {
                    Text(
                      text = "No",
                      style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                      color = if (!isHabit) Color.White else TextSecondaryLight,
                      modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                    )
                  }

                  Surface(
                    shape = RoundedCornerShape(13.dp),
                    color = if (isHabit) AccentCharcoal else Color.Transparent,
                    modifier = Modifier
                      .bouncyClick {
                        isHabit = true
                        showHabitCartel = true
                      }
                      .testTag("habit_yes_chip")
                  ) {
                    Text(
                      text = "Sí",
                      style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                      color = if (isHabit) Color.White else TextSecondaryLight,
                      modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                    )
                  }
                }
              }

              if (isHabit) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                  shape = RoundedCornerShape(14.dp),
                  color = Color.White,
                  border = BorderStroke(1.dp, BorderLight),
                  modifier = Modifier
                    .fillMaxWidth()
                    .bouncyClick { showHabitCartel = true }
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      modifier = Modifier.weight(1f)
                    ) {
                      Icon(
                        imageVector = Icons.Outlined.EventRepeat,
                        contentDescription = null,
                        tint = AccentCharcoal,
                        modifier = Modifier.size(18.dp)
                      )
                      Spacer(modifier = Modifier.width(8.dp))
                      Text(
                        text = "Cada $habitRepeatWeeks sem. · ${habitDays.joinToString(", ")} · $habitEndRule",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = TextPrimaryLight
                      )
                    }
                    Icon(
                      imageVector = Icons.Default.Edit,
                      contentDescription = "Editar hábito",
                      tint = TextSecondaryLight,
                      modifier = Modifier.size(16.dp)
                    )
                  }
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          val createBtnInteraction = rememberBounceInteractionSource()
          Button(
            onClick = {
              val finalTitle = title.trim().ifEmpty { "Paso de ${selectedDimension.title}" }
              val desc = description.trim().ifEmpty {
                "Acción personal para cuidar tu área de ${selectedDimension.title}."
              }
              val durationLabel = "$selectedMinutes min"
              val newMission = BalanceMission(
                id = UUID.randomUUID().toString(),
                dimensionIndex = selectedDimIndex,
                dimensionTitle = selectedDimension.title,
                title = finalTitle,
                summary = desc.take(90),
                description = desc,
                steps = listOf(
                  "Reserva $durationLabel sin prisas.",
                  desc,
                  "Al terminar, anota cómo te hizo sentir."
                ),
                benefit = "Pequeños pasos elegidos por ti sostienen tu bienestar real.",
                durationLabel = durationLabel,
                durationMinutes = selectedMinutes,
                isPriorityFocus = false,
                isCompleted = false,
                isCustom = true,
                frequency = selectedFrequency,
                isHabit = isHabit,
                habitRepeatWeeks = if (isHabit) habitRepeatWeeks else 1,
                habitDays = if (isHabit) habitDays.toList() else emptyList(),
                habitEndRule = if (isHabit) habitEndRule else "Nunca"
              )
              onMissionCreated(newMission)
            },
            interactionSource = createBtnInteraction,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = AccentCharcoal,
              contentColor = Color.White
            ),
            modifier = Modifier
              .fillMaxWidth()
              .height(52.dp)
              .bouncyScale(createBtnInteraction)
              .testTag("btn_submit_create_mission")
          ) {
            Icon(
              imageVector = Icons.Default.Add,
              contentDescription = null,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Guardar acción",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
          }
        }
      }

      AnimatedVisibility(
        visible = showHabitCartel,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut()
      ) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.50f))
            .clickable(
              interactionSource = remember { MutableInteractionSource() },
              indication = null
            ) {},
          contentAlignment = Alignment.Center
        ) {
          Surface(
            shape = RoundedCornerShape(26.dp),
            color = Color.White,
            shadowElevation = 24.dp,
            border = BorderStroke(1.5.dp, BorderLight),
            modifier = Modifier
              .fillMaxWidth(0.92f)
              .testTag("habit_cartel_dialog")
          ) {
            Column(
              modifier = Modifier.padding(22.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Box(
                modifier = Modifier
                  .size(46.dp)
                  .clip(CircleShape)
                  .background(AccentCharcoal.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Outlined.EventRepeat,
                  contentDescription = null,
                  tint = AccentCharcoal,
                  modifier = Modifier.size(24.dp)
                )
              }

              Spacer(modifier = Modifier.height(10.dp))

              Text(
                text = "Frecuencia del hábito",
                style = MaterialTheme.typography.titleLarge.copy(
                  fontWeight = FontWeight.Bold,
                  fontSize = 20.sp
                ),
                color = TextPrimaryLight
              )

              Spacer(modifier = Modifier.height(16.dp))

              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .background(Color(0xFFFAFAFC), RoundedCornerShape(16.dp))
                  .border(1.dp, BorderLight, RoundedCornerShape(16.dp))
                  .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "Cada:",
                  style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                  color = TextSecondaryLight
                )
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  IconButton(
                    onClick = { if (habitRepeatWeeks > 1) habitRepeatWeeks-- },
                    modifier = Modifier
                      .size(36.dp)
                      .clip(CircleShape)
                      .background(Color.White)
                      .border(1.dp, BorderLight, CircleShape)
                  ) {
                    Icon(Icons.Default.Remove, contentDescription = "Menos semanas", modifier = Modifier.size(16.dp))
                  }
                  Text(
                    text = "$habitRepeatWeeks ${if (habitRepeatWeeks == 1) "semana" else "semanas"}",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimaryLight
                  )
                  IconButton(
                    onClick = { if (habitRepeatWeeks < 8) habitRepeatWeeks++ },
                    modifier = Modifier
                      .size(36.dp)
                      .clip(CircleShape)
                      .background(Color.White)
                      .border(1.dp, BorderLight, CircleShape)
                  ) {
                    Icon(Icons.Default.Add, contentDescription = "Más semanas", modifier = Modifier.size(16.dp))
                  }
                }
              }

              Spacer(modifier = Modifier.height(12.dp))

              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .background(Color(0xFFFAFAFC), RoundedCornerShape(16.dp))
                  .border(1.dp, BorderLight, RoundedCornerShape(16.dp))
                  .padding(14.dp)
              ) {
                Text(
                  text = "Días preferidos:",
                  style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                  color = TextSecondaryLight
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  weekDays.forEach { (abbr, letter) ->
                    val isDaySelected = habitDays.contains(abbr)
                    Surface(
                      shape = CircleShape,
                      color = if (isDaySelected) AccentCharcoal else Color.White,
                      border = BorderStroke(1.dp, if (isDaySelected) AccentCharcoal else BorderLight),
                      modifier = Modifier
                        .size(38.dp)
                        .bouncyClick {
                          if (isDaySelected) {
                            if (habitDays.size > 1) habitDays.remove(abbr)
                          } else {
                            habitDays.add(abbr)
                          }
                        }
                    ) {
                      Box(contentAlignment = Alignment.Center) {
                        Text(
                          text = letter,
                          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                          color = if (isDaySelected) Color.White else TextPrimaryLight
                        )
                      }
                    }
                  }
                }
              }

              Spacer(modifier = Modifier.height(12.dp))

              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .background(Color(0xFFFAFAFC), RoundedCornerShape(16.dp))
                  .border(1.dp, BorderLight, RoundedCornerShape(16.dp))
                  .padding(14.dp)
              ) {
                Text(
                  text = "Revisión:",
                  style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                  color = TextSecondaryLight
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  listOf("Nunca", "En 30 días", "En 90 días").forEach { rule ->
                    val isRuleSelected = habitEndRule == rule
                    Surface(
                      shape = RoundedCornerShape(12.dp),
                      color = if (isRuleSelected) AccentCharcoal else Color.White,
                      border = BorderStroke(1.dp, if (isRuleSelected) AccentCharcoal else BorderLight),
                      modifier = Modifier
                        .weight(1f)
                        .bouncyClick { habitEndRule = rule }
                    ) {
                      Box(
                        modifier = Modifier.padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                      ) {
                        Text(
                          text = rule,
                          style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isRuleSelected) FontWeight.Bold else FontWeight.Medium
                          ),
                          color = if (isRuleSelected) Color.White else TextPrimaryLight
                        )
                      }
                    }
                  }
                }
              }

              Spacer(modifier = Modifier.height(18.dp))

              val saveHabitInteraction = rememberBounceInteractionSource()
              Button(
                onClick = { showHabitCartel = false },
                interactionSource = saveHabitInteraction,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                  containerColor = AccentCharcoal,
                  contentColor = Color.White
                ),
                modifier = Modifier
                  .fillMaxWidth()
                  .height(48.dp)
                  .bouncyScale(saveHabitInteraction)
                  .testTag("btn_confirm_habit_cartel")
              ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "Guardar hábito",
                  style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
              }
            }
          }
        }
      }
    }
  }
}
