package com.example.analysis

import com.example.model.AssessmentReport
import com.example.model.DimensionDiagnostic
import com.example.model.DimensionKey
import com.example.model.DimensionsProvider
import com.example.model.LifeDimension
import com.example.model.PatternInsight
import com.example.model.ScoreTier
import kotlin.math.pow
import kotlin.math.sqrt

object LifeAnalysisEngine {

  fun generateReport(
    scores: Map<Int, Int>,
    previousScores: Map<Int, Int>? = null
  ): AssessmentReport {
    val dimensions = DimensionsProvider.dimensions
    val validScores = dimensions.associate { it.index to (scores[it.index] ?: 5) }

    val scoreValues = validScores.values.toList()
    val averageScore = scoreValues.average().toFloat()

    val validPrevScores = previousScores?.let { prev ->
      dimensions.associate { it.index to (prev[it.index] ?: 5) }
    }
    val previousAverage = validPrevScores?.values?.toList()?.average()?.toFloat()

    val deltas = dimensions.associate { dim ->
      val curr = validScores[dim.index] ?: 5
      val prev = validPrevScores?.get(dim.index) ?: curr
      dim.index to (curr - prev)
    }

    val variance = scoreValues.map { (it - averageScore).toDouble().pow(2) }.average()
    val stdDev = sqrt(variance).toFloat()
    val harmony = ((1f - (stdDev / 4.5f).coerceIn(0f, 0.85f)) * 100).toInt()

    val highestIndex = validScores.maxByOrNull { it.value }?.key ?: 0
    val lowestIndex = validScores.minByOrNull { it.value }?.key ?: 1

    val highestDim = dimensions.firstOrNull { it.index == highestIndex } ?: dimensions[0]
    val lowestDim = dimensions.firstOrNull { it.index == lowestIndex } ?: dimensions[1]

    val autoScore = validScores[0] ?: 5
    val ejerScore = validScores[1] ?: 5
    val descScore = validScores[2] ?: 5
    val relaScore = validScores[3] ?: 5
    val hobbScore = validScores[4] ?: 5
    val exceScore = validScores[5] ?: 5

    val (archetypeTitle, archetypeSummary, coreTakeaway) = determineProfileSummary(
      autoScore, ejerScore, descScore, relaScore, hobbScore, exceScore, averageScore, stdDev
    )

    val (wisdomQuote, wisdomAuthor) = selectWisdomQuote(lowestDim.key)

    val patternInsights = detectCrossPatterns(
      autoScore, ejerScore, descScore, relaScore, hobbScore, exceScore, stdDev
    )

    val diagnostics = dimensions.map { dim ->
      val score = validScores[dim.index] ?: 5
      val prevScore = validPrevScores?.get(dim.index)
      buildDiagnosticForDimension(dim, score, prevScore)
    }

    val areasToNurtureCount = diagnostics.count { it.score <= 6 }
    val strengthsCount = diagnostics.count { it.score >= 7 }

    return AssessmentReport(
      scores = validScores,
      previousScores = validPrevScores,
      averageScore = averageScore,
      previousAverageScore = previousAverage,
      deltas = deltas,
      harmonyPercentage = harmony,
      highestDimension = highestDim,
      lowestDimension = lowestDim,
      archetypeTitle = archetypeTitle,
      archetypeSummary = archetypeSummary,
      coreTakeaway = coreTakeaway,
      wisdomQuote = wisdomQuote,
      wisdomAuthor = wisdomAuthor,
      patternInsights = patternInsights,
      diagnostics = diagnostics,
      areasToNurtureCount = areasToNurtureCount,
      strengthsCount = strengthsCount
    )
  }

