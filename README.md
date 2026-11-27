# BibleLib: Multi-Bible Reader

A clean, offline-first Bible reader built for focused study. Explore a number of translations side by side, search instantly, personalize your reading experience, and keep everything organized with bookmarks, notes, reading history, and reusable scripture lists for sermons, studies, or personal devotions.

---

## Features

READ YOUR WAY
- Download multiple Bible translations and switch between them freely
- Parallel reading — view your primary translation alongside other translations side by side
- Jump between books and chapters with a simple, fast navigator
- Your last-read verse is remembered automatically so you always pick up where you left off

FIND ANY VERSE FAST
- Full-text search across your downloaded Bible
- Recent searches saved for quick access
- Look up a specific reference directly — book, chapter, and verse — with
  quick-open shortcuts
- Save frequently used passages into custom scripture lists you can reopen
  anytime, and queue up several passages to read through in one sitting

BOOKMARKS, HIGHLIGHTS & NOTES
- Tap to select one or more verses and highlight them in a color of your
  choice
- Bookmark verses for quick recall
- Attach personal notes to any verse
- Browse and manage all your bookmarks and notes in one place

HISTORY
- Automatic reading history, grouped by date, so you can retrace your steps
- Full search history alongside your reading history

MAKE IT YOURS
- 10 reader background themes, from soft parchment and sepia tones to
  dark options like Night, Charcoal, and Forest
- Multiple font choices
- Adjustable font size for comfortable reading at any distance

BUILT FOR OFFLINE USE
- Download a Bible once, then read, search, and study without an internet
  connection
- Download progress, retry, and re-download controls if a download is
  interrupted

---

## Screenshots

<table>
  <tr>
    <td><img src="screenshots/image1.jpg" width="200px" /></td>
    <td><img src="screenshots/image2.jpg" width="200px" /></td>
    <td><img src="screenshots/image3.jpg" width="200px" /></td>
  </tr>
  <tr>
    <td><img src="screenshots/image4.jpg" width="200px" /></td>
    <td><img src="screenshots/image5.jpg" width="200px" /></td>
    <td><img src="screenshots/image6.jpg" width="200px" /></td>
  </tr>
</table>

---

## Table of contents

