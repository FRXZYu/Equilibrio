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
    val validScores = dimensions.map { it.index to (scores[it.index] ?: 5) }.toMap()

    val scoreValues = validScores.values.toList()
    val averageScore = scoreValues.average().toFloat()

    val validPrevScores = previousScores?.let { prev ->
      dimensions.map { it.index to (prev[it.index] ?: 5) }.toMap()
    }
    val previousAverage = validPrevScores?.values?.toList()?.average()?.toFloat()

    // Calculate deltas
    val deltas = dimensions.associate { dim ->
      val curr = validScores[dim.index] ?: 5
      val prev = validPrevScores?.get(dim.index) ?: curr
      dim.index to (curr - prev)
    }

    // Standard deviation for harmony calculation
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

    // Archetype generation
    val (archetypeTitle, archetypeSummary, coreTakeaway) = determineArchetype(
      autoScore, ejerScore, descScore, relaScore, hobbScore, exceScore, averageScore, stdDev
    )

    // Wisdom quote tailored to archetype
    val (wisdomQuote, wisdomAuthor) = selectWisdomQuote(lowestDim.key, highestDim.key)

    // Cross-pattern insights
    val patternInsights = detectCrossPatterns(
      autoScore, ejerScore, descScore, relaScore, hobbScore, exceScore, stdDev
    )

    // Diagnostics for all 6 dimensions
    val diagnostics = dimensions.map { dim ->
      val score = validScores[dim.index] ?: 5
      val prevScore = validPrevScores?.get(dim.index)
      buildDiagnosticForDimension(dim, score, prevScore)
    }

    val weaknessesCount = diagnostics.count { it.score <= 6 }
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
      weaknessesCount = weaknessesCount,
      strengthsCount = strengthsCount
    )
  }

  private fun determineArchetype(
    auto: Int, ejer: Int, desc: Int, rela: Int, hobb: Int, exce: Int, avg: Float, stdDev: Float
  ): Triple<String, String, String> {
    return when {
      avg >= 8.2f && stdDev <= 1.3f -> {
        Triple(
          "El Integrador Pleno",
          "Tus seis dimensiones dialogan con armonía y madurez. Has superado la falsa dicotomía entre rendir al máximo y cuidarte con cariño.",
          "Tu mayor reto ya no es construir cimientos, sino defender tus fronteras para no permitir que la inercia del entorno diluya tu lucidez."
        )
      }
      exce >= 8 && desc <= 5 -> {
        "El Arquitecto en Sobremarcha" to Triple(
          "El Arquitecto en Sobremarcha",
          "Tu estándar de entrega es implacable, pero tu energía somática está financiando tus victorias a un interés biológico insostenible.",
          "El agotamiento no es un trofeo de disciplina. Si no programas tu recuperación de forma deliberada, tu cuerpo la programará por ti en forma de colapso o apatía."
        ).third to Triple(
          "El Arquitecto en Sobremarcha",
          "Tu estándar de entrega es implacable, pero tu energía somática está financiando tus victorias a un interés biológico insostenible.",
          "El agotamiento no es un trofeo de disciplina. Si no programas tu recuperación de forma deliberada, tu cuerpo la programará por ti en forma de colapso o apatía."
        ).second
        Triple(
          "El Arquitecto en Sobremarcha",
          "Tu estándar de entrega es implacable, pero tu energía somática está financiando tus victorias a un interés biológico insostenible.",
          "El agotamiento no es un trofeo de disciplina. Si no programas tu recuperación deliberada, tu cuerpo la programará por ti en forma de colapso o apatía."
        )
      }
      auto <= 5 && exce >= 8 -> {
        Triple(
          "El Perfeccionista Cauto",
          "Tus entregas deslumbran por rigor, pero internamente sientes que caminas sobre hielo delgado esperando que alguien descubra un fallo inexistente.",
          "Trabajar el doble no cura la inseguridad interna; solo alimenta la ficción de que tu valor como ser humano depende de tu último resultado."
        )
      }
      rela >= 8 && hobb >= 7 && exce <= 5 -> {
        Triple(
          "El Conector Afectivo",
          "Eres el corazón cálido de tus círculos y posees una relación fecunda con el gozo no utilitario, aunque postergas proyectos que exigen disciplina solitaria.",
          "La excelencia no es soberbia ni frialdad: es la ofrenda que le haces a tus propios talentos para que no se queden en meras intenciones."
        )
      }
      ejer >= 8 && desc >= 7 && exce >= 7 -> {
        Triple(
          "El Atleta Disciplinado",
          "Cuentas con un motor somático blindado y una alta tolerancia al esfuerzo continuo.",
          "Tu siguiente umbral de crecimiento no está en empujar más fuerte, sino en permitirte el juego libre, la risa absurda y la vulnerabilidad con otros."
        )
      }
      rela <= 4 && hobb <= 4 -> {
        Triple(
          "El Operador Instrumental",
          "Has reducido tu vida al estricto cumplimiento de compromisos prácticos. Eres funcional por fuera, pero tu interior sufre sequía de asombro y cercanía.",
          "El aislamiento no te protege del cansancio: te priva del único antídoto comprobado contra la fatiga existencial: el vínculo afectivo y el juego."
        )
      }
      avg <= 4.2f -> {
        Triple(
          "El Navegante en Reconstrucción",
          "Atraviesas un valle de agotamiento sistémico. La motivación no puede florecer donde la batería biológica y emocional marca reserva.",
          "No necesitas más fuerza de voluntad ni cursos de productividad. Necesitas compasión urgente, descanso inviolable y micro-pasos de 10 minutos."
        )
      }
      stdDev >= 2.8f -> {
        Triple(
          "El Creador Polarizado",
          "Vives en un contraste marcado: tienes áreas sobresalientes que brillan con intensidad mientras otras operan con déficit crítico.",
          "El eslabón más débil de una cadena determina su resistencia máxima. Elevar tus áreas bajas de un 3 a un 6 multiplicará por diez tu estabilidad global."
        )
      }
      else -> {
        Triple(
          "El Buscador Consciente",
          "Mantienes un equilibrio funcional con cimientos estables y una lucidez admirable para reconocer dónde se escapa tu energía cotidiana.",
          "La serenidad no es ausencia de retos, sino la habilidad de calibrar tu rumbo semana a semana sin juicios destructivos."
        )
      }
    }
  }

  private fun selectWisdomQuote(lowest: DimensionKey, highest: DimensionKey): Pair<String, String> {
    return when (lowest) {
      DimensionKey.DESCANSO -> "El reposo es el salario de la lucidez. Quien no descansa, no piensa: solo reacciona." to "Marco Aurelio"
      DimensionKey.AUTOESTIMA -> "Tu valía no se negocia con tus aciertos ni se destruye con tus caídas; es tu punto de partida, nunca el premio final." to "Viktor Frankl"
      DimensionKey.EJERCICIO -> "El cuerpo es el templo que sostiene tu mente. Trátalo con el respeto que le debes a tu único hogar en este mundo." to "Séneca"
      DimensionKey.RELACIONES -> "La herida se produce en soledad, pero la curación siempre ocurre en presencia de un testigo compasivo." to "Carl Rogers"
      DimensionKey.HOBBIES -> "Hacer algo exclusivamente por el placer de hacerlo es la mayor rebelión contra un mundo obsesionado con la utilidad." to "Alan Watts"
      DimensionKey.EXCELENCIA -> "No somos lo que decimos que haremos, sino la calidad con la que tratamos los detalles invisibles de lo que tocamos." to "Aristóteles"
    }
  }

  private fun detectCrossPatterns(
    auto: Int, ejer: Int, desc: Int, rela: Int, hobb: Int, exce: Int, stdDev: Float
  ): List<PatternInsight> {
    val list = mutableListOf<PatternInsight>()

    // Pattern 1: Excelencia vs Descanso
    if (exce >= 8 && desc <= 5) {
      list.add(
        PatternInsight(
          title = "La Trampa del Rendimiento Neurótico",
          subtitle = "Excelencia ($exce/10) financiada a costa de Descanso ($desc/10)",
          tag = "Riesgo de Burnout",
          description = "Entregas con un rigor intachable mientras tu biología acumula deuda de sueño y tensión crónica. No es disciplina real si te está destruyendo lentamente. El descanso profundo no es un premio que se merece tras el colapso: es la condición indispensable para que tu lucidez no se marchite.",
          recommendation = "Convierte el descanso en un bloque no negociable de tu agenda. Si te cuesta parar, míralo como parte de tu entrenamiento: sin recuperación celular, la mente pierde capacidad estratégica.",
          isAlert = true
        )
      )
    }

    // Pattern 2: Autoestima vs Excelencia
    if (auto <= 5 && exce >= 7) {
      list.add(
        PatternInsight(
          title = "La Paradoja del Mérito Insuficiente",
          subtitle = "Autoestima ($auto/10) rezagada frente a tu Exigencia ($exce/10)",
          tag = "Síndrome del Impostor",
          description = "Cuidas cada detalle para blindarte contra el error por temor a que una equivocación confirme tu falsa insuficiencia. Logras metas altas, pero la calma dura apenas unas horas antes de que la autoexigencia te reclute para la siguiente batalla.",
          recommendation = "Separa tu identidad de tu producción: tú eres la persona que crea, no la obra creada. Practica celebrar el esfuerzo honesto, incluso en proyectos que salgan defectuosos.",
          isAlert = true
        )
      )
    }

    // Pattern 3: Aislamiento Funcional
    if (rela <= 5 && hobb <= 5) {
      list.add(
        PatternInsight(
          title = "Sequía de Vínculos y Asombro",
          subtitle = "Relaciones ($rela/10) y Hobbies ($hobb/10) bajo mínimos",
          tag = "Alerta Emocional",
          description = "Has sacrificado la ternura compartida y el juego no utilitario para cumplir con la lista de deberes. La psique humana no puede sostenerse solo con obligaciones; sin risas compartidas ni pasiones desinteresadas, el cerebro entra en modo apatía defensiva.",
          recommendation = "Agenda esta semana una cita sagrada contigo misma para una actividad puramente creativa (sin móvil) y haz una llamada sincera de 15 minutos a alguien que te quiera sin condiciones.",
          isAlert = true
        )
      )
    }

    // Pattern 4: Eje Biológico (Ejercicio + Descanso)
    if (ejer <= 5 && desc <= 5) {
      list.add(
        PatternInsight(
          title = "Déficit Somático de Base",
          subtitle = "Cuerpo en modo reserva (Ejercicio $ejer/10, Descanso $desc/10)",
          tag = "Salud Biológica",
          description = "El estado anímico, la autoconfianza y la resiliencia mental no son conceptos etéreos: son neurotransmisores (dopamina, serotonina, GABA) sintetizados a partir del sueño reparador y la contracción muscular. Sin cuerpo descansado, cualquier esfuerzo mental cuesta el triple.",
          recommendation = "Prioriza una caminata de 20 minutos con luz natural por la mañana y apaga las pantallas 45 minutos antes de dormir. Recuperar el eje biológico es la palanca más rápida para transformar tu motivación.",
          isAlert = true
        )
      )
    } else if (ejer >= 7 && desc >= 7) {
      list.add(
        PatternInsight(
          title = "Fundamento Somático Privilegiado",
          subtitle = "Ejercicio ($ejer/10) y Descanso ($desc/10) en sincronía",
          tag = "Pilar Protector",
          description = "Tu cuerpo es un aliado leal que responde con claridad, vitalidad y tolerancia a la frustración. Esta reserva energética es tu mayor activo para impulsar las dimensiones que hoy demandan tu cariño.",
          recommendation = "Aprovecha esta claridad física para tomar decisiones valientes en tus proyectos o relaciones personales.",
          isAlert = false
        )
      )
    }

    // Pattern 5: Polarización vs Armonía
    if (stdDev >= 2.8f) {
      list.add(
        PatternInsight(
          title = "Dispersión Energética Polarizada",
          subtitle = "Brecha marcada entre tus picos y tus valles",
          tag = "Equilibrio Sistémico",
          description = "Tener áreas en 9 o 10 mientras otras caen a 2 o 3 crea un desbalance silencioso. Las áreas abandonadas drenan la satisfacción que cosechas en tus triunfos.",
          recommendation = "Aplica la ley de Pareto: no busques el 10 en todo. Lleva las dimensiones críticas al menos a un 5 o 6 funcional; notarás un alivio inmediato de tensión interna.",
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
      DimensionKey.AUTOESTIMA -> buildAutoestimaDiagnostic(dimension, score, prevScore, tier)
      DimensionKey.EJERCICIO -> buildEjercicioDiagnostic(dimension, score, prevScore, tier)
      DimensionKey.DESCANSO -> buildDescansoDiagnostic(dimension, score, prevScore, tier)
      DimensionKey.RELACIONES -> buildRelacionesDiagnostic(dimension, score, prevScore, tier)
      DimensionKey.HOBBIES -> buildHobbiesDiagnostic(dimension, score, prevScore, tier)
      DimensionKey.EXCELENCIA -> buildExcelenciaDiagnostic(dimension, score, prevScore, tier)
    }
  }

  private fun buildAutoestimaDiagnostic(
    dim: LifeDimension, score: Int, prevScore: Int?, tier: ScoreTier
  ): DimensionDiagnostic {
    val philosophy = "La autoestima auténtica no es creer que eres invulnerable, sino saber que puedes equivocarte sin dejar de respetarte y acogerte."

    return when (tier) {
      ScoreTier.CRITICAL -> DimensionDiagnostic(
        dimension = dim,
        score = score,
        previousScore = prevScore,
        tier = tier,
        corePhilosophy = philosophy,
        diagnosisSummary = "Predomina una autocrítica corrosiva. Conviertes tropiezos naturales en sentencias sobre tu valor esencial.",
        currentImpact = "Dudas antes de alzar la voz; postergas decisiones autónomas por miedo al ridículo y necesitas excesiva validación para sentir calma.",
        blindSpots = listOf(
          "Asumir que si alguien te corrige o discrepa de ti, es una prueba irrefutable de tu insuficiencia.",
          "Exigirte saberlo todo antes de atreverte a dar el primer paso.",
          "Fijarte obsesivamente en lo que te faltó en lugar de reconocer lo que ya sostienes con dignidad."
        ),
        actionableImprovements = listOf(
          "Aprende a hablarte como le hablarías a una amiga muy querida en su momento más difícil.",
          "Cada noche, registra 1 decisión que tomaste por ti misma sin consultar la aprobación de terceros.",
          "Practica decir un 'no' sereno y firme a una petición menor esta semana, sin inventar disculpas elaboradas."
        )
      )
      ScoreTier.DEVELOPING -> DimensionDiagnostic(
        dimension = dim,
        score = score,
        previousScore = prevScore,
        tier = tier,
        corePhilosophy = philosophy,
        diagnosisSummary = "Tu autoconfianza es reactiva: se mantiene firme cuando los resultados acompañan, pero tambalea ante la incertidumbre o una mirada fría.",
        currentImpact = "Un elogio te eleva y una crítica menor te nubla el día. Tu estado anímico depende en exceso de cómo responde tu entorno.",
        blindSpots = listOf(
          "Delegar la definición de tu valor en la mirada o el aplauso de los demás.",
          "Comparar tu trastienda desordenada con el escaparate impecable de otras personas en redes sociales."
        ),
        actionableImprovements = listOf(
          "Define tu propio estándar de suficiencia antes de iniciar cualquier entrega.",
          "Acepta cumplidos con un simple 'gracias, lo aprecio' sin restarles mérito ni justificarte.",
          "Asume una pequeña incomodidad semanal: expresa tu opinión genuina en un tema donde antes callabas por prudencia."
        )
      )
      ScoreTier.SOLID -> DimensionDiagnostic(
        dimension = dim,
        score = score,
        previousScore = prevScore,
        tier = tier,
        corePhilosophy = philosophy,
        diagnosisSummary = "Posees un arraigo emocional maduro. Sabes quién eres y no necesitas justificarte ante cada encrucijada.",
        currentImpact = "Afrontas conversaciones difíciles con templanza, reconoces tus fortalezas y aceptas tus límites sin angustia existencial.",
        blindSpots = listOf(
          "Cierta reticencia a mostrarte frágil por proteger la imagen de 'persona resuelta'.",
          "Olvidar que la autoaceptación incluye abrazar los días donde la energía flaquea."
        ),
        actionableImprovements = listOf(
          "Atrévete a pedir ayuda explícita cuando te sientas sobrecargada; la vulnerabilidad es el pico más alto de la seguridad interna.",
          "Nutre tus convicciones en soledad antes de entrar en debates donde otros intentan imponer su ritmo."
        )
      )
      ScoreTier.EXCEPTIONAL -> DimensionDiagnostic(
        dimension = dim,
        score = score,
        previousScore = prevScore,
        tier = tier,
        corePhilosophy = philosophy,
        diagnosisSummary = "Confianza sólida, generosa y desapegada del ego. Irradias tranquilidad sin necesidad de imponerla.",
        currentImpact = "Eres refugio de seguridad para otros; asumes riesgos constructivos y toleras el fracaso como una simple variable de aprendizaje.",
        blindSpots = listOf(
          "Riesgo de perder empatía con quienes viven atrapados en dudas paralizantes.",
          "Cuidar que la alta seguridad no derive en ceguera ante perspectivas ajenas."
        ),
        actionableImprovements = listOf(
          "Sé mentora y puente para que personas inseguras descubran su propia voz.",
          "Combina tu seguridad con curiosidad socrática constante: escuchar con apertura multiplica tu impacto."
        )
      )
    }
  }

  private fun buildEjercicioDiagnostic(
    dim: LifeDimension, score: Int, prevScore: Int?, tier: ScoreTier
  ): DimensionDiagnostic {
    val philosophy = "El movimiento no es una penitencia por lo que comiste ni un deber estético; es el homenaje que le rindes a tu único vehículo existencial."

    return when (tier) {
      ScoreTier.CRITICAL -> DimensionDiagnostic(
        dimension = dim,
        score = score,
        previousScore = prevScore,
        tier = tier,
        corePhilosophy = philosophy,
        diagnosisSummary = "Inmovilidad predominante. Tu cuerpo acumula tensión estática y tus mitocondrias pierden capacidad energética.",
        currentImpact = "Pesadez corporal matutina, dolores cervicales y lumbares, y una fatiga difusa que no se alivia durmiendo más.",
        blindSpots = listOf(
          "Pensar que si no haces 90 minutos de gimnasio intenso, no vale la pena moverse.",
          "Esperar 'tener ganas' para empezar: la motivación no precede al movimiento, la motivación es hija del movimiento.",
          "Ver el deporte como una obligación odiosa en vez de buscar disciplinas que alegren tu corazón."
        ),
        actionableImprovements = listOf(
          "Regla del micro-hábito: sal a caminar 15 minutos a paso ligero justo después de la comida principal.",
          "Tres pausas activas al día de 2 minutos para descomprimir la columna y activar la circulación.",
          "Elige bailar, nadar o andar en bicicleta: la mejor rutina es la que te saca una sonrisa."
        )
      )
      ScoreTier.DEVELOPING -> DimensionDiagnostic(
        dimension = dim,
        score = score,
        previousScore = prevScore,
        tier = tier,
        corePhilosophy = philosophy,
        diagnosisSummary = "Actividad espasmódica: semanas de entusiasmo riguroso seguidas de semanas de inercia sedentaria.",
        currentImpact = "Montaña rusa de energía; agujetas excesivas por retomar bruscamente y frustración por la falta de hábito arraigado.",
        blindSpots = listOf(
          "Abandonar toda la semana porque fallaste el martes.",
          "No proteger un horario sagrado en la agenda para moverte."
        ),
        actionableImprovements = listOf(
          "Fija un mínimo no negociable: 3 días por semana de 30 minutos, con la ropa deportiva preparada desde la noche anterior.",
          "La constancia humilde vence al heroísmo intermitente: 20 minutos todos los días superan a 2 horas una vez al mes.",
          "Mide tu progreso por cómo te sientes después (claridad, calma) y no solo por marcas estéticas."
        )
      )
      ScoreTier.SOLID -> DimensionDiagnostic(
        dimension = dim,
        score = score,
        previousScore = prevScore,
        tier = tier,
        corePhilosophy = philosophy,
        diagnosisSummary = "Hábito motriz integrado con alegría y disciplina. Tu fisiología responde con vigor y resiliencia.",
        currentImpact = "Digestión óptima, claridad mental sostenida durante el trabajo y excelente regulación del estrés.",
        blindSpots = listOf(
          "Caer en rutinas monótonas que ya no despiertan adaptaciones neurológicas.",
          "Descuidar el trabajo de movilidad y estiramientos profundos."
        ),
        actionableImprovements = listOf(
          "Introduce estímulos nuevos: combina fuerza funcional con intervalos cardiovasculares y sesiones de estiramiento consciente.",
          "Escucha a tus articulaciones con sabiduría para no confundir esfuerzo con desgaste lesivo."
        )
      )
      ScoreTier.EXCEPTIONAL -> DimensionDiagnostic(
        dimension = dim,
        score = score,
        previousScore = prevScore,
        tier = tier,
        corePhilosophy = philosophy,
        diagnosisSummary = "Vigor biológico excepcional. El movimiento es parte natural e innegociable de tu identidad.",
        currentImpact = "Postura erguida, energía inagotable, sistema inmune robusto y serenidad hormonal.",
        blindSpots = listOf(
          "Riesgo de sobreentrenamiento si no se acompaña de descanso regenerativo.",
          "Rigidez ante viajes o imprevistos que alteren tu pauta habitual."
        ),
        actionableImprovements = listOf(
          "Planifica semanas de descarga activa para permitir que tendones y sistema nervioso se regeneren.",
          "Acompaña a otros a descubrir el placer de habitar un cuerpo ágil y vital."
        )
      )
    }
  }

  private fun buildDescansoDiagnostic(
    dim: LifeDimension, score: Int, prevScore: Int?, tier: ScoreTier
  ): DimensionDiagnostic {
    val philosophy = "El sueño es el único momento en que el alma se repara y el cerebro limpia sus toxinas. No descansar es hipotecar el mañana a intereses de usura."

    return when (tier) {
      ScoreTier.CRITICAL -> DimensionDiagnostic(
        dimension = dim,
        score = score,
        previousScore = prevScore,
        tier = tier,
        corePhilosophy = philosophy,
        diagnosisSummary = "Desregulación circadiana severa. Vives con una deuda de sueño que nubla tu juicio y tu equilibrio anímico.",
        currentImpact = "Niebla mental matutina, irritabilidad desproporcionada, taquicardia por estrés y antojos compulsivos de café y azúcar.",
        blindSpots = listOf(
          "Llevarte el teléfono a la cama hasta quedar noqueada por agotamiento óptico.",
          "Creer que puedes maltratar tu descanso de lunes a viernes y 'arreglarlo' durmiendo 11 horas el domingo.",
          "Consumir cafeína pasadas las 14:00 horas ignorando su vida media de 6 a 8 horas en sangre."
        ),
        actionableImprovements = listOf(
          "Crea un santuario de descanso: retira pantallas de tu campo visual 45 minutos antes de acostarte.",
          "Fija una hora innegociable de despertar con luz natural (+/- 30 minutos) todos los días de la semana.",
          "Cena ligero al menos 2 horas antes de dormir para que tu cuerpo no gaste su energía reparadora en digestiones pesadas."
        )
      )
      ScoreTier.DEVELOPING -> DimensionDiagnostic(
        dimension = dim,
        score = score,
        previousScore = prevScore,
        tier = tier,
        corePhilosophy = philosophy,
        diagnosisSummary = "Duermes horas suficientes en el papel, pero la calidad profunda es deficiente y despiertas con fatiga residual.",
        currentImpact = "Bajas de energía hacia las 16:00 y dificultad para desconectar la rumiación de pendientes antes de dormir.",
        blindSpots = listOf(
          "No realizar un cierre formal de tu jornada laboral.",
          "Confundir descansar con mirar pantallas o redes sociales (eso estimula la dopamina, no relaja el sistema parasimpático)."
        ),
        actionableImprovements = listOf(
          "Aplica un 'cierre de jornada': vacía en una libreta tus 3 pendientes clave para sacarlos de tu cabeza antes de cenar.",
          "Prueba respiración 4-7-8 o una ducha templada antes de entrar a la cama.",
          "Bloquea al menos 2 micro-pausas diurnas de 3 minutos para mirar al horizonte y soltar la mandíbula."
        )
      )
      ScoreTier.SOLID -> DimensionDiagnostic(
        dimension = dim,
        score = score,
        previousScore = prevScore,
        tier = tier,
        corePhilosophy = philosophy,
        diagnosisSummary = "Higiene de sueño madura y capacidad de desconexión sin culpa. Tu mente amanece despejada.",
        currentImpact = "Lucidez constante, buena memoria de trabajo y un estado de ánimo templado frente a crisis cotidianas.",
        blindSpots = listOf(
          "Ceder horas de sueño con excesiva facilidad cuando surgen urgencias laborales.",
          "No cuidar la hidratación celular al despertar."
        ),
        actionableImprovements = listOf(
          "Bebe un vaso de agua templada antes de tu primer café de la mañana.",
          "Introduce fines de semana con desconexión digital planificada para regenerar tu atención profunda."
        )
      )
      ScoreTier.EXCEPTIONAL -> DimensionDiagnostic(
        dimension = dim,
        score = score,
        previousScore = prevScore,
        tier = tier,
        corePhilosophy = philosophy,
        diagnosisSummary = "Maestría en autorregulación y descanso reparador. Honras tus ritmos biológicos como un arte sagrado.",
        currentImpact = "Despiertas con entusiasmo fresco y tu sistema nervioso opera con un balance simpático/parasimpático envidiable.",
        blindSpots = listOf(
          "Frustración excesiva cuando un compromiso social inevitable altera tus horarios óptimos."
        ),
        actionableImprovements = listOf(
          "Integra prácticas de meditación profunda o baños de naturaleza para expandir aún más tu paz interna.",
          "Enseña con tu ejemplo a tu entorno que el descanso es la cúspide de la elegancia vital."
        )
      )
    }
  }

  private fun buildRelacionesDiagnostic(
    dim: LifeDimension, score: Int, prevScore: Int?, tier: ScoreTier
  ): DimensionDiagnostic {
    val philosophy = "Somos seres de manada: la soledad funcional marchita el alma. Una conversación auténtica tiene más poder curativo que mil lecturas teóricas."

    return when (tier) {
      ScoreTier.CRITICAL -> DimensionDiagnostic(
        dimension = dim,
        score = score,
        previousScore = prevScore,
        tier = tier,
        corePhilosophy = philosophy,
        diagnosisSummary = "Aislamiento emocional o desconexión en tu entorno inmediato. Guardas tus cargas en soledad.",
        currentImpact = "Falta de un refugio seguro donde ser tú misma sin filtros ni máscaras; sensación silenciosa de incomprensión.",
        blindSpots = listOf(
          "Creer que a nadie le importas o que 'molestas' si compartes tu sentir.",
          "Sustituir la intimidad cara a cara por interacciones superficiales en pantallas.",
          "Confundir autosuficiencia con fortaleza: no necesitar a nadie es a menudo un trauma de apego no resuelto."
        ),
        actionableImprovements = listOf(
          "Llama esta semana a una persona que valores y dile con franqueza: 'me acordé de ti y quería escuchar tu voz'.",
          "Cuando alguien te pregunte cómo estás, atrévete a responder con verdad en lugar del automático 'bien, con trabajo'.",
          "Únete a un grupo, club de lectura o taller con intereses compartidos para sembrar nuevos afectos."
        )
      )
      ScoreTier.DEVELOPING -> DimensionDiagnostic(
        dimension = dim,
        score = score,
        previousScore = prevScore,
        tier = tier,
        corePhilosophy = philosophy,
        diagnosisSummary = "Mantienes contactos frecuentes pero la profundidad y la presencia son escasas por culpa de la prisa.",
        currentImpact = "Sensación de vínculos desbalanceados donde escuchas mucho pero eres poco escuchada; relaciones mantenidas por compromiso.",
        blindSpots = listOf(
          "Estar físicamente en una mesa mientras tu atención sigue enganchada a notificaciones del teléfono.",
          "Sostener amistades tóxicas por nostalgia o culpa que drenan tu tranquilidad."
        ),
        actionableImprovements = listOf(
          "Cenas y cafés con el teléfono guardado en el bolso: la presencia indivisa es el regalo más valioso de este siglo.",
          "Haz una pregunta profunda a un amigo: '¿qué es lo que más te ilusiona o te preocupa en este momento de tu vida?' y escucha sin juzgar.",
          "Aprende a distanciarte de vínculos extractivos que solo aparecen cuando necesitan algo de ti."
        )
      )
      ScoreTier.SOLID -> DimensionDiagnostic(
        dimension = dim,
        score = score,
        previousScore = prevScore,
        tier = tier,
        corePhilosophy = philosophy,
        diagnosisSummary = "Círculo afectivo nutritivo, sólido y recíproco. Tienes dónde reír y dónde llorar con seguridad.",
        currentImpact = "Respaldo emocional genuino en momentos de crisis y alegría compartida en tus victorias.",
        blindSpots = listOf(
          "Descuidar amistades queridas por la inercia devoradora de la rutina laboral."
        ),
        actionableImprovements = listOf(
          "Envía un mensaje de gratitud espontánea a alguien que haya sido un pilar en tu historia.",
          "Establece tradiciones fijas (como un almuerzo mensual) para que los encuentros no dependan del azar de las agendas."
        )
      )
      ScoreTier.EXCEPTIONAL -> DimensionDiagnostic(
        dimension = dim,
        score = score,
        previousScore = prevScore,
        tier = tier,
        corePhilosophy = philosophy,
        diagnosisSummary = "Vínculos luminosos, maduros, libres y profundamente enriquecedores. Transmites pertenencia y calor.",
        currentImpact = "Tus relaciones son tu mayor fuente de inspiración y disfrute; sabes poner límites con amor y abrir tu corazón con valentía.",
        blindSpots = listOf(
          "Asumir responsabilidades emocionales de terceros que no te corresponden solucionar.",
          "No reservar suficiente espacio de intimidad serena contigo misma."
        ),
        actionableImprovements = listOf(
          "Conecta a personas afines de tu círculo para tejer comunidad y apoyo mutuo.",
          "Protege tus ratos de soledad elegida como un oasis para recargar tu propia esencia."
        )
      )
    }
  }

  private fun buildHobbiesDiagnostic(
    dim: LifeDimension, score: Int, prevScore: Int?, tier: ScoreTier
  ): DimensionDiagnostic {
    val philosophy = "El juego libre es el único refugio donde el alma no rinde cuentas a nadie. Si todo lo que haces debe ser productivo, tu vida es una fábrica sin ventanas."

    return when (tier) {
      ScoreTier.CRITICAL -> DimensionDiagnostic(
        dimension = dim,
        score = score,
        previousScore = prevScore,
        tier = tier,
        corePhilosophy = philosophy,
        diagnosisSummary = "La creatividad desinteresada ha sido asfixiada por el utilitarismo constante. Te cuesta disfrutar sin culpa.",
        currentImpact = "Fines de semana que se sienten como un ensayo de más obligaciones; pérdida de curiosidad y sensación de envejecer antes de tiempo.",
        blindSpots = listOf(
          "Pensar que si un hobby no genera dinero o prestigio, es una 'pérdida de tiempo'.",
          "Abandonar una pasión si no eres excepcionalmente brillante en ella: se juega por jugar, no para ganar medallas.",
          "Creer que descansar es no hacer nada tirada mirando el móvil."
        ),
        actionableImprovements = listOf(
          "Reserva 45 minutos intocables el sábado o domingo para pintar, tocar música, cocinar algo nuevo o leer ficción.",
          "Recupera la mentalidad de principiante: permítete hacer cosas torpes y disfrutarlas con ternura.",
          "Pregúntate qué amabas hacer cuando tenías 10 años y rescata una versión contemporánea de ese juego este mes."
        )
      )
      ScoreTier.DEVELOPING -> DimensionDiagnostic(
        dimension = dim,
        score = score,
        previousScore = prevScore,
        tier = tier,
        corePhilosophy = philosophy,
        diagnosisSummary = "Practicas tus pasiones solo cuando 'sobra tiempo', lo que en la vida adulta equivale a decir casi nunca.",
        currentImpact = "Nostalgia por talentos dormidos y admiración lejana hacia personas que cultivan proyectos creativos propios.",
        blindSpots = listOf(
          "Esperar tener un bloque libre de 4 horas seguidas para empezar una actividad creativa.",
          "Consumir contenido de hobbies ajenos en YouTube o TikTok en vez de ensuciarte las manos con el tuyo."
        ),
        actionableImprovements = listOf(
          "El poder de los 20 minutos: 20 minutos de guitarra, escritura o costura cambian por completo la química de tu día.",
          "Deja los instrumentos o materiales visibles y listos en casa para reducir a cero la fricción de arranque.",
          "No intentes monetizar tu hobby: protégelo como tu santuario libre de métricas comerciales."
        )
      )
      ScoreTier.SOLID -> DimensionDiagnostic(
        dimension = dim,
        score = score,
        previousScore = prevScore,
        tier = tier,
        corePhilosophy = philosophy,
        diagnosisSummary = "Tienes un espacio sagrado para el gozo personal y la curiosidad. Tu identidad es rica y polifacética.",
        currentImpact = "Mente flexible, alta capacidad para resolver problemas complejos y alegría genuina en tus días libres.",
        blindSpots = listOf(
          "La tentación constante de transformar tu pasión en un negocio secundario."
        ),
        actionableImprovements = listOf(
          "Explora una disciplina complementaria que te saque de tu zona de confort estética o manual.",
          "Comparte tus creaciones con personas queridas como un acto de cariño, no como un examen de calidad."
        )
      )
      ScoreTier.EXCEPTIONAL -> DimensionDiagnostic(
        dimension = dim,
        score = score,
        previousScore = prevScore,
        tier = tier,
        corePhilosophy = philosophy,
        diagnosisSummary = "Fecundidad creativa extraordinaria. Vives conectada con el entusiasmo puro y el asombro infantil.",
        currentImpact = "Tu entusiasmo es magnético; tienes una vida interior fascinante que no depende de títulos laborales para brillar.",
        blindSpots = listOf(
          "Dificultad ocasional para aterrizar en deberes pragmáticos cuando la inspiración te rapta."
        ),
        actionableImprovements = listOf(
          "Organiza encuentros creativos donde invites a otros a desinhibirse y crear sin miedo al juicio.",
          "Lleva un cuaderno de notas y bocetos donde atesores tus chispas de inspiración cotidiana."
        )
      )
    }
  }

  private fun buildExcelenciaDiagnostic(
    dim: LifeDimension, score: Int, prevScore: Int?, tier: ScoreTier
  ): DimensionDiagnostic {
    val philosophy = "La excelencia es el amor puesto en los detalles que nadie verá. El perfeccionismo nace del miedo; la excelencia nace de la devoción."

    return when (tier) {
      ScoreTier.CRITICAL -> DimensionDiagnostic(
        dimension = dim,
        score = score,
        previousScore = prevScore,
        tier = tier,
        corePhilosophy = philosophy,
        diagnosisSummary = "Desconexión con tus proyectos. Vives en cumplimiento pasivo, desmotivación o apatía operativa.",
        currentImpact = "Haces las cosas por salir del paso; no sientes orgullo al contemplar lo que hiciste y el aburrimiento te invade.",
        blindSpots = listOf(
          "Pensar que dar lo mejor de ti no tiene sentido porque 'nadie lo nota ni lo agradece'.",
          "Permanecer en entornos mediocres que adormecen tu ambición sana.",
          "Castigarte con cinismo para no admitir que extrañas sentir entusiasmo por tus obras."
        ),
        actionableImprovements = listOf(
          "Elige una sola tarea pequeña hoy y realízala con un cuidado exquisito solo por respeto hacia ti misma.",
          "Identifica qué aspecto de tu labor actual conecta con tu verdadera vocación y dale prioridad consciente.",
          "Fija un reto personal desafiante a 30 días que encienda de nuevo tu chispa de superación."
        )
      )
      ScoreTier.DEVELOPING -> DimensionDiagnostic(
        dimension = dim,
        score = score,
        previousScore = prevScore,
        tier = tier,
        corePhilosophy = philosophy,
        diagnosisSummary = "Eres solvente y cumples con eficacia, pero sabes en el fondo que estás operando al 60% de tu grandeza potencial.",
        currentImpact = "Zona de confort cómoda pero estéril; una sensación subterránea de que tus mejores talentos están guardados en un cajón.",
        blindSpots = listOf(
          "Quedarte en el estándar 'suficiente' para no llamar la atención ni asumir mayor responsabilidad.",
          "Dejarte devorar por lo urgente y posponer eternamente los proyectos que de verdad te importan."
        ),
        actionableImprovements = listOf(
          "La regla del 10% adicional: en tu próxima entrega, añade un refinamiento extra que nadie pidió pero que eleve la obra.",
          "Estudia a los maestros de tu disciplina: desmenuza con lupa qué hábitos hacen impecable su trabajo.",
          "Pide retroalimentación honesta y sin edulcorar a alguien de tu máxima confianza profesional."
        )
      )
      ScoreTier.SOLID -> DimensionDiagnostic(
        dimension = dim,
        score = score,
        previousScore = prevScore,
        tier = tier,
        corePhilosophy = philosophy,
        diagnosisSummary = "Compromiso ejemplar con el rigor, orgullo profesional y pasión auténtica por el buen hacer.",
        currentImpact = "Generas confianza instantánea en colaboradores y clientes; tu palabra tiene peso y tus obras hablan por ti.",
        blindSpots = listOf(
          "Dificultad para soltar el control o delegar tareas por temor a que bajen el estándar.",
          "Confundir excelencia con perfeccionismo paralizante en la recta final de un proyecto."
        ),
        actionableImprovements = listOf(
          "Aprende el arte de declarar un proyecto 'terminado': una obra buena entregada a tiempo vale más que una obra perfecta en un cajón.",
          "Dedica tiempo a mentorear a otros para que el estándar colectivo suba sin que tengas que hacerlo todo tú."
        )
      )
      ScoreTier.EXCEPTIONAL -> DimensionDiagnostic(
        dimension = dim,
        score = score,
        previousScore = prevScore,
        tier = tier,
        corePhilosophy = philosophy,
        diagnosisSummary = "Maestría consagrada. Tu entrega trasciende la mera técnica para convertirse en arte y vocación de servicio.",
        currentImpact = "Marcas el norte ético y profesional de tu entorno; tus creaciones inspiran y transforman a quienes las reciben.",
        blindSpots = listOf(
          "Impaciencia o frustración ante personas con menor grado de compromiso.",
          "Desatender tu salud o afectos en nombre de la obra monumental."
        ),
        actionableImprovements = listOf(
          "Recuerda que la excelencia suprema incluye la compasión con el ritmo de los demás y la sabiduría de saber descansar.",
          "Canaliza tu maestría hacia proyectos formativos o comunitarios que dejen una huella que te sobreviva."
        )
      )
    }
  }
}
