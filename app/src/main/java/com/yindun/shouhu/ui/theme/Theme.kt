package com.yindun.shouhu.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * 淡蓝色主题 - 银盾守护
 * 清新、柔和、护眼的淡蓝色设计
 */

// 浅色主题 - 淡蓝色
private val LightBlueColorScheme = lightColorScheme(
    primary = Primary,  // 天蓝色
    onPrimary = Color.White,
    primaryContainer = LightBlue100,
    onPrimaryContainer = PrimaryDark,
    secondary = Secondary,  // 绿色
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFC8E6C9),
    onSecondaryContainer = SecondaryVariant,
    tertiary = Color(0xFF7E57C2),  // 紫色
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD1C4E9),
    onTertiaryContainer = Color(0xFF4527A0),
    background = BackgroundLight,  // 淡蓝色背景
    onBackground = TextPrimary,
    surface = SurfaceLight,
    onSurface = TextPrimary,
    surfaceVariant = LightBlue50,
    onSurfaceVariant = TextSecondary,
    outline = LightBlue300,
    error = Danger,
    onError = Color.White,
    errorContainer = Color(0xFFFFCDD2),
    onErrorContainer = Color(0xFFB71C1C),
    inverseSurface = SurfaceDark,
    inverseOnSurface = Color(0xFFF5F5F5),
    inversePrimary = LightBlue300,
    surfaceTint = Primary
)

// 深色主题 - 深蓝色
private val DarkBlueColorScheme = darkColorScheme(
    primary = LightBlue300,  // 浅蓝色（深色模式下更亮）
    onPrimary = Color(0xFF003C8F),
    primaryContainer = PrimaryDark,
    onPrimaryContainer = LightBlue100,
    secondary = Color(0xFFA5D6A7),  // 浅绿色
    onSecondary = Color(0xFF1B5E20),
    secondaryContainer = SecondaryVariant,
    onSecondaryContainer = Color(0xFFC8E6C9),
    tertiary = Color(0xFFB39DDB),  // 浅紫色
    onTertiary = Color(0xFF311B92),
    tertiaryContainer = Color(0xFF4527A0),
    onTertiaryContainer = Color(0xFFD1C4E9),
    background = BackgroundDark,  // 深蓝色背景
    onBackground = Color(0xFFE8EAF6),
    surface = SurfaceDark,
    onSurface = Color(0xFFE8EAF6),
    surfaceVariant = Color(0xFF1E2A3A),
    onSurfaceVariant = Color(0xFFB0BEC5),
    outline = LightBlue700,
    error = Color(0xFFEF9A9A),
    onError = Color(0xFFB71C1C),
    errorContainer = Color(0xFFD32F2F),
    onErrorContainer = Color(0xFFFFCDD2),
    inverseSurface = Color(0xFFE8EAF6),
    inverseOnSurface = SurfaceDark,
    inversePrimary = PrimaryDark,
    surfaceTint = LightBlue300
)

@Composable
fun YinDunShouHuTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // 禁用动态颜色，使用统一的淡蓝色主题
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkBlueColorScheme else LightBlueColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ElderTypography,
        content = content
    )
}