- [Tech stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Project structure](#project-structure)
- [Architecture overview](#architecture-overview)
    - [Module graph](#module-graph)
    - [Core modules](#core-modules)
    - [Feature modules](#feature-modules)
    - [Navigation](#navigation)
    - [Data flow and downloads](#data-flow-and-downloads)
- [Getting started](#getting-started)
    - [1. Clone the repository](#1-clone-the-repository)
    - [2. Create local.properties](#2-create-localproperties)
    - [3. Run the app](#3-run-the-app)
    - [4. Release builds (optional)](#4-release-builds-optional)
- [Contributing](#contributing)

---

## Tech stack

| Concern                        | Library / tool                       |
|--------------------------------|--------------------------------------|
| UI                             | Jetpack Compose, Material 3          |
| Architecture                   | MVVM, multi-module Gradle            |
| Dependency injection           | Hilt                                 |
| Local database                 | Room (schema version 4)              |
| Networking                     | Retrofit 2 + OkHttp                  |
| Background downloads           | WorkManager (Hilt-integrated worker) |
| Error & performance monitoring | Sentry                               |
| Min SDK                        | 26 (Android 8.0)                     |
| Target / Compile SDK           | 37                                   |

---

## Prerequisites

Before you start, make sure you have the following:

- **Android Studio** Hedgehog (2023.1.1) or newer
- **JDK 17** — required by the `build-logic` convention plugins
- **Android SDK** with API 26–37 installed (the SDK Manager inside Android Studio handles this)

---

## Project structure

```
BibleLib/
├── app/                             # Application shell — entry point, navigation, top-level DI
│   └── src/main/java/com/biblelib/
│       ├── MainActivity.kt
│       ├── BibleLibApp.kt
│       ├── app/di/AppConfigModule.kt
│       └── app/navigation/AppNavHost.kt
│
├── build-logic/                     # Shared Gradle convention plugins (Hilt, Compose, library, feature)
│
├── core/
│   ├── common/                      # Routes, ApiConstants, PrefConstants, UiState, shared data classes — no Android deps beyond parcelize
│   ├── data/                        # Repositories, PrefsRepo, ThemeRepo, SyncWorker, SyncScheduler
│   ├── database/                    # Room database, all DAOs and entity models
│   ├── design_system/                # Material 3 theme, colours, typography
│   ├── network/                     # Retrofit services, DTOs, NetworkModule
│   └── ui/                          # Shared Compose components (MainViewModel, AppTopBar, states, dialogs...)
│
└── feature/
    ├── selection/                   # First-launch / re-selection Bible picker
    ├── reader/                      # Chapter reader — book drawer, chapter nav, verse list
    ├── search/                      # Full-text verse search + recent search history
    ├── history/                     # Reading history
    ├── settings/                    # Theme, font size, app settings
    └── help/                        # Help screen
```

---

## Architecture overview

### Module graph

Feature modules depend only on `core` modules and never on each other. All cross-feature navigation
lives in `app`.

```
app
 ├── core:common
 ├── core:data          → core:database, core:network, core:common
 ├── core:design_system
 ├── core:ui             → core:common, core:data, core:design_system
 └── feature:*           → core:common, core:data, core:database, core:design_system, core:ui
```

### Core modules

**`core:common`** — pure Kotlin/Parcelize. Holds `Routes` (all navigation route strings, including
argument encoding for the reader and payment WebView), `ApiConstants`, `PrefConstants`, `AppFonts`,
`UiState` (sealed interface: `Loading`, `Loaded`, `Saving`, `Saved`, `Error`), shared data classes (
`BibleInfo`, `BibleBook`, `BibleChapter`, `VerseDisplay`, `ReadingHistory`, `SearchResult`), and
`NetworkUtils`.

**`core:data`** — all data-access logic sitting above the DB and network layers:

- `PrefsRepo` — a strongly typed `SharedPreferences` wrapper covering install date, selected/primary
  Bible, last-read position, theme mode, font size timestamp, last-sync timestamp, and
  `resetAppData()` for clearing selection state.
- `ThemeRepo` — a `@HiltViewModel` that exposes the current `ThemeMode` (`SYSTEM` / `LIGHT` /
  `DARK`) as Compose state and persists changes via `PrefsRepo`.
- `BibleRepo` — fetches available Bibles, books, chapters, and verses from the BibleLib content API,
  parses the nested verse-content JSON into flat `VerseDisplay` lists, and persists everything to
  Room. Also serves local reads and in-database verse search.
- `TrackingRepo` — records reading history and search-query history to Room, pruning old entries.
- `DonationRepo` — initializes a Paystack transaction (amount, donor email/name, callback URL) and
  returns the authorization URL for the in-app WebView checkout.
- `SyncWorker` / `BibleSyncWorkerFactory` / `SyncScheduler` — a Hilt-integrated `CoroutineWorker`
  that downloads a secondary Bible translation in the background with a foreground progress
  notification, scheduled via WorkManager with network constraints and exponential backoff.

**`core:database`** — Room database (`AppDatabase`, version 4) with six entities: `BibleEntity`,
`BookEntity`, `ChapterEntity`, `VerseEntity`, `HistoryEntity`, `SearchEntity`, and their DAOs.

**`core:network`** — `NetworkModule` (Hilt) wires up two Retrofit instances: one for the BibleLib
content API (`BibleLibService`, serving `info.json`, `books.json`, `chapters.json`, and per-book
verse JSON) and one for the Paystack API (`PaystackService`, transaction initialization).

**`core:design_system`** — Material 3 theme, colour palette, and typography scale.

**`core:ui`** — `MainViewModel` (determines the app's start destination) plus shared Compose
components: `AppTopBar`, `EmptyState`, `ErrorState`, loading indicators, `DonationBanner`,
`AppDialogs`, auto-sizing text utilities, `PageCurlEffect`, `CornerNavZone`, and
`ThemeSelectorDialog`.

### Feature modules

| Module              | Screens                                                                         | ViewModels           |
|---------------------|---------------------------------------------------------------------------------|----------------------|
| `feature:selection` | Bible selection (first launch or re-selection)                                  | `SelectionViewModel` |
| `feature:reader`    | Chapter reader (book drawer, chapter nav bar, verse list, Bible selector sheet) | `ReaderViewModel`    |
| `feature:search`    | Verse search, recent search history                                             | `SearchViewModel`    |
| `feature:history`   | Reading history                                                                 | `HistoryViewModel`   |
| `feature:settings`  | Theme and font-size settings                                                    | `SettingsViewModel`  |
| `feature:help`      | Help                                                                            | —                    |

All ViewModels are `@HiltViewModel`-annotated and follow the same pattern: `StateFlow` for UI
state (observed with `collectAsState()` in the composable), coroutines on `viewModelScope` doing
database/network work on `Dispatchers.IO`.

### Navigation

Navigation is handled by a single `NavHostController` in `AppNavHost.kt` at the `app` level. Route
strings are constants in `Routes` (`core:common`); the reader route carries `bibleAbbr`, `bookId`,
and `chapterId` as query arguments, and the payment WebView route carries a URL-encoded redirect
URL.

`MainViewModel` determines the start destination at launch by reading `PrefsRepo`: if the user
hasn't completed Bible selection (or asked to re-select), it routes to `SELECTION`; otherwise it
routes straight to `READER`.

```
SELECTION ──► READER ──┬──► SEARCH
                        ├──► HISTORY
                        ├──► SETTINGS
                        └──► HELP
```

### Data flow and downloads

When a user selects a Bible in `feature:selection`, `BibleRepo.downloadBible()` runs the full fetch
pipeline: books → chapters → verses (per book), converting the API's nested verse-content JSON into
flat `VerseDisplay` rows and writing everything to Room. Selecting a second (secondary) translation
later schedules the same download as a background job — `SyncScheduler.scheduleSecondaryDownload()`
enqueues `SyncWorker` via WorkManager with a network constraint, exponential backoff, and a
foreground notification that reports per-book download progress.

Once a translation is downloaded, `feature:reader` reads purely from Room — no network calls are
needed to read, browse, or search within an already-downloaded Bible.

---

## Getting started

### 1. Clone the repository

```bash
git clone git@github.com:SiroDevs/BibleLib.git
cd BibleLib
```

### 2. Run the app

Open the project in Android Studio. Gradle sync will run automatically. Once it completes, select
the `debug` build variant and run the `app` configuration on a device or emulator running API 26 or
higher.

```bash
# Or from the command line:
./gradlew :app:installDebug
```

The `debug` build variant uses the application ID `com.biblelib.dev`, so it installs alongside a
production build without conflict.

On first launch the app walks through Bible selection, then downloads the chosen translation(s) from
the live content API. You need a network connection for this initial download — after that, reading,
browsing, and searching that translation work fully offline.

### 3. Release builds (optional)

Release signing is configured in `app/build.gradle.kts` and reads
`app/keystore/key.properties`. This is only needed for release builds.

Create `app/keystore/key.properties`:

```properties
KEY_ALIAS=your_key_alias
KEY_PASSWORD=your_key_password
KEYSTORE_PASSWORD=your_store_password
STORE_FILE=release.jks
```

Then build:

```bash
./gradlew :app:bundleRelease    # AAB for Play Store
./gradlew :app:assembleRelease  # APK for direct install
```

### 4. GitHub Actions production release

The workflow in `.github/workflows/release-android.yml` runs on pushes to
`main` or manually from **Actions**. It compares `versionName` and
`versionCode` in `app/build.gradle.kts` with the latest GitHub release. A new
version builds a signed APK and AAB, uploads the AAB to the Play Store
production track, then creates a GitHub Release containing the APK. Play
review and processing still apply.

Before the first run:

1. Add these as repository secrets under **Settings → Secrets and variables →
   Actions**. The four Android signing values are in the ignored local file
   `app/keystore/signing-secrets.env`; copy them without committing that file.

   | Secret | Value |
   |---|---|
   | `ANDROID_KEYSTORE_BASE64` | Base64 value from `signing-secrets.env` |
   | `ANDROID_KEYSTORE_PASSWORD` | Keystore password from `signing-secrets.env` |
   | `ANDROID_KEY_ALIAS` | Key alias from `signing-secrets.env` |
   | `ANDROID_KEY_PASSWORD` | Private key password from `signing-secrets.env` |
   | `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON` | Google Play service account JSON key |
   | `PAYSTACK_SECRET_KEY` | Optional; needed for live Paystack donations |
   | `SENTRY_AUTH_TOKEN` | Optional; enables Sentry source and mapping uploads |

2. Enable the Google Play Android Developer API. Add the service account to
   Play Console and grant it permission to release `com.biblelib` to
   production.
3. Ensure the `com.biblelib` app exists in Play Console and Play App Signing
   is enabled. If this is the replacement keystore for a lost upload key,
   request an upload key reset using `app/keystore/upload_certificate.pem` and
   wait until the reset is active. The Android secrets above must match the
   active upload key.
4. Increase both `versionName` and `versionCode` for each release. The
   workflow skips versions that are not newer than the latest GitHub release
   and fails if the build number is not higher than the previous release.
   If earlier Play releases were made outside GitHub Releases, make sure the
   Gradle version and code are also higher than the current Play production
   release before the first workflow run.

The base64, keystore, and properties files stay local and are excluded by
`.gitignore`.

---

## Contributing

1. Fork the repository and create a feature branch off `develop`.
2. New screens belong in a `feature` module. If you're adding a genuinely new section of the app,
   create a new feature module following the same `build.gradle.kts` structure as the existing ones.
3. Shared UI components go in `core:ui`. Logic that multiple features need goes in a repository in
   `core:data`.
4. All ViewModels must be `@HiltViewModel`-annotated. All repositories must be `@Singleton`-scoped.
5. Open a pull request with a clear description and reference any related issue.

For questions, open a GitHub issue.

**License:** MIT — feel free to use, modify, and distribute.
