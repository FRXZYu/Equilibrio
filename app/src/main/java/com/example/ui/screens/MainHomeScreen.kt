package com.example.ui.screens

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.graphicsLayer
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.AssignmentTurnedIn
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.EventRepeat
import androidx.compose.material.icons.outlined.Hexagon
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.AssessmentReport
import com.example.model.BalanceMission
import com.example.model.DimensionsProvider
import com.example.model.MissionsProvider
import com.example.model.UserProfile
import com.example.ui.components.bouncyClick
import com.example.ui.theme.AccentCharcoal
import com.example.ui.theme.BorderLight
import com.example.ui.theme.SurfaceCardLight
import com.example.ui.theme.SurfaceVariantLight
import com.example.ui.theme.TextPrimaryLight
import com.example.ui.theme.TextSecondaryLight
import com.example.ui.theme.TextTertiaryLight
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MainHomeScreen(
  userProfile: UserProfile,
  report: AssessmentReport,
  onOpenReport: () -> Unit,
  onUpdateGoal: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedTab by remember { mutableIntStateOf(2) } // 0: Misiones, 1: Habilidades, 2: Inicio, 3: Plan, 4: Logros
  var showEditGoalDialog by remember { mutableStateOf(false) }
  var editedGoal by remember { mutableStateOf(userProfile.monthlyGoal) }

  // "+" FAB Expansion state
  var isFabExpanded by remember { mutableStateOf(false) }
  var showCreateMissionDialog by remember { mutableStateOf(false) }

  // Missions state (tailored to user's weakest dimensions)
  val missionsList = remember(report.scores) {
    mutableStateListOf<BalanceMission>().apply {
      addAll(MissionsProvider.generateMissionsForUser(report.scores))
    }
  }

  // Dynamic user stats based on completed missions
  var currentExp by remember { mutableIntStateOf(userProfile.exp) }
  var completedMissionsCount by remember { mutableIntStateOf(userProfile.completedMissions) }

  val homeScrollState = rememberScrollState()

  // Clean, radiant, minimalist white canvas
  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFFFAFAFC))
  ) {
    // Main Content
    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
    ) {
      Box(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
      ) {
        if (selectedTab == 0) {
          // ==========================================
          // APARTADO DE MISIONES (ENFOCADO EN DEBILIDADES)
          // ==========================================
          MissionsSection(
            report = report,
            missions = missionsList,
            onToggleMission = { mission ->
              val index = missionsList.indexOfFirst { it.id == mission.id }
              if (index >= 0) {
                val newStatus = !missionsList[index].isCompleted
                missionsList[index] = missionsList[index].copy(isCompleted = newStatus)
                if (newStatus) {
                  currentExp += mission.expReward
                  completedMissionsCount += 1
                } else {
                  currentExp = (currentExp - mission.expReward).coerceAtLeast(userProfile.exp)
                  completedMissionsCount = (completedMissionsCount - 1).coerceAtLeast(userProfile.completedMissions)
                }
              }
            },
            onOpenReport = onOpenReport,
            onOpenCreateMission = { showCreateMissionDialog = true }
          )
        } else {
          // ==========================================
          // INICIO: ESTRUCTURA DE LA IMAGEN DE REFERENCIA
          // ==========================================
          Column(
            modifier = Modifier
              .fillMaxSize()
              .verticalScroll(homeScrollState)
              .padding(horizontal = 24.dp, vertical = 16.dp)
          ) {
            // 1. Header: Greeting & Core Purpose of Personal Balance
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = if (userProfile.name.isNotEmpty()) "Hi! ${userProfile.name}" else "Hi! Kirenia",
                  style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                  ),
                  color = TextPrimaryLight
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = "Es hora de cultivar tu equilibrio",
                  style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                  ),
                  color = TextSecondaryLight
                )
              }

              // Subtle zodiac chip
              if (userProfile.zodiacSymbol.isNotEmpty()) {
                Surface(
                  shape = RoundedCornerShape(12.dp),
                  color = SurfaceCardLight,
                  border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(
                      text = userProfile.zodiacSymbol,
                      style = MaterialTheme.typography.titleMedium,
                      color = AccentCharcoal
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = userProfile.zodiacSign,
                      style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                      color = TextSecondaryLight
                    )
                  }
                }
              }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Star Level & Progress Bar (Minimalist Light Styling)
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.fillMaxWidth()
            ) {
              // Golden 8-pointed Star Badge
              Box(
                modifier = Modifier.size(52.dp),
                contentAlignment = Alignment.Center
              ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                  drawEightPointStar(
                    center = Offset(size.width / 2f, size.height / 2f),
                    outerRadius = size.minDimension / 2f,
                    innerRadius = size.minDimension / 3.4f,
                    color = Color(0xFFF59E0B) // Radiant Gold Star
                  )
                }
                Text(
                  text = "${userProfile.level}",
                  style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                  ),
                  color = Color.White
                )
              }

              Spacer(modifier = Modifier.width(10.dp))

              // Minimalist Progress Bar with Rank
              Box(
                modifier = Modifier
                  .weight(1f)
                  .height(36.dp)
                  .clip(RoundedCornerShape(18.dp))
                  .background(SurfaceCardLight)
                  .border(1.dp, BorderLight, RoundedCornerShape(18.dp))
                  .padding(horizontal = 4.dp),
                contentAlignment = Alignment.CenterStart
              ) {
                // Active progress bar fill
                Box(
                  modifier = Modifier
                    .fillMaxWidth(0.42f)
                    .height(28.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(AccentCharcoal)
                )

                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = "Nivel 2",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                  )
                  Text(
                    text = userProfile.rank,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = TextPrimaryLight
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // 3. Hero Section Side-by-Side: Circular Photo + Spiderweb Hexagon Radar
            Surface(
              shape = RoundedCornerShape(26.dp),
              color = SurfaceCardLight,
              border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
              shadowElevation = 2.dp,
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                // Left: Circular Profile Picture
                Box(
                  modifier = Modifier
                    .size(126.dp)
                    .shadow(4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color(0xFFFFDFBA))
                    .border(3.dp, BorderLight, CircleShape),
                  contentAlignment = Alignment.Center
                ) {
                  if (userProfile.photoUri != null) {
                    AsyncImage(
                      model = Uri.parse(userProfile.photoUri),
                      contentDescription = "Foto de perfil",
                      contentScale = ContentScale.Crop,
                      modifier = Modifier.fillMaxSize()
                    )
                  } else {
                    IllustratedAvatar()
                  }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Right: Spiderweb Hexagonal Radar (Live statistical representation)
                Column(
                  horizontalAlignment = Alignment.CenterHorizontally,
                  modifier = Modifier
                    .weight(1f)
                    .bouncyClick { onOpenReport() }
                    .testTag("spiderweb_hexagon_widget")
                ) {
                  SpiderwebHexagonCanvas(
                    scores = report.scores,
                    modifier = Modifier.size(136.dp)
                  )
                  Spacer(modifier = Modifier.height(6.dp))
                  Text(
                    text = "Toca para ver análisis",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    color = TextTertiaryLight
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 4. Stats Row Card (EXP, Misiones, Día) - Pristine White Minimalist Card
            Surface(
              shape = RoundedCornerShape(24.dp),
              color = SurfaceCardLight,
              border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
              shadowElevation = 2.dp,
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 18.dp, horizontal = 14.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
              ) {
                // Column 1: EXP
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Text(
                    text = "EXP",
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontWeight = FontWeight.Bold,
                      letterSpacing = 1.sp
                    ),
                    color = TextTertiaryLight
                  )
                  Spacer(modifier = Modifier.height(2.dp))
                  Text(
                    text = "+$currentExp",
                    style = MaterialTheme.typography.headlineSmall.copy(
                      fontWeight = FontWeight.Bold,
                      fontSize = 22.sp
                    ),
                    color = TextPrimaryLight
                  )
                  Text(
                    text = "+${userProfile.expToday} hoy",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF10B981)
                  )
                }

                // Vertical Divider
                Box(
                  modifier = Modifier
                    .width(1.dp)
                    .height(42.dp)
                    .background(BorderLight)
                )

                // Column 2: Misiones
                Column(
                  horizontalAlignment = Alignment.CenterHorizontally,
                  modifier = Modifier.clickable { selectedTab = 0 }
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      imageVector = Icons.Default.CheckCircle,
                      contentDescription = null,
                      tint = Color(0xFF10B981),
                      modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                      text = "Misiones",
                      style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                      color = TextTertiaryLight
                    )
                  }
                  Spacer(modifier = Modifier.height(2.dp))
                  Text(
                    text = "$completedMissionsCount",
                    style = MaterialTheme.typography.headlineSmall.copy(
                      fontWeight = FontWeight.Bold,
                      fontSize = 22.sp
                    ),
                    color = TextPrimaryLight
                  )
                  Text(
                    text = "Completadas",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondaryLight
                  )
                }

                // Vertical Divider
                Box(
                  modifier = Modifier
                    .width(1.dp)
                    .height(42.dp)
                    .background(BorderLight)
                )

                // Column 3: Día / Racha
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Text(
                    text = "DÍA",
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontWeight = FontWeight.Bold,
                      letterSpacing = 1.sp
                    ),
                    color = TextTertiaryLight
                  )
                  Spacer(modifier = Modifier.height(2.dp))
                  Text(
                    text = "${userProfile.activeStreakDays}",
                    style = MaterialTheme.typography.headlineSmall.copy(
                      fontWeight = FontWeight.Bold,
                      fontSize = 22.sp
                    ),
                    color = TextPrimaryLight
                  )
                  Text(
                    text = "Racha Activa",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFFF59E0B)
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 5. Purpose & Goal Card ("¿Por qué empezaste?")
            Surface(
              shape = RoundedCornerShape(24.dp),
              color = SurfaceCardLight,
              border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
              shadowElevation = 2.dp,
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(20.dp)
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                      modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(AccentCharcoal.copy(alpha = 0.08f)),
                      contentAlignment = Alignment.Center
                    ) {
                      Icon(
                        imageVector = Icons.Default.Explore,
                        contentDescription = null,
                        tint = AccentCharcoal,
                        modifier = Modifier.size(16.dp)
                      )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                      text = "¿Por qué empezaste?",
                      style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                      ),
                      color = TextSecondaryLight
                    )
                  }

                  IconButton(
                    onClick = { showEditGoalDialog = true },
                    modifier = Modifier.size(32.dp)
                  ) {
                    Icon(
                      imageVector = Icons.Default.Edit,
                      contentDescription = "Editar meta",
                      tint = AccentCharcoal,
                      modifier = Modifier.size(16.dp)
                    )
                  }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                  text = "“${userProfile.monthlyGoal}”",
                  style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    lineHeight = 26.sp
                  ),
                  color = TextPrimaryLight
                )
              }
            }

            Spacer(modifier = Modifier.height(120.dp))
          }
        }
      }

      // ==========================================
      // CENTRED, FLOATING NAVIGATION BAR WITH EQUAL BALANCED ICONS
      // ==========================================
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .navigationBarsPadding()
          .padding(horizontal = 18.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
      ) {
        Surface(
          shape = RoundedCornerShape(32.dp),
          color = AccentCharcoal,
          shadowElevation = 8.dp,
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Tab 0: Misiones
            NavBottomItem(
              icon = Icons.Outlined.TaskAlt,
              label = "Misiones",
              isSelected = selectedTab == 0,
              modifier = Modifier.weight(1f),
              onClick = { selectedTab = 0 }
            )

            // Tab 1: Habilidades
            NavBottomItem(
              icon = Icons.Outlined.Hexagon,
              label = "Habilidades",
              isSelected = selectedTab == 1,
              modifier = Modifier.weight(1f),
              onClick = {
                selectedTab = 1
                onOpenReport()
              }
            )

            // Tab 2: Inicio
            NavBottomItem(
              icon = Icons.Outlined.Person,
              label = "Inicio",
              isSelected = selectedTab == 2,
              modifier = Modifier.weight(1f),
              onClick = { selectedTab = 2 }
            )

            // Tab 3: Plan
            NavBottomItem(
              icon = Icons.Outlined.CalendarMonth,
              label = "Plan",
              isSelected = selectedTab == 3,
              modifier = Modifier.weight(1f),
              onClick = { selectedTab = 3 }
            )

            // Tab 4: Logros
            NavBottomItem(
              icon = Icons.Outlined.EmojiEvents,
              label = "Logros",
              isSelected = selectedTab == 4,
              modifier = Modifier.weight(1f),
              onClick = { selectedTab = 4 }
            )
          }
        }
      }
    }

    // ==========================================
    // FLOATING ACTION BUTTON (+) WITH PILL POPUP
    // Elevated safely above the action bar with ample clearance
    // ==========================================
    Column(
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .navigationBarsPadding()
        .padding(bottom = 104.dp, end = 20.dp),
      horizontalAlignment = Alignment.End
    ) {
      // Deployed Material Design Pill: "Misión"
      AnimatedVisibility(
        visible = isFabExpanded,
        enter = fadeIn() + scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + slideInVertically { it / 2 },
        exit = fadeOut() + scaleOut() + slideOutVertically { it / 2 }
      ) {
        Surface(
          shape = RoundedCornerShape(26.dp),
          color = AccentCharcoal,
          shadowElevation = 8.dp,
          border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
          modifier = Modifier
            .padding(bottom = 12.dp)
            .bouncyClick {
              isFabExpanded = false
              showCreateMissionDialog = true
            }
            .testTag("fab_mission_pill")
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp)
          ) {
            Icon(
              imageVector = Icons.Outlined.Checklist,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Misión",
              style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
              ),
              color = Color.White
            )
          }
        }
      }

      // Main FAB (+)
      val fabInteractionSource = remember { MutableInteractionSource() }
      val isFabPressed by fabInteractionSource.collectIsPressedAsState()
      val fabScale by animateFloatAsState(
        targetValue = if (isFabPressed) 0.88f else 1f,
        animationSpec = spring(
          dampingRatio = Spring.DampingRatioMediumBouncy,
          stiffness = Spring.StiffnessLow
        ),
        label = "fab_bounce"
      )

      val rotationDegree by animateFloatAsState(
        targetValue = if (isFabExpanded) 45f else 0f,
        animationSpec = spring(
          dampingRatio = Spring.DampingRatioMediumBouncy,
          stiffness = Spring.StiffnessLow
        ),
        label = "fab_rotation"
      )

      FloatingActionButton(
        onClick = { isFabExpanded = !isFabExpanded },
        interactionSource = fabInteractionSource,
        containerColor = AccentCharcoal,
        contentColor = Color.White,
        shape = CircleShape,
        modifier = Modifier
          .size(54.dp)
          .graphicsLayer {
            scaleX = fabScale
            scaleY = fabScale
          }
          .shadow(6.dp, CircleShape)
          .testTag("fab_add_mission")
      ) {
        Icon(
          imageVector = Icons.Default.Add,
          contentDescription = if (isFabExpanded) "Cerrar" else "Añadir misión",
          modifier = Modifier
            .size(26.dp)
            .rotate(rotationDegree)
        )
      }
    }
  }

  // Dialog to Create Custom Mission & Habit
  if (showCreateMissionDialog) {
    CreateMissionDialog(
      onDismiss = { showCreateMissionDialog = false },
      onMissionCreated = { newMission ->
        missionsList.add(0, newMission)
        showCreateMissionDialog = false
        selectedTab = 0 // Switch to Misiones tab so user immediately sees their created mission!
      }
    )
  }

  // Edit Goal Dialog ("Puedes cambiarlo después")
  if (showEditGoalDialog) {
    AlertDialog(
      onDismissRequest = { showEditGoalDialog = false },
      title = {
        Text("Tu Meta para el Mes", style = MaterialTheme.typography.titleLarge)
      },
      text = {
        Column {
          Text(
            text = "Puedes cambiarlo después cuando tus prioridades evolucionen.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondaryLight
          )
          Spacer(modifier = Modifier.height(12.dp))
          OutlinedTextField(
            value = editedGoal,
            onValueChange = { editedGoal = it },
            placeholder = { Text("¿Cuál es tu propósito?") },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        TextButton(
          onClick = {
            if (editedGoal.isNotBlank()) {
              onUpdateGoal(editedGoal.trim())
            }
            showEditGoalDialog = false
          }
        ) {
          Text("Guardar", fontWeight = FontWeight.Bold, color = AccentCharcoal)
        }
      },
      dismissButton = {
        TextButton(onClick = { showEditGoalDialog = false }) {
          Text("Cancelar", color = TextSecondaryLight)
        }
      }
    )
  }
}

/**
 * Misiones de Equilibrio: Focused directly on the user's weakest dimensions!
 * Provides productive, wellbeing-enhancing daily activities without loading screens.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MissionsSection(
  report: AssessmentReport,
  missions: List<BalanceMission>,
  onToggleMission: (BalanceMission) -> Unit,
  onOpenReport: () -> Unit,
  onOpenCreateMission: () -> Unit
) {
  var filterWeaknessesOnly by remember { mutableStateOf(true) }

  val filteredMissions = if (filterWeaknessesOnly) {
    missions.filter { it.isWeaknessFocus }
  } else {
    missions
  }

  val weakDimensions = DimensionsProvider.dimensions.filter { (report.scores[it.index] ?: 5) <= 6 }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 24.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Top Title & Context
    item(key = "missions_header") {
      Column(modifier = Modifier.padding(top = 16.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Misiones de Equilibrio",
            style = MaterialTheme.typography.displayMedium.copy(
              fontSize = 26.sp,
              fontWeight = FontWeight.Bold
            ),
            color = TextPrimaryLight
          )

          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Surface(
              color = AccentCharcoal,
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.bouncyClick { onOpenCreateMission() }
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
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
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                  color = Color.White
                )
              }
            }

            Surface(
              color = AccentCharcoal.copy(alpha = 0.08f),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.bouncyClick { onOpenReport() }
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Outlined.Hexagon,
                  contentDescription = null,
                  tint = AccentCharcoal,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "Tu Hexágono",
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                  color = AccentCharcoal
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
          text = "Actividades productivas diseñadas para elevar tus dimensiones prioritarias y cultivar bienestar integral.",
          style = MaterialTheme.typography.bodyMedium,
          color = TextSecondaryLight
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Weakness Focus Alert Banner
        Surface(
          shape = RoundedCornerShape(20.dp),
          color = Color(0xFFEFF6FF),
          border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Outlined.Lightbulb,
                contentDescription = null,
                tint = Color(0xFF1D4ED8),
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "ENFOQUE EN TUS DEBILIDADES",
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 0.5.sp
                ),
                color = Color(0xFF1D4ED8)
              )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = if (weakDimensions.isNotEmpty()) {
                "Tus áreas que más necesitan recarga hoy son: ${weakDimensions.joinToString { it.title }}. Completa estas misiones para restablecer la armonía."
              } else {
                "Tu equilibrio general es sólido. Mantén el ritmo con estas prácticas diarias de bienestar."
              },
              style = MaterialTheme.typography.bodyMedium,
              color = Color(0xFF1E3A8A)
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Filter chips: "Tus Debilidades" vs "Todas"
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          FilterChip(
            selected = filterWeaknessesOnly,
            onClick = { filterWeaknessesOnly = true },
            label = { Text("Tus Debilidades (${missions.count { it.isWeaknessFocus }})") },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = AccentCharcoal,
              selectedLabelColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.bouncyClick { filterWeaknessesOnly = true }
          )

          FilterChip(
            selected = !filterWeaknessesOnly,
            onClick = { filterWeaknessesOnly = false },
            label = { Text("Todas (${missions.size})") },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = AccentCharcoal,
              selectedLabelColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.bouncyClick { filterWeaknessesOnly = false }
          )
        }
      }
    }

    // List of Mission Cards
    items(filteredMissions, key = { it.id }) { mission ->
      MissionCard(
        mission = mission,
        onToggle = { onToggleMission(mission) }
      )
    }

    item(key = "bottom_space") {
      Spacer(modifier = Modifier.height(120.dp))
    }
  }
}

@Composable
private fun MissionCard(
  mission: BalanceMission,
  onToggle: () -> Unit
) {
  val dimension = DimensionsProvider.dimensions.getOrNull(mission.dimensionIndex)
  val accentColor = dimension?.accentColor ?: AccentCharcoal

  Surface(
    shape = RoundedCornerShape(22.dp),
    color = SurfaceCardLight,
    border = androidx.compose.foundation.BorderStroke(
      1.dp,
      if (mission.isCompleted) Color(0xFF10B981).copy(alpha = 0.5f) else BorderLight
    ),
    shadowElevation = if (mission.isCompleted) 1.dp else 2.dp,
    modifier = Modifier
      .fillMaxWidth()
      .bouncyClick { onToggle() }
      .testTag("mission_card_${mission.id}")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(18.dp),
      verticalAlignment = Alignment.Top
    ) {
      // Checkbox / Circle
      Box(
        modifier = Modifier
          .padding(top = 2.dp)
          .size(28.dp)
          .clip(CircleShape)
          .background(if (mission.isCompleted) Color(0xFF10B981) else SurfaceVariantLight)
          .border(
            width = if (mission.isCompleted) 0.dp else 1.5.dp,
            color = if (mission.isCompleted) Color(0xFF10B981) else Color(0xFF9CA3AF),
            shape = CircleShape
          ),
        contentAlignment = Alignment.Center
      ) {
        if (mission.isCompleted) {
          Icon(
            imageVector = Icons.Default.Check,
            contentDescription = "Completada",
            tint = Color.White,
            modifier = Modifier.size(18.dp)
          )
        }
      }

      Spacer(modifier = Modifier.width(14.dp))

      Column(modifier = Modifier.weight(1f)) {
        // Tag row: Dimension name + Time + EXP reward
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = accentColor.copy(alpha = 0.12f)
          ) {
            Text(
              text = mission.dimensionTitle.uppercase(),
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = accentColor,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Timer,
              contentDescription = null,
              tint = TextTertiaryLight,
              modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
              text = mission.durationLabel,
              style = MaterialTheme.typography.labelSmall,
              color = TextSecondaryLight
            )
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = Color(0xFFFEF3C7) // Gold/Amber pill
            ) {
              Text(
                text = "+${mission.expReward} EXP",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF92400E),
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Mission Title
        Text(
          text = mission.title,
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp
          ),
          color = if (mission.isCompleted) TextSecondaryLight else TextPrimaryLight
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Mission Description
        Text(
          text = mission.description,
          style = MaterialTheme.typography.bodyMedium,
          color = TextSecondaryLight
        )

        if (mission.isHabit || mission.isCustom) {
          Spacer(modifier = Modifier.height(6.dp))
          Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            if (mission.isCustom) {
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = AccentCharcoal.copy(alpha = 0.08f)
              ) {
                Text(
                  text = "TU MISIÓN",
                  style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                  color = AccentCharcoal,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            }
            if (mission.isHabit) {
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFFEFF6FF)
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                  Icon(
                    imageVector = Icons.Outlined.EventRepeat,
                    contentDescription = null,
                    tint = Color(0xFF1D4ED8),
                    modifier = Modifier.size(12.dp)
                  )
                  Spacer(modifier = Modifier.width(3.dp))
                  Text(
                    text = "Hábito: ${mission.habitDays.joinToString(", ")}",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                    color = Color(0xFF1D4ED8)
                  )
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Wellbeing impact explanation
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceVariantLight)
            .padding(10.dp)
        ) {
          Text(
            text = "Bienestar: ${mission.benefit}",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = TextPrimaryLight
          )
        }
      }
    }
  }
}

@Composable
private fun NavBottomItem(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  label: String,
  isSelected: Boolean,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
    modifier = modifier
      .height(52.dp)
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick
      )
      .padding(horizontal = 2.dp, vertical = 2.dp)
  ) {
    Box(
      modifier = Modifier.size(24.dp),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = label,
        tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.50f),
        modifier = Modifier.size(22.dp)
      )
    }
    Spacer(modifier = Modifier.height(2.dp))
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall.copy(
        fontSize = 10.5.sp,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
      ),
      color = if (isSelected) Color.White else Color.White.copy(alpha = 0.50f),
      maxLines = 1,
      textAlign = androidx.compose.ui.text.style.TextAlign.Center
    )
    Spacer(modifier = Modifier.height(3.dp))
    // Symmetrical indicator dot slot for ALL tabs, avoiding vertical wobble
    Box(
      modifier = Modifier
        .size(3.5.dp)
        .clip(CircleShape)
        .background(if (isSelected) Color.White else Color.Transparent)
    )
  }
}

/**
 * Spiderweb Hexagonal Radar Canvas matching image.png on white background,
 * and dynamically filled with the user's 6 assessment scores!
 */
@Composable
private fun SpiderwebHexagonCanvas(
  scores: Map<Int, Int>,
  modifier: Modifier = Modifier
) {
  Canvas(modifier = modifier) {
    val center = Offset(size.width / 2f, size.height / 2f)
    val maxRadius = (size.minDimension / 2f) * 0.90f

    // 4 Concentric hexagon rings (spiderweb lines)
    val ringLevels = listOf(0.25f, 0.50f, 0.75f, 1.0f)
    ringLevels.forEach { frac ->
      val path = Path()
      for (i in 0..5) {
        val angle = -PI / 2 + i * (PI / 3)
        val x = (center.x + maxRadius * frac * cos(angle)).toFloat()
        val y = (center.y + maxRadius * frac * sin(angle)).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
      }
      path.close()
      drawPath(
        path = path,
        color = BorderLight,
        style = Stroke(width = 1.2.dp.toPx())
      )
    }

    // 6 Radial spoke lines from center to outer vertices
    for (i in 0..5) {
      val angle = -PI / 2 + i * (PI / 3)
      val end = Offset(
        (center.x + maxRadius * cos(angle)).toFloat(),
        (center.y + maxRadius * sin(angle)).toFloat()
      )
      drawLine(
        color = BorderLight,
        start = center,
        end = end,
        strokeWidth = 1.2.dp.toPx()
      )
    }

    // Filled radar polygon based on user's assessment
    val radarPath = Path()
    val radarPoints = (0..5).map { i ->
      val score = scores[i] ?: 6
      val r = maxRadius * (score / 10f).coerceIn(0.18f, 1f)
      val angle = -PI / 2 + i * (PI / 3)
      Offset(
        (center.x + r * cos(angle)).toFloat(),
        (center.y + r * sin(angle)).toFloat()
      )
    }

    radarPath.moveTo(radarPoints[0].x, radarPoints[0].y)
    for (i in 1 until radarPoints.size) {
      radarPath.lineTo(radarPoints[i].x, radarPoints[i].y)
    }
    radarPath.close()

    // Soft charcoal gradient fill
    drawPath(
      path = radarPath,
      brush = Brush.radialGradient(
        colors = listOf(
          AccentCharcoal.copy(alpha = 0.25f),
          AccentCharcoal.copy(alpha = 0.08f)
        ),
        center = center,
        radius = maxRadius
      ),
      style = Fill
    )

    // Crisp border
    drawPath(
      path = radarPath,
      color = AccentCharcoal,
      style = Stroke(
        width = 2.dp.toPx(),
        join = StrokeJoin.Round,
        cap = StrokeCap.Round
      )
    )

    // Glowing vertex dots
    radarPoints.forEachIndexed { i, pt ->
      val accent = DimensionsProvider.dimensions[i].accentColor
      drawCircle(
        color = Color.White,
        radius = 4.dp.toPx(),
        center = pt
      )
      drawCircle(
        color = accent,
        radius = 2.5.dp.toPx(),
        center = pt
      )
    }
  }
}

/**
 * 8-pointed golden star badge matching image.png level 2
 */
private fun DrawScope.drawEightPointStar(
  center: Offset,
  outerRadius: Float,
  innerRadius: Float,
  color: Color
) {
  val path = Path()
  val numPoints = 8
  val totalPoints = numPoints * 2

  for (i in 0 until totalPoints) {
    val r = if (i % 2 == 0) outerRadius else innerRadius
    val angle = -PI / 2 + i * (2 * PI / totalPoints)
    val x = (center.x + r * cos(angle)).toFloat()
    val y = (center.y + r * sin(angle)).toFloat()
    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
  }
  path.close()
  drawPath(path = path, color = color, style = Fill)
}

/**
 * Warm illustrated woman avatar placeholder matching image.png
 */
@Composable
private fun IllustratedAvatar() {
  Canvas(modifier = Modifier.fillMaxSize()) {
    val center = Offset(size.width / 2f, size.height / 2f)

    // Peach background circle
    drawCircle(
      color = Color(0xFFFFDFBA),
      radius = size.minDimension / 2f,
      center = center
    )

    // Dark hair silhouette
    val hairPath = Path()
    hairPath.moveTo(size.width * 0.22f, size.height * 0.95f)
    hairPath.cubicTo(
      size.width * 0.15f, size.height * 0.45f,
      size.width * 0.35f, size.height * 0.15f,
      size.width * 0.50f, size.height * 0.15f
    )
    hairPath.cubicTo(
      size.width * 0.65f, size.height * 0.15f,
      size.width * 0.85f, size.height * 0.45f,
      size.width * 0.78f, size.height * 0.95f
    )
    hairPath.close()
    drawPath(path = hairPath, color = Color(0xFF374151), style = Fill)

    // Face / Neck oval
    drawOval(
      color = Color(0xFFFBBF8C),
      topLeft = Offset(size.width * 0.34f, size.height * 0.28f),
      size = androidx.compose.ui.geometry.Size(size.width * 0.32f, size.height * 0.42f)
    )

    // Orange/coral top
    val shirtPath = Path()
    shirtPath.moveTo(size.width * 0.24f, size.height)
    shirtPath.cubicTo(
      size.width * 0.32f, size.height * 0.70f,
      size.width * 0.68f, size.height * 0.70f,
      size.width * 0.76f, size.height
    )
    shirtPath.close()
    drawPath(path = shirtPath, color = Color(0xFFFB923C), style = Fill)
  }
}
