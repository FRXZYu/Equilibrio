package com.example.ui.screens

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.domain.HexagonCalculator
import com.example.model.AssessmentReport
import com.example.model.BalanceMission
import com.example.model.DimensionsProvider
import com.example.model.UserProfile
import com.example.ui.components.DualLayerHexagonRadarChart
import com.example.ui.components.bouncyClick
import com.example.ui.theme.AccentCharcoal
import com.example.ui.theme.BorderLight
import com.example.ui.theme.SurfaceCardLight
import com.example.ui.theme.SurfaceVariantLight
import com.example.ui.theme.TextPrimaryLight
import com.example.ui.theme.TextSecondaryLight

/**
 * Nueva pantalla "Hoy" (sustituye al Inicio actual) de arriba abajo:
 * 1. Saludo corto con el nombre.
 * 2. Hexágono grande de dos capas (Percibido punteado + Real relleno).
 * 3. Línea con tono de apoyo: "Aquí hay espacio para crecer: Descanso. Una misión de 10 min lo mueve."
 *    (Sin palabras "debilidad", "fallo" ni "bajo").
 * 4. "La misión de hoy": UNA misión destacada en tarjeta grande con botón completar.
 * 5. Las demás misiones del día, colapsadas bajo "Ver más".
 * 6. Exp, nivel y racha en una barra discreta al final.
 * Sin chip de zodiaco en esta pantalla.
 */
