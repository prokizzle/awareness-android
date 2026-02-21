package com.feelingmindful.awareness.di

import com.feelingmindful.awareness.data.repositories.FirebaseAuthRepository
import com.feelingmindful.awareness.data.repositories.FirestoreDailyPlanRepository
import com.feelingmindful.awareness.data.repositories.FirestoreMantraRepository
import com.feelingmindful.awareness.data.repositories.FirestoreMeditationRepository
import com.feelingmindful.awareness.data.repositories.FirestoreRescueRepository
import com.feelingmindful.awareness.data.repositories.FirestoreUserRepository
import com.feelingmindful.awareness.domain.repositories.AuthRepository
import com.feelingmindful.awareness.domain.repositories.DailyPlanRepository
import com.feelingmindful.awareness.domain.repositories.MantraRepository
import com.feelingmindful.awareness.domain.repositories.MeditationRepository
import com.feelingmindful.awareness.domain.repositories.RescueRepository
import com.feelingmindful.awareness.domain.repositories.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: FirebaseAuthRepository): AuthRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: FirestoreUserRepository): UserRepository

    @Binds
    @Singleton
    abstract fun bindDailyPlanRepository(impl: FirestoreDailyPlanRepository): DailyPlanRepository

    @Binds
    @Singleton
    abstract fun bindMeditationRepository(impl: FirestoreMeditationRepository): MeditationRepository

    @Binds
    @Singleton
    abstract fun bindMantraRepository(impl: FirestoreMantraRepository): MantraRepository

    @Binds
    @Singleton
    abstract fun bindRescueRepository(impl: FirestoreRescueRepository): RescueRepository
}
