package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BalanceMission
import com.example.model.DimensionsProvider
import com.example.ui.components.bouncyScale
import com.example.ui.components.rememberBounceInteractionSource
import com.example.ui.theme.AccentCharcoal
import com.example.ui.theme.BorderLight
import com.example.ui.theme.SurfaceCardLight
import com.example.ui.theme.SurfaceVariantLight
import com.example.ui.theme.TextPrimaryLight
import com.example.ui.theme.TextSecondaryLight
import com.example.ui.theme.TextTertiaryLight

/**
 * Pantalla "Explorar": biblioteca serena de acciones organizadas por:
 * - Para tu enfoque (áreas elegidas o recomendadas suavemente)
 * - Las 6 áreas del hexágono
 * - Guardadas para otro momento
 * - Tus hábitos y acciones propias
 * Sin EXP, sin multiplicadores artificiales y sin límites punitivos.
 */
@Composable
fun MissionsSection(
  missions: List<BalanceMission>,
  chosenFocusDimensions: Set<Int>,
  onViewMissionDetails: (BalanceMission) -> Unit,
  onOpenFocusPicker: () -> Unit,
  onOpenCreateMission: () -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedCategoryIndex by remember { mutableIntStateOf(0) }
  val haptic = LocalHapticFeedback.current

  val categories = remember {
    listOf(
      "Para tu enfoque",
      "Autoestima",
      "Ejercicio",
      "Descanso",
      "Relaciones",
      "Hobbies",
      "Excelencia",
      "Guardadas",
      "Tus creaciones"
    )
  }

  val filteredMissions = remember(selectedCategoryIndex, missions, chosenFocusDimensions) {
    when (selectedCategoryIndex) {
      0 -> if (chosenFocusDimensions.isNotEmpty()) {
        missions.filter { it.dimensionIndex in chosenFocusDimensions }
      } else {
        missions.filter { it.isPriorityFocus }
      }
      in 1..6 -> missions.filter { it.dimensionIndex == selectedCategoryIndex - 1 }
      7 -> missions.filter { it.isSavedForLater }
      8 -> missions.filter { it.isCustom || it.isHabit }
      else -> missions
    }
  }

  val createBtnInteraction = rememberBounceInteractionSource()
  val focusBtnInteraction = rememberBounceInteractionSource()

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 20.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    item(key = "explore_header") {
      Column(modifier = Modifier.padding(top = 12.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Explorar acciones",
              style = MaterialTheme.typography.headlineMedium.copy(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
              ),
              color = TextPrimaryLight
            )
            Text(
              text = "Elige pasos pequeños que encajen con tu día",
              style = MaterialTheme.typography.bodySmall,
              color = TextSecondaryLight
            )
          }

          Surface(
            onClick = {
              haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              onOpenCreateMission()
            },
            interactionSource = createBtnInteraction,
            color = AccentCharcoal,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .minimumInteractiveComponentSize()
              .bouncyScale(createBtnInteraction, scaleDown = 0.92f)
              .testTag("btn_missions_create")
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "Crear",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          itemsIndexed(categories) { index, catName ->
            val isSelected = selectedCategoryIndex == index
            val chipInteraction = rememberBounceInteractionSource()
            val chipBg by animateColorAsState(
              targetValue = if (isSelected) AccentCharcoal else Color.White,
              animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
              label = "cat_bg_$index"
            )
            val chipTextColor by animateColorAsState(
              targetValue = if (isSelected) Color.White else TextPrimaryLight,
              label = "cat_text_$index"
            )

            Surface(
              onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                selectedCategoryIndex = index
              },
              interactionSource = chipInteraction,
              shape = RoundedCornerShape(14.dp),
              color = chipBg,
              border = BorderStroke(1.dp, if (isSelected) AccentCharcoal else BorderLight),
              modifier = Modifier
                .minimumInteractiveComponentSize()
                .bouncyScale(chipInteraction, scaleDown = 0.93f)
                .testTag("mission_category_$index")
            ) {
              Text(
                text = catName,
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                ),
                color = chipTextColor,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (selectedCategoryIndex) {
          0 -> {
            Surface(
              onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onOpenFocusPicker()
              },
              interactionSource = focusBtnInteraction,
              shape = RoundedCornerShape(16.dp),
              color = SurfaceVariantLight,
              border = BorderStroke(1.dp, BorderLight),
              modifier = Modifier
                .fillMaxWidth()
                .bouncyScale(focusBtnInteraction, scaleDown = 0.98f)
            ) {
              Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.weight(1f)
                ) {
                  Icon(
                    imageVector = Icons.Outlined.Explore,
                    contentDescription = null,
                    tint = AccentCharcoal,
                    modifier = Modifier.size(18.dp)
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = if (chosenFocusDimensions.isNotEmpty()) {
                      "Mostrando acciones de tu enfoque actual. Toca para cambiar áreas."
                    } else {
                      "Sugerencias suaves según tu mapa. Toca para elegir tus áreas."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondaryLight
                  )
                }
              }
            }
          }

          7 -> if (filteredMissions.isEmpty()) {
            EmptyExploreNote("Aún no tienes acciones guardadas. Abre cualquier tarjeta y toca 'Guardar para otro momento'.")
          }

          8 -> if (filteredMissions.isEmpty()) {
            EmptyExploreNote("Crea tus propios pasos o hábitos con el botón 'Crear'.")
          }
        }
      }
    }

    items(filteredMissions, key = { it.id }) { mission ->
      CompactMissionCard(
        mission = mission,
        onContinue = { onViewMissionDetails(mission) }
      )
    }

    item(key = "bottom_space") {
      Spacer(modifier = Modifier.height(104.dp))
    }
  }
}

