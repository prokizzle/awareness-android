# Android Native MVP Design

## Overview

FeelingMindful Android app with feature parity to iOS, sharing the same Firebase backend. Built with Jetpack Compose and Material 3.

## Decisions

| Decision | Choice | Rationale |
|----------|--------|-----------|
| Architecture | Mirror iOS + Hilt | Same mental model across platforms, easier maintenance |
| Authentication | Google Sign-In only | Simplest, covers 99% of Android users |
| Health tracking | Full Health Connect | Matches iOS HealthKit experience |
| Audio playback | Media3/ExoPlayer | Modern API, background playback support |
| Min SDK | 30 (Android 11) | Health Connect requirement, modern APIs |

## Project Structure

```
app/src/main/java/com/feelingmindful/awareness/
├── di/                     # Hilt modules
│   ├── AppModule.kt        # Singletons (Firebase, repositories)
│   └── ServiceModule.kt    # Services
├── domain/
│   ├── entities/           # Data classes (DailyPlan, User, Meditation, etc.)
│   └── repositories/       # Repository interfaces
├── data/
│   ├── repositories/       # Firebase implementations
│   └── services/           # Business logic (DailyPlanService, etc.)
├── features/
│   ├── onboarding/         # Sign-in flow
│   ├── dailyplan/          # Main screen with tasks
│   ├── playback/           # Meditation player
│   ├── reflection/         # Journaling prompts
│   └── settings/           # (placeholder)
├── ui/
│   ├── theme/              # Colors, typography, shapes
│   └── components/         # Shared composables (TaskCard, MantraCard, etc.)
└── FeelingMindfulApp.kt    # Application class with Hilt
```

## Domain Entities

Entities mirror iOS exactly to map to the same Firestore documents:

```kotlin
// User & Progress
data class User(
    val id: String,
    val email: String,
    val displayName: String?,
    val photoUrl: String?,
    val authProvider: AuthProvider
)

data class UserProgress(
    val visitsCount: Int = 0,
    val streakDays: Int = 0,
    val completedMeditationIds: List<String> = emptyList(),
    val currentCourseId: String? = null,
    val currentCourseMeditationIndex: Int = 0
)

// Daily Plan
data class DailyPlan(
    val id: String,               // "YYYY-MM-DD"
    val userId: String,
    val tasks: DailyTasks,
    val mantraId: String?,
    val meditationId: String?,
    val allCompleted: Boolean = false,
    val completedAt: Instant? = null
)

data class DailyTasks(
    val meditation: TaskState,
    val reflection: TaskState,
    val cardio: TaskState,
    val meals: TaskState
)

data class TaskState(
    val status: TaskStatus,
    val completedAt: Instant? = null,
    val minutes: Int? = null,        // Cardio-specific
    val autoCompleted: Boolean = false
)

enum class TaskStatus { LOCKED, AVAILABLE, IN_PROGRESS, COMPLETED }

// Content (read-only from Firestore)
data class Meditation(
    val id: String,
    val title: String,
    val description: String,
    val duration: Int,
    val audioUrl: String?,
    val hasChunks: Boolean,
    val prompts: List<ReflectionPrompt>,
    val courseId: String?,
    val orderInCourse: Int?,
    val themes: List<String>,
    val teacherId: String?,
    val publishedAt: Instant
)

data class Course(
    val id: String,
    val title: String,
    val description: String,
    val meditationIds: List<String>,
    val publishedAt: Instant,
    val isIntro: Boolean
)

data class Mantra(
    val id: String,
    val text: String,
    val windowType: WindowType,
    val active: Boolean,
    val attribution: String? = null
)

enum class WindowType { MORNING, AFTERNOON, EVENING }

data class ReflectionPrompt(
    val id: String,
    val question: String,
    val order: Int
)

data class ReflectionAnswer(
    val promptId: String,
    val question: String,
    val answer: String
)
```

## Repository Interfaces

```kotlin
interface AuthRepository {
    val currentUser: Flow<User?>
    suspend fun signInWithGoogle(idToken: String): User
    suspend fun signOut()
    suspend fun deleteAccount()
}

interface DailyPlanRepository {
    suspend fun getDailyPlan(userId: String, date: LocalDate): DailyPlan?
    suspend fun saveDailyPlan(userId: String, plan: DailyPlan)
}

interface MeditationRepository {
    suspend fun getMeditation(id: String): Meditation
    suspend fun getCourse(id: String): Course
    suspend fun getIntroCourse(): Course?
    suspend fun getUnplayedMeditations(excludeIds: List<String>, limit: Int): List<Meditation>
    suspend fun getSystemPrompts(themes: List<String>): List<ReflectionPrompt>
}

interface UserRepository {
    suspend fun getProfile(userId: String): UserProfile?
    suspend fun getProgress(userId: String): UserProgress
    suspend fun updateProgress(userId: String, progress: UserProgress)
}

interface MantraRepository {
    suspend fun getRandomMantra(windowType: WindowType): Mantra?
}
```

## Service Interfaces

