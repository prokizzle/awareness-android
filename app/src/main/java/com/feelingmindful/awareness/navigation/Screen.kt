package com.feelingmindful.awareness.navigation

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object DailyPlan : Screen("daily_plan")
    object Player : Screen("player/{meditationId}") {
        fun createRoute(meditationId: String) = "player/$meditationId"
    }
    object Reflection : Screen("reflection")
}
