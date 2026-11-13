package com.example.model

import com.example.domain.HelpfulnessRating
import com.example.domain.MissionCandidate

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
  val durationMinutes: Int = 10,
  val isPriorityFocus: Boolean = false,
  val isCompleted: Boolean = false,
  val isSavedForLater: Boolean = false,
  val isCustom: Boolean = false,
  val frequency: String = "Diariamente",
  val isHabit: Boolean = false,
  val habitRepeatWeeks: Int = 1,
  val habitDays: List<String> = emptyList(),
  val habitEndRule: String = "Nunca",
  val helpfulRating: HelpfulnessRating? = null,
  val reflectionNote: String = ""
) {
  fun toCandidate(): MissionCandidate = MissionCandidate(
    id = id,
    dimensionIndex = dimensionIndex,
    durationMinutes = durationMinutes,
    isCustom = isCustom,
    isHabit = isHabit
  )
}

/**
 * Catálogo amplio de acciones concretas, pequeñas y realistas (36 acciones, 6 por dimensión).
 * Pensadas para ser posibles en un día normal sin convertirse en una carga.
 */
object MissionsProvider {

  fun generateMissionsForUser(
    scores: Map<Int, Int>,
    chosenFocusDimensions: Set<Int> = emptySet()
  ): List<BalanceMission> {
    val dimensions = DimensionsProvider.dimensions
    val sortedDims = dimensions.sortedWith(
      compareByDescending<LifeDimension> { it.index in chosenFocusDimensions }
        .thenBy { scores[it.index] ?: 5 }
    )

    val list = mutableListOf<BalanceMission>()
    val fallbackFocusIndices = dimensions.sortedBy { scores[it.index] ?: 5 }
      .take(2)
      .map { it.index }
      .toSet()
    val effectivePriority = if (chosenFocusDimensions.isNotEmpty()) chosenFocusDimensions else fallbackFocusIndices

    sortedDims.forEach { dim ->
      val isPriority = dim.index in effectivePriority
      list.addAll(buildMissionsForDimension(dim, isPriority))
    }

    return list
  }