  private fun determineProfileSummary(
    auto: Int, ejer: Int, desc: Int, rela: Int, hobb: Int, exce: Int, avg: Float, stdDev: Float
  ): Triple<String, String, String> {
    return when {
      avg >= 8.2f && stdDev <= 1.3f -> Triple(
        "Equilibrio Armónico",
        "Tus seis áreas conviven con bastante estabilidad. Cuidas lo que haces sin dejarte fuera a ti.",
        "El siguiente paso es proteger tus pausas y elegir con calma dónde quieres seguir creciendo."
      )
      exce >= 8 && desc <= 5 -> Triple(
        "Mucho Empuje, Poco Respiro",
        "Pones mucha dedicación en tus proyectos, pero tu descanso está quedando en segundo plano.",
        "Descansar a tiempo no frena lo que construyes; al contrario, sostiene tu claridad."
      )
      auto <= 5 && exce >= 8 -> Triple(
        "Exigencia Alta",
        "Cumples con gran cuidado hacia afuera, aunque por dentro te cuesta reconocer lo que ya logras.",
        "Puedes empezar a valorar tus avances cotidianos con el mismo respeto que pones en tu trabajo."
      )
      rela >= 8 && hobb >= 7 && exce <= 5 -> Triple(
        "Calidez y Disfrute",
        "Tienes muy presentes tus vínculos y el gusto por lo que te apasiona; ahora puede ayudarte dar un poco más de estructura a tus metas.",
        "Un bloque breve y constante al día basta para avanzar en tus proyectos sin perder tu calma."
      )
      ejer >= 8 && desc >= 7 && exce >= 7 -> Triple(
        "Base Física Firme",
        "Cuentas con buenos hábitos de movimiento, descanso y enfoque diario.",
        "Desde esa base estable puedes abrir más espacio para disfrutar sin prisa o compartir con otros."
      )
      rela <= 4 && hobb <= 4 -> Triple(
        "Modo Práctico",
        "Últimamente gran parte de tu energía se ha ido a cumplir responsabilidades, dejando poco espacio para compartir y disfrutar.",
        "Pequeños ratos de charla genuina o un pasatiempo breve pueden devolverte aire esta semana."
      )
      avg <= 4.2f -> Triple(
        "Momento de Cuidado Suave",
        "Hay varias áreas pidiendo atención al mismo tiempo, por lo que no conviene exigirte cambios grandes de golpe.",
        "Elige una sola área prioritaria y da pasos de 5 o 10 minutos al día."
      )
      stdDev >= 2.8f -> Triple(
        "Contraste entre Áreas",
        "Tienes pilares muy bien cuidados mientras otras facetas han recibido menos atención últimamente.",
        "Dedicar pequeños gestos diarios a lo que hoy está más rezagado traerá más calma general."
      )
      else -> Triple(
        "Camino en Construcción",
        "Tienes una base funcional y claridad para notar qué área merece un poco más de cariño en esta etapa.",
        "La constancia amable funciona mejor que la prisa: elige tu enfoque y ve paso a paso."
      )
    }
  }

  private fun selectWisdomQuote(lowest: DimensionKey): Pair<String, String> {
    return when (lowest) {
      DimensionKey.DESCANSO -> "El descanso es parte del camino, no una pausa que haya que justificar." to "Equilibrio"
      DimensionKey.AUTOESTIMA -> "Tratarte con respeto en los días difíciles vale tanto como celebrar tus aciertos." to "Equilibrio"
      DimensionKey.EJERCICIO -> "Un poco de movimiento amable cambia el tono entero del día." to "Equilibrio"
      DimensionKey.RELACIONES -> "Compartir lo que sientes con alguien cercano hace más ligero cualquier peso." to "Equilibrio"
      DimensionKey.HOBBIES -> "Hacer algo solo porque te gusta recuerda que la vida es más que una lista de tareas." to "Equilibrio"
      DimensionKey.EXCELENCIA -> "La serenidad también nace de cuidar con presencia aquello que eliges hacer hoy." to "Equilibrio"
    }
  }

