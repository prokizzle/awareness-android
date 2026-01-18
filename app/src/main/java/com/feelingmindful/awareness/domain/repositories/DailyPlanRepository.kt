package com.feelingmindful.awareness.domain.repositories

import com.feelingmindful.awareness.domain.entities.DailyPlan
import com.feelingmindful.awareness.domain.entities.ReflectionAnswer
import kotlinx.datetime.LocalDate

interface DailyPlanRepository {
    suspend fun getDailyPlan(userId: String, date: LocalDate): DailyPlan?
    suspend fun saveDailyPlan(userId: String, plan: DailyPlan)
    suspend fun saveReflection(userId: String, date: LocalDate, answers: List<ReflectionAnswer>)
}
