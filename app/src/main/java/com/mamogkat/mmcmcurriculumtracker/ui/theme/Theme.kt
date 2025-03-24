package com.mamogkat.mmcmcurriculumtracker.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.colorResource
import androidx.core.view.WindowCompat
import com.mamogkat.mmcmcurriculumtracker.R

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun MMCMCurriculumTrackerTheme(
    content: @Composable () -> Unit
) {
    val forcedLightColorScheme = lightColorScheme(
        primary = colorResource(R.color.mmcm_blue),       // mmcm_blue (Main accent color)
        onPrimary = colorResource(R.color.mmcm_white),           // Text/icons on primary should be white for contrast
        secondary = colorResource(R.color.mmcm_red),      // mmcm_red (For secondary elements)
        onSecondary = colorResource(R.color.mmcm_white),         // Ensure readability on red backgrounds
        background = colorResource(R.color.mmcm_white),          // mmcm_white (Forced white background)
        onBackground = colorResource(R.color.mmcm_black),        // mmcm_black (Black text for contrast)
        surface = colorResource(R.color.mmcm_white),        // mmcm_silver (For cards, surfaces)
        onSurface = colorResource(R.color.mmcm_black),           // Black text/icons on surfaces
        error = colorResource(R.color.mmcm_red),         // mmcm_orange (For warnings, errors)
        onError = colorResource(R.color.mmcm_white)              // Text/icons on error should be white
    )

    MaterialTheme(
        colorScheme = forcedLightColorScheme, // Always apply forced light colors
        typography = Typography,  // Use default or custom typography
        content = content
    )
}