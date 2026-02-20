package com.feelingmindful.awareness.navigation

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object DailyPlan : Screen("daily_plan")
    object Library : Screen("library")
    object Settings : Screen("settings")
    object Player : Screen("player/{meditationId}") {
        fun createRoute(meditationId: String) = "player/$meditationId"
    }
    object Reflection : Screen("reflection")
}
