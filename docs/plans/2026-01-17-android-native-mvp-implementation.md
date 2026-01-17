# Android Native MVP Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Build the FeelingMindful Android app with feature parity to iOS, sharing the same Firebase backend.

**Architecture:** Mirror iOS structure (View → ViewModel → Service → Repository → Firebase) with Hilt for dependency injection. Single Activity with Compose Navigation. Media3/ExoPlayer for audio, Health Connect for fitness tracking.

**Tech Stack:** Kotlin, Jetpack Compose, Material 3, Hilt, Firebase (Auth/Firestore/Storage), Media3, Health Connect, Credential Manager

---

## Phase 1: Project Setup

### Task 1: Add Dependencies

**Files:**
- Modify: `build.gradle.kts` (project root)
- Modify: `app/build.gradle.kts`
- Modify: `settings.gradle.kts`
- Modify: `gradle/libs.versions.toml`

**Step 1: Update version catalog**

Add to `gradle/libs.versions.toml`:

```toml
[versions]
agp = "8.7.3"
kotlin = "2.1.0"
coreKtx = "1.15.0"
junit = "4.13.2"
junitVersion = "1.2.1"
espressoCore = "3.6.1"
lifecycleRuntimeKtx = "2.8.7"
activityCompose = "1.9.3"
composeBom = "2024.12.01"
hilt = "2.53.1"
hiltNavigationCompose = "1.2.0"
firebaseBom = "33.7.0"
media3 = "1.5.1"
healthConnect = "1.1.0-alpha10"
credentials = "1.3.0"
googleid = "1.1.1"
navigationCompose = "2.8.5"
kotlinxSerialization = "1.7.3"
kotlinxDatetime = "0.6.1"
coil = "2.7.0"
ksp = "2.1.0-1.0.29"
googleServices = "4.4.2"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
junit = { group = "junit", name = "junit", version.ref = "junit" }
androidx-junit = { group = "androidx.test.ext", name = "junit", version.ref = "junitVersion" }
androidx-espresso-core = { group = "androidx.test.espresso", name = "espresso-core", version.ref = "espressoCore" }
androidx-lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycleRuntimeKtx" }
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycleRuntimeKtx" }
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activityCompose" }
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
androidx-ui = { group = "androidx.compose.ui", name = "ui" }
androidx-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
androidx-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
androidx-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
androidx-ui-test-manifest = { group = "androidx.compose.ui", name = "ui-test-manifest" }
androidx-ui-test-junit4 = { group = "androidx.compose.ui", name = "ui-test-junit4" }
androidx-material3 = { group = "androidx.compose.material3", name = "material3" }
androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigationCompose" }

# Hilt
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-compiler", version.ref = "hilt" }
hilt-navigation-compose = { group = "androidx.hilt", name = "hilt-navigation-compose", version.ref = "hiltNavigationCompose" }

# Firebase
firebase-bom = { group = "com.google.firebase", name = "firebase-bom", version.ref = "firebaseBom" }
firebase-auth = { group = "com.google.firebase", name = "firebase-auth-ktx" }
firebase-firestore = { group = "com.google.firebase", name = "firebase-firestore-ktx" }
firebase-storage = { group = "com.google.firebase", name = "firebase-storage-ktx" }

# Media3
media3-exoplayer = { group = "androidx.media3", name = "media3-exoplayer", version.ref = "media3" }
media3-session = { group = "androidx.media3", name = "media3-session", version.ref = "media3" }
media3-ui = { group = "androidx.media3", name = "media3-ui", version.ref = "media3" }

# Health Connect
health-connect = { group = "androidx.health.connect", name = "connect-client", version.ref = "healthConnect" }

# Credentials (Google Sign-In)
credentials = { group = "androidx.credentials", name = "credentials", version.ref = "credentials" }
credentials-play-services = { group = "androidx.credentials", name = "credentials-play-services-auth", version.ref = "credentials" }
googleid = { group = "com.google.android.libraries.identity.googleid", name = "googleid", version.ref = "googleid" }

# Serialization
kotlinx-serialization-json = { group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version.ref = "kotlinxSerialization" }
kotlinx-datetime = { group = "org.jetbrains.kotlinx", name = "kotlinx-datetime", version.ref = "kotlinxDatetime" }

# Image loading
coil-compose = { group = "io.coil-kt", name = "coil-compose", version.ref = "coil" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
google-services = { id = "com.google.gms.google-services", version.ref = "googleServices" }
```

**Step 2: Update project build.gradle.kts**

Replace `build.gradle.kts` (root):

```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.google.services) apply false
}
```

**Step 3: Update app build.gradle.kts**

Replace `app/build.gradle.kts`:

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.feelingmindful.awareness"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.feelingmindful.awareness"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)

    // Media3
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.session)
    implementation(libs.media3.ui)

    // Health Connect
    implementation(libs.health.connect)

    // Credentials (Google Sign-In)
    implementation(libs.credentials)
    implementation(libs.credentials.play.services)
    implementation(libs.googleid)

    // Serialization
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)

    // Image loading
    implementation(libs.coil.compose)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
```

**Step 4: Sync Gradle**

Run: `./gradlew --refresh-dependencies`
Expected: BUILD SUCCESSFUL

**Step 5: Commit**

```bash
git add -A
git commit -m "chore: add project dependencies

