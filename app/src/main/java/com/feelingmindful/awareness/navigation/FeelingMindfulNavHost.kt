package com.feelingmindful.awareness.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.feelingmindful.awareness.R
import com.feelingmindful.awareness.domain.repositories.AuthRepository
import com.feelingmindful.awareness.features.dailyplan.DailyPlanScreen
import com.feelingmindful.awareness.features.library.LibraryScreen
import com.feelingmindful.awareness.features.onboarding.OnboardingScreen
import com.feelingmindful.awareness.features.playback.PlayerScreen
import com.feelingmindful.awareness.features.reflection.ReflectionScreen
import com.feelingmindful.awareness.ui.components.LoadingIndicator
import kotlinx.coroutines.flow.first

/**
 * Data class representing a tab in the bottom navigation.
 */
data class TabItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private val tabScreens = setOf(
    Screen.DailyPlan.route,
    Screen.Library.route,
    Screen.Settings.route
)

@Composable
fun FeelingMindfulNavHost(
    navController: NavHostController,
    authRepository: AuthRepository
) {
    // Wait for the first auth emission before deciding start destination.
    // This prevents the race condition where collectAsState(initial = null)
    // always starts with null, causing signed-in users to see onboarding.
    var isAuthResolved by remember { mutableStateOf(false) }
    var startDestination by remember { mutableStateOf(Screen.Onboarding.route) }

    LaunchedEffect(Unit) {
        val firstUser = authRepository.currentUser.first()
        startDestination = if (firstUser != null) {
            Screen.DailyPlan.route
        } else {
            Screen.Onboarding.route
        }
        isAuthResolved = true
    }

    if (!isAuthResolved) {
        LoadingIndicator(message = "Loading...")
        return
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Only show bottom bar on main tab screens
    val showBottomBar = currentRoute in tabScreens

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(navController = navController)
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onSignedIn = {
                        navController.navigate(Screen.DailyPlan.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.DailyPlan.route) {
                DailyPlanScreen(
                    onNavigateToPlayer = { meditationId ->
                        navController.navigate(Screen.Player.createRoute(meditationId))
                    },
                    onNavigateToReflection = {
                        navController.navigate(Screen.Reflection.route)
                    }
                )
            }

            composable(Screen.Library.route) {
                LibraryScreen(
                    onNavigateToPlayer = { meditationId ->
                        navController.navigate(Screen.Player.createRoute(meditationId))
                    }
                )
            }

            composable(Screen.Settings.route) {
                SettingsPlaceholder()
            }

            composable(
                route = Screen.Player.route,
                arguments = listOf(navArgument("meditationId") { type = NavType.StringType })
            ) { backStackEntry ->
                val meditationId = backStackEntry.arguments?.getString("meditationId") ?: return@composable
                PlayerScreen(
                    meditationId = meditationId,
                    onNavigateBack = { navController.popBackStack() },
                    onMeditationComplete = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.Reflection.route) {
                ReflectionScreen(
                    onComplete = { navController.popBackStack() },
                    onDismiss = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
private fun BottomNavigationBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Remember navigation callbacks to avoid recreating lambdas on every recomposition
    val onNavigateToToday = remember(navController) {
        {
            navController.navigate(Screen.DailyPlan.route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    val onNavigateToLibrary = remember(navController) {
        {
            navController.navigate(Screen.Library.route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    val onNavigateToSettings = remember(navController) {
        {
            navController.navigate(Screen.Settings.route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        // Today tab
        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = ImageVector.vectorResource(
                        if (currentDestination?.route == Screen.DailyPlan.route)
                            R.drawable.ic_sun_filled
                        else
                            R.drawable.ic_sun_outlined
                    ),
                    contentDescription = "Today"
                )
            },
            label = { Text("Today") },
            selected = currentDestination?.hierarchy?.any { it.route == Screen.DailyPlan.route } == true,
            onClick = onNavigateToToday
        )

        // Library tab
        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = ImageVector.vectorResource(
                        if (currentDestination?.route == Screen.Library.route)
                            R.drawable.ic_library_filled
                        else
                            R.drawable.ic_library_outlined
                    ),
                    contentDescription = "Library"
                )
            },
            label = { Text("Library") },
            selected = currentDestination?.hierarchy?.any { it.route == Screen.Library.route } == true,
            onClick = onNavigateToLibrary
        )

        // Settings tab
        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = if (currentDestination?.route == Screen.Settings.route)
                        Icons.Filled.Settings
                    else
                        Icons.Outlined.Settings,
                    contentDescription = "Settings"
                )
            },
            label = { Text("Settings") },
            selected = currentDestination?.hierarchy?.any { it.route == Screen.Settings.route } == true,
            onClick = onNavigateToSettings
        )
    }
}

@Composable
private fun SettingsPlaceholder() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium
        )
    }
}
