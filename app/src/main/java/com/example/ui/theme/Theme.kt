package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = UrbanEmeraldLight,
    onPrimary = Color(0xFF042113),
    primaryContainer = Color(0xFF104F35),
    onPrimaryContainer = UrbanEmeraldContainer,
    secondary = BaiaTealLight,
    onSecondary = Color(0xFF032330),
    secondaryContainer = Color(0xFF124659),
    onSecondaryContainer = BaiaTealContainer,
    tertiary = SolarAmberLight,
    onTertiary = Color(0xFF2C1800),
    tertiaryContainer = Color(0xFF4F3205),
    onTertiaryContainer = SolarAmberContainer,
    background = DarkGraphiteBackground,
    onBackground = DarkOnSurface,
    surface = DarkGraphiteSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkGraphiteSurfaceElevated,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = Color(0xFF425861),
    outlineVariant = CartographicGridLineDark,
    error = Color(0xFFFF7A7A),
    onError = Color(0xFF3E0004),
)

private val LightColorScheme = lightColorScheme(
    primary = UrbanEmerald,
    onPrimary = Color.White,
    primaryContainer = UrbanEmeraldContainer,
    onPrimaryContainer = OnUrbanEmeraldContainer,
    secondary = BaiaTeal,
    onSecondary = Color.White,
    secondaryContainer = BaiaTealContainer,
    onSecondaryContainer = OnBaiaTealContainer,
    tertiary = SolarAmber,
    onTertiary = Color.White,
    tertiaryContainer = SolarAmberContainer,
    onTertiaryContainer = OnSolarAmberContainer,
    background = CartographicSage,
    onBackground = UrbanGraphite,
    surface = CartographicSurfaceLight,
    onSurface = UrbanGraphite,
    surfaceVariant = CartographicSurfaceElevatedLight,
    onSurfaceVariant = UrbanGraphiteSoft,
    outline = Color(0xFF84A198),
    outlineVariant = CartographicGridLineLight,
    error = SeverityCriticalColor,
    onError = Color.White,
)

val UrbanAlertShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
)

@Composable
fun UrbanAlertTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = UrbanAlertShapes,
        content = content,
    )
}