  private fun detectCrossPatterns(
    auto: Int, ejer: Int, desc: Int, rela: Int, hobb: Int, exce: Int, stdDev: Float
  ): List<PatternInsight> {
    val list = mutableListOf<PatternInsight>()

    if (exce >= 8 && desc <= 5) {
      list.add(
        PatternInsight(
          title = "Ritmo muy exigente frente al descanso",
          subtitle = "Excelencia ($exce/10) y Descanso ($desc/10)",
          tag = "Balance de energía",
          description = "Estás dando mucho de ti en tus metas mientras el descanso ha quedado algo corto. Cuando ambos se equilibran, sostener lo que haces cuesta menos esfuerzo.",
          recommendation = "Prueba reservar 10 o 15 minutos sin pantallas antes de dormir o una pausa breve a mitad de la tarde.",
          isAlert = true
        )
      )
    }

    if (auto <= 5 && exce >= 7) {
      list.add(
        PatternInsight(
          title = "Dudas internas pese a tu dedicación",
          subtitle = "Autoestima ($auto/10) y Excelencia ($exce/10)",
          tag = "Mirada amable",
          description = "Pones empeño en hacer las cosas bien, pero a veces cuesta reconocer tu propio mérito cuando algo sale adelante.",
          recommendation = "Anota al final del día una cosa concreta que hiciste bien, por pequeña que parezca.",
          isAlert = true
        )
      )
    }

    if (rela <= 5 && hobb <= 5) {
      list.add(
        PatternInsight(
          title = "Espacio para compartir y disfrutar",
          subtitle = "Relaciones ($rela/10) y Hobbies ($hobb/10)",
          tag = "Tiempo personal",
          description = "Entre obligaciones diarias ha quedado poco hueco para ver a quienes aprecias o hacer algo por puro gusto.",
          recommendation = "Un mensaje breve a alguien cercano o 10 minutos con algo que disfrutes puede cambiar el aire de la semana.",
          isAlert = true
        )
      )
    }

    if (ejer <= 5 && desc <= 5) {
      list.add(
        PatternInsight(
          title = "El cuerpo pide cuidado cotidiano",
          subtitle = "Ejercicio ($ejer/10) y Descanso ($desc/10)",
          tag = "Vitalidad diaria",
          description = "Cuando el descanso y el movimiento bajan juntos, cualquier tarea cotidiana tiende a sentirse más pesada.",
          recommendation = "Empieza con algo accesible: una caminata de 10 minutos o cerrar el día un poco antes.",
          isAlert = true
        )
      )
    } else if (ejer >= 7 && desc >= 7) {
      list.add(
        PatternInsight(
          title = "Buena base física",
          subtitle = "Ejercicio ($ejer/10) y Descanso ($desc/10)",
          tag = "Pilar estable",
          description = "Cuidar tu movimiento y tu descanso te da un respaldo valioso para el resto de tus metas.",
          recommendation = "Usa esta base para avanzar poco a poco en el área que elijas cuidar ahora.",
          isAlert = false
        )
      )
    }

    if (stdDev >= 2.8f) {
      list.add(
        PatternInsight(
          title = "Áreas con ritmos distintos",
          subtitle = "Diferencia visible entre tus áreas altas y las más bajas",
          tag = "Visión general",
          description = "Es normal que no todas las áreas estén igual al mismo tiempo. No hace falta perfeccionar todo a la vez.",
          recommendation = "Escoge una sola área que hoy quieras cuidar con pasos breves y realistas.",
          isAlert = false
        )
      )
    }

    return list
  }

  private fun buildDiagnosticForDimension(
    dimension: LifeDimension, score: Int, prevScore: Int?
  ): DimensionDiagnostic {
    val tier = ScoreTier.fromScore(score)
    return when (dimension.key) {
      DimensionKey.AUTOESTIMA -> buildAutoestima(dimension, score, prevScore, tier)
      DimensionKey.EJERCICIO -> buildEjercicio(dimension, score, prevScore, tier)
      DimensionKey.DESCANSO -> buildDescanso(dimension, score, prevScore, tier)
      DimensionKey.RELACIONES -> buildRelaciones(dimension, score, prevScore, tier)
      DimensionKey.HOBBIES -> buildHobbies(dimension, score, prevScore, tier)
      DimensionKey.EXCELENCIA -> buildExcelencia(dimension, score, prevScore, tier)
    }
  }

