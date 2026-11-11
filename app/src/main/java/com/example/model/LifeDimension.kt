package com.example.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Celebration
import androidx.compose.material.icons.outlined.DirectionsRun
import androidx.compose.material.icons.outlined.EmojiPeople
import androidx.compose.material.icons.outlined.NightlightRound
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.theme.ColorAutoestima
import com.example.ui.theme.ColorDescanso
import com.example.ui.theme.ColorEjercicio
import com.example.ui.theme.ColorExcelencia
import com.example.ui.theme.ColorHobbies
import com.example.ui.theme.ColorRelaciones

enum class DimensionKey {
  AUTOESTIMA,
  EJERCICIO,
  DESCANSO,
  RELACIONES,
  HOBBIES,
  EXCELENCIA
}

data class LifeDimension(
  val index: Int,
  val key: DimensionKey,
  val title: String,
  val question: String,
  val description: String,
  val minLabel: String,
  val maxLabel: String,
  val icon: ImageVector,
  val accentColor: Color
)

object DimensionsProvider {
  val dimensions: List<LifeDimension> = listOf(
    LifeDimension(
      index = 0,
      key = DimensionKey.AUTOESTIMA,
      title = "Autoestima",
      question = "¿Cuánto confías en ti misma y en tus capacidades?",
      description = "Confianza intrínseca, autoeficacia y seguridad para enfrentar desafíos sin depender exclusivamente de la aprobación externa.",
      minLabel = "Dudas frecuentes e inseguridad",
      maxLabel = "Confianza plena y autoaceptación",
      icon = Icons.Outlined.EmojiPeople,
      accentColor = ColorAutoestima
    ),
    LifeDimension(
      index = 1,
      key = DimensionKey.EJERCICIO,
      title = "Ejercicio",
      question = "¿Tienes una vida físicamente activa?",
      description = "Movimiento corporal regular, vitalidad, resistencia y el compromiso de honrar la salud somática diaria.",
      minLabel = "Sedentarismo predominante",
      maxLabel = "Actividad física constante y vital",
      icon = Icons.Outlined.DirectionsRun,
      accentColor = ColorEjercicio
    ),
    LifeDimension(
      index = 2,
      key = DimensionKey.DESCANSO,
      title = "Descanso",
      question = "¿Tus hábitos te permiten descansar y recuperar energía?",
      description = "Higiene de sueño reparador, pausas cognitivas intencionales y capacidad de desconexión sin culpa.",
      minLabel = "Agotamiento crónico o mal sueño",
      maxLabel = "Recuperación profunda y energía matutina",
      icon = Icons.Outlined.NightlightRound,
      accentColor = ColorDescanso
    ),
    LifeDimension(
      index = 3,
      key = DimensionKey.RELACIONES,
      title = "Relaciones",
      question = "¿Qué tanto tiempo le dedicas a tus relaciones interpersonales?",
      description = "Conexión afectiva genuina, tiempo de calidad con vínculos significativos y presencia empática.",
      minLabel = "Aislamiento o poco tiempo compartido",
      maxLabel = "Vínculos profundos, nutritivos y activos",
      icon = Icons.Outlined.Celebration,
      accentColor = ColorRelaciones
    ),
    LifeDimension(
      index = 4,
      key = DimensionKey.HOBBIES,
      title = "Hobbies",
      question = "¿Cuánto espacio dedicas a lo que te apasiona?",
      description = "Expresión creativa, proyectos no utilitarios, juego y actividades que te devuelven el entusiasmo puro.",
      minLabel = "Sin tiempo para pasiones personales",
      maxLabel = "Espacio sagrado y recurrente de disfrute",
      icon = Icons.Outlined.Palette,
      accentColor = ColorHobbies
    ),
    LifeDimension(
      index = 5,
      key = DimensionKey.EXCELENCIA,
      title = "Excelencia",
      question = "¿Das lo mejor de ti en cada proyecto?",
      description = "Rigor, compromiso consciente, pasión por los detalles y la aspiración constante de maestría personal.",
      minLabel = "Conformismo o desinterés",
      maxLabel = "Dedicación total, orgullo y maestría",
      icon = Icons.Outlined.WorkspacePremium,
      accentColor = ColorExcelencia
    )
  )
}
