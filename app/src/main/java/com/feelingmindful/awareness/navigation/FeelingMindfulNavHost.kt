package com.feelingmindful.awareness.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.feelingmindful.awareness.domain.repositories.AuthRepository
import com.feelingmindful.awareness.features.dailyplan.DailyPlanScreen
import com.feelingmindful.awareness.features.onboarding.OnboardingScreen
import com.feelingmindful.awareness.features.playback.PlayerScreen
import com.feelingmindful.awareness.features.reflection.ReflectionScreen

@Composable
fun FeelingMindfulNavHost(
    navController: NavHostController,
    authRepository: AuthRepository
) {
    val currentUser by authRepository.currentUser.collectAsState(initial = null)
    val startDestination = if (currentUser != null) Screen.DailyPlan.route else Screen.Onboarding.route

    NavHost(
        navController = navController,
        startDestination = startDestination
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
