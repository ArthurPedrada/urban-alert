package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Identidade principal Urban Alert AI (Esmeralda Cívica + Baía Teal + Âmbar Solar)
val UrbanEmerald = Color(0xFF1E9E63)
val UrbanEmeraldDark = Color(0xFF126E45)
val UrbanEmeraldLight = Color(0xFF3FD08D)
val UrbanEmeraldContainer = Color(0xFFC5F2DC)
val OnUrbanEmeraldContainer = Color(0xFF052918)

val BaiaTeal = Color(0xFF0E7C9E)
val BaiaTealDark = Color(0xFF08546D)
val BaiaTealLight = Color(0xFF47C1E6)
val BaiaTealContainer = Color(0xFFCCEFFA)
val OnBaiaTealContainer = Color(0xFF052736)

val SolarAmber = Color(0xFFE08E16)
val SolarAmberLight = Color(0xFFF7B94B)
val SolarAmberContainer = Color(0xFFFFECC7)
val OnSolarAmberContainer = Color(0xFF3A2100)

val UrbanGraphite = Color(0xFF1A2529)
val UrbanGraphiteSoft = Color(0xFF3A4B52)
val UrbanGraphiteContainer = Color(0xFFD2E2E6)

// Superfícies cartográficas luminosas (claro e escuro)
val CartographicSage = Color(0xFFE5F0EC)
val CartographicSurfaceLight = Color(0xFFF7FBFA)
val CartographicSurfaceElevatedLight = Color(0xFFFFFFFF)
val CartographicGridLineLight = Color(0xFFC0D6CE)

val DarkGraphiteBackground = Color(0xFF0F181B)
val DarkGraphiteSurface = Color(0xFF172429)
val DarkGraphiteSurfaceElevated = Color(0xFF213137)
val CartographicGridLineDark = Color(0xFF2B3F47)
val DarkOnSurface = Color(0xFFEBF4F0)
val DarkOnSurfaceVariant = Color(0xFFB3C7C1)

// Tokens semânticos (sempre acompanhados de texto e ícone)
val SeverityLowColor = Color(0xFF1E9E63)
val SeverityMediumColor = Color(0xFFE08E16)
val SeverityHighColor = Color(0xFFE04848)
val SeverityCriticalColor = Color(0xFFA81927)

val StatusOpenColor = Color(0xFF0E7C9E)
val StatusUnderReviewColor = Color(0xFF9C6B14)
val StatusInProgressColor = Color(0xFF168399)
val StatusResolvedColor = Color(0xFF1E9E63)
val StatusRejectedColor = Color(0xFF5D6C72)

fun categoryAccentColor(slug: String): Color = when (slug) {
    "buracos-em-vias" -> Color(0xFFD96B27)
    "acumulo-de-lixo" -> Color(0xFF7A5AF8)
    "iluminacao-publica" -> Color(0xFFE09B16)
    "vazamentos" -> Color(0xFF0E86D4)
    "alagamentos" -> Color(0xFF0077B6)
    "problemas-de-drenagem" -> Color(0xFF179299)
    "sinalizacao-danificada" -> Color(0xFFD94862)
    "semaforos-quebrados" -> Color(0xFFC92A2A)
    "barreiras-de-acessibilidade" -> Color(0xFF1E9E63)
    else -> UrbanEmerald
}