  private fun buildAutoestima(dim: LifeDimension, score: Int, prevScore: Int?, tier: ScoreTier): DimensionDiagnostic {
    val philosophy = "La confianza crece cuando te tratas con justicia en los días comunes, no solo cuando todo sale perfecto."
    return when (tier) {
      ScoreTier.CRITICAL -> DimensionDiagnostic(
        dimension = dim, score = score, previousScore = prevScore, tier = tier, corePhilosophy = philosophy,
        diagnosisSummary = "Hoy te cuesta reconocer tu propio valor y sueles exigirte más de la cuenta.",
        currentImpact = "Puede hacer que dudes antes de opinar o que sientas cansancio por buscar aprobación externa.",
        blindSpots = listOf(
          "Fijarte más en lo que faltó que en lo que sí sostuviste hoy.",
          "Hablar contigo con dureza cuando algo no sale a la primera."
        ),
        actionableImprovements = listOf(
          "Anota cada tarde una cosa pequeña que resolviste bien.",
          "Practica decir que no con calma a una petición secundaria."
        )
      )
      ScoreTier.DEVELOPING -> DimensionDiagnostic(
        dimension = dim, score = score, previousScore = prevScore, tier = tier, corePhilosophy = philosophy,
        diagnosisSummary = "Tu confianza se siente estable en días buenos, pero se mueve cuando hay incertidumbre.",
        currentImpact = "Un comentario externo puede pesar más de lo necesario en tu ánimo del día.",
        blindSpots = listOf(
          "Medir tu día únicamente por resultados externos.",
          "Minimizar tus logros cuando alguien te los reconoce."
        ),
        actionableImprovements = listOf(
          "Acepta los cumplidos con un simple 'gracias'.",
          "Toma una decisión cotidiana confiando en tu propio criterio."
        )
      )
      ScoreTier.SOLID -> DimensionDiagnostic(
        dimension = dim, score = score, previousScore = prevScore, tier = tier, corePhilosophy = philosophy,
        diagnosisSummary = "Mantienes una relación sana y respetuosa contigo.",
        currentImpact = "Te ayuda a afrontar conversaciones o decisiones cotidianas con serenidad.",
        blindSpots = listOf("Recordar que también puedes pedir ayuda en días de poca energía."),
        actionableImprovements = listOf("Comparte cómo te sientes cuando necesites apoyo, sin cargarlo todo solo.")
      )
      ScoreTier.EXCEPTIONAL -> DimensionDiagnostic(
        dimension = dim, score = score, previousScore = prevScore, tier = tier, corePhilosophy = philosophy,
        diagnosisSummary = "Tienes una confianza serena y realista en tus capacidades.",
        currentImpact = "Te permite aprender de los tropiezos sin castigarte.",
        blindSpots = listOf("Cuidar la escucha paciente hacia quienes atraviesan momentos de duda."),
        actionableImprovements = listOf("Sigue cuidando tu voz interior con honestidad y calma.")
      )
    }
  }

  private fun buildEjercicio(dim: LifeDimension, score: Int, prevScore: Int?, tier: ScoreTier): DimensionDiagnostic {
    val philosophy = "Mover el cuerpo no es una deuda ni una competencia; es una forma sencilla de sentirte más vivo hoy."
    return when (tier) {
      ScoreTier.CRITICAL -> DimensionDiagnostic(
        dimension = dim, score = score, previousScore = prevScore, tier = tier, corePhilosophy = philosophy,
        diagnosisSummary = "Últimamente te has movido poco y el cuerpo puede sentirse pesado.",
        currentImpact = "Es común notar rigidez en cuello o espalda y menos energía durante la jornada.",
        blindSpots = listOf(
          "Creer que si no haces una rutina larga no cuenta.",
          "Esperar a tener muchas ganas para dar el primer paso."
        ),
        actionableImprovements = listOf(
          "Camina 10 minutos al aire libre sin exigirte ritmo alto.",
          "Haz 3 minutos de estiramientos suaves al levantarte de la silla."
        )
      )
      ScoreTier.DEVELOPING -> DimensionDiagnostic(
        dimension = dim, score = score, previousScore = prevScore, tier = tier, corePhilosophy = philosophy,
        diagnosisSummary = "Alternas días activos con rachas en las que cuesta retomar el movimiento.",
        currentImpact = "La energía sube y baja según si logras hacerte un hueco en la semana.",
        blindSpots = listOf("Abandonar toda la semana por haber faltado un día."),
        actionableImprovements = listOf(
          "Elige acciones breves de 10 minutos los días ocupados.",
          "Acompaña el movimiento con música o un paseo agradable."
        )
      )
      ScoreTier.SOLID -> DimensionDiagnostic(
        dimension = dim, score = score, previousScore = prevScore, tier = tier, corePhilosophy = philosophy,
        diagnosisSummary = "El movimiento forma parte natural de tu semana.",
        currentImpact = "Ayuda a regular el estrés y sostener tu vitalidad.",
        blindSpots = listOf("Acordarte de incluir estiramientos suaves además del esfuerzo."),
        actionableImprovements = listOf("Mantén un ritmo sostenible que disfrutes a largo plazo.")
      )
      ScoreTier.EXCEPTIONAL -> DimensionDiagnostic(
        dimension = dim, score = score, previousScore = prevScore, tier = tier, corePhilosophy = philosophy,
        diagnosisSummary = "Tu cuerpo cuenta con un hábito activo muy bien integrado.",
        currentImpact = "Se traduce en buena energía física y claridad mental.",
        blindSpots = listOf("Respetar también los días de descarga y descanso."),
        actionableImprovements = listOf("Escucha tus sensaciones para equilibrar actividad y recuperación.")
      )
    }
  }