@Composable
fun HoySection(
  userProfile: UserProfile,
  report: AssessmentReport,
  realScores: Map<Int, Int>,
  realScoresFloat: Map<Int, Float>,
  todayCompletedIds: Set<String>,
  allMissions: List<BalanceMission>,
  selectedDimensionForDetail: Int?,
  onSelectDimension: (Int) -> Unit,
  onToggleCompleteMission: (BalanceMission) -> Unit,
  onViewMissionDetails: (BalanceMission) -> Unit,
  modifier: Modifier = Modifier
) {
  val homeScrollState = rememberScrollState()
  var isOtherMissionsExpanded by remember { mutableStateOf(false) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(homeScrollState)
      .padding(horizontal = 20.dp, vertical = 14.dp)
  ) {
    // 1. Saludo corto con el nombre
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = if (userProfile.name.isNotBlank()) "Hola, ${userProfile.name}" else "Hola, Kirenia",
          style = MaterialTheme.typography.displaySmall.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp
          ),
          color = TextPrimaryLight
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = "Tu equilibrio diario en acción",
          style = MaterialTheme.typography.titleMedium.copy(
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
          ),
          color = TextSecondaryLight
        )
      }

      if (userProfile.photoUri != null) {
        AsyncImage(
          model = Uri.parse(userProfile.photoUri),
          contentDescription = "Foto de perfil",
          contentScale = ContentScale.Crop,
          modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .border(2.dp, BorderLight, CircleShape)
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // 2. Hexágono grande de dos capas (Percibido punteado + Real relleno)
    Surface(
      shape = RoundedCornerShape(26.dp),
      color = SurfaceCardLight,
      border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
      shadowElevation = 2.dp,
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 16.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        DualLayerHexagonRadarChart(
          perceivedScores = report.scores,
          realScores = realScoresFloat,
          selectedDimensionIndex = selectedDimensionForDetail,
          onDimensionSelect = onSelectDimension
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // 3. Una línea con tono de apoyo (NUNCA usa palabras "debilidad", "fallo" ni "bajo")
    val lowestDimIndex = remember(realScores, report.scores) {
      HexagonCalculator.findLowestRealDimension(realScores, report.scores)
    }
    val lowestDim = DimensionsProvider.dimensions.getOrNull(lowestDimIndex) ?: DimensionsProvider.dimensions[0]

    Surface(
      shape = RoundedCornerShape(16.dp),
      color = SurfaceVariantLight,
      border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(10.dp)
            .clip(CircleShape)
            .background(lowestDim.accentColor)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
          text = "Aquí hay espacio para crecer: ${lowestDim.title}. Una misión de 10 min lo mueve.",
          style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
          color = TextPrimaryLight
        )
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // 4. "La misión de hoy": UNA misión destacada en tarjeta grande con botón completar
    val uncompletedInLowest = allMissions.filter {
      it.dimensionIndex == lowestDimIndex && !it.isCompleted
    }
    val todayFeaturedMission = uncompletedInLowest.firstOrNull()
      ?: allMissions.firstOrNull { !it.isCompleted }

    Text(
      text = "La misión de hoy",
      style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
      color = TextPrimaryLight
    )

    Spacer(modifier = Modifier.height(8.dp))

    if (todayFeaturedMission != null) {
      FeaturedMissionBigCard(
        mission = todayFeaturedMission,
        isCompleted = todayFeaturedMission.isCompleted,
        onToggleComplete = { onToggleCompleteMission(todayFeaturedMission) },
        onViewDetails = { onViewMissionDetails(todayFeaturedMission) }
      )
    } else {
      // Todas las misiones completadas hoy
      Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFF0FDF4),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(18.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = Color(0xFF16A34A),
            modifier = Modifier.size(28.dp)
          )
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text(
              text = "¡Todas tus misiones de hoy están cumplidas!",
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
              color = Color(0xFF15803D)
            )
            Text(
              text = "Tu hexágono real refleja tu dedicación. ¡Sigue así mañana!",
              style = MaterialTheme.typography.bodySmall,
              color = Color(0xFF166534)
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // 5. Las demás misiones del día, colapsadas bajo "Ver más"
    val otherMissions = allMissions.filter { it.id != todayFeaturedMission?.id }
    if (otherMissions.isNotEmpty()) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { isOtherMissionsExpanded = !isOtherMissionsExpanded }
          .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Misiones del día (${otherMissions.size})",
          style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
          color = TextSecondaryLight
        )

        Surface(
          shape = RoundedCornerShape(12.dp),
          color = SurfaceVariantLight,
          modifier = Modifier.bouncyClick { isOtherMissionsExpanded = !isOtherMissionsExpanded }
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = if (isOtherMissionsExpanded) "Ocultar" else "Ver más",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = AccentCharcoal
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
              imageVector = if (isOtherMissionsExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
              contentDescription = null,
              tint = AccentCharcoal,
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }

      AnimatedVisibility(visible = isOtherMissionsExpanded) {
        Column(
          modifier = Modifier.padding(top = 8.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          otherMissions.forEach { mission ->
            CompactMissionCard(
              mission = mission,
              onContinue = { onViewMissionDetails(mission) }
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // 6. Exp, nivel y racha en una barra discreta al final
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = SurfaceVariantLight,
      border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Nivel y rango
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = null,
            tint = Color(0xFFF59E0B),
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Column {
            Text(
              text = "Nivel ${userProfile.level}",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
              color = TextPrimaryLight
            )
            Text(
              text = userProfile.rank,
              style = MaterialTheme.typography.labelSmall,
              color = TextSecondaryLight
            )
          }
        }

        Box(
          modifier = Modifier
            .width(1.dp)
            .height(28.dp)
            .background(BorderLight)
        )

        // EXP total y hoy
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Filled.ElectricBolt,
            contentDescription = null,
            tint = AccentCharcoal,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Column {
            Text(
              text = "${userProfile.exp} EXP",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
              color = TextPrimaryLight
            )
            Text(
              text = "+${userProfile.expToday} hoy",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
              color = Color(0xFF10B981)
            )
          }
        }

        Box(
          modifier = Modifier
            .width(1.dp)
            .height(28.dp)
            .background(BorderLight)
        )

        // Racha
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = androidx.compose.material.icons.Icons.Outlined.LocalFireDepartment,
            contentDescription = null,
            tint = Color(0xFFF59E0B),
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Column {
            Text(
              text = "${userProfile.activeStreakDays} días",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
              color = TextPrimaryLight
            )
            Text(
              text = if (userProfile.activeStreakDays > 0) "Racha activa" else "Comienza hoy",
              style = MaterialTheme.typography.labelSmall,
              color = TextSecondaryLight
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(110.dp))
  }
}

/**
 * Tarjeta grande para "La misión de hoy"
 */
@Composable
fun FeaturedMissionBigCard(
  mission: BalanceMission,
  isCompleted: Boolean,
  onToggleComplete: () -> Unit,
  onViewDetails: () -> Unit,
  modifier: Modifier = Modifier
) {
  val dimension = DimensionsProvider.dimensions.getOrNull(mission.dimensionIndex)
  val accentColor = dimension?.accentColor ?: AccentCharcoal

  Surface(
    shape = RoundedCornerShape(22.dp),
    color = SurfaceCardLight,
    border = androidx.compose.foundation.BorderStroke(
      1.5.dp,
      if (isCompleted) Color(0xFF10B981) else BorderLight
    ),
    shadowElevation = 2.dp,
    modifier = modifier.fillMaxWidth()
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(18.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Tag con color de dimensión
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = accentColor.copy(alpha = 0.12f)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(accentColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = mission.dimensionTitle,
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = accentColor
            )
          }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          if (isCompleted) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = Color(0xFFDCFCE7)
            ) {
              Text(
                text = "✓ Completada",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF15803D),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
              )
            }
            Spacer(modifier = Modifier.width(6.dp))
          }

          Surface(
            shape = RoundedCornerShape(8.dp),
            color = SurfaceVariantLight
          ) {
            Text(
              text = mission.durationLabel,
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
              color = TextSecondaryLight,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
          }

          Spacer(modifier = Modifier.width(6.dp))

          Surface(
            shape = RoundedCornerShape(8.dp),
            color = AccentCharcoal.copy(alpha = 0.08f)
          ) {
            Text(
              text = "+${mission.expReward} EXP",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = AccentCharcoal,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      Text(
        text = mission.title,
        style = MaterialTheme.typography.titleLarge.copy(
          fontWeight = FontWeight.Bold,
          fontSize = 18.sp
        ),
        color = TextPrimaryLight
      )

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = mission.summary.ifEmpty { mission.description },
        style = MaterialTheme.typography.bodyMedium,
        color = TextSecondaryLight,
        lineHeight = 20.sp
      )

      Spacer(modifier = Modifier.height(16.dp))

      // Único botón en tarjeta: Ver Detalles
      Button(
        onClick = onViewDetails,
        colors = ButtonDefaults.buttonColors(
          containerColor = accentColor,
          contentColor = Color.White
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Icon(
          imageVector = Icons.Outlined.Info,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "Ver Detalles",
          style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
          color = Color.White
        )
      }
    }
  }
}