- Hilt for DI
- Firebase (Auth, Firestore, Storage)
- Media3 for audio playback
- Health Connect for fitness tracking
- Credential Manager for Google Sign-In
- Kotlinx Serialization and Datetime"
```

---

### Task 2: Add Firebase Configuration

**Files:**
- Create: `app/google-services.json` (from Firebase Console)
- Modify: `app/src/main/AndroidManifest.xml`

**Step 1: Download google-services.json**

1. Go to Firebase Console → Project Settings
2. Select Android app (or add one with package `com.feelingmindful.awareness`)
3. Download `google-services.json`
4. Place in `app/` directory

**Step 2: Update AndroidManifest.xml**

Replace `app/src/main/AndroidManifest.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK" />

    <application
        android:name=".FeelingMindfulApp"
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.Awareness"
        tools:targetApi="36">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:label="@string/app_name"
            android:theme="@style/Theme.Awareness">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <!-- Health Connect permissions activity -->
        <activity
            android:name="androidx.health.connect.client.permission.HealthPermissionsRequestContract"
            android:exported="true">
            <intent-filter>
                <action android:name="androidx.health.ACTION_SHOW_PERMISSIONS_RATIONALE" />
            </intent-filter>
        </activity>

        <activity-alias
            android:name="ViewPermissionUsageActivity"
            android:exported="true"
            android:targetActivity=".MainActivity"
            android:permission="android.permission.START_VIEW_PERMISSION_USAGE">
            <intent-filter>
                <action android:name="android.intent.action.VIEW_PERMISSION_USAGE" />
                <category android:name="android.intent.category.HEALTH_PERMISSIONS" />
            </intent-filter>
        </activity-alias>

    </application>

    <!-- Health Connect permissions -->
    <queries>
        <package android:name="com.google.android.apps.healthdata" />
    </queries>

</manifest>
```

**Step 3: Commit**

```bash
git add -A
git commit -m "chore: add Firebase config and manifest permissions"
```

---

### Task 3: Create Application Class with Hilt

**Files:**
- Create: `app/src/main/java/com/feelingmindful/awareness/FeelingMindfulApp.kt`

**Step 1: Create Application class**

Create `app/src/main/java/com/feelingmindful/awareness/FeelingMindfulApp.kt`:

```kotlin
package com.feelingmindful.awareness

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FeelingMindfulApp : Application()
```

**Step 2: Update MainActivity**

Replace `app/src/main/java/com/feelingmindful/awareness/MainActivity.kt`:

```kotlin
package com.feelingmindful.awareness

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import com.feelingmindful.awareness.ui.theme.AwarenessTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AwarenessTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Text("FeelingMindful")
                }
            }
        }
    }
}
```

**Step 3: Build and verify**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

**Step 4: Commit**

```bash
git add -A
git commit -m "chore: setup Hilt application and entry point"
```

---

## Phase 2: Domain Layer

### Task 4: Create Domain Entities

**Files:**
- Create: `app/src/main/java/com/feelingmindful/awareness/domain/entities/User.kt`
- Create: `app/src/main/java/com/feelingmindful/awareness/domain/entities/UserProgress.kt`
- Create: `app/src/main/java/com/feelingmindful/awareness/domain/entities/DailyPlan.kt`
- Create: `app/src/main/java/com/feelingmindful/awareness/domain/entities/Meditation.kt`
- Create: `app/src/main/java/com/feelingmindful/awareness/domain/entities/Course.kt`
- Create: `app/src/main/java/com/feelingmindful/awareness/domain/entities/Mantra.kt`
- Create: `app/src/main/java/com/feelingmindful/awareness/domain/entities/Reflection.kt`

**Step 1: Create User entities**

Create `app/src/main/java/com/feelingmindful/awareness/domain/entities/User.kt`:

```kotlin
package com.feelingmindful.awareness.domain.entities

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val email: String,
    val displayName: String? = null,
    val photoUrl: String? = null,
    val authProvider: AuthProvider
)

@Serializable
enum class AuthProvider {
    GOOGLE,
    APPLE,
    EMAIL
}
```

Create `app/src/main/java/com/feelingmindful/awareness/domain/entities/UserProgress.kt`:

```kotlin
package com.feelingmindful.awareness.domain.entities

import kotlinx.serialization.Serializable

@Serializable
data class UserProgress(
    val visitsCount: Int = 0,
    val streakDays: Int = 0,
    val completedMeditationIds: List<String> = emptyList(),
    val currentCourseId: String? = null,
    val currentCourseMeditationIndex: Int = 0
)
```

**Step 2: Create DailyPlan entities**

Create `app/src/main/java/com/feelingmindful/awareness/domain/entities/DailyPlan.kt`:

```kotlin
package com.feelingmindful.awareness.domain.entities

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class DailyPlan(
    val id: String,
    val userId: String,
    val date: LocalDate,
    val tasks: DailyTasks,
    val mantraId: String? = null,
    val meditationId: String? = null,
    val allCompleted: Boolean = false,
    val completedAt: Instant? = null
) {
    companion object {
        fun create(userId: String, date: LocalDate, meditationId: String? = null): DailyPlan {
            return DailyPlan(
                id = date.toString(),
                userId = userId,
                date = date,
                tasks = DailyTasks.initial(),
                meditationId = meditationId
            )
        }
    }
}

