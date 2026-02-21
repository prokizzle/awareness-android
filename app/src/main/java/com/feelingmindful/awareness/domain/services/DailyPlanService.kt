package com.feelingmindful.awareness.domain.services

import com.feelingmindful.awareness.domain.entities.DailyPlan
import com.feelingmindful.awareness.domain.entities.ReflectionAnswer

interface DailyPlanService {
    suspend fun getTodaysPlan(userId: String): DailyPlan
    suspend fun completeMeditation(userId: String, plan: DailyPlan): DailyPlan
    suspend fun completeReflection(userId: String, plan: DailyPlan, answers: List<ReflectionAnswer>): DailyPlan
    suspend fun completeCardio(userId: String, plan: DailyPlan, minutes: Int, autoCompleted: Boolean): DailyPlan
    suspend fun uncompleteCardio(userId: String, plan: DailyPlan): DailyPlan
    suspend fun completeMeals(userId: String, plan: DailyPlan): DailyPlan
    suspend fun uncompleteMeals(userId: String, plan: DailyPlan): DailyPlan
}
