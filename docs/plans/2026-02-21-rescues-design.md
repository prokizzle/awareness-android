# Rescues: Attention Anchors for Meditation Sessions

## Overview

Rescues are short audio clips (5–15 seconds) that gently redirect attention during meditation. They replace portions of silence gaps between chunks in the PlaybackCueSheet, helping meditators — especially beginners — return focus when the mind wanders.

Examples: "Gently bring your attention back to your breath," "Notice where your mind has gone and return to the present moment."

## Core Design Decisions

- **Insertion model**: Rescues replace portions of existing silence gaps between body chunks. No new timeline slots — they layer into the current silence distribution.
- **Frequency**: Driven by user skill level. Beginner ~80% of eligible gaps, Intermediate ~40%, Advanced ~10%.
- **Content source**: Shared global library (~20–30 clips). No per-teacher or per-course libraries.
- **Selection**: Random, no repeats within a session.
- **Eligible gap**: Silence period must be ≥ rescue duration + 4s (2s padding before and after the clip).

## Data Model

### New Firestore Collection: `/rescues/{rescueId}`

| Field | Type | Description |
|-------|------|-------------|
| `id` | String | Document ID |
| `title` | String | Display name (e.g., "Breath anchor gentle") |
| `audioUrl` | String | Firebase Storage download URL |
| `durationMs` | Int | Clip length in milliseconds |
| `tags` | [String] | Optional metadata for future use |
| `publishedAt` | Timestamp | Publication date |

### Firebase Storage Path

`/rescues/{rescueId}.mp3`

### UserProgress Addition

Add `skillLevel` field to existing `UserProgress` entity:

```
enum SkillLevel { BEGINNER, INTERMEDIATE, ADVANCED }
```

Set during onboarding or adjustable in settings.

## PlaybackCueSheet Integration

The existing `PlaybackCueSheet` builds: `[Chunk] [Silence] [Chunk] [Silence] [Chunk]`.

With rescues, the silence algorithm adds a step:

1. Calculate total silence needed (unchanged)
2. Distribute silence across gaps (unchanged)
3. **New**: For each gap, roll against skill-level density
   - If selected, pick a random rescue (no repeats)
   - Only if gap ≥ rescue duration + 4s
4. Build cue entries: `[Chunk] [Silence 2s] [Rescue clip] [Remaining silence] [Chunk]`

`CueEntry` gains a new variant for rescue entries with a separate `audioUrl` pointing to the rescue clip.

## ExtendedPlaybackEngine Changes

- Use a secondary audio player for rescue clips (separate from the meditation AVPlayer/ExoPlayer)
- When the virtual timeline hits a rescue cue entry, play the rescue clip
- After the clip finishes, resume silence countdown
- Seek behavior: if user seeks past a rescue, skip it

## Studio (MeditationProcessor) Changes

### Rescue Library Management (new view)

- Upload rescue audio clips (MP3/M4A)
- Set title, preview playback, delete
- Publishes to `/rescues` collection and `/rescues/{id}.mp3` in Storage
- No per-meditation assignment needed

### AI Rescue Detection in Processing Pipeline

The AI analysis step (OpenRouterAnalysisService) is extended to detect rescue sections:

**Prompt changes**: The analysis prompt instructs the AI to identify sections where the teacher redirects attention (e.g., "bring your attention back to your breath", "notice where your mind has wandered", "gently return to the present moment"). These are flagged with a new `isRescue: true` field on `SectionSuggestion`.

**Schema changes**: `SectionSuggestion` gains an `isRescue: Bool` field. The JSON schema for structured output is updated to include this field.

**Review step**: Rescue-flagged sections appear with a visual indicator in the ReviewEditor. The creator can confirm or dismiss the rescue flag per section.

**Publish step**: When publishing, sections flagged as rescues are:
1. Saved as normal chunks in the meditation (preserving the teacher's original placement)
2. **Also** saved to the `/rescues` collection as standalone clips for the global rescue library

This means a single recording session can populate both the meditation content and the rescue library simultaneously. The rescue audio gets split out, uploaded to `/rescues/{id}.mp3` in Storage, and a Firestore document created in `/rescues`.

**Deduplication**: Before saving a rescue, check the existing library for clips with very similar titles or from the same source meditation. Surface a warning to the creator if a potential duplicate is detected.

## Android Player Changes

1. **Fetch rescues**: Load `/rescues` collection at app startup or on first playback (small dataset, cache locally)
2. **Domain entity**: New `Rescue` data class in `domain/entities/`
3. **Repository**: `RescueRepository` interface + `FirestoreRescueRepository`
4. **PlayerViewModel**: When building playback for chunked meditations, apply rescue insertion based on `UserProgress.skillLevel`
5. **ExoPlayer**: Use playlist concatenation to interleave rescue clips with silence and meditation chunks

## MVP Scope (SAT-55)

### In scope
- Rescue Firestore collection + Storage path
- Studio: Upload/manage rescue clips
- Studio: AI detection of rescue sections during analysis + dual save on publish
- Android: Fetch rescues, insert into playback based on skill level
- User skill level field in `UserProgress`

### Out of scope (future)
- Themed rescue matching (match rescue theme to meditation theme)
- Per-teacher or per-course rescue libraries
- Studio preview with rescue simulation
- Analytics on rescue effectiveness
- iOS native player rescue support (when iOS app is built)

## Notes

- The Flutter app (shanks-mindfulness-app) is deprecated. Only native apps are supported.
- The Amplify/admin portal schema does not need changes — rescues are managed through Studio (MeditationProcessor).
