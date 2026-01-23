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
    
    object History : Screen(
        route = "history",
        title = "Historique",
        icon = Icons.Default.History
    )
    
    object Progress : Screen(
        route = "progress",
        title = "Progression",
        icon = Icons.Default.TrendingUp
    )
    
    companion object {
        val items = listOf(Workout, History, Progress)
    }
}
