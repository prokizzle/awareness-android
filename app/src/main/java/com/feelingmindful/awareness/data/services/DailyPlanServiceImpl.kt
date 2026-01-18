package com.feelingmindful.awareness.data.services

import com.feelingmindful.awareness.domain.entities.DailyPlan
import com.feelingmindful.awareness.domain.entities.ReflectionAnswer
import com.feelingmindful.awareness.domain.entities.TaskStatus
import com.feelingmindful.awareness.domain.repositories.DailyPlanRepository
import com.feelingmindful.awareness.domain.repositories.MeditationRepository
import com.feelingmindful.awareness.domain.repositories.UserRepository
import com.feelingmindful.awareness.domain.services.DailyPlanService
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DailyPlanServiceImpl @Inject constructor(
    private val dailyPlanRepository: DailyPlanRepository,
    private val meditationRepository: MeditationRepository,
    private val userRepository: UserRepository
) : DailyPlanService {

    override suspend fun getTodaysPlan(userId: String): DailyPlan {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val existingPlan = dailyPlanRepository.getDailyPlan(userId, today)

        if (existingPlan != null) {
            return existingPlan
        }

        // Create new plan
        val progress = userRepository.getProgress(userId)
        val meditationId = getNextMeditationId(progress)

        val newPlan = DailyPlan.create(
            userId = userId,
            date = today,
            meditationId = meditationId
        )

        dailyPlanRepository.saveDailyPlan(userId, newPlan)
        return newPlan
    }

    override suspend fun completeMeditation(userId: String, plan: DailyPlan): DailyPlan {
        val now = Clock.System.now()
        val updated = plan.copy(
            tasks = plan.tasks.copy(
                meditation = plan.tasks.meditation.copy(
                    status = TaskStatus.COMPLETED,
                    completedAt = now
                ),
                reflection = plan.tasks.reflection.copy(
                    status = TaskStatus.AVAILABLE
                )
            )
        )
        dailyPlanRepository.saveDailyPlan(userId, updated)
        return updated
    }

    override suspend fun completeReflection(
        userId: String,
        plan: DailyPlan,
        answers: List<ReflectionAnswer>
    ): DailyPlan {
        val now = Clock.System.now()
        dailyPlanRepository.saveReflection(userId, plan.date, answers)

        val updated = plan.copy(
            tasks = plan.tasks.copy(
                reflection = plan.tasks.reflection.copy(
                    status = TaskStatus.COMPLETED,
                    completedAt = now
                ),
                cardio = plan.tasks.cardio.copy(
                    status = TaskStatus.AVAILABLE
                )
            )
        )
        dailyPlanRepository.saveDailyPlan(userId, updated)
        return updated
    }

    override suspend fun completeCardio(
        userId: String,
        plan: DailyPlan,
        minutes: Int,
        autoCompleted: Boolean
    ): DailyPlan {
        val now = Clock.System.now()
        val updated = plan.copy(
            tasks = plan.tasks.copy(
                cardio = plan.tasks.cardio.copy(
                    status = TaskStatus.COMPLETED,
                    completedAt = now,
                    minutes = minutes,
                    autoCompleted = autoCompleted
                ),
                meals = plan.tasks.meals.copy(
                    status = TaskStatus.AVAILABLE
                )
            )
        )
        dailyPlanRepository.saveDailyPlan(userId, updated)
        return updated
    }

    override suspend fun uncompleteCardio(userId: String, plan: DailyPlan): DailyPlan {
        val updated = plan.copy(
            tasks = plan.tasks.copy(
                cardio = plan.tasks.cardio.copy(
                    status = TaskStatus.AVAILABLE,
                    completedAt = null,
                    minutes = null,
                    autoCompleted = false
                ),
                meals = plan.tasks.meals.copy(
                    status = TaskStatus.LOCKED
                )
            )
        )
        dailyPlanRepository.saveDailyPlan(userId, updated)
        return updated
    }

    override suspend fun completeMeals(userId: String, plan: DailyPlan): DailyPlan {
        val now = Clock.System.now()
        val progress = userRepository.getProgress(userId)

        val updated = plan.copy(
            tasks = plan.tasks.copy(
                meals = plan.tasks.meals.copy(
                    status = TaskStatus.COMPLETED,
                    completedAt = now
                )
            ),
            allCompleted = true,
            completedAt = now
        )

        // Update streak
        val updatedProgress = progress.copy(
            streakDays = progress.streakDays + 1,
            completedMeditationIds = progress.completedMeditationIds + listOfNotNull(plan.meditationId)
        )

        userRepository.updateProgress(userId, updatedProgress)
        dailyPlanRepository.saveDailyPlan(userId, updated)
        return updated
    }

    override suspend fun uncompleteMeals(userId: String, plan: DailyPlan): DailyPlan {
        val progress = userRepository.getProgress(userId)

        val updated = plan.copy(
            tasks = plan.tasks.copy(
                meals = plan.tasks.meals.copy(
                    status = TaskStatus.AVAILABLE,
                    completedAt = null
                )
            ),
            allCompleted = false,
            completedAt = null
        )

        // Decrement streak if needed
        if (progress.streakDays > 0) {
            val updatedProgress = progress.copy(streakDays = progress.streakDays - 1)
            userRepository.updateProgress(userId, updatedProgress)
        }

        dailyPlanRepository.saveDailyPlan(userId, updated)
        return updated
    }

    private suspend fun getNextMeditationId(progress: com.feelingmindful.awareness.domain.entities.UserProgress): String? {
        // Check if user is in a course
        val courseId = progress.currentCourseId
        if (courseId != null) {
            val course = meditationRepository.getCourse(courseId)
            val nextIndex = progress.currentCourseMeditationIndex
            if (nextIndex < course.meditationIds.size) {
                return course.meditationIds[nextIndex]
            }
        }

        // Otherwise get intro course or random meditation
        val introCourse = meditationRepository.getIntroCourse()
        if (introCourse != null && progress.completedMeditationIds.none { it in introCourse.meditationIds }) {
            return introCourse.meditationIds.firstOrNull()
        }

        // Fall back to unplayed meditations
        val unplayed = meditationRepository.getUnplayedMeditations(
            excludeIds = progress.completedMeditationIds,
            limit = 1
        )
        return unplayed.firstOrNull()?.id
    }
}
