package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val AssiSTILightColorScheme = lightColorScheme(
  primary = RoyalNavy,
  onPrimary = PureWhite,
  primaryContainer = CampusBlue,
  onPrimaryContainer = RoyalNavy,
  secondary = CampusBlue,
  onSecondary = PureWhite,
  secondaryContainer = SkyCircleBlue,
  onSecondaryContainer = InkDark,
  tertiary = StiGold,
  onTertiary = RoyalNavy,
  background = SoftIceBg,
  onBackground = InkDark,
  surface = PureWhite,
  onSurface = InkDark,
  surfaceVariant = SoftCardBg,
  onSurfaceVariant = MutedInk,
  error = ErrorRed,
  onError = PureWhite,
  outline = RoyalNavy,
)

@Composable
fun AssiSTITheme(content: @Composable () -> Unit) {
  MaterialTheme(
    colorScheme = AssiSTILightColorScheme,
    typography = Typography,
    content = content,
  )
}
