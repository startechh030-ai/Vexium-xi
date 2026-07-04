# 🎮 Obris — Play. Compete. Win.

A skill-based mini gaming app for Android. Built with Jetpack Compose, landscape-only, powered by Supabase.

## Brand Colors
- **Primary:** Electric Orange `#FF8C00`
- **Secondary:** Electric Cyan `#00E5FF`
- **Tertiary:** Deep Purple `#9C27B0`
- **Background:** Deep warm dark `#0A0608`

## Splash Screen
1. **Axiom Studio Intro** (2s) — diamond logo slides left, "AXIOM" text fades in
2. **Obris Video** (5s) — auto-selects best aspect ratio (16:9, 4:3, 5:4, 1:1)

## Tech Stack
| Tech | Version | Purpose |
|------|---------|---------|
| Kotlin | 2.3.21 | Language |
| Jetpack Compose | BOM 2026.05.00 | UI |
| Hilt | 2.58 | DI |
| Supabase-kt | 3.5.0 | Auth + Database |
| Media3/ExoPlayer | 1.10.1 | Video splash |
| Coil 3 + SVG | 3.2.0 | Image loading |
| Ktor | 3.4.3 | HTTP engine |

## Architecture
- Clean Architecture + MVVM
- Single Activity, Compose Navigation
- Dark theme only — vibrant orange/cyan/purple
- Landscape only

## Auth Flow
```
Splash (Axiom → Obris video) → Welcome → Google Sign-In → Home
```

## Project Structure
```
lux.obris.app/
├── MainActivity.kt              # Entry point, landscape locked
├── ObrisApp.kt                   # @HiltAndroidApp
├── core/
│   ├── common/                   # Constants, extensions, utilities
│   ├── components/               # ObrisLogo, FullScreenLoading, ModalLoading
│   ├── navigation/               # NavGraph, Screen routes, BottomBar
│   └── theme/                    # Colors (orange/cyan/purple), Typography
├── data/local/                   # DataStore preferences
├── di/                           # Hilt modules (App, Network, Supabase)
└── feature/
    ├── auth/                     # Google auth, session management
    ├── games/                    # Game list
    ├── home/                     # Dashboard
    ├── profile/                  # User profile
    ├── settings/                 # App settings
    ├── splash/                   # Axiom intro + Obris video
    └── welcome/                  # Auth screen with energy particles

res/raw/                          # Splash videos (16:9, 4:3, 5:4, 1:1)
assets/logo/                      # Obris SVG logo
```

## Setup
1. Clone repo
2. Place splash videos in `res/raw/` (splash_16x9.mp4, splash_4x3.mp4, etc.)
3. Update `Constants.kt` with Supabase credentials
4. Update Google OAuth client for package `lux.obris.app`
5. Build & run

## CI/CD
GitHub Actions: lint → test → debug APK → release APK on push to `main`/`develop`.
