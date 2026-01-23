package com.gymtracker.app.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.gymtracker.app.data.ThemeColor

// Couleurs de base par thème
object ThemeColors {
    // Vert (défaut)
    val GreenPrimary = Color(0xFF4CAF50)
    val GreenPrimaryDark = Color(0xFF388E3C)
    val GreenPrimaryLight = Color(0xFF81C784)

    // Bleu
    val BluePrimary = Color(0xFF2196F3)
    val BluePrimaryDark = Color(0xFF1976D2)
    val BluePrimaryLight = Color(0xFF64B5F6)

    // Violet
    val PurplePrimary = Color(0xFF9C27B0)
    val PurplePrimaryDark = Color(0xFF7B1FA2)
    val PurplePrimaryLight = Color(0xFFBA68C8)

    // Rose
    val PinkPrimary = Color(0xFFE91E63)
    val PinkPrimaryDark = Color(0xFFC2185B)
    val PinkPrimaryLight = Color(0xFFF06292)

    // Orange
    val OrangePrimary = Color(0xFFFF9800)
    val OrangePrimaryDark = Color(0xFFF57C00)
    val OrangePrimaryLight = Color(0xFFFFB74D)

    // Rouge
    val RedPrimary = Color(0xFFF44336)
    val RedPrimaryDark = Color(0xFFD32F2F)
    val RedPrimaryLight = Color(0xFFE57373)

    // Turquoise
    val TealPrimary = Color(0xFF009688)
    val TealPrimaryDark = Color(0xFF00796B)
    val TealPrimaryLight = Color(0xFF4DB6AC)
}

// État global des couleurs (modifiable dynamiquement)
object AppColors {
    var Primary by mutableStateOf(ThemeColors.GreenPrimary)
    var PrimaryDark by mutableStateOf(ThemeColors.GreenPrimaryDark)
    var PrimaryLight by mutableStateOf(ThemeColors.GreenPrimaryLight)

    fun updateColors(themeColor: ThemeColor) {
        when (themeColor) {
            ThemeColor.GREEN -> {
                Primary = ThemeColors.GreenPrimary
                PrimaryDark = ThemeColors.GreenPrimaryDark
                PrimaryLight = ThemeColors.GreenPrimaryLight
            }
            ThemeColor.BLUE -> {
                Primary = ThemeColors.BluePrimary
                PrimaryDark = ThemeColors.BluePrimaryDark
                PrimaryLight = ThemeColors.BluePrimaryLight
            }
            ThemeColor.PURPLE -> {
                Primary = ThemeColors.PurplePrimary
                PrimaryDark = ThemeColors.PurplePrimaryDark
                PrimaryLight = ThemeColors.PurplePrimaryLight
            }
            ThemeColor.PINK -> {
                Primary = ThemeColors.PinkPrimary
                PrimaryDark = ThemeColors.PinkPrimaryDark
                PrimaryLight = ThemeColors.PinkPrimaryLight
            }
            ThemeColor.ORANGE -> {
                Primary = ThemeColors.OrangePrimary
                PrimaryDark = ThemeColors.OrangePrimaryDark
                PrimaryLight = ThemeColors.OrangePrimaryLight
            }
            ThemeColor.RED -> {
                Primary = ThemeColors.RedPrimary
                PrimaryDark = ThemeColors.RedPrimaryDark
                PrimaryLight = ThemeColors.RedPrimaryLight
            }
            ThemeColor.TEAL -> {
                Primary = ThemeColors.TealPrimary
                PrimaryDark = ThemeColors.TealPrimaryDark
                PrimaryLight = ThemeColors.TealPrimaryLight
            }
        }
    }
}

// Couleurs fixes (ne changent pas avec le thème)
val Secondary = Color(0xFFFFC107)
val SecondaryDark = Color(0xFFFFA000)

// Dark Theme
val BackgroundDark = Color(0xFF121212)
val SurfaceDark = Color(0xFF1E1E1E)
val SurfaceVariantDark = Color(0xFF2D2D2D)

// Light Theme
val BackgroundLight = Color(0xFFFAFAFA)
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceVariantLight = Color(0xFFF5F5F5)

val Error = Color(0xFFCF6679)

val OnPrimary = Color.White
val OnSecondary = Color.Black
val OnBackgroundDark = Color.White
val OnSurfaceDark = Color.White
val OnSurfaceVariantDark = Color(0xFFB0B0B0)
val OnBackgroundLight = Color.Black
val OnSurfaceLight = Color.Black
val OnSurfaceVariantLight = Color(0xFF666666)

// Status Colors
val Completed = Color(0xFF4CAF50)
val InProgress = Color(0xFFFFC107)
val Pending = Color(0xFF757575)

// Chart Colors
val ChartLine1 = Color(0xFF4CAF50)
val ChartLine2 = Color(0xFF2196F3)
val ChartLine3 = Color(0xFFFFC107)

// Variables de couleur utilisées dans l'app (seront mises à jour dynamiquement)
var Primary by mutableStateOf(AppColors.Primary)
var PrimaryDark by mutableStateOf(AppColors.PrimaryDark)
var PrimaryLight by mutableStateOf(AppColors.PrimaryLight)
var Background by mutableStateOf(BackgroundDark)
var Surface by mutableStateOf(SurfaceDark)
var SurfaceVariant by mutableStateOf(SurfaceVariantDark)
var OnBackground by mutableStateOf(OnBackgroundDark)
var OnSurface by mutableStateOf(OnSurfaceDark)
var OnSurfaceVariant by mutableStateOf(OnSurfaceVariantDark)

fun updateThemeMode(isDark: Boolean) {
    if (isDark) {
        Background = BackgroundDark
        Surface = SurfaceDark
        SurfaceVariant = SurfaceVariantDark
        OnBackground = OnBackgroundDark
        OnSurface = OnSurfaceDark
        OnSurfaceVariant = OnSurfaceVariantDark
    } else {
        Background = BackgroundLight
        Surface = SurfaceLight
        SurfaceVariant = SurfaceVariantLight
        OnBackground = OnBackgroundLight
        OnSurface = OnSurfaceLight
        OnSurfaceVariant = OnSurfaceVariantLight
    }
}

fun updatePrimaryColor(themeColor: ThemeColor) {
    AppColors.updateColors(themeColor)
    Primary = AppColors.Primary
    PrimaryDark = AppColors.PrimaryDark
    PrimaryLight = AppColors.PrimaryLight
}

