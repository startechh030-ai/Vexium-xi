# 🎮 Obris — Play. Compete. Win.

A skill-based mini gaming app for Android. Cyberpunk space theme, landscape-only, built with Jetpack Compose.

## Tech Stack

| Tech | Version | Purpose |
|------|---------|---------|
| Kotlin | 2.3.21 | Language |
| Jetpack Compose | BOM 2026.05.00 | UI |
| Hilt | 2.58 | Dependency Injection |
| Supabase-kt | 3.5.0 | Auth + Database |
| Ktor | 3.4.3 | HTTP (Supabase engine) |
| Coil 3 + SVG | 3.2.0 | Image/SVG loading |
| Room | 2.8.4 | Local database |
| DataStore | 1.1.7 | Preferences |
| Biometric | 1.1.0 | Fingerprint/Face |

## Architecture

- **Clean Architecture** — Presentation → Domain → Data
- **MVVM** — ViewModels + Compose state
- **Single Activity** — Compose Navigation
- **Dark theme only** — Cyberpunk space aesthetic
- **Landscape only** — Gaming oriented

## Project Structure

```
lux.obris.app/
├── MainActivity.kt
├── ObrisApp.kt
├── core/
│   ├── common/          # Constants, extensions, utilities
│   ├── components/      # Reusable UI (logo, loading)
│   ├── navigation/      # NavGraph, Screen routes, BottomBar
│   └── theme/           # Colors, typography, theme
├── data/local/          # DataStore preferences
├── di/                  # Hilt modules
└── feature/
    ├── auth/            # Google auth, session management
    ├── games/           # Game list + gameplay
    ├── home/            # Dashboard
    ├── profile/         # User profile
    ├── settings/        # App settings
    ├── splash/          # Splash screen
    └── welcome/         # Auth/welcome screen
```

## Auth Flow

```
App opens → Splash → Welcome (if not signed in) / Home (if signed in)
Welcome → Google Sign-In → Home
Logout → Welcome
```

## Setup

1. Clone the repo
2. Add your `vexium-release-key.keystore` to project root
3. Update `Constants.kt` with your Supabase URL/key
4. Update Google OAuth client with package `lux.obris.app`
5. Build & run

## CI/CD

GitHub Actions builds debug + release APKs on push to `main`/`develop`.
