package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun rememberBounceInteractionSource(): MutableInteractionSource = remember {
  MutableInteractionSource()
}

/**
 * Aplica únicamente la animación de escala y rebote al presionar usando un InteractionSource compartido.
 * Úsalo en Material 3 Button / OutlinedButton / Surface(onClick = ...) pasando el mismo interactionSource
 * para evitar dobles clics y conservar el ripple nativo de Material Design.
 */
fun Modifier.bouncyScale(
  interactionSource: InteractionSource,
  scaleDown: Float = 0.94f
): Modifier = composed {
  val isPressed by interactionSource.collectIsPressedAsState()

  val scale by animateFloatAsState(
    targetValue = if (isPressed) scaleDown else 1f,
    animationSpec = spring(
      dampingRatio = Spring.DampingRatioMediumBouncy,
      stiffness = Spring.StiffnessMediumLow
    ),
    label = "bouncy_scale_shared"
  )

  this.graphicsLayer {
    scaleX = scale
    scaleY = scale
  }
}

/**
 * Modificador táctil con animación de rebote Material 3 para tarjetas o superficies sin onClick propio.
 */
fun Modifier.bouncyClick(
  scaleDown: Float = 0.94f,
  onClick: (() -> Unit)? = null
): Modifier = composed {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()

  val scale by animateFloatAsState(
    targetValue = if (isPressed) scaleDown else 1f,
    animationSpec = spring(
      dampingRatio = Spring.DampingRatioMediumBouncy,
      stiffness = Spring.StiffnessMediumLow
    ),
    label = "bouncy_scale"
  )

  val baseModifier = this.graphicsLayer {
    scaleX = scale
    scaleY = scale
  }

  if (onClick != null) {
    baseModifier.clickable(
      interactionSource = interactionSource,
      indication = null,
      onClick = onClick
    )
  } else {
    baseModifier
  }
}