  private fun buildDescanso(dim: LifeDimension, score: Int, prevScore: Int?, tier: ScoreTier): DimensionDiagnostic {
    val philosophy = "Descansar bien es lo que permite vivir y pensar con claridad, no un premio para el final."
    return when (tier) {
      ScoreTier.CRITICAL -> DimensionDiagnostic(
        dimension = dim, score = score, previousScore = prevScore, tier = tier, corePhilosophy = philosophy,
        diagnosisSummary = "Tu descanso está siendo ligero o insuficiente y el cansancio se acumula.",
        currentImpact = "Cuesta más concentrarse, el ánimo se vuelve más frágil y la mente sigue encendida de noche.",
        blindSpots = listOf(
          "Quedarte con el teléfono en la cama hasta que vence el sueño.",
          "Intentar resolver de madrugada lo que quedó abierto en el día."
        ),
        actionableImprovements = listOf(
          "Deja las pantallas 15 minutos antes de acostarte.",
          "Anota 3 pendientes en papel para sacarlos de la cabeza antes de dormir."
        )
      )
      ScoreTier.DEVELOPING -> DimensionDiagnostic(
        dimension = dim, score = score, previousScore = prevScore, tier = tier, corePhilosophy = philosophy,
        diagnosisSummary = "Aunque duermes algunas horas, cuesta desconectar del todo al terminar el día.",
        currentImpact = "Es habitual notar bajones de energía a media tarde.",
        blindSpots = listOf("Confundir descansar con pasar tiempo mirando redes o noticias."),
        actionableImprovements = listOf(
          "Haz una pausa de 3 minutos sin pantallas a mitad del día.",
          "Prueba 6 respiraciones lentas al meterte a la cama."
        )
      )
      ScoreTier.SOLID -> DimensionDiagnostic(
        dimension = dim, score = score, previousScore = prevScore, tier = tier, corePhilosophy = philosophy,
        diagnosisSummary = "Cuidas tus horarios y sueles despertar con energía suficiente.",
        currentImpact = "Sostiene tu estado de ánimo y tu enfoque cotidiano.",
        blindSpots = listOf("Evitar recortar sueño cuando se juntan pendientes."),
        actionableImprovements = listOf("Protege tu ritual nocturno sencillo incluso en semanas movidas.")
      )
      ScoreTier.EXCEPTIONAL -> DimensionDiagnostic(
        dimension = dim, score = score, previousScore = prevScore, tier = tier, corePhilosophy = philosophy,
        diagnosisSummary = "Tienes una relación muy sana con tus pausas y tu sueño.",
        currentImpact = "Tu mente y cuerpo cuentan con buena recuperación diaria.",
        blindSpots = listOf("Mantener flexibilidad amable si algún día cambian tus horarios."),
        actionableImprovements = listOf("Conserva esas pausas que te devuelven serenidad.")
      )
    }
  }