  private fun buildMissionsForDimension(
    dim: LifeDimension,
    isPriority: Boolean
  ): List<BalanceMission> {
    return when (dim.key) {
      DimensionKey.DESCANSO -> listOf(
        mission(
          id = "desc_1", dim = dim, isPriority = isPriority, durationLabel = "10 min", durationMinutes = 10,
          title = "Apagar pantallas 15 min antes de dormir",
          summary = "Deja el teléfono cargando fuera de tu alcance al ir a la cama.",
          description = "Reducir estímulos luminosos y notificaciones justo antes de acostarte ayuda a cerrar el día con menos ruido mental.",
          steps = listOf(
            "Elige un punto fuera de la cama para dejar el teléfono.",
            "Pon la alarma de mañana antes de soltarlo.",
            "Respira lento 5 veces ya acostado sin mirar ninguna pantalla."
          ),
          benefit = "Puede facilitar que la mente baje revoluciones antes de dormir."
        ),
        mission(
          id = "desc_2", dim = dim, isPriority = isPriority, durationLabel = "5 min", durationMinutes = 5,
          title = "Vaciar pendientes en papel",
          summary = "Anota 3 cosas para mañana y cierra la libreta.",
          description = "Pasar tus pendientes de la cabeza al papel evita seguir repasándolos por la noche.",
          steps = listOf(
            "Toma una hoja o nota rápida al terminar tu tarde.",
            "Escribe como máximo 3 cosas importantes para mañana.",
            "Cierra el cuaderno como señal de que hoy ya hiciste suficiente."
          ),
          benefit = "Suele aliviar la sensación de tener todo abierto en la mente."
        ),
        mission(
          id = "desc_3", dim = dim, isPriority = isPriority, durationLabel = "3 min", durationMinutes = 3,
          title = "Pausa sin pantallas a mitad del día",
          summary = "Mira por la ventana o cierra los ojos 3 minutos.",
          description = "Una micropausa breve corta el agotamiento visual y te devuelve al ritmo real.",
          steps = listOf(
            "Apártate de la pantalla por 3 minutos.",
            "Suelta hombros y mandíbula mientras miras lejos.",
            "Toma un sorbo de agua antes de retomar."
          ),
          benefit = "Ayuda a evitar que el cansancio se acumule sin notarlo."
        ),
        mission(
          id = "desc_4", dim = dim, isPriority = isPriority, durationLabel = "5 min", durationMinutes = 5,
          title = "Bajar la luz al caer la tarde",
          summary = "Usa una lámpara cálida o luz suave por la noche.",
          description = "Una iluminación tenue acompaña la transición natural hacia el descanso.",
          steps = listOf(
            "Apaga la luz blanca principal una hora antes de dormir.",
            "Deja encendida una lámpara suave.",
            "Haz una actividad tranquila como leer o escuchar algo sereno."
          ),
          benefit = "Acompaña la calma vespertina de manera sencilla."
        ),
        mission(
          id = "desc_5", dim = dim, isPriority = isPriority, durationLabel = "5 min", durationMinutes = 5,
          title = "Respiración lenta antes de dormir",
          summary = "Haz 6 respiraciones profundas exhalando más largo.",
          description = "Alargar la exhalación envía una señal física de calma sin exigir esfuerzo.",
          steps = listOf(
            "Acuéstate cómodo boca arriba o de lado.",
            "Inhala suave contando hasta 4 y exhala lento contando hasta 6.",
            "Repite 6 veces sin forzar."
          ),
          benefit = "Puede ayudarte a soltar tensión física acumulada durante el día."
        ),
        mission(
          id = "desc_6", dim = dim, isPriority = isPriority, durationLabel = "10 min", durationMinutes = 10,
          title = "Desayunar o merendar sin teléfono",
          summary = "Come un momento sin revisar mensajes ni noticias.",
          description = "Comer sin prisa informativa convierte una comida común en un pequeño descanso.",
          steps = listOf(
            "Deja el teléfono boca abajo o en otra habitación.",
            "Nota el sabor, la temperatura y la calma del momento.",
            "Tómate al menos 10 minutos tranquilos."
          ),
          benefit = "Convierte una rutina cotidiana en una pausa real."
        )
      )

      DimensionKey.AUTOESTIMA -> listOf(
        mission(
          id = "auto_1", dim = dim, isPriority = isPriority, durationLabel = "5 min", durationMinutes = 5,
          title = "Anotar un logro pequeño de hoy",
          summary = "Reconoce algo concreto que resolviste o cuidaste hoy.",
          description = "Registrar hechos reales equilibra la tendencia a fijarnos solo en lo pendiente.",
          steps = listOf(
            "Piensa en una situación de hoy o ayer en la que actuaste bien.",
            "Escríbela en una frase simple y concreta.",
            "Léela sin minimizarla."
          ),
          benefit = "Ayuda a construir confianza desde hechos reales, no desde exigencias."
        ),
        mission(
          id = "auto_2", dim = dim, isPriority = isPriority, durationLabel = "5 min", durationMinutes = 5,
          title = "Decir que no a algo secundario",
          summary = "Posterga o declina con amabilidad una tarea no urgente.",
          description = "Cuidar tu energía eligiendo qué sí cabe hoy protege tu paz mental.",
          steps = listOf(
            "Detecta una petición o tarea que hoy te sobrepasa.",
            "Responde claro y amable: 'Hoy no llego a esto, lo vemos mañana'.",
            "Observa el alivio de respetar tu propio límite."
          ),
          benefit = "Refuerza el respeto por tu tiempo sin necesidad de dar largas explicaciones."
        ),
        mission(
          id = "auto_3", dim = dim, isPriority = isPriority, durationLabel = "3 min", durationMinutes = 3,
          title = "Hablarte como a alguien cercano",
          summary = "Reformula un pensamiento exigente con un tono más justo.",
          description = "Cuando aparece una crítica dura hacia ti, cambiar el tono reduce la carga innecesaria.",
          steps = listOf(
            "Identifica una frase dura que te hayas dicho hoy.",
            "Pregúntate qué le dirías a un buen amigo en esa misma situación.",
            "Repite esa frase más justa para ti."
          ),
          benefit = "Puede suavizar la presión interna en días difíciles."
        ),
        mission(
          id = "auto_4", dim = dim, isPriority = isPriority, durationLabel = "5 min", durationMinutes = 5,
          title = "Aceptar un cumplido sin restarle valor",
          summary = "Cuando alguien te reconozca algo, responde solo 'gracias'.",
          description = "Evitar justificarte o minimizar lo que haces bien entrena una autoestima más tranquila.",
          steps = listOf(
            "Nota si recibes un comentario positivo o agradecimiento hoy.",
            "Responde con un simple 'Gracias, me alegra que te sirviera'.",
            "Permítete recibirlo tal cual."
          ),
          benefit = "Te ayuda a reconocer tu aporte cotidiano con naturalidad."
        ),
        mission(
          id = "auto_5", dim = dim, isPriority = isPriority, durationLabel = "10 min", durationMinutes = 10,
          title = "Elegir una decisión propia hoy",
          summary = "Haz algo pequeño a tu manera sin pedir validación previa.",
          description = "Tomar microdecisiones confiando en tu criterio fortalece la seguridad cotidiana.",
          steps = listOf(
            "Elige algo sencillo (orden de tareas, pausa, comida o paseo).",
            "Decídelo según cómo te sientes tú.",
            "Confía en tu elección sin darle más vueltas."
          ),
          benefit = "Devuelve protagonismo tranquilo a tu propio criterio."
        ),
        mission(
          id = "auto_6", dim = dim, isPriority = isPriority, durationLabel = "5 min", durationMinutes = 5,
          title = "Pausa de redes si te estás comparando",
          summary = "Cierra redes sociales 15 minutos si notas desgaste.",
          description = "Cortar la comparación automática a tiempo protege tu estado de ánimo.",
          steps = listOf(
            "Observa si al mirar pantallas empiezas a sentirte insuficiente.",
            "Cierra la app en ese instante.",
            "Vuelve a algo tangible a tu alrededor."
          ),
          benefit = "Cuida tu claridad emocional en momentos de cansancio."
        )
      )

      DimensionKey.EJERCICIO -> listOf(
        mission(
          id = "ejer_1", dim = dim, isPriority = isPriority, durationLabel = "10 min", durationMinutes = 10,
          title = "Caminar 10 minutos al aire libre",
          summary = "Da una vuelta breve sin prisa ni objetivo de marca.",
          description = "Salir a caminar unos minutos mueve el cuerpo y despeja la cabeza.",
          steps = listOf(
            "Ponte calzado cómodo y sal a la calle o patio.",
            "Camina a un paso natural durante 10 minutos.",
            "Nota el aire y el ritmo de tu respiración."
          ),
          benefit = "Ayuda a activar el cuerpo de manera amable y realista."
        ),
        mission(
          id = "ejer_2", dim = dim, isPriority = isPriority, durationLabel = "5 min", durationMinutes = 5,
          title = "Estirar cuello, hombros y espalda",
          summary = "Haz 4 estiramientos suaves para soltar tensión postural.",
          description = "Unos minutos de movilidad alivian la rigidez de estar sentado o tenso.",
          steps = listOf(
            "Ponte de pie y eleva los brazos inhalando profundo.",
            "Gira suave cuello y hombros hacia atrás 5 veces.",
            "Inclínate despacio hacia adelante dejando colgar la espalda."
          ),
          benefit = "Suele aliviar la pesadez física tras horas de estudio o trabajo."
        ),
        mission(
          id = "ejer_3", dim = dim, isPriority = isPriority, durationLabel = "5 min", durationMinutes = 5,
          title = "Subir escaleras o caminar tras comer",
          summary = "Muévete suave durante 5 minutos después de una comida.",
          description = "Aprovechar un momento cotidiano evita depender de grandes rutinas.",
          steps = listOf(
            "Al terminar de comer, levántate de inmediato.",
            "Camina por casa, la oficina o sube un tramo de escaleras.",
            "Vuelve con sensación de ligereza."
          ),
          benefit = "Suma movimiento real sin exigir bloques largos de agenda."
        ),
        mission(
          id = "ejer_4", dim = dim, isPriority = isPriority, durationLabel = "10 min", durationMinutes = 10,
          title = "Poner una canción y moverte",
          summary = "Mueve el cuerpo con música que te guste durante 2 canciones.",
          description = "Moverse con música rompe la inercia y mejora el ánimo sin rigidez.",
          steps = listOf(
            "Elige dos canciones con buen ritmo.",
            "Baila, estírate o muévete libremente por tu espacio.",
            "Bebe un vaso de agua al terminar."
          ),
          benefit = "Une energía física y disfrute en pocos minutos."
        ),
        mission(
          id = "ejer_5", dim = dim, isPriority = isPriority, durationLabel = "15 min", durationMinutes = 15,
          title = "Caminata breve con luz de día",
          summary = "Aprovecha la luz natural para moverte 15 minutos.",
          description = "Combinar luz solar y paso ligero revitaliza mejor que otro café.",
          steps = listOf(
            "Busca un momento de la mañana o mediodía.",
            "Camina 15 minutos sin mirar mensajes.",
            "Observa cómo cambia tu nivel de energía."
          ),
          benefit = "Favorece vitalidad diurna y descanso nocturno."
        ),
        mission(
          id = "ejer_6", dim = dim, isPriority = isPriority, durationLabel = "5 min", durationMinutes = 5,
          title = "Activar caderas y piernas",
          summary = "Haz 10 sentadillas suaves o elevaciones de talones.",
          description = "Pequeñas activaciones musculares despiertan el cuerpo en días sedentarios.",
          steps = listOf(
            "Levántate de la silla y apoya bien ambos pies.",
            "Haz entre 8 y 12 repeticiones suaves sin forzar.",
            "Estira pantorrillas 30 segundos."
          ),
          benefit = "Mantiene el cuerpo activo incluso en jornadas ocupadas."
        )
      )

      DimensionKey.RELACIONES -> listOf(
        mission(
          id = "rela_1", dim = dim, isPriority = isPriority, durationLabel = "5 min", durationMinutes = 5,
          title = "Enviar un mensaje sincero a alguien",
          summary = "Escribe a una persona querida solo para saber cómo está.",
          description = "Un gesto corto mantiene vivos los lazos importantes sin esperar ocasiones especiales.",
          steps = listOf(
            "Piensa en alguien con quien hace días no hablas.",
            "Envíale un texto o audio breve y cálido.",
            "Hazlo sin esperar respuesta inmediata."
          ),
          benefit = "Fortalece la cercanía cotidiana con poco esfuerzo."
        ),
        mission(
          id = "rela_2", dim = dim, isPriority = isPriority, durationLabel = "10 min", durationMinutes = 10,
          title = "Escuchar sin mirar el teléfono",
          summary = "Regala 10 minutos de atención real en una charla de hoy.",
          description = "Guardar el móvil cuando hablas con alguien transforma la calidad del encuentro.",
          steps = listOf(
            "Cuando hables con alguien hoy, guarda el teléfono en el bolsillo.",
            "Haz una pregunta abierta y escucha con calma.",
            "Disfruta el momento compartido."
          ),
          benefit = "Mejora la calidez y la sensación de estar acompañado."
        ),
        mission(
          id = "rela_3", dim = dim, isPriority = isPriority, durationLabel = "5 min", durationMinutes = 5,
          title = "Agradecer un gesto pequeño",
          summary = "Reconoce algo que alguien hizo por ti recientemente.",
          description = "Expresar gratitud concreta cuida los vínculos diarios en casa o el trabajo.",
          steps = listOf(
            "Recuerda un detalle amable que alguien tuvo contigo.",
            "Díselo o escríbele especificando qué valoraste.",
            "Nota cómo cambia el ambiente."
          ),
          benefit = "Nutre relaciones más amables y recíprocas."
        ),
        mission(
          id = "rela_4", dim = dim, isPriority = isPriority, durationLabel = "15 min", durationMinutes = 15,
          title = "Llamada corta con alguien de confianza",
          summary = "Llama 10-15 min a un amigo o familiar cercano.",
          description = "Escuchar la voz de alguien querido suele dar más calma que muchos chats.",
          steps = listOf(
            "Elige a una persona con quien te sientas cómodo.",
            "Propón una llamada breve mientras caminas o tomas algo.",
            "Comparte cómo estás de verdad."
          ),
          benefit = "Ayuda a sentir apoyo real y salir del aislamiento."
        ),
        mission(
          id = "rela_5", dim = dim, isPriority = isPriority, durationLabel = "5 min", durationMinutes = 5,
          title = "Proponer un café o paseo sencillo",
          summary = "Fija un día concreto para ver a alguien esta semana.",
          description = "Pasar de 'a ver cuándo nos vemos' a proponer un día hace reales los encuentros.",
          steps = listOf(
            "Piensa en alguien con quien disfrutas pasar tiempo.",
            "Envía una propuesta concreta de día u hora.",
            "Anótalo si confirman."
          ),
          benefit = "Transforma buenas intenciones en compañía real."
        ),
        mission(
          id = "rela_6", dim = dim, isPriority = isPriority, durationLabel = "5 min", durationMinutes = 5,
          title = "Pedir una mano en algo pequeño",
          summary = "Comparte una carga o duda con alguien de confianza.",
          description = "Permitir que te acompañen o ayuden alivia la sensación de cargarlo todo solo.",
          steps = listOf(
            "Identifica algo pequeño en lo que vendría bien apoyo.",
            "Pídelo con claridad y sencillez.",
            "Agradece la respuesta."
          ),
          benefit = "Fomenta vínculos más honestos y equilibrados."
        )
      )

      DimensionKey.HOBBIES -> listOf(
        mission(
          id = "hobb_1", dim = dim, isPriority = isPriority, durationLabel = "15 min", durationMinutes = 15,
          title = "15 minutos para algo que disfrutes",
          summary = "Lee, dibuja, cocina o toca música sin buscar productividad.",
          description = "Reservar un rato breve para hacer algo solo por gusto devuelve aire al día.",
          steps = listOf(
            "Elige una actividad creativa o manual que tengas a mano.",
            "Pon un temporizador suave de 15 minutos.",
            "Disfruta el proceso sin juzgar si sale perfecto."
          ),
          benefit = "Ayuda a desconectar de obligaciones y recuperar entusiasmo."
        ),
        mission(
          id = "hobb_2", dim = dim, isPriority = isPriority, durationLabel = "10 min", durationMinutes = 10,
          title = "Leer 5 páginas de algo por placer",
          summary = "Lee ficción, poesía o un tema curioso fuera del trabajo.",
          description = "Pocas páginas al día mantienen vivo un espacio personal sin requerir horas libres.",
          steps = listOf(
            "Ten un libro o artículo interesante cerca.",
            "Lee tranquilo durante 10 minutos.",
            "Guarda el punto de lectura sin exigirte avanzar rápido."
          ),
          benefit = "Despierta curiosidad y calma mental."
        ),
        mission(
          id = "hobb_3", dim = dim, isPriority = isPriority, durationLabel = "5 min", durationMinutes = 5,
          title = "Dejar tus materiales a la vista",
          summary = "Prepara tu libro, guitarra o libreta para mañana.",
          description = "Reducir el esfuerzo de empezar hace mucho más fácil retomar tus pasiones.",
          steps = listOf(
            "Elige un hobby que hayas dejado guardado.",
            "Saca lo básico y déjalo visible en un rincón.",
            "Dedícale aunque sea 2 minutos hoy."
          ),
          benefit = "Elimina la fricción para disfrutar tu tiempo libre."
        ),
        mission(
          id = "hobb_4", dim = dim, isPriority = isPriority, durationLabel = "10 min", durationMinutes = 10,
          title = "Escuchar un álbum o pieza con atención",
          summary = "Pon música nueva o favorita sin hacer otra cosa.",
          description = "Escuchar de verdad, y no solo de fondo, convierte 10 minutos en disfrute pleno.",
          steps = listOf(
            "Elige 2 o 3 canciones o una pieza instrumental.",
            "Siéntate o recuéstate cómodo.",
            "Escucha con atención durante unos minutos."
          ),
          benefit = "Regala un respiro sensorial agradable."
        ),
        mission(
          id = "hobb_5", dim = dim, isPriority = isPriority, durationLabel = "10 min", durationMinutes = 10,
          title = "Boceto o escritura libre",
          summary = "Dibuja o escribe media página sin corregirlo.",
          description = "Expresarte sin objetivo de resultado libera creatividad contenida.",
          steps = listOf(
            "Abre una libreta o papel suelto.",
            "Traza ideas, líneas o pensamientos durante 10 minutos.",
            "No lo evalúes ni busques que quede bonito."
          ),
          benefit = "Ayuda a despejar la cabeza de forma creativa."
        ),
        mission(
          id = "hobb_6", dim = dim, isPriority = isPriority, durationLabel = "15 min", durationMinutes = 15,
          title = "Probar una receta o bebida especial",
          summary = "Prepara con calma algo rico para ti.",
          description = "Cocinar algo sencillo con atención plena es una forma accesible de crear.",
          steps = listOf(
            "Elige algo simple que te apetezca.",
            "Prepáralo sin prisas ni pantallas.",
            "Disfrútalo sentado."
          ),
          benefit = "Convierte una necesidad diaria en un momento tuyo."
        )
      )

      DimensionKey.EXCELENCIA -> listOf(
        mission(
          id = "exce_1", dim = dim, isPriority = isPriority, durationLabel = "15 min", durationMinutes = 15,
          title = "Un bloque de 15 min en una sola tarea",
          summary = "Avanza en algo importante cerrando otras pestañas.",
          description = "Quince minutos sin interrupciones valen más que una hora fragmentada.",
          steps = listOf(
            "Elige una sola tarea relevante para hoy.",
            "Silencia notificaciones 15 minutos.",
            "Avanza lo posible con calma y claridad."
          ),
          benefit = "Genera sensación de avance real sin agobio."
        ),
        mission(
          id = "exce_2", dim = dim, isPriority = isPriority, durationLabel = "10 min", durationMinutes = 10,
          title = "Ordenar tu espacio de trabajo",
          summary = "Despeja tu escritorio o mochila en 10 minutos.",
          description = "Un entorno ordenado reduce ruido visual y facilita empezar mañana con serenidad.",
          steps = listOf(
            "Retira tazas, papeles sueltos y objetos fuera de lugar.",
            "Deja a la vista solo lo necesario.",
            "Observa la claridad del espacio."
          ),
          benefit = "Aporta calma y foco inmediato."
        ),
        mission(
          id = "exce_3", dim = dim, isPriority = isPriority, durationLabel = "10 min", durationMinutes = 10,
          title = "Cerrar una tarea pequeña pendiente",
          summary = "Termina algo breve que llevas días posponiendo.",
          description = "Resolver un pendiente ligero quita peso mental de inmediato.",
          steps = listOf(
            "Elige un trámite o mensaje corto que tome menos de 10 minutos.",
            "Hazlo de principio a fin ahora.",
            "Táchalo de tu lista."
          ),
          benefit = "Libera atención que antes gastabas recordándolo."
        ),
        mission(
          id = "exce_4", dim = dim, isPriority = isPriority, durationLabel = "10 min", durationMinutes = 10,
          title = "Cuidar un detalle de tu entrega",
          summary = "Revisa con cariño el acabado de algo que estás haciendo.",
          description = "Dar un poco más de esmero por satisfacción propia alimenta el buen hacer sin perfeccionismo.",
          steps = listOf(
            "Toma un proyecto o documento actual.",
            "Mejora su claridad o presentación en 10 minutos.",
            "Dalo por bueno al terminar."
          ),
          benefit = "Une orgullo sano por tu trabajo y límites claros."
        ),
        mission(
          id = "exce_5", dim = dim, isPriority = isPriority, durationLabel = "5 min", durationMinutes = 5,
          title = "Definir lo esencial de mañana",
          summary = "Elige la única tarea que hará valioso el día de mañana.",
          description = "Clarificar una prioridad evita dispersarte entre diez cosas urgentes.",
          steps = listOf(
            "Piensa qué asunto realmente importa mañana.",
            "Escríbelo en una línea corta.",
            "Reserva cuándo lo harás."
          ),
          benefit = "Da dirección serena a tu jornada."
        ),
        mission(
          id = "exce_6", dim = dim, isPriority = isPriority, durationLabel = "15 min", durationMinutes = 15,
          title = "Aprender algo útil de tu oficio",
          summary = "Dedica 15 minutos a mejorar una herramienta o habilidad.",
          description = "Avanzar de a poco en lo que haces mantiene viva la motivación profesional.",
          steps = listOf(
            "Elige un tema breve o tutorial práctico.",
            "Aplica al menos una idea en el momento.",
            "Anota qué te sirvió."
          ),
          benefit = "Fomenta crecimiento continuo sin saturación."
        )
      )
    }
  }

  private fun mission(
    id: String,
    dim: LifeDimension,
    isPriority: Boolean,
    durationLabel: String,
    durationMinutes: Int,
    title: String,
    summary: String,
    description: String,
    steps: List<String>,
    benefit: String
  ) = BalanceMission(
    id = id,
    dimensionIndex = dim.index,
    dimensionTitle = dim.title,
    title = title,
    summary = summary,
    description = description,
    steps = steps,
    benefit = benefit,
    durationLabel = durationLabel,
    durationMinutes = durationMinutes,
    isPriorityFocus = isPriority
  )
}
