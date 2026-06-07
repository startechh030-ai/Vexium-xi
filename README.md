# 🎮💎 Vexium — Starter Template

A production-ready Jetpack Compose Android starter template pre-configured with modern Android libraries. **Zero Gradle headaches — just open and start coding!**

## ✅ What's Pre-Configured

| Library | Version | Purpose |
|---------|---------|---------|
| **AGP** | 8.13.2 | Android Gradle Plugin |
| **Kotlin** | 2.3.21 | Language |
| **Compose BOM** | 2026.05.00 | UI Framework (manages all Compose lib versions) |
| **Material 3** | Latest via BOM | Design System |
| **Hilt** | 2.58 | Dependency Injection |
| **KSP** | 2.3.9 | Annotation Processing (replaces kapt) |
| **Navigation Compose** | 2.9.7 | Type-safe Navigation |
| **Room** | 2.8.4 | Local Database |
| **Retrofit** | 3.0.0 | HTTP Client |
| **OkHttp** | 5.3.2 | HTTP Engine + Logging |
| **Kotlinx Serialization** | 1.10.0 | JSON Parsing |
| **Coil 3** | 3.2.0 | Image Loading |
| **Coroutines** | 1.10.2 | Async Programming |
| **DataStore** | 1.1.7 | Preferences Storage |
| **Lottie** | 6.7.2 | Animations |
| **Lifecycle** | 2.10.0 | ViewModel + Lifecycle |
| **Splash Screen** | 1.0.1 | Splash Screen API |
| **Gradle** | 8.13 | Build System |

## 📁 Project Structure

```
lux.vexium.app/
├── VexiumApp.kt                    # @HiltAndroidApp Application class
├── MainActivity.kt                 # Single Activity + Compose + Splash
├── core/
│   ├── common/
│   │   ├── Resource.kt             # Loading/Success/Error wrapper
│   │   ├── UiEvent.kt              # One-time UI events
│   │   ├── Constants.kt            # App constants
│   │   └── Extensions.kt           # Useful extension functions
│   ├── navigation/
│   │   ├── NavGraph.kt             # Navigation host + routes
│   │   ├── Screen.kt               # Type-safe route definitions
│   │   └── BottomNavBar.kt         # Bottom navigation bar
│   └── theme/
│       ├── Theme.kt                # Dark gaming theme
│       ├── Color.kt                # Neon color palette
│       └── Type.kt                 # Typography
├── di/
│   ├── AppModule.kt                # App-level DI
│   └── NetworkModule.kt            # Retrofit + OkHttp DI
├── data/
│   └── local/
│       └── PreferencesManager.kt   # DataStore preferences
└── feature/
    ├── home/presentation/          # Home/Dashboard screen
    ├── games/presentation/         # Games hub screen
    ├── nft/presentation/           # NFT gallery screen
    ├── trade/presentation/         # Trading screen
    └── profile/presentation/       # Profile screen
```

## 🚀 Getting Started

1. **Open in Android Studio** (Quail 1 / Panda 4 recommended)
2. **Wait for Gradle sync** to complete
3. **Run the app** — you'll see the full UI with bottom navigation
4. **Start building your features!**

## 🏗️ Architecture

- **Clean Architecture** — Presentation → Domain → Data layers
- **MVVM** — ViewModels + Compose state management
- **Type-safe Navigation** — Using Kotlin Serialization routes
- **Version Catalog** — All versions managed in `gradle/libs.versions.toml`

## 🎨 Theme

Dark gaming theme with neon accents:
- **Background:** Deep navy (#0A0E1A)
- **Primary:** Neon Cyan (#00F5FF)
- **Secondary:** Neon Purple (#BB86FC)
- **Tertiary:** Neon Green (#00FF85)
- **Accent:** Neon Orange (#FF6B35)

## 📦 Adding New Features

Each feature follows this structure:
```
feature/your_feature/
├── data/
│   ├── remote/YourApi.kt
│   ├── local/YourDao.kt
│   └── repository/YourRepositoryImpl.kt
├── domain/
│   ├── model/YourModel.kt
│   ├── repository/YourRepository.kt
│   └── usecase/YourUseCase.kt
└── presentation/
    ├── YourScreen.kt
    ├── YourViewModel.kt
    └── components/
```

## ⚡ Key Version Compatibility Notes

- **Kotlin 2.3.21 + AGP 8.13.2** — Stable, well-tested combo
- **KSP 2.3.9** — Compatible with Kotlin 2.3.x
- **Hilt 2.58** — Last version supporting AGP 8.x (use 2.59+ if upgrading to AGP 9)
- **Compose BOM 2026.05.00** — Latest stable, manages all Compose versions
- Uses **KSP** instead of kapt for faster builds
