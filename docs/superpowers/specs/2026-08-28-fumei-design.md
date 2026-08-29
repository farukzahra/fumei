# Fumei — Design Spec (MVP)

**Date:** 2026-08-28  
**Status:** Approved  
**Package:** `br.dev.faruk.fumei` (`fumei.faruk.dev.br`)

## Problem

Track how many times the user vaped per day, with timestamps, using a single tap. No accounts, no cloud, no complexity.

## Goals

- One-tap logging via a **"Fumei"** button
- Show **today's count** and **list of times** for today
- Persist data locally on device
- Publish to **Google Play Store** (public, free)

## Non-Goals (MVP)

- iOS / cross-platform
- User accounts or cloud sync
- Dose / nicotine strength / puff count per session
- Charts, weekly stats, reminders, widgets
- Undo / edit / delete entries (future)

## User Flow

1. User opens app → sees today's count and list of times
2. User taps **"Fumei"** → haptic feedback, count increments, new time appears at top of list
3. At midnight (local timezone) → count resets to 0, list shows empty state for new day
4. Historical data from previous days remains in DB but is not shown in MVP UI

## Architecture

```
┌─────────────────────────────────────┐
│           MainActivity              │
│         (Jetpack Compose UI)        │
│  - TodayCount                       │
│  - FumeiButton                      │
│  - TodayTimesList                   │
└──────────────┬──────────────────────┘
               │ ViewModel
               ▼
┌─────────────────────────────────────┐
│         MainViewModel               │
│  - uiState: TodayUiState            │
│  - onFumeiClick()                   │
└──────────────┬──────────────────────┘
               │ Repository
               ▼
┌─────────────────────────────────────┐
│         PuffRepository              │
└──────────────┬──────────────────────┘
               │ Room DAO
               ▼
┌─────────────────────────────────────┐
│  PuffEntity (id, timestamp)         │
│  SQLite via Room                    │
└─────────────────────────────────────┘
```

## Tech Stack

| Layer | Choice |
|-------|--------|
| Language | Kotlin 2.x |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM (ViewModel + StateFlow) |
| Database | Room (SQLite) |
| Min SDK | 26 (Android 8.0) |
| Target SDK | 35 |
| DI | None (manual construction in MVP) |
| Tests | JUnit 5 + Room in-memory tests |

## Data Model

```kotlin
@Entity(tableName = "puffs")
data class PuffEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long // epoch millis, local device time
)
```

**Query for today:** `WHERE timestamp >= startOfDay AND timestamp < endOfDay` using `LocalDate` boundaries in device timezone.

## UI Spec

### Screen: Home (single screen)

| Element | Behavior |
|---------|----------|
| Title | "Fumei" |
| Counter | Large text: "Hoje: N" |
| Button | Full-width, prominent, label "Fumei", triggers haptic |
| List | Today's times formatted as `HH:mm` (24h), newest first |
| Empty state | "Nenhum registro hoje" when count is 0 |

### Theme

- Material 3 dark theme by default
- Primary color: muted teal/green (health-tracking feel, not clinical)
- Large touch targets (min 48dp)

## Permissions

- **None required** for MVP (no internet, no sensors, no notifications)

## Play Store Requirements

- App name: **Fumei**
- Category: Health & Fitness (or Lifestyle)
- Free, no ads, no IAP
- Privacy policy URL required (simple static page: "all data stays on device")
- Content rating questionnaire (likely Everyone)
- Target API level 35

## Error Handling

- DB write failure: show Snackbar "Erro ao salvar", do not increment UI
- Empty list: show empty state text, not an error

## Testing Strategy

| Level | What |
|-------|------|
| Unit | `PuffDao` queries with in-memory Room |
| Unit | `PuffRepository.getTodayPuffs()` date boundary logic |
| Manual | Tap button, verify count and list update |
| Manual | Change device date to next day, verify reset |

## Future (Out of Scope)

- Undo last entry
- History by day / week charts
- Home screen widget
- Export CSV
- iOS port
