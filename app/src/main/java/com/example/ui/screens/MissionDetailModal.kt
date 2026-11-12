package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.FormatListNumbered
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.model.GamificationStats
import com.example.ui.components.bouncyClick
import com.example.ui.theme.AccentCharcoal
import com.example.ui.theme.BorderLight
import com.example.ui.theme.SurfaceCardLight
import com.example.ui.theme.SurfaceVariantLight
import com.example.ui.theme.TextPrimaryLight
import com.example.ui.theme.TextSecondaryLight
import com.example.ui.theme.TextTertiaryLight

/**
 * Modal interactivo detallado para una misión.
 * Despliega información bien estructurada ("¿Cómo hacerlo?"), pasos guiados,
 * justificación de bienestar, multiplicador 1.5x, límite diario y acciones
 * de "Completar" o "Guardar para más tarde".
 */
@Composable
fun MissionDetailModal(
  mission: BalanceMission,
  stats: GamificationStats,
  onDismiss: () -> Unit,
  onComplete: () -> Unit,
  onSaveForLater: () -> Unit
) {
  val dimension = DimensionsProvider.dimensions.getOrNull(mission.dimensionIndex)
  val accentColor = dimension?.accentColor ?: AccentCharcoal

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(Color.Black.copy(alpha = 0.45f))
        .padding(16.dp),
      contentAlignment = Alignment.Center
    ) {
      Surface(
        shape = RoundedCornerShape(28.dp),
        color = Color(0xFFFAFAFC),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
        shadowElevation = 18.dp,
        modifier = Modifier
          .fillMaxWidth()
          .heightIn(max = 720.dp)
          .testTag("mission_detail_modal")
      ) {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 22.dp, vertical = 20.dp)
        ) {
          // Encabezado: Categoría + Multiplicador + Cerrar
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
                color = accentColor.copy(alpha = 0.15f)
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

              // Badge 1.5x si es área débil
              if (mission.hasMultiplier) {
                Surface(
                  shape = RoundedCornerShape(10.dp),
                  color = Color(0xFFFEF3C7),
                  border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A))
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(
                      imageVector = Icons.Default.ElectricBolt,
                      contentDescription = null,
                      tint = Color(0xFFD97706),
                      modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                      text = "1.5x EXP (Debilidad)",
                      style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                      color = Color(0xFFB45309)
                    )
                  }
                }
              }
            }

            IconButton(
              onClick = onDismiss,
              modifier = Modifier
                .size(34.dp)
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

          // Contenido con scroll
          Column(
            modifier = Modifier
              .weight(1f)
              .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
          ) {
            // Título de la misión
            Text(
              text = mission.title,
              style = MaterialTheme.typography.headlineSmall.copy(
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 28.sp
              ),
              color = TextPrimaryLight
            )

            // Breve contexto
            Text(
              text = mission.description,
              style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp, lineHeight = 22.sp),
              color = TextSecondaryLight
            )

            // Duración y EXP
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
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
                    text = "Duración: ${mission.durationLabel}",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = TextPrimaryLight
                  )
                }
              }

              Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (mission.hasMultiplier) Color(0xFFFEF3C7) else Color.White,
                border = androidx.compose.foundation.BorderStroke(
                  1.dp,
                  if (mission.hasMultiplier) Color(0xFFFDE68A) else BorderLight
                )
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = "Recompensa: +${mission.expReward} EXP",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (mission.hasMultiplier) Color(0xFF92400E) else TextPrimaryLight
                  )
                }
              }
            }

            // SECCIÓN: ¿CÓMO HACERLO? (Pasos estructurados)
            Surface(
              shape = RoundedCornerShape(20.dp),
              color = Color.White,
              border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(18.dp)) {
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

                Spacer(modifier = Modifier.height(14.dp))

                val stepsToRender = if (mission.steps.isNotEmpty()) {
                  mission.steps
                } else {
                  listOf(
                    "Paso 1 (Preparación): Reserva el tiempo necesario y desconéctate de multitareas.",
                    "Paso 2 (Ejecución): Realiza la actividad con presencia plena y serenidad.",
                    "Paso 3 (Reflexión): Reconoce el beneficio inmediato de haber cumplido con este hábito."
                  )
                }

                stepsToRender.forEachIndexed { idx, stepText ->
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(vertical = 5.dp),
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

            // SECCIÓN: IMPACTO EN TU BIENESTAR
            Surface(
              shape = RoundedCornerShape(20.dp),
              color = Color(0xFFF0FDF4),
              border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Outlined.Spa,
                    contentDescription = null,
                    tint = Color(0xFF16A34A),
                    modifier = Modifier.size(18.dp)
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = "Por qué funciona",
                    style = MaterialTheme.typography.labelMedium.copy(
                      fontWeight = FontWeight.Bold,
                      letterSpacing = 0.5.sp
                    ),
                    color = Color(0xFF15803D)
                  )
                }
                Spacer(modifier = Modifier.height(6.dp))
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

            // LÍMITE DIARIO DE EXP INFO BANNER
            Surface(
              shape = RoundedCornerShape(14.dp),
              color = if (stats.isDailyCapReached) Color(0xFFFFFBEB) else Color.White,
              border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (stats.isDailyCapReached) Color(0xFFFDE68A) else BorderLight
              ),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Outlined.Info,
                  contentDescription = null,
                  tint = if (stats.isDailyCapReached) Color(0xFFD97706) else TextSecondaryLight,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = if (stats.isDailyCapReached) {
                    "Límite diario alcanzado (${stats.expToday}/${GamificationStats.DAILY_EXP_CAP} EXP). Esta misión sumará a tus misiones completadas y racha sin saturarte."
                  } else {
                    "Progreso diario: ${stats.expToday}/${GamificationStats.DAILY_EXP_CAP} EXP disponibles hoy."
                  },
                  style = MaterialTheme.typography.bodySmall.copy(
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Medium
                  ),
                  color = if (stats.isDailyCapReached) Color(0xFFB45309) else TextSecondaryLight
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(18.dp))

          // BOTONES DE ACCIÓN: COMPLETAR O GUARDAR PARA MÁS TARDE
          Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Botón 1: Completar
            Button(
              onClick = onComplete,
              shape = RoundedCornerShape(16.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = if (mission.isCompleted) Color(0xFF10B981) else AccentCharcoal,
                contentColor = Color.White
              ),
              modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .bouncyClick()
                .testTag("btn_complete_mission_detail")
            ) {
              Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = if (mission.isCompleted) "¡Misión Completada!" else "Completar Misión",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
              )
            }

            // Botón 2: Guardar para más tarde (sale de la misión y permite buscar otra)
            OutlinedButton(
              onClick = onSaveForLater,
              shape = RoundedCornerShape(16.dp),
              colors = ButtonDefaults.outlinedButtonColors(
                contentColor = AccentCharcoal
              ),
              border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .bouncyClick()
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
                text = if (mission.isSavedForLater) "Quitar de guardadas y buscar otra" else "Guardar para más tarde",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
              )
            }
          }
        }
      }
    }
  }
}
