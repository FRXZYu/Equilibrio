package com.example.model

data class BalanceMission(
  val id: String,
  val dimensionIndex: Int,
  val dimensionTitle: String,
  val title: String,
  val summary: String = "",
  val description: String,
  val steps: List<String> = emptyList(),
  val benefit: String,
  val durationLabel: String,
  val baseExpReward: Int = 30,
  val isWeaknessFocus: Boolean = false,
  val hasMultiplier: Boolean = false,
  val expReward: Int = if (hasMultiplier) (baseExpReward * 1.5).toInt() else baseExpReward,
  var isCompleted: Boolean = false,
  var isSavedForLater: Boolean = false,
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

    // Ordenar dimensiones de menor a mayor puntuación (debilidades primero)
    val sortedDims = dimensions.sortedBy { scores[it.index] ?: 5 }

    sortedDims.forEachIndexed { rank, dim ->
      val score = scores[dim.index] ?: 5
      // Se considera debilidad prioritaria si la nota es <= 6 o es de las 2 más bajas
      val isWeak = score <= 6 || rank < 2
      val hasBonus = isWeak // Multiplicador 1.5x

      when (dim.key) {
        DimensionKey.DESCANSO -> {
          list.add(
            BalanceMission(
              id = "desc_1",
              dimensionIndex = dim.index,
              dimensionTitle = dim.title,
              title = "Santuario Digital Nocturno",
              summary = "Apaga pantallas y prepara tu mente para un sueño profundo y reparador.",
              description = "Desconecta todos tus dispositivos emisores de luz azul 45 minutos antes de acostarte para sincronizar tus ritmos biológicos.",
              steps = listOf(
                "Paso 1 (Preparación): Coloca tu teléfono fuera del dormitorio o en modo avión y atenúa las luces principales.",
                "Paso 2 (Ejecución): Dedica 30 minutos a una lectura analógica, respiración diafragmática o estiramientos suaves en penumbra.",
                "Paso 3 (Reflexión): Observa cómo la velocidad de tus pensamientos desciende al eliminar los estímulos algorítmicos."
              ),
              benefit = "Puede ayudarte a desconectar la mente y facilitar un descanso nocturno más continuo y reparador.",
              durationLabel = "45 min",
              baseExpReward = 40,
              isWeaknessFocus = isWeak,
              hasMultiplier = hasBonus
            )
          )
          list.add(
            BalanceMission(
              id = "desc_2",
              dimensionIndex = dim.index,
              dimensionTitle = dim.title,
              title = "Cierre de Jornada y Vaciado Mental",
              summary = "Escribe tus pendientes de mañana para apagar la rumiación nocturna.",
              description = "Anota en una libreta física las 3 tareas prioritarias de mañana antes de cenar para liberar tu memoria operativa.",
              steps = listOf(
                "Paso 1 (Preparación): Abre una libreta en tu escritorio al terminar tus actividades principales.",
                "Paso 2 (Ejecución): Escribe tus 3 objetivos indispensables de mañana y ciérrala físicamente con intención de pausa.",
                "Paso 3 (Reflexión): Respira hondo reconociendo que el día de hoy ya concluyó y mereces descansar plenamente."
              ),
              benefit = "Mucha gente nota que al plasmar sus pendientes en papel disminuye la inquietud y se duerme con mayor tranquilidad.",
              durationLabel = "10 min",
              baseExpReward = 30,
              isWeaknessFocus = isWeak,
              hasMultiplier = hasBonus
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
              summary = "Registra 2 victorias recientes donde confiaste en tu propio criterio.",
              description = "Identifica dos momentos recientes donde resolviste un problema o tomaste una decisión con tus propios recursos internos.",
              steps = listOf(
                "Paso 1 (Preparación): Busca un espacio tranquilo con un lápiz y papel o tu bloc de notas.",
                "Paso 2 (Ejecución): Detalla dos situaciones complejas donde tus capacidades y serenidad marcaron la diferencia.",
                "Paso 3 (Reflexión): Lee tus notas en voz alta reconociendo tu competencia intrínseca sin falsas modestias."
              ),
              benefit = "Puede ayudarte a recordar tus fortalezas reales en momentos de duda y reconocer lo que ya eres capaz de lograr.",
              durationLabel = "10 min",
              baseExpReward = 40,
              isWeaknessFocus = isWeak,
              hasMultiplier = hasBonus
            )
          )
          list.add(
            BalanceMission(
              id = "auto_2",
              dimensionIndex = dim.index,
              dimensionTitle = dim.title,
              title = "Límite Sereno sin Disculpas",
              summary = "Comunica un límite firme y amable ante una petición no prioritaria.",
              description = "Practica decir 'no' a una demanda accesoria de tu tiempo, con educación impecable pero sin inventar pretextos.",
              steps = listOf(
                "Paso 1 (Preparación): Identifica una petición o compromiso que no resuene con tus prioridades reales.",
                "Paso 2 (Ejecución): Responde con gratitud y firmeza: 'Gracias por pensar en mí, pero en este momento no puedo comprometerme'.",
                "Paso 3 (Reflexión): Tolera la incomodidad inicial recordando que cuidar tu energía es un acto de honestidad mutua."
              ),
              benefit = "Mucha gente nota que proteger su tiempo con amabilidad refuerza la confianza en sus propias prioridades.",
              durationLabel = "Hoy",
              baseExpReward = 35,
              isWeaknessFocus = isWeak,
              hasMultiplier = hasBonus
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
              summary = "Camina 20 min al sol para reactivar circulación y dopamina.",
              description = "Sal al aire libre a paso ágil durante la mañana para absorber fotones naturales y activar tu musculatura.",
              steps = listOf(
                "Paso 1 (Preparación): Ponte calzado cómodo e hidrátate con un vaso de agua fresca antes de salir.",
                "Paso 2 (Ejecución): Camina 20 minutos con la mirada al frente, sintiendo el impulso del suelo y la luz natural en tu rostro.",
                "Paso 3 (Reflexión): Nota el aumento de temperatura corporal, lucidez mental y disposición energética para el día."
              ),
              benefit = "Puede ayudarte a iniciar la mañana con mayor vitalidad, despejar la mente y activar el cuerpo de forma gradual.",
              durationLabel = "20 min",
              baseExpReward = 40,
              isWeaknessFocus = isWeak,
              hasMultiplier = hasBonus
            )
          )
          list.add(
            BalanceMission(
              id = "ejer_2",
              dimensionIndex = dim.index,
              dimensionTitle = dim.title,
              title = "Descompresión Somática Activa",
              summary = "Realiza estiramientos para soltar la tensión de espalda y cuello.",
              description = "Dedica una sesión breve a estirar cadenas musculares posteriores acumuladas por el trabajo sedentario.",
              steps = listOf(
                "Paso 1 (Preparación): Despéjate de la silla y busca un espacio con una esterilla o alfombra.",
                "Paso 2 (Ejecución): Realiza 3 series de gato-vaca, apertura de pecho y estiramiento profundo de isquiotibiales.",
                "Paso 3 (Reflexión): Exhala prolongadamente en cada postura liberando la rigidez contenida en articulaciones."
              ),
              benefit = "Mucha gente nota que una pausa breve de estiramiento alivia la pesadez en hombros y espalda tras horas de trabajo.",
              durationLabel = "12 min",
              baseExpReward = 30,
              isWeaknessFocus = isWeak,
              hasMultiplier = hasBonus
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
              summary = "Conecta 15 min con alguien valioso sin ninguna distracción.",
              description = "Llama a un amigo, familiar o colega importante con el único fin de escuchar con calidez e interés genuino.",
              steps = listOf(
                "Paso 1 (Preparación): Elige a una persona querida y busca un entorno silencioso sin ordenadores encendidos.",
                "Paso 2 (Ejecución): Pregúntale con curiosidad sincera por su vida y escucha sin interrumpir con tus propias historias.",
                "Paso 3 (Reflexión): Agradece internamente el valor de los vínculos reales que sostienen tu bienestar emocional."
              ),
              benefit = "Puede ayudarte a sentirte más cerca de quienes quieres y recordar el valor de una conversación con atención plena.",
              durationLabel = "15 min",
              baseExpReward = 35,
              isWeaknessFocus = isWeak,
              hasMultiplier = hasBonus
            )
          )
          list.add(
            BalanceMission(
              id = "rela_2",
              dimensionIndex = dim.index,
              dimensionTitle = dim.title,
              title = "Agradecimiento Genuino Espontáneo",
              summary = "Envía un mensaje de gratitud sincera a quien haya sumado a tu camino.",
              description = "Haz saber a alguien por qué valoras su presencia en tu vida mediante unas palabras honestas y concretas.",
              steps = listOf(
                "Paso 1 (Preparación): Piensa en alguien que haya tenido un gesto generoso o cuya compañía valores especialmente.",
                "Paso 2 (Ejecución): Envíale un mensaje destacando un recuerdo o cualidad específica que admiras de su persona.",
                "Paso 3 (Reflexión): Nota el florecimiento de calidez que genera expresar aprecio sincero sin esperar nada a cambio."
              ),
              benefit = "Mucha gente nota que expresar gratitud espontánea alegra el día de la otra persona y fortalece el vínculo mutuo.",
              durationLabel = "5 min",
              baseExpReward = 30,
              isWeaknessFocus = isWeak,
              hasMultiplier = hasBonus
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
              summary = "30 min dedicados a tu pasión creativa sin buscar productividad.",
              description = "Sumérgete en dibujar, escribir, tocar un instrumento, cocinar o jardinería sin pretensión de rendimiento.",
              steps = listOf(
                "Paso 1 (Preparación): Silencia notificaciones y reúne tus materiales favoritos para tu actividad artística o lúdica.",
                "Paso 2 (Ejecución): Disfruta del proceso sensorial puro: los colores, sonidos o texturas, sin juzgar el resultado final.",
                "Paso 3 (Reflexión): Reconoce cómo tu mente recupera ligereza cuando no todo en tu vida está orientado al lucro o la utilidad."
              ),
              benefit = "Puede ayudarte a desconectar de las exigencias cotidianas y disfrutar del placer de crear sin presiones.",
              durationLabel = "30 min",
              baseExpReward = 40,
              isWeaknessFocus = isWeak,
              hasMultiplier = hasBonus
            )
          )
          list.add(
            BalanceMission(
              id = "hobb_2",
              dimensionIndex = dim.index,
              dimensionTitle = dim.title,
              title = "Exploración de Curiosidad Libre",
              summary = "Investiga un tema fascinante sólo por el placer de aprender algo nuevo.",
              description = "Lee un capítulo de divulgación, astronomía, filosofía o arte que despierte tu asombro intelectual.",
              steps = listOf(
                "Paso 1 (Preparación): Elige un tema que siempre te haya intrigado pero que nunca hayas tenido tiempo de explorar.",
                "Paso 2 (Ejecución): Lee o escucha durante 20 minutos con libre fascinación, permitiéndote sorprenderte.",
                "Paso 3 (Reflexión): Siente el deleite intrínseco de expandir tus horizontes sin plazos ni evaluaciones."
              ),
              benefit = "Mucha gente nota que aprender algo nuevo por pura curiosidad renueva el entusiasmo y despeja la rutina diaria.",
              durationLabel = "20 min",
              baseExpReward = 30,
              isWeaknessFocus = isWeak,
              hasMultiplier = hasBonus
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
              summary = "Pule un detalle con maestría sólo por tu propio orgullo profesional.",
              description = "Elige una tarea cotidiana y dedícale 15 minutos adicionales para elevarla al estándar que tú respetas.",
              steps = listOf(
                "Paso 1 (Preparación): Selecciona un trabajo, documento o espacio que estés completando el día de hoy.",
                "Paso 2 (Ejecución): Aplica un estándar de pulido superior en claridad, orden o presentación que demuestre tu maestría.",
                "Paso 3 (Reflexión): Contempla tu obra sintiendo la satisfacción interna de haber entregado lo mejor de ti."
              ),
              benefit = "Puede ayudarte a experimentar orgullo sincero por tu trabajo al saber que diste un toque de esmero personal.",
              durationLabel = "15 min",
              baseExpReward = 35,
              isWeaknessFocus = isWeak,
              hasMultiplier = hasBonus
            )
          )
          list.add(
            BalanceMission(
              id = "exce_2",
              dimensionIndex = dim.index,
              dimensionTitle = dim.title,
              title = "Monotarea Impecable sin Interrupciones",
              summary = "25 minutos de enfoque absoluto en un solo reto de alto impacto.",
              description = "Trabaja en tu proyecto principal en un bloque Pomodoro puro, sin pestañas secundarias ni multitarea.",
              steps = listOf(
                "Paso 1 (Preparación): Cierra todas las pestañas de mensajería y define el entregable exacto de los próximos 25 minutos.",
                "Paso 2 (Ejecución): Enfoca tu atención exclusiva en esa meta, canalizando toda tu destreza cognitiva.",
                "Paso 3 (Reflexión): Nota la potencia del trabajo profundo (*deep work*) frente a la dispersión superficial habitual."
              ),
              benefit = "Mucha gente nota que avanzar sin distracciones durante 25 minutos genera una gran sensación de claridad y logro.",
              durationLabel = "25 min",
              baseExpReward = 40,
              isWeaknessFocus = isWeak,
              hasMultiplier = hasBonus
            )
          )
        }
      }
    }

    return list
  }
}
