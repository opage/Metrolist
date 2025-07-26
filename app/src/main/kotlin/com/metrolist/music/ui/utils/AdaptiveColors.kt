package com.metrolist.music.ui.utils

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils

/**
 * Calculates an adaptive text color based on the background color with improved dark mode support.
 * This ensures consistent color behavior across the app and better visibility in dark mode.
 * 
 * @param backgroundColor The background color to calculate contrast against
 * @return The appropriate text color (Color.White or Color.Black)
 */
@Composable
fun adaptiveTextColor(backgroundColor: Color): Color {
    // Handle translucent/transparent backgrounds by using a fallback approach
    val effectiveBackground = if (backgroundColor.alpha < 0.01f) {
        // For fully transparent backgrounds, use the surface color as fallback
        MaterialTheme.colorScheme.surface
    } else if (backgroundColor.alpha < 1.0f) {
        // For translucent backgrounds, blend with surface color to get effective color
        val surface = MaterialTheme.colorScheme.surface
        Color(
            red = backgroundColor.red * backgroundColor.alpha + surface.red * (1f - backgroundColor.alpha),
            green = backgroundColor.green * backgroundColor.alpha + surface.green * (1f - backgroundColor.alpha),
            blue = backgroundColor.blue * backgroundColor.alpha + surface.blue * (1f - backgroundColor.alpha),
            alpha = 1.0f
        )
    } else {
        backgroundColor
    }
    
    val backgroundArgb = effectiveBackground.toArgb()
    
    // Check if we're in dark mode
    val isDarkMode = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    
    // Use the same logic as Player.kt for consistent behavior
    val whiteContrast = ColorUtils.calculateContrast(backgroundArgb, Color.White.toArgb())
    val blackContrast = ColorUtils.calculateContrast(backgroundArgb, Color.Black.toArgb())
    
    // In dark mode, prefer white text for better visibility on light backgrounds
    return if (isDarkMode) {
        if (whiteContrast >= 2f) {
            Color.White
        } else if (blackContrast >= 2f) {
            Color.Black
        } else {
            // Default to white in dark mode for better visibility
            Color.White
        }
    } else {
        // In light mode, use original logic
        if (whiteContrast < 2f && blackContrast > 2f) {
            Color.Black
        } else if (whiteContrast > 2f && blackContrast < 2f) {
            Color.White
        } else {
            // Default fallback - use black for better visibility in light mode
            Color.Black
        }
    }
}

/**
 * Legacy compatibility function for adaptiveTextColor with custom colors
 * This maintains backward compatibility while using the new Player.kt logic
 */
@Composable
fun adaptiveTextColor(
    backgroundColor: Color,
    lightColor: Color,
    darkColor: Color,
    contrastThreshold: Double = 2.0
): Color {
    val baseColor = adaptiveTextColor(backgroundColor)
    return if (baseColor == Color.Black) darkColor else lightColor
}

/**
 * Calculates adaptive colors for topbar content based on the background color.
 * Optimized for both light and dark modes with proper Material Design 3 integration.
 * 
 * @param backgroundColor The topbar background color
 * @return AdaptiveTopBarColors containing all necessary colors for topbar content
 */
@Composable
fun adaptiveTopBarColors(backgroundColor: Color): AdaptiveTopBarColors {
    // Handle edge case where backgroundColor is fully transparent
    if (backgroundColor.alpha < 0.01f) {
        // Return default colors for transparent backgrounds
        return AdaptiveTopBarColors(
            titleColor = MaterialTheme.colorScheme.onSurface,
            subtitleColor = MaterialTheme.colorScheme.onSurfaceVariant,
            iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            actionColor = MaterialTheme.colorScheme.primary
        )
    }
    
    // Get the base adaptive color (White or Black) using Player.kt logic
    val baseTextColor = adaptiveTextColor(backgroundColor)
    
    // Check if we're in dark mode
    val isDarkMode = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    
    // Create Material 3 compatible colors based on the base color and theme
    val isDarkText = baseTextColor == Color.Black
    
    val titleColor = if (isDarkText) {
        // For dark text on light background
        if (isDarkMode) {
            // In dark mode on light background, use black for better visibility
            Color.Black
        } else {
            MaterialTheme.colorScheme.onSurface
        }
    } else {
        // For light text on dark background
        if (isDarkMode) {
            // In dark mode, use pure white for better visibility
            Color.White
        } else {
            // In light mode, use slightly dimmed white
            Color.White.copy(alpha = 0.95f)
        }
    }
    
    val subtitleColor = if (isDarkText) {
        // For dark text
        if (isDarkMode) {
            // In dark mode on light background, use black for better visibility
            Color.Black.copy(alpha = 0.8f)
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        }
    } else {
        // For light text
        if (isDarkMode) {
            // In dark mode, use slightly dimmed white
            Color.White.copy(alpha = 0.85f)
        } else {
            // In light mode, use more dimmed white
            Color.White.copy(alpha = 0.8f)
        }
    }
    
    val iconColor = if (isDarkText) {
        // For dark icons
        if (isDarkMode) {
            // In dark mode on light background, use black for better visibility
            Color.Black
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        }
    } else {
        // For light icons
        if (isDarkMode) {
            // In dark mode, use pure white
            Color.White
        } else {
            // In light mode, use slightly dimmed white
            Color.White.copy(alpha = 0.9f)
        }
    }
    
    val actionColor = if (isDarkText) {
        // For dark text
        if (isDarkMode) {
            // In dark mode on light background, use primary for better visibility
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.primary
        }
    } else {
        // For light text
        if (isDarkMode) {
            // In dark mode, use primary container for better contrast
            MaterialTheme.colorScheme.primaryContainer
        } else {
            // In light mode, use primary
            MaterialTheme.colorScheme.primary
        }
    }
    
    return AdaptiveTopBarColors(
        titleColor = titleColor,
        subtitleColor = subtitleColor,
        iconColor = iconColor,
        actionColor = actionColor
    )
}

/**
 * Data class containing adaptive colors for topbar content
 */
data class AdaptiveTopBarColors(
    val titleColor: Color,
    val subtitleColor: Color,
    val iconColor: Color,
    val actionColor: Color
)

/**
 * Extension function to check if a color is considered "light"
 */
fun Color.isLight(): Boolean = luminance() > 0.5f

/**
 * Extension function to check if a color is considered "dark"  
 */
fun Color.isDark(): Boolean = luminance() <= 0.5f

/**
 * Extension function to get a contrasting color (black or white) based on luminance
 */
fun Color.contrastingColor(): Color = if (isLight()) Color.Black else Color.White