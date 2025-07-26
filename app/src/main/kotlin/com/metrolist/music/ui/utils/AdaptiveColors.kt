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
    // Check if we're in dark mode
    val isDarkMode = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    
    // Handle edge case where backgroundColor is fully transparent or nearly transparent
    if (backgroundColor.alpha < 0.3f) {
        // Return default colors for transparent/very translucent backgrounds
        return AdaptiveTopBarColors(
            titleColor = MaterialTheme.colorScheme.onSurface,
            subtitleColor = MaterialTheme.colorScheme.onSurfaceVariant,
            iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            actionColor = MaterialTheme.colorScheme.primary
        )
    }
    
    // For semi-transparent backgrounds, blend with surface to get effective color
    val effectiveBackground = if (backgroundColor.alpha < 1.0f) {
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
    
    // Calculate luminance of the effective background
    val backgroundLuminance = effectiveBackground.luminance()
    val isLightBackground = backgroundLuminance > 0.5f
    
    // Calculate contrast ratios
    val backgroundArgb = effectiveBackground.toArgb()
    val whiteContrast = ColorUtils.calculateContrast(backgroundArgb, Color.White.toArgb())
    val blackContrast = ColorUtils.calculateContrast(backgroundArgb, Color.Black.toArgb())
    
    // Determine the best text color based on contrast and mode
    val shouldUseDarkText = if (isDarkMode) {
        // In dark mode, prefer dark text if background is very light and provides good contrast
        isLightBackground && blackContrast >= 4.5f
    } else {
        // In light mode, prefer dark text if it provides sufficient contrast
        blackContrast >= 3.0f
    }
    
    // Create adaptive colors based on the determined text style
    val (titleColor, subtitleColor, iconColor, actionColor) = if (shouldUseDarkText) {
        // Dark text style
        val titleColor = if (isDarkMode) {
            // In dark mode with light background, use pure black for maximum contrast
            Color.Black
        } else {
            // In light mode, use theme's onSurface or black based on contrast
            if (blackContrast >= 4.5f) Color.Black else MaterialTheme.colorScheme.onSurface
        }
        
        val subtitleColor = if (isDarkMode) {
            // In dark mode with light background, use semi-transparent black
            Color.Black.copy(alpha = 0.7f)
        } else {
            // In light mode, use theme's onSurfaceVariant or dimmed black
            if (blackContrast >= 3.0f) Color.Black.copy(alpha = 0.75f) else MaterialTheme.colorScheme.onSurfaceVariant
        }
        
        val iconColor = if (isDarkMode) {
            // In dark mode with light background, use black for icons
            Color.Black
        } else {
            // In light mode, use theme's color or black based on contrast
            if (blackContrast >= 3.0f) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
        }
        
        val actionColor = if (isDarkMode) {
            // In dark mode with light background, use primary that works on light
            MaterialTheme.colorScheme.primary
        } else {
            // In light mode, use primary
            MaterialTheme.colorScheme.primary
        }
        
        AdaptiveTopBarColors(titleColor, subtitleColor, iconColor, actionColor)
    } else {
        // Light text style
        val titleColor = if (isDarkMode) {
            // In dark mode with dark background, use pure white
            Color.White
        } else {
            // In light mode with dark background, use white with good contrast
            if (whiteContrast >= 4.5f) Color.White else Color.White.copy(alpha = 0.95f)
        }
        
        val subtitleColor = if (isDarkMode) {
            // In dark mode with dark background, use semi-transparent white
            Color.White.copy(alpha = 0.8f)
        } else {
            // In light mode with dark background, use dimmed white
            if (whiteContrast >= 3.0f) Color.White.copy(alpha = 0.85f) else Color.White.copy(alpha = 0.7f)
        }
        
        val iconColor = if (isDarkMode) {
            // In dark mode with dark background, use white for icons
            Color.White
        } else {
            // In light mode with dark background, use white with appropriate opacity
            if (whiteContrast >= 3.0f) Color.White else Color.White.copy(alpha = 0.9f)
        }
        
        val actionColor = if (isDarkMode) {
            // In dark mode with dark background, use primary container for better visibility
            MaterialTheme.colorScheme.primaryContainer
        } else {
            // In light mode with dark background, use primary or a lighter variant
            if (whiteContrast >= 3.0f) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer
        }
        
        AdaptiveTopBarColors(titleColor, subtitleColor, iconColor, actionColor)
    }
    
    return AdaptiveTopBarColors(titleColor, subtitleColor, iconColor, actionColor)
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