  private fun buildRelaciones(dim: LifeDimension, score: Int, prevScore: Int?, tier: ScoreTier): DimensionDiagnostic {
    val philosophy = "Sentirse escuchado y acompañar a otros hace más habitable la vida cotidiana."
    return when (tier) {
      ScoreTier.CRITICAL -> DimensionDiagnostic(
        dimension = dim, score = score, previousScore = prevScore, tier = tier, corePhilosophy = philosophy,
        diagnosisSummary = "Últimamente has compartido poco tiempo cercano y puedes estar cargando cosas en silencio.",
        currentImpact = "La distancia cotidiana puede generar sensación de soledad aunque estés rodeado de gente.",
        blindSpots = listOf(
          "Pensar que escribes para molestar cuando en realidad acercas.",
          "Dejar los mensajes importantes siempre para luego."
        ),
        actionableImprovements = listOf(
          "Envía un mensaje breve hoy a alguien que aprecies.",
          "Comparte con sinceridad cómo estás con alguien de confianza."
        )
      )
      ScoreTier.DEVELOPING -> DimensionDiagnostic(
        dimension = dim, score = score, previousScore = prevScore, tier = tier, corePhilosophy = philosophy,
        diagnosisSummary = "Mantienes contacto frecuente, aunque a veces falta tiempo de calidad sin prisas.",
        currentImpact = "Las charlas rápidas no siempre dejan sensación de cercanía real.",
        blindSpots = listOf("Mirar notificaciones mientras conversas con alguien."),
        actionableImprovements = listOf(
          "Guarda el teléfono durante una conversación de 10 minutos hoy.",
          "Propón un café o llamada tranquila esta semana."
        )
      )
      ScoreTier.SOLID -> DimensionDiagnostic(
        dimension = dim, score = score, previousScore = prevScore, tier = tier, corePhilosophy = philosophy,
        diagnosisSummary = "Cuentas con vínculos cercanos donde hay escucha y cariño mutuo.",
        currentImpact = "Te da respaldo emocional en momentos buenos y retadores.",
        blindSpots = listOf("No dar por sentadas las amistades cuando aprieta la rutina."),
        actionableImprovements = listOf("Agradece algún gesto cotidiano de quienes te acompañan.")
      )
      ScoreTier.EXCEPTIONAL -> DimensionDiagnostic(
        dimension = dim, score = score, previousScore = prevScore, tier = tier, corePhilosophy = philosophy,
        diagnosisSummary = "Tus relaciones son un apoyo vivo y recíproco en tu día a día.",
        currentImpact = "Aportan calidez, alegría y pertenencia.",
        blindSpots = listOf("Recordar guardar también espacios de descanso personal."),
        actionableImprovements = listOf("Sigue cuidando esos encuentros reales que te hacen bien.")
      )
    }
  }

  private fun buildHobbies(dim: LifeDimension, score: Int, prevScore: Int?, tier: ScoreTier): DimensionDiagnostic {
    val philosophy = "Hacer algo por simple disfrute da respiro a la mente y recuerda que no todo es producir."
    return when (tier) {
      ScoreTier.CRITICAL -> DimensionDiagnostic(
        dimension = dim, score = score, previousScore = prevScore, tier = tier, corePhilosophy = philosophy,
        diagnosisSummary = "Hace tiempo que no te regalas un rato para hacer algo solo porque te gusta.",
        currentImpact = "La rutina se siente repetitiva y cuesta desconectar de las obligaciones.",
        blindSpots = listOf(
          "Sentir culpa si una actividad no tiene utilidad práctica.",
          "Esperar a tener toda la tarde libre para empezar."
        ),
        actionableImprovements = listOf(
          "Dedica 10 o 15 minutos a leer, dibujar, cocinar o escuchar música con calma.",
          "Deja tus materiales a la vista para que sea fácil retomarlos."
        )
      )
      ScoreTier.DEVELOPING -> DimensionDiagnostic(
        dimension = dim, score = score, previousScore = prevScore, tier = tier, corePhilosophy = philosophy,
        diagnosisSummary = "Disfrutas tus pasatiempos cuando aparece un hueco, aunque suelen quedar al final de la fila.",
        currentImpact = "Extrañas tener un espacio propio más constante.",
        blindSpots = listOf("Pasar el tiempo libre viendo contenido en vez de crear o jugar."),
        actionableImprovements = listOf(
          "Reserva bloques cortos de 15 minutos sin buscar hacerlo perfecto.",
          "Elige algo sencillo que tengas ganas de probar hoy."
        )
      )
      ScoreTier.SOLID -> DimensionDiagnostic(
        dimension = dim, score = score, previousScore = prevScore, tier = tier, corePhilosophy = philosophy,
        diagnosisSummary = "Tienes un espacio real para tu creatividad y disfrute personal.",
        currentImpact = "Te ayuda a oxigenar la mente y volver con mejor ánimo a lo demás.",
        blindSpots = listOf("Evitar convertir cada pasión en una obligación más."),
        actionableImprovements = listOf("Protege ese rato libre de métricas y exigencias.")
      )
      ScoreTier.EXCEPTIONAL -> DimensionDiagnostic(
        dimension = dim, score = score, previousScore = prevScore, tier = tier, corePhilosophy = philosophy,
        diagnosisSummary = "Cultivas con constancia aquello que despierta tu curiosidad y alegría.",
        currentImpact = "Mantiene fresca tu creatividad y tu bienestar cotidiano.",
        blindSpots = listOf("Equilibrar la inspiración con los compromisos prácticos."),
        actionableImprovements = listOf("Sigue disfrutando sin presiones de rendimiento.")
      )
    }
  }

