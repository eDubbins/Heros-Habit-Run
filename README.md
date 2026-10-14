# 🗡️ Hero's Habit Run

[![Android SDK](https://img.shields.io/badge/Android%20SDK-API%2036%20(Android%2016)-brightgreen.svg)](https://developer.android.com)
[![Language](https://img.shields.io/badge/Kotlin-2.0+-purple.svg)](https://kotlinlang.org)
[![UI Framework](https://img.shields.io/badge/Jetpack%20Compose-BOM-blue.svg)](https://developer.android.com/jetpack/compose)
[![Architecture](https://img.shields.io/badge/Architecture-MVVM%20%7C%20Offline--First-orange.svg)]()
[![Database](https://img.shields.io/badge/Storage-Room%20SQLite-red.svg)](https://developer.android.com/training/data-storage/room)
[![Testing](https://img.shields.io/badge/Testing-Robolectric%20%7C%20Roborazzi-lightgrey.svg)]()

> **An offline-first, retro 8-bit roguelike habit & mental well-being tracker built with modern Android, Jetpack Compose, Room SQLite, and Kotlin Coroutines.**

---

## 📌 Executive Summary & Motivation

Standard habit trackers tend to suffer from two fundamental problems:
1. **The Checkbox Fatigue:** Treating daily habits as endless checklists creates friction and boredom without emotional connection.
2. **The Looming Anxiety Void:** Hard, stressful tasks or persistent worries are typically avoided in traditional task managers, compounding personal anxiety over time. Furthermore, conventional trackers don't inform you *which* habits actually improve your state of mind.

**Hero's Habit Run** was developed to bridge behavioral psychology with classic gaming mechanics:
- Every day is treated as a **Roguelike "Run"** where consistency yields XP, levels, and streak resilience.
- Lingering tasks and anxieties are treated as **"Suffering Inventory" (monsters/dragons)** that dynamically accrue **Suffering Value (SV)** over time—the longer an obstacle has weighed on you, the more triumph and XP you earn for finally slaying it.
- A built-in **Habit-Mood Correlation Engine** statistically measures the lagged impact of previous-day habits on subsequent-day emotional well-being ($M_t \cdot H_{h, t-1} / N$), providing empirical data on what routines genuinely help you thrive.
- Built **100% offline-first with zero accounts or third-party telemetry**, guaranteeing complete privacy for personal mental reflections and sub-second cold starts.

---

## 🎮 Core Features

### 1. ⚔️ The Tavern (Daily Quest & Habit Loop)
- **Daily Quests:** Check off habits with instant visual and audio feedback, earning XP and progression towards character leveling.
- **Morning Check-In Questionnaire:** Prompts you at the start of each new day to reflect on yesterday's mood (`Great`, `Meh`, or `Hard`) and record notable events before starting today's run.
- **Dynamic Habit Weighting:** Habits are dynamically annotated with their statistical correlation score to your happiness.

### 2. 🐉 The Suffering Inventory ("Slay Your Dragons")
- Add lingering worries, dread-inducing tasks, or mental burdens with classified severity (`Small`, `Medium`, `Large`).
- **Compound Suffering Value (SV):**
  $$\text{Small: } 10 + (\Delta\text{days} \times 1.5)$$
  $$\text{Medium: } 25 + (\Delta\text{days} \times 3.0)$$
  $$\text{Large (Boss): } 50 \text{ base XP}$$
  Conquering long-delayed tasks yields high-tier reward spikes, incentivizing you to tackle procrastination head-on.

### 3. 🛡️ The Closet (Character Customization & Progression)
- Retro 8-bit hero avatar rendered procedurally using an internal **Canvas Pixel-Matrix Engine**.
- Customizable palettes (tunic colors, secondary accents, accessory hues) and title badges as you advance levels through accrued XP.

### 4. 🪦 The Morgue (History & Analytics)
- Comprehensive ledger of all historical daily runs.
- Graveyard of conquered suffering items, recording date of victory and final XP claimed.
- Historical trend analysis correlating routine consistency with emotional resilience.

### 5. 🕹️ Tactile Retro Gamepad Interface
- Includes a functional on-screen virtual NES D-Pad, Start/Select buttons, and tactile action controls for one-handed retro-styled navigation alongside standard touch gestures.

---

## 🧠 Algorithmic Highlights

### Habit-Mood Correlation Engine
Rather than relying on subjective intuition, the app computes empirical correlation between habit adherence at day $t-1$ and reported mood state at day $t$:

$$\text{Correlation}(h) = \frac{1}{N} \sum_{t=1}^{N} \left( \text{Mood}_t \times \text{Completed}_{h, t-1} \right)$$

*Where:*
- $\text{Mood}_t \in \{-1 \text{ (Hard)}, 0 \text{ (Meh)}, +1 \text{ (Great)}\}$
- $\text{Completed}_{h, t-1} \in \{0, 1\}$

The resulting value highlights which habits reliably precede positive mood days and flags routines that may be correlated with burnout or fatigue.

---

## 🏗️ Architecture & Engineering Design

The project follows Google's recommended **Clean Architecture & Android Jetpack** principles:

```
┌────────────────────────────────────────────────────────┐
│                   UI Layer (Jetpack Compose)           │
│  MainLayout.kt  │  ZeldaSprites.kt  │  RetroComponents │
└───────────────────────────▲────────────────────────────┘
                            │ StateFlow / Events
┌───────────────────────────┴────────────────────────────┐
│                  ViewModel Layer (MVVM)                │
│  MainViewModel.kt (StateFlow streams, coroutine scope) │
└───────────────────────────▲────────────────────────────┘
                            │ Suspended calls / Flows
┌───────────────────────────┴────────────────────────────┐
│                    Repository Layer                    │
│  AppRepository.kt (Single Source of Truth)             │
└───────────────────────────▲────────────────────────────┘
                            │ Room DAOs
┌───────────────────────────┴────────────────────────────┐
│                    Local Database                      │
│  Room SQLite (habits, suffering_inventory, daily_runs) │
└────────────────────────────────────────────────────────┘
```

### Key Technical Decisions:
- **Declarative UI with Zero Asset Bloat:** Custom retro Zelda sprites (`ZeldaSprites.kt`) and double-bordered NES panels are rendered directly on Jetpack Compose `Canvas` using discrete color mappings. This avoids heavy raster asset downloads and ensures crisp scaling at any DPI.
- **Unidirectional Data Flow (UDF):** The UI observes immutable `StateFlow` streams exposed by `MainViewModel` with lifecycle-aware subscriptions (`SharingStarted.WhileSubscribed(5000)`).
- **Offline-First Room Persistence:** Complete local database caching with transactional SQLite operations. Schema changes and queries are verified compile-time using **KSP (Kotlin Symbol Processing)**.
- **Asynchronous Execution:** Heavy analytical calculations (correlation matrices, day-difference parsing) run asynchronously within `viewModelScope` using Kotlin Coroutines.

---

## 💻 Tech Stack

| Domain | Technology / Library |
|---|---|
| **Language** | Kotlin 2.0+ (100%) |
| **Minimum SDK** | API 24 (Android 7.0 Nougat) |
| **Target / Compile SDK** | API 36 (Android 16 / Vanilla Ice Cream) |
| **UI Toolkit** | Jetpack Compose (BOM 2024.x) & Material 3 |
| **Architecture** | MVVM, Repository Pattern, Clean Architecture |
| **Concurrency** | Kotlin Coroutines & Asynchronous Flows (`StateFlow`, `SharedFlow`) |
| **Local Storage** | Android Room Database (SQLite) with KSP |
| **Testing** | JUnit 4, AndroidX Test, Robolectric, Roborazzi Screenshot Testing |
| **Build System** | Gradle Kotlin DSL (`build.gradle.kts`), Gradle Version Catalogs (`libs.versions.toml`) |

---

## 📂 Project Structure

```
Hero's-Habit-Run/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/
│   │   │   │   ├── MainActivity.kt            # Edge-to-edge entry point
│   │   │   │   ├── data/                      # Persistence layer
│   │   │   │   │   ├── AppDatabase.kt         # Room database singleton
│   │   │   │   │   ├── Daos.kt                # Reactive DAO queries
│   │   │   │   │   ├── Entities.kt            # Habit, Run, Profile & Suffering entities
│   │   │   │   │   └── Repository.kt          # Clean repository interface
│   │   │   │   ├── ui/                        # Presentation layer
│   │   │   │   │   ├── MainLayout.kt          # Compose UI, tabs, dialogs & controller
│   │   │   │   │   ├── MainViewModel.kt       # StateFlow logic, math & correlation
│   │   │   │   │   ├── ZeldaSprites.kt        # Canvas procedural 8-bit sprite generator
│   │   │   │   │   └── theme/                 # NES retro color palettes & typography
│   │   │   │   └── AndroidManifest.xml
│   │   │   └── res/                           # App icons, strings, and launch vectors
│   │   └── test/                              # Unit & Robolectric test suite
│   │       ├── java/com/example/
│   │       │   ├── MainActivityTest.kt        # Robolectric Activity lifecycle tests
│   │       │   ├── ExampleRobolectricTest.kt  # Context and string resource tests
│   │       │   └── GreetingScreenshotTest.kt  # Roborazzi screenshot verification
│   │       └── screenshots/
│   └── build.gradle.kts                       # App module build configuration
├── gradle/
│   └── libs.versions.toml                     # Centralized version catalog
├── build.gradle.kts                           # Root build script
├── settings.gradle.kts
└── README.md
```

---

## 🛠️ Building & Running Locally

### Prerequisites
- **Android Studio Ladybug (2024.2+)** or newer.
- **JDK 17 or JDK 21**.
- Android SDK with platform tools for **API 36** (or an emulator running API 26+).

### Steps
1. **Clone the repository:**
   ```bash
   git clone https://github.com/eDubbins/Heros-Habit-Run.git
   cd Heros-Habit-Run
   ```

2. **Open in Android Studio:**
   - Select **File > Open...** and choose the `Heros-Habit-Run` root directory.
   - Allow Gradle to sync dependencies defined in `gradle/libs.versions.toml`.

3. **Build the Debug APK via CLI:**
   ```bash
   # On Windows (PowerShell)
   .\gradlew assembleDebug

   # On macOS / Linux
   ./gradlew assembleDebug
   ```

4. **Run Unit and Robolectric Tests:**
   ```bash
   .\gradlew test
   ```

---

## 🧪 Testing Strategy

Quality assurance and UI reliability are verified through multi-layered testing:
- **Unit Tests:** Verify business logic, day difference calculations, and suffering value accrual algorithms.
- **Robolectric Integration Tests:** Validate Activity initialization, context injection, and lifecycle state management without requiring an attached physical emulator.
- **Roborazzi Screenshot Tests:** Guarantee that retro canvas drawing patterns maintain pixel accuracy across layout updates.

---

## 👨‍💻 About the Author

Built by **Eric (eDubbins)** as an exploration of gamified behavioral design, algorithmic habit optimization, and production-grade offline-first Android architecture.

- **GitHub:** [@eDubbins](https://github.com/eDubbins)
- **LinkedIn:** [Connect on LinkedIn](https://www.linkedin.com) *(Update with your direct link)*

---

## 📄 License
This project is open source and available under the [MIT License](LICENSE).