@Serializable
data class DailyTasks(
    val meditation: TaskState,
    val reflection: TaskState,
    val cardio: TaskState,
    val meals: TaskState
) {
    companion object {
        fun initial(): DailyTasks = DailyTasks(
            meditation = TaskState(status = TaskStatus.AVAILABLE),
            reflection = TaskState(status = TaskStatus.LOCKED),
            cardio = TaskState(status = TaskStatus.LOCKED),
            meals = TaskState(status = TaskStatus.LOCKED)
        )
    }
}

@Serializable
data class TaskState(
    val status: TaskStatus,
    val completedAt: Instant? = null,
    val minutes: Int? = null,
    val autoCompleted: Boolean = false
)

@Serializable
enum class TaskStatus {
    LOCKED,
    AVAILABLE,
    IN_PROGRESS,
    COMPLETED;

    val isActionable: Boolean
        get() = this == AVAILABLE || this == IN_PROGRESS
}
```

**Step 3: Create Content entities**

Create `app/src/main/java/com/feelingmindful/awareness/domain/entities/Meditation.kt`:

```kotlin
package com.feelingmindful.awareness.domain.entities

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Meditation(
    val id: String,
    val title: String,
    val description: String,
    val duration: Int,
    val audioUrl: String? = null,
    val hasChunks: Boolean = false,
    val prompts: List<ReflectionPrompt> = emptyList(),
    val courseId: String? = null,
    val orderInCourse: Int? = null,
    val themes: List<String> = emptyList(),
    val teacherId: String? = null,
    val publishedAt: Instant? = null
) {
    val durationText: String
        get() {
            val minutes = duration / 60
            return "$minutes min"
        }
}
```

Create `app/src/main/java/com/feelingmindful/awareness/domain/entities/Course.kt`:

```kotlin
package com.feelingmindful.awareness.domain.entities

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Course(
    val id: String,
    val title: String,
    val description: String,
    val meditationIds: List<String>,
    val publishedAt: Instant? = null,
    val isIntro: Boolean = false
)
```

Create `app/src/main/java/com/feelingmindful/awareness/domain/entities/Mantra.kt`:

```kotlin
package com.feelingmindful.awareness.domain.entities

import kotlinx.serialization.Serializable

@Serializable
data class Mantra(
    val id: String,
    val text: String,
    val windowType: WindowType,
    val active: Boolean = true,
    val attribution: String? = null
)

@Serializable
enum class WindowType {
    MORNING,
    AFTERNOON,
    EVENING;

    companion object {
        fun fromHour(hour: Int): WindowType = when (hour) {
            in 5..11 -> MORNING
            in 12..16 -> AFTERNOON
            else -> EVENING
        }
    }
}
```

Create `app/src/main/java/com/feelingmindful/awareness/domain/entities/Reflection.kt`:

```kotlin
package com.feelingmindful.awareness.domain.entities

import kotlinx.serialization.Serializable

@Serializable
data class ReflectionPrompt(
    val id: String,
    val question: String,
    val order: Int
)

@Serializable
data class ReflectionAnswer(
    val promptId: String,
    val question: String,
    val answer: String
)
```

**Step 4: Commit**

```bash
git add -A
git commit -m "feat: add domain entities

- User and UserProgress
- DailyPlan with tasks and status
- Meditation, Course, Mantra
- ReflectionPrompt and ReflectionAnswer"
```

---

### Task 5: Create Repository Interfaces

**Files:**
- Create: `app/src/main/java/com/feelingmindful/awareness/domain/repositories/AuthRepository.kt`
- Create: `app/src/main/java/com/feelingmindful/awareness/domain/repositories/UserRepository.kt`
- Create: `app/src/main/java/com/feelingmindful/awareness/domain/repositories/DailyPlanRepository.kt`
- Create: `app/src/main/java/com/feelingmindful/awareness/domain/repositories/MeditationRepository.kt`
- Create: `app/src/main/java/com/feelingmindful/awareness/domain/repositories/MantraRepository.kt`

**Step 1: Create repository interfaces**

Create `app/src/main/java/com/feelingmindful/awareness/domain/repositories/AuthRepository.kt`:

```kotlin
package com.feelingmindful.awareness.domain.repositories

import com.feelingmindful.awareness.domain.entities.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<User?>
    suspend fun signInWithGoogle(idToken: String): User
    suspend fun signOut()
    suspend fun deleteAccount()
}
```

Create `app/src/main/java/com/feelingmindful/awareness/domain/repositories/UserRepository.kt`:

```kotlin
package com.feelingmindful.awareness.domain.repositories

import com.feelingmindful.awareness.domain.entities.UserProgress

interface UserRepository {
    suspend fun getProgress(userId: String): UserProgress
    suspend fun updateProgress(userId: String, progress: UserProgress)
}
```

Create `app/src/main/java/com/feelingmindful/awareness/domain/repositories/DailyPlanRepository.kt`:

```kotlin
package com.feelingmindful.awareness.domain.repositories

import com.feelingmindful.awareness.domain.entities.DailyPlan
import com.feelingmindful.awareness.domain.entities.ReflectionAnswer
import kotlinx.datetime.LocalDate

interface DailyPlanRepository {
    suspend fun getDailyPlan(userId: String, date: LocalDate): DailyPlan?
    suspend fun saveDailyPlan(userId: String, plan: DailyPlan)
    suspend fun saveReflection(userId: String, date: LocalDate, answers: List<ReflectionAnswer>)
}
```

Create `app/src/main/java/com/feelingmindful/awareness/domain/repositories/MeditationRepository.kt`:

```kotlin
package com.feelingmindful.awareness.domain.repositories

