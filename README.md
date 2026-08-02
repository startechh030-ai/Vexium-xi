# 🎮 OBRIS — Beyond The Ruins

> A skill-based competitive mini gaming app for Android.
> Landscape-only. Full immersive. Built with Jetpack Compose + Supabase.

---

## 📋 Table of Contents

- [Overview](#overview)
- [App Flow](#app-flow)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Features Built](#features-built)
- [Supabase Backend](#supabase-backend)
- [Build & CI/CD](#build--cicd)
- [Full Screen Implementation](#full-screen-implementation)
- [Brand & Theme](#brand--theme)
- [What Was Built Previously (Vexium)](#what-was-built-previously-vexium)
- [Next Steps](#next-steps)
- [Setup Guide](#setup-guide)

---

## Overview

Obris is a skill-based mini gaming app designed for the Google Play Store. Players compete in quick mini games, earn in-app currency, and climb leaderboards. The app uses in-app purchases via Google Play Billing — no real-money gambling, no financial liability.

**Key decisions:**
- **Play Store only** — legitimate distribution, no gambling license needed
- **In-app purchases** — cosmetics, battle passes, premium games (via Google Play Billing)
- **Landscape only** — full immersive gaming experience
- **Dark theme only** — cyberpunk gaming aesthetic with orange/cyan/purple accents

---

## App Flow

```
┌─────────────────────────────────────────────────────────┐
│                    USER OPENS APP                         │
│                         │                                │
│                    Pure Black Screen                      │
│                         │                                │
│               ┌─────────▼──────────┐                     │
│               │   SPLASH (2s)      │                     │
│               │ Obris logo + glitch│                     │
│               │ + sound effect     │                     │
│               └─────────┬──────────┘                     │
│                         │                                │
│               ┌─────────▼──────────┐                     │
│               │  LOADING SCREEN 1  │                     │
│               │ Background art     │                     │
│               │ Progress bar (3s)  │                     │
│               │ Status messages    │                     │
│               └─────────┬──────────┘                     │
│                         │                                │
│              ┌──────────▼──────────┐                     │
│              │  Already signed in? │                     │
│              └──┬───────────────┬──┘                     │
│             YES │               │ NO                     │
│                 │    ┌──────────▼──────────┐              │
│                 │    │   WELCOME SCREEN    │              │
│                 │    │ Background art      │              │
│                 │    │ AuthLayout overlay  │              │
│                 │    │ Google/Email/Guest  │              │
│                 │    └──────────┬──────────┘              │
│                 │               │ (auth)                  │
│              ┌──▼───────────────▼──┐                     │
│              │  LOADING SCREEN 2   │                     │
│              │  Different bg (2.5s)│                     │
│              └─────────┬───────────┘                     │
│                        │                                 │
│              ┌─────────▼──────────┐                      │
│              │      HOME          │                      │
│              │  Games / Profile   │                      │
│              └────────────────────┘                      │
└─────────────────────────────────────────────────────────┘
```

---

## Tech Stack

| Technology | Version | Purpose |
|-----------|---------|---------|
| **Kotlin** | 2.3.21 | Language |
| **Jetpack Compose** | BOM 2026.05.00 | UI Framework |
| **Hilt** | 2.58 | Dependency Injection |
| **Supabase-kt** | 3.5.0 | Auth + Postgrest |
| **Ktor** | 3.4.3 | HTTP Engine (for Supabase) |
| **Coil 3 + SVG** | 3.2.0 | Image & SVG Loading |
| **Room** | 2.8.4 | Local Database (ready, not yet used) |
| **DataStore** | 1.1.7 | Preferences/Session Storage |
| **KSP** | 2.3.9 | Annotation Processing |
| **Compose Navigation** | 2.9.7 | Type-safe Navigation |
| **Kotlinx Serialization** | 1.10.0 | JSON + Nav route serialization |
| **AppCompat** | 1.7.0 | Activity base class |
| **SplashScreen API** | 1.0.1 | Override Android 12+ default splash |
| **AGP** | 8.13.2 | Android Gradle Plugin |
| **Gradle** | 8.13 | Build System |

---

## Architecture

```
┌──────────────────────────────────────────────┐
│              PRESENTATION LAYER               │
│  Jetpack Compose Screens + ViewModels         │
│  Screens, Components, Navigation, Theme       │
├──────────────────────────────────────────────┤
│                DOMAIN LAYER                   │
│  (Planned — Use Cases, Business Logic)        │
├──────────────────────────────────────────────┤
│                 DATA LAYER                    │
│  AuthRepository, SettingsRepository           │
│  Supabase (Remote), DataStore (Local)         │
│  Room (Local DB — ready, not yet used)        │
└──────────────────────────────────────────────┘
```

- **Clean Architecture** — Presentation → Domain → Data
- **MVVM** — ViewModels expose StateFlow, Compose observes
- **Single Activity** — `MainActivity` handles everything via Compose Navigation
- **Feature-based modules** — each feature is self-contained under `feature/`
- **Hilt DI** — all dependencies injected via modules in `di/`

---

## Project Structure

```
lux.obris.app/
│
├── MainActivity.kt                    # Entry point — landscape, full immersive
├── ObrisApp.kt                        # @HiltAndroidApp Application class
│
├── core/
│   ├── common/
│   │   ├── Constants.kt               # Supabase URL, keys, app constants
│   │   ├── Extensions.kt              # Utility extension functions
│   │   ├── Resource.kt                # Loading/Success/Error sealed class
│   │   └── UiEvent.kt                 # One-time UI events
│   │
│   ├── components/
│   │   ├── FullScreenLoading.kt       # Full-screen auth loading overlay
│   │   ├── GamefyLoading.kt           # Hexagonal spinner with orbiting particles
│   │   ├── ModalLoading.kt            # Semi-transparent modal loading
│   │   └── ObrisLogo.kt               # SVG logo loader via Coil
│   │
│   ├── navigation/
│   │   ├── BottomNavBar.kt            # Bottom nav — Home, Games, Profile
│   │   ├── NavGraph.kt                # Full navigation graph + auth flow
│   │   └── Screen.kt                  # Type-safe route definitions
│   │
│   └── theme/
│       ├── Color.kt                   # Brand colors — orange/cyan/purple palette
│       ├── Theme.kt                   # Dark-only Material3 theme
│       └── Type.kt                    # Typography definitions
│
├── data/
│   └── local/
│       └── PreferencesManager.kt      # DataStore — user session, preferences
│
├── di/
│   ├── AppModule.kt                   # App-wide Hilt module
│   ├── NetworkModule.kt               # Retrofit/OkHttp (ready, not yet used)
│   └── SupabaseModule.kt              # Supabase client + Auth + Postgrest + ComposeAuth
│
└── feature/
    ├── auth/
    │   ├── data/
    │   │   └── AuthRepository.kt      # Supabase auth — sign in/out, session
    │   └── presentation/
    │       ├── AuthLayout.kt           # Auth buttons overlay — Google/Email/Guest/More
    │       └── AuthViewModel.kt        # Auth state management, Google sign-in flow
    │
    ├── games/
    │   └── presentation/
    │       └── GamesScreen.kt          # Game list (placeholder)
    │
    ├── home/
    │   └── presentation/
    │       └── HomeScreen.kt           # Dashboard with game cards + logout
    │
    ├── loading/
    │   └── presentation/
    │       └── LoadingScreen.kt        # Reusable loading — bg image, progress, scan line
    │
    ├── profile/
    │   └── presentation/
    │       └── ProfileScreen.kt        # User profile (placeholder)
    │
    ├── settings/
    │   ├── data/
    │   │   └── SettingsRepository.kt   # DataStore-backed settings
    │   └── presentation/
    │       ├── GeneralSettingsScreen.kt # Settings UI
    │       └── SettingsViewModel.kt     # Settings state
    │
    ├── splash/
    │   └── presentation/
    │       └── SplashScreen.kt         # Cyberpunk glitch splash — logo + sound
    │
    └── welcome/
        └── presentation/
            └── WelcomeScreen.kt        # Welcome — bg image + AuthLayout overlay

Assets:
├── splash/obris.png                    # Obris logo for splash screen
├── screens/
│   ├── loading_bg_1.jpg                # Loading screen 1 background
│   └── loading_bg_2.jpg                # Loading screen 2 background
└── logo/dark_text.svg                  # SVG brand wordmark

Resources:
├── res/raw/glitch.mp3                  # Splash screen glitch sound effect
├── res/drawable/ic_google.xml          # Google icon (vector)
├── res/drawable/ic_email.xml           # Email icon (vector)
└── res/values/themes.xml               # App themes (dark, startup)
```

---

## Features Built

### ✅ Splash Screen
- **Cyberpunk glitch effect** — horizontal slice displacement, chromatic RGB artifacts
- Loads `obris.png` from assets, plays `glitch.mp3` simultaneously
- Sound and visual start at the exact same frame (parallel coroutines)
- 1s forward glitch + 1s reverse settle + 400ms fade out
- Pure black background — no Android icon flash
- Uses SplashScreen API to override Android 12+ default (transparent icon, instant exit)

### ✅ Loading Screens (Reusable)
- Full-bleed background image from `assets/screens/`
- Progress bar at bottom-left (40% width), ice blue → white gradient
- Monospace status text with **scan line animation** (vertical `|` sweeps across)
- Transparent border edges on text
- Dark gradient overlay at bottom for readability
- Configurable: different background per screen, custom messages, custom duration
- Loading Screen 1: 3 seconds, Loading Screen 2: 2.5 seconds

### ✅ Auth System (AuthLayout)
- **Separate composable** — easy to edit independently
- Transparent overlay sits on bottom 40% of screen
- **Circuit grid lines** — dashed neon lines along edges with animated moving dot
- **Google button** — wide, white, cut corner shape (cyberpunk sharp edges)
- **Email | Guest | More** — three equal buttons below in ice blue/dark
- Ice blue + white + cool grey palette
- Legal text at bottom

### ✅ Google Sign-In
- Native Android Credential Manager via Supabase ComposeAuth
- Google account picker → ID token → Supabase session
- Auto-navigate to Loading Screen 2 → Home on success
- Existing users skip auth on return (session persisted)

### ✅ Navigation
- Type-safe routes with Kotlin Serialization
- Splash → Loading1 → Welcome/Home → Loading2 → Home
- Bottom nav: Home | Games | Profile
- Scaffold only on main tabs — splash/loading/welcome are true full screen (no padding)

### ✅ Full Screen / Immersive Mode
- `FLAG_FULLSCREEN` + `FLAG_LAYOUT_NO_LIMITS` before `super.onCreate()`
- `WindowInsetsCompat.Type.statusBars() or navigationBars()` hidden
- `BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE` — swipe to peek, auto-hides
- Fallback `SYSTEM_UI_FLAG_IMMERSIVE_STICKY` for Android < 11
- `requestedOrientation = SCREEN_ORIENTATION_LANDSCAPE` in code + manifest
- Theme: `android:windowFullscreen=true`, black `windowBackground`
- Every pixel is usable game space

### ✅ Gamified Loading (Module Loading)
- **Hexagonal spinner** — outer hex rotates clockwise, inner counter-rotates
- **3 orbiting particles** with fade trails at different phase offsets
- Center pulse dot
- Semi-transparent black overlay
- Monospace status text
- Used for in-app operations (auth transitions, data loading)

### ✅ Theme System
- Dark-only Material3 color scheme
- Brand orange (`#FF8C00`), cyan (`#00E5FF`), purple (`#9C27B0`)
- Deep warm dark background (`#0A0608`)
- Surface variants with warm undertones
- Full color palette for all Material3 slots

### ✅ CI/CD
- GitHub Actions workflow on push to `main`/`develop`
- Lint → Unit Tests → Build Debug APK → Build Release APK
- Both APKs signed with release keystore (committed to private repo)
- Artifacts uploaded with 30-day retention

---

## Supabase Backend

### Database Tables
| Table | Purpose | Status |
|-------|---------|--------|
| `profiles` | User profiles — username, avatar, referral code | ✅ Deployed |
| `user_ranks` | XP, rank level, rank name | ✅ Deployed |
| `device_sessions` | Device verification — HMAC hash, session tokens | ✅ SQL Written |

### Edge Functions
| Function | Purpose | Status |
|----------|---------|--------|
| `verify-session` | Full device verification — HMAC-SHA256, issues session token | ✅ Written |
| `validate-token` | Fast token check for subsequent requests | ✅ Written |

### Auth Providers
- **Google** — native Android sign-in via Credential Manager ✅
- **Email** — placeholder, not yet implemented
- **Guest** — skips auth, goes to home ✅

### Security (Designed, Partially Deployed)
- JWT auth via Supabase Auth with auto-refresh
- HMAC-SHA256 device hash (secret lives only in Edge Function env var)
- Short-lived session tokens (24h) stored locally
- RLS policies on all tables

---

## Build & CI/CD

### GitHub Actions Workflow (`.github/workflows/build.yml`)
```
Push to main/develop
    │
    ▼
☕ JDK 17 + 🐘 Gradle Setup
    │
    ▼
🔍 Lint Check → 📋 Upload Report
    │
    ▼
🧪 Unit Tests → 📋 Upload Report
    │
    ▼
🔨 Build Debug APK (release-signed)
    │
    ▼
🔨 Build Release APK
    │
    ▼
📦 Upload Both APKs as Artifacts
```

### Signing
- Release keystore committed to private repo (`vexium-release-key.keystore`)
- Debug builds also signed with release key (consistent SHA-1 for Google OAuth)
- Key alias: `vexium`, SHA-1 registered in Google Cloud Console

---

## Full Screen Implementation

```kotlin
// Before super.onCreate() — prevents any system UI flash
window.addFlags(FLAG_FULLSCREEN or FLAG_KEEP_SCREEN_ON or FLAG_LAYOUT_NO_LIMITS)

// Modern API
WindowCompat.setDecorFitsSystemWindows(window, false)
WindowInsetsControllerCompat(window, window.decorView).let {
    it.hide(Type.statusBars() or Type.navigationBars())
    it.systemBarsBehavior = BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
}

// Fallback for Android < 11
window.decorView.systemUiVisibility =
    SYSTEM_UI_FLAG_IMMERSIVE_STICKY or SYSTEM_UI_FLAG_FULLSCREEN or
    SYSTEM_UI_FLAG_HIDE_NAVIGATION or SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
    SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
```

---

## Brand & Theme

| Color | Hex | Usage |
|-------|-----|-------|
| Electric Orange | `#FF8C00` | Primary accent, splash glow |
| Electric Cyan | `#00E5FF` | Secondary accent |
| Deep Purple | `#9C27B0` | Tertiary accent |
| Ice Blue | `#7DD3FC` | Auth buttons, loading bar, scan line |
| Warm Dark | `#0A0608` | Background |
| Cool White | `#F0F4F8` | Google button, text |

### Logo
- Splash: `obris.png` — shattered chrome O mark with blade shard
- Brand wordmark: `dark_text.svg` — loaded via Coil SVG decoder

---

## What Was Built Previously (Vexium)

This project was originally called **Vexium** — a crypto/NFT gambling app. It was refactored into **Obris** (a legitimate Play Store gaming app) after evaluating legal and business risks.

### Carried over from Vexium:
- Supabase integration (auth, database, edge functions)
- Google Sign-In implementation
- Clean Architecture + MVVM pattern
- CI/CD pipeline
- DataStore preferences
- Hilt DI setup
- Version catalog (`libs.versions.toml`)

### Removed from Vexium:
- PIN/biometric authentication (no financial data to protect)
- Telegram bot auth flow
- NFT/Trade screens
- Crypto wallet features
- Security edge functions (HMAC device verification)
- Portrait mode
- Light theme

---

## Next Steps

### Immediate (UI/UX)
- [ ] Polish splash screen glitch timing
- [ ] Design and implement Home Screen with real game cards
- [ ] Build Games Screen with actual mini game list
- [ ] Design Profile Screen with stats, avatar, settings access
- [ ] Add proper app icon (replace default ic_launcher)
- [ ] Add onboarding tutorial for first-time users

### Games (Core Product)
- [ ] Design first mini game (e.g., reaction speed, memory match, trivia)
- [ ] Implement game engine / game loop
- [ ] Add scoring system
- [ ] Leaderboards (Supabase Postgrest)
- [ ] In-game currency (coins/gems)

### Monetization
- [ ] Google Play Billing integration
- [ ] In-app purchase: coins, gems, battle passes
- [ ] Rewarded ads (AdMob)
- [ ] Premium game unlocks

### Backend
- [ ] Deploy device_sessions table + edge functions
- [ ] User profile management (update username, avatar)
- [ ] Game results storage
- [ ] Leaderboard API
- [ ] Push notifications (Firebase Cloud Messaging)

### Polish
- [ ] Haptic feedback on game interactions
- [ ] Sound effects library
- [ ] Animated transitions between screens
- [ ] Custom app icon + adaptive icon
- [ ] Play Store listing assets (screenshots, feature graphic)
- [ ] Privacy policy + Terms of Service pages

### Release
- [ ] Internal testing track on Play Store
- [ ] Closed beta testing
- [ ] Open beta
- [ ] Production release

---

## Setup Guide

### Prerequisites
- Android Studio (Quail 1 or later)
- JDK 17
- Android SDK 36

### Steps

1. **Clone the repo**
   ```bash
   git clone <repo-url>
   cd Vexium-xi
   ```

2. **Keystore** — place `vexium-release-key.keystore` in project root

3. **Supabase** — update `Constants.kt`:
   ```kotlin
   const val SUPABASE_URL = "your-url"
   const val SUPABASE_ANON_KEY = "your-key"
   const val GOOGLE_WEB_CLIENT_ID = "your-web-client-id"
   ```

4. **Google OAuth** — register Android client in Google Cloud Console:
   - Package: `lux.obris.app`
   - SHA-1: from your release keystore

5. **Splash sound** — replace `res/raw/glitch.mp3` with your 2-second glitch sound

6. **Build**
   ```bash
   ./gradlew assembleDebug
   ```

7. **Run** — install on device in landscape mode

---

## License

Private repository. All rights reserved.

---

*Built with ❤️ by the Obris team — Kotlin, Compose, Supabase.*
