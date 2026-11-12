package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AssessmentReport
import com.example.model.DimensionsProvider
import com.example.ui.theme.AccentCharcoal
import com.example.ui.theme.BorderLight
import com.example.ui.theme.SurfaceCardLight
import com.example.ui.theme.SurfaceVariantLight
import com.example.ui.theme.TextPrimaryLight
import com.example.ui.theme.TextSecondaryLight

/**
 * Diálogo modal con el detalle y diagnóstico psicológico de una dimensión del hexágono.
 * Se abre al tocar un vértice o chip en el hexágono de dos capas.
 */
@Composable
fun DimensionDetailDialog(
  dimensionIndex: Int,
  report: AssessmentReport,
  realScore: Int = 0,
  onDismiss: () -> Unit
) {
  val dimension = DimensionsProvider.dimensions.getOrNull(dimensionIndex) ?: return
  val diagnostic = report.diagnostics.find { it.dimension.index == dimensionIndex }
  val perceivedScore = report.scores[dimensionIndex] ?: 5

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(24.dp),
    containerColor = Color.White,
    titleContentColor = TextPrimaryLight,
    textContentColor = TextSecondaryLight,
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(14.dp)
              .clip(CircleShape)
              .background(dimension.accentColor)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = dimension.title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = TextPrimaryLight
          )
        }

        IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
          Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextSecondaryLight)
        }
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
      ) {
        // Tarjeta de puntajes comparados
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = SurfaceVariantLight,
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                text = "PERCIBIDO",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = TextSecondaryLight
              )
              Text(
                text = "$perceivedScore / 10",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimaryLight
              )
              Text(
                text = "En tu cuestionario",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondaryLight
              )
            }

            Box(
              modifier = Modifier
                .width(1.dp)
                .height(36.dp)
                .background(BorderLight)
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                text = "REAL (14 DÍAS)",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = TextSecondaryLight
              )
              Text(
                text = "$realScore / 10",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = if (realScore > 0) AccentCharcoal else TextSecondaryLight
              )
              Text(
                text = if (realScore > 0) "Comprobado en hechos" else "Empieza hoy",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondaryLight
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Diagnóstico descriptivo
        if (diagnostic != null) {
          Text(
            text = diagnostic.diagnosisSummary,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = TextPrimaryLight
          )

          Spacer(modifier = Modifier.height(6.dp))

          Text(
            text = diagnostic.currentImpact,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondaryLight,
            lineHeight = 20.sp
          )

          Spacer(modifier = Modifier.height(14.dp))

          // Consejos clave de crecimiento
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = SurfaceCardLight,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Outlined.Lightbulb,
                  contentDescription = null,
                  tint = dimension.accentColor,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "Acción clave recomendada",
                  style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                  color = TextPrimaryLight
                )
              }
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = diagnostic.actionableImprovements.firstOrNull() ?: diagnostic.corePhilosophy,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondaryLight,
                lineHeight = 18.sp
              )
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = onDismiss,
        colors = ButtonDefaults.buttonColors(containerColor = AccentCharcoal),
        shape = RoundedCornerShape(14.dp)
      ) {
        Text("Entendido", color = Color.White)
      }
    }
  )
}