import com.feelingmindful.awareness.domain.entities.Course
import com.feelingmindful.awareness.domain.entities.Meditation
import com.feelingmindful.awareness.domain.entities.ReflectionPrompt

interface MeditationRepository {
    suspend fun getMeditation(id: String): Meditation
    suspend fun getMeditations(ids: List<String>): List<Meditation>
    suspend fun getCourse(id: String): Course
    suspend fun getIntroCourse(): Course?
    suspend fun getUnplayedMeditations(excludeIds: List<String>, limit: Int): List<Meditation>
    suspend fun getSystemPrompts(themes: List<String>): List<ReflectionPrompt>
}
```

Create `app/src/main/java/com/feelingmindful/awareness/domain/repositories/MantraRepository.kt`:

```kotlin
package com.feelingmindful.awareness.domain.repositories

import com.feelingmindful.awareness.domain.entities.Mantra
import com.feelingmindful.awareness.domain.entities.WindowType

interface MantraRepository {
    suspend fun getMantra(id: String): Mantra?
    suspend fun getRandomMantra(windowType: WindowType): Mantra?
}
```

**Step 2: Commit**

```bash
git add -A
git commit -m "feat: add repository interfaces

- AuthRepository for authentication
- UserRepository for user data
- DailyPlanRepository for daily plans
- MeditationRepository for content
- MantraRepository for mantras"
```

---

### Task 6: Create Service Interfaces

**Files:**
- Create: `app/src/main/java/com/feelingmindful/awareness/domain/services/DailyPlanService.kt`
- Create: `app/src/main/java/com/feelingmindful/awareness/domain/services/HealthConnectService.kt`
- Create: `app/src/main/java/com/feelingmindful/awareness/domain/services/NotificationService.kt`

**Step 1: Create service interfaces**

Create `app/src/main/java/com/feelingmindful/awareness/domain/services/DailyPlanService.kt`:

```kotlin
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
```

Create `app/src/main/java/com/feelingmindful/awareness/domain/services/HealthConnectService.kt`:

```kotlin
package com.feelingmindful.awareness.domain.services

interface HealthConnectService {
    val isAvailable: Boolean
    suspend fun hasPermissions(): Boolean
    suspend fun requestPermissions(): Boolean
    suspend fun getTodayWorkoutMinutes(): Int
    suspend fun saveMindfulnessSession(durationSeconds: Int)
}
```

Create `app/src/main/java/com/feelingmindful/awareness/domain/services/NotificationService.kt`:

```kotlin
package com.feelingmindful.awareness.domain.services

interface NotificationService {
    suspend fun hasPermission(): Boolean
    suspend fun requestPermission(): Boolean
    suspend fun scheduleDailyReminder(hour: Int, minute: Int)
    suspend fun cancelAllReminders()
}
```

**Step 2: Commit**

```bash
git add -A
git commit -m "feat: add service interfaces

- DailyPlanService for task orchestration
- HealthConnectService for fitness tracking
- NotificationService for reminders"
```

---

## Phase 3: Data Layer

### Task 7: Create Firebase Repository Implementations

**Files:**
- Create: `app/src/main/java/com/feelingmindful/awareness/data/repositories/FirebaseAuthRepository.kt`
- Create: `app/src/main/java/com/feelingmindful/awareness/data/repositories/FirestoreUserRepository.kt`
- Create: `app/src/main/java/com/feelingmindful/awareness/data/repositories/FirestoreDailyPlanRepository.kt`
- Create: `app/src/main/java/com/feelingmindful/awareness/data/repositories/FirestoreMeditationRepository.kt`
- Create: `app/src/main/java/com/feelingmindful/awareness/data/repositories/FirestoreMantraRepository.kt`

**Step 1: Create FirebaseAuthRepository**

Create `app/src/main/java/com/feelingmindful/awareness/data/repositories/FirebaseAuthRepository.kt`:

```kotlin
package com.feelingmindful.awareness.data.repositories

import com.feelingmindful.awareness.domain.entities.AuthProvider
import com.feelingmindful.awareness.domain.entities.User
import com.feelingmindful.awareness.domain.repositories.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthRepository @Inject constructor(
    private val auth: FirebaseAuth
) : AuthRepository {

    override val currentUser: Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser?.toUser())
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun signInWithGoogle(idToken: String): User {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = auth.signInWithCredential(credential).await()
        return result.user?.toUser() ?: throw Exception("Sign in failed")
    }

    override suspend fun signOut() {
        auth.signOut()
    }

    override suspend fun deleteAccount() {
        auth.currentUser?.delete()?.await()
    }

    private fun com.google.firebase.auth.FirebaseUser.toUser(): User {
        return User(
            id = uid,
            email = email ?: "",
            displayName = displayName,
            photoUrl = photoUrl?.toString(),
            authProvider = AuthProvider.GOOGLE
        )
    }
}
```

**Step 2: Create FirestoreUserRepository**

Create `app/src/main/java/com/feelingmindful/awareness/data/repositories/FirestoreUserRepository.kt`:

```kotlin
package com.feelingmindful.awareness.data.repositories

