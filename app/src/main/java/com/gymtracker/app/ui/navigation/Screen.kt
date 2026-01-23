package com.gymtracker.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Workout : Screen(
        route = "workout",
        title = "Séance",
        icon = Icons.Default.FitnessCenter
    )
    
    object Templates : Screen(
        route = "templates",
        title = "Séances",
        icon = Icons.Default.FolderOpen
    )

    object History : Screen(
        route = "history",
        title = "Historique",
        icon = Icons.Default.History
    )
    
    object Progress : Screen(
        route = "progress",
        title = "Stats",
        icon = Icons.Default.TrendingUp
    )
    
    object Settings : Screen(
        route = "settings",
        title = "Réglages",
        icon = Icons.Default.Settings
    )

    companion object {
        val items = listOf(Workout, Templates, History, Progress, Settings)
    }
}
