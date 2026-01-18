# FeelingMindful Android

Android native implementation of the FeelingMindful mindfulness app using Kotlin and Jetpack Compose.

## Project Structure

```
app/src/main/java/com/feelingmindful/awareness/
├── di/                     # Hilt dependency injection modules
├── domain/
│   ├── entities/           # Data classes (User, DailyPlan, Meditation, etc.)
│   ├── repositories/       # Repository interfaces
│   └── services/           # Service interfaces
├── data/
│   ├── repositories/       # Firebase repository implementations
│   └── services/           # Service implementations (Health Connect, Notifications)
├── features/
│   ├── dailyplan/          # Main screen with daily tasks
│   ├── onboarding/         # Google Sign-In flow
│   ├── playback/           # Meditation player
│   └── reflection/         # Journaling prompts
├── navigation/             # Compose Navigation setup
├── ui/
│   ├── theme/              # Material 3 design system
│   └── components/         # Shared composables (TaskCard, MantraCard, etc.)
├── MainActivity.kt         # Entry point
└── FeelingMindfulApp.kt    # Hilt Application class
```

## Tech Stack

- **Language**: Kotlin 2.1
- **UI**: Jetpack Compose with Material 3
- **DI**: Hilt
- **Navigation**: Compose Navigation
- **Backend**: Firebase (Auth, Firestore, Storage)
- **Health**: Health Connect API
- **Media**: Media3/ExoPlayer
- **Serialization**: kotlinx.serialization

## Build Commands

```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease

# Run tests
./gradlew test

# Lint check
./gradlew lint
```

## Configuration

### Required Setup

1. **Firebase**: Place `google-services.json` in `app/` directory (gitignored)

2. **Google Sign-In**: Set `GOOGLE_WEB_CLIENT_ID` via:
   - Environment variable (CI/CD)
   - Or `local.properties` file (local dev, gitignored)

### Environment Variables

| Variable | Description |
|----------|-------------|
| `GOOGLE_WEB_CLIENT_ID` | OAuth web client ID from Firebase Console |

## Architecture

The app follows Clean Architecture with three layers:

1. **Domain**: Pure Kotlin entities, repository interfaces, service interfaces
2. **Data**: Firebase implementations, Health Connect, notifications
3. **UI**: Compose screens, ViewModels, navigation

### Key Patterns

- **Unidirectional Data Flow**: ViewModel exposes StateFlow, UI observes
- **Repository Pattern**: Abstract data sources behind interfaces
- **Dependency Injection**: Hilt modules bind implementations

## Firebase Collections

- `users/{userId}/data/progress` - User progress and streaks
- `users/{userId}/dailyPlans/{date}` - Daily task completion
- `meditations/{id}` - Meditation content
- `courses/{id}` - Course groupings
- `mantras/{id}` - Daily mantras
- `systemPrompts/{id}` - Reflection prompts

## Daily Plan Flow

1. User opens app → loads/creates today's DailyPlan
2. Tasks unlock sequentially: Meditation → Reflection → Cardio → Meals
3. Completing all tasks increments streak
4. Health Connect auto-completes Cardio if workout detected

## Health Connect

Reads `ExerciseSessionRecord` to detect workouts and auto-complete cardio task.

Required permissions declared in manifest:
- `health.permission.READ_EXERCISE`

## Testing

- Unit tests: `app/src/test/`
- UI tests: `app/src/androidTest/`
- Run with: `./gradlew test` or `./gradlew connectedAndroidTest`