import com.feelingmindful.awareness.domain.entities.UserProgress
import com.feelingmindful.awareness.domain.repositories.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreUserRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : UserRepository {

    override suspend fun getProgress(userId: String): UserProgress {
        val doc = firestore
            .collection("users")
            .document(userId)
            .collection("data")
            .document("progress")
            .get()
            .await()

        return if (doc.exists()) {
            UserProgress(
                visitsCount = doc.getLong("visitsCount")?.toInt() ?: 0,
                streakDays = doc.getLong("streakDays")?.toInt() ?: 0,
                completedMeditationIds = (doc.get("completedMeditationIds") as? List<*>)
                    ?.filterIsInstance<String>() ?: emptyList(),
                currentCourseId = doc.getString("currentCourseId"),
                currentCourseMeditationIndex = doc.getLong("currentCourseMeditationIndex")?.toInt() ?: 0
            )
        } else {
            UserProgress()
        }
    }

    override suspend fun updateProgress(userId: String, progress: UserProgress) {
        firestore
            .collection("users")
            .document(userId)
            .collection("data")
            .document("progress")
            .set(
                mapOf(
                    "visitsCount" to progress.visitsCount,
                    "streakDays" to progress.streakDays,
                    "completedMeditationIds" to progress.completedMeditationIds,
                    "currentCourseId" to progress.currentCourseId,
                    "currentCourseMeditationIndex" to progress.currentCourseMeditationIndex
                )
            )
            .await()
    }
}
```

**Step 3: Create FirestoreDailyPlanRepository**

Create `app/src/main/java/com/feelingmindful/awareness/data/repositories/FirestoreDailyPlanRepository.kt`:

```kotlin
package com.feelingmindful.awareness.data.repositories

