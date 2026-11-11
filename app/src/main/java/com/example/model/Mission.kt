package com.example.model

data class BalanceMission(
  val id: String,
  val dimensionIndex: Int,
  val dimensionTitle: String,
  val title: String,
  val description: String,
  val benefit: String,
  val durationLabel: String,
  val expReward: Int,
  val isWeaknessFocus: Boolean,
  var isCompleted: Boolean = false,
  val isCustom: Boolean = false,
  val difficulty: String = "Media",
  val frequency: String = "Diariamente",
  val isHabit: Boolean = false,
  val habitRepeatWeeks: Int = 1,
  val habitDays: List<String> = emptyList(),
  val habitEndRule: String = "Nunca"
)

object MissionsProvider {
  fun generateMissionsForUser(scores: Map<Int, Int>): List<BalanceMission> {
    val dimensions = DimensionsProvider.dimensions
    val list = mutableListOf<BalanceMission>()

    // Sort dimensions by score ascending (weakest first!)
    val sortedDims = dimensions.sortedBy { scores[it.index] ?: 5 }

    // Weakest dimensions get high-priority wellbeing activities
    sortedDims.forEachIndexed { rank, dim ->
      val score = scores[dim.index] ?: 5
      val isWeak = score <= 6 || rank < 2

      when (dim.key) {
        DimensionKey.DESCANSO -> {
          list.add(
            BalanceMission(
              id = "desc_1",
              dimensionIndex = dim.index,
              dimensionTitle = dim.title,
              title = "Santuario Digital Nocturno",
              description = "Apaga todas las pantallas (teléfono, tablet, TV) 45 minutos antes de dormir y atenúa las luces de tu habitación.",
              benefit = "Permite la liberación natural de melatonina y reduce el cortisol para entrar en sueño profundo delta.",
              durationLabel = "45 min",
              expReward = 50,
              isWeaknessFocus = isWeak
            )
          )
          list.add(
            BalanceMission(
              id = "desc_2",
              dimensionIndex = dim.index,
              dimensionTitle = dim.title,
              title = "Cierre de Jornada y Vaciado Mental",
              description = "Anota en un papel los 3 pendientes clave de mañana para liberar tu memoria de trabajo antes de cenar.",
              benefit = "Frena la rumiación de pensamientos laborales durante la noche.",
              durationLabel = "10 min",
              expReward = 35,
              isWeaknessFocus = isWeak
            )
          )
        }
        DimensionKey.AUTOESTIMA -> {
          list.add(
            BalanceMission(
              id = "auto_1",
              dimensionIndex = dim.index,
              dimensionTitle = dim.title,
              title = "Registro de Autoeficacia Real",
              description = "Escribe 2 situaciones difíciles o decisiones que tomaste recientemente y resolviste con tus propios recursos.",
              benefit = "Reconecta tus circuitos neuronales con evidencia empírica de tu valía personal.",
              durationLabel = "10 min",
              expReward = 45,
              isWeaknessFocus = isWeak
            )
          )
          list.add(
            BalanceMission(
              id = "auto_2",
              dimensionIndex = dim.index,
              dimensionTitle = dim.title,
              title = "Límite Sereno sin Disculpas",
              description = "Di 'no' a una petición no prioritaria de forma educada y firme, sin añadir pretextos ni excusas elaboradas.",
              benefit = "Fortalece la soberanía personal y el respeto por tu propia energía.",
              durationLabel = "Hoy",
              expReward = 40,
              isWeaknessFocus = isWeak
            )
          )
        }
        DimensionKey.EJERCICIO -> {
          list.add(
            BalanceMission(
              id = "ejer_1",
              dimensionIndex = dim.index,
              dimensionTitle = dim.title,
              title = "Caminata Solar Matutina",
              description = "Camina 20 minutos a paso vivo con exposición a luz natural durante las primeras horas de la mañana.",
              benefit = "Sincroniza el ritmo circadiano y estimula la secreción de serotonina y dopamina de base.",
              durationLabel = "20 min",
              expReward = 45,
              isWeaknessFocus = isWeak
            )
          )
          list.add(
            BalanceMission(
              id = "ejer_2",
              dimensionIndex = dim.index,
              dimensionTitle = dim.title,
              title = "Descompresión Somática Activa",
              description = "Realiza 3 series de estiramientos de cadera, columna y cuello para soltar tensiones posturales.",
              benefit = "Alivia la fatiga física acumulada y mejora el flujo sanguíneo cerebral.",
              durationLabel = "12 min",
              expReward = 30,
              isWeaknessFocus = isWeak
            )
          )
        }
        DimensionKey.RELACIONES -> {
          list.add(
            BalanceMission(
              id = "rela_1",
              dimensionIndex = dim.index,
              dimensionTitle = dim.title,
              title = "Llamada de Presencia Total",
              description = "Llama a un vínculo significativo por 15 minutos con atención plena, sin mirar el móvil ni hacer multitarea.",
              benefit = "Activa el nervio vago y la oxitocina, neutralizando la sensación de aislamiento funcional.",
              durationLabel = "15 min",
              expReward = 40,
              isWeaknessFocus = isWeak
            )
          )
          list.add(
            BalanceMission(
              id = "rela_2",
              dimensionIndex = dim.index,
              dimensionTitle = dim.title,
              title = "Agradecimiento Genuino Espontáneo",
              description = "Envía un mensaje honesto a alguien reconociendo una cualidad o gesto que haya aportado a tu vida.",
              benefit = "Cultiva reciprocidad y enriquece tu red de contención afectiva.",
              durationLabel = "5 min",
              expReward = 35,
              isWeaknessFocus = isWeak
            )
          )
        }
        DimensionKey.HOBBIES -> {
          list.add(
            BalanceMission(
              id = "hobb_1",
              dimensionIndex = dim.index,
              dimensionTitle = dim.title,
              title = "Espacio de Juego Desinteresado",
              description = "Dedica 30 minutos a dibujar, tocar música, jardinería o leer ficción sin buscar resultados ni monetización.",
              benefit = "Devuelve a tu cerebro la plasticidad creativa del juego puro sin presiones utilitarias.",
              durationLabel = "30 min",
              expReward = 45,
              isWeaknessFocus = isWeak
            )
          )
        }
        DimensionKey.EXCELENCIA -> {
          list.add(
            BalanceMission(
              id = "exce_1",
              dimensionIndex = dim.index,
              dimensionTitle = dim.title,
              title = "La Regla del Detalle Invisible",
              description = "Toma una sola tarea de hoy y pule un detalle fino que nadie pidió pero que honre tu propio orgullo profesional.",
              benefit = "Eleva tu sentido de maestría interna y respeto por tu propia obra.",
              durationLabel = "15 min",
              expReward = 40,
              isWeaknessFocus = isWeak
            )
          )
        }
      }
    }

    return list
  }
}
