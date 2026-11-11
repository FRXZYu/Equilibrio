package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
  primary = AccentCharcoal,
  onPrimary = SurfaceCardLight,
  primaryContainer = SurfaceVariantLight,
  onPrimaryContainer = TextPrimaryLight,
  secondary = TextSecondaryLight,
  onSecondary = SurfaceCardLight,
  background = CanvasBackgroundLight,
  onBackground = TextPrimaryLight,
  surface = SurfaceCardLight,
  onSurface = TextPrimaryLight,
  surfaceVariant = SurfaceVariantLight,
  onSurfaceVariant = TextSecondaryLight,
  outline = BorderLight
)

private val DarkColorScheme = darkColorScheme(
  primary = TextPrimaryDark,
  onPrimary = CanvasBackgroundDark,
  primaryContainer = SurfaceVariantDark,
  onPrimaryContainer = TextPrimaryDark,
  secondary = TextSecondaryDark,
  onSecondary = CanvasBackgroundDark,
  background = CanvasBackgroundDark,
  onBackground = TextPrimaryDark,
  surface = SurfaceCardDark,
  onSurface = TextPrimaryDark,
  surfaceVariant = SurfaceVariantDark,
  onSurfaceVariant = TextSecondaryDark,
  outline = BorderDark
)

val AppShapes = Shapes(
  small = RoundedCornerShape(12.dp),
  medium = RoundedCornerShape(18.dp),
  large = RoundedCornerShape(26.dp),
  extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Keep clean minimal palette consistent
  content: @Composable () -> Unit,
) {
  val colorScheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
      val context = LocalContext.current
      if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    darkTheme -> DarkColorScheme
    else -> LightColorScheme
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    shapes = AppShapes,
    content = content
  )
}