@Composable
private fun EmptyExploreNote(message: String) {
  Surface(
    shape = RoundedCornerShape(16.dp),
    color = SurfaceCardLight,
    border = BorderStroke(1.dp, BorderLight),
    modifier = Modifier.fillMaxWidth()
  ) {
    Text(
      text = message,
      style = MaterialTheme.typography.bodySmall,
      color = TextSecondaryLight,
      modifier = Modifier.padding(14.dp)
    )
  }
}

@Composable
internal fun CompactMissionCard(
  mission: BalanceMission,
  onContinue: () -> Unit
) {
  val dimension = DimensionsProvider.dimensions.getOrNull(mission.dimensionIndex)
  val accentColor = dimension?.accentColor ?: AccentCharcoal
  val cardInteraction = rememberBounceInteractionSource()
  val btnInteraction = rememberBounceInteractionSource()

  val cardBg by animateColorAsState(
    targetValue = if (mission.isCompleted) Color(0xFFF0FDF4) else SurfaceCardLight,
    animationSpec = tween(durationMillis = 260),
    label = "card_bg_${mission.id}"
  )
  val borderColor by animateColorAsState(
    targetValue = if (mission.isCompleted) Color(0xFFBBF7D0) else BorderLight,
    label = "card_border_${mission.id}"
  )

  Surface(
    onClick = onContinue,
    interactionSource = cardInteraction,
    shape = RoundedCornerShape(22.dp),
    color = cardBg,
    border = BorderStroke(1.dp, borderColor),
    shadowElevation = if (mission.isCompleted) 0.dp else 1.dp,
    modifier = Modifier
      .fillMaxWidth()
      .bouncyScale(cardInteraction, scaleDown = 0.97f)
      .testTag("mission_card_${mission.id}")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = if (mission.isCompleted) Color(0xFFDCFCE7) else accentColor.copy(alpha = 0.12f)
        ) {
          Text(
            text = mission.dimensionTitle.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = if (mission.isCompleted) Color(0xFF166534) else accentColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
          )
        }

        Row(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          if (mission.isHabit || mission.isCustom) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = SurfaceVariantLight
            ) {
              Text(
                text = if (mission.isHabit) "Hábito" else "Propia",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = TextSecondaryLight,
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
              )
            }
          }

          if (mission.isCompleted) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = Color(0xFFDCFCE7)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Filled.CheckCircle,
                  contentDescription = null,
                  tint = Color(0xFF15803D),
                  modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "Hecha hoy",
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                  color = Color(0xFF166534)
                )
              }
            }
          } else if (mission.isSavedForLater) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = SurfaceVariantLight
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.Bookmark,
                  contentDescription = null,
                  tint = AccentCharcoal,
                  modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                  text = "Guardada",
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                  color = AccentCharcoal
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = mission.title,
        style = MaterialTheme.typography.titleMedium.copy(
          fontWeight = FontWeight.Bold,
          fontSize = 16.5.sp
        ),
        color = TextPrimaryLight
      )

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = mission.summary.ifEmpty { mission.description },
        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 19.sp),
        color = TextSecondaryLight,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
      )

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Timer,
            contentDescription = null,
            tint = TextTertiaryLight,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = mission.durationLabel,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondaryLight
          )
        }

        Button(
          onClick = onContinue,
          interactionSource = btnInteraction,
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = if (mission.isCompleted) Color(0xFFDCFCE7) else AccentCharcoal,
            contentColor = if (mission.isCompleted) Color(0xFF166534) else Color.White
          ),
          contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
          modifier = Modifier
            .height(40.dp)
            .bouncyScale(btnInteraction, scaleDown = 0.92f)
            .testTag("btn_continue_mission_${mission.id}")
        ) {
          Text(
            text = if (mission.isCompleted) "Revisar" else "Ver pasos",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            modifier = Modifier.size(14.dp)
          )
        }
      }
    }
  }
}
