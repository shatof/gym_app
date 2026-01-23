package com.gymtracker.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.gymtracker.app.data.SettingsManager
import com.gymtracker.app.data.repository.GymRepository
import com.gymtracker.app.ui.screens.*
import com.gymtracker.app.ui.theme.*
import com.gymtracker.app.ui.viewmodel.ProgressViewModel
import com.gymtracker.app.ui.viewmodel.TemplateViewModel
import com.gymtracker.app.ui.viewmodel.WorkoutViewModel
import kotlinx.coroutines.launch

@Composable
fun GymTrackerNavigation(
    repository: GymRepository
) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val settingsManager = remember { SettingsManager(context) }

    // Observer et appliquer les paramètres de thème
    val isDarkTheme by settingsManager.isDarkTheme.collectAsState(initial = true)
    val themeColor by settingsManager.themeColor.collectAsState(initial = com.gymtracker.app.data.ThemeColor.GREEN)

    // Appliquer le thème
    LaunchedEffect(isDarkTheme) {
        updateThemeMode(isDarkTheme)
    }

    LaunchedEffect(themeColor) {
        updatePrimaryColor(themeColor)
    }

    // ViewModels partagés
    val workoutViewModel: WorkoutViewModel = viewModel(
        factory = WorkoutViewModel.provideFactory(repository)
    )
    val progressViewModel: ProgressViewModel = viewModel(
        factory = ProgressViewModel.provideFactory(repository)
    )
    val templateViewModel: TemplateViewModel = viewModel(
        factory = TemplateViewModel.provideFactory(repository)
    )

    // Collecter les messages UI
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        workoutViewModel.uiMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = Surface,
                contentColor = OnSurface
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                
                Screen.items.forEach { screen ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                screen.icon,
                                contentDescription = screen.title
                            )
                        },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Primary,
                            selectedTextColor = Primary,
                            unselectedIconColor = OnSurfaceVariant,
                            unselectedTextColor = OnSurfaceVariant,
                            indicatorColor = Primary.copy(alpha = 0.2f)
                        )
                    )
                }
            }
        },
        containerColor = Background
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Workout.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Workout.route) {
                WorkoutScreen(
                    viewModel = workoutViewModel,
                    settingsManager = settingsManager
                )
            }
            
            composable(Screen.Templates.route) {
                TemplateScreen(viewModel = templateViewModel)
            }

            composable(Screen.History.route) {
                HistoryScreen(viewModel = workoutViewModel)
            }
            
            composable(Screen.Progress.route) {
                ProgressScreen(viewModel = progressViewModel)
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    settingsManager = settingsManager,
                    repository = repository,
                    onShowSnackbar = { message ->
                        scope.launch {
                            snackbarHostState.showSnackbar(message)
                        }
                    }
                )
            }
        }
    }
}