  private fun buildExcelencia(dim: LifeDimension, score: Int, prevScore: Int?, tier: ScoreTier): DimensionDiagnostic {
    val philosophy = "Hacer las cosas con cuidado nace de la presencia, no de la prisa ni del perfeccionismo."
    return when (tier) {
      ScoreTier.CRITICAL -> DimensionDiagnostic(
        dimension = dim, score = score, previousScore = prevScore, tier = tier, corePhilosophy = philosophy,
        diagnosisSummary = "Hoy cuesta encontrar foco o motivación en tus tareas y proyectos.",
        currentImpact = "Es fácil dispersarse o sentir que avanzas por inercia.",
        blindSpots = listOf(
          "Querer resolver diez cosas a la vez en lugar de una sola.",
          "Postergar tareas pequeñas que luego ocupan espacio mental."
        ),
        actionableImprovements = listOf(
          "Elige una tarea breve y dedícale 15 minutos sin distracciones.",
          "Ordena lo básico de tu espacio para despejar la vista."
        )
      )
      ScoreTier.DEVELOPING -> DimensionDiagnostic(
        dimension = dim, score = score, previousScore = prevScore, tier = tier, corePhilosophy = philosophy,
        diagnosisSummary = "Cumples con lo importante, aunque a veces sientes dispersión o falta de cierre.",
        currentImpact = "Terminar el día con cosas a medias genera cansancio extra.",
        blindSpots = listOf("Dejar lo verdaderamente valioso para cuando se acaben las urgencias."),
        actionableImprovements = listOf(
          "Define una única prioridad al empezar la jornada.",
          "Cierra un pendiente pequeño de menos de 10 minutos."
        )
      )
      ScoreTier.SOLID -> DimensionDiagnostic(
        dimension = dim, score = score, previousScore = prevScore, tier = tier, corePhilosophy = philosophy,
        diagnosisSummary = "Trabajas con constancia, claridad y gusto por los detalles bien hechos.",
        currentImpact = "Aporta tranquilidad saber que avanzas con orden en lo que importa.",
        blindSpots = listOf("Saber parar a tiempo cuando algo ya está suficientemente bien."),
        actionableImprovements = listOf("Equilibra tus bloques de foco con pausas breves.")
      )
      ScoreTier.EXCEPTIONAL -> DimensionDiagnostic(
        dimension = dim, score = score, previousScore = prevScore, tier = tier, corePhilosophy = philosophy,
        diagnosisSummary = "Tienes un gran compromiso con la calidad y la claridad en lo que emprendes.",
        currentImpact = "Tu dedicación da frutos sólidos y constantes.",
        blindSpots = listOf("Cuidar que la exigencia no desplace tu descanso ni tus vínculos."),
        actionableImprovements = listOf("Celebra también los avances intermedios sin esperar al final.")
      )
    }
  }
}