```kotlin
interface DailyPlanService {
    suspend fun getTodaysPlan(userId: String): DailyPlan
    suspend fun completeMeditation(userId: String, plan: DailyPlan): DailyPlan
    suspend fun completeReflection(userId: String, plan: DailyPlan, answers: List<ReflectionAnswer>): DailyPlan
    suspend fun completeCardio(userId: String, plan: DailyPlan, minutes: Int, autoCompleted: Boolean): DailyPlan
    suspend fun uncompleteCardio(userId: String, plan: DailyPlan): DailyPlan
    suspend fun completeMeals(userId: String, plan: DailyPlan): DailyPlan
    suspend fun uncompleteMeals(userId: String, plan: DailyPlan): DailyPlan
}

interface HealthConnectService {
    val isAvailable: Boolean
    suspend fun requestPermissions(): Boolean
    suspend fun getTodayWorkoutMinutes(): Int
    suspend fun saveMindfulnessSession(durationSeconds: Int)
}

interface NotificationService {
    suspend fun requestPermission(): Boolean
    suspend fun scheduleDailyReminder(hour: Int, minute: Int)
    suspend fun cancelAllReminders()
}
```

## Navigation

Single Activity with Compose Navigation:

```kotlin
sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object DailyPlan : Screen("daily_plan")
    object Player : Screen("player/{meditationId}")
    object Reflection : Screen("reflection")
}

@Composable
fun FeelingMindfulNavHost(navController: NavHostController) {
    NavHost(navController, startDestination = Screen.Onboarding.route) {
        composable(Screen.Onboarding.route) { OnboardingScreen(...) }
        composable(Screen.DailyPlan.route) { DailyPlanScreen(...) }
        composable(Screen.Player.route) { PlayerScreen(...) }
        composable(Screen.Reflection.route) { ReflectionSheet(...) }
    }
}
```

## Key Screens

| Screen | Description |
|--------|-------------|
| `OnboardingScreen` | Google Sign-In button, intro carousel |
| `DailyPlanScreen` | Main screen with greeting, mantra, 4 task cards |
| `PlayerScreen` | Full-screen meditation player with ExoPlayer |
| `ReflectionSheet` | Bottom sheet with journaling prompts |

## Shared Components

```kotlin
@Composable
fun TaskCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    state: TaskStatus,
    showsCheckbox: Boolean = false,
    onClick: () -> Unit
)

@Composable
fun MantraCard(
    mantra: Mantra?,
    isLoading: Boolean,
    onRefresh: () -> Unit
)
```

## External Integrations

### Firebase

Uses existing Firebase project (same as iOS):
- `google-services.json` from Firebase Console
- Dependencies: `firebase-auth-ktx`, `firebase-firestore-ktx`, `firebase-storage-ktx`

### Google Sign-In

```kotlin
// Uses Credential Manager API (modern replacement for legacy GoogleSignIn)
val credentialManager = CredentialManager.create(context)
val googleIdOption = GetGoogleIdOption.Builder()
    .setServerClientId(WEB_CLIENT_ID)
    .build()
```

### Audio Playback (Media3/ExoPlayer)

```kotlin
val player = ExoPlayer.Builder(context).build()
player.setMediaItem(MediaItem.fromUri(audioUrl))

// Foreground service for background playback
class PlaybackService : MediaSessionService() { ... }
```

### Health Connect

```kotlin
val healthConnectClient = HealthConnectClient.getOrCreate(context)

// Read workout data
val response = healthConnectClient.readRecords(
    ReadRecordsRequest(
        recordType = ExerciseSessionRecord::class,
        timeRangeFilter = TimeRangeFilter.between(startOfDay, now)
    )
)

// Write mindfulness minutes
healthConnectClient.insertRecords(
    listOf(MindfulnessSessionRecord(...))
)
```

## Error Handling

```kotlin
// Sealed class for UI state
data class DailyPlanUiState(
    val isLoading: Boolean = false,
    val dailyPlan: DailyPlan? = null,
    val mantra: Mantra? = null,
    val error: String? = null
)

// ViewModel catches exceptions
fun loadTodaysPlan() {
    viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true)
        try {
            val plan = dailyPlanService.getTodaysPlan(userId)
            _uiState.value = _uiState.value.copy(dailyPlan = plan, isLoading = false)
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(error = e.message, isLoading = false)
        }
    }
}
```

## Testing Strategy

| Layer | Approach |
|-------|----------|
| Entities | Unit tests (pure Kotlin) |
| Repositories | Fake implementations for testing |
| ViewModels | Unit tests with fakes, Turbine for Flow testing |
| UI | Compose UI tests with `createComposeRule()` |

### Fake Pattern

```kotlin
class FakeDailyPlanRepository : DailyPlanRepository {
    var planToReturn: DailyPlan? = null
    override suspend fun getDailyPlan(...) = planToReturn
}
```

### Preview Support

Each screen has `@Preview` composables with hardcoded data. No dependency on ViewModels in previews.

## Dependencies

```kotlin
// Firebase
implementation(platform("com.google.firebase:firebase-bom:33.0.0"))
implementation("com.google.firebase:firebase-auth-ktx")
implementation("com.google.firebase:firebase-firestore-ktx")
implementation("com.google.firebase:firebase-storage-ktx")

// Hilt
implementation("com.google.dagger:hilt-android:2.51")
kapt("com.google.dagger:hilt-compiler:2.51")
implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

// Media3/ExoPlayer
implementation("androidx.media3:media3-exoplayer:1.3.0")
implementation("androidx.media3:media3-session:1.3.0")
implementation("androidx.media3:media3-ui:1.3.0")

// Health Connect
implementation("androidx.health.connect:connect-client:1.1.0-alpha07")

// Credential Manager (Google Sign-In)
implementation("androidx.credentials:credentials:1.3.0")
implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
implementation("com.google.android.libraries.identity.googleid:googleid:1.1.0")

// Serialization
implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
```

## Permissions

```xml
<!-- AndroidManifest.xml -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK" />

<!-- Health Connect permissions declared at runtime -->
```
