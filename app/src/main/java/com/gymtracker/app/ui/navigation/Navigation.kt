package com.gymtracker.app.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gymtracker.app.data.SettingsManager
import com.gymtracker.app.data.repository.GymRepository
import com.gymtracker.app.ui.screens.*
import com.gymtracker.app.ui.theme.*
import com.gymtracker.app.ui.viewmodel.ProgressViewModel
import com.gymtracker.app.ui.viewmodel.TemplateViewModel
import com.gymtracker.app.ui.viewmodel.WorkoutViewModel
import kotlinx.coroutines.launch

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun GymTrackerNavigation(
    repository: GymRepository
) {
    val context = LocalContext.current
    val settingsManager = remember { SettingsManager(context) }
    val scope = rememberCoroutineScope()

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

    LaunchedEffect(Unit) {
        workoutViewModel.uiMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    // État du pager pour le swipe
    val screens = Screen.items
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { screens.size }
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = Surface,
                contentColor = OnSurface
            ) {
                screens.forEachIndexed { index, screen ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                screen.icon,
                                contentDescription = screen.title
                            )
                        },
                        label = { Text(screen.title) },
                        selected = pagerState.currentPage == index,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(
                                    page = index,
                                    animationSpec = tween(durationMillis = 300)
                                )
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
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.padding(innerPadding),
            key = { screens[it].route }
        ) { page ->
            when (screens[page]) {
                Screen.Workout -> WorkoutScreen(
                    viewModel = workoutViewModel,
                    settingsManager = settingsManager
                )
                Screen.Templates -> TemplateScreen(viewModel = templateViewModel)
                Screen.History -> HistoryScreen(viewModel = workoutViewModel)
                Screen.Progress -> ProgressScreen(viewModel = progressViewModel)
                Screen.Settings -> SettingsScreen(
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
