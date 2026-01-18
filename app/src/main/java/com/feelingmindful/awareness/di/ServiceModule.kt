package com.feelingmindful.awareness.di

import com.feelingmindful.awareness.data.services.DailyPlanServiceImpl
import com.feelingmindful.awareness.data.services.HealthConnectServiceImpl
import com.feelingmindful.awareness.data.services.NotificationServiceImpl
import com.feelingmindful.awareness.domain.services.DailyPlanService
import com.feelingmindful.awareness.domain.services.HealthConnectService
import com.feelingmindful.awareness.domain.services.NotificationService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ServiceModule {

    @Binds
    @Singleton
    abstract fun bindDailyPlanService(impl: DailyPlanServiceImpl): DailyPlanService

    @Binds
    @Singleton
    abstract fun bindHealthConnectService(impl: HealthConnectServiceImpl): HealthConnectService

    @Binds
    @Singleton
    abstract fun bindNotificationService(impl: NotificationServiceImpl): NotificationService
}