import com.feelingmindful.awareness.domain.entities.DailyPlan
import com.feelingmindful.awareness.domain.entities.DailyTasks
import com.feelingmindful.awareness.domain.entities.ReflectionAnswer
import com.feelingmindful.awareness.domain.entities.TaskState
import com.feelingmindful.awareness.domain.entities.TaskStatus
import com.feelingmindful.awareness.domain.repositories.DailyPlanRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreDailyPlanRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : DailyPlanRepository {

    override suspend fun getDailyPlan(userId: String, date: LocalDate): DailyPlan? {
        val doc = firestore
            .collection("users")
            .document(userId)
            .collection("dailyPlans")
            .document(date.toString())
            .get()
            .await()

        return if (doc.exists()) {
            doc.toDailyPlan(userId, date)
        } else {
            null
        }
    }

    override suspend fun saveDailyPlan(userId: String, plan: DailyPlan) {
        firestore
            .collection("users")
            .document(userId)
            .collection("dailyPlans")
            .document(plan.id)
            .set(plan.toMap())
            .await()
    }

    override suspend fun saveReflection(userId: String, date: LocalDate, answers: List<ReflectionAnswer>) {
        firestore
            .collection("users")
            .document(userId)
            .collection("dailyPlans")
            .document(date.toString())
            .collection("reflections")
            .document("answers")
            .set(
                mapOf(
                    "answers" to answers.map { answer ->
                        mapOf(
                            "promptId" to answer.promptId,
                            "question" to answer.question,
                            "answer" to answer.answer
                        )
                    },
                    "completedAt" to Timestamp.now()
                )
            )
            .await()
    }

    @Suppress("UNCHECKED_CAST")
    private fun com.google.firebase.firestore.DocumentSnapshot.toDailyPlan(
        userId: String,
        date: LocalDate
    ): DailyPlan {
        val tasks = get("tasks") as? Map<String, Any> ?: emptyMap()
        return DailyPlan(
            id = id,
            userId = userId,
            date = date,
            tasks = DailyTasks(
                meditation = tasks.parseTaskState("meditation"),
                reflection = tasks.parseTaskState("reflection"),
                cardio = tasks.parseTaskState("cardio"),
                meals = tasks.parseTaskState("meals")
            ),
            mantraId = getString("mantraId"),
            meditationId = getString("meditationId"),
            allCompleted = getBoolean("allCompleted") ?: false,
            completedAt = getTimestamp("completedAt")?.toInstant()
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun Map<String, Any>.parseTaskState(key: String): TaskState {
        val task = get(key) as? Map<String, Any> ?: return TaskState(TaskStatus.LOCKED)
        return TaskState(
            status = TaskStatus.valueOf(task["status"] as? String ?: "LOCKED"),
            completedAt = (task["completedAt"] as? Timestamp)?.toInstant(),
            minutes = (task["minutes"] as? Long)?.toInt(),
            autoCompleted = task["autoCompleted"] as? Boolean ?: false
        )
    }

    private fun DailyPlan.toMap(): Map<String, Any?> = mapOf(
        "userId" to userId,
        "tasks" to mapOf(
            "meditation" to tasks.meditation.toMap(),
            "reflection" to tasks.reflection.toMap(),
            "cardio" to tasks.cardio.toMap(),
            "meals" to tasks.meals.toMap()
        ),
        "mantraId" to mantraId,
        "meditationId" to meditationId,
        "allCompleted" to allCompleted,
        "completedAt" to completedAt?.toTimestamp()
    )

    private fun TaskState.toMap(): Map<String, Any?> = mapOf(
        "status" to status.name,
        "completedAt" to completedAt?.toTimestamp(),
        "minutes" to minutes,
        "autoCompleted" to autoCompleted
    )

    private fun Timestamp.toInstant(): Instant =
        Instant.fromEpochSeconds(seconds, nanoseconds)

    private fun Instant.toTimestamp(): Timestamp =
        Timestamp(epochSeconds, nanosecondsOfSecond)
}
```

**Step 4: Create FirestoreMeditationRepository**

Create `app/src/main/java/com/feelingmindful/awareness/data/repositories/FirestoreMeditationRepository.kt`:

```kotlin
package com.feelingmindful.awareness.data.repositories

import com.feelingmindful.awareness.domain.entities.Course
import com.feelingmindful.awareness.domain.entities.Meditation
import com.feelingmindful.awareness.domain.entities.ReflectionPrompt
import com.feelingmindful.awareness.domain.repositories.MeditationRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.datetime.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreMeditationRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : MeditationRepository {

    override suspend fun getMeditation(id: String): Meditation {
        val doc = firestore
            .collection("meditations")
            .document(id)
            .get()
            .await()

        return doc.toMeditation()
    }

    override suspend fun getMeditations(ids: List<String>): List<Meditation> {
        if (ids.isEmpty()) return emptyList()

        return firestore
            .collection("meditations")
            .whereIn("__name__", ids.map { firestore.collection("meditations").document(it) })
            .get()
            .await()
            .documents
            .map { it.toMeditation() }
    }

    override suspend fun getCourse(id: String): Course {
        val doc = firestore
            .collection("courses")
            .document(id)
            .get()
            .await()

        return doc.toCourse()
    }

    override suspend fun getIntroCourse(): Course? {
        val docs = firestore
            .collection("courses")
            .whereEqualTo("isIntro", true)
            .limit(1)
            .get()
            .await()

        return docs.documents.firstOrNull()?.toCourse()
    }

    override suspend fun getUnplayedMeditations(excludeIds: List<String>, limit: Int): List<Meditation> {
        val query = firestore
            .collection("meditations")
            .whereEqualTo("courseId", null)
            .limit(limit.toLong())

        return query.get().await().documents
            .map { it.toMeditation() }
            .filter { it.id !in excludeIds }
    }

    override suspend fun getSystemPrompts(themes: List<String>): List<ReflectionPrompt> {
        val docs = firestore
            .collection("systemPrompts")
            .orderBy("order")
            .limit(3)
            .get()
            .await()

        return docs.documents.mapIndexed { index, doc ->
            ReflectionPrompt(
                id = doc.id,
                question = doc.getString("question") ?: "",
                order = doc.getLong("order")?.toInt() ?: index
            )
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun com.google.firebase.firestore.DocumentSnapshot.toMeditation(): Meditation {
        val prompts = (get("prompts") as? List<Map<String, Any>>)?.mapIndexed { index, map ->
            ReflectionPrompt(
                id = map["id"] as? String ?: "$id-prompt-$index",
                question = map["question"] as? String ?: "",
                order = (map["order"] as? Long)?.toInt() ?: index
            )
        } ?: emptyList()

        return Meditation(
            id = id,
            title = getString("title") ?: "",
            description = getString("description") ?: "",
            duration = getLong("duration")?.toInt() ?: 0,
            audioUrl = getString("audioUrl"),
            hasChunks = getBoolean("hasChunks") ?: false,
            prompts = prompts,
            courseId = getString("courseId"),
            orderInCourse = getLong("orderInCourse")?.toInt(),
            themes = (get("themes") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            teacherId = getString("teacherId"),
            publishedAt = getTimestamp("publishedAt")?.toInstant()
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun com.google.firebase.firestore.DocumentSnapshot.toCourse(): Course {
        return Course(
            id = id,
            title = getString("title") ?: "",
            description = getString("description") ?: "",
            meditationIds = (get("meditationIds") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            publishedAt = getTimestamp("publishedAt")?.toInstant(),
            isIntro = getBoolean("isIntro") ?: false
        )
    }

    private fun Timestamp.toInstant(): Instant =
        Instant.fromEpochSeconds(seconds, nanoseconds)
}
```

**Step 5: Create FirestoreMantraRepository**

Create `app/src/main/java/com/feelingmindful/awareness/data/repositories/FirestoreMantraRepository.kt`:

```kotlin
package com.feelingmindful.awareness.data.repositories

import com.feelingmindful.awareness.domain.entities.Mantra
import com.feelingmindful.awareness.domain.entities.WindowType
import com.feelingmindful.awareness.domain.repositories.MantraRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreMantraRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : MantraRepository {

    override suspend fun getMantra(id: String): Mantra? {
        val doc = firestore
            .collection("mantras")
            .document(id)
            .get()
            .await()

        return if (doc.exists()) doc.toMantra() else null
    }

    override suspend fun getRandomMantra(windowType: WindowType): Mantra? {
        val docs = firestore
            .collection("mantras")
            .whereEqualTo("windowType", windowType.name.lowercase())
            .whereEqualTo("active", true)
            .get()
            .await()

        return docs.documents.randomOrNull()?.toMantra()
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toMantra(): Mantra {
        val windowTypeStr = getString("windowType") ?: "morning"
        return Mantra(
            id = id,
            text = getString("text") ?: "",
            windowType = WindowType.valueOf(windowTypeStr.uppercase()),
            active = getBoolean("active") ?: true,
            attribution = getString("attribution")
        )
    }
}
```

**Step 6: Commit**

```bash
git add -A
git commit -m "feat: add Firebase repository implementations

- FirebaseAuthRepository with Google Sign-In
- FirestoreUserRepository for user progress
- FirestoreDailyPlanRepository for daily plans
- FirestoreMeditationRepository for content
- FirestoreMantraRepository for mantras"
```

---

### Task 8: Create DailyPlanService Implementation

**Files:**
- Create: `app/src/main/java/com/feelingmindful/awareness/data/services/DailyPlanServiceImpl.kt`

**Step 1: Create DailyPlanServiceImpl**

Create `app/src/main/java/com/feelingmindful/awareness/data/services/DailyPlanServiceImpl.kt`:

```kotlin
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
```

**Step 2: Commit**

```bash
git add -A
git commit -m "feat: add DailyPlanService implementation

- Task completion and uncomplete logic
- Sequential task unlocking
- Streak tracking
- Course progression"
```

---

### Task 9: Create Hilt Modules

**Files:**
- Create: `app/src/main/java/com/feelingmindful/awareness/di/AppModule.kt`
- Create: `app/src/main/java/com/feelingmindful/awareness/di/RepositoryModule.kt`
- Create: `app/src/main/java/com/feelingmindful/awareness/di/ServiceModule.kt`

**Step 1: Create AppModule**

Create `app/src/main/java/com/feelingmindful/awareness/di/AppModule.kt`:

```kotlin
package com.feelingmindful.awareness.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()
}
```

**Step 2: Create RepositoryModule**

Create `app/src/main/java/com/feelingmindful/awareness/di/RepositoryModule.kt`:

```kotlin
package com.feelingmindful.awareness.di

import com.feelingmindful.awareness.data.repositories.FirebaseAuthRepository
import com.feelingmindful.awareness.data.repositories.FirestoreDailyPlanRepository
import com.feelingmindful.awareness.data.repositories.FirestoreMantraRepository
import com.feelingmindful.awareness.data.repositories.FirestoreMeditationRepository
import com.feelingmindful.awareness.data.repositories.FirestoreUserRepository
import com.feelingmindful.awareness.domain.repositories.AuthRepository
import com.feelingmindful.awareness.domain.repositories.DailyPlanRepository
import com.feelingmindful.awareness.domain.repositories.MantraRepository
import com.feelingmindful.awareness.domain.repositories.MeditationRepository
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
}
```

**Step 3: Create ServiceModule**

Create `app/src/main/java/com/feelingmindful/awareness/di/ServiceModule.kt`:

```kotlin
package com.feelingmindful.awareness.di

import com.feelingmindful.awareness.data.services.DailyPlanServiceImpl
import com.feelingmindful.awareness.domain.services.DailyPlanService
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
}
```

**Step 4: Build and verify**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

**Step 5: Commit**

```bash
git add -A
git commit -m "feat: add Hilt dependency injection modules

- AppModule for Firebase instances
- RepositoryModule for repository bindings
- ServiceModule for service bindings"
```

---

## Phase 4: UI Layer

### Task 10: Create Design System

**Files:**
- Modify: `app/src/main/java/com/feelingmindful/awareness/ui/theme/Color.kt`
- Modify: `app/src/main/java/com/feelingmindful/awareness/ui/theme/Type.kt`
- Modify: `app/src/main/java/com/feelingmindful/awareness/ui/theme/Theme.kt`

**Step 1: Update Color.kt**

Replace `app/src/main/java/com/feelingmindful/awareness/ui/theme/Color.kt`:

```kotlin
package com.feelingmindful.awareness.ui.theme

import androidx.compose.ui.graphics.Color

// Primary purple palette
val Purple500 = Color(0xFF7C3AED)
val Purple600 = Color(0xFF6D28D9)
val Purple700 = Color(0xFF5B21B6)

// Neutral palette
val Gray50 = Color(0xFFF9FAFB)
val Gray100 = Color(0xFFF3F4F6)
val Gray200 = Color(0xFFE5E7EB)
val Gray300 = Color(0xFFD1D5DB)
val Gray400 = Color(0xFF9CA3AF)
val Gray500 = Color(0xFF6B7280)
val Gray600 = Color(0xFF4B5563)
val Gray700 = Color(0xFF374151)
val Gray800 = Color(0xFF1F2937)
val Gray900 = Color(0xFF111827)

// Semantic colors
val Success = Color(0xFF10B981)
val Error = Color(0xFFEF4444)
val Warning = Color(0xFFF59E0B)

// Surface colors
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceDark = Color(0xFF121212)
val BackgroundLight = Gray50
val BackgroundDark = Color(0xFF0A0A0A)
```

**Step 2: Update Type.kt**

Replace `app/src/main/java/com/feelingmindful/awareness/ui/theme/Type.kt`:

```kotlin
package com.feelingmindful.awareness.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.5).sp
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp
    )
)
```

**Step 3: Update Theme.kt**

Replace `app/src/main/java/com/feelingmindful/awareness/ui/theme/Theme.kt`:

```kotlin
package com.feelingmindful.awareness.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Purple500,
    onPrimary = SurfaceLight,
    primaryContainer = Purple700,
    onPrimaryContainer = SurfaceLight,
    secondary = Gray400,
    onSecondary = Gray900,
    background = BackgroundDark,
    onBackground = SurfaceLight,
    surface = SurfaceDark,
    onSurface = SurfaceLight,
    surfaceVariant = Gray800,
    onSurfaceVariant = Gray300,
    error = Error,
    onError = SurfaceLight
)

private val LightColorScheme = lightColorScheme(
    primary = Purple500,
    onPrimary = SurfaceLight,
    primaryContainer = Purple600,
    onPrimaryContainer = SurfaceLight,
    secondary = Gray600,
    onSecondary = SurfaceLight,
    background = BackgroundLight,
    onBackground = Gray900,
    surface = SurfaceLight,
    onSurface = Gray900,
    surfaceVariant = Gray100,
    onSurfaceVariant = Gray600,
    error = Error,
    onError = SurfaceLight
)

@Composable
fun AwarenessTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

**Step 4: Commit**

```bash
git add -A
git commit -m "feat: add design system

- Purple primary color palette
- Custom typography scale
- Light and dark color schemes
- Dynamic color support"
```

---

### Task 11: Create Shared UI Components

**Files:**
- Create: `app/src/main/java/com/feelingmindful/awareness/ui/components/TaskCard.kt`
- Create: `app/src/main/java/com/feelingmindful/awareness/ui/components/MantraCard.kt`
- Create: `app/src/main/java/com/feelingmindful/awareness/ui/components/LoadingIndicator.kt`

**Step 1: Create TaskCard**

Create `app/src/main/java/com/feelingmindful/awareness/ui/components/TaskCard.kt`:

```kotlin
package com.feelingmindful.awareness.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.feelingmindful.awareness.domain.entities.TaskStatus
import com.feelingmindful.awareness.ui.theme.AwarenessTheme

@Composable
fun TaskCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    state: TaskStatus,
    showsCheckbox: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isEnabled = when {
        showsCheckbox -> state == TaskStatus.AVAILABLE || state == TaskStatus.COMPLETED
        else -> state.isActionable
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (state == TaskStatus.LOCKED) 0.5f else 1f)
            .clickable(enabled = isEnabled, onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = if (state == TaskStatus.COMPLETED) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = when {
                    state == TaskStatus.LOCKED -> Icons.Default.Lock
                    showsCheckbox && state == TaskStatus.COMPLETED -> Icons.Default.CheckCircle
                    showsCheckbox -> Icons.Outlined.Circle
                    else -> Icons.Default.ChevronRight
                },
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = when {
                    state == TaskStatus.COMPLETED -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

@Preview
@Composable
private fun TaskCardPreview() {
    AwarenessTheme {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            TaskCard(
                title = "Meditation",
                subtitle = "10 min guided",
                icon = Icons.Default.CheckCircle,
                state = TaskStatus.AVAILABLE,
                onClick = {}
            )
            TaskCard(
                title = "Cardio",
                subtitle = "30 min movement",
                icon = Icons.Default.CheckCircle,
                state = TaskStatus.COMPLETED,
                showsCheckbox = true,
                onClick = {}
            )
            TaskCard(
                title = "Meals",
                subtitle = "Mindful eating",
                icon = Icons.Default.CheckCircle,
                state = TaskStatus.LOCKED,
                onClick = {}
            )
        }
    }
}
```

**Step 2: Create MantraCard**

Create `app/src/main/java/com/feelingmindful/awareness/ui/components/MantraCard.kt`:

```kotlin
package com.feelingmindful.awareness.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.feelingmindful.awareness.domain.entities.Mantra
import com.feelingmindful.awareness.domain.entities.WindowType
import com.feelingmindful.awareness.ui.theme.AwarenessTheme

@Composable
fun MantraCard(
    mantra: Mantra?,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = !isLoading, onClick = onRefresh),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isLoading) "..." else (mantra?.text ?: ""),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
            )

            mantra?.attribution?.let { attribution ->
                Text(
                    text = "— $attribution",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Text(
                text = "Tap for a new mantra",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f),
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

@Preview
@Composable
private fun MantraCardPreview() {
    AwarenessTheme {
        MantraCard(
            mantra = Mantra(
                id = "1",
                text = "Be present in this moment.",
                windowType = WindowType.MORNING,
                attribution = "Thich Nhat Hanh"
            ),
            isLoading = false,
            onRefresh = {}
        )
    }
}
```

**Step 3: Create LoadingIndicator**

Create `app/src/main/java/com/feelingmindful/awareness/ui/components/LoadingIndicator.kt`:

```kotlin
package com.feelingmindful.awareness.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.feelingmindful.awareness.ui.theme.AwarenessTheme

@Composable
fun LoadingIndicator(
    message: String = "Loading...",
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

@Preview
@Composable
private fun LoadingIndicatorPreview() {
    AwarenessTheme {
        LoadingIndicator(message = "Loading your plan...")
    }
}
```

**Step 4: Commit**

```bash
git add -A
git commit -m "feat: add shared UI components

- TaskCard with checkbox and chevron variants
- MantraCard with tap-to-refresh
- LoadingIndicator"
```

---

## Phase 5: Features

*Remaining tasks for features, navigation, Health Connect, and playback service will follow the same pattern. Each task includes:*

- *Task 12: Create DailyPlanScreen and ViewModel*
- *Task 13: Create OnboardingScreen with Google Sign-In*
- *Task 14: Create Navigation and RootScreen*
- *Task 15: Create PlayerScreen and PlaybackService*
- *Task 16: Create ReflectionSheet*
- *Task 17: Add Health Connect integration*
- *Task 18: Add notification permissions*
- *Task 19: Add CLAUDE.md for Android project*
- *Task 20: Final integration and testing*

---

**Plan saved to `docs/plans/2026-01-17-android-native-mvp-implementation.md`**

This plan covers the foundational setup through Phase 4. The remaining feature tasks (12-20) follow the same bite-sized pattern. Ready to continue with those, or save this as the initial plan?
