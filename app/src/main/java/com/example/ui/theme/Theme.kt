package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Immutable
data class StudioExtendedColors(
    val deskBackground: Color,
    val deskDots: Color,
    val paperWhite: Color,
    val chromeSurface: Color,
    val chromeMuted: Color,
    val chromeBorder: Color,
    val inkPrimary: Color,
    val inkMuted: Color,
    val inkSubtle: Color,
    val orangeAccent: Color,
    val orangeSoft: Color,
    val roseAccent: Color,
    val amberBg: Color,
    val amberText: Color,
    val amberBorder: Color,
    val kidLockActive: Color,
    val palmLockActive: Color,
    val emeraldSave: Color,
    val emeraldBadge: Color,
    val dangerText: Color,
    val dangerBg: Color
)

val LocalStudioColors = staticCompositionLocalOf {
    StudioExtendedColors(
        deskBackground = StudioDeskBg,
        deskDots = StudioDeskDot,
        paperWhite = StudioPaperWhite,
        chromeSurface = StudioSurface,
        chromeMuted = StudioSurfaceMuted,
        chromeBorder = StudioBorder,
        inkPrimary = StudioInk,
        inkMuted = StudioInkMuted,
        inkSubtle = StudioInkSubtle,
        orangeAccent = StudioOrange,
        orangeSoft = StudioOrangeSoft,
        roseAccent = StudioRose,
        amberBg = StudioAmberBg,
        amberText = StudioAmberText,
        amberBorder = StudioAmberBorder,
        kidLockActive = StudioAmber,
        palmLockActive = StudioPalmBlue,
        emeraldSave = StudioEmerald,
        emeraldBadge = StudioEmeraldBadge,
        dangerText = StudioDangerRed,
        dangerBg = StudioDangerSoft
    )
}

private val ChromaLightColorScheme = lightColorScheme(
    primary = StudioOrange,
    onPrimary = Color.White,
    primaryContainer = StudioOrangeSoft,
    onPrimaryContainer = StudioOrangePressed,
    secondary = StudioInk,
    onSecondary = Color.White,
    secondaryContainer = StudioSurfaceMuted,
    onSecondaryContainer = StudioInk,
    tertiary = StudioEmerald,
    onTertiary = Color.White,
    background = StudioBackgroundWarm,
    onBackground = StudioInk,
    surface = StudioSurface,
    onSurface = StudioInk,
    surfaceVariant = StudioSurfaceMuted,
    onSurfaceVariant = StudioInkMuted,
    outline = StudioBorder,
    outlineVariant = StudioBorderStrong,
    error = StudioDangerRed,
    onError = Color.White,
    errorContainer = StudioDangerSoft,
    onErrorContainer = StudioDangerRed
)

val ChromaShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun ChromaStudioTheme(
    content: @Composable () -> Unit
) {
    val extendedColors = StudioExtendedColors(
        deskBackground = StudioDeskBg,
        deskDots = StudioDeskDot,
        paperWhite = StudioPaperWhite,
        chromeSurface = StudioSurface,
        chromeMuted = StudioSurfaceMuted,
        chromeBorder = StudioBorder,
        inkPrimary = StudioInk,
        inkMuted = StudioInkMuted,
        inkSubtle = StudioInkSubtle,
        orangeAccent = StudioOrange,
        orangeSoft = StudioOrangeSoft,
        roseAccent = StudioRose,
        amberBg = StudioAmberBg,
        amberText = StudioAmberText,
        amberBorder = StudioAmberBorder,
        kidLockActive = StudioAmber,
        palmLockActive = StudioPalmBlue,
        emeraldSave = StudioEmerald,
        emeraldBadge = StudioEmeraldBadge,
        dangerText = StudioDangerRed,
        dangerBg = StudioDangerSoft
    )

    androidx.compose.runtime.CompositionLocalProvider(
        LocalStudioColors provides extendedColors
    ) {
        MaterialTheme(
            colorScheme = ChromaLightColorScheme,
            typography = ChromaTypography,
            shapes = ChromaShapes,
            content = content
        )
    }
}
